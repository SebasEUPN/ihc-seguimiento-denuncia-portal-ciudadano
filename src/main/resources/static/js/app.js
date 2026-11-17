/* =====================================================================
   app.js — Lógica de las pantallas del Portal Ciudadano.
   Es el mismo comportamiento del prototipo, pero la información ya no
   está escrita en el código: se consulta a SQL Server por medio del
   backend (ver api.js) después de validar el código y el PIN.
   ===================================================================== */

// Atajo para buscar un elemento por su id
const $ = id => document.getElementById(id);

// Estado de la consulta actual en el navegador
const estado = {
  ciudadano: null,       // datos del ciudadano verificado (null = sin verificar)
  denuncias: [],         // lista "Mis denuncias registradas"
  actual: null,          // detalle de la denuncia abierta
  codigoPendiente: "",   // código escrito en el paso 1, a la espera del PIN
  mensajesChat: -1       // cantidad de mensajes dibujados (para saber si llegó uno nuevo)
};

// Iconos SVG (Bootstrap Icons). Son textos fijos del programa, por eso pueden insertarse con innerHTML.
const ICONO_CARPETA = '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16"><path d="M9.828 3h3.982a2 2 0 0 1 1.992 2.181l-.637 7A2 2 0 0 1 13.174 14H2.825a2 2 0 0 1-1.991-1.819l-.637-7a2 2 0 0 1 .342-1.31L.5 3a2 2 0 0 1 2-2h3.672a2 2 0 0 1 1.414.586l.828.828A2 2 0 0 0 9.828 3m-8.322.12q.322-.119.684-.12h5.396l-.707-.707A1 1 0 0 0 6.172 2H2.5a1 1 0 0 0-1 .981z"/></svg>';
const ICONO_CONFIRMADO = '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16"><path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0m-3.97-3.03a.75.75 0 0 0-1.08.022L7.477 9.417 5.384 7.323a.75.75 0 0 0-1.06 1.06L6.97 11.03a.75.75 0 0 0 1.079-.02l3.992-4.99a.75.75 0 0 0-.01-1.05z"/></svg>';
// Iconos de las pestañas Evidencias e Historial y de los documentos del Ministerio Público (Bootstrap Icons)
const ICONO_DOCUMENTO = '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16"><path d="M9.293 0H4a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V4.707A1 1 0 0 0 13.707 4L10 .293A1 1 0 0 0 9.293 0M9.5 3.5v-2l3 3h-2a1 1 0 0 1-1-1M4.5 9a.5.5 0 0 1 0-1h7a.5.5 0 0 1 0 1zM4 10.5a.5.5 0 0 1 .5-.5h7a.5.5 0 0 1 0 1h-7a.5.5 0 0 1-.5-.5m.5 2.5a.5.5 0 0 1 0-1h4a.5.5 0 0 1 0 1z"/></svg>';
const ICONO_REGISTRADA = '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16"><path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0m-3.97-3.03a.75.75 0 0 0-1.08.022L7.477 9.417 5.384 7.323a.75.75 0 0 0-1.06 1.06L6.97 11.03a.75.75 0 0 0 1.079-.02l3.992-4.99a.75.75 0 0 0-.01-1.05z"/></svg>';
const ICONO_ACTUALIZACION = '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16"><path fill-rule="evenodd" d="M8 3a5 5 0 1 0 4.546 2.914.5.5 0 0 1 .908-.417A6 6 0 1 1 8 2z"/><path d="M8 4.466V.534a.25.25 0 0 1 .41-.192l2.36 1.966c.12.1.12.284 0 .384L8.41 4.658A.25.25 0 0 1 8 4.466"/></svg>';
const ICONO_FISCAL = '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16"><path d="M8.515 1.019A7 7 0 0 0 8 1V0a8 8 0 0 1 .589.022zm2.004.45a7 7 0 0 0-.985-.299l.219-.976q.576.129 1.126.342zm1.37.71a7 7 0 0 0-.439-.27l.493-.87a8 8 0 0 1 .979.654l-.615.789a7 7 0 0 0-.418-.302zm1.834 1.79a7 7 0 0 0-.653-.796l.724-.69q.406.429.747.91zm.744 1.352a7 7 0 0 0-.214-.468l.893-.45a8 8 0 0 1 .45 1.088l-.95.313a7 7 0 0 0-.179-.483m.53 2.507a7 7 0 0 0-.1-1.025l.985-.17q.1.58.116 1.17zm-.131 1.538q.05-.254.081-.51l.993.123a8 8 0 0 1-.23 1.155l-.964-.267q.069-.247.12-.501m-.952 2.379q.276-.436.486-.908l.914.405q-.24.54-.555 1.038zm-.964 1.205q.183-.183.35-.378l.758.653a8 8 0 0 1-.401.432z"/><path d="M8 1a7 7 0 1 0 4.95 11.95l.707.707A8.001 8.001 0 1 1 8 0z"/><path d="M7.5 3a.5.5 0 0 1 .5.5v5.21l3.248 1.856a.5.5 0 0 1-.496.868l-3.5-2A.5.5 0 0 1 7 9V3.5a.5.5 0 0 1 .5-.5"/></svg>';

