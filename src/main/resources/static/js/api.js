/* =====================================================================
   api.js — Comunicación entre el portal ciudadano y el backend Spring Boot.
   Todas las peticiones van a rutas que empiezan con /api/portal y viajan
   con la cookie de sesión, por eso el servidor sabe qué ciudadano consulta.
   ===================================================================== */

const API = {

  /**
   * Envía una petición al backend y devuelve la respuesta convertida de JSON.
   * metodo: "GET" o "POST"
   * ruta:   ruta sin el prefijo /api/portal (ej.: "/denuncias")
   * cuerpo: objeto que se envía como JSON (opcional)
   */
  async solicitar(metodo, ruta, cuerpo) {
    const opciones = { method: metodo, credentials: "same-origin", headers: {} };
    if (cuerpo !== undefined) {
      opciones.headers["Content-Type"] = "application/json";
      opciones.body = JSON.stringify(cuerpo);
    }

    let respuesta;
    try {
      respuesta = await fetch("/api/portal" + ruta, opciones);
    } catch (error) {
      // El servidor está apagado o no hay conexión
      throw new Error("No se pudo conectar con el servidor. Revisa tu conexión e inténtalo de nuevo.");
    }

    // 204 = respuesta correcta pero sin contenido
    if (respuesta.status === 204) return null;

    // Los errores llegan como { "mensaje": "..." }
    let datos = null;
    try { datos = await respuesta.json(); } catch (error) { /* respuesta sin JSON */ }

    if (!respuesta.ok) {
      // 401 fuera de las rutas de acceso = la consulta se cerró en el servidor (por tiempo)
      if (respuesta.status === 401 && !ruta.startsWith("/acceso/") && typeof alCerrarseLaConsulta === "function") {
        alCerrarseLaConsulta();
      }
      throw new Error((datos && datos.mensaje) || "No se pudo completar la operación.");
    }
    return datos;
  },

  // Atajos
  get(ruta) { return this.solicitar("GET", ruta); },
  post(ruta, cuerpo) { return this.solicitar("POST", ruta, cuerpo === undefined ? {} : cuerpo); }
};
