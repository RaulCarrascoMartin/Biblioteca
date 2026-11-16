import { $, esc, pintar, setHidden } from "../dom.js";
import { etiquetaGenero } from "../covers.js";
import { esAdmin } from "../state.js";

const fila = (l) => {
  const estado = l.disponible
    ? '<span class="status status--ok">EN SALA</span>'
    : '<span class="status status--lent">PRESTADO</span>';
  const retirar = l.disponible
    ? `<button type="button" class="btn btn--danger" data-retirar="${esc(l.id)}">Retirar</button>`
    : "";
  const genero = etiquetaGenero(l) || '<span class="genre-tag genre-tag--empty">&mdash;</span>';
  return (
    `<tr><td><b>${esc(l.titulo)}</b></td>` +
    `<td>${esc(l.autor || "")}</td>` +
    `<td>${genero}</td>` +
    `<td><i>${esc(l.isbn || "")}</i></td>` +
    `<td>${estado}</td>` +
    `<td>${retirar}</td></tr>`
  );
};

export const renderAdmin = (s) => {
  const admin = esAdmin(s);
  setHidden($("admin-section"), !admin);
  if (!admin || !s.cargado) return;
  pintar($("admin-books").querySelector("tbody"), s.libros.length
    ? s.libros.map(fila).join("")
    : '<tr><td colspan="6" class="state-msg state-msg--table-empty">El catálogo está vacío.</td></tr>');
};
