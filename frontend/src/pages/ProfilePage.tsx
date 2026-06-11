import { useAsync } from "@/hooks/useAsync";
import { getCurrentUser } from "@/services/hotelApi";

export function ProfilePage() {
  const { data: user, error, loading } = useAsync(getCurrentUser, []);

  return (
    <div>
      <p className="eyebrow">Account</p>
      <h1>Profile</h1>
      {loading ? <p className="muted">Loading profile...</p> : null}
      {error ? <p className="error">{error}</p> : null}
      {user ? (
        <article className="panel">
          <h2>{user.fullName}</h2>
          <p>{user.email}</p>
          <p>{user.phone}</p>
          <p>{user.dob}</p>
        </article>
      ) : null}
    </div>
  );
}
