document.addEventListener("DOMContentLoaded", () => {
    const toggleBtn = document.getElementById("toggle-theme");
    const currentTheme = localStorage.getItem("theme");

    if (currentTheme === "dark") {
        document.body.classList.add("dark-theme");
    }

    toggleBtn.addEventListener("click", () => {
        document.body.classList.toggle("dark-theme");

        // Guarda preferencia
        if (document.body.classList.contains("dark-theme")) {
            localStorage.setItem("theme", "dark");
        } else {
            localStorage.setItem("theme", "light");
        }

        // 🔄 Fuerza repintado visual por si algún componente no se actualiza
        document.querySelectorAll("main, section").forEach(el => {
            el.style.transition = "background-color 0.2s ease";
            el.style.willChange = "background-color";
        });
    });
});
