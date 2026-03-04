import * as React from "react"
import Image from "next/image"

interface LeftPosterLayoutProps {
    children: React.ReactNode
}

export function LeftPosterLayout({ children }: LeftPosterLayoutProps) {
    return (
        <main className="flex min-h-svh w-full bg-background">
            {/* Left Area - Branding & Image (Hidden on mobile) */}
            <div className="hidden lg:flex lg:w-1/2 relative flex-col justify-between p-12 overflow-hidden bg-zinc-900 border-r border-border">
                {/* Background Image automatically optimized by Next.js */}
                <Image
                    src="/images_CHEKET/444.png"
                    alt="Background Poster"
                    fill
                    priority
                    className="absolute inset-0 z-0 object-contain object-center"
                />
                {/* Soft dark gradient overlay for text readability */}
                <div className="absolute inset-0 z-10 bg-gradient-to-t from-black/90 via-black/50 to-black/20" />

                {/* Top Logo */}
                <div className="relative z-20 flex items-center gap-2 text-white">
                    <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary shadow-lg">
                        <span className="font-extrabold text-xl leading-none text-white">C</span>
                    </div>
                    <span className="text-xl font-bold tracking-tight">CHEKET HOST</span>
                </div>

                {/* Bottom Text */}
                <div className="relative z-20 text-white mt-auto">
                    <p className="font-semibold text-primary-foreground/80 tracking-widest text-sm uppercase mb-3">
                        하나의 플랫폼, 모든 공연 관리
                    </p>
                    <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight mb-4 leading-tight">
                        더 투명하게, 더 안전하게, <br />
                        체켓 호스트 관리 페이지
                    </h1>
                    <p className="text-lg text-zinc-300 mb-8 max-w-md">
                        대한민국에서 가장 혁신적인 NFT 티켓팅 네트워크.<br />
                        지금 바로 체켓 에코시스템에 합류하세요.
                    </p>
                </div>
            </div>

            {/* Right Area - Form Content */}
            <div className="flex w-full lg:w-1/2 items-center justify-center p-8 sm:p-16 lg:p-24 relative">
                {/* Optional decorative background effect */}
                <div className="absolute inset-0 z-0 bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-primary/10 via-background to-background pointer-events-none" />

                <div className="w-full max-w-sm relative z-10">
                    {children}
                </div>
            </div>
        </main>
    )
}
