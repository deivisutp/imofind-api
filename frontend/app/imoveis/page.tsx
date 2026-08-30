"use client";

import { useEffect, useState, type FormEvent } from "react";
import Link from "next/link";
import {
  fetchImoveis,
  fetchFontes,
  fetchTipos,
  fetchCidades,
  formatPrice,
  type Imovel,
  type ImovelFilters,
} from "@/lib/api";

type Applied = {
  city: string;
  neighborhood: string;
  origem: string;
  tipo: string;
  minPrice: string;
  maxPrice: string;
  sort: string;
};

const EMPTY: Applied = {
  city: "",
  neighborhood: "",
  origem: "",
  tipo: "",
  minPrice: "",
  maxPrice: "",
  sort: "recent",
};

const SIZE_OPTIONS = [20, 30, 50];

export default function ImoveisPage() {
  const [imoveis, setImoveis] = useState<Imovel[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // valores do formulário (ainda não aplicados)
  const [city, setCity] = useState("");
  const [neighborhood, setNeighborhood] = useState("");
  const [origem, setOrigem] = useState("");
  const [tipo, setTipo] = useState("");
  const [minPrice, setMinPrice] = useState("");
  const [maxPrice, setMaxPrice] = useState("");
  const [sort, setSort] = useState("recent");

  // opções dos selects
  const [fontes, setFontes] = useState<string[]>([]);
  const [tipos, setTipos] = useState<string[]>([]);
  const [cidades, setCidades] = useState<string[]>([]);

  // busca corrente (o que foi efetivamente aplicado)
  const [applied, setApplied] = useState<Applied>(EMPTY);
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(30);

  const totalPages = Math.max(1, Math.ceil(total / size));
  const start = total === 0 ? 0 : (page - 1) * size + 1;
  const end = Math.min(page * size, total);

  function toFilters(a: Applied, targetPage: number, pageSize: number): ImovelFilters {
    return {
      city: a.city || undefined,
      neighborhood: a.neighborhood || undefined,
      origem: a.origem || undefined,
      type: a.tipo || undefined,
      minPrice: a.minPrice ? Number(a.minPrice) : undefined,
      maxPrice: a.maxPrice ? Number(a.maxPrice) : undefined,
      sort: a.sort || undefined,
      page: targetPage,
      size: pageSize,
    };
  }

  async function load(targetPage: number, a: Applied = applied, pageSize: number = size) {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchImoveis(toFilters(a, targetPage, pageSize));
      setImoveis(data.imoveis ?? []);
      setTotal(data.totalElements ?? 0);
      setPage(targetPage);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Erro desconhecido");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchFontes().then(setFontes).catch(() => setFontes([]));
    fetchTipos().then(setTipos).catch(() => setTipos([]));
    fetchCidades().then(setCidades).catch(() => setCidades([]));
    load(1);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function onSubmit(e: FormEvent) {
    e.preventDefault();
    const next: Applied = { city, neighborhood, origem, tipo, minPrice, maxPrice, sort };
    setApplied(next);
    load(1, next);
  }

  function onClear() {
    setCity("");
    setNeighborhood("");
    setOrigem("");
    setTipo("");
    setMinPrice("");
    setMaxPrice("");
    setSort("recent");
    setApplied(EMPTY);
    load(1, EMPTY);
  }

  function onChangeSize(next: number) {
    setSize(next);
    load(1, applied, next);
  }

  function goTo(target: number) {
    if (target < 1 || target > totalPages || target === page) return;
    load(target);
    if (typeof window !== "undefined") {
      window.scrollTo({ top: 0, behavior: "smooth" });
    }
  }

  return (
    <main className="container">
      <p>
        <Link href="/">← Início</Link>
      </p>
      <h1>Ofertas coletadas</h1>
      <p className="muted">
        {total > 0 ? `Mostrando ${start}–${end} de ${total} anúncio(s)` : "Nenhum anúncio"} · dados baseados em anúncios.
      </p>

      <form className="filters" onSubmit={onSubmit}>
        <select value={city} onChange={(e) => setCity(e.target.value)}>
          <option value="">Todas as cidades</option>
          {cidades.map((c) => (
            <option key={c} value={c}>
              {c}
            </option>
          ))}
        </select>
        <input
          placeholder="Bairro"
          value={neighborhood}
          onChange={(e) => setNeighborhood(e.target.value)}
        />
        <input
          type="number"
          min={0}
          inputMode="numeric"
          placeholder="R$ mín"
          value={minPrice}
          onChange={(e) => setMinPrice(e.target.value)}
        />
        <input
          type="number"
          min={0}
          inputMode="numeric"
          placeholder="R$ máx"
          value={maxPrice}
          onChange={(e) => setMaxPrice(e.target.value)}
        />
        <select value={origem} onChange={(e) => setOrigem(e.target.value)}>
          <option value="">Todas as fontes</option>
          {fontes.map((f) => (
            <option key={f} value={f}>
              {f}
            </option>
          ))}
        </select>
        <select value={tipo} onChange={(e) => setTipo(e.target.value)}>
          <option value="">Todos os tipos</option>
          {tipos.map((t) => (
            <option key={t} value={t}>
              {t}
            </option>
          ))}
        </select>
        <select value={sort} onChange={(e) => setSort(e.target.value)}>
          <option value="recent">Mais recentes</option>
          <option value="price_asc">Menor preço</option>
          <option value="price_desc">Maior preço</option>
        </select>
        <button className="button" type="submit">
          Filtrar
        </button>
        <button className="button secondary" type="button" onClick={onClear}>
          Limpar
        </button>
      </form>

      {error && <p className="error">Erro: {error}</p>}
      {loading && <p className="muted">Carregando…</p>}

      {!error && (
        <>
          <div className="grid">
            {imoveis.map((imovel, i) => (
              <article className="card" key={imovel.id ?? imovel.externalId ?? i}>
                {imovel.image ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img
                    className="card-img"
                    src={imovel.image}
                    alt={`${imovel.type ?? "Imóvel"} em ${imovel.neighborhood ?? ""}`}
                    loading="lazy"
                  />
                ) : (
                  <div className="card-img card-img--empty">sem foto</div>
                )}
                <h3>
                  {imovel.type ?? "Imóvel"} · {imovel.neighborhood ?? "—"}
                </h3>
                <p className="price">{formatPrice(imovel)}</p>
                <p className="meta">
                  {imovel.city ?? ""}
                  {imovel.extra ? ` · ${imovel.extra}` : ""}
                </p>
                <p className="meta">Fonte: {imovel.origem ?? "—"}</p>
                {imovel.link && (
                  <p>
                    <a href={imovel.link} target="_blank" rel="noreferrer">
                      Ver anúncio
                    </a>
                  </p>
                )}
              </article>
            ))}
          </div>

          {!loading && imoveis.length === 0 && <p>Nenhum imóvel encontrado.</p>}

          {totalPages > 1 && (
            <div className="pagination">
              <button
                className="button secondary"
                type="button"
                disabled={page <= 1 || loading}
                onClick={() => goTo(page - 1)}
              >
                ← Anterior
              </button>
              <span className="muted">
                Página {page} de {totalPages}
              </span>
              <button
                className="button secondary"
                type="button"
                disabled={page >= totalPages || loading}
                onClick={() => goTo(page + 1)}
              >
                Próxima →
              </button>
            </div>
          )}

          <div className="pagesize muted">
            <label>
              Por página:{" "}
              <select value={size} onChange={(e) => onChangeSize(Number(e.target.value))}>
                {SIZE_OPTIONS.map((s) => (
                  <option key={s} value={s}>
                    {s}
                  </option>
                ))}
              </select>
            </label>
          </div>
        </>
      )}
    </main>
  );
}
