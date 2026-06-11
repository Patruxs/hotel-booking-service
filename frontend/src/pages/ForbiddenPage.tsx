import { Link } from "react-router-dom";

export function ForbiddenPage() {
  return (
    <section className="section narrow">
      <h1>Forbidden</h1>
      <p>You do not have access to this area.</p>
      <Link to="/">Return home</Link>
    </section>
  );
}
