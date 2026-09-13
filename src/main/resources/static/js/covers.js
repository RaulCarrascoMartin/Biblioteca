(function () {
  "use strict";

  var TAPAS = [
    ["#6f3a34", "#5a2d29"], /* burdeos */
    ["#48604a", "#3a4f3d"], /* verde musgo */
    ["#3c5871", "#334a60"], /* azul apagado */
    ["#9a6f34", "#825c2a"], /* ocre */
    ["#5c4a68", "#4c3d57"], /* violeta */
    ["#9c5a44", "#834a37"], /* terracota */
    ["#596069", "#4b5159"], /* pizarra */
    ["#a8873d", "#8f7231"]  /* mostaza */
  ];

  function hash(s) {
    var h = 5381;
    for (var i = 0; i < s.length; i++) h = ((h << 5) + h + s.charCodeAt(i)) | 0;
    return Math.abs(h);
  }

  function tapaPara(libro) {
    var semilla = (libro.isbn || "") + "|" + (libro.titulo || "");
    if (!semilla) semilla = String(libro.id || 0);
    return TAPAS[hash(semilla) % TAPAS.length];
  }

  function esc(ctx) {
    return String(ctx).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
  }

  /*
   * Devuelve el HTML de una cubierta de libro (lomo + texto vertical).
   * Si numero no viene, se omite la signatura.
   */
  function cubierta(libro, numero) {
    var tapa = tapaPara(libro);
    var numeroHtml = numero
      ? "<span class=\"spine-num\">" + esc(numero) + "</span>"
      : "";
    return (
      '<div class="book-cover" style="--tapa:' + tapa[0] + '" title="' + esc(libro.titulo) + '">' +
        '<span class="tapa-linea"></span>' +
        '<span class="spine-texto">' +
          '<span class="spine-titulo">' + esc(libro.titulo) + "</span>" +
          numeroHtml +
        "</span>" +
      "</div>"
    );
  }

  function ficon(s) {
    return s.charAt(0).toUpperCase();
  }

  function fecha(iso) {
    var d = new Date(iso + "T00:00:00");
    return d.toLocaleDateString("es-ES", { day: "2-digit", month: "short", year: "2-digit" });
  }

  window.covers = { tapaPara: tapaPara, cubierta: cubierta, ficon: ficon, fecha: fecha };
})();