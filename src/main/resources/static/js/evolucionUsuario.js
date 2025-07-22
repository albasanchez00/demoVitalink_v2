// evolucionUsuario.js
document.addEventListener('DOMContentLoaded', () => {
    // 1) Evolución de síntomas (línea)
    const lineCtx = document.getElementById('lineChart');
    if (lineCtx) {
        new Chart(lineCtx, {
            type: 'line',
            data: {
                labels: ['Semana 1', 'Semana 2', 'Semana 3', 'Semana 4'],
                datasets: [{
                    label: 'Síntomas registrados',
                    data: [10, 8, 5, 3],
                    borderColor: '#008d95',
                    backgroundColor: 'rgba(0, 141, 149, 0.1)',
                    tension: 0.3,
                    fill: true
                }]
            },
            options: { responsive: true }
        });
    }

    // 2) Síntomas comunes (barras)
    const barCtx = document.getElementById('barChart');
    if (barCtx) {
        new Chart(barCtx, {
            type: 'bar',
            data: {
                labels: ['Dolor', 'Fiebre', 'Fatiga', 'Náuseas'],
                datasets: [{
                    label: 'Frecuencia',
                    data: [6, 3, 8, 4],
                    backgroundColor: ['#006380', '#008d95', '#00acc1', '#4dd0e1']
                }]
            },
            options: { responsive: true }
        });
    }

    // 3) Adherencia al tratamiento (pastel)
    const pieCtx = document.getElementById('pieChart');
    if (pieCtx) {
        new Chart(pieCtx, {
            type: 'pie',
            data: {
                labels: ['Dosis Tomadas', 'Dosis Olvidadas'],
                datasets: [{
                    data: [85, 15],
                    backgroundColor: ['#66bb6a', '#ef5350']
                }]
            },
            options: { responsive: true }
        });
    }
});
