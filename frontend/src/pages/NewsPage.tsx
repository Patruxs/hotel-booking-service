import { Link } from "react-router-dom";

export function NewsPage() {
  return (
    <section className="section">
      <p className="eyebrow">Updates</p>
      <h1>News</h1>
      <div className="panel">
        <p>The XML frontend included news routes, but no matching Spring Boot news endpoints exist in this repository.</p>
        <Link to="/admin/news">Review admin news TODO</Link>
      </div>
    </section>
  );
}
