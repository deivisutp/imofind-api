import type { Metadata } from "next";
import type { ReactNode } from "react";
import "./globals.css";

export const metadata: Metadata = {
  title: "ImoFind — Inteligência imobiliária regional de Blumenau",
  description:
    "Indicadores e histórico de ofertas imobiliárias em Blumenau: preço pedido, estoque por bairro e evolução das ofertas. Dados baseados em anúncios.",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="pt-BR">
      <body>{children}</body>
    </html>
  );
}
