"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import {
  fetchIndicadores,
  fetchCidades,
  fetchTipos,
  formatBRL,
  type NeighborhoodAggregate,
} from "@/lib/api";

type SortKey =
  | "neighborhood"
  | "count"
  | "medianPrice"
  | "medianPricePerSqm"
  | "minPrice"
  | "maxPrice"
  | "avgPrice";

type Column = { key: SortKey; label: string; numeric: boolean; highlight?: boolean };

const COLUMNS: Column[] = [
  { key: "neighborhood", label: "Bairro", numeric: false },
  { key: "count", label: "Estoque", numeric: true },
  { key: "medianPrice", label: "Mediana", numeric: true },
  { key: "medianPricePerSqm", label: "R$/m² (mediana)", numeric: true, highlight: true },
  { key: "minPrice", label: "Mínimo", numeric: true },
  { key: "maxPrice", label: "Máximo", numeric: true },
  { key: "avgPrice", label: "Média", numeric: true },
];

export default function IndicadoresPage() {
  const [rows, setRows] = useState<NeighborhoodAggregate[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [city, setCity] = useState("");
  const [tipo, setTipo] = useState("");
  const [neighborhood, setNeighborhood] = useState("");
  const [cidades, setCidades] = useState<string[]>([]);
  const [tipos, setTipos] = useState<string[]>([]);

  const [sortKey, setSortKey] = useState<SortKey>("medianPricePerSqm");
  const [sortDir, setSortDir] = useState<"asc" | "desc">("desc");

  async function load(c = city, t = tipo) {
    setLoading(true);
    setError(null);
    try {
      setRows(await fetchIndicadores(c || undefined, t || undefined));
    } catch (e) {
      setError(e instanceof Error ? e.message : "Erro desconhecido");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchCidades().then(setCidades).catch(() => setCidades([]));
    fetchTipos().then(setTipos).catch(() => setTipos([]));
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function onChangeCity(value: string) {
    setCity(value);
    load(value, tipo);
  }

  function onChangeTipo(value: string) {
    setTipo(value);
    load(city, value);
  }

  function onClear() {
    setCity("");
    setTipo("");
    setNeighborhood("");
    load("", "");
  }

  function toggleSort(key: SortKey) {
    if (key === sortKey) {
      setSortDir((d) => (d === "asc" ? "desc" : "asc"));
    } else {
      setSortKey(key);
      setSortDir(key === "neighborhood" ? "asc" : "desc");
    }
  }

  const view = useMemo(() => {
    const term = neighborhood.trim().toLowerCase();
    const filtered = term
      ? rows.filter((r) => (r.neighborhood ?? "").toLowerCase().includes(term))
      : rows;
    const factor = sortDir === "asc" ? 1 : -1;
    return [...filtered].sort((a, b) => {
      const av = a[sortKey];
      const bv = b[sortKey];
      // valores ausentes sempre por último, independente da direção
      if (av == null && bv == null) return 0;
      if (av == null) return 1;
      if (bv == null) return -1;
      if (typeof av === "string" && typeof bv === "string") {
        return factor * av.localeCompare(bv, "pt-BR");
      }
      return factor * (Number(av) - Number(bv));
    });
  }, [rows, neighborhood, sortKey, sortDir]);

  return (
    <main className="container">
      <p>
        <Link href="/">← Início</Link>
      </p>
      <h1>Indicadores por bairro</h1>
      <p className="muted">
        Ofertas ativas de venda · valores refletem preço pedido, não preço de transação.
      </p>

      <form className="filters" onSubmit={(e) => e.preventDefault()}>
        <select value={city} onChange={(e) => onChangeCity(e.target.value)}>
          <option value="">Todas as cidades</option>
          {cidades.map((c) => (
            <option key={c} value={c}>
              {c}
            </option>
          ))}
        </select>
        <select value={tipo} onChange={(e) => onChangeTipo(e.target.value)}>
          <option value="">Todos os tipos</option>
          {tipos.map((t) => (
            <option key={t} value={t}>
              {t}
            </option>
          ))}
        </select>
        <input
          placeholder="Filtrar bairro"
          value={neighborhood}
          onChange={(e) => setNeighborhood(e.target.value)}
        />
        <button className="button secondary" type="button" onClick={onClear}>
          Limpar
        </button>
      </form>

      {loading && <p className="muted">Carregando…</p>}
      {error && <p className="error">Erro: {error}</p>}
      {!loading && !error && rows.length === 0 && <p>Sem dados agregados ainda.</p>}

      {!error && rows.length > 0 && (
        <>
          <p className="muted">
            {view.length} bairro(s) · clique nos títulos para ordenar. R$/m² usa a amostra com área (n).
          </p>
          <table className="table">
            <thead>
              <tr>
                {COLUMNS.map((col) => (
                  <th
                    key={col.key}
                    className={[
                      "sortable",
                      col.numeric ? "num" : "",
                      col.highlight ? "col-highlight" : "",
                    ]
                      .filter(Boolean)
                      .join(" ")}
                    onClick={() => toggleSort(col.key)}
                    aria-sort={sortKey === col.key ? (sortDir === "asc" ? "ascending" : "descending") : "none"}
                  >
                    {col.label}
                    {sortKey === col.key && (
                      <span className="sort-ind">{sortDir === "asc" ? "▲" : "▼"}</span>
                    )}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {view.map((row, i) => (
                <tr key={`${row.city}-${row.neighborhood}-${i}`}>
                  <td>
                    {row.neighborhood ?? "—"}
                    {row.city ? <span className="muted"> · {row.city}</span> : null}
                  </td>
                  <td className="num">{row.count}</td>
                  <td className="num">{formatBRL(row.medianPrice)}</td>
                  <td className="num col-highlight">
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
              {view.length === 0 && (
                <tr>
                  <td colSpan={COLUMNS.length}>Nenhum bairro encontrado.</td>
                </tr>
              )}
            </tbody>
          </table>
        </>
      )}
    </main>
  );
}
