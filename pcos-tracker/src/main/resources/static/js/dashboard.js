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
  .catch(() => {
    logout(); // force clean logout
  });


    /* ==============================
       DAILY LOG – LOAD TODAY
    ============================== */
    fetch("/daily-log/today", {
      headers: { Authorization: "Bearer " + token }
    })
      .then(res => res.json())
      .then(data => {
        if (data.message) return; // no log for today

        const map = {
          cramps: "cramps",
          acne: "acne",
          mood: "mood",
          bloating: "bloating",
          fatigue: "fatigue",
          headache: "headache"
        };

        Object.keys(map).forEach(key => {
          const slider = document.getElementById(map[key]);
          const valueSpan = document.getElementById(`val-${map[key]}`);

          if (slider && data[key] != null) {
            slider.value = data[key];
            if (valueSpan) valueSpan.innerText = data[key];
          }
        });
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

      const yyyy = dateObj.getFullYear();
      const mm = String(dateObj.getMonth() + 1).padStart(2, "0");
      const dd = String(dateObj.getDate()).padStart(2, "0");
      cell.dataset.date = `${yyyy}-${mm}-${dd}`;

      calGrid.appendChild(cell);
    }

    paintCycleHistory();
  }


  /* ==============================
     PAINT PERIOD HISTORY (ONLY CLASSES)
  ============================== */
  function paintCycleHistory() {
    const cells = document.querySelectorAll(".cal-cell");

    // Clear old highlights
    cells.forEach(cell =>
      cell.classList.remove("period", "period-faded")
    );

    cachedCycles.forEach(cycle => {
      const start = new Date(cycle.startDate);
      const end = new Date(cycle.endDate);

      // Ignore cycles that don't touch current month
      if (
        end < new Date(viewDate.getFullYear(), viewDate.getMonth(), 1) ||
        start > new Date(viewDate.getFullYear(), viewDate.getMonth() + 1, 0)
      ) {
        return;
      }

      cells.forEach(cell => {
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
        p.innerText = `${c.startDate} – ${c.endDate} (${Math.round(days)})`;
        timeline.appendChild(p);
      });
  }

   ["cramps","acne","mood","bloating","fatigue","headache"].forEach(id => {
      const slider = document.getElementById(id);
      const valueSpan = document.getElementById(`val-${id}`);
      if (!slider || !valueSpan) return;

      slider.addEventListener("input", () => {
        valueSpan.innerText = slider.value;
      });
    });

    const saveDailyLogBtn = document.getElementById("saveDailyLog");

    /* ==============================
       DAILY LOG – LOAD TODAY
    ============================== */

    const dailyLogModal = document.getElementById("dailyLogModal");

    if (dailyLogModal) {
      dailyLogModal.addEventListener("show.bs.modal", () => {
        fetch("/daily-log/today", {
          headers: { Authorization: "Bearer " + token }
        })
          .then(res => res.json())
          .then(data => {
            if (!data.exists) return;

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


    if (saveDailyLogBtn) {
      saveDailyLogBtn.addEventListener("click", () => {

        const payload = {
          date: new Date().toISOString().split("T")[0],
          cramps: +document.getElementById("cramps").value,
          acne: +document.getElementById("acne").value,
          mood: +document.getElementById("mood").value,
          bloating: +document.getElementById("bloating").value,
          fatigue: +document.getElementById("fatigue").value,
          headache: +document.getElementById("headache").value
        };

        // 🔒 Buffer state: Saving…
        saveDailyLogBtn.disabled = true;
        saveDailyLogBtn.innerText = "Saving…";

        fetch("/daily-log/add", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer " + token
          },
          body: JSON.stringify(payload)
        })
          .then(res => {
            if (!res.ok) throw new Error("Save failed");
            return res.json();
          })
          .then(() => {
            // ✅ Success state
            saveDailyLogBtn.innerText = "Saved ✓";
            showToast("Daily log saved 🌸");

            // 🔁 Refresh dashboard analytics immediately
            fetch("/analytics/dashboard", {
              headers: { Authorization: "Bearer " + token }
            })
              .then(res => res.json())
              .then(data => renderDashboardCards(data));

            // ⏱ Reset button after short pause
            setTimeout(() => {
              saveDailyLogBtn.disabled = false;
              saveDailyLogBtn.innerText = "Save Log";
            }, 1500);

            // Close modal slightly after save feedback
            setTimeout(() => {
              bootstrap.Modal.getInstance(
                document.getElementById("dailyLogModal")
              ).hide();
            }, 300);
          })
          .catch(() => {
            // ❌ Error state
            saveDailyLogBtn.disabled = false;
            saveDailyLogBtn.innerText = "Save Log";
            showToast("Could not save log. Please try again 💗");
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
      // 🔐 clear auth
      localStorage.removeItem("token");
      localStorage.removeItem("user"); // optional

      // ✨ optional toast / alert
      alert("You’ve been logged out 👋");

      // 🏠 redirect to landing page
      window.location.replace("/");
      // OR if your index is explicit:
      // window.location.replace("/index");
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

    /* ==============================
       PERIOD CYCLE LENGTH
    ============================== */
    if (cycleLen) {
      if (data.averageCycleLength != null) {
        cycleLen.innerText = `${data.averageCycleLength} days`;

        if (data.averageCycleLength > 35) {
          cycleLen.innerText += " — Irregular";
        } else if (data.averageCycleLength < 21) {
          cycleLen.innerText += " — Short";
        }
      } else {
        cycleLen.innerText = "--";
      }
    }

    /* ==============================
       PREVIOUS PERIOD
    ============================== */
    if (prevPeriod) {
      prevPeriod.innerText =
        data.previousPeriodLength != null
          ? `${data.previousPeriodLength} days`
          : "--";
    }

    /* ==============================
       NEXT PERIOD (FIXED & SMART)
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

    /* ==============================
       REGULARITY SCORE
    ============================== */
    if (regEl) {
      if (data.regularityScore != null) {
        regEl.innerText = `${data.regularityScore}%`;
      } else {
        regEl.innerText = "--";
      }
    }

    /* ==============================
       INSIGHTS (DYNAMIC, AFTER SAVE)
    ============================== */
    if (insights) {
      insights.innerHTML = "";

      if (Array.isArray(data.insights) && data.insights.length > 0) {
        data.insights.forEach(text => {
          const li = document.createElement("li");
          li.innerText = text;
          insights.appendChild(li);
        });
      } else {
        // sensible fallback
        const li = document.createElement("li");
        li.innerText = "Log more cycles to unlock personalized insights.";
        insights.appendChild(li);
      }
    }
  }

});