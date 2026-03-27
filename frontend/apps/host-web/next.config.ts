import type { NextConfig } from "next"

const nextConfig: NextConfig = {
  images: {
    remotePatterns: [
      {
        protocol: "https",
        hostname: "**",
      },
      {
        protocol: "http",
        hostname: "**",
      },
    ],
  },
  async rewrites() {
    return [
      {
        source: "/collection-preview",
        destination: process.env.COLLECTION_PREVIEW_URL || "http://localhost:3100/preview",
      },
    ]
  },
}

export default nextConfig
