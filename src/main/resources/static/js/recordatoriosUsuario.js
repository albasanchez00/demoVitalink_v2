document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("form-recordatorio");
    const lista = document.getElementById("lista-recordatorios");

    const recordatorios = JSON.parse(localStorage.getItem("recordatorios")) || [];

    function renderizar() {
      lista.innerHTML = "";
      if (recordatorios.length === 0) {
        lista.innerHTML = "<li>No hay recordatorios aún.</li>";
        return;
      }
      recordatorios.forEach((rec, index) => {
        const item = document.createElement("li");
        item.innerHTML = `<strong>${rec.titulo}</strong> - ${rec.fecha} ${rec.hora}<br>${rec.descripcion}<br>
          <button onclick="eliminarRecordatorio(${index})">Eliminar</button>`;
        lista.appendChild(item);
      });
    }

    window.eliminarRecordatorio = (index) => {
      recordatorios.splice(index, 1);
      localStorage.setItem("recordatorios", JSON.stringify(recordatorios));
      renderizar();
    };

    form.addEventListener("submit", (e) => {
      e.preventDefault();
      const nuevo = {
        titulo: form.titulo.value,
        fecha: form.fecha.value,
        hora: form.hora.value,
        descripcion: form.descripcion.value
      };
      recordatorios.push(nuevo);
      localStorage.setItem("recordatorios", JSON.stringify(recordatorios));
      form.reset();
      renderizar();
    });

    renderizar();
});