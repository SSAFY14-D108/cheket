import * as React from "react"

interface LeftPosterLayoutProps {
  children: React.ReactNode
}

export function LeftPosterLayout({ children }: LeftPosterLayoutProps) {
  return (
    <main className="relative grid h-[100dvh] w-full place-items-center overflow-hidden bg-white px-6 text-[#171717] sm:px-10">
      <section className="relative flex w-full max-w-[29rem] items-center justify-center">
        <div className="w-full">{children}</div>
      </section>
    </main>
  )
}
