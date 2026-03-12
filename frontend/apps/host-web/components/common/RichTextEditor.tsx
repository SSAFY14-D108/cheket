"use client"

import { useEffect, useRef, type ChangeEvent, type ComponentType } from "react"
import { EditorContent, type Editor, useEditor } from "@tiptap/react"
import StarterKit from "@tiptap/starter-kit"
import TiptapImage from "@tiptap/extension-image"
import Placeholder from "@tiptap/extension-placeholder"
import Underline from "@tiptap/extension-underline"
import { Markdown, type MarkdownStorage } from "tiptap-markdown"
import {
  Bold,
  Heading1,
  Heading2,
  Heading3,
  ImagePlus,
  Italic,
  List,
  ListOrdered,
  Minus,
  Redo2,
  Strikethrough,
  Underline as UnderlineIcon,
  Undo2,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { cn } from "@/lib/utils"

interface RichTextEditorProps {
  value: string
  onChange: (md: string) => void
  placeholder?: string
  minHeight?: number
}

interface ToolbarButtonProps {
  editor: Editor | null
  label: string
  icon: ComponentType<{ className?: string }>
  onClick: () => void
  isActive?: boolean
  disabled?: boolean
}

function ToolbarButton({
  editor,
  label,
  icon: Icon,
  onClick,
  isActive = false,
  disabled = false,
}: ToolbarButtonProps) {
  return (
    <Button
      type="button"
      variant="ghost"
      size="sm"
      className={cn("h-8 px-2", isActive && "bg-muted")}
      onClick={onClick}
      disabled={!editor || disabled}
      aria-label={label}
      title={label}
    >
      <Icon className="size-4" />
    </Button>
  )
}

function ToolbarDivider() {
  return <div className="h-5 w-px bg-border" aria-hidden="true" />
}

function EditorToolbar({
  editor,
  onImageUpload,
}: {
  editor: Editor | null
  onImageUpload: () => void
}) {
  return (
    <div className="flex flex-wrap items-center gap-1 border-b bg-muted/20 px-3 py-2">
      <ToolbarButton
        editor={editor}
        label="굵게"
        icon={Bold}
        onClick={() => editor?.chain().focus().toggleBold().run()}
        isActive={Boolean(editor?.isActive("bold"))}
      />
      <ToolbarButton
        editor={editor}
        label="기울임"
        icon={Italic}
        onClick={() => editor?.chain().focus().toggleItalic().run()}
        isActive={Boolean(editor?.isActive("italic"))}
      />
      <ToolbarButton
        editor={editor}
        label="밑줄"
        icon={UnderlineIcon}
        onClick={() => editor?.chain().focus().toggleUnderline().run()}
        isActive={Boolean(editor?.isActive("underline"))}
      />
      <ToolbarButton
        editor={editor}
        label="취소선"
        icon={Strikethrough}
        onClick={() => editor?.chain().focus().toggleStrike().run()}
        isActive={Boolean(editor?.isActive("strike"))}
      />

      <ToolbarDivider />

      <ToolbarButton
        editor={editor}
        label="제목 1"
        icon={Heading1}
        onClick={() => editor?.chain().focus().toggleHeading({ level: 1 }).run()}
        isActive={Boolean(editor?.isActive("heading", { level: 1 }))}
      />
      <ToolbarButton
        editor={editor}
        label="제목 2"
        icon={Heading2}
        onClick={() => editor?.chain().focus().toggleHeading({ level: 2 }).run()}
        isActive={Boolean(editor?.isActive("heading", { level: 2 }))}
      />
      <ToolbarButton
        editor={editor}
        label="제목 3"
        icon={Heading3}
        onClick={() => editor?.chain().focus().toggleHeading({ level: 3 }).run()}
        isActive={Boolean(editor?.isActive("heading", { level: 3 }))}
      />

      <ToolbarDivider />

      <ToolbarButton
        editor={editor}
        label="글머리 목록"
        icon={List}
        onClick={() => editor?.chain().focus().toggleBulletList().run()}
        isActive={Boolean(editor?.isActive("bulletList"))}
      />
      <ToolbarButton
        editor={editor}
        label="번호 목록"
        icon={ListOrdered}
        onClick={() => editor?.chain().focus().toggleOrderedList().run()}
        isActive={Boolean(editor?.isActive("orderedList"))}
      />
      <ToolbarButton
        editor={editor}
        label="구분선"
        icon={Minus}
        onClick={() => editor?.chain().focus().setHorizontalRule().run()}
      />

      <ToolbarDivider />

      <ToolbarButton
        editor={editor}
        label="이미지"
        icon={ImagePlus}
        onClick={onImageUpload}
      />

      <ToolbarDivider />

      <ToolbarButton
        editor={editor}
        label="실행 취소"
        icon={Undo2}
        onClick={() => editor?.chain().focus().undo().run()}
        disabled={!editor?.can().chain().focus().undo().run()}
      />
      <ToolbarButton
        editor={editor}
        label="다시 실행"
        icon={Redo2}
        onClick={() => editor?.chain().focus().redo().run()}
        disabled={!editor?.can().chain().focus().redo().run()}
      />
    </div>
  )
}

export function RichTextEditor({
  value,
  onChange,
  placeholder = "공연 상세 설명을 입력하세요...",
  minHeight = 480,
}: RichTextEditorProps) {
  const fileInputRef = useRef<HTMLInputElement>(null)
  const isSyncingRef = useRef(false)

  const getMarkdown = (currentEditor: Editor) =>
    (currentEditor.storage as typeof currentEditor.storage & { markdown: MarkdownStorage }).markdown.getMarkdown()

  const editor = useEditor({
    extensions: [
      StarterKit,
      TiptapImage,
      Underline,
      Placeholder.configure({
        placeholder,
      }),
      Markdown,
    ],
    content: value,
    immediatelyRender: false,
    onUpdate: ({ editor: currentEditor }) => {
      if (isSyncingRef.current) {
        return
      }

      onChange(getMarkdown(currentEditor))
    },
  })

  useEffect(() => {
    if (!editor) {
      return
    }

    const currentMarkdown = getMarkdown(editor)

    if (currentMarkdown === value) {
      return
    }

    isSyncingRef.current = true
    editor.commands.setContent(value || "")
    isSyncingRef.current = false
  }, [editor, value])

  const handleImageUpload = () => {
    fileInputRef.current?.click()
  }

  const handleFileChange = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]

    if (!file || !editor) {
      return
    }

    const blobUrl = URL.createObjectURL(file)
    editor.chain().focus().setImage({ src: blobUrl, alt: "업로드된 이미지" }).run()
    event.target.value = ""
  }

  return (
    <div className="tiptap-editor">
      <EditorToolbar editor={editor} onImageUpload={handleImageUpload} />
      <div className="bg-card">
        <EditorContent editor={editor} />
      </div>
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        className="hidden"
        onChange={handleFileChange}
      />

      <style jsx global>{`
        .tiptap-editor .ProseMirror {
          min-height: ${minHeight}px;
          padding: 1rem;
          outline: none;
          line-height: 1.7;
        }

        .tiptap-editor .ProseMirror p.is-editor-empty:first-child::before {
          content: attr(data-placeholder);
          color: hsl(var(--muted-foreground));
          pointer-events: none;
          float: left;
          height: 0;
        }

        .tiptap-editor .ProseMirror h1 {
          margin: 1rem 0 0.5rem;
          font-size: 1.5rem;
          font-weight: 700;
        }

        .tiptap-editor .ProseMirror h2 {
          margin: 0.75rem 0 0.25rem;
          font-size: 1.25rem;
          font-weight: 700;
        }

        .tiptap-editor .ProseMirror h3 {
          margin: 0.5rem 0 0.25rem;
          font-size: 1.125rem;
          font-weight: 600;
        }

        .tiptap-editor .ProseMirror img {
          margin: 1.25rem 0;
          max-width: 100%;
          height: auto;
          border-radius: 0.75rem;
        }

        .tiptap-editor .ProseMirror ul {
          list-style: disc;
          padding-left: 1.5rem;
        }

        .tiptap-editor .ProseMirror ol {
          list-style: decimal;
          padding-left: 1.5rem;
        }

        .tiptap-editor .ProseMirror hr {
          margin: 1rem 0;
          border-color: hsl(var(--border));
        }

        .tiptap-editor .ProseMirror blockquote {
          margin: 1rem 0;
          border-left: 3px solid hsl(var(--border));
          padding-left: 1rem;
          color: hsl(var(--muted-foreground));
        }

        .tiptap-editor .ProseMirror pre {
          overflow-x: auto;
          border-radius: 0.75rem;
          background: hsl(var(--muted));
          padding: 0.75rem 1rem;
        }

        .tiptap-editor .ProseMirror code {
          border-radius: 0.375rem;
          background: hsl(var(--muted));
          padding: 0.125rem 0.375rem;
        }
      `}</style>
    </div>
  )
}
