/* Orquestacion: llama a la API y actualiza el estado. Sin acceso al DOM salvo toast. */
import { api } from "./api.js";
import { getState, setState } from "./state.js";
import { toast } from "./dom.js";

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const sello = (texto, error = false) => setState({ authMsg: { texto, error } });

const irAAuth = (texto, error = true) =>
  setState({
    vista: "auth", usuario: null, prestamos: [], libros: [],
    cargado: false, cargando: false, enviando: false,
    authMsg: texto ? { texto, error } : null
  });

export const sesionCaducada = () => irAAuth("La sesión ha caducado.");

export const cambiarTab = (tab) => setState({ tab: tab === "register" ? "register" : "login", authMsg: null });

export const cargarBiblioteca = async () => {
  if (!api.tieneSesion()) { irAAuth(null); return; }
  setState({ vista: "library", cargando: true, error: null });
  try {
    const [usuario, prestamos, libros] = await Promise.all([api.miUsuario(), api.misPrestamos(), api.libros()]);
    setState({ usuario, prestamos, libros, cargado: true, cargando: false });
  } catch (err) {
    if (err.message && err.message.includes("caducada")) {
      irAAuth("La sesión caducó. Firme la ficha de nuevo.");
    } else {
      setState({ cargando: false, error: err.message });
      toast(err.message, true);
    }
  }
};

/* datos: { nombre, email, password } ya leidos del formulario. */
export const enviarAuth = async ({ nombre, email, password }) => {
  const { tab } = getState();
  const esRegistro = tab === "register";
  if (!EMAIL_RE.test(email)) { sello("Indique un correo válido.", true); return; }
  if (esRegistro && !nombre) { sello("Indique su nombre para el alta.", true); return; }
  if (password.length < 8 || password.length > 72) {
    sello("La contraseña debe tener entre 8 y 72 caracteres.", true);
    return;
  }

  setState({ enviando: true, authMsg: null });
  try {
    if (esRegistro) {
      try { await api.register(nombre, email, password); } catch { /* si falla, se intenta el login */ }
    }
    await api.login(email, password);
    api.marcarSesion(true);
    setState({ enviando: false });
    await cargarBiblioteca();
  } catch (err) {
    setState({ enviando: false, authMsg: { texto: err.message, error: true } });
  }
};

export const logout = async () => {
  try { await api.logout(); } catch { /* se ignora: se cierra la sesion local igualmente */ }
  api.marcarSesion(false);
  location.reload();
};

const operacion = async (btn, peticion, mensajeOk) => {
  btn.disabled = true;
  try {
    const res = await peticion();
    toast(mensajeOk(res));
    await cargarBiblioteca();
  } catch (err) {
    btn.disabled = false;
    toast(err.message, true);
  }
};

export const solicitar = (btn, id) =>
  operacion(btn, () => api.solicitar(id), (p) => `El libro «${p.libro.titulo}» pasa a su estantería.`);

export const devolver = (btn, id) =>
  operacion(btn, () => api.devolver(id), (p) => `Se devuelve «${p.libro.titulo}» a la sala.`);

export const retirar = (btn, id) =>
  operacion(btn, () => api.eliminarLibro(id), () => "Volumen retirado del catálogo.");

export const crearLibro = async (form, { titulo, autor, isbn, genero }, btn) => {
  if (!titulo || !autor || !isbn) { toast("Complete título, autor e ISBN.", true); return; }
  btn.disabled = true;
  try {
    const nuevo = await api.crearLibro({ titulo, autor, isbn, genero });
    toast(`«${nuevo.titulo}» añadido al catálogo.`);
    form.reset();
    btn.disabled = false;
    await cargarBiblioteca();
  } catch (err) {
    btn.disabled = false;
    toast(err.message, true);
  }
};
