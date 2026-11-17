/**
 * Capa de datos: unico modulo que usa fetch.
 * La sesion es un par de tokens (acceso JWT + refresh opaco) guardado en sessionStorage. El acceso se
 * envia en `Authorization: Bearer`; ante un 401 se intenta renovar con el refresh una sola vez.
 */

const CLAVE_ACCESO = "biblioteca_access_token";
const CLAVE_REFRESH = "biblioteca_refresh_token";

export const EVENTO_SESION_CADUCADA = "sesion:caducada";
export const EVENTO_CARGA = "api:carga";

const BASE_URL = "";
// Rutas publicas de auth: un 401 ahi son credenciales incorrectas, no un acceso caducado
const RUTAS_PUBLICAS = ["/api/auth/login", "/api/auth/register", "/api/auth/refresh"];

const RUTAS = {
  login: "/api/auth/login",
  register: "/api/auth/register",
  refresh: "/api/auth/refresh",
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

function leerToken(clave) {
  try { return sessionStorage.getItem(clave); } catch { return null; }
}

function guardarTokens(accessToken, refreshToken) {
  try {
    if (accessToken && refreshToken) {
      sessionStorage.setItem(CLAVE_ACCESO, accessToken);
      sessionStorage.setItem(CLAVE_REFRESH, refreshToken);
    } else {
      sessionStorage.removeItem(CLAVE_ACCESO);
      sessionStorage.removeItem(CLAVE_REFRESH);
    }
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

/** Una sola renovacion a la vez: las peticiones concurrentes que reciben 401 comparten la misma promesa. */
let renovacion = null;

async function renovarTokens() {
  const refreshToken = leerToken(CLAVE_REFRESH);
  if (!refreshToken) return false;
  if (!renovacion) {
    renovacion = (async () => {
      try {
        const res = await fetch(BASE_URL + RUTAS.refresh, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ refreshToken })
        });
        if (!res.ok) return false;
        const datos = await res.json();
        guardarTokens(datos.accessToken, datos.refreshToken);
        return true;
      } catch {
        return false;
      } finally {
        renovacion = null;
      }
    })();
  }
  return renovacion;
}

function opcionesDe(metodo, cuerpo) {
  const cabeceras = { "Content-Type": "application/json" };
  const acceso = leerToken(CLAVE_ACCESO);
  if (acceso) cabeceras.Authorization = `Bearer ${acceso}`;
  const opciones = { method: metodo, headers: cabeceras };
  // El cuerpo puede ser una funcion para leerlo de nuevo tras renovar los tokens
  const valor = typeof cuerpo === "function" ? cuerpo() : cuerpo;
  if (valor !== undefined) opciones.body = JSON.stringify(valor);
  return opciones;
}

async function enviar(ruta, metodo, cuerpo) {
  try {
    return await fetch(BASE_URL + ruta, opcionesDe(metodo, cuerpo));
  } catch {
    throw new ApiError("No se pudo conectar con el servidor. Compruebe su conexión.", 0);
  }
}

async function solicitar(ruta, { metodo = "GET", cuerpo } = {}) {
  cambiarPendientes(1);
  try {
    let res = await enviar(ruta, metodo, cuerpo);

    if (res.status === 401 && !RUTAS_PUBLICAS.includes(ruta)) {
      // Acceso caducado: se intenta renovar una vez y se repite la peticion
      if (await renovarTokens()) res = await enviar(ruta, metodo, cuerpo);
      if (res.status === 401) {
        guardarTokens(null, null);
        window.dispatchEvent(new CustomEvent(EVENTO_SESION_CADUCADA));
        throw new ApiError("Sesión caducada. Vuelva a firmar la ficha.", 401);
      }
    }

    const datos = await leerCuerpo(res);
    if (!res.ok) throw new ApiError(mensajeDeError(datos, res.status), res.status);
    return datos;
  } finally {
    cambiarPendientes(-1);
  }
}

export const api = {
  tieneSesion: () => !!leerToken(CLAVE_REFRESH),
  /** Con `false` borra los tokens guardados (el login ya los guarda). */
  marcarSesion: (activa) => { if (!activa) guardarTokens(null, null); },

  login: async (email, password) => {
    const datos = await solicitar(RUTAS.login, { metodo: "POST", cuerpo: { email, password } });
    guardarTokens(datos.accessToken, datos.refreshToken);
    return datos;
  },
  register: (nombre, email, password) =>
    solicitar(RUTAS.register, { metodo: "POST", cuerpo: { nombre, email, password } }),
  logout: () => solicitar(RUTAS.logout, { metodo: "POST", cuerpo: () => ({ refreshToken: leerToken(CLAVE_REFRESH) }) }),

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
