(() => {
    const statusEl = document.getElementById("role-update-status");
    if (!statusEl) {
        return;
    }

    const editorRows = Array.from(document.querySelectorAll(".role-editor-row"));

    const showStatus = (type, message) => {
        statusEl.classList.remove("alert-error", "alert-success");
        statusEl.classList.add(type === "success" ? "alert-success" : "alert-error");
        statusEl.textContent = message;
        statusEl.style.display = "block";
    };

    const hideStatus = () => {
        statusEl.style.display = "none";
        statusEl.textContent = "";
        statusEl.classList.remove("alert-error", "alert-success");
    };

    const closeAllEditors = () => {
        editorRows.forEach((row) => {
            row.hidden = true;
        });
    };

    document.querySelectorAll(".role-edit-trigger").forEach((button) => {
        button.addEventListener("click", () => {
            const userId = button.getAttribute("data-user-id");
            const targetRow = document.getElementById(`role-editor-${userId}`);

            if (!targetRow) {
                return;
            }

            const willOpen = targetRow.hidden;
            closeAllEditors();
            hideStatus();
            targetRow.hidden = !willOpen;
        });
    });

    document.querySelectorAll(".role-editor-cancel").forEach((button) => {
        button.addEventListener("click", () => {
            const row = button.closest(".role-editor-row");
            if (row) {
                row.hidden = true;
            }
            hideStatus();
        });
    });

    document.querySelectorAll(".role-editor-form").forEach((form) => {
        form.addEventListener("submit", async (event) => {
            event.preventDefault();
            hideStatus();

            const userId = form.getAttribute("data-user-id");
            const selectedRoles = Array.from(form.querySelectorAll('input[name="roles"]:checked'))
                .map((input) => input.value);

            if (selectedRoles.length === 0) {
                showStatus("error", "Please select at least one role.");
                return;
            }

            const submitButton = form.querySelector('button[type="submit"]');
            const originalLabel = submitButton ? submitButton.textContent : "Save";
            if (submitButton) {
                submitButton.disabled = true;
                submitButton.textContent = "Saving...";
            }

            try {
                const response = await fetch(`/api/users/${userId}/roles`, {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                        "Accept": "application/json"
                    },
                    body: JSON.stringify({ roles: selectedRoles })
                });

                if (!response.ok) {
                    const data = await response.json().catch(() => ({}));
                    throw new Error(data.message || "Failed to update roles.");
                }

                showStatus("success", "Roles updated successfully. Refreshing...");
                window.setTimeout(() => {
                    window.location.reload();
                }, 600);
            } catch (error) {
                showStatus("error", error.message || "Unable to update roles right now.");
            } finally {
                if (submitButton) {
                    submitButton.disabled = false;
                    submitButton.textContent = originalLabel;
                }
            }
        });
    });
})();
