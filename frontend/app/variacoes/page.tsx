"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import {
  fetchVariacoes,
  formatBRL,
  type ReducaoPreco,
  type VariacaoResumo,
} from "@/lib/api";

function pctQueda(r: ReducaoPreco): string {
  if (r.oldPrice == null || r.newPrice == null || r.oldPrice <= 0) {
    return "—";
  }
  const pct = ((r.oldPrice - r.newPrice) / r.oldPrice) * 100;
  return `-${pct.toFixed(1)}%`;
}

export default function VariacoesPage() {
  const [resumo, setResumo] = useState<VariacaoResumo | null>(null);
  const [reducoes, setReducoes] = useState<ReducaoPreco[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const data = await fetchVariacoes(7);
        setResumo(data.resumo);
        setReducoes(data.reducoes ?? []);
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
      <h1>Variações da semana</h1>
      <p className="muted">Últimos 7 dias · eventos observados nos anúncios.</p>

      {loading && <p>Carregando…</p>}
      {error && <p style={{ color: "#dc2626" }}>Erro: {error}</p>}

      {!loading && !error && resumo && (
        <div className="grid">
          <div className="card">
            <h3>Novos</h3>
            <p className="price">{resumo.novos ?? 0}</p>
          </div>
          <div className="card">
            <h3>Reduções de preço</h3>
            <p className="price">{resumo.reducoes ?? 0}</p>
          </div>
          <div className="card">
            <h3>Aumentos de preço</h3>
            <p className="price">{resumo.aumentos ?? 0}</p>
          </div>
          <div className="card">
            <h3>Removidos</h3>
            <p className="price">{resumo.removidos ?? 0}</p>
          </div>
          <div className="card">
            <h3>Reativados</h3>
            <p className="price">{resumo.reativados ?? 0}</p>
          </div>
        </div>
      )}

      {!loading && !error && (
        <>
          <h2>Maiores reduções de preço</h2>
          {reducoes.length === 0 ? (
            <p className="muted">Nenhuma redução registrada no período.</p>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Bairro</th>
                  <th className="num">De</th>
                  <th className="num">Para</th>
                  <th className="num">Queda</th>
                  <th>Fonte</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {reducoes.map((r, i) => (
                  <tr key={`${r.link ?? ""}-${i}`}>
                    <td>
                      {r.neighborhood ?? "—"}
                      {r.city ? <span className="muted"> · {r.city}</span> : null}
                    </td>
                    <td className="num">{formatBRL(r.oldPrice)}</td>
                    <td className="num">{formatBRL(r.newPrice)}</td>
                    <td className="num">{pctQueda(r)}</td>
                    <td>{r.origem ?? "—"}</td>
                    <td>
                      {r.link && (
                        <a href={r.link} target="_blank" rel="noreferrer">
                          Ver
                        </a>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </main>
  );
}
