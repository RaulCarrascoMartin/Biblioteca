/**
 * Capa de datos: unico modulo que usa fetch.
 * La sesion real es una cookie same-origin; en localStorage solo se guarda un flag.
 */

const CLAVE_SESION = "biblioteca_sesion";

export const EVENTO_SESION_CADUCADA = "sesion:caducada";
export const EVENTO_CARGA = "api:carga";

const BASE_URL = "";
const PREFIJO_AUTH = "/api/auth/";

const RUTAS = {
  login: "/api/auth/login",
  register: "/api/auth/register",
  logout: "/api/auth/logout",
  miUsuario: "/api/usuarios/me",
  misPrestamos: "/api/prestamos/mios",
  prestamos: "/api/prestamos",
  libros: "/api/libros",
  devolver: (id) => `/api/prestamos/${encodeURIComponent(id)}/devolver`,
  libro: (id) => `/api/libros/${encodeURIComponent(id)}`
};

export class ApiError extends Error {
  constructor(mensaje, status = 0) {
    super(mensaje);
    this.name = "ApiError";
    this.status = status;
  }
}

let pendientes = 0;

function cambiarPendientes(delta) {
  pendientes += delta;
  window.dispatchEvent(new CustomEvent(EVENTO_CARGA, { detail: { pendientes } }));
}

function leerFlag() {
  try { return !!localStorage.getItem(CLAVE_SESION); } catch { return false; }
}

function escribirFlag(activa) {
  try {
    if (activa) localStorage.setItem(CLAVE_SESION, "1");
    else localStorage.removeItem(CLAVE_SESION);
  } catch { /* almacenamiento no disponible */ }
}

async function leerCuerpo(res) {
  const tipo = res.headers.get("content-type") || "";
  try {
    if (tipo.includes("json")) return await res.json();
    const texto = await res.text();
    return texto === "" ? null : texto;
  } catch {
    return null;
  }
}

/** Extrae un mensaje legible: {error}, ProblemDetail {detail,title}, texto plano o generico. */
function mensajeDeError(cuerpo, status) {
  if (cuerpo && typeof cuerpo === "object") {
    const m = cuerpo.error || cuerpo.detail || cuerpo.title || cuerpo.message;
    if (typeof m === "string" && m) return m;
  } else if (typeof cuerpo === "string" && cuerpo.trim() && !cuerpo.trimStart().startsWith("<")) {
    return cuerpo;
  }
  return `La solicitud falló (${status})`;
}

async function solicitar(ruta, { metodo = "GET", cuerpo } = {}) {
  const opciones = {
    method: metodo,
    headers: { "Content-Type": "application/json" },
    credentials: "same-origin"
  };
  if (cuerpo !== undefined) opciones.body = JSON.stringify(cuerpo);

  cambiarPendientes(1);
  try {
    let res;
    try {
      res = await fetch(BASE_URL + ruta, opciones);
    } catch {
      throw new ApiError("No se pudo conectar con el servidor. Compruebe su conexión.", 0);
    }

    if (res.status === 401 && !ruta.startsWith(PREFIJO_AUTH)) {
      escribirFlag(false);
      window.dispatchEvent(new CustomEvent(EVENTO_SESION_CADUCADA));
      throw new ApiError("Sesión caducada. Vuelva a firmar la ficha.", 401);
    }

    const datos = await leerCuerpo(res);
    if (!res.ok) throw new ApiError(mensajeDeError(datos, res.status), res.status);
    return datos;
  } finally {
    cambiarPendientes(-1);
  }
}

export const api = {
  tieneSesion: () => leerFlag(),
  marcarSesion: (activa) => escribirFlag(!!activa),

  login: (email, password) => solicitar(RUTAS.login, { metodo: "POST", cuerpo: { email, password } }),
  register: (nombre, email, password) =>
    solicitar(RUTAS.register, { metodo: "POST", cuerpo: { nombre, email, password } }),
  logout: () => solicitar(RUTAS.logout, { metodo: "POST" }),

  miUsuario: () => solicitar(RUTAS.miUsuario),
  misPrestamos: () => solicitar(RUTAS.misPrestamos),
  libros: () => solicitar(RUTAS.libros),
  solicitar: (libroId) =>
    solicitar(`${RUTAS.prestamos}?libroId=${encodeURIComponent(libroId)}`, { metodo: "POST" }),
  devolver: (prestamoId) => solicitar(RUTAS.devolver(prestamoId), { metodo: "PUT" }),
  crearLibro: ({ titulo, autor, isbn, genero }) =>
    solicitar(RUTAS.libros, { metodo: "POST", cuerpo: { titulo, autor, isbn, genero } }),
  eliminarLibro: (libroId) => solicitar(RUTAS.libro(libroId), { metodo: "DELETE" })
};
