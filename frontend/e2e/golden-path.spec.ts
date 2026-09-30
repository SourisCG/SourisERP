import { expect, test } from "@playwright/test";

test("golden path: login, dashboard, catalogs, billing and reports", async ({ page }) => {
  await page.goto("/login");
  await expect(page.getByRole("heading", { name: "Sign in" })).toBeVisible();

  await page.getByRole("button", { name: /Administrator/ }).click();
  await page.waitForURL("/");
  await expect(page.getByText("DEMO MODE")).toBeVisible();
  await page.waitForTimeout(2500);
  await page.screenshot({ path: "screenshots/01-dashboard.png", fullPage: true });

  await page.getByRole("link", { name: "Products" }).click();
  await page.waitForURL("/products");
  await expect(page.getByRole("heading", { name: "Products" })).toBeVisible();
  await page.waitForTimeout(1200);
  await page.screenshot({ path: "screenshots/02-products.png", fullPage: true });

  await page.getByRole("link", { name: "Stock" }).click();
  await page.waitForURL("/inventory");
  await page.waitForTimeout(1200);
  await page.screenshot({ path: "screenshots/03-inventory.png", fullPage: true });

  await page.getByRole("link", { name: "Sales orders" }).click();
  await page.waitForURL("/sales-orders");
  await page.waitForTimeout(1200);
  await page.screenshot({ path: "screenshots/04-sales-orders.png", fullPage: true });

  await page.getByRole("link", { name: "Invoices" }).click();
  await page.waitForURL("/invoices");
  await page.waitForTimeout(1200);
  await page.screenshot({ path: "screenshots/05-invoices.png", fullPage: true });

  await page.getByRole("link", { name: "Reports" }).click();
  await page.waitForURL("/reports");
  await page.waitForTimeout(2000);
  await page.screenshot({ path: "screenshots/06-reports.png", fullPage: true });

  await page.getByRole("link", { name: "Audit trail" }).click();
  await page.waitForURL("/audit");
  await page.getByRole("button", { name: "JSON" }).first().click();
  await page.waitForTimeout(800);
  await page.screenshot({ path: "screenshots/07-audit.png", fullPage: true });

  await page.getByRole("button", { name: "ES", exact: true }).click();
  await page.waitForTimeout(800);
  await page.screenshot({ path: "screenshots/08-spanish.png", fullPage: true });
});
