import { useContext } from "react";
import { AuthContext } from "../AuthContext";

export default function Main() {
  const { user } = useContext(AuthContext);

  return (
    <section>
      <h2>Main Page</h2>
      {user ? (
        <div>
          <p>Welcome, <strong>{user.email}</strong>!</p>
          <p>Role: {user.role}</p>
        </div>
      ) : (
        <p>You are not logged in. Please log in first.</p>
      )}
    </section>
  );
}
