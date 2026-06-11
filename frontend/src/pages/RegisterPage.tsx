import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { register } from "@/services/hotelApi";

export function RegisterPage() {
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setSubmitting(true);
    try {
      await register({
        fullName: String(form.get("fullName")),
        email: String(form.get("email")),
        phone: String(form.get("phone")),
        password: String(form.get("password")),
        dob: String(form.get("dob")),
      });
      toast.success("Account created");
      navigate("/login");
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "Registration failed");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="section narrow">
      <p className="eyebrow">Join Stayra</p>
      <h1>Register</h1>
      <form className="form-card" onSubmit={onSubmit}>
        <label>Full name<input name="fullName" required /></label>
        <label>Email<input name="email" type="email" required /></label>
        <label>Phone<input name="phone" required /></label>
        <label>Date of birth<input name="dob" type="date" required /></label>
        <label>Password<input name="password" type="password" minLength={6} required /></label>
        <button disabled={submitting} type="submit">{submitting ? "Creating..." : "Create account"}</button>
      </form>
    </section>
  );
}
