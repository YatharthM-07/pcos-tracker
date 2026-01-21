document.addEventListener("DOMContentLoaded", () => {
  console.log("Dashboard JS loaded");

  const BASE_URL = "https://pcos-tracker-9a53.onrender.com";

  /* ==============================
     AUTH CHECK
  ============================== */
  const token = localStorage.getItem("token");
  if (!token) {
    window.location.replace("/login.html");
    return;
  }

  /* ==============================
     LOAD USER INFO
  ============================== */
  fetch(`${BASE_URL}/auth/me`, {
    headers: { Authorization: "Bearer " + token }
  })
    .then(res => {
      if (!res.ok) throw new Error("Unauthorized");
      return res.json();
    })
    .then(user => {
      const usernameEl = document.getElementById("username");
      if (usernameEl && user.name) {
        usernameEl.textContent = `Hi, ${user.name} 🌸`;
      }
    })
    .catch(() => logout());

  /* ==============================
     DAILY LOG – LOAD TODAY (DASHBOARD)
  ============================== */
  fetch(`${BASE_URL}/daily-log/today`, {
    headers: { Authorization: "Bearer " + token }
  })
    .then(res => res.json())
    .then(data => {
      if (data.message) return;

      ["cramps","acne","mood","bloating","fatigue","headache"].forEach(id => {
        const slider = document.getElementById(id);
        const val = document.getElementById(`val-${id}`);
        if (slider && data[id] != null) {
          slider.value = data[id];
          if (val) val.innerText = data[id];
        }
      });
    });

  /* ==============================
     DASHBOARD ANALYTICS
  ============================== */
  fetch(`${BASE_URL}/analytics/dashboard`, {
    headers: { Authorization: "Bearer " + token }
  })
    .then(res => res.json())
    .then(data => renderDashboardCards(data));

  /* ==============================
     CALENDAR SETUP (READ ONLY)
  ============================== */
  const calGrid = document.getElementById("calendarGrid");
  const monthLbl = document.getElementById("monthLabel");
  const saveBtn = document.getElementById("savePeriodBtn");
  if (saveBtn) saveBtn.style.display = "none";

  let viewDate = new Date();
  let cachedCycles = [];

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
      } else if (i < firstDay + daysInMonth) {
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
      } else {
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

  function paintCycleHistory() {
    document.querySelectorAll(".cal-cell").forEach(cell =>
      cell.classList.remove("period", "period-faded")
    );

    cachedCycles.forEach(cycle => {
      const start = new Date(cycle.startDate);
      const end = new Date(cycle.endDate);

      document.querySelectorAll(".cal-cell").forEach(cell => {
        const cellDate = new Date(cell.dataset.date);
        if (cellDate >= start && cellDate <= end) {
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
     FETCH CYCLE HISTORY
  ============================== */
  fetch(`${BASE_URL}/cycles/my-cycles`, {
    headers: { Authorization: "Bearer " + token }
  })
    .then(res => res.json())
    .then(cycles => {
      cachedCycles = cycles || [];
      renderCalendar();
      renderCycleTimeline(cycles);
    });

  document.getElementById("prevBtn").onclick = () => {
    viewDate = new Date(viewDate.getFullYear(), viewDate.getMonth() - 1, 1);
    renderCalendar();
  };

  document.getElementById("nextBtn").onclick = () => {
    viewDate = new Date(viewDate.getFullYear(), viewDate.getMonth() + 1, 1);
    renderCalendar();
  };

  function renderCycleTimeline(cycles) {
    const timeline = document.getElementById("cycleHistory");
    if (!timeline) return;

    timeline.querySelectorAll(".cycle-entry").forEach(e => e.remove());

    cycles
      .filter(c => c.endDate && new Date(c.endDate) < new Date())
      .sort((a, b) => new Date(b.endDate) - new Date(a.endDate))
      .slice(0, 3)
      .forEach(c => {
        const days =
          Math.round(
            (new Date(c.endDate) - new Date(c.startDate)) /
              (1000 * 60 * 60 * 24)
          ) + 1;

        const p = document.createElement("p");
        p.className = "cycle-entry";
        p.innerText = `${c.startDate} – ${c.endDate} (${days} days)`;
        timeline.appendChild(p);
      });
  }

  /* ==============================
     DAILY LOG MODAL
  ============================== */
  const dailyLogModal = document.getElementById("dailyLogModal");

  if (dailyLogModal) {
    dailyLogModal.addEventListener("show.bs.modal", () => {
      fetch(`${BASE_URL}/daily-log/today`, {
        headers: { Authorization: "Bearer " + token }
      })
        .then(res => res.json())
        .then(data => {
          if (!data.exists || !data.log) return;

          const log = data.log;
          ["cramps","acne","mood","bloating","fatigue","headache"].forEach(id => {
            const slider = document.getElementById(id);
            const val = document.getElementById(`val-${id}`);
            if (slider && log[id] != null) {
              slider.value = log[id];
              if (val) val.innerText = log[id];
            }
          });
        });
    });
  }

  /* ==============================
     SLIDER LIVE UPDATE
  ============================== */
  ["cramps","acne","mood","bloating","fatigue","headache"].forEach(id => {
    const slider = document.getElementById(id);
    const val = document.getElementById(`val-${id}`);
    if (slider && val) {
      slider.addEventListener("input", () => {
        val.innerText = slider.value;
      });
    }
  });

  /* ==============================
     SAVE DAILY LOG
  ============================== */
  const saveDailyLogBtn = document.getElementById("saveDailyLog");

  if (saveDailyLogBtn) {
    saveDailyLogBtn.addEventListener("click", () => {
      const payload = {
        date: new Date().toISOString().split("T")[0],
        cramps: +cramps.value,
        acne: +acne.value,
        mood: +mood.value,
        bloating: +bloating.value,
        fatigue: +fatigue.value,
        headache: +headache.value
      };

      saveDailyLogBtn.disabled = true;
      saveDailyLogBtn.innerText = "Saving…";

      fetch(`${BASE_URL}/daily-log/add`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer " + token
        },
        body: JSON.stringify(payload)
      })
        .then(res => {
          if (!res.ok) throw new Error();
          return res.json();
        })
        .then(() => {
          // ✅ Success buffer state (ADDED, no deletion)
          saveDailyLogBtn.innerText = "Saved ✓";
          showToast("Daily log saved 🌸");

          fetch(`${BASE_URL}/analytics/dashboard`, {
            headers: { Authorization: "Bearer " + token }
          })
            .then(res => res.json())
            .then(data => renderDashboardCards(data));

          // ⏱ Reset button after short pause
          setTimeout(() => {
            saveDailyLogBtn.disabled = false;
            saveDailyLogBtn.innerText = "Save Log";
          }, 1500);

          // 🚪 Defensive modal close (ADDED)
          setTimeout(() => {
            const instance = bootstrap.Modal.getInstance(dailyLogModal);
            if (instance) instance.hide();
          }, 300);
        })
        .catch(() => {
          saveDailyLogBtn.disabled = false;
          saveDailyLogBtn.innerText = "Save Log";
          showToast("Could not save log 💗");
        });
    });
  }

  function showToast(message) {
    const toast = document.getElementById("toast");
    if (!toast) return;
    toast.innerText = message;
    toast.style.display = "block";
    setTimeout(() => (toast.style.display = "none"), 3000);
  }

  function logout() {
    localStorage.clear();
    alert("You’ve been logged out 👋");
    window.location.replace("/");
  }

  /* ==============================
     DASHBOARD CARDS
  ============================== */
  function renderDashboardCards(data) {
    const cycleLen = document.getElementById("cycleLength");
    const prevPeriod = document.getElementById("previousPeriod");
    const nextPeriod = document.getElementById("nextPeriod");
    const regEl = document.getElementById("regularityScore");
    const insights = document.getElementById("insightsList");

    if (cycleLen)
      cycleLen.innerText =
        data.averageCycleLength != null
          ? `${data.averageCycleLength} days`
          : "--";

    if (prevPeriod)
      prevPeriod.innerText =
        data.previousPeriodLength != null
          ? `${data.previousPeriodLength} days`
          : "--";

    /* ==============================
       NEXT PERIOD (SMART WORDING)
    ============================== */
    if (nextPeriod) {
      if (data.nextPeriodIn != null) {
        const n = data.nextPeriodIn;

        const today = new Date();
        const expectedDate = new Date();
        expectedDate.setDate(today.getDate() + n);

        const options = { day: "numeric", month: "short" };
        const formattedDate = expectedDate.toLocaleDateString("en-US", options);

        if (n > 0) {
          nextPeriod.innerText = `Around ${formattedDate}`;
        } else if (n === 0) {
          nextPeriod.innerText = "Expected today";
        } else {
          nextPeriod.innerText = `Delayed by ~${Math.abs(n)} days`;
        }
      } else {
        nextPeriod.innerText = "--";
      }
    }

    if (regEl)
      regEl.innerText =
        data.regularityScore != null
          ? `${data.regularityScore}%`
          : "--";

    if (insights) {
      insights.innerHTML = "";
      if (data.insights?.length) {
        data.insights.forEach(text => {
          const li = document.createElement("li");
          li.innerText = text;
          insights.appendChild(li);
        });
      } else {
        const li = document.createElement("li");
        li.innerText = "Log more cycles to unlock personalized insights.";
        insights.appendChild(li);
      }
    }
  }
});
