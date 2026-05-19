async function login() {
    const loginId = document.getElementById("login-id-input").value.trim();
    const password = document.getElementById("password-input").value.trim();
    const errorMessage = document.getElementById("error-message");

    if (!loginId || !password) {
        errorMessage.textContent = "아이디와 비밀번호를 입력해주세요.";
        errorMessage.classList.remove("hidden");
        return;
    }

    const response = await fetch("/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "same-origin",
        body: JSON.stringify({ loginId, password })
    });

    if (!response.ok) {
        const error = await response.json();
        errorMessage.textContent = error.message;
        errorMessage.classList.remove("hidden");
        return;
    }

    const meResponse = await fetch("/members/me", {
        credentials: "same-origin"
    });
    const me = await meResponse.json();

    if (me.role === "ADMIN") {
        location.href = "/admin-page";
        return;
    }

    const redirectTo = new URLSearchParams(window.location.search).get("redirect") || "/";
    location.href = redirectTo.startsWith("/") ? redirectTo : "/";
}

document.addEventListener("keydown", (e) => {
    if (e.key === "Enter") login();
});
