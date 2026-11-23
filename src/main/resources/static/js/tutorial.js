/* ---------------------------------------------------------------------
   Tutorial del portal ciudadano (usa js/tour.js).
   Cada pantalla tiene su propio recorrido, adaptado a lo que el ciudadano ve:
     inicio (consulta del código), verificación con PIN, historial de denuncias
     y detalle de la denuncia.
   - Cada recorrido se abre solo la primera vez que se llega a esa pantalla.
   - El botón "Tutorial" pregunta si se quiere repetir el recorrido de la pantalla actual.
   --------------------------------------------------------------------- */

const barraAccesibilidad = [
  { el: "#colorMode", titulo: "Modos de color", texto: "Cambia los colores de la página: modo oscuro, alto contraste y modos para distintos tipos de daltonismo." },
  { el: "#voiceReader", titulo: "Escuchar", texto: "Lee en voz alta el contenido de la página." },
  { el: ["#smaller", "#larger"], titulo: "Tamaño del texto", texto: "Reduce (A−) o aumenta (A+) el tamaño de las letras." },
  { el: "#helpTop", titulo: "Ayuda", texto: "Preguntas frecuentes y un glosario con los términos del proceso explicados en lenguaje sencillo." },
  { el: "#tutorialBtn", titulo: "Repetir el tutorial", texto: "Si quieres ver este recorrido otra vez, pulsa <b>Tutorial</b>." }
];

const TUTORIALES = {
  home: [
    { titulo: "¡Bienvenido al portal de seguimiento de denuncias!",
      texto: "Te mostramos en pocos pasos cómo consultar el avance de tu denuncia. Puedes avanzar, retroceder o salir cuando quieras (también con las flechas del teclado y Esc)." },
    { el: ".navwrap nav", titulo: "Secciones del portal", texto: "Inicio, Ministerio Público, Seguimiento, Notificaciones y Ayuda. Las secciones con datos de tu denuncia se habilitan después de verificar tu identidad." },
    { el: "#lookupForm .inputrow", titulo: "1. Escribe tu código", texto: "Ingresa el código de tu constancia de registro, tal como aparece, y pulsa <b>Consultar</b>. Para la demostración usa <b>D-000584</b>." },
    { el: ".privacy", titulo: "Consulta protegida", texto: "Después del código te pediremos tu <b>PIN personal de 6 dígitos</b>. Sin él no se muestra ningún dato de la denuncia." },
    { el: "aside.info", titulo: "¿Cómo funciona?", texto: "Los cuatro pasos para consultar tu denuncia, siempre a la vista." },
    { el: "#sampleBtn", titulo: "Ver un ejemplo", texto: "Si quieres conocer el portal antes de consultar, abre un ejemplo de seguimiento." },
    ...barraAccesibilidad,
    { titulo: "¡Listo para consultar!", texto: "Cuando entres a tu denuncia te mostraremos un recorrido corto de esa pantalla." }
  ],
  verify: [
    { el: ".verify-code", titulo: "Verificación de identidad", texto: "Este es el código de denuncia que vas a consultar." },
    { el: ".pin-wrap", titulo: "2. Ingresa tu PIN", texto: "Escribe tu PIN personal de 6 dígitos. Con el ojo puedes mostrarlo u ocultarlo. Para la demostración usa <b>123456</b>." },
    { el: "#forgotPin", titulo: "¿Olvidaste tu PIN?", texto: "Te indicamos cómo recuperarlo sin perder tu consulta." },
    { el: "#pinForm button[type=submit]", titulo: "Verificar", texto: "Pulsa <b>Verificar</b> para ver tus denuncias." }
  ],
  cases: [
    { el: ".cases-heading", titulo: "Mis denuncias registradas", texto: "Aquí aparecen todas las denuncias registradas a tu nombre." },
    { el: () => document.querySelector("#caseList > *") || document.querySelector("#caseList"), titulo: "Elige una denuncia", texto: "Cada tarjeta muestra el código, el tipo de denuncia y su estado. Selecciónala para ver el detalle." },
    { el: ".navwrap nav", titulo: "Otras secciones", texto: "Ya verificaste tu identidad: ahora también puedes ver <b>Ministerio Público</b> (tu carpeta fiscal) y tus <b>Notificaciones</b>." },
    { el: "#logoutTop", titulo: "Salir", texto: "Cuando termines, pulsa <b>Salir</b> para cerrar tu consulta de forma segura." }
  ],
  result: [
    { el: "#backCases", titulo: "Volver al historial", texto: "Regresa a la lista de tus denuncias." },
    { el: () => document.getElementById("resultCode")?.closest(".casehead"), titulo: "Tu denuncia y su estado", texto: "El código de tu denuncia y su <b>estado actual</b>." },
    { el: ".case-facts", titulo: "Datos del caso", texto: "Carpeta fiscal, despacho, fiscal responsable, fechas y lugar de los hechos." },
    { el: () => document.getElementById("resultMeaning")?.closest(".alert"), titulo: "¿Qué significa?", texto: "Una explicación sencilla de la etapa en la que está tu denuncia y lo que sigue." },
    { el: "#evidenceSection", titulo: "Evidencias y avances", texto: "Revisa las evidencias, las pericias y el historial de actuaciones en las pestañas." },
    { el: "#responsibleCard", titulo: "Responsable asignado", texto: "El funcionario a cargo de tu denuncia y su dependencia." },
    { el: "#contactCard", titulo: "Comunícate con el responsable", texto: "Envía una consulta al funcionario a cargo. Sus respuestas aparecerán en el chat." },
    { el: "#printCase", titulo: "Constancia", texto: "Ver e imprimir la constancia de tu denuncia." },
    { el: "#logoutTop", titulo: "Salir", texto: "Al terminar, pulsa <b>Salir</b> para cerrar tu consulta de forma segura." }
  ]
};

const claveTutorial = vista => `pnp-tutorial-visto-${vista}`;
const vistaActual = () => document.querySelector("main .view:not(.hidden)")?.id || "home";

function iniciarTutorial(vista) {
  const pasos = TUTORIALES[vista];
  if (!pasos) return;
  Tour.iniciar(pasos, { alTerminar: () => Tour.marcarVisto(claveTutorial(vista)) });
}

// Se llama cada vez que cambia la pantalla: abre su recorrido solo la primera vez
function tutorialDeVista(vista) {
  if (!TUTORIALES[vista] || Tour.visto(claveTutorial(vista)) || Tour.activo) return;
  setTimeout(() => { if (vistaActual() === vista && !Tour.activo) iniciarTutorial(vista); }, 600);
}

document.getElementById("tutorialBtn")?.addEventListener("click", async () => {
  if (Tour.activo) return;
  // Las pantallas sin recorrido propio (Ministerio Público, Notificaciones, Ayuda) usan el de inicio
  const vista = TUTORIALES[vistaActual()] ? vistaActual() : "home";
  const detalle = vista === "home" ? "Se mostrará el recorrido de la página de inicio."
    : "Se mostrará otra vez el recorrido de esta pantalla.";
  if (await Tour.confirmar("¿Quieres volver a repetir el tutorial?", detalle)) {
    if (vista !== vistaActual()) showView("home");
    iniciarTutorial(vista);
  }
});

// Primera visita al portal
tutorialDeVista(vistaActual());
