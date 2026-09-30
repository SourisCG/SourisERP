package com.portfolio.erp.infrastructure.out.report;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.portfolio.erp.domain.model.Invoice;
import com.portfolio.erp.domain.model.InvoiceLine;

/**
 * Renders the invoice PDF from an HTML template (openhtmltopdf).
 */
@Component
public class InvoicePdfRenderer {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MessageSource messageSource;

    public InvoicePdfRenderer(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public byte[] render(Invoice invoice, Locale locale) {
        String html = buildHtml(invoice, locale);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("Could not render invoice PDF " + invoice.getNumber(), ex);
        }
        return out.toByteArray();
    }

    private String buildHtml(Invoice invoice, Locale locale) {
        StringBuilder rows = new StringBuilder();
        for (InvoiceLine line : invoice.getLines()) {
            rows.append("<tr>")
                    .append("<td>").append(escape(line.getSku())).append("</td>")
                    .append("<td>").append(escape(line.getProductName())).append("</td>")
                    .append("<td class=\"num\">").append(line.getQuantity()).append("</td>")
                    .append("<td class=\"num\">").append(money(line.getUnitPrice(), locale)).append("</td>")
                    .append("<td class=\"num\">").append(line.getTaxRate()).append("%</td>")
                    .append("<td class=\"num\">").append(money(line.lineTotal(), locale)).append("</td>")
                    .append("</tr>");
        }

        String status = msg("invoice.status." + invoice.getStatus().name().toLowerCase(Locale.ROOT), locale);

        return """
                <!DOCTYPE html>
                <html>
                <head>
                <style>
                  body { font-family: Helvetica, sans-serif; font-size: 11px; color: #1e293b; }
                  h1 { font-size: 22px; margin: 0 0 2px 0; color: #0f172a; }
                  .muted { color: #64748b; }
                  .header { width: 100%%; }
                  .header td { vertical-align: top; }
                  .box { border: 1px solid #cbd5e1; padding: 8px; }
                  table.items { width: 100%%; border-collapse: collapse; margin-top: 14px; }
                  table.items th { background: #0f172a; color: #fff; padding: 6px; text-align: left; }
                  table.items td { border-bottom: 1px solid #e2e8f0; padding: 6px; }
                  .num { text-align: right; }
                  .totals { width: 45%%; margin-left: 55%%; margin-top: 10px; }
                  .totals td { padding: 3px 6px; }
                  .totals .grand { font-weight: bold; font-size: 13px; border-top: 2px solid #0f172a; }
                </style>
                </head>
                <body>
                  <table class="header"><tr>
                    <td>
                      <h1>ERP Core</h1>
                      <div class="muted">%s</div>
                    </td>
                    <td style="text-align:right">
                      <div><strong>%s</strong> %s</div>
                      <div class="muted">%s: %s</div>
                      <div class="muted">%s: %s</div>
                      <div class="muted">%s: %s</div>
                      <div class="muted">%s: %s</div>
                      <div class="muted">%s: %s</div>
                    </td>
                  </tr></table>
                  <div class="box" style="margin-top:10px">
                    <div class="muted">%s</div>
                    <div><strong>%s</strong></div>
                    <div>%s</div>
                  </div>
                  <table class="items">
                    <thead><tr>
                      <th>%s</th><th>%s</th><th class="num">%s</th><th class="num">%s</th>
                      <th class="num">%s</th><th class="num">%s</th>
                    </tr></thead>
                    <tbody>%s</tbody>
                  </table>
                  <table class="totals">
                    <tr><td>%s</td><td class="num">%s</td></tr>
                    <tr><td>%s</td><td class="num">%s</td></tr>
                    <tr class="grand"><td>%s</td><td class="num">%s</td></tr>
                    <tr><td>%s</td><td class="num">%s</td></tr>
                    <tr><td><strong>%s</strong></td><td class="num"><strong>%s</strong></td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                msg("invoice.pdf.subtitle", locale),
                msg("invoice.pdf.number", locale), invoice.getNumber(),
                msg("invoice.pdf.status", locale), status,
                msg("invoice.pdf.issue", locale), invoice.getIssueDate().format(DATE),
                msg("invoice.pdf.due", locale), invoice.getDueDate().format(DATE),
                msg("invoice.pdf.order", locale), invoice.getSalesOrderNumber(),
                msg("invoice.pdf.customer", locale), invoice.getCustomerName(),
                msg("invoice.pdf.billTo", locale), invoice.getCustomerName(),
                invoice.getCustomerId() != null ? "#" + invoice.getCustomerId() : "",
                msg("invoice.pdf.sku", locale), msg("invoice.pdf.item", locale),
                msg("invoice.pdf.qty", locale), msg("invoice.pdf.price", locale),
                msg("invoice.pdf.tax", locale), msg("invoice.pdf.lineTotal", locale),
                rows.toString(),
                msg("invoice.pdf.subtotal", locale), money(invoice.getSubtotal(), locale),
                msg("invoice.pdf.taxTotal", locale), money(invoice.getTaxTotal(), locale),
                msg("invoice.pdf.total", locale), money(invoice.getTotal(), locale),
                msg("invoice.pdf.paid", locale), money(invoice.getPaidAmount(), locale),
                msg("invoice.pdf.balance", locale), money(invoice.balance(), locale));
    }

    private String msg(String key, Locale locale) {
        return messageSource.getMessage(key, null, key, locale);
    }

    private String money(java.math.BigDecimal value, Locale locale) {
        return NumberFormat.getCurrencyInstance(locale).format(value);
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