// Pantallas que solo pueden verse después de validar el código y el PIN
const VISTAS_PROTEGIDAS = ["prosecutor", "cases", "result", "notifications"];

/* ---------------------------------------------------------------------
   Utilidades
   --------------------------------------------------------------------- */

// Crea un elemento con clase y texto. Se usa textContent (no innerHTML) para que
// ningún dato de la base de datos pueda interpretarse como HTML (protección contra XSS).
function crear(etiqueta, clase, texto) {
  const elemento = document.createElement(etiqueta);
  if (clase) elemento.className = clase;
  if (texto !== undefined && texto !== null) elemento.textContent = texto;
  return elemento;
}

// "2026-10-12T09:42:00" -> "12 oct 2026 · 09:42"   |   "2026-10-12" -> "12 oct 2026"
const MESES = ["ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"];
function fecha(iso, conHora = true) {
  if (!iso) return "—";
  const [dia, hora = ""] = iso.split("T");
  const [anio, mes, d] = dia.split("-");
  let texto = `${d} ${MESES[Number(mes) - 1]} ${anio}`;
  if (conHora && hora) texto += " · " + hora.slice(0, 5);
  return texto;
}

// Iniciales para los avatares: "Dra. Carla Mendoza" -> "CM"
function iniciales(nombre) {
  return String(nombre || "").replace(/\b(Dr|Dra|Sr|Sra)\.\s*/g, "").split(" ").filter(Boolean).slice(0, 2).map(p => p[0].toUpperCase()).join("") || "—";
}

// Color de la etiqueta según el estado: verde (en curso), amarillo (pendiente) o gris (cerrado)
function claseEstado(nombreEstado) {
  if (["Por resolver", "Con disposición"].includes(nombreEstado)) return "status review";
  if (["Resuelto", "Archivado"].includes(nombreEstado)) return "status closed";
  return "status";
}
function pintarEstado(elemento, nombreEstado) {
  elemento.textContent = String(nombreEstado || "—").toUpperCase();
  elemento.className = claseEstado(nombreEstado);
}

// Muestra u oculta un mensaje dentro de un recuadro de alerta
function avisar(elemento, texto, tipo = "error") {
  elemento.className = "alert " + tipo;
  elemento.textContent = texto;
}

/* ---------------------------------------------------------------------
   Navegación entre pantallas
   --------------------------------------------------------------------- */

const views = [...document.querySelectorAll(".view")], nav = [...document.querySelectorAll("nav button")];

function showView(id) {
  // Sin verificación no se puede entrar a las pantallas con datos del expediente
  if (VISTAS_PROTEGIDAS.includes(id) && !estado.ciudadano) {
    id = "home";
    avisar(error, "Para ver esa sección, primero consulta tu denuncia con tu código y tu PIN.");
  }
  views.forEach(v => v.classList.toggle("hidden", v.id !== id));
  const navView = id === "result" ? "cases" : id === "verify" ? "home" : id;
  nav.forEach(b => { const on = b.dataset.view === navView; b.classList.toggle("active", on); if (on) b.setAttribute("aria-current", "page"); else b.removeAttribute("aria-current"); });
  window.scrollTo({ top: 0, behavior: "smooth" });

  // Cada pantalla pide al backend la información que necesita
  if (id === "cases") cargarDenuncias();
  if (id === "prosecutor") mostrarFiscalia();
  if (id === "notifications") cargarNotificaciones();
}
nav.forEach(b => b.addEventListener("click", () => showView(b.dataset.view)));
$("helpTop").addEventListener("click", () => showView("help"));

