/* ==============================
   DASHBOARD FLOATING AI
============================== */

let dashboardAIVisible = false;

function toggleDashboardAI() {
  const popup = document.getElementById("dashboardAIPopup");
  if (!popup) return;

  dashboardAIVisible = !dashboardAIVisible;
  popup.style.display = dashboardAIVisible ? "block" : "none";
}

function generateDashboardAI() {
  const token = localStorage.getItem("token");
  const output = document.getElementById("dashboardAIResponse");

  if (!token || !output) {
    output.innerHTML = "Please log in again 🌸";
    return;
  }

  output.innerHTML = "Maitri is thinking… ✨";

  fetch("/dashboard/ai-summary", {
    headers: {
      Authorization: "Bearer " + token
    }
  })
    .then(res => {
      if (!res.ok) throw new Error("AI failed");
      return res.text();
    })
    .then(text => {
      output.innerHTML = `<div style="white-space:pre-line;">${text}</div>`;
    })
    .catch(() => {
      output.innerHTML =
        "I’m here for you 💗<br/>But I couldn’t generate insights right now.";
    });
}
// ✅ Bind Floating AI button clicks safely
document.addEventListener("DOMContentLoaded", () => {
  const fab = document.getElementById("dashboardAIFab");
  const generateBtn = document.getElementById("generateDashboardAI");

  if (fab) {
    fab.addEventListener("click", () => {
      console.log("AI FAB clicked"); // 🔍 debug proof
      toggleDashboardAI();
    });
  }

  if (generateBtn) {
    generateBtn.addEventListener("click", () => {
      console.log("Generate AI clicked"); // 🔍 debug proof
      generateDashboardAI();
    });
  }
});

