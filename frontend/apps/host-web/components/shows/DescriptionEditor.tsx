"use client"

import { Label } from "@/components/ui/label"
import { Type } from "lucide-react"

interface DescriptionEditorProps {
    value: string
    onChange: (value: string) => void
    id?: string
}

export function DescriptionEditor({ value, onChange, id = "description" }: DescriptionEditorProps) {
    return (
        <div className="rounded-xl border bg-card shadow-sm overflow-hidden">
            <div className="flex items-center justify-between px-4 py-3 border-b bg-card">
                <Label htmlFor={id} className="text-base font-semibold flex items-center gap-2">
                    <Type className="size-4" />
                    공연 요약 설명
                </Label>
            </div>

            <div className="bg-card p-4">
                <textarea
                    id={id}
                    value={value}
                    onChange={(event) => onChange(event.target.value)}
                    placeholder="관람객들에게 보여질 공연의 상세한 설명을 작성해주세요."
                    className="min-h-[480px] w-full resize-y rounded-md border border-input bg-background px-4 py-3 text-sm leading-7 text-foreground outline-none transition-colors placeholder:text-muted-foreground focus:ring-2 focus:ring-ring"
                />
            </div>

            <div className="px-4 py-2 border-t bg-muted/20 flex items-center justify-end">
                <span className="text-[10px] text-muted-foreground">{value.length}자</span>
            </div>
        </div>
    )
}
