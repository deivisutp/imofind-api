"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { fetchIndicadores, formatBRL, type NeighborhoodAggregate } from "@/lib/api";

export default function IndicadoresPage() {
  const [rows, setRows] = useState<NeighborhoodAggregate[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        setRows(await fetchIndicadores());
      } catch (e) {
        setError(e instanceof Error ? e.message : "Erro desconhecido");
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  return (
    <main className="container">
      <p>
        <Link href="/">← Início</Link>
      </p>
      <h1>Indicadores por bairro</h1>
      <p className="muted">
        Ofertas ativas de venda · valores refletem preço pedido, não preço de transação.
      </p>

      {loading && <p>Carregando…</p>}
      {error && <p style={{ color: "#dc2626" }}>Erro: {error}</p>}
      {!loading && !error && rows.length === 0 && <p>Sem dados agregados ainda.</p>}

      {!loading && !error && rows.length > 0 && (
        <table className="table">
          <thead>
            <tr>
              <th>Bairro</th>
              <th className="num">Estoque</th>
              <th className="num">Mediana</th>
              <th className="num">R$/m² (mediana)</th>
              <th className="num">Mínimo</th>
              <th className="num">Máximo</th>
              <th className="num">Média</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row, i) => (
              <tr key={`${row.city}-${row.neighborhood}-${i}`}>
                <td>
                  {row.neighborhood ?? "—"}
                  {row.city ? <span className="muted"> · {row.city}</span> : null}
                </td>
                <td className="num">{row.count}</td>
                <td className="num">{formatBRL(row.medianPrice)}</td>
                <td className="num">
                  {formatBRL(row.medianPricePerSqm)}
                  {row.sampleWithArea > 0 && (
                    <span className="muted"> (n={row.sampleWithArea})</span>
                  )}
                </td>
                <td className="num">{formatBRL(row.minPrice)}</td>
                <td className="num">{formatBRL(row.maxPrice)}</td>
                <td className="num">{formatBRL(row.avgPrice)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </main>
  );
}
