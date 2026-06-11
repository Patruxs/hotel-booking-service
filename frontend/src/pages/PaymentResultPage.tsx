import { useSearchParams } from "react-router-dom";

export function PaymentResultPage() {
  const [params] = useSearchParams();
  return (
    <section className="section narrow">
      <p className="eyebrow">Payment</p>
      <h1>Payment result</h1>
      <div className="panel">
        <p>Status: {params.get("status") || "pending"}</p>
        <p className="todo">TODO: Payment provider callback endpoints are not present in the Spring Boot backend.</p>
      </div>
    </section>
  );
}
