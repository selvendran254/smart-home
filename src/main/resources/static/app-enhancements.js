/**
 * Smart Home — shared UI: toast, i18n, notifications, status, skeletons.
 */
(function (global) {
    "use strict";

    var PAGE_ACCENTS = {
        "Rooms": "#f97316",
        "Sensor": "#10b981",
        "Automation": "#a855f7",
        "Energy": "#f97316",
        "History": "#ec4899",
        "Door Unlock": "#2563eb",
        "Voice": "#06b6d4",
        "Plant Health": "#22c55e",
        "Profile": "#6366f1"
    };

    var I18N = {
        en: {
            "menu.rooms": "Rooms",
            "menu.sensor": "Sensor",
            "menu.automation": "Automation",
            "menu.energy": "Energy",
            "menu.history": "History",
            "menu.door": "Door Unlock",
            "menu.voice": "Voice",
            "menu.plant": "Plant Health",
            "menu.profile": "Profile",
            "menu.logout": "Logout",
            "menu.theme": "Light Mode",
            "menu.themeDark": "Dark Mode",
            "notif.empty": "No alerts yet",
            "weather.loading": "Loading weather…"
        },
        ta: {
            "menu.rooms": "அறைகள்",
            "menu.sensor": "சensor",
            "menu.automation": "தானியங்கி",
            "menu.energy": "மின்சாரம்",
            "menu.history": "வரலாறு",
            "menu.door": "கதவு திற",
            "menu.voice": "குரல்",
            "menu.plant": "தாவரம்",
            "menu.profile": "சுயவிவரம்",
            "menu.logout": "வெளியேறு",
            "menu.theme": "வெளிச்ச Mode",
            "menu.themeDark": "இருள் Mode",
            "notif.empty": "அறிவிப்புகள் இல்லை",
            "weather.loading": "வானிலை ஏற்றுகிறது…"
        }
    };

    function t(key) {
        var lang = localStorage.getItem("smarthome_lang") || "en";
        var pack = I18N[lang] || I18N.en;
        return pack[key] || I18N.en[key] || key;
    }

    function setLanguage(lang) {
        if (lang !== "en" && lang !== "ta") return;
        localStorage.setItem("smarthome_lang", lang);
        document.documentElement.lang = lang;
        applyLanguage();
        document.querySelectorAll(".lang-btn").forEach(function (btn) {
            btn.classList.toggle("active", btn.getAttribute("data-lang") === lang);
        });
    }

    function applyLanguage() {
        document.querySelectorAll("[data-i18n]").forEach(function (el) {
            el.textContent = t(el.getAttribute("data-i18n"));
        });
        updateThemeToggleI18n();
    }

    function updateThemeToggleI18n() {
        var btn = document.getElementById("themeToggleBtn");
        if (!btn) return;
        btn.textContent = document.body.classList.contains("light-mode") ? t("menu.themeDark") : t("menu.theme");
    }

    function showToast(message, type) {
        type = type || "info";
        var container = document.getElementById("toast-container");
        if (!container) {
            container = document.createElement("div");
            container.id = "toast-container";
            container.className = "toast-container";
            document.body.appendChild(container);
        }
        var toast = document.createElement("div");
        toast.className = "app-toast toast-" + type;
        toast.textContent = message;
        container.appendChild(toast);
        requestAnimationFrame(function () { toast.classList.add("show"); });
        setTimeout(function () {
            toast.classList.remove("show");
            setTimeout(function () { toast.remove(); }, 300);
        }, 3200);
    }

    function getNotifications() {
        try { return JSON.parse(localStorage.getItem("smarthome_notifications") || "[]"); }
        catch (e) { return []; }
    }

    function pushNotification(title, message, type) {
        var list = getNotifications();
        list.unshift({ id: Date.now(), title: title, message: message, type: type || "info", time: new Date().toLocaleString() });
        if (list.length > 30) list = list.slice(0, 30);
        localStorage.setItem("smarthome_notifications", JSON.stringify(list));
        renderNotificationPanel();
    }

    function renderNotificationPanel() {
        var panel = document.getElementById("notifPanel");
        var countEl = document.getElementById("notifCount");
        if (!panel) return;
        var list = getNotifications();
        if (countEl) countEl.textContent = list.length > 9 ? "9+" : String(list.length);
        panel.innerHTML = !list.length ? '<p class="notif-empty">' + t("notif.empty") + "</p>" :
            list.slice(0, 8).map(function (n) {
                return '<div class="notif-item notif-' + n.type + '"><strong>' + esc(n.title) + "</strong><span>" + esc(n.message) + '</span><small>' + esc(n.time) + "</small></div>";
            }).join("");
    }

    function toggleNotifPanel() {
        var panel = document.getElementById("notifPanel");
        if (panel) panel.classList.toggle("open");
    }

    function esc(s) {
        return String(s).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }

    function setActiveMenu(section) {
        document.querySelectorAll(".menu-item[data-section]").forEach(function (el) {
            el.classList.toggle("active", el.getAttribute("data-section") === section);
        });
    }

    function setPageAccent(section) {
        document.documentElement.style.setProperty("--page-accent", PAGE_ACCENTS[section] || "#6366f1");
        document.body.setAttribute("data-active-page", section || "");
    }

    function closeMobileSidebar() { document.body.classList.remove("sidebar-open"); }

    function skeletonHtml(type) {
        if (type === "rooms") {
            return '<div class="skeleton-grid">' + [1, 2, 3, 4, 5, 6].map(function () {
                return '<div class="skeleton-card"><div class="sk-line sk-w60"></div><div class="sk-line sk-w40"></div></div>';
            }).join("") + "</div>";
        }
        if (type === "sensor") {
            return '<div class="skeleton-grid">' + [1, 2, 3, 4].map(function () {
                return '<div class="skeleton-card tall"><div class="sk-line sk-w50"></div><div class="sk-line sk-w30"></div></div>';
            }).join("") + "</div>";
        }
        return '<div class="skeleton-page"><div class="sk-line sk-w70"></div><div class="sk-line sk-w50"></div></div>';
    }

    function showSkeleton(container, type) {
        if (container) container.innerHTML = skeletonHtml(type);
    }

    function updateConnectionBadges() {
        fetch("/api/system/status").then(function (r) { return r.json(); }).then(function (data) {
            badge("status-ollama", data.ollama);
            badge("status-esp", data.esp);
        }).catch(function () {
            badge("status-ollama", { online: false });
            badge("status-esp", { online: false });
        });
    }

    function badge(id, info) {
        var el = document.getElementById(id);
        if (!el || !info) return;
        el.classList.toggle("online", !!info.online);
        el.classList.toggle("offline", !info.online);
    }

    function loadWeatherWidget() {
        var el = document.getElementById("weatherWidget");
        if (!el) return;
        fetch("/api/weather").then(function (r) { return r.json(); }).then(function (w) {
            el.innerHTML = '<span class="weather-icon">' + (w.icon || "🌤️") + '</span><div class="weather-info"><strong>' + w.temp + '°C</strong><span>' + esc(w.description || "") + '</span><small>' + esc(w.city || "") + '</small></div>';
        }).catch(function () { el.innerHTML = "🌡️ —"; });
    }

    function initShellEnhancements() {
        setLanguage(localStorage.getItem("smarthome_lang") || "en");
        renderNotificationPanel();
        updateConnectionBadges();
        loadWeatherWidget();
        setInterval(updateConnectionBadges, 45000);
        var btn = document.getElementById("mobileMenuBtn");
        if (btn) btn.onclick = function () { document.body.classList.toggle("sidebar-open"); };
        var ov = document.getElementById("sidebarOverlay");
        if (ov) ov.onclick = closeMobileSidebar;
    }

    global.SmartHomeUI = {
        t: t, setLanguage: setLanguage, applyLanguage: applyLanguage, updateThemeToggleI18n: updateThemeToggleI18n,
        showToast: showToast, pushNotification: pushNotification, toggleNotifPanel: toggleNotifPanel,
        setActiveMenu: setActiveMenu, setPageAccent: setPageAccent, closeMobileSidebar: closeMobileSidebar,
        showSkeleton: showSkeleton, skeletonHtml: skeletonHtml, initShellEnhancements: initShellEnhancements
    };
})(window);
