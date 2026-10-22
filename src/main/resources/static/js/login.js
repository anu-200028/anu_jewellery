// --- LOGIN FORM SUBMISSION HANDLER ---
document.addEventListener("DOMContentLoaded", () => {
  const loginForm = document.getElementById("loginForm"); // Make sure your form has id="loginForm" in login.html
  
  if (!loginForm) return;

  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const usernameInput = document.getElementById("username").value;
    const passwordInput = document.getElementById("password").value;

    try {
      const response = await fetch("/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          username: usernameInput,
          password: passwordInput
        })
      });

      const result = await response.json().catch(() => ({}));

      if (!response.ok) {
        alert(result.message || "Invalid username or password");
        return;
      }

      // Safely grab the token regardless of whether your backend calls it 'token', 'accessToken', or 'jwt'
      const authToken = result.token || result.accessToken || result.jwt;

      // Save user details to browser storage
      localStorage.setItem("token", authToken);
      localStorage.setItem("username", result.username || usernameInput);
      localStorage.setItem("role", result.role || "");

      alert("Login successful!");

      // Smart Redirection based on Role
      const role = result.role ? result.role.toUpperCase() : "";

      if (role.includes("ADMIN")) {
        window.location.href = "/admin-home.html";
      } else {
        window.location.href = "/index.html";
      }

    } catch (error) {
      console.error("Login error:", error);
      alert("An error occurred during login.");
    }
  });
});