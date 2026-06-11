export function ContactPage({ variant = "contact" }: { variant?: "contact" | "partner" }) {
  return (
    <section className="section narrow">
      <p className="eyebrow">{variant === "partner" ? "Partner inquiry" : "Contact"}</p>
      <h1>{variant === "partner" ? "Work with Stayra" : "Contact us"}</h1>
      <form className="form-card">
        <label>Name<input name="name" /></label>
        <label>Email<input name="email" type="email" /></label>
        <label>Message<textarea name="message" /></label>
        <button type="button">Prepare message</button>
      </form>
      <p className="todo">TODO: The XML frontend had contact and partner flows, but this Spring Boot backend has no contact endpoint yet.</p>
    </section>
  );
}
