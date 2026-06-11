export function StaticFlowPage({ title, unsupported }: { title: string; unsupported?: string }) {
  return (
    <section className="section narrow">
      <p className="eyebrow">Migrated route</p>
      <h1>{title}</h1>
      {unsupported ? <p className="todo">TODO: Missing backend support for {unsupported}.</p> : null}
    </section>
  );
}
