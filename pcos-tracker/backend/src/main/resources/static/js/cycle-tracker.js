document.addEventListener("DOMContentLoaded", () => {

  /* ==============================
     AUTH CHECK (FIXED LOCATION)
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
      updateSummaryCards();
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
      renderCalendar();
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
        if (d === today.getDate() && m === today.getMonth() && y === today.getFullYear()) {
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
     PAINTING LOGIC
  ============================== */
  function paintPeriods(cells) {
    cells.forEach(c => {
      if (isPeriodDay(c.iso)) c.el.classList.add("period");
    });
  }

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

  function isPeriodDay(iso) {
    return backendPeriods.some(p => {
      const d = new Date(iso);
      return d >= new Date(p.startDate) && d <= new Date(p.endDate);
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


  function renderDashboardCards(data) {

    // Top 3 cards
    const cycleLen = document.getElementById("cycle-length");
    const prevLen = document.getElementById("prev-length");
    const nextIn = document.getElementById("next-in");
    const regScore = document.getElementById("reg-score");

    if (cycleLen && data.averageCycleLength != null) {
      cycleLen.innerText = `${data.averageCycleLength} days`;
    }

    if (prevLen && data.previousPeriodLength != null) {
      prevLen.innerText = `${data.previousPeriodLength} days`;
    }

    if (nextIn) {
      if (data.nextPeriodIn == null) {
        nextIn.innerText = "—";
      } else if (data.nextPeriodIn > 0) {
        nextIn.innerText = `${data.nextPeriodIn} days`;
      } else if (data.nextPeriodIn === 0) {
        nextIn.innerText = "Today";
      } else {
        nextIn.innerText = `Late by ${Math.abs(data.nextPeriodIn)} days`;
      }
    }

    if (regScore && data.regularityScore != null) {
      regScore.innerText = `${data.regularityScore}%`;
    }

    // Insights list
    const insightsEl = document.getElementById("insightsList");
    if (insightsEl) {
      insightsEl.innerHTML = "";
      (data.insights || []).forEach(text => {
        const li = document.createElement("li");
        li.innerText = text;
        insightsEl.appendChild(li);
      });
    }
  }

  async function refreshAnalytics() {
    try {
      const res = await fetch("/analytics/dashboard", {
        headers: {
          "Authorization": "Bearer " + token
        }
      });

      if (!res.ok) throw new Error("Analytics fetch failed");

      const data = await res.json();
      renderDashboardCards(data);

    } catch (err) {
      console.error("Failed to refresh analytics", err);
    }
  }



  /* ==============================
     TIMELINE & SUMMARY
  ============================== */
  function updateTimeline() {
    if (!timelineBox) return;
    timelineBox.innerHTML = "";
    backendCycles.slice(0, parseInt(timelineCount.value)).forEach(c => {
      timelineBox.innerHTML += `
        <div class="timeline-line">
          ${fmt(c.startDate)} → ${fmt(c.endDate)} — ${c.duration} days
        </div>`;
    });
  }

  function updateSummaryCards() {
    if (!backendCycles.length) return;
    document.getElementById("cycle-length").textContent = backendCycles[0].duration + " days";
    if (backendCycles[1]) {
      document.getElementById("prev-length").textContent = backendCycles[1].duration + " days";
    }
   document.getElementById("next-in").textContent =
     calculateNextPeriodIn();

    document.getElementById("reg-score").textContent = "84%";
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
    return new Date(date).toLocaleString("default", { month: "short", day: "numeric" });
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

});
