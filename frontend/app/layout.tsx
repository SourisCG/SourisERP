import type { Metadata } from "next";
import { cookies } from "next/headers";
import "./globals.css";
import { Providers } from "@/components/providers";
import type { Locale } from "@/lib/i18n";

export const metadata: Metadata = {
  title: "ERP Core - Portfolio",
  description: "Hexagonal ERP portfolio: Spring Boot 4 + Next.js + Docker",
};

export default async function RootLayout({ children }: { children: React.ReactNode }) {
  const store = await cookies();
  const locale: Locale = store.get("locale")?.value === "es" ? "es" : "en";

  return (
    <html lang={locale}>
      <body className="min-h-screen antialiased">
        <Providers initialLocale={locale}>{children}</Providers>
      </body>
    </html>
  );
}
