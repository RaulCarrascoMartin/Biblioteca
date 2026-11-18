/* Estado unico de la aplicacion + suscripcion simple + derivados (computed). */

const inicial = () => ({
  vista: "auth",        // "auth" | "library"
  tab: "login",         // "login" | "register"
  authMsg: null,        // { texto, error } | null
  enviando: false,      // formulario de acceso en curso
  usuario: null,
  prestamos: [],
  libros: [],
  cargado: false,       // ya se cargaron datos alguna vez
  cargando: false,
  error: null           // ultimo mensaje de error de carga
});

let state = inicial();
const oyentes = new Set();

export const getState = () => state;

export const setState = (parcial) => {
  state = { ...state, ...parcial };
  oyentes.forEach((fn) => fn(state));
};

export const subscribe = (fn) => {
  oyentes.add(fn);
  return () => oyentes.delete(fn);
};

/* Derivado memoizado: se recalcula solo si cambia el origen (por referencia). */
const derivado = (origen, calcular) => {
  let ultimo;
  let resultado;
  let calculado = false;
  return (s) => {
    const entrada = origen(s);
    if (!calculado || entrada !== ultimo) {
      ultimo = entrada;
      resultado = calcular(entrada);
      calculado = true;
    }
    return resultado;
  };
};

export const activos = derivado((s) => s.prestamos, (ps) => ps.filter((p) => p.returnDate === null));
export const devueltos = derivado((s) => s.prestamos, (ps) => ps.filter((p) => p.returnDate !== null));
export const disponibles = derivado((s) => s.libros, (ls) => ls.filter((l) => l.available));
export const esAdmin = (s) => !!s.usuario && s.usuario.rol === "ADMIN";
