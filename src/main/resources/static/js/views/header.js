import { $, esc, pintar, setHidden } from "../dom.js";
import { ficon } from "../covers.js";

export const renderHeader = (s) => {
  setHidden($("app-header"), s.vista !== "library");
  const area = $("user-area");
  const u = s.usuario;
  setHidden(area, !u);
  if (!u) { pintar(area, ""); return; }
  const nombre = u.name || u.email;
  pintar(
    area,
    `<span class="user__avatar">${esc(ficon(nombre))}</span>` +
    '<span class="user__meta">' +
      `<span class="user__name">${esc(nombre)}</span><br>` +
      `<span class="user__role">${esc(u.role || "LECTOR")} &middot; ${esc(u.email)}</span>` +
    "</span>" +
    '<button type="button" class="btn btn--ghost" id="btn-logout">Cerrar sesión</button>'
  );
};
