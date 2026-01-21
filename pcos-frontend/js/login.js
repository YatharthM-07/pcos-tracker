// login.js

document.addEventListener("DOMContentLoaded", () => {

  const form = document.querySelector("form");
  const error = document.getElementById("loginError");

  form.addEventListener("submit", handleLogin);

  function hideLoginError() {
    error.style.display = "none";
  }

  window.hideLoginError = hideLoginError;

  window.toggleLoginPassword = function (event) {
    const pwd = document.getElementById("loginPassword");
    const icon = event.target;

    if (pwd.type === "password") {
      pwd.type = "text";
      icon.classList.replace("fa-eye", "fa-eye-slash");
    } else {
      pwd.type = "password";
      icon.classList.replace("fa-eye-slash", "fa-eye");
    }
  };

  function handleLogin(event) {
    event.preventDefault();

    const email = document.getElementById("loginEmail").value.trim();
    const password = document.getElementById("loginPassword").value.trim();

    if (!email || !password) {
      error.innerText = "Please enter email and password.";
      error.style.display = "block";
      return;
    }

    fetch("https://pcos-tracker-9a53.onrender.com/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password })
    })
      .then(res => {
        if (!res.ok) throw new Error("Invalid email or password");
        return res.json();
      })
      .then(data => {
        // 🔑 STORE TOKEN FIRST
        localStorage.setItem("token", data.token);

        // 🧭 THEN REDIRECT (STATIC HTML ON FIREBASE)
        window.location.href = "/user-dashboard.html";
      })
      .catch(err => {
        error.innerText = err.message;
        error.style.display = "block";
      });
  }
});
