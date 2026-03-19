"use client"

import { Label } from "@/components/ui/label"
import { Button } from "@/components/ui/button"
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog"
import Image from "next/image"

export interface TicketEffect {
    id: number
    effect: string
}

interface TicketEffectPreviewProps {
    posterUrl: string | null
    selectedEffectId?: string
    onSelectEffect: (effectId: string) => void
    ticketEffects: TicketEffect[]
}

export function TicketEffectPreview({
    posterUrl,
    selectedEffectId,
    onSelectEffect,
    ticketEffects
}: TicketEffectPreviewProps) {
    const selectedEffect = ticketEffects.find((effect) => effect.id.toString() === selectedEffectId)
    const normalizedEffectKey = selectedEffect?.effect?.trim().toLowerCase() || ""

    // 기본 포스터 (업로드 안 했을 때)
    const displayImage = posterUrl || "/placeholder.svg"
    const shouldBypassOptimization =
        displayImage.startsWith("data:") ||
        displayImage.startsWith("http://") ||
        displayImage.startsWith("https://")

    // 효과 매핑 함수 (각 effect 문자열에 따른 CSS 스타일/클래스)
    const getEffectClasses = (effectStr: string) => {
        switch (effectStr) {
            case 'glow1':
                // 반짝이는 네온 테두리 효과
                return "shadow-[0_0_15px_rgba(124,110,240,0.8)] border-primary/80 animate-pulse"
            case 'glow2':
                // 빛이 스쳐지나가는 홀로그램 스타일 (CSS 애니메이션은 아래 <style>에서 정의)
                return "effect-glow2 border-white/40"
            case 'glow3':
                // 색조가 변하는 효과
                return "animate-[hue-rotate_3s_infinite_linear] border-purple-400 border-2"
            case 'glow4':
                // 부드럽게 밝아졌다 어두워지는 효과 + 대비 증가
                return "animate-[pulse_2s_ease-in-out_infinite] brightness-125 contrast-125 border-yellow-400 border-2"
            default:
                return "border-transparent"
        }
    }

    const currentEffectName = selectedEffectId
        ? selectedEffect?.effect?.toUpperCase() || '선택 안됨'
        : '선택 안됨'

    return (
        <div className="flex items-center gap-2 mt-1">
            <Label className="text-[10px] text-muted-foreground w-12 shrink-0">티켓 효과</Label>

            <Dialog>
                <DialogTrigger asChild>
                    <Button
                        variant="outline"
                        size="sm"
                        className={`h-7 px-3 text-[10px] font-semibold tracking-wider ${selectedEffectId ? 'bg-primary/10 text-primary border-primary/50 hover:bg-primary/20 hover:text-primary z-10' : 'text-muted-foreground'}`}
                    >
                        {selectedEffectId ? `✨ ${currentEffectName}` : '효과 추가하기'}
                    </Button>
                </DialogTrigger>

                <DialogContent className="max-w-md w-full">
                    <DialogHeader>
                        <DialogTitle>티켓 효과 미리보기 설정</DialogTitle>
                    </DialogHeader>

                    <div className="flex items-start gap-6 p-4 bg-muted/30 rounded-lg mt-2">
                        {/* 왼쪽: 실시간 티켓 미리보기 화면 */}
                        <div className="flex-shrink-0 flex flex-col items-center gap-2 w-[120px]">
                            <div
                                className={`relative w-full aspect-[3/4] rounded-md overflow-hidden bg-muted transition-all duration-300 border-2 ${selectedEffectId ? getEffectClasses(normalizedEffectKey) : 'border-transparent shadow-sm'}`}
                            >
                                <Image
                                    src={displayImage}
                                    alt="티켓 미리보기"
                                    fill
                                    className="object-cover"
                                    unoptimized={shouldBypassOptimization}
                                />
                                {normalizedEffectKey === 'glow2' && (
                                    <div className="absolute inset-0 z-10 pointer-events-none shimmer-overlay" />
                                )}
                                {!posterUrl && (
                                    <div className="absolute inset-0 flex items-center justify-center bg-black/50 text-white text-xs font-medium backdrop-blur-[2px]">
                                        포스터 없음
                                    </div>
                                )}
                            </div>
                            <span className="text-[10px] uppercase font-bold text-muted-foreground mt-1">
                                {currentEffectName}
                            </span>
                        </div>

                        {/* 오른쪽: 버튼 리스트 */}
                        <div className="flex-1 flex flex-col gap-3">
                            <Label className="text-xs text-muted-foreground">적용할 효과를 선택하세요</Label>
                            <div className="flex flex-wrap gap-2">
                                <div
                                    onClick={() => onSelectEffect('')} // 선택 해제용 (옵션)
                                    className={`flex items-center justify-center h-9 px-3 min-w-[70px] text-xs font-semibold rounded-md cursor-pointer transition-all border ${!selectedEffectId
                                            ? 'border-muted-foreground bg-muted-foreground text-background shadow-md'
                                            : 'border-border bg-background hover:bg-muted text-foreground'
                                        }`}
                                >
                                    NONE
                                </div>
                                {ticketEffects.filter(effect => effect.effect.toLowerCase() !== "none").map(effect => {
                                    const isSelected = selectedEffectId === effect.id.toString()
                                    return (
                                        <div
                                            key={effect.id}
                                            onClick={() => onSelectEffect(effect.id.toString())}
                                            className={`flex items-center justify-center h-9 px-3 min-w-[70px] text-xs font-semibold uppercase rounded-md cursor-pointer transition-all border ${isSelected
                                                    ? 'border-primary bg-primary text-primary-foreground shadow-md ring-2 ring-primary/20 ring-offset-1'
                                                    : 'border-border bg-background hover:bg-muted text-foreground hover:border-primary/50'
                                                }`}
                                        >
                                            {effect.effect}
                                        </div>
                                    )
                                })}
                            </div>
                        </div>
                    </div>
                </DialogContent>
            </Dialog>

            {/* 커스텀 CSS 키프레임 */}
            <style jsx>{`
                @keyframes shimmer {
                    0% { transform: translateX(-150%) skewX(-15deg); }
                    50% { transform: translateX(150%) skewX(-15deg); }
                    100% { transform: translateX(150%) skewX(-15deg); }
                }
                .shimmer-overlay {
                    background: linear-gradient(
                        90deg,
                        transparent,
                        rgba(255, 255, 255, 0.4),
                        transparent
                    );
                    animation: shimmer 2.5s infinite;
                }
                .effect-glow2 {
                    box-shadow: 0 0 15px rgba(255, 255, 255, 0.4) inset;
                }
                @keyframes hue-rotate {
                    0% { filter: hue-rotate(0deg); }
                    100% { filter: hue-rotate(360deg); }
                }
            `}</style>
        </div>
    )
}