/* ---------------------------------------------------------------------
   Accesibilidad: modo de color, lectura por voz y tamaño del texto
   --------------------------------------------------------------------- */

// Cada modo tiene sus colores en styles.css (reglas body[data-color-mode="..."]).
// "corto" es el texto que se muestra en el botón para saber qué modo está activo.
const colorModes = [
  { id: "normal", label: "vista normal", corto: "Color" },
  { id: "colorblind", label: "paleta general para daltonismo", corto: "Daltonismo" },
  { id: "protanopia", label: "protanopia (dificultad con el rojo)", corto: "Protanopia" },
  { id: "deuteranopia", label: "deuteranopia (dificultad con el verde)", corto: "Deuteranopia" },
  { id: "tritanopia", label: "tritanopia (dificultad con azul y amarillo)", corto: "Tritanopia" },
  { id: "mono", label: "acromatopsia (escala de grises)", corto: "Grises" },
  { id: "contrast", label: "alto contraste", corto: "Contraste" },
  { id: "dark", label: "modo oscuro", corto: "Oscuro" }
];
const colorModeButton = $("colorMode");
function setColorMode(modeId) {
  const mode = colorModes.find(item => item.id === modeId) || colorModes[0];
  if (mode.id === "normal") document.body.removeAttribute("data-color-mode"); else document.body.dataset.colorMode = mode.id;
  colorModeButton.setAttribute("aria-label", `Cambiar colores: ${mode.label}`);
  colorModeButton.title = `Modo actual: ${mode.label}. Pulsa para cambiar.`;
  const etiqueta = colorModeButton.querySelector("span");
  if (etiqueta) etiqueta.textContent = mode.corto;
  // La preferencia se recuerda en el navegador (si este lo permite)
  try { localStorage.setItem("pnp-color-mode", mode.id); } catch (e) { /* almacenamiento no disponible */ }
}
let modoGuardado = "normal";
try { modoGuardado = localStorage.getItem("pnp-color-mode") || "normal"; } catch (e) { /* almacenamiento no disponible */ }
setColorMode(modoGuardado);
colorModeButton.addEventListener("click", () => {
  const current = document.body.dataset.colorMode || "normal";
  const next = colorModes[(colorModes.findIndex(item => item.id === current) + 1) % colorModes.length];
  setColorMode(next.id);
});

const voiceReader = $("voiceReader");
voiceReader.addEventListener("click", () => {
  if (!("speechSynthesis" in window)) { voiceReader.setAttribute("title", "La lectura por voz no está disponible en este navegador"); return; }
  if (speechSynthesis.speaking) { speechSynthesis.cancel(); voiceReader.setAttribute("aria-pressed", "false"); voiceReader.querySelector("span").textContent = "Escuchar"; return; }
  const visibleView = document.querySelector(".view:not(.hidden)");
  const message = new SpeechSynthesisUtterance(visibleView.innerText);
  message.lang = "es-PE"; message.rate = .95;
  message.addEventListener("end", () => { voiceReader.setAttribute("aria-pressed", "false"); voiceReader.querySelector("span").textContent = "Escuchar"; });
  speechSynthesis.speak(message); voiceReader.setAttribute("aria-pressed", "true"); voiceReader.querySelector("span").textContent = "Detener";
});

let font = 16;
$("larger").addEventListener("click", () => { font = Math.min(32, font + 2); document.documentElement.style.setProperty("--base", font + "px"); });
$("smaller").addEventListener("click", () => { font = Math.max(12, font - 2); document.documentElement.style.setProperty("--base", font + "px"); });

