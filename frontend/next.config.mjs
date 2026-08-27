/** @type {import('next').NextConfig} */
const nextConfig = {
  // Export estatico: gera a pasta out/ para o Cloudflare Pages.
  output: "export",
  images: { unoptimized: true },
  trailingSlash: true,
  eslint: { ignoreDuringBuilds: true },
};

export default nextConfig;
