(function () {
  "use strict";

  var TOKEN_KEY = "biblioteca_token";

  var api = {
    setToken: function (token) {
      if (token) localStorage.setItem(TOKEN_KEY, token);
      else localStorage.removeItem(TOKEN_KEY);
    },
    getToken: function () {
      return localStorage.getItem(TOKEN_KEY);
    },
    tieneSesion: function () {
      return !!api.getToken();
    }
  };

  function jsonBody(payload) {
    return {
      "Content-Type": "application/json"
    };
  }

  function pedir(path, method, payload) {
    var headers = { "Content-Type": "application/json" };
    var token = api.getToken();
    if (token) headers["Authorization"] = "Bearer " + token;

    var opts = { method: method || "GET", headers: headers };
    if (payload !== undefined) opts.body = JSON.stringify(payload);

    return fetch(path, opts).then(function (res) {
      if (res.status === 401) {
        api.setToken(null);
        window.dispatchEvent(new CustomEvent("sesion:caducada"));
        throw new Error("Sesi\u00f3n caducada. Vuelva a firmar la ficha.");
      }
      var ct = res.headers.get("content-type") || "";
      var bodyPromise = ct.indexOf("application/json") !== -1 ? res.json().catch(function () { return null; }) : res.text().catch(function () { return null; });

      return bodyPromise.then(function (body) {
        if (!res.ok) {
          var msg = body && typeof body === "object" && body.error ? body.error
            : (body && typeof body === "string" ? body : null);
          throw new Error(msg || "La solicitud fall\u00f3 (" + res.status + ")");
        }
        return body;
      });
    });
  }

  api.login = function (email, password) {
    return pedir("/api/auth/login", "POST", { email: email, password: password });
  };

  api.register = function (nombre, email, password) {
    return pedir("/api/auth/register", "POST", { nombre: nombre, email: email, password: password });
  };

  api.miUsuario = function () {
    return pedir("/api/usuarios/me");
  };

  api.misPrestamos = function () {
    return pedir("/api/prestamos/mios");
  };

  api.libros = function () {
    return pedir("/api/libros");
  };

  api.solicitar = function (libroId) {
    return pedir("/api/prestamos?libroId=" + encodeURIComponent(libroId), "POST");
  };

  api.devolver = function (prestamoId) {
    return pedir("/api/prestamos/" + encodeURIComponent(prestamoId) + "/devolver", "PUT");
  };

  api.crearLibro = function (dto) {
    return pedir("/api/libros", "POST", dto);
  };

  api.eliminarLibro = function (libroId) {
    return pedir("/api/libros/" + encodeURIComponent(libroId), "DELETE");
  };

  window.api = api;
})();