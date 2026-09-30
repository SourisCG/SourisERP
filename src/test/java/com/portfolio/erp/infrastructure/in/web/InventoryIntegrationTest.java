package com.portfolio.erp.infrastructure.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;
import com.portfolio.erp.domain.model.InventoryItem;
import com.portfolio.erp.domain.ports.in.InventoryUseCase;
import com.portfolio.erp.support.AbstractIntegrationTest;

class InventoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    InventoryUseCase inventory;

    @Test
    void stock_adjustment_transfer_and_movements_ledger() throws Exception {
        String token = login("admin", "admin123");
        long categoryId = createCategory(token, "Inventory Cat");
        long productId = createProduct(token, categoryId, "INV-001");
        long warehouseA = createWarehouse(token, "WH-A");
        long warehouseB = createWarehouse(token, "WH-B");

        mockMvc.perform(post("/api/v1/inventory/adjustments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":%d,"warehouseId":%d,"newQuantity":10,"reason":"Opening stock"}
                                """.formatted(productId, warehouseA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.availableQuantity").value(10));

        mockMvc.perform(post("/api/v1/inventory/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":%d,"fromWarehouseId":%d,"toWarehouseId":%d,"quantity":4}
                                """.formatted(productId, warehouseA, warehouseB)))
                .andExpect(status().isNoContent());

        MvcResult inventoryList = mockMvc.perform(get("/api/v1/inventory")
                        .param("productId", String.valueOf(productId))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andReturn();
        List<Integer> quantities = JsonPath.read(inventoryList.getResponse().getContentAsString(), "$[*].quantity");
        assertThat(quantities).containsExactlyInAnyOrder(6, 4);

        mockMvc.perform(post("/api/v1/inventory/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":%d,"fromWarehouseId":%d,"toWarehouseId":%d,"quantity":5}
                                """.formatted(productId, warehouseB, warehouseA)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("error.inventory.insufficientStock"));

        mockMvc.perform(get("/api/v1/inventory/movements").param("productId", String.valueOf(productId))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.type=='TRANSFER_OUT')]").exists())
                .andExpect(jsonPath("$.content[?(@.type=='TRANSFER_IN')]").exists())
                .andExpect(jsonPath("$.content[?(@.type=='ADJUSTMENT')]").exists());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/v1/warehouses/" + warehouseA)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("error.warehouse.hasStock"));
    }

    @Test
    void concurrent_reservations_do_not_oversell_the_last_unit() throws Exception {
        String token = login("admin", "admin123");
        long categoryId = createCategory(token, "Concurrency Cat");
        long productId = createProduct(token, categoryId, "RACE-001");
        long warehouseId = createWarehouse(token, "WH-RACE");

        inventory.adjust(productId, warehouseId, 1, "seed for concurrency test");

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<Boolean> reserveLastUnit = () -> {
            start.await();
            try {
                inventory.reserve(productId, warehouseId, 1, "CONCURRENCY_TEST", null);
                return true;
            } catch (Exception ex) {
                return false;
            }
        };

        Future<Boolean> first = pool.submit(reserveLastUnit);
        Future<Boolean> second = pool.submit(reserveLastUnit);
        start.countDown();
        List<Boolean> results = List.of(
                first.get(30, TimeUnit.SECONDS),
                second.get(30, TimeUnit.SECONDS));
        pool.shutdown();

        assertThat(results).containsExactlyInAnyOrder(true, false);

        List<InventoryItem> items = inventory.list(warehouseId, productId, null);
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getQuantity()).isEqualTo(1);
        assertThat(items.get(0).getReservedQuantity()).isEqualTo(1);
        assertThat(items.get(0).available()).isZero();
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    private long createCategory(String token, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s"}
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private long createProduct(String token, long categoryId, String sku) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"%s","name":"Product %s","categoryId":%d,"unit":"UNIT",
                                 "salePrice":10.00,"costPrice":5.00}
                                """.formatted(sku, sku, categoryId)))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private long createWarehouse(String token, String code) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/warehouses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"%s","name":"Warehouse %s"}
                                """.formatted(code, code)))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }
}
