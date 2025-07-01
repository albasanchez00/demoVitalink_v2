// Gráfico de línea - Evolución de síntomas
const lineChart = new Chart(document.getElementById('lineChart'), {
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
    options: {
      responsive: true
    }
});

// Gráfico de barras - Síntomas comunes
const barChart = new Chart(document.getElementById('barChart'), {
    type: 'bar',
    data: {
        labels: ['Dolor', 'Fiebre', 'Fatiga', 'Náuseas'],
        datasets: [{
        label: 'Frecuencia',
        data: [6, 3, 8, 4],
        backgroundColor: ['#006380', '#008d95', '#00acc1', '#4dd0e1']
        }]
    },
    options: {
        responsive: true
    }
});

// Gráfico de pastel - Adherencia
const pieChart = new Chart(document.getElementById('pieChart'), {
    type: 'pie',
    data: {
        labels: ['Dosis Tomadas', 'Dosis Olvidadas'],
        datasets: [{
        data: [85, 15],
        backgroundColor: ['#66bb6a', '#ef5350']
        }]
    },
    options: {
        responsive: true
    }
});
  