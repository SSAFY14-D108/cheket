"use client"

import { useState, useRef } from "react"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { ImagePlus, Type } from "lucide-react"

interface DescriptionEditorProps {
    value: string
    onChange: (value: string) => void
    id?: string
}

export function DescriptionEditor({ value, onChange, id = "description" }: DescriptionEditorProps) {
    const [imageUrl, setImageUrl] = useState("")
    const [isImageInputVisible, setIsImageInputVisible] = useState(false)
    const textareaRef = useRef<HTMLTextAreaElement>(null)

    const handleAddImage = () => {
        if (!imageUrl.trim()) return

        const markdownImage = `\n![이미지](${imageUrl.trim()})\n`

        // Insert at cursor position or append
        const textarea = textareaRef.current
        if (textarea) {
            const start = textarea.selectionStart
            const end = textarea.selectionEnd
            const newValue = value.substring(0, start) + markdownImage + value.substring(end)
            onChange(newValue)

            // Focus back and move cursor
            setTimeout(() => {
                textarea.focus()
                textarea.setSelectionRange(start + markdownImage.length, start + markdownImage.length)
            }, 0)
        } else {
            onChange(value + markdownImage)
        }

        setImageUrl("")
        setIsImageInputVisible(false)
    }

    return (
        <div className="flex flex-col gap-2 rounded-md border p-4 bg-card">
            <div className="flex items-center justify-between border-b pb-2">
                <Label htmlFor={id} className="text-base font-semibold flex items-center gap-2">
                    <Type className="size-4" />
                    공연 상세 설명
                </Label>
                <Button
                    variant="outline"
                    size="sm"
                    className="h-8 gap-1.5"
                    onClick={() => setIsImageInputVisible(!isImageInputVisible)}
                    type="button"
                >
                    <ImagePlus className="size-4" />
                    이미지 추가
                </Button>
            </div>

            {isImageInputVisible && (
                <div className="flex items-center gap-2 mt-2 bg-muted/50 p-2 rounded-md">
                    <Input
                        placeholder="이미지 URL을 입력하세요 (https://...)"
                        value={imageUrl}
                        onChange={(e) => setImageUrl(e.target.value)}
                        className="h-8 text-sm"
                        onKeyDown={(e) => {
                            if (e.key === "Enter") {
                                e.preventDefault()
                                handleAddImage()
                            }
                        }}
                    />
                    <Button size="sm" className="h-8" onClick={handleAddImage} type="button">
                        삽입
                    </Button>
                </div>
            )}

            <Textarea
                id={id}
                ref={textareaRef}
                placeholder="관람객들에게 보여질 공연의 상세한 설명을 작성해주세요. 블로그처럼 자유롭게 작성 가능합니다."
                value={value}
                onChange={(e) => onChange(e.target.value)}
                className="min-h-[300px] mt-2 resize-y border-0 focus-visible:ring-0 p-0 text-base leading-relaxed bg-transparent"
            />

            <div className="text-xs text-muted-foreground mt-2 text-right">
                마크다운 형식을 지원합니다.
            </div>
        </div>
    )
}