// Si la imagen del escudo no se pudiera mostrar, aparece un recuadro con el texto "PNP"
const escudo = document.querySelector(".seal img");
function escudoDeReserva() { const caja = escudo.parentElement; caja.classList.add("seal-text"); caja.textContent = "PNP"; }
if (escudo.complete && escudo.naturalWidth === 0) escudoDeReserva(); else escudo.addEventListener("error", escudoDeReserva);

/* ---------------------------------------------------------------------
   Acceso — paso 1: código de denuncia
   --------------------------------------------------------------------- */

const codeInput = $("caseCode"), error = $("errorText");

async function lookup() {
  const val = codeInput.value.trim().toUpperCase();
  error.classList.add("hidden");
  if (!val) { avisar(error, "Escribe el código de denuncia para continuar."); codeInput.focus(); return; }
  try {
    // El backend solo confirma que el código existe; todavía no entrega ningún dato
    const respuesta = await API.post("/acceso/consulta", { codigo: val });
    estado.codigoPendiente = respuesta.codigo;
    $("verifyCode").textContent = respuesta.codigo;
    $("citizenPin").value = ""; $("pinFeedback").classList.add("hidden");
    showView("verify"); $("citizenPin").focus();
  } catch (e) { avisar(error, e.message); codeInput.focus(); }
}
$("lookupForm").addEventListener("submit", e => { e.preventDefault(); lookup(); });
$("sampleBtn").addEventListener("click", () => { codeInput.value = "D-000584"; lookup(); });

/* ---------------------------------------------------------------------
   Acceso — paso 2: PIN del ciudadano
   --------------------------------------------------------------------- */

$("togglePin").addEventListener("click", e => {
  const pin = $("citizenPin"), show = pin.type === "password";
  pin.type = show ? "text" : "password"; e.currentTarget.setAttribute("aria-pressed", String(show)); e.currentTarget.setAttribute("aria-label", show ? "Ocultar PIN" : "Mostrar PIN");
});
$("forgotPin").addEventListener("click", () => {
  avisar($("pinFeedback"), "Para recuperar tu PIN, comunícate con la dependencia policial donde registraste tu denuncia.", "success");
});
$("pinForm").addEventListener("submit", async e => {
  e.preventDefault();
  const pin = $("citizenPin"), feedback = $("pinFeedback");
  if (!/^\d{6}$/.test(pin.value)) { avisar(feedback, "El PIN debe tener 6 dígitos."); pin.focus(); return; }
  try {
    // El backend compara el PIN con el que tiene cifrado y, si coincide, abre la sesión
    const respuesta = await API.post("/acceso/verificar", { pin: pin.value });
    pin.value = ""; feedback.classList.add("hidden");
    iniciarConsulta(respuesta.ciudadano);
    await abrirDenuncia(respuesta.codigo);
  } catch (err) { pin.value = ""; avisar(feedback, err.message); pin.focus(); }
});

// Guarda al ciudadano verificado y muestra el botón "Salir"
function iniciarConsulta(ciudadano) {
  estado.ciudadano = ciudadano; estado.actual = null; estado.denuncias = [];
  $("logoutTop").classList.remove("hidden");
}

// Borra del navegador todo lo consultado y vuelve al inicio
function limpiarConsulta(mensaje) {
  estado.ciudadano = null; estado.denuncias = []; estado.actual = null; estado.codigoPendiente = ""; estado.mensajesChat = -1;
  $("logoutTop").classList.add("hidden");
  $("caseList").replaceChildren(); $("noticeList").replaceChildren(); $("chatMessages").replaceChildren();
  codeInput.value = ""; error.classList.add("hidden");
  showView("home");
  if (mensaje) avisar(error, mensaje);
}

// Cierra la consulta en el servidor (botones "Salir" y "Salir y hacer otra consulta")
async function salir() {
  try { await API.post("/acceso/salir"); } catch (e) { /* aunque falle, se limpia la pantalla */ }
  limpiarConsulta(); codeInput.focus();
}
$("logoutTop").addEventListener("click", salir);
$("newLookup").addEventListener("click", salir);

