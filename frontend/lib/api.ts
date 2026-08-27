export interface Imovel {
  id: number | null;
  titulo: string | null;
  extra: string | null;
  price: number | null;
  origem: string | null;
  price_varchar: string | null;
  link: string | null;
  image: string | null;
  city: string | null;
  neighborhood: string | null;
  type: string | null;
  externalId: string | null;
  firstSeenAt: string | null;
  lastSeenAt: string | null;
}

export interface ImovelResponse {
  imoveis: Imovel[];
  totalElements: number;
}

export interface ImovelFilters {
  city?: string;
  neighborhood?: string;
  type?: string;
  page?: number;
  size?: number;
}

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export async function fetchImoveis(filters: ImovelFilters = {}): Promise<ImovelResponse> {
  const params = new URLSearchParams();
  if (filters.city) params.set("city", filters.city);
  if (filters.neighborhood) params.set("neighborhood", filters.neighborhood);
  if (filters.type) params.set("type", filters.type);
  params.set("page", String(filters.page ?? 1));
  params.set("size", String(filters.size ?? 20));

  const res = await fetch(`${API_URL}/api/v1/imoveis/buscar?${params.toString()}`);
  if (!res.ok) {
    throw new Error(`Falha ao buscar imoveis: ${res.status}`);
  }
  return res.json();
}

export function formatPrice(imovel: Imovel): string {
  if (imovel.price_varchar) {
    return imovel.price_varchar;
  }
  if (imovel.price == null) {
    return "Sob consulta";
  }
  return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(imovel.price);
}

export interface NeighborhoodAggregate {
  city: string | null;
  neighborhood: string | null;
  count: number;
  medianPrice: number | null;
  minPrice: number | null;
  maxPrice: number | null;
  avgPrice: number | null;
  medianPricePerSqm: number | null;
  sampleWithArea: number;
}

export async function fetchIndicadores(city?: string, type?: string): Promise<NeighborhoodAggregate[]> {
  const params = new URLSearchParams();
  if (city) params.set("city", city);
  if (type) params.set("type", type);
  const qs = params.toString();
  const res = await fetch(`${API_URL}/api/v1/imoveis/indicadores${qs ? `?${qs}` : ""}`);
  if (!res.ok) {
    throw new Error(`Falha ao buscar indicadores: ${res.status}`);
  }
  return res.json();
}

export function formatBRL(value: number | null): string {
  if (value == null) {
    return "—";
  }
  return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(value);
}
