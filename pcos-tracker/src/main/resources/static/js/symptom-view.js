// symptom-view.js
// Purpose: View-only Symptom Page + Insights (no data entry)

document.addEventListener("DOMContentLoaded", () => {
  enableViewOnlyMode();
  renderSymptomFrequencyChart();
  renderSymptomTrendChart();
});

/* ==============================
   VIEW-ONLY MODE
============================== */
function enableViewOnlyMode() {
  document.querySelectorAll("input, textarea").forEach(el => {
    el.setAttribute("disabled", true);
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
  note.style.fontSize = "0.9rem";
  note.innerHTML = `
    <strong>Note:</strong>
    Symptoms are logged using the <b>Daily Log</b> on the dashboard.
    This page helps you review your symptoms and patterns.
  `;
  main.prepend(note);
}

/* ==============================
   FREQUENCY CHART (BAR)
============================== */
function renderSymptomFrequencyChart() {
  fetch("/symptoms/frequency", {
    headers: {
      "Authorization": `Bearer ${localStorage.getItem("token")}`
    }
  })
  .then(res => res.json())
  .then(scoreMap => {

    // 🟡 Empty-data guard
    if (!scoreMap || Object.keys(scoreMap).length === 0) {
      const insight = document.getElementById("symptomInsightText");
      if (insight) {
        insight.textContent =
          "Start logging symptoms to see insights here.";
      }
      return;
    }

    const canvas = document.getElementById("symptomFrequencyChart");
    if (!canvas || typeof Chart === "undefined") return;

    // 🛑 Prevent duplicate rendering
    if (canvas.chart) {
      canvas.chart.destroy();
    }

    const ctx = canvas.getContext("2d");

    // 🎨 Gradient for bars
    const gradient = ctx.createLinearGradient(0, 0, 0, canvas.height);
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
          barThickness: 36,
          hoverBackgroundColor: "#EE6B6B"
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: "#fff",
            titleColor: "#333",
            bodyColor: "#555",
            borderColor: "#F37A7A",
            borderWidth: 1
          }
        },
        scales: {
          x: {
            grid: { display: false },
            ticks: { font: { size: 13, weight: "600" } }
          },
          y: {
            beginAtZero: true,
            ticks: { stepSize: 2 },
            grid: {
              color: "rgba(0,0,0,0.06)",
              drawBorder: false
            }
          }
        }
      }
    });

    updateInsightText(scoreMap);
    highlightTopSymptoms(scoreMap);

  })
  .catch(err => console.error("Frequency chart error:", err));
}

/* ==============================
   TREND CHART (LINE)
============================== */
function renderSymptomTrendChart() {
  fetch("/symptoms/trend?days=7", {
    headers: {
      "Authorization": `Bearer ${localStorage.getItem("token")}`
    }
  })
  .then(res => res.json())
  .then(data => {
    const labels = data.map(d => d.date);
    const values = data.map(d => d.fatigue);

    const canvas = document.getElementById("symptomTrendChart");
    if (!canvas || typeof Chart === "undefined") return;

    // 🛑 Prevent duplicate rendering
    if (canvas.chart) {
      canvas.chart.destroy();
    }

    const ctx = canvas.getContext("2d");

    // 🎨 Gradient under line
    const gradient = ctx.createLinearGradient(0, 0, 0, canvas.height);
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
          borderWidth: 4,
          pointRadius: 6,
          pointHoverRadius: 8,
          pointBackgroundColor: "#F37A7A",
          pointBorderColor: "#fff",
          pointBorderWidth: 2,
          fill: true,
          tension: 0.45
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: "#fff",
            titleColor: "#333",
            bodyColor: "#555",
            borderColor: "#F37A7A",
            borderWidth: 1
          }
        },
        scales: {
          x: {
            grid: { display: false },
            ticks: { font: { size: 12 } }
          },
          y: {
            min: 0,
            max: 10,
            ticks: { stepSize: 2 },
            grid: {
              color: "rgba(0,0,0,0.06)",
              drawBorder: false
            }
          }
        }
      }
    });
  })
  .catch(err => console.error("Trend chart error:", err));
}

/* ==============================
   INSIGHT TEXT
============================== */
function updateInsightText(data) {
  if (!data || !Object.keys(data).length) return;

  const [topSymptom] = Object.entries(data)
    .sort((a, b) => b[1] - a[1])[0];

  const insightEl = document.getElementById("symptomInsightText");
  if (insightEl) {
    insightEl.textContent =
      `${topSymptom} appears most frequently in your recent symptom logs.`;
  }
}
function highlightTopSymptoms(scoreMap) {
  const top = Object.entries(scoreMap)
    .sort((a,b) => b[1] - a[1])
    .slice(0,2)
    .map(([name]) => name);

  document.querySelectorAll(".symptom-item").forEach(item => {
    if (top.includes(item.dataset.name)) {
      item.classList.add("highlight");
    }
  });
}
function highlightTopSymptoms(scoreMap) {
  const topSymptoms = Object.entries(scoreMap)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 2)           // highlight top 2
    .map(([name]) => name);

  document.querySelectorAll(".symptom-item").forEach(item => {
    const name = item.dataset.name;
    if (topSymptoms.includes(name)) {
      item.classList.add("highlight");
    }
  });
}


