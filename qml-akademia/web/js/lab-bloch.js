/* Laboratorium: lista narzędzi, wspólne funkcje rysowania, interaktywna sfera Blocha. */
(function () {
  'use strict';
  var A = App, Q = QSim, esc = MD.esc;

  // ---------- Wspólne narzędzia ----------
  function colors() {
    var cs = getComputedStyle(document.documentElement);
    function v(n) { return cs.getPropertyValue(n).trim(); }
    return {
      bg: v('--bg'), card: v('--card'), card2: v('--card2'), text: v('--text'), muted: v('--muted'), line: v('--line'),
      accent: v('--accent'), accent2: v('--accent2'), ok: v('--ok'), bad: v('--bad'), warn: v('--warn'),
      light: document.documentElement.getAttribute('data-theme') === 'light'
    };
  }

  function setupCanvas(canvas, w, h) {
    var dpr = Math.min(3, window.devicePixelRatio || 1);
    canvas.width = Math.round(w * dpr);
    canvas.height = Math.round(h * dpr);
    canvas.style.height = h + 'px';
    var ctx = canvas.getContext('2d');
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    return ctx;
  }

  // Rzut punktu sfery na ekran. view = {yaw, el}
  function project(p, view, cx, cy, R) {
    var ca = Math.cos(view.yaw), sa = Math.sin(view.yaw);
    var x1 = p[0] * ca - p[1] * sa, y1 = p[0] * sa + p[1] * ca, z1 = p[2];
    var ce = Math.cos(view.el), se = Math.sin(view.el);
    var up = y1 * se + z1 * ce;
    var depth = -y1 * ce + z1 * se;
    return { x: cx + R * x1, y: cy - R * up, d: depth };
  }

  function rotate(v, n, t) {
    // wzór Rodriguesa: obrót wektora v o kąt t wokół osi n (jednostkowej)
    var c = Math.cos(t), s = Math.sin(t);
    var dot = n[0] * v[0] + n[1] * v[1] + n[2] * v[2];
    var cr = [n[1] * v[2] - n[2] * v[1], n[2] * v[0] - n[0] * v[2], n[0] * v[1] - n[1] * v[0]];
    return [0, 1, 2].map(function (i) { return v[i] * c + cr[i] * s + n[i] * dot * (1 - c); });
  }

  function drawBloch(ctx, size, vec, opts) {
    opts = opts || {};
    var col = colors();
    var view = opts.view || { yaw: -1.1, el: 0.32 };
    var cx = size / 2, cy = size / 2, R = size * (opts.labels === false ? 0.42 : 0.36);
    ctx.clearRect(0, 0, size, size);

    // kula
    var g = ctx.createRadialGradient(cx - R * 0.35, cy - R * 0.4, R * 0.1, cx, cy, R);
    g.addColorStop(0, col.light ? 'rgba(102,68,238,0.10)' : 'rgba(124,92,255,0.22)');
    g.addColorStop(1, col.light ? 'rgba(102,68,238,0.02)' : 'rgba(77,225,255,0.04)');
    ctx.fillStyle = g;
    ctx.beginPath(); ctx.arc(cx, cy, R, 0, Math.PI * 2); ctx.fill();
    ctx.strokeStyle = col.muted; ctx.globalAlpha = 0.6; ctx.lineWidth = 1.2;
    ctx.stroke(); ctx.globalAlpha = 1;

    // okręgi: równik i dwa południki
    function circle(fn) {
      var N = 72, prev = null;
      for (var i = 0; i <= N; i++) {
        var a = 2 * Math.PI * i / N;
        var p = project(fn(a), view, cx, cy, R);
        if (prev) {
          ctx.beginPath();
          ctx.globalAlpha = p.d >= 0 ? 0.55 : 0.18;
          ctx.moveTo(prev.x, prev.y); ctx.lineTo(p.x, p.y); ctx.stroke();
        }
        prev = p;
      }
      ctx.globalAlpha = 1;
    }
    ctx.strokeStyle = col.muted; ctx.lineWidth = 1;
    circle(function (a) { return [Math.cos(a), Math.sin(a), 0]; });
    circle(function (a) { return [Math.cos(a), 0, Math.sin(a)]; });
    circle(function (a) { return [0, Math.cos(a), Math.sin(a)]; });

    // osie
    var axes = [
      [[0, 0, 1], '|0⟩'], [[0, 0, -1], '|1⟩'], [[1, 0, 0], '|+⟩'], [[-1, 0, 0], '|−⟩'], [[0, 1, 0], '|+i⟩'], [[0, -1, 0], '|−i⟩']
    ];
    var c0 = project([0, 0, 0], view, cx, cy, R);
    axes.forEach(function (ax) {
      var p = project(ax[0], view, cx, cy, R);
      ctx.strokeStyle = col.muted; ctx.globalAlpha = p.d >= -0.05 ? 0.7 : 0.3; ctx.lineWidth = 1;
      ctx.setLineDash([3, 3]);
      ctx.beginPath(); ctx.moveTo(c0.x, c0.y); ctx.lineTo(p.x, p.y); ctx.stroke();
      ctx.setLineDash([]);
      if (opts.labels !== false) {
        var q = project(ax[0].map(function (x) { return x * 1.2; }), view, cx, cy, R);
        ctx.globalAlpha = p.d >= -0.05 ? 1 : 0.5;
        ctx.fillStyle = ax[1] === '|0⟩' || ax[1] === '|1⟩' ? col.text : col.muted;
        ctx.font = (size > 200 ? 14 : 10) + 'px system-ui, sans-serif';
        ctx.textAlign = 'center'; ctx.textBaseline = 'middle';
        ctx.fillText(ax[1], q.x, q.y);
      }
      ctx.globalAlpha = 1;
    });

    // ślad animacji
    if (opts.trail && opts.trail.length > 1) {
      ctx.strokeStyle = col.warn; ctx.lineWidth = 2;
      for (var k = 1; k < opts.trail.length; k++) {
        var a1 = project(opts.trail[k - 1], view, cx, cy, R), a2 = project(opts.trail[k], view, cx, cy, R);
        ctx.globalAlpha = 0.15 + 0.6 * k / opts.trail.length;
        ctx.beginPath(); ctx.moveTo(a1.x, a1.y); ctx.lineTo(a2.x, a2.y); ctx.stroke();
      }
      ctx.globalAlpha = 1;
    }

    if (!vec) return;
    var len = Math.sqrt(vec[0] * vec[0] + vec[1] * vec[1] + vec[2] * vec[2]);
    var tip = project(vec, view, cx, cy, R);
    // rzut na płaszczyznę równika
    if (opts.labels !== false && len > 0.05) {
      var fl = project([vec[0], vec[1], 0], view, cx, cy, R);
      ctx.strokeStyle = col.warn; ctx.globalAlpha = 0.45; ctx.setLineDash([2, 4]);
      ctx.beginPath(); ctx.moveTo(tip.x, tip.y); ctx.lineTo(fl.x, fl.y); ctx.lineTo(c0.x, c0.y); ctx.stroke();
      ctx.setLineDash([]); ctx.globalAlpha = 1;
    }
    ctx.strokeStyle = col.warn; ctx.lineWidth = size > 200 ? 3.5 : 2.5; ctx.lineCap = 'round';
    ctx.beginPath(); ctx.moveTo(c0.x, c0.y); ctx.lineTo(tip.x, tip.y); ctx.stroke();
    var rg = ctx.createRadialGradient(tip.x, tip.y, 0, tip.x, tip.y, size > 200 ? 14 : 8);
    rg.addColorStop(0, '#fff'); rg.addColorStop(0.4, col.warn); rg.addColorStop(1, 'rgba(255,176,46,0)');
    ctx.fillStyle = rg;
    ctx.beginPath(); ctx.arc(tip.x, tip.y, size > 200 ? 14 : 8, 0, Math.PI * 2); ctx.fill();
    if (opts.showLen && len < 0.98) {
      ctx.fillStyle = col.muted; ctx.font = '10px system-ui'; ctx.textAlign = 'center';
      ctx.fillText('|r|=' + len.toFixed(2), cx, size - 6);
    }
  }

  function dragView(canvas, view, redraw) {
    var start = null;
    function down(e) { var p = e.touches ? e.touches[0] : e; start = { x: p.clientX, y: p.clientY, yaw: view.yaw, el: view.el }; }
    function move(e) {
      if (!start) return;
      var p = e.touches ? e.touches[0] : e;
      view.yaw = start.yaw - (p.clientX - start.x) * 0.012;
      view.el = Math.max(-1.3, Math.min(1.3, start.el + (p.clientY - start.y) * 0.012));
      redraw();
      if (e.cancelable) e.preventDefault();
    }
    function up() { start = null; }
    canvas.addEventListener('mousedown', down);
    canvas.addEventListener('touchstart', down, { passive: true });
    window.addEventListener('mousemove', move);
    canvas.addEventListener('touchmove', move, { passive: false });
    window.addEventListener('mouseup', up);
    canvas.addEventListener('touchend', up);
    return function () { window.removeEventListener('mousemove', move); window.removeEventListener('mouseup', up); };
  }

  // Wygładzona mapa ciepła: values[j*G+i] (j - wiersz od góry), colorFn(v) -> [r,g,b,a(0..1)]
  var heatCanvas = null;
  function heatmap(ctx, size, G, values, colorFn) {
    if (!heatCanvas) heatCanvas = document.createElement('canvas');
    heatCanvas.width = G; heatCanvas.height = G;
    var hctx = heatCanvas.getContext('2d');
    var img = hctx.createImageData(G, G);
    for (var k = 0; k < G * G; k++) {
      var c = colorFn(values[k]);
      img.data[4 * k] = c[0]; img.data[4 * k + 1] = c[1]; img.data[4 * k + 2] = c[2]; img.data[4 * k + 3] = Math.round(255 * c[3]);
    }
    hctx.putImageData(img, 0, 0);
    ctx.save();
    ctx.imageSmoothingEnabled = true;
    ctx.imageSmoothingQuality = 'high';
    // środki komórek siatki leżą w (i+0.5)*cell, więc rysujemy obraz G×G na pełnym kwadracie
    ctx.drawImage(heatCanvas, 0, 0, G, G, 0, 0, size, size);
    ctx.restore();
  }

  // Kontur (marching squares) poziomu `level` dla siatki wartości w środkach komórek
  function contour(ctx, size, G, values, level, style, width) {
    var cell = size / G;
    function v(i, j) { return values[j * G + i] - level; }
    function P(i, j) { return [(i + 0.5) * cell, (j + 0.5) * cell]; }
    function lerp(a, b, va, vb) { var t = va / (va - vb); return [a[0] + t * (b[0] - a[0]), a[1] + t * (b[1] - a[1])]; }
    ctx.save();
    ctx.strokeStyle = style; ctx.lineWidth = width || 2; ctx.lineCap = 'round';
    ctx.beginPath();
    for (var j = 0; j < G - 1; j++) for (var i = 0; i < G - 1; i++) {
      var a = v(i, j), b = v(i + 1, j), c = v(i + 1, j + 1), d = v(i, j + 1);
      var pts = [];
      if ((a > 0) !== (b > 0)) pts.push(lerp(P(i, j), P(i + 1, j), a, b));
      if ((b > 0) !== (c > 0)) pts.push(lerp(P(i + 1, j), P(i + 1, j + 1), b, c));
      if ((c > 0) !== (d > 0)) pts.push(lerp(P(i + 1, j + 1), P(i, j + 1), c, d));
      if ((d > 0) !== (a > 0)) pts.push(lerp(P(i, j + 1), P(i, j), d, a));
      for (var k = 0; k + 1 < pts.length; k += 2) {
        ctx.moveTo(pts[k][0], pts[k][1]); ctx.lineTo(pts[k + 1][0], pts[k + 1][1]);
      }
    }
    ctx.stroke();
    ctx.restore();
  }

  window.Lab = {
    colors: colors, setupCanvas: setupCanvas, drawBloch: drawBloch, project: project, rotate: rotate,
    dragView: dragView, heatmap: heatmap, contour: contour
  };

  // ---------- Lista narzędzi ----------
  A.route(/^\/lab$/, function (el) {
    A.header({ title: 'Laboratorium', tab: 'lab', parent: '#/' });
    var tools = [
      ['bloch', '🌐', 'Sfera Blocha', 'Nakładaj bramki na pojedynczy kubit i obserwuj obroty stanu.'],
      ['circuit', '🔌', 'Symulator obwodów', 'Do 4 kubitów: H, CNOT, rotacje… Splątanie, stany Bella, Grover, teleportacja.'],
      ['train', '🤖', 'Trening: VQC vs klasyczne ML', 'Wariacyjny klasyfikator kwantowy (data re‑uploading) kontra regresja logistyczna i MLP.'],
      ['kernel', '🧩', 'Kernele kwantowe', 'Macierz Grama, szerokość pasma i klasyfikator jądrowy z kwantową mapą cech.'],
      ['shift', '📈', 'Reguła parameter‑shift', 'Dokładny gradient obwodu z dwóch pomiarów i wpływ szumu shotów.']
    ];
    var html = '<p class="muted intro">Interaktywne symulacje działają lokalnie na telefonie — bez internetu.</p><div class="lab-grid">';
    tools.forEach(function (t) {
      html += '<a class="card lab-card" href="#/lab/' + t[0] + '"><span class="lab-ico">' + t[1] + '</span><span class="grow"><b>' + t[2] + '</b><p>' + t[3] + '</p></span></a>';
    });
    el.innerHTML = html + '</div>';
  });

  // ---------- Sfera Blocha ----------
  var GATE_ROT = {
    X: [[1, 0, 0], Math.PI], Y: [[0, 1, 0], Math.PI], Z: [[0, 0, 1], Math.PI],
    H: [[Math.SQRT1_2, 0, Math.SQRT1_2], Math.PI],
    S: [[0, 0, 1], Math.PI / 2], SDG: [[0, 0, 1], -Math.PI / 2],
    T: [[0, 0, 1], Math.PI / 4], TDG: [[0, 0, 1], -Math.PI / 4],
    SX: [[1, 0, 0], Math.PI / 2]
  };
  var LABEL = { SDG: 'S†', TDG: 'T†', SX: '√X', RX: 'Rx', RY: 'Ry', RZ: 'Rz' };

  A.route(/^\/lab\/bloch$/, function (el) {
    A.header({ title: 'Sfera Blocha', tab: 'lab', parent: '#/lab' });
    var state = new Q.State(1);
    var view = { yaw: -1.1, el: 0.32 };
    var vec = [0, 0, 1];
    var trail = [];
    var hist = [];
    var anim = null;
    var theta = Math.PI / 2;

    el.innerHTML =
      '<div class="card"><div class="canvas-wrap"><canvas id="bc"></canvas></div>' +
      '<p class="muted small center">Przeciągnij palcem, aby obrócić widok</p>' +
      '<div class="history" id="hist"></div></div>' +
      '<div class="card"><div class="field-label">Bramki</div><div class="gate-pad">' +
      ['X', 'Y', 'Z', 'H', 'S', 'SDG', 'T', 'TDG'].map(function (g) { return '<button class="gate" data-g="' + g + '">' + (LABEL[g] || g) + '</button>'; }).join('') +
      '</div><div class="field-label">Rotacje o kąt θ</div>' +
      '<div class="slider-row"><input type="range" id="th" min="-180" max="180" step="15" value="90"><output id="tho">π/2</output></div>' +
      '<div class="gate-pad"><button class="gate" data-g="RX">Rx(θ)</button><button class="gate" data-g="RY">Ry(θ)</button><button class="gate" data-g="RZ">Rz(θ)</button><button class="gate" data-g="SX">√X</button></div>' +
      '<div class="field-label">Stan początkowy</div><div class="gate-pad five" style="grid-template-columns:repeat(3,1fr)">' +
      ['0', '1', '+', '−', '+i', '−i'].map(function (s) { return '<button class="gate" data-s="' + s + '">|' + s + '⟩</button>'; }).join('') +
      '</div><div class="btn-row"><button class="btn" id="meas">📏 Zmierz</button><button class="btn ghost" id="shots">100 pomiarów</button></div></div>' +
      '<div class="card"><div class="field-label">Stan kubitu</div><div id="info"></div></div>' +
      '<div class="card md"><div class="callout callout-key"><div class="callout-title">Co tu widać?</div>' +
      MD.render('Każdy czysty stan kubitu $|\\psi\\rangle=\\cos\\tfrac{\\theta}{2}|0\\rangle+e^{i\\varphi}\\sin\\tfrac{\\theta}{2}|1\\rangle$ to punkt na sferze. ' +
        'Bramka jednokubitowa to **obrót sfery**: $X$, $Y$, $Z$ obracają o $\\pi$ wokół swoich osi, $H$ o $\\pi$ wokół osi $(x+z)/\\sqrt2$, a $S$ i $T$ o $\\pi/2$ i $\\pi/4$ wokół osi $z$. ' +
        'Pomiar w bazie obliczeniowej daje 0 z prawdopodobieństwem $\\cos^2\\tfrac{\\theta}{2}$ i „rzutuje” stan na biegun.') + '</div></div>';

    var canvas = A.$('#bc', el);
    var ctx, size;
    function layout() {
      size = Math.min(canvas.parentNode.clientWidth, 420);
      canvas.style.width = size + 'px';
      canvas.style.margin = '0 auto';
      ctx = Lab.setupCanvas(canvas, size, size);
      draw();
    }
    function draw() { Lab.drawBloch(ctx, size, vec, { view: view, trail: trail }); }

    function info() {
      var b = Q.bloch(state, 0);
      var p0 = state.re[0] * state.re[0] + state.im[0] * state.im[0];
      var th = Math.acos(Math.max(-1, Math.min(1, b.z)));
      var ph = Math.abs(Math.sin(th)) < 1e-6 ? 0 : Math.atan2(b.y, b.x);
      A.$('#info', el).innerHTML =
        '<div class="md">' + MD.render('$$|\\psi\\rangle = (' + Q.fmtComplex(state.re[0], state.im[0]) + ')\\,|0\\rangle + (' + Q.fmtComplex(state.re[1], state.im[1]) + ')\\,|1\\rangle$$') + '</div>' +
        '<div class="probs">' +
        '<div class="prow"><code>P(0)</code><div class="pb"><i style="width:' + (100 * p0) + '%"></i></div><span>' + (100 * p0).toFixed(1) + '%</span></div>' +
        '<div class="prow"><code>P(1)</code><div class="pb"><i style="width:' + (100 * (1 - p0)) + '%"></i></div><span>' + (100 * (1 - p0)).toFixed(1) + '%</span></div></div>' +
        '<dl class="kv"><dt>θ (biegunowy)</dt><dd>' + Q.fmtAngle(th) + ' (' + (th * 180 / Math.PI).toFixed(1) + '°)</dd>' +
        '<dt>φ (faza względna)</dt><dd>' + Q.fmtAngle(ph) + ' (' + (ph * 180 / Math.PI).toFixed(1) + '°)</dd>' +
        '<dt>Wektor Blocha</dt><dd>(' + Q.fmtNum(b.x, 2) + ', ' + Q.fmtNum(b.y, 2) + ', ' + Q.fmtNum(b.z, 2) + ')</dd>' +
        '<dt>⟨Z⟩</dt><dd>' + Q.fmtNum(b.z, 3) + '</dd></dl>';
      A.$('#hist', el).textContent = hist.length ? '|ψ₀⟩ → ' + hist.join(' → ') : 'Historia bramek pojawi się tutaj';
    }

    function stopAnim() {
      if (anim) { cancelAnimationFrame(anim.raf); anim.finish(); }
    }

    function animateTo(axis, angle, done) {
      var v0 = vec.slice(), t0 = performance.now(), dur = 380 + 260 * Math.min(1, Math.abs(angle) / Math.PI);
      trail = [v0];
      anim = {
        finish: function () { anim = null; done(); draw(); }
      };
      function step(now) {
        var t = Math.min(1, (now - t0) / dur);
        var e = t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
        vec = Lab.rotate(v0, axis, angle * e);
        trail.push(vec);
        draw();
        if (t < 1) anim.raf = requestAnimationFrame(step); else anim.finish();
      }
      anim.raf = requestAnimationFrame(step);
    }

    function syncVec() { var b = Q.bloch(state, 0); vec = [b.x, b.y, b.z]; }

    function applyGate(g) {
      stopAnim();
      var axis, angle, label;
      if (g === 'RX' || g === 'RY' || g === 'RZ') {
        axis = g === 'RX' ? [1, 0, 0] : g === 'RY' ? [0, 1, 0] : [0, 0, 1];
        angle = theta;
        label = LABEL[g] + '(' + Q.fmtAngle(theta) + ')';
      } else {
        axis = GATE_ROT[g][0]; angle = GATE_ROT[g][1]; label = LABEL[g] || g;
      }
      Q.apply1(state, 0, Q.gateMatrix(g, theta));
      hist.push(label);
      if (hist.length > 30) hist.shift();
      info();
      animateTo(axis, angle, syncVec);
    }

    function setState(s) {
      stopAnim();
      state = new Q.State(1);
      var r = Math.SQRT1_2;
      var amps = { '0': [1, 0, 0, 0], '1': [0, 0, 1, 0], '+': [r, 0, r, 0], '−': [r, 0, -r, 0], '+i': [r, 0, 0, r], '−i': [r, 0, 0, -r] }[s];
      state.re[0] = amps[0]; state.im[0] = amps[1]; state.re[1] = amps[2]; state.im[1] = amps[3];
      hist = [];
      trail = [];
      syncVec();
      info(); draw();
    }

    function measure() {
      stopAnim();
      var r = Q.measure(state, 0);
      hist.push('M=' + r);
      var target = r ? [0, 0, -1] : [0, 0, 1];
      // animacja "kolapsu" po łuku wielkim
      var v0 = vec.slice();
      var axis = [v0[1] * target[2] - v0[2] * target[1], v0[2] * target[0] - v0[0] * target[2], v0[0] * target[1] - v0[1] * target[0]];
      var n = Math.hypot(axis[0], axis[1], axis[2]);
      info();
      A.toast('Wynik pomiaru: ' + r + ' → stan |' + r + '⟩');
      if (n < 1e-6) { syncVec(); draw(); return; }
      var ang = Math.acos(Math.max(-1, Math.min(1, v0[0] * target[0] + v0[1] * target[1] + v0[2] * target[2])));
      animateTo(axis.map(function (x) { return x / n; }), ang, syncVec);
    }

    A.$$('[data-g]', el).forEach(function (b) { b.addEventListener('click', function () { applyGate(b.getAttribute('data-g')); }); });
    A.$$('[data-s]', el).forEach(function (b) { b.addEventListener('click', function () { setState(b.getAttribute('data-s')); }); });
    A.$('#meas', el).addEventListener('click', measure);
    A.$('#shots', el).addEventListener('click', function () {
      var p = Q.probs(state);
      var c = Q.sample(p, 100);
      A.toast('100 pomiarów: „0” × ' + c[0] + ', „1” × ' + c[1] + ' (stan bez zmian — to statystyka)');
    });
    var th = A.$('#th', el), tho = A.$('#tho', el);
    th.addEventListener('input', function () {
      theta = +th.value * Math.PI / 180;
      tho.textContent = Q.fmtAngle(theta);
    });

    var undrag = Lab.dragView(canvas, view, function () { draw(); });
    window.addEventListener('resize', layout);
    layout();
    info();
    return function () {
      if (anim) cancelAnimationFrame(anim.raf);
      undrag();
      window.removeEventListener('resize', layout);
    };
  });
})();
