// dashboard.js

document.addEventListener("DOMContentLoaded", () => {
  console.log("Dashboard JS loaded");

  /* ==============================
     AUTH CHECK
  =============================== */
  const token = localStorage.getItem("token");
  if (!token) {
    window.location.replace("/login");
    return;
  }

  /* ==============================
     LOAD USER INFO (Fixes "Hi, User")
  =============================== */
  fetch("/auth/me", {
    headers: {
      "Authorization": "Bearer " + token
    }
  })
    .then(res => {
      if (!res.ok) throw new Error("Auth failed");
      return res.json();
    })
    .then(user => {
      const usernameEl = document.getElementById("username");
      if (usernameEl && user.name) {
        usernameEl.textContent = `Hi, ${user.name} 🌸`;
      }
    })
    .catch(err => console.error("User load error:", err));

  /* ==============================
     CALENDAR SETUP
  =============================== */
  const calGrid = document.getElementById("calendarGrid");
  const monthLbl = document.getElementById("monthLabel");
  const savePeriodBtn = document.getElementById("savePeriodBtn");

  let view = new Date();
  let periodStart = null;
  let periodEnd = null;

  savePeriodBtn.disabled = true;

  const pad = n => String(n).padStart(2, "0");
  const todayKey = new Date().toISOString().split("T")[0];

  function renderCalendar() {
    const y = view.getFullYear();
    const m = view.getMonth();

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

      let dateKey = null;

      if (i < firstDay) {
        cell.textContent = prevMonthDays - (firstDay - 1 - i);
        cell.classList.add("disabled");
      } else if (i < firstDay + daysInMonth) {
        const day = i - firstDay + 1;
        cell.textContent = day;
        dateKey = `${y}-${pad(m + 1)}-${pad(day)}`;

        if (dateKey === todayKey) {
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
    clearSelection();
    loadSavedPeriods();
  };

  document.getElementById("nextBtn").onclick = () => {
    view = new Date(view.getFullYear(), view.getMonth() + 1, 1);
    renderCalendar();
    clearSelection();
    loadSavedPeriods();
  };

  /* ==============================
     CALENDAR CLICK LOGIC
  =============================== */
  calGrid.addEventListener("click", e => {
    const cell = e.target;
    if (!cell.classList.contains("cal-cell")) return;
    if (cell.classList.contains("disabled")) return;

    const day = Number(cell.textContent);
    const clickedDate = new Date(
      view.getFullYear(),
      view.getMonth(),
      day
    );

    if (!periodStart) {
      clearSelection();
      periodStart = clickedDate;
      cell.classList.add("period");
      return;
    }

    if (periodStart && !periodEnd) {
      if (clickedDate < periodStart) {
        clearSelection();
        periodStart = clickedDate;
        cell.classList.add("period");
        return;
      }

      periodEnd = clickedDate;
      highlightRange(periodStart, periodEnd);
      savePeriodBtn.disabled = false;
      return;
    }

    clearSelection();
    periodStart = clickedDate;
    cell.classList.add("period");
  });

  function clearSelection() {
    document.querySelectorAll(".cal-cell.period")
      .forEach(c => c.classList.remove("period"));
    periodStart = null;
    periodEnd = null;
    savePeriodBtn.disabled = true;
  }

  function highlightRange(start, end) {
    document.querySelectorAll(".cal-cell").forEach(cell => {
      if (cell.classList.contains("disabled")) return;

      const day = Number(cell.textContent);
      const date = new Date(
        view.getFullYear(),
        view.getMonth(),
        day
      );

      if (date >= start && date <= end) {
        cell.classList.add("period");
      }
    });
  }

  /* ==============================
     LOAD SAVED PERIODS
  =============================== */
  function loadSavedPeriods() {
    fetch("/cycles/my-periods", {
      headers: {
        "Authorization": "Bearer " + token
      }
    })
      .then(res => res.json())
      .then(cycles => paintSavedPeriods(cycles))
      .catch(err => console.error("Failed to load periods", err));
  }

  function paintSavedPeriods(cycles) {
    cycles.forEach(cycle => {
      const start = new Date(cycle.startDate);
      const end = new Date(cycle.endDate);

      document.querySelectorAll(".cal-cell").forEach(cell => {
        if (cell.classList.contains("disabled")) return;

        const day = Number(cell.textContent);
        const cellDate = new Date(
          view.getFullYear(),
          view.getMonth(),
          day
        );

        if (cellDate >= start && cellDate <= end) {
          cell.classList.add("period");
        }
      });
    });
  }

  loadSavedPeriods();

  /* ==============================
     SAVE PERIOD (UI ONLY)
  =============================== */
  savePeriodBtn.addEventListener("click", () => {
    fetch("/cycles", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + token
      },
      body: JSON.stringify({
        startDate: formatLocalDate(periodStart),
        endDate: formatLocalDate(periodEnd)
      })
    })
    .then(res => {
      if (!res.ok) throw new Error("Save failed");
      return res.json();
    })
    .then(() => {
      showToast("Period saved successfully 🌸");
      savePeriodBtn.disabled = true;
    })
    .catch(err => {
      showToast("Failed to save period 😕", true);
      console.error(err);
    });
  });



  function showToast(message, isError = false) {
    const toast = document.getElementById("toast");
    toast.textContent = message;
    toast.style.background = isError ? "#e74c3c" : "#2ecc71";
    toast.style.display = "block";

    setTimeout(() => {
      toast.style.display = "none";
    }, 2000);
  }
function formatLocalDate(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}


});
