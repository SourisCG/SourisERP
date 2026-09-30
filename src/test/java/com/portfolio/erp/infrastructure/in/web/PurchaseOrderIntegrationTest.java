package com.portfolio.erp.infrastructure.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;
import com.portfolio.erp.support.AbstractIntegrationTest;

class PurchaseOrderIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void receive_flow_adds_stock_and_refreshes_product_cost() throws Exception {
        String token = login("admin", "admin123");
        long categoryId = createCategory(token, "Purchasing Cat");
        long productId = createProduct(token, categoryId, "PUR-001");
        long warehouseId = createWarehouse(token, "WH-PUR");
        long supplierId = createSupplier(token, "PUX-001", "Global Supplies");

        MvcResult created = mockMvc.perform(post("/api/v1/purchase-orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierId":%d,
                                  "lines":[{"productId":%d,"warehouseId":%d,"quantity":10,"unitCost":7.50}]
                                }
                                """.formatted(supplierId, productId, warehouseId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.number").value(org.hamcrest.Matchers.startsWith("PO-")))
                .andExpect(jsonPath("$.subtotal").value(75.00))
                .andReturn();
        long orderId = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(post("/api/v1/purchase-orders/" + orderId + "/receive")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("error.purchaseOrder.invalidStatus"));

        mockMvc.perform(post("/api/v1/purchase-orders/" + orderId + "/approve")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(post("/api/v1/purchase-orders/" + orderId + "/receive")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"));

        mockMvc.perform(get("/api/v1/inventory").param("productId", String.valueOf(productId))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity").value(10));

        mockMvc.perform(get("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costPrice").value(7.50));

        mockMvc.perform(post("/api/v1/purchase-orders/" + orderId + "/cancel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("error.purchaseOrder.invalidStatus"));
    }

    @Test
    void draft_order_can_be_cancelled() throws Exception {
        String token = login("admin", "admin123");
        long categoryId = createCategory(token, "Cancelling Cat");
        long productId = createProduct(token, categoryId, "CAN-001");
        long warehouseId = createWarehouse(token, "WH-CAN");
        long supplierId = createSupplier(token, "PUX-002", "Cancel Supplies");

        MvcResult created = mockMvc.perform(post("/api/v1/purchase-orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"supplierId":%d,"lines":[{"productId":%d,"warehouseId":%d,"quantity":1,"unitCost":1.00}]}
                                """.formatted(supplierId, productId, warehouseId)))
                .andExpect(status().isCreated())
                .andReturn();
        long orderId = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(post("/api/v1/purchase-orders/" + orderId + "/cancel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
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
                                 "salePrice":20.00,"costPrice":5.00,"taxRate":21}
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

    private long createSupplier(String token, String code, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/suppliers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"%s","name":"%s","email":"%s@example.com"}
                                """.formatted(code, name, code.toLowerCase())))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }
}
