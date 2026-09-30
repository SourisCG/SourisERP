package com.portfolio.erp.infrastructure.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;
import com.portfolio.erp.support.AbstractIntegrationTest;

class CatalogIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void category_crud_and_duplicate_conflict() throws Exception {
        String token = login("admin", "admin123");

        MvcResult created = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Electronics","description":"Devices"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();
        long categoryId = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Electronics"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("error.category.nameExists"));

        mockMvc.perform(get("/api/v1/categories").param("search", "elect")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Electronics"));

        mockMvc.perform(put("/api/v1/categories/" + categoryId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Electronics & Gadgets","description":"Devices"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Electronics & Gadgets"));

        mockMvc.perform(delete("/api/v1/categories/" + categoryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/categories/" + categoryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("error.category.notFound"));
    }

    @Test
    void product_lifecycle_with_unique_sku() throws Exception {
        String token = login("admin", "admin123");
        long categoryId = createCategory(token, "Computing");

        MvcResult created = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku":"LAP-001",
                                  "name":"Laptop Pro 14",
                                  "description":"14 inch laptop",
                                  "categoryId":%d,
                                  "unit":"UNIT",
                                  "salePrice":1499.99,
                                  "costPrice":1100.00,
                                  "taxRate":21
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("LAP-001"))
                .andExpect(jsonPath("$.categoryName").value("Computing"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.version").value(0))
                .andReturn();
        long productId = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku":"LAP-001","name":"Other","categoryId":%d,"unit":"UNIT",
                                  "salePrice":10,"costPrice":5
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("error.product.skuExists"));

        mockMvc.perform(put("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku":"LAP-001","name":"Laptop Pro 14","categoryId":%d,"unit":"UNIT",
                                  "salePrice":1399.99,"costPrice":1100.00,"taxRate":21,"active":true
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salePrice").value(1399.99))
                .andExpect(jsonPath("$.version").value(1));

        mockMvc.perform(get("/api/v1/products").param("search", "laptop")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value("LAP-001"));

        mockMvc.perform(delete("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void category_with_products_cannot_be_deleted() throws Exception {
        String token = login("admin", "admin123");
        long categoryId = createCategory(token, "Furniture");

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku":"CHR-001","name":"Office Chair","categoryId":%d,"unit":"UNIT",
                                  "salePrice":199.00,"costPrice":120.00
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/categories/" + categoryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("error.category.hasProducts"));
    }

    @Test
    void sales_role_can_read_but_not_write_catalog() throws Exception {
        String adminToken = login("admin", "admin123");

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"seller1","email":"seller1@erp.local","firstName":"Sara",
                                  "lastName":"Seller","password":"seller12345","roles":["SALES"]
                                }
                                """))
                .andExpect(status().isCreated());

        String salesToken = login("seller1", "seller12345");

        mockMvc.perform(get("/api/v1/products")
                        .header("Authorization", "Bearer " + salesToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + salesToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Forbidden Category"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("error.forbidden"));
    }

    @Test
    void product_validation_returns_field_errors() throws Exception {
        String token = login("admin", "admin123");

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"No SKU"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("error.validation"))
                .andExpect(jsonPath("$.errors").isArray());
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
}
