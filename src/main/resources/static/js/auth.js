// --- REGISTRATION HANDLER ---
async function registerUser(event) {
  event.preventDefault();

  const data = {
    name: document.getElementById("name").value,
    username: document.getElementById("username").value,
    password: document.getElementById("password").value
  };

  try {
    const response = await fetch("/auth/register", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(data)
    });

    const result = await response.json().catch(() => ({}));

    const messageEl = document.getElementById("message");
    if (messageEl) {
      messageEl.innerText = result.message || "Registration completed";
    }

    if (response.ok) {
      // Automatically redirect to login page after successful signup
      setTimeout(() => {
        window.location.href = "/login.html";
      }, 1000);
    }
  } catch (error) {
    console.error("Registration error:", error);
    const messageEl = document.getElementById("message");
    if (messageEl) messageEl.innerText = "An error occurred during registration.";
  }
}

// --- LOGIN HANDLER ---
async function loginUser(event, isAdminLogin = false) {
  event.preventDefault();

  const data = {
    username: document.getElementById("username").value,
    password: document.getElementById("password").value
  };

  try {
    const response = await fetch("/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(data)
    });

    const result = await response.json().catch(() => ({}));

    if (!response.ok) {
      const messageEl = document.getElementById("message");
      if (messageEl) {
        messageEl.innerText = result.message || "Invalid credentials";
      } else {
        alert(result.message || "Invalid credentials");
      }
      return;
    }

    // Save tokens and user info
    localStorage.setItem("token", result.token);
    localStorage.setItem("username", result.username);
    localStorage.setItem("role", result.role);

    // Smart Redirection based on Role
    const role = result.role ? result.role.toUpperCase() : "";

    if (role.includes("ADMIN")) {
      window.location.href = "/admin-home.html";
    } else {
      // Redirect customers immediately to index.html storefront
      window.location.href = "/index.html";
    }

  } catch (error) {
    console.error("Login error:", error);
    alert("An error occurred during login.");
  }
}

// --- DOM EVENT LISTENERS ---
document.addEventListener("DOMContentLoaded", () => {
  const signupForm = document.getElementById("signupForm");
  const loginForm = document.getElementById("loginForm");
  const adminLoginForm = document.getElementById("adminLoginForm");

  if (signupForm) {
    signupForm.addEventListener("submit", registerUser);
  }

  if (loginForm) {
    loginForm.addEventListener("submit", (e) => loginUser(e, false));
  }

  if (adminLoginForm) {
    adminLoginForm.addEventListener("submit", (e) => loginUser(e, true));
  }
});