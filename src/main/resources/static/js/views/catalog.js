import { $, esc, pintar, setBusy } from "../dom.js";
import { cubierta, etiquetaGenero } from "../covers.js";
import { disponibles } from "../state.js";

const item = (l) =>
  '<div class="catalog-item">' +
    cubierta(l, "SALA") +
    `<div class="catalog-item__caption"><b class="catalog-item__title">${esc(l.title)}</b>` +
      `<span class="catalog-item__author">${esc(l.author || "")}</span>` +
      `<span class="catalog-item__genre">${etiquetaGenero(l)}</span></div>` +
    `<button type="button" class="catalog-item__action" data-solicitar="${esc(l.id)}">Solicitar</button>` +
  "</div>";

export const renderCatalog = (s) => {
  const cont = $("catalog");
  setBusy(cont, s.cargando);
  if (s.cargando && !s.cargado) {
    pintar(cont, '<div class="state-msg state-msg--loading">Ordenando el cat&aacute;logo&hellip;</div>');
    return;
  }
  if (!s.cargado) return;
  const libros = disponibles(s);
  pintar(cont, libros.length
    ? libros.map(item).join("")
    : '<div class="state-msg state-msg--grid-empty">No hay volúmenes disponibles en este momento.</div>');
};
