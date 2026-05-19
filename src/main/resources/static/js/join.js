async function join() {
    const name = document.getElementById("name-input").value.trim();
    const loginId = document.getElementById("login-id-input").value.trim();
    const password = document.getElementById("password-input").value.trim();
    const errorMessage = document.getElementById("error-message");

    if (!name || !loginId || !password) {
        errorMessage.textContent = "모든 항목을 입력해주세요.";
        errorMessage.classList.remove("hidden");
        return;
    }

    const response = await fetch("/members", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "same-origin",
        body: JSON.stringify({ name, loginId, password })
    });

    if (!response.ok) {
        const error = await response.json();
        errorMessage.textContent = error.message;
        errorMessage.classList.remove("hidden");
        return;
    }

    location.href = "/login-page";
}

document.addEventListener("keydown", (e) => {
    if (e.key === "Enter") join();
});
