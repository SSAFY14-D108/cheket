/** @type {import('next').NextConfig} */
const nextConfig = {
  typescript: {
    ignoreBuildErrors: true,
  },
  images: {
    unoptimized: true,
    remotePatterns: [
      {
        protocol: 'http',
        hostname: 'www.kopis.or.kr',
      },
      {
        protocol: 'https',
        hostname: 'www.kopis.or.kr',
      },
    ],
  },
}

export default nextConfig
