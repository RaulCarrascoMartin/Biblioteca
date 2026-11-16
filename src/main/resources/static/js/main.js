import { api, EVENTO_CARGA } from "./api.js";
import { subscribe, getState } from "./state.js";
import { registrarEventos } from "./events.js";
import { cargarBiblioteca } from "./acciones.js";
import { renderHeader } from "./views/header.js";
import { renderAuth } from "./views/auth.js";
import { renderShelf } from "./views/shelf.js";
import { renderCatalog } from "./views/catalog.js";
import { renderHistory } from "./views/history.js";
import { renderAdmin } from "./views/admin.js";

const render = (s) => {
  renderAuth(s);
  renderHeader(s);
  renderShelf(s);
  renderCatalog(s);
  renderHistory(s);
  renderAdmin(s);
};

subscribe(render);
registrarEventos();

/* Indicador global de peticiones en curso (sin repintar vistas). */
window.addEventListener(EVENTO_CARGA, (ev) => {
  document.body.setAttribute("aria-busy", String(ev.detail.pendientes > 0));
});

render(getState());
if (api.tieneSesion()) cargarBiblioteca();
