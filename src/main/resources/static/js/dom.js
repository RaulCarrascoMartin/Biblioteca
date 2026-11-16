/* Helpers de DOM sin estado de negocio. */

export const $ = (id) => document.getElementById(id);

export const esc = (valor) =>
  String(valor ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");

export const setHidden = (el, oculto) => el.classList.toggle("is-hidden", !!oculto);

/* Pinta html en el contenedor solo si cambia (evita repintados inutiles). */
const ultimoHtml = new WeakMap();
export const pintar = (el, html) => {
  if (ultimoHtml.get(el) === html) return;
  ultimoHtml.set(el, html);
  el.innerHTML = html;
};

export const setBusy = (el, ocupado) => el.setAttribute("aria-busy", ocupado ? "true" : "false");

let toastTimer = null;
export const toast = (msg, esError = false) => {
  const el = $("toast");
  el.textContent = msg;
  el.classList.toggle("toast--error", esError);
  el.classList.remove("is-hidden");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => el.classList.add("is-hidden"), 3600);
};
