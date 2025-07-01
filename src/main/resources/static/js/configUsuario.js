function mostrarSeccion(id, boton) {
    document.querySelectorAll('.config-seccion').forEach(seccion => {
      seccion.classList.remove('activa');
    });
    document.getElementById(id).classList.add('activa');
  
    document.querySelectorAll('.btn-config').forEach(btn => {
      btn.classList.remove('activo');
    });
    boton.classList.add('activo');
  }
  
  document.querySelector('.btn-guardar-config').addEventListener('click', function() {
    const mensaje = document.getElementById('mensaje-confirmacion');
    mensaje.style.display = 'block';
    setTimeout(() => {
      mensaje.style.display = 'none';
    }, 3000);
});
  