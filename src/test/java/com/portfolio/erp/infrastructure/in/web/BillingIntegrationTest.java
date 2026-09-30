package com.portfolio.erp.infrastructure.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;
import com.portfolio.erp.support.AbstractIntegrationTest;

class BillingIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void full_billing_flow_pdf_payments_reports_and_audit() throws Exception {
        String token = login("admin", "admin123");
        long categoryId = createCategory(token, "Billing Cat");
        long productId = createProduct(token, categoryId, "BILL-001", "100.00", "21");
        long warehouseId = createWarehouse(token, "WH-BILL");
        long customerId = createCustomer(token, "BILLCUST", "Billing Customer");
        adjustStock(token, productId, warehouseId, 10);

        long orderId = createConfirmedOrder(token, customerId, productId, warehouseId, 2);

        MvcResult invoiceResult = mockMvc.perform(post("/api/v1/invoices")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"salesOrderId":%d}
                                """.formatted(orderId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").value(org.hamcrest.Matchers.startsWith("INV-")))
                .andExpect(jsonPath("$.status").value("ISSUED"))
                .andExpect(jsonPath("$.total").value(242.00))
                .andExpect(jsonPath("$.balance").value(242.00))
                .andReturn();
        long invoiceId = ((Number) JsonPath.read(invoiceResult.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(get("/api/v1/invoices/" + invoiceId + "/pdf")
                        .header("Authorization", "Bearer " + token)
                        .header("Accept-Language", "es"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(result -> {
                    byte[] body = result.getResponse().getContentAsByteArray();
                    assertThat(new String(body, 0, 5)).isEqualTo("%PDF-");
                    assertThat(body.length).isGreaterThan(1000);
                });

        mockMvc.perform(post("/api/v1/invoices/" + invoiceId + "/payments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":100.00,"method":"TRANSFER","reference":"SEPA-1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PARTIALLY_PAID"))
                .andExpect(jsonPath("$.balance").value(142.00))
                .andExpect(jsonPath("$.payments.length()").value(1));

        mockMvc.perform(get("/api/v1/reports/receivables")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.balance==142.00)]").exists());

        mockMvc.perform(post("/api/v1/invoices/" + invoiceId + "/payments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":142.00,"method":"CARD"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.balance").value(0));

        mockMvc.perform(post("/api/v1/invoices/" + invoiceId + "/payments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":10.00,"method":"CASH"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("error.invoice.alreadyPaid"));

        LocalDate today = LocalDate.now();
        mockMvc.perform(get("/api/v1/reports/sales-summary")
                        .param("from", today.withDayOfMonth(1).toString())
                        .param("to", today.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(242.00)));

        mockMvc.perform(get("/api/v1/reports/inventory-valuation")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.warehouseCode=='WH-BILL')]").exists());

        mockMvc.perform(get("/api/v1/reports/sales-summary/export")
                        .param("from", today.withDayOfMonth(1).toString())
                        .param("to", today.toString())
                        .header("Authorization", "Bearer " + token)
                        .header("Accept-Language", "es"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Periodo")));

        mockMvc.perform(get("/api/v1/audit")
                        .param("entityType", "Invoice")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.action=='INVOICE_CREATE')]").exists())
                .andExpect(jsonPath("$.content[?(@.action=='INVOICE_PAYMENT')]").exists());
    }

    @Test
    void an_order_can_only_be_invoiced_once() throws Exception {
        String token = login("admin", "admin123");
        long categoryId = createCategory(token, "Once Cat");
        long productId = createProduct(token, categoryId, "ONCE-001", "50.00", "21");
        long warehouseId = createWarehouse(token, "WH-ONCE");
        long customerId = createCustomer(token, "ONCECUST", "Once Customer");
        adjustStock(token, productId, warehouseId, 5);

        long orderId = createConfirmedOrder(token, customerId, productId, warehouseId, 1);

        mockMvc.perform(post("/api/v1/invoices")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"salesOrderId":%d}
                                """.formatted(orderId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/invoices")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"salesOrderId":%d}
                                """.formatted(orderId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("error.invoice.orderNotConfirmed"));
    }

    private long createConfirmedOrder(String token, long customerId, long productId,
                                      long warehouseId, int quantity) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/sales-orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":%d,"lines":[{"productId":%d,"warehouseId":%d,"quantity":%d}]}
                                """.formatted(customerId, productId, warehouseId, quantity)))
                .andExpect(status().isCreated())
                .andReturn();
        long orderId = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(post("/api/v1/sales-orders/" + orderId + "/confirm")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        return orderId;
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
                                 "salePrice":%s,"costPrice":10.00,"taxRate":%s}
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
                                {"code":"%s","name":"%s"}
                                """.formatted(code, name)))
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
