import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  output: 'standalone',
  basePath: '/collection',
  images: {
    remotePatterns: [
      { protocol: 'https', hostname: '**' },
      { protocol: 'http', hostname: '**' },
    ],
  },
  async rewrites() {
    return [
      {
        source: '/proxy/api/:path*',
        destination: 'https://j14d108.p.ssafy.io/api/:path*',
      },
    ]
  },
}

export default nextConfig
