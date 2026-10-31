/* Laboratorium: reguła parameter-shift oraz kernele kwantowe. */
(function () {
  'use strict';
  var A = App, Q = QSim;
  var PI = Math.PI;

  function binomEstimate(f, shots) {
    // estymata <Z> z N pomiarów: P(0) = (1+f)/2
    if (!shots) return f;
    var p0 = (1 + f) / 2, k = 0;
    if (shots > 2000) {
      // przybliżenie normalne dla dużych N (szybciej)
      var u = Math.sqrt(-2 * Math.log(Math.random() + 1e-12)) * Math.cos(2 * PI * Math.random());
      k = shots * p0 + u * Math.sqrt(shots * p0 * (1 - p0));
    } else {
      for (var i = 0; i < shots; i++) if (Math.random() < p0) k++;
    }
    return 2 * k / shots - 1;
  }

  // =====================================================================
  // Parameter-shift
  // =====================================================================
  A.route(/^\/lab\/shift$/, function (el) {
    A.header({ title: 'Reguła parameter‑shift', tab: 'lab', parent: '#/lab' });
    var theta = 0.6, shots = 0, stats = null;
    var f = function (t) { return Math.cos(t); };

    el.innerHTML =
      '<div class="card md">' + MD.render('Obwód: $|0\\rangle \\to R_Y(\\theta) \\to$ pomiar $Z$, więc $f(\\theta)=\\langle Z\\rangle=\\cos\\theta$. ' +
        'Reguła przesunięcia parametru daje **dokładną** pochodną z dwóch obwodów:\n\n$$\\frac{\\partial f}{\\partial\\theta}=\\frac{f(\\theta+\\tfrac{\\pi}{2})-f(\\theta-\\tfrac{\\pi}{2})}{2}$$') + '</div>' +
      '<div class="card"><canvas id="pc"></canvas>' +
      '<div class="legend"><span><i style="background:var(--accent2)"></i>f(θ)=cos θ</span><span><i style="background:var(--muted)"></i>f′(θ)</span><span><i style="background:var(--warn)"></i>θ ± π/2</span><span><i style="background:var(--ok)"></i>styczna</span></div>' +
      '<div class="field-label">Kąt θ</div><div class="slider-row"><input type="range" id="th" min="-314" max="314" value="60"><output id="tho"></output></div>' +
      '<div class="field-label">Liczba pomiarów (shots) na obwód</div><div class="seg" id="sh">' +
      [[0, '∞ (dokładnie)'], [10000, '10 000'], [1000, '1000'], [100, '100']].map(function (s) { return '<button data-v="' + s[0] + '"' + (s[0] === 0 ? ' class="on"' : '') + '>' + s[1] + '</button>'; }).join('') +
      '</div><div id="tbl"></div><button class="btn block ghost" id="rep">Powtórz estymację 300 razy</button><div id="stats"></div></div>' +
      '<div class="card md">' + MD.render('> [!key] Dlaczego nie różnice skończone?\n> Na sprzęcie kwantowym $f$ znamy tylko z dokładnością $\\sim 1/\\sqrt{N}$ (szum shotów). Iloraz różnicowy $[f(\\theta+h)-f(\\theta-h)]/2h$ dzieli ten szum przez małe $h$, więc wariancja eksploduje. Parameter‑shift używa dużego przesunięcia $\\pi/2$ i jest nieobciążony — dlatego jest standardem w QML (PennyLane, Qiskit).\n\nReguła działa dla bramek postaci $e^{-i\\theta G/2}$, gdzie $G$ ma wartości własne $\\pm1$ (np. $R_X, R_Y, R_Z$). Dla $P$ parametrów potrzeba $2P$ obwodów na jeden gradient.') + '</div>';

    var cv = A.$('#pc', el), ctx, W, H = 220;
    function layout() { W = cv.parentNode.clientWidth - 32; cv.style.width = W + 'px'; ctx = Lab.setupCanvas(cv, W, H); draw(); }

    function draw() {
      var col = Lab.colors();
      ctx.clearRect(0, 0, W, H);
      var X = function (t) { return 10 + (W - 20) * (t + PI) / (2 * PI); };
      var Y = function (v) { return H / 2 - v * (H / 2 - 16); };
      ctx.strokeStyle = col.line; ctx.lineWidth = 1;
      ctx.beginPath(); ctx.moveTo(10, Y(0)); ctx.lineTo(W - 10, Y(0)); ctx.moveTo(X(0), 6); ctx.lineTo(X(0), H - 6); ctx.stroke();
      ctx.fillStyle = col.muted; ctx.font = '11px system-ui';
      ctx.fillText('−π', X(-PI) - 2, Y(0) + 14); ctx.fillText('π', X(PI) - 10, Y(0) + 14); ctx.fillText('1', X(0) + 4, Y(1) + 4); ctx.fillText('−1', X(0) + 4, Y(-1));
      function curve(fn, color, dash) {
        ctx.strokeStyle = color; ctx.lineWidth = 2; ctx.setLineDash(dash || []);
        ctx.beginPath();
        for (var i = 0; i <= 200; i++) { var t = -PI + 2 * PI * i / 200; var px = X(t), py = Y(fn(t)); if (i) ctx.lineTo(px, py); else ctx.moveTo(px, py); }
        ctx.stroke(); ctx.setLineDash([]);
      }
      curve(function (t) { return -Math.sin(t); }, col.muted, [4, 4]);
      curve(f, col.accent2);
      // punkty przesunięte
      [theta + PI / 2, theta - PI / 2].forEach(function (t) {
        var tt = ((t + PI) % (2 * PI) + 2 * PI) % (2 * PI) - PI;
        ctx.fillStyle = col.warn; ctx.beginPath(); ctx.arc(X(tt), Y(f(t)), 5, 0, 2 * PI); ctx.fill();
      });
      // styczna z parameter-shift
      var g = (f(theta + PI / 2) - f(theta - PI / 2)) / 2;
      ctx.strokeStyle = col.ok; ctx.lineWidth = 2;
      ctx.beginPath(); ctx.moveTo(X(theta - 0.8), Y(f(theta) - 0.8 * g)); ctx.lineTo(X(theta + 0.8), Y(f(theta) + 0.8 * g)); ctx.stroke();
      ctx.fillStyle = col.text; ctx.beginPath(); ctx.arc(X(theta), Y(f(theta)), 5, 0, 2 * PI); ctx.fill();
    }

    function table() {
      var exact = -Math.sin(theta);
      var ps = (binomEstimate(f(theta + PI / 2), shots) - binomEstimate(f(theta - PI / 2), shots)) / 2;
      var h = 0.01;
      var fd = (binomEstimate(f(theta + h), shots) - binomEstimate(f(theta - h), shots)) / (2 * h);
      A.$('#tho', el).textContent = theta.toFixed(2) + ' rad';
      A.$('#tbl', el).innerHTML = '<div class="md"><div class="table-wrap"><table><thead><tr><th>Metoda</th><th>∂f/∂θ</th></tr></thead><tbody>' +
        '<tr><td>Dokładnie: −sin θ</td><td><b>' + exact.toFixed(4) + '</b></td></tr>' +
        '<tr><td>Parameter‑shift (2 obwody)</td><td>' + ps.toFixed(4) + '</td></tr>' +
        '<tr><td>Różnica skończona, h = 0.01</td><td>' + fd.toFixed(4) + '</td></tr></tbody></table></div></div>';
    }

    function repeat() {
      var R = 300, h = 0.01, a = [], b = [];
      for (var i = 0; i < R; i++) {
        a.push((binomEstimate(f(theta + PI / 2), shots) - binomEstimate(f(theta - PI / 2), shots)) / 2);
        b.push((binomEstimate(f(theta + h), shots) - binomEstimate(f(theta - h), shots)) / (2 * h));
      }
      function ms(v) { var m = v.reduce(function (s, x) { return s + x; }, 0) / v.length; var sd = Math.sqrt(v.reduce(function (s, x) { return s + (x - m) * (x - m); }, 0) / v.length); return [m, sd]; }
      var A1 = ms(a), B1 = ms(b);
      A.$('#stats', el).innerHTML = '<p class="small">Wynik 300 powtórzeń (' + (shots ? shots + ' shots' : 'bez szumu') + '):<br>' +
        'parameter‑shift: średnia <b>' + A1[0].toFixed(3) + '</b>, odchylenie <b>' + A1[1].toFixed(3) + '</b><br>' +
        'różnica skończona: średnia <b>' + B1[0].toFixed(3) + '</b>, odchylenie <b>' + B1[1].toFixed(3) + '</b>' +
        (shots ? ' ← ~' + Math.round(B1[1] / Math.max(A1[1], 1e-9)) + '× większy szum' : '') + '</p>';
    }

    var th = A.$('#th', el);
    th.addEventListener('input', function () { theta = +th.value / 100; draw(); table(); A.$('#stats', el).innerHTML = ''; });
    A.$$('#sh button', el).forEach(function (b) {
      b.addEventListener('click', function () {
        A.$$('#sh button', el).forEach(function (x) { x.classList.toggle('on', x === b); });
        shots = +b.getAttribute('data-v'); table(); A.$('#stats', el).innerHTML = '';
      });
    });
    A.$('#rep', el).addEventListener('click', repeat);
    window.addEventListener('resize', layout);
    layout(); table();
    return function () { window.removeEventListener('resize', layout); };
  });

  // =====================================================================
  // Kernele kwantowe
  // =====================================================================
  function featureState(map, x, s) {
    var st = new Q.State(2);
    var a = x[0] * s, b = x[1] * s;
    if (map === 'angle') {
      Q.apply1(st, 0, Q.RY(a)); Q.apply1(st, 1, Q.RY(b));
      return st;
    }
    // ZZFeatureMap (Havlíček i in. 2019), 2 powtórzenia
    for (var r = 0; r < 2; r++) {
      Q.apply1(st, 0, Q.G.H); Q.apply1(st, 1, Q.G.H);
      Q.apply1(st, 0, Q.P(2 * a)); Q.apply1(st, 1, Q.P(2 * b));
      Q.apply1(st, 1, Q.G.X, 0);
      Q.apply1(st, 1, Q.P(2 * (PI - a) * (PI - b)));
      Q.apply1(st, 1, Q.G.X, 0);
    }
    return st;
  }
  function fidelity(s1, s2) {
    var re = 0, im = 0;
    for (var i = 0; i < s1.dim; i++) { re += s1.re[i] * s2.re[i] + s1.im[i] * s2.im[i]; im += s1.re[i] * s2.im[i] - s1.im[i] * s2.re[i]; }
    return re * re + im * im;
  }
  function solve(M, y) {
    var n = y.length, a = M.map(function (r, i) { return r.slice().concat([y[i]]); });
    for (var c = 0; c < n; c++) {
      var p = c;
      for (var r = c + 1; r < n; r++) if (Math.abs(a[r][c]) > Math.abs(a[p][c])) p = r;
      var t = a[c]; a[c] = a[p]; a[p] = t;
      for (var r2 = c + 1; r2 < n; r2++) {
        var k = a[r2][c] / a[c][c];
        for (var j = c; j <= n; j++) a[r2][j] -= k * a[c][j];
      }
    }
    var x = new Array(n).fill(0);
    for (var i = n - 1; i >= 0; i--) {
      var sum = a[i][n];
      for (var j2 = i + 1; j2 < n; j2++) sum -= a[i][j2] * x[j2];
      x[i] = sum / a[i][i];
    }
    return x;
  }

  A.route(/^\/lab\/kernel$/, function (el) {
    A.header({ title: 'Kernele kwantowe', tab: 'lab', parent: '#/lab' });
    var cfg = { data: 'circle', map: 'angle', s: 1.5 };

    function seg(id, items, cur) {
      return '<div class="seg" id="' + id + '">' + items.map(function (it) { return '<button data-v="' + it[0] + '"' + (it[0] === cur ? ' class="on"' : '') + '>' + it[1] + '</button>'; }).join('') + '</div>';
    }
    el.innerHTML =
      '<div class="card md">' + MD.render('Kernel kwantowy to podobieństwo stanów: $k(\\mathbf x,\\mathbf x\')=|\\langle\\phi(\\mathbf x)|\\phi(\\mathbf x\')\\rangle|^2$. Na sprzęcie liczy się go obwodem $U^\\dagger(\\mathbf x\')U(\\mathbf x)$ i prawdopodobieństwem wyniku $|00\\rangle$. Poniżej: macierz Grama i klasyfikator jądrowy (kernel ridge) zbudowany na tym kernelu.') + '</div>' +
      '<div class="card"><div class="field-label">Mapa cech</div>' + seg('mp', [['angle', 'Kątowa (RY)'], ['zz', 'ZZ feature map'], ['rbf', 'Klasyczny RBF']], cfg.map) +
      '<div class="field-label">Dane</div>' + seg('ds', [['circle', 'Okrąg'], ['xor', 'XOR'], ['moons', 'Księżyce']], cfg.data) +
      '<div class="field-label">Skala wejścia / szerokość pasma s</div><div class="slider-row"><input type="range" id="bw" min="10" max="600" value="150"><output id="bwo"></output></div></div>' +
      '<div class="card"><div class="field-label">Macierz Grama (posortowana wg klas)</div><canvas id="gram"></canvas>' +
      '<p class="small muted" id="gramInfo"></p></div>' +
      '<div class="card"><div class="field-label">Granica decyzyjna klasyfikatora jądrowego</div><canvas id="dec"></canvas><div class="metrics" id="km"></div></div>' +
      '<div class="card md">' + MD.render('> [!warn] Szerokość pasma ma znaczenie\n> Dla dużego $s$ stany różnych punktów są prawie ortogonalne: $K\\approx I$, model zapamiętuje dane (przeuczenie). Dla wielu kubitów to zjawisko **wykładniczej koncentracji** kerneli — wszystkie wartości poza przekątną dążą do zera. Dla małego $s$ wszystkie stany są podobne ($K\\approx$ macierz jedynek) i model nie widzi różnic.\n\nZZ feature map z pracy Havlíček i in. (Nature 2019) zawiera człony $(\\pi-x_1)(\\pi-x_2)$ — splątanie tworzy cechy będące iloczynami wejść.') + '</div>';

    var train, test, gram = A.$('#gram', el), dec = A.$('#dec', el);
    function kfun(xs, ys, sx, sy) {
      if (cfg.map === 'rbf') { var dx = xs[0] - ys[0], dy = xs[1] - ys[1]; return Math.exp(-cfg.s * cfg.s * (dx * dx + dy * dy) / 2); }
      return fidelity(sx, sy);
    }
    function compute() {
      var seed = { circle: 11, xor: 23, moons: 5 }[cfg.data];
      train = QMLModels.makeData(cfg.data, 48, seed).sort(function (a, b) { return a[2] - b[2]; });
      test = QMLModels.makeData(cfg.data, 60, seed + 1000);
      var states = train.map(function (d) { return cfg.map === 'rbf' ? null : featureState(cfg.map, d, cfg.s); });
      var N = train.length, K = [];
      for (var i = 0; i < N; i++) { K.push([]); for (var j = 0; j < N; j++) K[i].push(kfun(train[i], train[j], states[i], states[j])); }
      var lam = 0.05;
      var M = K.map(function (r, i) { return r.map(function (v, j) { return v + (i === j ? lam : 0); }); });
      var alpha = solve(M, train.map(function (d) { return d[2] ? 1 : -1; }));
      function predict(x) {
        var sx = cfg.map === 'rbf' ? null : featureState(cfg.map, x, cfg.s), s = 0;
        for (var i2 = 0; i2 < N; i2++) s += alpha[i2] * kfun(x, train[i2], sx, states[i2]);
        return s;
      }
      // statystyki macierzy
      var off = 0, cnt = 0;
      for (var a = 0; a < N; a++) for (var b = 0; b < N; b++) if (a !== b) { off += K[a][b]; cnt++; }
      return { K: K, predict: predict, offMean: off / cnt };
    }

    function render() {
      A.$('#bwo', el).textContent = 's = ' + cfg.s.toFixed(2);
      var res = compute(), col = Lab.colors();
      var W = Math.min(gram.parentNode.clientWidth - 32, 420);
      gram.style.width = W + 'px'; gram.style.margin = '0 auto'; gram.style.display = 'block';
      var g = Lab.setupCanvas(gram, W, W), N = res.K.length, c = W / N;
      for (var i = 0; i < N; i++) for (var j = 0; j < N; j++) {
        var v = res.K[i][j];
        g.fillStyle = 'rgba(124,92,255,' + v.toFixed(3) + ')';
        g.fillRect(j * c, i * c, c + 0.5, c + 0.5);
      }
      var split = train.filter(function (d) { return !d[2]; }).length * c;
      g.strokeStyle = col.warn; g.lineWidth = 1.5;
      g.beginPath(); g.moveTo(split, 0); g.lineTo(split, W); g.moveTo(0, split); g.lineTo(W, split); g.stroke();
      A.$('#gramInfo', el).innerHTML = 'Średnia wartość poza przekątną: <b>' + res.offMean.toFixed(3) + '</b>. Idealnie: jasne bloki na przekątnej (podobne punkty tej samej klasy), ciemne poza nimi.';

      dec.style.width = W + 'px'; dec.style.margin = '0 auto'; dec.style.display = 'block';
      var d = Lab.setupCanvas(dec, W, W), G = 30, vals = new Float64Array(G * G);
      for (var a = 0; a < G; a++) for (var b = 0; b < G; b++) {
        vals[b * G + a] = res.predict([-1.2 + 2.4 * (a + 0.5) / G, 1.2 - 2.4 * (b + 0.5) / G]);
      }
      Lab.heatmap(d, W, G, vals, function (f) {
        var s = Math.min(1, Math.abs(f)), al = 0.12 + 0.45 * s;
        return f > 0 ? [255, 122, 89, al] : [77, 155, 255, al];
      });
      Lab.contour(d, W, G, vals, 0, col.light ? 'rgba(20,20,40,0.7)' : 'rgba(255,255,255,0.85)', 2);
      function pt(p, hollow) {
        var px = (p[0] + 1.2) / 2.4 * W, py = (1.2 - p[1]) / 2.4 * W, cc = p[2] ? '#ff7a59' : '#4d9bff';
        d.beginPath(); d.arc(px, py, 4, 0, 2 * PI);
        if (hollow) { d.strokeStyle = cc; d.lineWidth = 2; d.stroke(); } else { d.fillStyle = cc; d.fill(); }
      }
      train.forEach(function (p) { pt(p, false); });
      test.forEach(function (p) { pt(p, true); });
      function acc(ds) { var ok = 0; ds.forEach(function (p) { if ((res.predict(p) > 0 ? 1 : 0) === p[2]) ok++; }); return Math.round(100 * ok / ds.length); }
      A.$('#km', el).innerHTML = '<div><b>' + acc(train) + '%</b>trening</div><div><b>' + acc(test) + '%</b>test</div><div><b>' + (cfg.map === 'rbf' ? 0 : N * (N - 1) / 2) + '</b>obwodów do K</div>';
    }

    var timer;
    var bw = A.$('#bw', el);
    bw.addEventListener('input', function () { cfg.s = +bw.value / 100; A.$('#bwo', el).textContent = 's = ' + cfg.s.toFixed(2); clearTimeout(timer); timer = setTimeout(render, 60); });
    ['mp', 'ds'].forEach(function (id) {
      A.$$('#' + id + ' button', el).forEach(function (b) {
        b.addEventListener('click', function () {
          A.$$('#' + id + ' button', el).forEach(function (x) { x.classList.toggle('on', x === b); });
          cfg[id === 'mp' ? 'map' : 'data'] = b.getAttribute('data-v');
          render();
        });
      });
    });
    window.addEventListener('resize', render);
    render();
    return function () { clearTimeout(timer); window.removeEventListener('resize', render); };
  });
})();
