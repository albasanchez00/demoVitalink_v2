let registros = [];

document.getElementById("form-sintomas").addEventListener("submit", function(e) {
    e.preventDefault();

    const registro = {
    userId: document.getElementById("userId").value,
    fecha: document.getElementById("fecha").value,
    sintoma: document.getElementById("sintoma").value,
    intensidad: parseInt(document.getElementById("intensidad").value),
    notas: document.getElementById("notas").value
    };

    registros.push(registro);
    mostrarRegistros();
    actualizarGrafico();
    e.target.reset();
});

function mostrarRegistros(filtrados = null) {
    const data = filtrados || registros;
    const tbody = document.getElementById("tablaRegistros");
    tbody.innerHTML = "";
    data.forEach(r => {
    const fila = `<tr><td>${r.fecha}</td><td>${r.sintoma}</td><td>${r.intensidad}</td><td>${r.notas}</td></tr>`;
    tbody.innerHTML += fila;
    });
}

function filtrarRegistros() {
    const fecha = document.getElementById("filtroFecha").value;
    const sintoma = document.getElementById("filtroSintoma").value.toLowerCase();

    const filtrados = registros.filter(r => {
    return (!fecha || r.fecha === fecha) && (!sintoma || r.sintoma.toLowerCase().includes(sintoma));
    });

    mostrarRegistros(filtrados);
}

let grafico;
function actualizarGrafico() {
    const porFecha = {};

    registros.forEach(r => {
    if (!porFecha[r.fecha]) porFecha[r.fecha] = 0;
    porFecha[r.fecha] += r.intensidad;
    });

    const fechas = Object.keys(porFecha);
    const intensidades = Object.values(porFecha);

    if (grafico) grafico.destroy();

    grafico = new Chart(document.getElementById("graficoSintomas"), {
    type: 'line',
    data: {
        labels: fechas,
        datasets: [{
        label: 'Intensidad Total por Día',
        data: intensidades,
        fill: false,
        tension: 0.2,
        borderWidth: 2
        }]
    }
    });
}