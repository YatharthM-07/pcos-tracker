// symptom-view.js
// Purpose: View-only Symptom Page + Insights (FIXED)

const API = "https://pcos-tracker-9a53.onrender.com";

document.addEventListener("DOMContentLoaded", () => {
  const token = localStorage.getItem("token");
  if (!token) {
    window.location.replace("/login.html");
    return;
  }

  enableViewOnlyMode();
  renderSymptomFrequencyChart(token);
  renderSymptomTrendChart(token);
});

/* ==============================
   VIEW-ONLY MODE
============================== */
function enableViewOnlyMode() {
  document.querySelectorAll("input, textarea").forEach(el => {
    el.disabled = true;
  });

  document.querySelectorAll(".symptom-item").forEach(item => {
    item.style.pointerEvents = "none";
    item.style.opacity = "0.95";
  });

  const saveBtn = document.querySelector("button.btn-pink");
  if (saveBtn) saveBtn.style.display = "none";

  injectInfoNote();
}

function injectInfoNote() {
  const main = document.querySelector(".main");
  if (!main) return;

  const note = document.createElement("div");
  note.className = "alert alert-light border rounded-3 mb-4";
  note.innerHTML = `
    <strong>Note:</strong>
    Symptoms are logged using the <b>Daily Log</b> on the dashboard.
    This page helps you review your symptoms and patterns.
  `;
  main.prepend(note);
}

/* ==============================
   FREQUENCY CHART
============================== */
function renderSymptomFrequencyChart(token) {
  fetch(`${API}/symptoms/frequency`, {  // ← CHANGED FROM /analytics/symptoms/frequency
    headers: {
      Authorization: `Bearer ${token}`
    }
  })
    .then(res => {
      if (!res.ok) throw new Error("Frequency API failed");
      return res.json();
    })
    .then(scoreMap => {
      if (!scoreMap || !Object.keys(scoreMap).length) {
        setInsight("Start logging symptoms to see insights here.");
        return;
      }

      const canvas = document.getElementById("symptomFrequencyChart");
      if (!canvas || typeof Chart === "undefined") return;

      if (canvas.chart) canvas.chart.destroy();

      const ctx = canvas.getContext("2d");
      const gradient = ctx.createLinearGradient(0, 0, 0, 320);
      gradient.addColorStop(0, "#F37A7A");
      gradient.addColorStop(1, "#FAD0D0");

      canvas.chart = new Chart(ctx, {
        type: "bar",
        data: {
          labels: Object.keys(scoreMap),
          datasets: [{
            data: Object.values(scoreMap),
            backgroundColor: gradient,
            borderRadius: 14,
            barThickness: 36
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { display: false } },
          scales: {
            y: { beginAtZero: true, ticks: { stepSize: 2 } }
          }
        }
      });

      updateInsightText(scoreMap);
      highlightTopSymptoms(scoreMap);
    })
    .catch(() => setInsight("Unable to load symptom insights."));
}

/* ==============================
   TREND CHART
============================== */
function renderSymptomTrendChart(token) {
  fetch(`${API}/symptoms/trend?days=7`, {  // ← CHANGED FROM /analytics/symptoms/trend
    headers: {
      Authorization: `Bearer ${token}`
    }
  })
    .then(res => {
      if (!res.ok) throw new Error("Trend API failed");
      return res.json();
    })
    .then(data => {
      if (!Array.isArray(data) || !data.length) return;

      const labels = data.map(d => d.date);
      const values = data.map(d => d.fatigue ?? 0);

      const canvas = document.getElementById("symptomTrendChart");
      if (!canvas || typeof Chart === "undefined") return;

      if (canvas.chart) canvas.chart.destroy();

      const ctx = canvas.getContext("2d");
      const gradient = ctx.createLinearGradient(0, 0, 0, 320);
      gradient.addColorStop(0, "rgba(243,122,122,0.35)");
      gradient.addColorStop(1, "rgba(243,122,122,0.05)");

      canvas.chart = new Chart(ctx, {
        type: "line",
        data: {
          labels,
          datasets: [{
            data: values,
            borderColor: "#F37A7A",
            backgroundColor: gradient,
            fill: true,
            tension: 0.45
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { display: false } },
          scales: {
            y: { min: 0, max: 10, ticks: { stepSize: 2 } }
          }
        }
      });
    })
    .catch(() => {});
}

/* ==============================
   INSIGHTS
============================== */
function updateInsightText(data) {
  const [top] = Object.entries(data).sort((a, b) => b[1] - a[1])[0];
  setInsight(`${top} appears most frequently in your recent logs.`);
}

function setInsight(text) {
  const el = document.getElementById("symptomInsightText");
  if (el) el.textContent = text;
}

function highlightTopSymptoms(scoreMap) {
  const top = Object.entries(scoreMap)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 2)
    .map(([k]) => k);

  document.querySelectorAll(".symptom-item").forEach(item => {
    if (top.includes(item.dataset.name)) {
      item.classList.add("highlight");
    }
  });
}