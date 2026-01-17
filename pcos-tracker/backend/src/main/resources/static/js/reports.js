document.addEventListener("DOMContentLoaded", () => {

  // ==================================================
  // CONFIG (temporary until JWT is wired)
  // ==================================================
  const USER_ID = 1; // will be replaced by auth/JWT

  // ==================================================
  // STATE
  // ==================================================
  let reports = [];

  // ==================================================
  // ELEMENTS
  // ==================================================
  const uploadBox = document.getElementById("uploadBox");
  const fileInput = document.getElementById("reportFile");
  const reportsList = document.getElementById("reportsList");
  const progressWrapper = document.getElementById("progressWrapper");
  const progressBar = document.getElementById("uploadProgress");

  // ==================================================
  // UI HELPERS
  // ==================================================
  function showToast(message) {
    const toast = document.createElement("div");
    toast.className = "toast-msg";
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
  }

  function renderReports() {
    reportsList.innerHTML = "";

    if (!reports.length) {
      reportsList.innerHTML =
        `<p style="color:#6f6f6f;">No reports uploaded yet.</p>`;
      return;
    }

    reports.forEach(r => {
      const date = r.uploadDate?.split("T")[0] ?? "";

      reportsList.innerHTML += `
        <div class="report-item">
          <div class="report-info">
            <div class="report-icon">
              <i class="fa-solid fa-file-medical"></i>
            </div>
            <div>
              <div class="fw-semibold">${r.fileName}</div>
              <div class="report-date">Uploaded on ${date}</div>
            </div>
          </div>

          <div style="display:flex; gap:10px;">
            <button class="action-btn" onclick="downloadReport(${r.id})">
              <i class="fa-solid fa-download"></i>
            </button>
            <button class="action-btn" onclick="deleteReport(${r.id})">
              <i class="fa-solid fa-trash"></i>
            </button>
          </div>
        </div>
      `;
    });
  }

  // ==================================================
  // BACKEND CALLS
  // ==================================================
  function loadReports() {
    fetch(`/reports/user?userId=${USER_ID}`)
      .then(res => res.json())
      .then(data => {
        reports = data;
        renderReports();
      })
      .catch(() => showToast("❌ Failed to load reports"));
  }

  function uploadFile(file) {
    const allowed = ["application/pdf", "image/jpeg", "image/png"];
    if (!allowed.includes(file.type)) {
      showToast("❌ Only PDF, JPG, PNG allowed");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("userId", USER_ID);

    const xhr = new XMLHttpRequest();
    xhr.open("POST", "/reports/upload");

    progressWrapper.style.display = "block";
    progressBar.style.width = "0%";

    xhr.upload.onprogress = (e) => {
      if (e.lengthComputable) {
        const percent = (e.loaded / e.total) * 100;
        progressBar.style.width = percent + "%";
      }
    };

    xhr.onload = () => {
      progressWrapper.style.display = "none";

      if (xhr.status === 200) {
        showToast("✔ Report uploaded successfully");
        loadReports();
      } else {
        showToast("❌ Upload failed");
      }
    };

    xhr.onerror = () => {
      progressWrapper.style.display = "none";
      showToast("❌ Upload error");
    };

    xhr.send(formData);
  }

  // ==================================================
  // ACTIONS (exposed globally for inline HTML buttons)
  // ==================================================
  window.downloadReport = function (id) {
    window.location.href = `/reports/download/${id}`;
  };

  window.deleteReport = function (id) {
    fetch(`/reports/${id}`, { method: "DELETE" })
      .then(() => {
        showToast("🗑 Report deleted");
        loadReports();
      })
      .catch(() => showToast("❌ Delete failed"));
  };

  // ==================================================
  // UPLOAD BOX INTERACTIONS
  // ==================================================
  uploadBox.addEventListener("click", () => fileInput.click());

  uploadBox.addEventListener("dragover", e => {
    e.preventDefault();
    uploadBox.classList.add("dragover");
  });

  uploadBox.addEventListener("dragleave", () => {
    uploadBox.classList.remove("dragover");
  });

  uploadBox.addEventListener("drop", e => {
    e.preventDefault();
    uploadBox.classList.remove("dragover");

    const file = e.dataTransfer.files[0];
    if (file) uploadFile(file);
  });

  fileInput.addEventListener("change", () => {
    const file = fileInput.files[0];
    if (file) uploadFile(file);
    fileInput.value = "";
  });

  // ==================================================
  // INIT
  // ==================================================
  loadReports();

});
