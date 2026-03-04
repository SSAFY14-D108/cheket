"use client"

import { useState, useRef } from "react"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Button } from "@/components/ui/button"
import { cn } from "@/lib/utils"
import { ImagePlus, Type, Eye, EyeOff, X } from "lucide-react"

interface DescriptionEditorProps {
    value: string
    onChange: (value: string) => void
    id?: string
}

// ── Markdown Renderer (simple) ─────────────────────────────────────
function MarkdownPreview({ value }: { value: string }) {
    if (!value.trim()) {
        return (
            <div className="flex flex-col items-center justify-center h-full text-muted-foreground">
                <Eye className="size-10 mb-3 opacity-20" />
                <p className="text-sm italic">미리볼 내용이 없습니다</p>
            </div>
        )
    }
    return (
        <div className="prose prose-sm max-w-none text-foreground">
            {value.split("\n").map((line, i) => {
                const imgMatch = line.match(/!\[.*?\]\((.*?)\)/)
                if (imgMatch) {
                    return (
                        <img
                            key={i}
                            src={imgMatch[1]}
                            alt="미리보기"
                            className="max-w-full h-auto rounded-xl my-5 shadow-lg border"
                        />
                    )
                }
                if (line.startsWith("# ")) return <h1 key={i} className="text-2xl font-bold mt-4 mb-2">{line.slice(2)}</h1>
                if (line.startsWith("## ")) return <h2 key={i} className="text-xl font-bold mt-3 mb-1">{line.slice(3)}</h2>
                if (line.startsWith("### ")) return <h3 key={i} className="text-lg font-semibold mt-2 mb-1">{line.slice(4)}</h3>
                if (line.trim() === "") return <br key={i} />
                return <p key={i} className="leading-relaxed mb-1">{line}</p>
            })}
        </div>
    )
}

// ── Main Component ─────────────────────────────────────────────────
export function DescriptionEditor({ value, onChange, id = "description" }: DescriptionEditorProps) {
    const [showPreview, setShowPreview] = useState(false)
    const textareaRef = useRef<HTMLTextAreaElement>(null)
    const fileInputRef = useRef<HTMLInputElement>(null)

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0]
        if (!file) return

        const blobUrl = URL.createObjectURL(file)
        const markdownImage = `\n![업로드된 이미지](${blobUrl})\n`

        const textarea = textareaRef.current
        if (textarea) {
            const start = textarea.selectionStart
            const end = textarea.selectionEnd
            const newValue = value.substring(0, start) + markdownImage + value.substring(end)
            onChange(newValue)
            setTimeout(() => {
                textarea.focus()
                textarea.setSelectionRange(start + markdownImage.length, start + markdownImage.length)
            }, 0)
        } else {
            onChange(value + markdownImage)
        }

        if (fileInputRef.current) fileInputRef.current.value = ""
    }

    return (
        <div className="rounded-xl border bg-card shadow-sm overflow-hidden">
            {/* Header */}
            <div className="flex items-center justify-between px-4 py-3 border-b bg-card">
                <Label htmlFor={id} className="text-base font-semibold flex items-center gap-2">
                    <Type className="size-4" />
                    공연 상세 설명
                </Label>
                <div className="flex items-center gap-2">
                    <Button
                        variant="ghost"
                        size="sm"
                        className="h-8 gap-1.5 text-xs font-medium border border-dashed hover:border-solid transition-all"
                        onClick={() => fileInputRef.current?.click()}
                        type="button"
                    >
                        <ImagePlus className="size-3.5 text-primary" />
                        이미지 추가
                    </Button>
                    <Button
                        variant={showPreview ? "secondary" : "outline"}
                        size="sm"
                        className="h-8 gap-1.5 text-xs font-medium transition-all"
                        onClick={() => setShowPreview(!showPreview)}
                        type="button"
                    >
                        {showPreview ? (
                            <><EyeOff className="size-3.5" /> 미리보기 닫기</>
                        ) : (
                            <><Eye className="size-3.5" /> 미리보기</>
                        )}
                    </Button>
                    <input
                        type="file"
                        ref={fileInputRef}
                        onChange={handleFileChange}
                        accept="image/*"
                        className="hidden"
                    />
                </div>
            </div>

            {/* Editor + Preview Panel */}
            <div className="flex min-h-[480px]">
                {/* Writing pane — shrinks when preview is open */}
                <div
                    className={cn(
                        "flex flex-col transition-all duration-300 ease-in-out origin-left",
                        showPreview ? "w-1/2 border-r" : "w-full"
                    )}
                >
                    <div className="flex items-center gap-2 px-3 py-1.5 border-b bg-muted/30">
                        <span className="inline-block size-1.5 rounded-full bg-blue-500" />
                        <span className="text-[10px] text-muted-foreground font-medium tracking-wide uppercase">Editor</span>
                    </div>
                    <Textarea
                        id={id}
                        ref={textareaRef}
                        placeholder="관람객들에게 보여질 공연의 상세한 설명을 작성해주세요.
예시:
# 공연 소개
이번 공연은 ...

## 주의사항
- 공연 중 사진 촬영은 금지됩니다."
                        value={value}
                        onChange={(e) => onChange(e.target.value)}
                        className="flex-1 min-h-[440px] resize-none border-0 rounded-none focus-visible:ring-0 p-4 text-sm leading-relaxed bg-transparent font-mono"
                    />
                </div>

                {/* Preview pane — slides in from right */}
                <div
                    className={cn(
                        "flex flex-col transition-all duration-300 ease-in-out overflow-hidden",
                        showPreview ? "w-1/2 opacity-100" : "w-0 opacity-0"
                    )}
                >
                    <div className="flex items-center justify-between px-3 py-1.5 border-b bg-muted/30 shrink-0">
                        <div className="flex items-center gap-2">
                            <span className="inline-block size-1.5 rounded-full bg-green-500 animate-pulse" />
                            <span className="text-[10px] text-muted-foreground font-medium tracking-wide uppercase">Preview</span>
                        </div>
                        <button
                            type="button"
                            onClick={() => setShowPreview(false)}
                            className="text-muted-foreground hover:text-foreground transition-colors rounded-sm hover:bg-muted p-0.5"
                        >
                            <X className="size-3.5" />
                        </button>
                    </div>
                    <div className="flex-1 overflow-y-auto p-5">
                        <MarkdownPreview value={value} />
                    </div>
                </div>
            </div>

            {/* Footer */}
            <div className="px-4 py-2 border-t bg-muted/20 flex items-center justify-between">
                <p className="text-[10px] text-muted-foreground">
                    💡 <span className="font-medium"># 제목</span>, <span className="font-medium">## 소제목</span>, <span className="font-medium">![설명](이미지URL)</span> 마크다운 문법을 지원합니다.
                </p>
                <span className="text-[10px] text-muted-foreground">{value.length}자</span>
            </div>
        </div>
    )
}
