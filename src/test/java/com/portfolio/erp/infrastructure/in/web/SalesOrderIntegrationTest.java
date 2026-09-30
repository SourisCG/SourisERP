package com.portfolio.erp.infrastructure.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;
import com.portfolio.erp.support.AbstractIntegrationTest;

class SalesOrderIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void order_lifecycle_reserves_and_releases_stock() throws Exception {
        String token = login("admin", "admin123");
        long categoryId = createCategory(token, "Sales Cat");
        long productId = createProduct(token, categoryId, "SALE-001", "100.00", "21");
        long warehouseId = createWarehouse(token, "WH-SALES");
        long customerId = createCustomer(token, "SOX-001", "ACME Corporation");

        adjustStock(token, productId, warehouseId, 10);

        MvcResult created = mockMvc.perform(post("/api/v1/sales-orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId":%d,
                                  "notes":"First demo order",
                                  "lines":[{"productId":%d,"warehouseId":%d,"quantity":3}]
                                }
                                """.formatted(customerId, productId, warehouseId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.number").value(org.hamcrest.Matchers.startsWith("SO-")))
                .andExpect(jsonPath("$.customerName").value("ACME Corporation"))
                .andExpect(jsonPath("$.subtotal").value(300.00))
                .andExpect(jsonPath("$.taxTotal").value(63.00))
                .andExpect(jsonPath("$.total").value(363.00))
                .andReturn();
        long orderId = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(post("/api/v1/sales-orders/" + orderId + "/confirm")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        assertReserved(token, productId, warehouseId, 3);

        mockMvc.perform(post("/api/v1/sales-orders/" + orderId + "/cancel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertReserved(token, productId, warehouseId, 0);
    }

    @Test
    void confirm_fails_when_stock_is_insufficient_and_order_stays_draft() throws Exception {
        String token = login("admin", "admin123");
        long categoryId = createCategory(token, "Scarce Cat");
        long productId = createProduct(token, categoryId, "SCARCE-001", "50.00", "21");
        long warehouseId = createWarehouse(token, "WH-SCARCE");
        long customerId = createCustomer(token, "SOX-002", "Scarce Buyer");

        adjustStock(token, productId, warehouseId, 1);

        MvcResult created = mockMvc.perform(post("/api/v1/sales-orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId":%d,
                                  "lines":[{"productId":%d,"warehouseId":%d,"quantity":5}]
                                }
                                """.formatted(customerId, productId, warehouseId)))
                .andExpect(status().isCreated())
                .andReturn();
        long orderId = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(post("/api/v1/sales-orders/" + orderId + "/confirm")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("error.inventory.insufficientStock"));

        mockMvc.perform(get("/api/v1/sales-orders/" + orderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void confirmed_order_cannot_be_edited() throws Exception {
        String token = login("admin", "admin123");
        long categoryId = createCategory(token, "Locked Cat");
        long productId = createProduct(token, categoryId, "LOCK-001", "10.00", "21");
        long warehouseId = createWarehouse(token, "WH-LOCK");
        long customerId = createCustomer(token, "SOX-003", "Locked Buyer");
        adjustStock(token, productId, warehouseId, 5);

        MvcResult created = mockMvc.perform(post("/api/v1/sales-orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":%d,"lines":[{"productId":%d,"warehouseId":%d,"quantity":1}]}
                                """.formatted(customerId, productId, warehouseId)))
                .andExpect(status().isCreated())
                .andReturn();
        long orderId = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(post("/api/v1/sales-orders/" + orderId + "/confirm")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/v1/sales-orders/" + orderId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":%d,"lines":[{"productId":%d,"warehouseId":%d,"quantity":2}]}
                                """.formatted(customerId, productId, warehouseId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("error.salesOrder.invalidStatus"));
    }

    private void assertReserved(String token, long productId, long warehouseId, int expectedReserved) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/inventory")
                        .param("productId", String.valueOf(productId))
                        .param("warehouseId", String.valueOf(warehouseId))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        List<Integer> reserved = JsonPath.read(result.getResponse().getContentAsString(), "$[*].reservedQuantity");
        assertThat(reserved).containsExactly(expectedReserved);
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

    private long createProduct(String token, long categoryId, String sku, String price, String tax) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"%s","name":"Product %s","categoryId":%d,"unit":"UNIT",
                                 "salePrice":%s,"costPrice":1.00,"taxRate":%s}
                                """.formatted(sku, sku, categoryId, price, tax)))
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

    private long createCustomer(String token, String code, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"%s","name":"%s","email":"%s@example.com"}
                                """.formatted(code, name, code.toLowerCase())))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private void adjustStock(String token, long productId, long warehouseId, int quantity) throws Exception {
        mockMvc.perform(post("/api/v1/inventory/adjustments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":%d,"warehouseId":%d,"newQuantity":%d,"reason":"test setup"}
                                """.formatted(productId, warehouseId, quantity)))
                .andExpect(status().isOk());
    }
}
