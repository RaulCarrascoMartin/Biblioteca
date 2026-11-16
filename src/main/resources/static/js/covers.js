import { esc } from "./dom.js";

const TAPAS = [
  ["#6f3a34", "#5a2d29"], /* burdeos */
  ["#48604a", "#3a4f3d"], /* verde musgo */
  ["#3c5871", "#334a60"], /* azul apagado */
  ["#c9973f", "#a97a29"], /* ocre */
  ["#eab08f", "#d68a62"], /* melocoton pastel */
  ["#d07a47", "#b25a2c"], /* terracota */
  ["#596069", "#4b5159"], /* pizarra */
  ["#d3a648", "#b98a33"]  /* mostaza */
];

const GENEROS = {
  CLASICA:         { label: "Clásica",         tapa: 0 },
  FANTASIA:        { label: "Fantasía",        tapa: 1 },
  CIENCIA_FICCION: { label: "Ciencia ficción", tapa: 2 },
  HISTORICA:       { label: "Histórica",       tapa: 3 },
  ROMANTICA:       { label: "Romántica",       tapa: 4 },
  AVENTURAS:       { label: "Aventuras",            tapa: 5 },
  MISTERIO:        { label: "Misterio",             tapa: 6 },
  POESIA:          { label: "Poesía",          tapa: 7 }
};

const hash = (s) => {
  let h = 5381;
  for (let i = 0; i < s.length; i++) h = ((h << 5) + h + s.charCodeAt(i)) | 0;
  return Math.abs(h);
};

export const genero = (g) => (g && GENEROS[g]) || null;

export const tapaPara = (libro) => {
  const def = genero(libro && libro.genero);
  if (def) return TAPAS[def.tapa];
  let semilla = (libro.isbn || "") + "|" + (libro.titulo || "");
  if (!semilla) semilla = String(libro.id || 0);
  return TAPAS[hash(semilla) % TAPAS.length];
};

/* HTML de una cubierta (lomo + texto vertical). Sin numero se omite la signatura. */
export const cubierta = (libro, numero) => {
  const tapa = tapaPara(libro);
  const numeroHtml = numero ? `<span class="cover__num">${esc(numero)}</span>` : "";
  return (
    `<div class="cover" style="--tapa:${tapa[0]}" title="${esc(libro.titulo)}">` +
      '<span class="cover__line"></span>' +
      '<span class="cover__spine">' +
        `<span class="cover__title">${esc(libro.titulo)}</span>${numeroHtml}` +
      "</span>" +
    "</div>"
  );
};

/* Etiqueta de genero; cadena vacia si el libro no tiene genero conocido. */
export const etiquetaGenero = (libro) => {
  const g = genero(libro && libro.genero);
  if (!g) return "";
  return `<span class="genre-tag" style="--gen:${tapaPara(libro)[0]}"><i class="genre-tag__dot"></i>${g.label}</span>`;
};

export const ficon = (s) => String(s).charAt(0).toUpperCase();

export const fecha = (iso) =>
  new Date(iso + "T00:00:00").toLocaleDateString("es-ES", { day: "2-digit", month: "short", year: "2-digit" });
