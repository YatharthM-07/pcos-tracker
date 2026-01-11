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
    });

  /* ==============================
     DASHBOARD ANALYTICS (CARDS)
  ============================== */
  fetch("/analytics/dashboard", {
    headers: { Authorization: "Bearer " + token }
  })
    .then(res => res.json())
    .then(data => renderDashboardCards(data));

  /* ==============================
     CALENDAR SETUP
  ============================== */
  const calGrid = document.getElementById("calendarGrid");
  const monthLbl = document.getElementById("monthLabel");
  const saveBtn = document.getElementById("savePeriodBtn");

  // 🔒 Dashboard calendar is READ-ONLY
  if (saveBtn) saveBtn.style.display = "none";

  let viewDate = new Date();
  let cachedCycles = [];

  /* ==============================
     RENDER CALENDAR (NO LAYOUT TOUCH)
  ============================== */
  function renderCalendar() {
    const year = viewDate.getFullYear();
    const month = viewDate.getMonth();
    const today = new Date();

    monthLbl.textContent = viewDate.toLocaleString("default", {
      month: "long",
      year: "numeric"
    });

    calGrid.innerHTML = "";

    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const prevMonthDays = new Date(year, month, 0).getDate();

    for (let i = 0; i < 42; i++) {
      const cell = document.createElement("div");
      cell.className = "cal-cell";

      let dateObj;

      if (i < firstDay) {
        const d = prevMonthDays - (firstDay - 1 - i);
        cell.textContent = d;
        cell.classList.add("disabled");
        dateObj = new Date(year, month - 1, d);
      }
      else if (i < firstDay + daysInMonth) {
        const d = i - firstDay + 1;
        cell.textContent = d;
        dateObj = new Date(year, month, d);

        if (
          d === today.getDate() &&
          month === today.getMonth() &&
          year === today.getFullYear()
        ) {
          cell.classList.add("today");
        }
      }
      else {
        const d = i - (firstDay + daysInMonth) + 1;
        cell.textContent = d;
        cell.classList.add("disabled");
        dateObj = new Date(year, month + 1, d);
      }

      cell.dataset.date = dateObj.toISOString().split("T")[0];
      calGrid.appendChild(cell);
    }

    paintCycleHistory();
  }

  /* ==============================
     PAINT PERIOD HISTORY (ONLY CLASSES)
  ============================== */
  function paintCycleHistory() {
    const cells = document.querySelectorAll(".cal-cell");

    cells.forEach(c =>
      c.classList.remove("period", "period-faded")
    );

    cachedCycles.forEach(cycle => {
      const start = cycle.startDate;
      const end = cycle.endDate;

      cells.forEach(cell => {
        const d = cell.dataset.date;
        if (!d) return;

        if (d >= start && d <= end) {
          cell.classList.add(
            cell.classList.contains("disabled")
              ? "period-faded"
              : "period"
          );
        }
      });
    });
  }

  /* ==============================
     FETCH CYCLE HISTORY (ONCE)
  ============================== */
  fetch("/cycles/my-cycles", {
    headers: { Authorization: "Bearer " + token }
  })
    .then(res => res.json())
    .then(cycles => {
      cachedCycles = cycles || [];
      renderCalendar();
      renderCycleTimeline(cycles);
    });

  /* ==============================
     MONTH NAVIGATION
  ============================== */
  document.getElementById("prevBtn").onclick = () => {
    viewDate = new Date(viewDate.getFullYear(), viewDate.getMonth() - 1, 1);
    renderCalendar();
  };

  document.getElementById("nextBtn").onclick = () => {
    viewDate = new Date(viewDate.getFullYear(), viewDate.getMonth() + 1, 1);
    renderCalendar();
  };

  /* ==============================
     CYCLE HISTORY TIMELINE
     (NO CONTAINER REWRITE)
  ============================== */
  function renderCycleTimeline(cycles) {
    const timeline = document.getElementById("cycleHistory");
    if (!timeline) return;

    // keep existing heading, just append entries
    const existing = timeline.querySelectorAll(".cycle-entry");
    existing.forEach(e => e.remove());

    cycles
      .sort((a, b) => new Date(b.startDate) - new Date(a.startDate))
      .slice(0, 3)
      .forEach(c => {
        const p = document.createElement("p");
        p.className = "cycle-entry";
        p.innerText = `${c.startDate} – ${c.endDate} (${c.duration} days)`;
        timeline.appendChild(p);
      });
  }

  /* ==============================
     DASHBOARD CARDS
  ============================== */
  function renderDashboardCards(data) {
    const cycleLen = document.getElementById("cycleLength");
    const prevPeriod = document.getElementById("previousPeriod");
    const nextPeriod = document.getElementById("nextPeriod");
    const regEl = document.getElementById("regularityScore");

    if (cycleLen && data.averageCycleLength != null) {
      cycleLen.innerText = Math.round(data.averageCycleLength);
    }

    if (prevPeriod) {
      prevPeriod.innerText =
        data.previousPeriodLength != null
          ? data.previousPeriodLength
          : "--";
    }

    if (nextPeriod) {
      const n = data.nextPeriodIn;
      nextPeriod.innerText =
        n == null ? "--" : n < 0 ? `${Math.abs(n)} overdue` : n;
    }

    if (regEl && data.regularityScore != null) {
      regEl.innerText = data.regularityScore;
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
