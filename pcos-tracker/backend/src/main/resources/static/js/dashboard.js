// dashboard.js

document.addEventListener("DOMContentLoaded", () => {
  console.log("Dashboard JS loaded");

  /* ==============================
     AUTH CHECK
  ============================== */
  const token = localStorage.getItem("token");
  if (!token) {
    window.location.replace("/login");
    return;
  }

  /* ==============================
     LOAD USER INFO
  ============================== */
  fetch("/auth/me", {
    headers: { Authorization: "Bearer " + token }
  })
    .then(res => res.json())
    .then(user => {
      const usernameEl = document.getElementById("username");
      if (usernameEl && user.name) {
        usernameEl.textContent = `Hi, ${user.name} 🌸`;
      }
    })
    .catch(err => console.error("User load error", err));

  /* ==============================
     DASHBOARD ANALYTICS
  ============================== */
  fetch("/analytics/dashboard", {
    headers: { Authorization: "Bearer " + token }
  })
    .then(res => res.json())
    .then(data => renderDashboardCards(data))
    .catch(err => console.error("Dashboard error", err));

  /* ==============================
     CALENDAR (READ-ONLY)
  ============================== */
  const calGrid = document.getElementById("calendarGrid");
  const monthLbl = document.getElementById("monthLabel");
  const saveBtn = document.getElementById("savePeriodBtn");

  let view = new Date();

  if (saveBtn) saveBtn.style.display = "none";
  calGrid.style.pointerEvents = "none";

  function renderCalendar() {
    const y = view.getFullYear();
    const m = view.getMonth();
    const today = new Date();

    monthLbl.textContent = view.toLocaleString("default", {
      month: "long",
      year: "numeric"
    });

    calGrid.innerHTML = "";

    const firstDay = new Date(y, m, 1).getDay();
    const daysInMonth = new Date(y, m + 1, 0).getDate();
    const prevMonthDays = new Date(y, m, 0).getDate();

    for (let i = 0; i < 42; i++) {
      const cell = document.createElement("div");
      cell.className = "cal-cell";

      if (i < firstDay) {
        cell.textContent = prevMonthDays - (firstDay - 1 - i);
        cell.classList.add("disabled");
      } else if (i < firstDay + daysInMonth) {
        const day = i - firstDay + 1;
        cell.textContent = day;

        if (
          day === today.getDate() &&
          m === today.getMonth() &&
          y === today.getFullYear()
        ) {
          cell.classList.add("today");
        }
      } else {
        cell.textContent = i - (firstDay + daysInMonth) + 1;
        cell.classList.add("disabled");
      }

      calGrid.appendChild(cell);
    }
  }

  renderCalendar();

  document.getElementById("prevBtn").onclick = () => {
    view = new Date(view.getFullYear(), view.getMonth() - 1, 1);
    renderCalendar();
    loadData();
  };

  document.getElementById("nextBtn").onclick = () => {
    view = new Date(view.getFullYear(), view.getMonth() + 1, 1);
    renderCalendar();
    loadData();
  };

  /* ==============================
     LOAD PERIODS + CYCLES
  ============================== */
  function loadData() {
    fetch("/cycles/my-periods", {
      headers: { Authorization: "Bearer " + token }
    })
      .then(res => res.json())
      .then(periods => paintPeriods(periods))
      .catch(err => console.error("Period load error", err));

    fetch("/cycles/my-cycles", {
      headers: { Authorization: "Bearer " + token }
    })
      .then(res => res.json())
      .then(cycles => renderCycleTimeline(cycles))
      .catch(err => console.error("Cycle load error", err));
  }

  function paintPeriods(periods) {
    document.querySelectorAll(".cal-cell").forEach(c =>
      c.classList.remove("period")
    );

    periods.forEach(p => {
      const start = new Date(p.startDate);
      const end = new Date(p.endDate);

      document.querySelectorAll(".cal-cell").forEach(cell => {
        if (cell.classList.contains("disabled")) return;

        const day = Number(cell.textContent);
        const date = new Date(view.getFullYear(), view.getMonth(), day);

        if (date >= start && date <= end) {
          cell.classList.add("period");
        }
      });
    });
  }

  /* ==============================
     CYCLE TIMELINE (LAST 3)
  ============================== */
  function renderCycleTimeline(cycles) {
    const timeline = document.getElementById("cycleHistory");
    if (!timeline) return;

    timeline.innerHTML = "";

    if (!cycles || cycles.length === 0) {
      timeline.innerHTML = "<p>No cycle history available.</p>";
      return;
    }

    cycles
      .sort((a, b) => new Date(b.startDate) - new Date(a.startDate))
      .slice(0, 3)
      .forEach(c => {
        const start = new Date(c.startDate);
        const end = new Date(c.endDate);
        const p = document.createElement("p");
        p.innerText = `${start.toDateString()} – ${end.toDateString()} (${c.duration} days)`;
        timeline.appendChild(p);
      });
  }

  loadData();

  /* ==============================
     DASHBOARD CARDS
  ============================== */
  function renderDashboardCards(data) {
    const cycleLen = document.getElementById("cycleLength");
    const prevPeriod = document.getElementById("previousPeriod");
    const nextPeriod = document.getElementById("nextPeriod");
    const regEl = document.getElementById("regularityScore");
    const statusEl = document.getElementById("cycleStatus");

    const avg = data.averageCycleLength;

    if (cycleLen && avg != null) {
      cycleLen.innerText = `${Math.round(avg)} days`;
    }

    if (statusEl && avg != null) {
      if (avg >= 26 && avg <= 35) {
        statusEl.innerText = "Normal";
        statusEl.style.color = "#2ecc71";
      } else {
        statusEl.innerText = "Irregular";
        statusEl.style.color = "#e74c3c";
      }
    }

    if (prevPeriod) {
      prevPeriod.innerText =
        data.previousPeriodLength != null
          ? `${data.previousPeriodLength} days`
          : "--";
    }

    if (nextPeriod) {
      const n = data.nextPeriodIn;
      nextPeriod.innerText =
        n == null ? "--" : n < 0 ? `${Math.abs(n)} overdue` : `${n} days`;
    }

    if (regEl && data.regularityScore != null) {
      const r = data.regularityScore;
      regEl.innerText = r;
      regEl.style.color =
        r >= 80 ? "#2ecc71" : r >= 60 ? "#f39c12" : "#e74c3c";
    }

    const insights = document.getElementById("insightsList");
    if (insights) {
      insights.innerHTML = "";
      (data.insights || []).forEach(t => {
        const li = document.createElement("li");
        li.innerText = t;
        insights.appendChild(li);
      });
    }
  }
});
