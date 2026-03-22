import type { Metadata } from 'next'
import { Analytics } from '@vercel/analytics/next'
import { MockServiceWorkerProvider } from '@/components/providers/MockServiceWorkerProvider'
import { Toaster } from '@/components/ui/toaster'
import './globals.css'

export const metadata: Metadata = {
  title: 'CHEKET HOST',
  description: 'NFT 티켓 판매 관리자 시스템',
  icons: {
    icon: '/favicon.png',
    apple: '/apple-icon.png',
  },
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="ko">
      <body className="font-sans antialiased">
        <MockServiceWorkerProvider>
          {children}
          <Toaster />
          <Analytics />
        </MockServiceWorkerProvider>
      </body>
    </html>
  )
}
