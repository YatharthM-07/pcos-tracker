function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("user");
  window.location.replace("/");
}

function requireAuth() {
  const token = localStorage.getItem("token");
  if (!token) {
    window.location.replace("/");
  }
} 
