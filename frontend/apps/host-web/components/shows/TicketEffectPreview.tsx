"use client"

import { useEffect, useRef, useCallback } from "react"
import { Label } from "@/components/ui/label"
import { Button } from "@/components/ui/button"
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog"

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
    const iframeRef = useRef<HTMLIFrameElement>(null)

    const effectName = selectedEffect?.effect?.trim() || "none"

    const currentEffectName = selectedEffectId
        ? selectedEffect?.effect?.toUpperCase() || '선택 안됨'
        : '선택 안됨'

    // 효과 변경 → postMessage
    const sendEffect = useCallback((name: string) => {
        iframeRef.current?.contentWindow?.postMessage(
            { type: 'setEffect', effect: name },
            '*',
        )
    }, [])

    // 포스터 변경 → postMessage
    const sendPoster = useCallback((url: string) => {
        iframeRef.current?.contentWindow?.postMessage(
            { type: 'setPoster', poster: url },
            '*',
        )
    }, [])

    // 효과가 바뀔 때 postMessage
    useEffect(() => {
        sendEffect(effectName)
    }, [effectName, sendEffect])

    // 포스터가 바뀔 때 postMessage
    useEffect(() => {
        if (posterUrl) sendPoster(posterUrl)
    }, [posterUrl, sendPoster])

    // iframe 로드 완료 시 현재 상태 전달
    const handleIframeLoad = useCallback(() => {
        sendEffect(effectName)
        if (posterUrl) sendPoster(posterUrl)
    }, [effectName, posterUrl, sendEffect, sendPoster])

    // iframe src: 환경변수 기반 direct URL, fallback은 rewrite 프록시
    const previewBase = process.env.NEXT_PUBLIC_COLLECTION_PREVIEW_URL || "/collection-preview"
    const posterParam = posterUrl && !posterUrl.startsWith("data:")
        ? `&poster=${encodeURIComponent(posterUrl)}`
        : ""
    const iframeSrc = `${previewBase}?effect=${encodeURIComponent(effectName)}${posterParam}`

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

                <DialogContent className="max-w-lg w-full">
                    <DialogHeader>
                        <DialogTitle>티켓 효과 미리보기 설정</DialogTitle>
                    </DialogHeader>

                    <div className="flex items-start gap-6 p-4 bg-muted/30 rounded-lg mt-2">
                        {/* 왼쪽: iframe 실시간 미리보기 */}
                        <div className="flex-shrink-0 flex flex-col items-center gap-2 w-[150px]">
                            <div className="relative w-[150px] h-[290px] rounded-lg overflow-hidden bg-muted border border-border">
                                <iframe
                                    ref={iframeRef}
                                    src={iframeSrc}
                                    onLoad={handleIframeLoad}
                                    title="티켓 효과 미리보기"
                                    className="absolute inset-0 w-full h-full border-0"
                                    style={{ background: 'transparent' }}
                                    sandbox="allow-scripts allow-same-origin"
                                />
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
                                    onClick={() => onSelectEffect('')}
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
        </div>
    )
}