// api.js llama a esta función cuando el servidor responde que la consulta expiró
function alCerrarseLaConsulta() {
  if (estado.ciudadano) limpiarConsulta("Tu consulta se cerró por seguridad. Ingresa nuevamente tu código y PIN.");
}

/* ---------------------------------------------------------------------
   Seguimiento: "Mis denuncias registradas"
   --------------------------------------------------------------------- */

async function cargarDenuncias() {
  try { estado.denuncias = await API.get("/denuncias"); } catch (e) { return; }
  renderCaseList();
}

function renderCaseList() {
  const list = $("caseList"); list.replaceChildren();
  if (!estado.denuncias.length) { list.appendChild(crear("p", "empty-note", "No tienes denuncias registradas.")); return; }
  estado.denuncias.forEach(data => {
    const button = crear("button", "case-option"); button.type = "button";
	const icon = crear("span", "case-option-icon"); icon.innerHTML = ICONO_CARPETA; icon.setAttribute("aria-hidden", "true");    const info = crear("span");
    const meta = crear("span", "case-option-meta");
    meta.append(crear("span", "", fecha(data.fechaRegistro)), crear("span", "", data.tipoDelito));
    info.append(crear("h3", "", data.codigo), meta);
    const status = crear("span"); pintarEstado(status, data.estado);
    const arrow = crear("span", "case-option-arrow", "→"); arrow.setAttribute("aria-hidden", "true");
    button.append(icon, info, status, arrow);
    button.addEventListener("click", () => abrirDenuncia(data.codigo));
    list.appendChild(button);
  });
}
$("backCases").addEventListener("click", () => showView("cases"));
$("goTracking").addEventListener("click", () => showView("cases"));

/* ---------------------------------------------------------------------
   Resultado: detalle de una denuncia
   --------------------------------------------------------------------- */

async function abrirDenuncia(codigo) {
  let d;
  try { d = await API.get("/denuncias/" + encodeURIComponent(codigo)); }
  catch (e) { if (estado.ciudadano) { showView("cases"); } return; }
  estado.actual = d;

  // Datos personales del ciudadano verificado
  const c = estado.ciudadano;
  $("citizenName").textContent = c.nombreCompleto; $("citizenDni").textContent = c.dni;
  $("citizenEmail").textContent = c.correo || "No registrado"; $("citizenPhone").textContent = c.telefono || "No registrado";

  // Datos de la denuncia
  $("resultCode").textContent = d.codigo; pintarEstado($("resultStatus"), d.estado);
  $("resultMeaning").textContent = d.explicacion;
  $("resultType").textContent = d.tipoDelito;
  $("resultFolder").textContent = d.carpetaFiscal || "No asignada";
  $("resultOffice").textContent = d.despachoFiscal || "Pendiente de asignación";
  $("resultProsecutor").textContent = d.fiscalResponsable || "Pendiente de asignación";
  $("resultDate").textContent = fecha(d.fechaRegistro); $("resultUpdate").textContent = fecha(d.ultimaActualizacion);
  $("resultPlace").textContent = d.lugarHechos || "No registrado"; $("resultDescription").textContent = d.descripcion;

  // Pestaña "Evidencias": documentos registrados en el expediente
  const evidencias = $("evidenceList"); evidencias.replaceChildren();
  d.documentos.forEach(doc => evidencias.appendChild(filaExpediente(ICONO_DOCUMENTO, doc.nombre, "Registrado el " + fecha(doc.fecha, false) + ".",
    doc.disponible ? "Incorporado al expediente" : "En revisión de acceso", !doc.disponible)));
  if (!d.documentos.length) evidencias.appendChild(crear("p", "empty-note", "Aún no hay documentos registrados para esta denuncia."));

  // Pestaña "Historial": registro, cambios de estado y actuaciones
  const historial = $("historyList"); historial.replaceChildren();
  // Icono de cada hecho: registro (✓), actuación o cambio de la Fiscalía (reloj) o de la PNP (flecha circular)
  d.historial.forEach((ev, i) => historial.appendChild(filaExpediente(i === 0 ? ICONO_REGISTRADA : /fiscal/i.test(ev.detalle) ? ICONO_FISCAL : ICONO_ACTUALIZACION,
    fecha(ev.fechaHora, ev.conHora) + " — " + ev.titulo, ev.detalle)));

  // Funcionario responsable
  const r = d.responsable;
  $("officerInitials").textContent = r ? iniciales(r.nombre) : "—";
  $("officerName").textContent = r ? r.nombre : "Pendiente de asignación";
  $("officerRole").textContent = r && r.cargo ? r.cargo + " · Responsable asignado" : "Responsable asignado";
  $("officerOffice").textContent = r && r.dependencia ? r.dependencia : "Pendiente de asignación";
  $("officerLast").textContent = fecha(d.ultimaActualizacion);

  seleccionarPestana("tabEvidence");
  estado.mensajesChat = -1; $("contactForm").reset();
  $("contactFeedback").classList.add("hidden"); $("callFeedback").classList.add("hidden");
  showView("result");
  cargarChat();
}

