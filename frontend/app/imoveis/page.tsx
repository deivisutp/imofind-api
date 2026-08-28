"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { fetchImoveis, fetchFontes, formatPrice, type Imovel } from "@/lib/api";

export default function ImoveisPage() {
  const [imoveis, setImoveis] = useState<Imovel[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [city, setCity] = useState("");
  const [neighborhood, setNeighborhood] = useState("");
  const [origem, setOrigem] = useState("");
  const [fontes, setFontes] = useState<string[]>([]);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchImoveis({
        city: city || undefined,
        neighborhood: neighborhood || undefined,
        origem: origem || undefined,
        page: 1,
        size: 30,
      });
      setImoveis(data.imoveis ?? []);
      setTotal(data.totalElements ?? 0);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Erro desconhecido");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchFontes()
      .then(setFontes)
      .catch(() => setFontes([]));
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <main className="container">
      <p>
        <Link href="/">← Início</Link>
      </p>
      <h1>Ofertas coletadas</h1>
      <p className="muted">{total} anúncio(s) · dados baseados em anúncios.</p>

      <form
        className="filters"
        onSubmit={(e) => {
          e.preventDefault();
          load();
        }}
      >
        <input
          placeholder="Cidade"
          value={city}
          onChange={(e) => setCity(e.target.value)}
        />
        <input
          placeholder="Bairro"
          value={neighborhood}
          onChange={(e) => setNeighborhood(e.target.value)}
        />
        <select value={origem} onChange={(e) => setOrigem(e.target.value)}>
          <option value="">Todas as fontes</option>
          {fontes.map((f) => (
            <option key={f} value={f}>
              {f}
            </option>
          ))}
        </select>
        <button className="button" type="submit">
          Filtrar
        </button>
      </form>

      {loading && <p>Carregando…</p>}
      {error && <p style={{ color: "#dc2626" }}>Erro: {error}</p>}

      {!loading && !error && (
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
          {imoveis.length === 0 && <p>Nenhum imóvel encontrado.</p>}
        </div>
      )}
    </main>
  );
}
