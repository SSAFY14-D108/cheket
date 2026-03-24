import * as React from "react"

interface LeftPosterLayoutProps {
  children: React.ReactNode
}

export function LeftPosterLayout({ children }: LeftPosterLayoutProps) {
  return (
    <main className="relative flex h-svh w-full items-center justify-center overflow-hidden bg-white px-6 py-10 text-[#171717] sm:px-10">
      <div className="pointer-events-none absolute left-8 top-8 hidden items-center gap-3 text-[0.72rem] font-medium uppercase tracking-[0.34em] text-black/42 lg:flex">
        <span className="h-px w-10 bg-black/18" />
        CHEKET HOST
      </div>

      <section className="relative flex w-full max-w-[29rem] items-center justify-center">
        <div className="w-full">{children}</div>
      </section>
    </main>
  )
}