// Fila de las listas de evidencias e historial: icono, título, texto y etiqueta opcional
function filaExpediente(icono, titulo, texto, etiqueta, pendiente) {
  const fila = crear("div", "evidence-item");
  // Los iconos son SVG fijos del programa (no datos de la base), por eso pueden insertarse con innerHTML
  const icon = crear("div", "evidence-icon"); icon.innerHTML = icono; icon.setAttribute("aria-hidden", "true");
  const contenido = crear("div", "evidence-content");
  contenido.append(crear("h3", "", titulo), crear("p", "", texto));
  if (etiqueta) contenido.appendChild(crear("span", "pill" + (pendiente ? " pending" : ""), etiqueta));
  fila.append(icon, contenido);
  return fila;
}

// Pestañas Evidencias / Pericias / Historial
const evidenceTabs = [["tabEvidence", "panelEvidence"], ["tabPericia", "panelPericia"], ["tabHistory", "panelHistory"]];
function seleccionarPestana(tabId) {
  evidenceTabs.forEach(([tid, pid]) => { const active = tid === tabId; $(tid).setAttribute("aria-selected", String(active)); $(pid).classList.toggle("hidden", !active); });
}
evidenceTabs.forEach(([tabId]) => $(tabId).addEventListener("click", () => seleccionarPestana(tabId)));

$("printCase").addEventListener("click", () => window.print());

/* ---------------------------------------------------------------------
   Chat con el funcionario responsable
   (son las mismas tablas del "Chat institucional" del primer prototipo)
   --------------------------------------------------------------------- */

const chatMessages = $("chatMessages");

async function cargarChat() {
  if (!estado.actual) return;
  const codigo = estado.actual.codigo;
  let chat;
  try { chat = await API.get(`/denuncias/${encodeURIComponent(codigo)}/chat`); } catch (e) { return; }
  // Si mientras tanto se abrió otra denuncia, esta respuesta ya no corresponde
  if (estado.actual && estado.actual.codigo === codigo) dibujarChat(chat);
}

function dibujarChat(chat) {
  const formulario = $("contactForm"), panel = $("liveChat"), aviso = $("contactFeedback");

  // Sin funcionario asignado todavía no se puede conversar
  if (!chat.disponible) {
    formulario.classList.add("hidden"); panel.classList.add("hidden");
    avisar(aviso, "Tu denuncia aún no tiene un funcionario asignado. Podrás enviar consultas cuando se asigne.", "success");
    return;
  }
  // Primera vez: se muestra el formulario de consulta. Con mensajes: se muestra el chat.
  const hayMensajes = chat.mensajes.length > 0;
  formulario.classList.toggle("hidden", hayMensajes); panel.classList.toggle("hidden", !hayMensajes);
  $("chatInitials").textContent = iniciales(estado.actual.responsable ? estado.actual.responsable.nombre : chat.funcionario); $("chatOfficerName").textContent = chat.funcionario;
  $("chatStatus").textContent = chat.enLinea ? "En línea" : "Responderá cuando esté disponible";

  // Solo se vuelve a dibujar la lista si cambió la cantidad de mensajes
  if (chat.mensajes.length === estado.mensajesChat) return;
  estado.mensajesChat = chat.mensajes.length;
  chatMessages.replaceChildren();
  chat.mensajes.forEach(m => {
    const bubble = crear("div", "chat-bubble" + (m.mio ? " citizen" : ""));
    bubble.appendChild(crear("strong", "", (m.mio ? "Tú" : chat.funcionario) + " · " + fecha(m.fechaHora)));
    if (m.adjunto) {
      // Archivo enviado por el funcionario: enlace de descarga
      const enlace = crear("a", "chat-file", "⬇ " + m.adjunto.nombre + " (" + Math.max(1, Math.round(m.adjunto.tamano / 1024)) + " KB)");
      enlace.href = m.adjunto.url; enlace.download = m.adjunto.nombre;
      bubble.appendChild(enlace);
    } else {
      bubble.appendChild(crear("span", "", m.texto));
    }
    chatMessages.appendChild(bubble);
  });
  chatMessages.scrollTop = chatMessages.scrollHeight;
}

