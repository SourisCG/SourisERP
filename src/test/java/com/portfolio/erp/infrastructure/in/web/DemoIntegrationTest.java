package com.portfolio.erp.infrastructure.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;
import com.portfolio.erp.support.AbstractIntegrationTest;

@TestPropertySource(properties = "app.demo.mode=true")
class DemoIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void demo_data_can_be_seeded_and_reset() throws Exception {
        String token = login("admin", "admin123");

        mockMvc.perform(post("/api/v1/demo/reset")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/products").param("size", "100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(50)));

        login("sales", "sales123");
        login("warehouse", "warehouse123");
        login("accountant", "accountant123");
        login("viewer", "viewer123");

        LocalDate today = LocalDate.now();
        mockMvc.perform(get("/api/v1/reports/sales-summary")
                        .param("from", today.minusMonths(11).withDayOfMonth(1).toString())
                        .param("to", today.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(6)));

        mockMvc.perform(get("/api/v1/reports/receivables")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/v1/inventory")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(50)));

        mockMvc.perform(get("/api/v1/audit")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(5)));

        mockMvc.perform(post("/api/v1/demo/reset")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
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
}
