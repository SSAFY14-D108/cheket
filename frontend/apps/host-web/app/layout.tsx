import type { Metadata } from 'next'
import { Analytics } from '@vercel/analytics/next'
import { ConditionalFooter } from '@/components/layout/ConditionalFooter'
import { AlertToastBridge } from '@/components/providers/AlertToastBridge'
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
      <body className="min-h-screen bg-background font-sans antialiased">
        <MockServiceWorkerProvider>
          <AlertToastBridge />
          <div className="flex min-h-screen flex-col">
            <main className="flex-1 bg-white">{children}</main>
            <ConditionalFooter />
          </div>
          <Toaster />
          <Analytics />
        </MockServiceWorkerProvider>
      </body>
    </html>
  )
}
