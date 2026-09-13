(function () {
  "use strict";

  var api = window.api;
  var covers = window.covers;

  var $ = function (id) { return document.getElementById(id); };

  function esc(ctx) {
    return String(ctx).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
  }

  /* ---------------- Toast ---------------- */
  var toastTimer = null;
  function toast(msg, esError) {
    var el = $("toast");
    el.textContent = msg;
    el.classList.toggle("toast-err", !!esError);
    el.classList.remove("hidden");
    clearTimeout(toastTimer);
    toastTimer = setTimeout(function () { el.classList.add("hidden"); }, 3600);
  }

  /* ---------------- Vistas ---------------- */
  function mostrarVista(vista) {
    $("view-auth").classList.toggle("hidden", vista !== "auth");
    $("view-library").classList.toggle("hidden", vista !== "library");
    $("app-header").classList.toggle("hidden", vista !== "library");
  }

  function mostrarAuth(mensaje, esError) {
    mostrarVista("auth");
    var s = $("auth-msg");
    if (mensaje) {
      s.textContent = mensaje;
      s.classList.toggle("stamp-ok", !esError);
      s.classList.toggle("stamp-err", !!esError);
      s.classList.remove("hidden");
    } else {
      s.classList.add("hidden");
    }
  }

  function mostrarSello(mensaje, esError) {
    var s = $("auth-msg");
    s.textContent = mensaje;
    s.classList.toggle("stamp-ok", !esError);
    s.classList.toggle("stamp-err", !!esError);
    s.classList.remove("hidden");
  }

  function ocultarSello() {
    $("auth-msg").classList.add("hidden");
  }

  /* ---------------- Autenticación ---------------- */
  var esRegistro = false;

  function cambiarTab(tab) {
    esRegistro = tab === "register";
    $("tab-login").classList.toggle("active", !esRegistro);
    $("tab-register").classList.toggle("active", esRegistro);
    $("row-nombre").classList.toggle("hidden", !esRegistro);
    $("auth-submit").textContent = esRegistro ? "Dar de alta" : "Entrar";
    $("f-password").autocomplete = esRegistro ? "new-password" : "current-password";
    ocultarSello();
  }

  function pintarCabecera(usuario) {
    var area = $("user-area");
    area.innerHTML =
      '<span class="user-avatar">' + esc(covers.ficon(usuario.nombre || usuario.email)) + "</span>" +
      '<span class="user-meta">' +
        '<span class="user-name">' + esc(usuario.nombre || usuario.email) + "</span><br>" +
        '<span class="user-rol">' + esc(usuario.rol || "LECTOR") + " &middot; " + esc(usuario.email) + "</span>" +
      "</span>" +
      '<button type="button" class="btn-ghost" id="btn-logout">Cerrar sesi\u00f3n</button>';
    area.classList.remove("hidden");
    $("btn-logout").addEventListener("click", function () {
      api.setToken(null);
      location.reload();
    });
  }

  function autenticar(email, password) {
    $("auth-submit").disabled = true;
    ocultarSello();
    var paso = esRegistro
      ? api.register($("f-nombre").value.trim(), email, password)
          .then(function () { return api.login(email, password); }, function () { return api.login(email, password); })
      : api.login(email, password);

    paso.then(function (res) {
      api.setToken(res.token);
      cargarBiblioteca();
    }).catch(function (err) {
      mostrarSello(err.message, true);
      $("auth-submit").disabled = false;
    });
  }

  /* ---------------- Estanterías ---------------- */
  function chunk(arr, n) {
    var out = [];
    for (var i = 0; i < arr.length; i += n) out.push(arr.slice(i, i + n));
    return out;
  }

  function bookShelf(prestamo, indice) {
    var libro = prestamo.libro;
    var ribbonVisible = prestamo.fechaDevolucion === null;
    var ribbon = ribbonVisible
      ? '<span class="ribbon"><span class="ribbon-mites">P ' + covers.fecha(prestamo.fechaPrestamo) + "<br>D " +
        (prestamo.fechaDevolucion ? covers.fecha(prestamo.fechaDevolucion) : "&mdash;") + "</span></span>"
      : "";
    var fechaPlato = ribbonVisible
      ? "Prestado " + covers.fecha(prestamo.fechaPrestamo)
      : "Prestado " + covers.fecha(prestamo.fechaPrestamo) + " &mdash; devuelto " + covers.fecha(prestamo.fechaDevolucion);
    var firma = ribbonVisible
      ? '<button type="button" class="book-btn" data-devolver="' + prestamo.id + '">Devolver</button>'
      : "";
    return (
      '<div class="book ' + (indice % 2 === 0 ? "book-ladrillo" : "") + '">' +
        covers.cubierta(libro, "REG. " + (libro.isbn ? libro.isbn.slice(-4) : libro.id)) + ribbon +
        '<div class="book-plate">' +
          "<b>" + esc(libro.titulo) + "</b>" +
          esc(libro.autor || "") + "<br>" +
          "<i>" + fechaPlato + "</i>" +
        "</div>" + firma +
      "</div>"
    );
  }

  function estanteria(loans, labelInicial, vacioMsg) {
    if (!loans.length) {
      return '<div class="shelf-empty">' + esc(vacioMsg || "Este anaquel est\u00e1 vac\u00edo.") + "</div>";
    }
    var grupos = chunk(loans, 7);
    return grupos.map(function (grupo, g) {
      var libros = grupo.map(bookShelf).join("");
      return (
        '<div class="shelf">' +
          '<span class="shelf-label">' + esc(labelInicial) + (grupos.length > 1 ? " " + (g + 1) : "") + "</span>" +
          '<div class="shelf-rows">' + libros + '<span class="shelf-end"></span></div>' +
          '<div class="shelf-plank"></div>' +
          '<span class="shelf-brace left"></span>' +
          '<span class="shelf-brace right"></span>' +
        "</div>"
      );
    }).join("");
  }

  function pintarEstanteria(loans) {
    var activos = loans.filter(function (p) { return p.fechaDevolucion === null; });
    var devueltos = loans.filter(function (p) { return p.fechaDevolucion !== null; });
    $("my-shelf").innerHTML = estanteria(
      activos,
      "Anaquel del lector",
      "Este anaquel todav\u00eda est\u00e1 vac\u00edo. Solicite un volumen del cat\u00e1logo y lo encontrar\u00e1 aqu\u00ed."
    );

    var hist = $("history");
    if (!devueltos.length) {
      hist.innerHTML = '<li class="grid-empty" style="list-style:none">A\u00fan no hay libros devueltos.</li>';
    } else {
      hist.innerHTML = devueltos.map(function (p) {
        var l = p.libro;
        return (
          "<li>" +
            "<span><b>" + esc(l.titulo) + "</b> &mdash; " + esc(l.autor || "") + "</span>" +
            '<span class="fechas"><span>P.&nbsp;' + covers.fecha(p.fechaPrestamo) + "</span>" +
            "<span>D.&nbsp;" + covers.fecha(p.fechaDevolucion) + "</span></span>" +
          "</li>"
        );
      }).join("");
    }
  }

  function pintarCatalogo(libros) {
    var disponibles = libros.filter(function (l) { return l.disponible; });
    var cont = $("catalog");
    if (!disponibles.length) {
      cont.innerHTML = '<div class="grid-empty">No hay vol\u00famenes disponibles en este momento.</div>';
      return;
    }
    cont.innerHTML = disponibles.map(function (l) {
      return (
        '<div class="grid-book">' +
          covers.cubierta(l, "SALA") +
          '<div class="book-caption"><b>' + esc(l.titulo) + "</b><span>" + esc(l.autor || "") + "</span></div>" +
          '<button type="button" class="cat-btn" data-solicitar="' + l.id + '">Solicitar</button>' +
        "</div>"
      );
    }).join("");
  }

  /* ---------------- Gestión (admin) ---------------- */
  function pintarAdmin(usuario) {
    var seccion = $("admin-section");
    var esAdmin = usuario && usuario.rol === "ADMIN";
    seccion.classList.toggle("hidden", !esAdmin);
    if (!esAdmin) return;

    api.libros().then(function (libros) {
      var tbody = $("admin-books").querySelector("tbody");
      if (!libros.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="admin-empty">El cat\u00e1logo est\u00e1 vac\u00edo.</td></tr>';
        return;
      }
      tbody.innerHTML = libros.map(function (l) {
        var disponible = l.disponible;
        var estado = disponible
          ? '<span class="estado estado-ok">EN SALA</span>'
          : '<span class="estado estado-prestado">PRESTADO</span>';
        var btnDel = disponible
          ? '<button type="button" class="btn-del" data-retirar="' + l.id + '">Retirar</button>'
          : "";
        return (
          "<tr><td><b>" + esc(l.titulo) + "</b></td>" +
          "<td>" + esc(l.autor || "") + "</td>" +
          "<td><i>" + esc(l.isbn || "") + "</i></td>" +
          "<td>" + estado + "</td>" +
          "<td>" + btnDel + "</td></tr>"
        );
      }).join("");
      enlazarRetirar();
    }).catch(function (err) {
      toast(err.message, true);
    });
  }

  function enlazarRetirar() {
    document.querySelectorAll("[data-retirar]").forEach(function (btn) {
      btn.addEventListener("click", function () {
        var id = btn.getAttribute("data-retirar");
        btn.disabled = true;
        api.eliminarLibro(id)
          .then(function () {
            toast("Volumen retirado del cat\u00e1logo.");
            cargarBiblioteca();
          })
          .catch(function (err) {
            btn.disabled = false;
            toast(err.message, true);
          });
      });
    });
  }

  /* ---------------- Datos / carga ---------------- */
  function cargarBiblioteca() {
    if (!api.tieneSesion()) { mostrarAuth(null); return; }
    mostrarVista("library");

    Promise.all([api.miUsuario(), api.misPrestamos(), api.libros()])
      .then(function (resultados) {
        pintarCabecera(resultados[0]);
        pintarEstanteria(resultados[1]);
        pintarCatalogo(resultados[2]);
        pintarAdmin(resultados[0]);
        vincularAcciones();
      })
      .catch(function (err) {
        if (err.message && err.message.indexOf("caducada") !== -1) {
          mostrarAuth("La sesi\u00f3n caduc\u00f3. Firme la ficha de nuevo.", true);
        } else {
          toast(err.message, true);
        }
      });
  }

  function vincularAcciones() {
    document.querySelectorAll("[data-solicitar]").forEach(function (btn) {
      btn.addEventListener("click", function () {
        var id = btn.getAttribute("data-solicitar");
        btn.disabled = true;
        api.solicitar(id)
          .then(function (p) {
            toast("El libro \u00ab" + p.libro.titulo + "\u00bb pasa a su estanter\u00eda.");
            cargarBiblioteca();
          })
          .catch(function (err) {
            btn.disabled = false;
            toast(err.message, true);
          });
      });
    });

    document.querySelectorAll("[data-devolver]").forEach(function (btn) {
      btn.addEventListener("click", function () {
        var id = btn.getAttribute("data-devolver");
        btn.disabled = true;
        api.devolver(id)
          .then(function (p) {
            toast("Se devuelve \u00ab" + p.libro.titulo + "\u00bb a la sala.");
            cargarBiblioteca();
          })
          .catch(function (err) {
            btn.disabled = false;
            toast(err.message, true);
          });
      });
    });
  }

  /* ---------------- Inicialización ---------------- */
  function init() {
    [].forEach.call(document.querySelectorAll(".card-tab"), function (tab) {
      tab.addEventListener("click", function () { cambiarTab(tab.getAttribute("data-tab")); });
    });

    $("auth-form").addEventListener("submit", function (ev) {
      ev.preventDefault();
      var email = $("f-email").value.trim();
      var password = $("f-password").value;
      if (!email || !email.indexOf("@")) { mostrarSello("Indique un correo v\u00e1lido.", true); return; }
      if (esRegistro && !$("f-nombre").value.trim()) { mostrarSello("Indique su nombre para el alta.", true); return; }
      if (password.length < 8) { mostrarSello("La contrase\u00f1a requiere 8 caracteres como m\u00ednimo.", true); return; }
      autenticar(email, password);
    });

    $("book-add-form").addEventListener("submit", function (ev) {
      ev.preventDefault();
      var titulo = $("b-titulo").value.trim();
      var autor = $("b-autor").value.trim();
      var isbn = $("b-isbn").value.trim();
      var btn = $("b-submit");
      if (!titulo || !autor || !isbn) { toast("Complete t\u00edtulo, autor e ISBN.", true); return; }
      btn.disabled = true;
      api.crearLibro({ titulo: titulo, autor: autor, isbn: isbn })
        .then(function (nuevo) {
          toast("\u00ab" + nuevo.titulo + "\u00bb a\u00f1adido al cat\u00e1logo.");
          ev.target.reset();
          cargarBiblioteca();
        })
        .catch(function (err) {
          btn.disabled = false;
          toast(err.message, true);
        });
    });

    window.addEventListener("sesion:caducada", function () {
      mostrarAuth("La sesi\u00f3n ha caducado.", true);
    });

    cambiarTab("login");

    if (api.tieneSesion()) cargarBiblioteca();
    else mostrarVista("auth");
  }

  document.addEventListener("DOMContentLoaded", init);
})();