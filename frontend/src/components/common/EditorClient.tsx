// @ts-nocheck
"use client";
import { lazy, Suspense } from "react";

const Editor = lazy(() => import("@/components/common/Editor"));

const EditorClient = ({ content, onChange }: { content: string, onChange: (content: string) => void }) => {
  return (
    <Suspense fallback={<p>Loading...</p>}>
      <Editor content={content} onChange={onChange} />
    </Suspense>
  );
}
export default EditorClient;
