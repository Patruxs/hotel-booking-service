import { RouteHeader } from "@/components/RouteShell";
import { SimpleTiptapEditor } from "@/components/editor/SimpleTiptapEditor";

export function EditorPage() {
  return (
    <section className="mx-auto max-w-5xl px-4 py-8">
      <RouteHeader title="TipTap editor" description="Internal editor route migrated to a Vite-safe TipTap instance." />
      <SimpleTiptapEditor />
    </section>
  );
}

export function SimpleEditorPage() {
  return (
    <section className="mx-auto max-w-5xl px-4 py-8">
      <RouteHeader title="Simple editor" description="Internal development route preserved with the same editor core." />
      <SimpleTiptapEditor />
    </section>
  );
}
