import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { setAuthToken } from "@/lib/axios";
import { login } from "@/services/hotelApi";

export function LoginPage() {
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setSubmitting(true);
    try {
      const result = await login({
        email: String(form.get("email")),
        password: String(form.get("password")),
      });
      setAuthToken(result.token);
      toast.success("Logged in");
      navigate(result.role === "ADMIN" || result.role === "RECEPTIONIST" ? "/admin" : "/me");
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "Login failed");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="section narrow">
      <p className="eyebrow">Welcome back</p>
      <h1>Login</h1>
      <form className="form-card" onSubmit={onSubmit}>
        <label>
          Email
          <input name="email" type="email" required />
        </label>
        <label>
          Password
          <input name="password" type="password" required />
        </label>
        <button disabled={submitting} type="submit">{submitting ? "Logging in..." : "Login"}</button>
      </form>
      <p className="muted">New here? <Link to="/register">Create an account</Link>.</p>
    </section>
  );
}
