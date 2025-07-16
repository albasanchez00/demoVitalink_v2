function filtrarHistorial() {
    const fechaFiltro = document.getElementById('fecha').value;
    const filas = document.querySelectorAll("#historial-body tr");

    filas.forEach(fila => {
        const fecha = fila.querySelector("td").innerText.trim(); // primera columna
        if (!fechaFiltro || fecha === fechaFiltro) {
            fila.style.display = "";
        } else {
            fila.style.display = "none";
        }
    });
}
function limpiarFiltro() {
    document.getElementById('fecha').value = '';
    filtrarHistorial();
}