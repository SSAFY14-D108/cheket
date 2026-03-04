import { AppProvider } from '@/lib/app-context'
import { CheketApp } from '@/components/cheket/cheket-app'

export default function Page() {
  return (
    <AppProvider>
      <main className="min-h-screen bg-zinc-100 flex items-center justify-center">
        <CheketApp />
      </main>
    </AppProvider>
  )
}
