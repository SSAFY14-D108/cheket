import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  output: 'standalone',
  images: {
    remotePatterns: [
      { protocol: 'https', hostname: '**' },
      { protocol: 'http', hostname: '**' },
    ],
  },
  async headers() {
    return [
      {
        source: '/preview',
        headers: [
          {
            key: 'Content-Security-Policy',
            value: "frame-ancestors 'self' https://j14d108.p.ssafy.io http://j14d108.p.ssafy.io https://j14d108.p.ssafy.io:* http://j14d108.p.ssafy.io:* http://localhost:*",
          },
        ],
      },
    ]
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
