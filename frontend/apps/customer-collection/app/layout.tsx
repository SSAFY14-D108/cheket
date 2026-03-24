import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'Cheket Collection',
  description: 'NFT 티켓 컬렉션',
}

export const viewport = {
  width: 'device-width',
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ko" style={{ height: '100dvh', minHeight: '100dvh' }}>
      <head>
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" />
        <style dangerouslySetInnerHTML={{ __html: `
          html, body { height: 100dvh !important; min-height: 100dvh !important; margin: 0; padding: 0; overflow: hidden; }
          body > div { height: 100% !important; min-height: 100% !important; }
        `}} />
      </head>
      <body className="antialiased">{children}</body>
    </html>
  )
}
