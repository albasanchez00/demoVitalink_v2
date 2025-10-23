document.addEventListener("DOMContentLoaded", () => {
    const toggleBtn = document.getElementById("toggle-theme");
    const currentTheme = localStorage.getItem("theme");

    if (currentTheme === "dark") document.body.classList.add("dark-theme");
    if (!toggleBtn) return; // <-- guarda

    toggleBtn.addEventListener("click", () => {
        document.body.classList.toggle("dark-theme");
        localStorage.setItem("theme", document.body.classList.contains("dark-theme") ? "dark" : "light");
        document.querySelectorAll("main, section").forEach(el => {
            el.style.transition = "background-color 0.2s ease";
            el.style.willChange = "background-color";
        });
    });
});