// Primera consulta: motivo + mensaje
$("contactForm").addEventListener("submit", async e => {
  e.preventDefault();
  const topic = $("contactTopic").value, message = $("contactMessage").value.trim(), feedback = $("contactFeedback");
  if (!topic || !message) { avisar(feedback, "Selecciona un motivo y escribe tu mensaje para continuar."); return; }
  await enviarMensaje(`${topic}: ${message}`, feedback);
  $("chatMessage").focus();
});

// Mensajes siguientes desde el chat
$("chatForm").addEventListener("submit", async e => {
  e.preventDefault();
  const input = $("chatMessage"), message = input.value.trim();
  if (!message) return;
  if (await enviarMensaje(message, $("callFeedback"))) input.value = "";
});

// Guarda el mensaje en la base de datos y vuelve a dibujar la conversación
async function enviarMensaje(texto, dondeAvisar) {
  try {
    const chat = await API.post(`/denuncias/${encodeURIComponent(estado.actual.codigo)}/chat`, { texto });
    dibujarChat(chat); $("callFeedback").classList.add("hidden"); $("contactFeedback").classList.add("hidden");
    return true;
  } catch (e) { avisar(dondeAvisar, e.message); return false; }
}

$("callOfficer").addEventListener("click", () => {
  avisar($("callFeedback"), "Las llamadas no están disponibles desde el portal. Escribe tu consulta por este chat o acude a la dependencia responsable.", "success");
});

// Cada 10 segundos se consulta si el funcionario respondió (solo con la denuncia abierta)
setInterval(() => {
  if (estado.ciudadano && estado.actual && !document.hidden && !$("result").classList.contains("hidden")) cargarChat();
}, 10000);

/* ---------------------------------------------------------------------
   Ministerio Público: información fiscal de la denuncia
   --------------------------------------------------------------------- */

