import Link from "next/link";

export default function HomePage() {
  return (
    <main className="container">
      <section className="hero">
        <span className="badge">Piloto · Blumenau/SC</span>
        <h1>Inteligência sobre ofertas imobiliárias regionais</h1>
        <p>
          Acompanhe preço pedido, estoque por bairro e a evolução das ofertas de
          imóveis em Blumenau. Todos os indicadores são baseados em anúncios —
          preço pedido não é preço de transação.
        </p>
        <div className="actions">
          <Link className="button" href="/indicadores">
            Ver indicadores por bairro
          </Link>
          <Link className="button secondary" href="/imoveis">
            Ver ofertas coletadas
          </Link>
        </div>
      </section>

      <section>
        <h2>O que oferecemos</h2>
        <div className="grid">
          <div className="card">
            <h3>Preço pedido por bairro</h3>
            <p className="meta">Mediana e faixa de valores das ofertas ativas.</p>
          </div>
          <div className="card">
            <h3>Estoque aparente</h3>
            <p className="meta">Quantidade de ofertas por bairro e tipologia.</p>
          </div>
          <div className="card">
            <h3>Evolução das ofertas</h3>
            <p className="meta">Anúncios novos, removidos e reduções de preço.</p>
          </div>
        </div>
      </section>

      <section>
        <h2>Metodologia</h2>
        <p className="muted">
          Os dados são coletados de fontes permitidas e agregados de forma
          responsável. Indicadores refletem anúncios observados, não transações
          concluídas. A remoção de um anúncio não comprova venda.
        </p>
      </section>

      <footer className="footer">
        ImoFind · Dados de ofertas imobiliárias regionais · Piloto de validação.
      </footer>
    </main>
  );
}
