package com.portfolio.erp.infrastructure.out.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.portfolio.erp.domain.model.InventoryValuationItem;
import com.portfolio.erp.domain.model.ReceivableItem;
import com.portfolio.erp.domain.model.SalesSummaryItem;
import com.portfolio.erp.domain.ports.out.ReportQueryPort;

/**
 * Read-only analytical queries (native SQL tuned for reporting).
 */
@Repository
public class ReportQueryAdapter implements ReportQueryPort {

    private final JdbcClient jdbc;

    public ReportQueryAdapter(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<SalesSummaryItem> salesSummary(LocalDate from, LocalDate to) {
        return jdbc.sql("""
                select to_char(issue_date, 'YYYY-MM') as period,
                       count(*) as invoice_count,
                       coalesce(sum(subtotal), 0) as subtotal,
                       coalesce(sum(tax_total), 0) as tax_total,
                       coalesce(sum(total), 0) as total
                from invoices
                where issue_date between :from and :to
                group by 1
                order by 1
                """)
                .param("from", from)
                .param("to", to)
                .query((rs, rowNum) -> new SalesSummaryItem(
                        rs.getString("period"),
                        rs.getLong("invoice_count"),
                        rs.getBigDecimal("subtotal"),
                        rs.getBigDecimal("tax_total"),
                        rs.getBigDecimal("total")))
                .list();
    }

    @Override
    public List<InventoryValuationItem> inventoryValuation() {
        return jdbc.sql("""
                select w.id as warehouse_id,
                       w.code as warehouse_code,
                       coalesce(sum(i.quantity), 0) as total_quantity,
                       coalesce(sum(i.quantity * p.cost_price), 0) as total_cost,
                       coalesce(sum(i.quantity * p.sale_price), 0) as total_retail
                from warehouses w
                left join inventory_items i on i.warehouse_id = w.id
                left join products p on p.id = i.product_id
                group by w.id, w.code
                order by w.code
                """)
                .query((rs, rowNum) -> new InventoryValuationItem(
                        rs.getLong("warehouse_id"),
                        rs.getString("warehouse_code"),
                        rs.getLong("total_quantity"),
                        rs.getBigDecimal("total_cost"),
                        rs.getBigDecimal("total_retail")))
                .list();
    }

    @Override
    public List<ReceivableItem> receivables() {
        return jdbc.sql("""
                select inv.id as invoice_id,
                       inv.number as invoice_number,
                       c.id as customer_id,
                       c.name as customer_name,
                       inv.due_date,
                       inv.total,
                       inv.paid_amount,
                       (inv.total - inv.paid_amount) as balance,
                       greatest(0, current_date - inv.due_date) as days_overdue
                from invoices inv
                join customers c on c.id = inv.customer_id
                where inv.total - inv.paid_amount > 0.001
                order by inv.due_date
                """)
                .query((rs, rowNum) -> new ReceivableItem(
                        rs.getLong("invoice_id"),
                        rs.getString("invoice_number"),
                        rs.getLong("customer_id"),
                        rs.getString("customer_name"),
                        rs.getObject("due_date", LocalDate.class),
                        rs.getBigDecimal("total"),
                        rs.getBigDecimal("paid_amount"),
                        rs.getBigDecimal("balance"),
                        rs.getInt("days_overdue")))
                .list();
    }
}