async function mostrarFiscalia() {
  // Se muestra la denuncia abierta; si aún no se abrió ninguna, la más reciente
  if (!estado.actual) {
    try {
      if (!estado.denuncias.length) estado.denuncias = await API.get("/denuncias");
      if (estado.denuncias.length) estado.actual = await API.get("/denuncias/" + encodeURIComponent(estado.denuncias[0].codigo));
    } catch (e) { return; }
  }
  const d = estado.actual; if (!d) return;

  $("mpCode").textContent = d.codigo; $("mpFolder").textContent = d.carpetaFiscal || "No asignada";
  pintarEstado($("mpStatus"), d.estadoFiscal);
  $("mpType").textContent = d.tipoDelito; $("mpDate").textContent = fecha(d.fechaRegistro, false);
  $("mpOffice").textContent = d.despachoFiscal || "Pendiente de asignación";
  $("mpProsecutor").textContent = d.fiscalResponsable || "Pendiente de asignación";
  $("mpInitials").textContent = d.fiscalResponsable ? iniciales(d.fiscalResponsable) : "—";
  $("mpProsecutorName").textContent = d.fiscalResponsable || "Pendiente de asignación";
  $("mpProsecutorOffice").textContent = d.despachoFiscal || "";

  // Etapas de la investigación (calculadas por el backend a partir del estado fiscal)
  const etiquetas = { COMPLETADO: "Completado", EN_PROCESO: "En proceso", PENDIENTE: "Pendiente" };
  const pasos = $("mpProgress"); pasos.replaceChildren();
  d.avanceFiscal.forEach((paso, i) => {
    const li = crear("li"), contenido = crear("div"), hecho = paso.estado === "COMPLETADO";
    contenido.append(crear("h3", "", paso.titulo), crear("span", "pill" + (hecho ? "" : " pending"), etiquetas[paso.estado]));
    if (paso.fecha) contenido.appendChild(crear("time", "", fecha(paso.fecha, false)));
    li.append(crear("span", "mp-step" + (paso.estado === "PENDIENTE" ? " pending" : ""), String(i + 1)), contenido);
    pasos.appendChild(li);
  });

  // Documentos de la denuncia
  const docs = $("mpDocs"); docs.replaceChildren();
  d.documentos.forEach(doc => {
    const fila = crear("div", "mp-row"), icono = crear("div", "mp-row-icon"), texto = crear("div");
    icono.innerHTML = ICONO_DOCUMENTO;
    icono.setAttribute("aria-hidden", "true"); fila.style.marginBottom = "12px";
    texto.append(crear("strong", "", doc.nombre), crear("p", "", fecha(doc.fecha, false) + " · " + (doc.disponible ? "Incorporado al expediente" : "En revisión de acceso")));
    fila.append(icono, texto); docs.appendChild(fila);
  });
  if (!d.documentos.length) docs.appendChild(crear("p", "empty-note", "Aún no hay documentos registrados."));
}

/* ---------------------------------------------------------------------
   Notificaciones
   --------------------------------------------------------------------- */

async function cargarNotificaciones() {
  let avisos;
  try { avisos = await API.get("/notificaciones"); } catch (e) { return; }
  const lista = $("noticeList"); lista.replaceChildren();
  const iconos = { REGISTRO: "✓", ESTADO: "i", MENSAJE: "✉" };
  avisos.forEach(n => {
    const articulo = crear("article", "notice"), icono = crear("div", "noticeicon", iconos[n.tipo] || "i"), texto = crear("div");
    icono.setAttribute("aria-hidden", "true");
	if (n.tipo === "REGISTRO") icono.innerHTML = ICONO_CONFIRMADO; // aviso "Registro confirmado"
    const hora = crear("time", "", fecha(n.fechaHora)); hora.dateTime = n.fechaHora;
    texto.append(hora, crear("h3", "", n.titulo), crear("p", "", n.detalle));
    articulo.append(icono, texto); lista.appendChild(articulo);
  });
  if (!avisos.length) lista.appendChild(crear("p", "empty-note", "No tienes notificaciones por el momento."));
}

/* ---------------------------------------------------------------------
   Ayuda y glosario (búsqueda en la misma página, sin backend)
   --------------------------------------------------------------------- */

function searchTerms() {
  const q = $("glossarySearch").value.toLocaleLowerCase("es").trim(); let count = 0;
  document.querySelectorAll("#glossary article").forEach(a => { const match = !q || (a.textContent + " " + a.dataset.term).toLocaleLowerCase("es").includes(q); a.classList.toggle("hidden", !match); if (match) count++; });
  document.querySelector(".faq-heading").classList.toggle("hidden", Boolean(q)); $("noTerms").classList.toggle("hidden", count > 0);
}
$("searchGlossary").addEventListener("click", searchTerms);
$("glossarySearch").addEventListener("input", searchTerms);
$("glossarySearch").addEventListener("keydown", e => { if (e.key === "Enter") { e.preventDefault(); searchTerms(); } });

/* ---------------------------------------------------------------------
   Al cargar la página: si el servidor aún tiene abierta la consulta
   (por ejemplo, tras recargar con F5) se va directo a "Mis denuncias".
   --------------------------------------------------------------------- */
(async () => {
  try {
    const ciudadano = await API.get("/acceso/sesion");
    if (ciudadano) { iniciarConsulta(ciudadano); showView("cases"); }
  } catch (e) { /* sin consulta abierta: se queda en el inicio */ }
})();
