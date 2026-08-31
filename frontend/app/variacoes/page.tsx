"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import {
  fetchVariacoes,
  formatBRL,
  type ReducaoPreco,
  type VariacaoResumo,
} from "@/lib/api";

type SortKey = "neighborhood" | "oldPrice" | "newPrice" | "queda" | "origem";
type Column = { key: SortKey; label: string; numeric: boolean };

const COLUMNS: Column[] = [
  { key: "neighborhood", label: "Bairro", numeric: false },
  { key: "oldPrice", label: "De", numeric: true },
  { key: "newPrice", label: "Para", numeric: true },
  { key: "queda", label: "Queda", numeric: true },
  { key: "origem", label: "Fonte", numeric: false },
];

const PERIODOS = [7, 15, 30, 90];

function quedaFraction(r: ReducaoPreco): number | null {
  if (r.oldPrice == null || r.newPrice == null || r.oldPrice <= 0) {
    return null;
  }
  return (r.oldPrice - r.newPrice) / r.oldPrice;
}

function pctQueda(r: ReducaoPreco): string {
  const f = quedaFraction(r);
  return f == null ? "—" : `-${(f * 100).toFixed(1)}%`;
}

function valueOf(r: ReducaoPreco, key: SortKey): string | number | null {
  return key === "queda" ? quedaFraction(r) : r[key];
}

export default function VariacoesPage() {
  const [dias, setDias] = useState(7);
  const [resumo, setResumo] = useState<VariacaoResumo | null>(null);
  const [reducoes, setReducoes] = useState<ReducaoPreco[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [bairro, setBairro] = useState("");
  const [fonte, setFonte] = useState("");
  const [sortKey, setSortKey] = useState<SortKey>("queda");
  const [sortDir, setSortDir] = useState<"asc" | "desc">("desc");

  async function load(d = dias) {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchVariacoes(d);
      setResumo(data.resumo);
      setReducoes(data.reducoes ?? []);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Erro desconhecido");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function onChangeDias(value: number) {
    setDias(value);
    load(value);
  }

  function onClear() {
    setBairro("");
    setFonte("");
  }

  function toggleSort(key: SortKey) {
    if (key === sortKey) {
      setSortDir((d) => (d === "asc" ? "desc" : "asc"));
    } else {
      setSortKey(key);
      setSortDir(key === "neighborhood" || key === "origem" ? "asc" : "desc");
    }
  }

  const fonteOptions = useMemo(
    () => [...new Set(reducoes.map((r) => r.origem).filter((o): o is string => !!o))].sort(),
    [reducoes]
  );

  const view = useMemo(() => {
    const term = bairro.trim().toLowerCase();
    const filtered = reducoes.filter(
      (r) =>
        (!term || (r.neighborhood ?? "").toLowerCase().includes(term)) &&
        (!fonte || r.origem === fonte)
    );
    const factor = sortDir === "asc" ? 1 : -1;
    return [...filtered].sort((a, b) => {
      const av = valueOf(a, sortKey);
      const bv = valueOf(b, sortKey);
      // valores ausentes sempre por último, independente da direção
      if (av == null && bv == null) return 0;
      if (av == null) return 1;
      if (bv == null) return -1;
      if (typeof av === "string" && typeof bv === "string") {
        return factor * av.localeCompare(bv, "pt-BR");
      }
      return factor * (Number(av) - Number(bv));
    });
  }, [reducoes, bairro, fonte, sortKey, sortDir]);

  return (
    <main className="container">
      <p>
        <Link href="/">← Início</Link>
      </p>
      <h1>Variações do período</h1>
      <p className="muted">Últimos {dias} dias · eventos observados nos anúncios.</p>

      <form className="filters" onSubmit={(e) => e.preventDefault()}>
        <select value={dias} onChange={(e) => onChangeDias(Number(e.target.value))}>
          {PERIODOS.map((d) => (
            <option key={d} value={d}>
              Últimos {d} dias
            </option>
          ))}
        </select>
        <input
          placeholder="Filtrar bairro"
          value={bairro}
          onChange={(e) => setBairro(e.target.value)}
        />
        <select value={fonte} onChange={(e) => setFonte(e.target.value)}>
          <option value="">Todas as fontes</option>
          {fonteOptions.map((f) => (
            <option key={f} value={f}>
              {f}
            </option>
          ))}
        </select>
        <button className="button secondary" type="button" onClick={onClear}>
          Limpar
        </button>
      </form>

      {loading && <p className="muted">Carregando…</p>}
      {error && <p className="error">Erro: {error}</p>}

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
          <p className="muted">
            {view.length} redução(ões) · filtros e ordenação aplicam-se a esta tabela.
          </p>
          {reducoes.length === 0 ? (
            <p className="muted">Nenhuma redução registrada no período.</p>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  {COLUMNS.map((col) => (
                    <th
                      key={col.key}
                      className={["sortable", col.numeric ? "num" : ""].filter(Boolean).join(" ")}
                      onClick={() => toggleSort(col.key)}
                      aria-sort={
                        sortKey === col.key ? (sortDir === "asc" ? "ascending" : "descending") : "none"
                      }
                    >
                      {col.label}
                      {sortKey === col.key && (
                        <span className="sort-ind">{sortDir === "asc" ? "▲" : "▼"}</span>
                      )}
                    </th>
                  ))}
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {view.map((r, i) => (
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
                {view.length === 0 && (
                  <tr>
                    <td colSpan={COLUMNS.length + 1}>Nenhuma redução encontrada com os filtros.</td>
                  </tr>
                )}
              </tbody>
            </table>
          )}
        </>
      )}
    </main>
  );
}
