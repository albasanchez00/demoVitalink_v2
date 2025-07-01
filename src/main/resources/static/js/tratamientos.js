const form = document.getElementById("formulario");
const lista = document.getElementById("tratamientos-activos");

form.addEventListener("submit", function(e) {
  e.preventDefault();

  const tratamiento = {
    nombre: document.getElementById("nombre_tratamiento").value,
    medicamento: document.getElementById("nombre_medicamento").value,
    forma: document.getElementById("forma_farmaceutica").value,
    dosis: document.getElementById("dosis").value,
    frecuencia: document.getElementById("frecuencia").value,
    horarios: ["hora1", "hora2", "hora3"].map(id => document.getElementById(id).value).filter(Boolean),
    duracion: document.getElementById("duracion").value,
    inicio: document.getElementById("inicio").value,
    conAlimentos: document.getElementById("con_alimentos").checked,
    observaciones: document.getElementById("observaciones").value,
    estado: document.getElementById("estado").value,
    sintomas: document.getElementById("relacion_sintomas").value
  };

  const item = document.createElement("li");
  item.classList.add("card");

  item.innerHTML = `
    <strong>${tratamiento.nombre}</strong><br>
    <p><b>Medicamento:</b> ${tratamiento.medicamento} (${tratamiento.dosis}, ${tratamiento.forma})</p>
    <p><b>Frecuencia:</b> ${tratamiento.frecuencia}</p>
    <p><b>Horarios:</b> ${tratamiento.horarios.join(", ")}</p>
    <p><b>Inicio:</b> ${tratamiento.inicio} - ${tratamiento.duracion} días</p>
    <p><b>Con alimentos:</b> ${tratamiento.conAlimentos ? "Sí" : "No"}</p>
    <p><b>Estado:</b> ${tratamiento.estado}</p>
    <p><b>Síntomas relacionados:</b> ${tratamiento.sintomas}</p>
    <p><b>Observaciones:</b> ${tratamiento.observaciones}</p>
    <button class="btn-eliminar" onclick="this.parentElement.remove()">Eliminar</button>
  `;

  lista.appendChild(item);
  form.reset();
});