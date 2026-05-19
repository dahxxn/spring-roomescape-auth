async function initAuth() {
    const response = await fetch("/members/me", {
        credentials: "same-origin"
    });

    const isLoggedIn = response.ok;
    document.getElementById("login-link").style.display = isLoggedIn ? "none" : "block";
    document.getElementById("logout-button").style.display = isLoggedIn ? "block" : "none";

    return isLoggedIn;
}

function redirectToLogin() {
    const currentPath = encodeURIComponent(location.pathname);
    location.href = `/login-page?redirect=${currentPath}`;
}

async function handleAuthResponse(response) {
    if (response.status === 401) {
        redirectToLogin();
        return null;
    }
    if (response.status === 403) {
        alert("접근 권한이 없습니다.");
        return null;
    }
    return response;
}

async function logout() {
    await fetch("/logout", {
        method: "POST",
        credentials: "same-origin"
    });
    location.href = "/login-page";
}
