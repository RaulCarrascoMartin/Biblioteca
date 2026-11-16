/* Eventos: delegacion en document; se registran una sola vez. */
import { EVENTO_SESION_CADUCADA } from "./api.js";
import { $ } from "./dom.js";
import * as acciones from "./acciones.js";

export const registrarEventos = () => {
  document.addEventListener("click", (ev) => {
    const el = ev.target.closest("[data-tab], [data-solicitar], [data-devolver], [data-retirar], #btn-logout");
    if (!el) return;
    if (el.id === "btn-logout") acciones.logout();
    else if (el.hasAttribute("data-tab")) acciones.cambiarTab(el.dataset.tab);
    else if (el.hasAttribute("data-solicitar")) acciones.solicitar(el, el.dataset.solicitar);
    else if (el.hasAttribute("data-devolver")) acciones.devolver(el, el.dataset.devolver);
    else if (el.hasAttribute("data-retirar")) acciones.retirar(el, el.dataset.retirar);
  });

  document.addEventListener("submit", (ev) => {
    const form = ev.target;
    if (form.id === "auth-form") {
      ev.preventDefault();
      acciones.enviarAuth({
        nombre: $("f-nombre").value.trim(),
        email: $("f-email").value.trim(),
        password: $("f-password").value
      });
    } else if (form.id === "book-add-form") {
      ev.preventDefault();
      acciones.crearLibro(form, {
        titulo: $("b-titulo").value.trim(),
        autor: $("b-autor").value.trim(),
        isbn: $("b-isbn").value.trim(),
        genero: $("b-genero").value || null
      }, $("b-submit"));
    }
  });

  window.addEventListener(EVENTO_SESION_CADUCADA, acciones.sesionCaducada);
};
