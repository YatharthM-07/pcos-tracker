/* ==============================
   DASHBOARD AI (HEALTH SUMMARY)
   ============================== */

let dashboardAIVisible = false;

/* Toggle Dashboard AI Popup */
function toggleDashboardAI() {
  const popup = document.getElementById("dashboardAIPopup");
  if (!popup) {
    console.error("Dashboard AI popup not found");
    return;
  }

  dashboardAIVisible = !dashboardAIVisible;
  popup.classList.toggle("show", dashboardAIVisible);
}

/* Call Backend Gemini AI */
function generateDashboardAI() {
  const token = localStorage.getItem("token");
  const output = document.getElementById("dashboardAIResponse");

  if (!output) {
    console.error("dashboardAIResponse element missing");
    return;
  }

  if (!token) {
    output.innerHTML = "Please log in again 🌸";
    return;
  }

  output.innerHTML = "Maitri is thinking… ✨";

  fetch("/dashboard/ai-summary", {
    method: "GET",
    headers: {
      "Authorization": "Bearer " + token
    }
  })
    .then(res => {
      if (!res.ok) throw new Error("AI request failed");
      return res.json(); // ✅ IMPORTANT
    })
    .then(data => {
      output.innerHTML = `
        <div style="white-space: pre-line; font-size: 0.95rem;">
          ${data.aiSummary}
        </div>
      `;
    })
    .catch(err => {
      console.error("Dashboard AI error:", err);
      output.innerHTML =
        "I’m here for you 💗<br/>But I couldn’t generate insights right now.";
    });
}

/* Safe Event Binding */
document.addEventListener("DOMContentLoaded", () => {
  console.log("dashboard-ai.js loaded");

  const fab = document.getElementById("dashboardAIFab");
  const generateBtn = document.getElementById("generateDashboardAI");

  if (fab) {
    fab.addEventListener("click", () => {
      console.log("Dashboard AI FAB clicked");
      toggleDashboardAI();
    });
  } else {
    console.warn("dashboardAIFab not found");
  }

  if (generateBtn) {
    generateBtn.addEventListener("click", () => {
      console.log("Generate Dashboard AI clicked");
      generateDashboardAI();
    });
  } else {
    console.warn("generateDashboardAI button not found");
  }
});
