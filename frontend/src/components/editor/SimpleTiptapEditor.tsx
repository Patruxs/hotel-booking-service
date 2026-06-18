import { EditorContent, useEditor } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import { Bold, Heading2, Italic, List, Undo2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

const starterContent = `
  <h2>Hotel booking content editor</h2>
  <p>This Vite-safe TipTap editor preserves the old internal editor route while backend content APIs are pending.</p>
`;

export function SimpleTiptapEditor() {
  const editor = useEditor({
    extensions: [StarterKit],
    content: starterContent,
    editorProps: {
      attributes: {
        class: "min-h-64 rounded-md border bg-background p-4 leading-7 outline-none prose prose-neutral max-w-none",
      },
    },
  });

  return (
    <Card>
      <CardContent className="grid gap-3 p-4">
        <div className="flex flex-wrap gap-2">
          <Button
            type="button"
            variant="secondary"
            onClick={() => editor?.chain().focus().toggleBold().run()}
            aria-label="Bold"
            title="Bold"
          >
            <Bold className="h-4 w-4" />
          </Button>
          <Button
            type="button"
            variant="secondary"
            onClick={() => editor?.chain().focus().toggleItalic().run()}
            aria-label="Italic"
            title="Italic"
          >
            <Italic className="h-4 w-4" />
          </Button>
          <Button
            type="button"
            variant="secondary"
            onClick={() => editor?.chain().focus().toggleHeading({ level: 2 }).run()}
            aria-label="Heading"
            title="Heading"
          >
            <Heading2 className="h-4 w-4" />
          </Button>
          <Button
            type="button"
            variant="secondary"
            onClick={() => editor?.chain().focus().toggleBulletList().run()}
            aria-label="Bullet list"
            title="Bullet list"
          >
            <List className="h-4 w-4" />
          </Button>
          <Button
            type="button"
            variant="secondary"
            onClick={() => editor?.chain().focus().undo().run()}
            aria-label="Undo"
            title="Undo"
          >
            <Undo2 className="h-4 w-4" />
          </Button>
        </div>
        <EditorContent editor={editor} />
      </CardContent>
    </Card>
  );
}
