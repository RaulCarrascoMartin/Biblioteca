import { $, esc, pintar, setBusy, setHidden } from "../dom.js";
import { cubierta, etiquetaGenero, fecha } from "../covers.js";
import { activos } from "../state.js";

const POR_ANAQUEL = 7;

const chunk = (arr, n) => {
  const out = [];
  for (let i = 0; i < arr.length; i += n) out.push(arr.slice(i, i + n));
  return out;
};

const libroEnAnaquel = (prestamo, indice) => {
  const libro = prestamo.libro;
  const ribbon =
    '<span class="book__ribbon"><span class="book__ribbon-text">P ' + fecha(prestamo.fechaPrestamo) +
    "<br>D &mdash;</span></span>";
  return (
    `<div class="book${indice % 2 === 0 ? " book--ladrillo" : ""}">` +
      cubierta(libro, "REG. " + (libro.isbn ? libro.isbn.slice(-4) : libro.id)) + ribbon +
      '<div class="book__plate">' +
        `<b class="book__plate-title">${esc(libro.titulo)}</b>` +
        esc(libro.autor || "") + "<br>" +
        etiquetaGenero(libro) +
        `<i class="book__plate-dates">Prestado ${fecha(prestamo.fechaPrestamo)}</i>` +
      "</div>" +
      `<button type="button" class="book__action" data-devolver="${esc(prestamo.id)}">Devolver</button>` +
    "</div>"
  );
};

const estanteria = (prestamos, etiqueta, vacioMsg) => {
  if (!prestamos.length) return `<div class="state-msg state-msg--shelf-empty">${esc(vacioMsg)}</div>`;
  const grupos = chunk(prestamos, POR_ANAQUEL);
  return grupos.map((grupo, g) =>
    '<div class="shelf">' +
      `<span class="shelf__label">${esc(etiqueta)}${grupos.length > 1 ? " " + (g + 1) : ""}</span>` +
      `<div class="shelf__rows">${grupo.map((p, i) => libroEnAnaquel(p, i)).join("")}<span class="shelf__end"></span></div>` +
      '<div class="shelf__plank"></div>' +
      '<span class="shelf__brace shelf__brace--left"></span>' +
      '<span class="shelf__brace shelf__brace--right"></span>' +
    "</div>"
  ).join("");
};

export const renderShelf = (s) => {
  setHidden($("view-library"), s.vista !== "library");
  const cont = $("my-shelf");
  setBusy(cont, s.cargando);
  if (s.cargando && !s.cargado) {
    pintar(cont, '<div class="state-msg state-msg--loading">Abriendo anaqueles&hellip;</div>');
    return;
  }
  if (!s.cargado) return;
  pintar(cont, estanteria(
    activos(s),
    "Anaquel del lector",
    "Este anaquel todavía está vacío. Solicite un volumen del catálogo y lo encontrará aquí."
  ));
};
