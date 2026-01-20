document.addEventListener("DOMContentLoaded", () => {

  /* ==============================
     AUTH CHECK
  ============================== */
  const token = localStorage.getItem("token");
  if (!token) {
    window.location.replace("/login");
    return;
  }

  /* ==============================
     STATE & HELPERS
  ============================== */
  const pad = n => String(n).padStart(2, "0");

  let viewDate = new Date();
  let startSel = null;
  let endSel = null;

  let backendCycles = [];
  let backendPeriods = [];

  const grid = document.getElementById("calendarGrid");
  const monthLabel = document.getElementById("monthLabel");
  const prevBtn = document.getElementById("prevMonth");
  const nextBtn = document.getElementById("nextMonth");
  const timelineBox = document.getElementById("timelineBox");
  const timelineCount = document.getElementById("timelineCount");

  /* ==============================
     BACKEND LOADERS
  ============================== */
  async function loadCyclesFromBackend() {
    try {
      const res = await fetch("/cycles/my-cycles", {
        headers: { Authorization: "Bearer " + token }
      });
      backendCycles = await res.json();
      renderCalendar();
      updateTimeline();
    } catch (err) {
      console.error("Failed to load cycles", err);
    }
  }

  async function loadPeriodsFromBackend() {
    try {
      const res = await fetch("/cycles/my-periods", {
        headers: { Authorization: "Bearer " + token }
      });
      backendPeriods = await res.json();
      requestAnimationFrame(renderCalendar);
    } catch (err) {
      console.error("Failed to load periods", err);
    }
  }

  /* ==============================
     CALENDAR RENDER
  ============================== */
  function renderCalendar() {
    const y = viewDate.getFullYear();
    const m = viewDate.getMonth();

    monthLabel.textContent = viewDate.toLocaleString("default", {
      month: "long",
      year: "numeric"
    });

    grid.innerHTML = "";

    const firstDay = new Date(y, m, 1).getDay();
    const daysInMonth = new Date(y, m + 1, 0).getDate();
    const prevMonthDays = new Date(y, m, 0).getDate();

    let cells = [];

    for (let i = 0; i < 42; i++) {
      const cell = document.createElement("div");
      cell.className = "cal-cell";

      let dateObj;

      if (i < firstDay) {
        const d = prevMonthDays - (firstDay - 1 - i);
        cell.textContent = d;
        cell.classList.add("disabled");
        dateObj = new Date(y, m - 1, d);
      } else if (i < firstDay + daysInMonth) {
        const d = i - firstDay + 1;
        cell.textContent = d;
        dateObj = new Date(y, m, d);

        const today = new Date();
        if (
          d === today.getDate() &&
          m === today.getMonth() &&
          y === today.getFullYear()
        ) {
          cell.classList.add("today");
        }

        cell.onclick = () => handleClick(formatISO(dateObj));
      } else {
        const d = i - (firstDay + daysInMonth) + 1;
        cell.textContent = d;
        cell.classList.add("disabled");
        dateObj = new Date(y, m + 1, d);
      }

      const iso = formatISO(dateObj);
      cell.dataset.date = iso;

      cells.push({ iso, el: cell });
      grid.appendChild(cell);
    }

    paintPeriods(cells);
    paintCycleRange(cells);
  }

  /* ==============================
     DATE CLICK HANDLING
  ============================== */
  function handleClick(date) {
    if (!startSel) {
      startSel = date;
      endSel = null;
    } else if (!endSel) {
      endSel = date;
    } else {
      startSel = date;
      endSel = null;
    }
    renderCalendar();
  }

  /* ==============================
     PERIOD HIGHLIGHT (CURRENT + PREVIOUS)
  ============================== */
  function paintPeriods(cells) {
    if (!backendPeriods || backendPeriods.length === 0) return;

    // backend already sends DESC → trust it
    const current = backendPeriods[0];
    const previous = backendPeriods.length > 1 ? backendPeriods[1] : null;

    cells.forEach(c => {
      const d = new Date(c.iso);

      // ✅ Latest period (always paint)
      if (
        current &&
        d >= new Date(current.startDate) &&
        d <= new Date(current.endDate)
      ) {
        c.el.classList.add("period-current");
      }

      // ✅ Previous period (light shade)
      if (
        previous &&
        d >= new Date(previous.startDate) &&
        d <= new Date(previous.endDate)
      ) {
        c.el.classList.add("period-previous");
      }
    });
  }

  /* ==============================
     RANGE SELECTION
  ============================== */
  function paintCycleRange(cells) {
    if (!startSel) return;

    const s = new Date(startSel);
    const startCell = cells.find(c => c.iso === startSel);
    if (startCell) startCell.el.classList.add("start");

    if (!endSel) return;

    const e = new Date(endSel);
    const endCell = cells.find(c => c.iso === endSel);
    if (endCell) endCell.el.classList.add("end");

    const [min, max] = s < e ? [s, e] : [e, s];
    cells.forEach(c => {
      const cur = new Date(c.iso);
      if (cur > min && cur < max) c.el.classList.add("inrange");
    });
  }

  /* ==============================
     SAVE PERIOD
  ============================== */
  const savePeriodBtn = document.getElementById("savePeriodBtn");

  if (savePeriodBtn) {
    savePeriodBtn.onclick = async () => {
      if (!startSel || !endSel) return alert("Select start and end date");

      savePeriodBtn.disabled = true;
      savePeriodBtn.innerText = "Saving...";

      try {
        await fetch("/cycles", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer " + token
          },
          body: JSON.stringify({ startDate: startSel, endDate: endSel })
        });

        await fetch("/cycles/period", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer " + token
          },
          body: JSON.stringify({ startDate: startSel, endDate: endSel })
        });

        startSel = null;
        endSel = null;

        await loadCyclesFromBackend();
        await loadPeriodsFromBackend();
        await refreshAnalytics();

        savePeriodBtn.innerText = "Saved ✓";
        showToast("Period saved successfully 🌸");

        setTimeout(() => {
          savePeriodBtn.innerText = "Save Period";
          savePeriodBtn.disabled = false;
        }, 1200);
      } catch {
        savePeriodBtn.disabled = false;
        savePeriodBtn.innerText = "Save Period";
        alert("Failed to save period");
      }
    };
  }

  /* ==============================
     DASHBOARD CARDS & INSIGHTS
  ============================== */
  function renderDashboardCards(data) {

    const cycleLen = document.getElementById("cycle-length");
    const prevLen = document.getElementById("prev-length");
    const nextIn = document.getElementById("next-in");
    const regScore = document.getElementById("reg-score");

    cycleLen.innerText =
      data.averageCycleLength != null ? `${data.averageCycleLength} days` : "—";

    prevLen.innerText =
      data.previousPeriodLength != null
        ? `${data.previousPeriodLength} days`
        : "First cycle";

    if (data.nextPeriodIn == null) {
      nextIn.innerText = "Prediction pending";
    } else if (data.nextPeriodIn > 0) {
      nextIn.innerText = `${data.nextPeriodIn} days`;
    } else if (data.nextPeriodIn === 0) {
      nextIn.innerText = "Expected today";
    } else {
      nextIn.innerText = `Late by ${Math.abs(data.nextPeriodIn)} days`;
    }

    regScore.innerText =
      data.regularityScore != null ? `${data.regularityScore}%` : "—";

    const insights = document.getElementById("insightsList");
    insights.innerHTML = "";

    if (!data.averageCycleLength) {
      insights.innerHTML =
        "<li>Log your first cycle to start seeing insights.</li>";
      return;
    }

    insights.innerHTML += `<li>Your average cycle length is ${data.averageCycleLength} days.</li>`;

    if (data.regularityScore >= 80) {
      insights.innerHTML += "<li>Your cycle pattern appears regular.</li>";
    } else if (data.regularityScore >= 50) {
      insights.innerHTML += "<li>Your cycle shows mild variation.</li>";
    } else {
      insights.innerHTML += "<li>Your cycle is irregular.</li>";
    }

    if (data.previousPeriodLength != null) {
      insights.innerHTML += `<li>Your last period lasted ${data.previousPeriodLength} days.</li>`;
    }

    insights.innerHTML += "<li>Continue logging to improve predictions.</li>";
  }

  async function refreshAnalytics() {
    const res = await fetch("/analytics/dashboard", {
      headers: { Authorization: "Bearer " + token }
    });
    const data = await res.json();
    renderDashboardCards(data);
  }

  /* ==============================
     TIMELINE
  ============================== */
  function updateTimeline() {
    if (!timelineBox) return;
    timelineBox.innerHTML = "";
    backendCycles
      .slice(0, parseInt(timelineCount.value))
      .forEach(c => {
        timelineBox.innerHTML += `
          <div class="timeline-line">
            ${fmt(c.startDate)} → ${fmt(c.endDate)} — ${c.duration} days
          </div>`;
      });
  }

  /* ==============================
     NAVIGATION
  ============================== */
  prevBtn.onclick = () => {
    viewDate = new Date(viewDate.getFullYear(), viewDate.getMonth() - 1, 1);
    renderCalendar();
  };
  nextBtn.onclick = () => {
    viewDate = new Date(viewDate.getFullYear(), viewDate.getMonth() + 1, 1);
    renderCalendar();
  };
  timelineCount?.addEventListener("change", updateTimeline);

  /* ==============================
     UTILITIES
  ============================== */
  function formatISO(d) {
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
  }
  function fmt(date) {
    return new Date(date).toLocaleString("default", {
      month: "short",
      day: "numeric"
    });
  }
  function showToast(msg) {
    const t = document.getElementById("toast");
    if (!t) return;
    t.innerText = msg;
    t.style.display = "block";
    setTimeout(() => (t.style.display = "none"), 2500);
  }

  /* ==============================
     INIT
  ============================== */
  renderCalendar();
  loadCyclesFromBackend();
  loadPeriodsFromBackend();
  refreshAnalytics();

});
