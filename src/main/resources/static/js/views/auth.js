import { $, setHidden } from "../dom.js";

/* Pinta el estado de la vista de acceso sobre el HTML estatico (no recrea inputs). */
export const renderAuth = (s) => {
  setHidden($("view-auth"), s.vista !== "auth");
  const esRegistro = s.tab === "register";

  for (const [id, activa] of [["tab-login", !esRegistro], ["tab-register", esRegistro]]) {
    const el = $(id);
    el.classList.toggle("ficha__tab--active", activa);
    el.setAttribute("aria-selected", String(activa));
    el.tabIndex = activa ? 0 : -1;
  }
  setHidden($("row-nombre"), !esRegistro);
  $("auth-submit").textContent = esRegistro ? "Dar de alta" : "Entrar";
  $("auth-submit").disabled = s.enviando;
  $("f-password").autocomplete = esRegistro ? "new-password" : "current-password";

  const sello = $("auth-msg");
  if (s.authMsg) {
    sello.textContent = s.authMsg.texto;
    sello.classList.toggle("stamp--ok", !s.authMsg.error);
    sello.classList.toggle("stamp--err", !!s.authMsg.error);
    setHidden(sello, false);
  } else {
    setHidden(sello, true);
  }
};
