function logout() {
  localStorage.removeItem("token");
  window.location.replace("/index.html");
}

function requireAuth() {
  const token = localStorage.getItem("token");
  if (!token) {
    window.location.replace("/login.html");
  }
}
