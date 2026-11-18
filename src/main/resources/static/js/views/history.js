import { $, esc, pintar } from "../dom.js";
import { fecha } from "../covers.js";
import { devueltos } from "../state.js";

const pad4 = (n) => String(n).padStart(4, "0");

const linea = (p) => {
  const l = p.book;
  const artNum = l.isbn ? l.isbn.slice(-4) : l.id;
  return (
    '<li class="ticket__line">' +
      '<div class="ticket__info">' +
        `<b>${esc(l.title)}</b><span>${esc(l.author || "")}</span>` +
      "</div>" +
      '<div class="ticket__dates">' +
        `<span class="ticket__art">ART.&nbsp;${esc(artNum)}</span>` +
        `<span>P.&nbsp;${fecha(p.loanDate)}</span>` +
        `<span>D.&nbsp;${fecha(p.returnDate)}</span>` +
      "</div>" +
    "</li>"
  );
};

export const renderHistory = (s) => {
  if (!s.cargado) return;
  const lista = devueltos(s);
  pintar($("history"), lista.length
    ? lista.map(linea).join("")
    : '<li class="ticket__line ticket__line--empty">Sin devoluciones &mdash; este comprobante queda en blanco.</li>');
  $("ticket-total").textContent = lista.length;
  $("ticket-no").textContent = pad4(lista.length + 1);
  $("ticket-fecha").textContent = new Date().toLocaleDateString("es-ES", {
    day: "2-digit", month: "short", year: "numeric"
  });
};
