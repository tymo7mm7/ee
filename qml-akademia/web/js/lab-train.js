/* Laboratorium: wariacyjny klasyfikator kwantowy (VQC) vs klasyczne ML na danych 2D. */
(function () {
  'use strict';
  var Q = window.QSim;
  var HALF_PI = Math.PI / 2;

  // ---------- Dane ----------
  function rng(seed) {
    return function () {
      seed |= 0; seed = seed + 0x6D2B79F5 | 0;
      var t = Math.imul(seed ^ seed >>> 15, 1 | seed);
      t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t;
      return ((t ^ t >>> 14) >>> 0) / 4294967296;
    };
  }
  function gauss(r) { return Math.sqrt(-2 * Math.log(r() + 1e-12)) * Math.cos(2 * Math.PI * r()); }

  var DATASETS = {
    circle: { name: 'Okrąg', gen: function (r) { var x = 2 * r() - 1, y = 2 * r() - 1; return [x, y, x * x + y * y < 0.42 ? 1 : 0]; } },
    xor: {
      name: 'XOR', gen: function (r) {
        var x, y;
        do { x = 2 * r() - 1; y = 2 * r() - 1; } while (Math.abs(x) < 0.1 || Math.abs(y) < 0.1);
        return [x, y, x * y > 0 ? 1 : 0];
      }
    },
    moons: {
      name: 'Księżyce', gen: function (r) {
        var c = r() < 0.5 ? 0 : 1, t = Math.PI * r(), x, y;
        if (c === 0) { x = Math.cos(t); y = Math.sin(t) - 0.25; } else { x = 1 - Math.cos(t); y = 0.25 - Math.sin(t); }
        x = (x - 0.5) * 0.85 + 0.08 * gauss(r); y = y * 0.85 + 0.08 * gauss(r);
        return [x, y, c];
      }
    },
    linear: { name: 'Liniowe', gen: function (r) { var x = 2 * r() - 1, y = 2 * r() - 1; return [x, y, x - 0.6 * y + 0.15 * gauss(r) > 0.1 ? 1 : 0]; } }
  };

  function makeData(kind, n, seed) {
    var r = rng(seed), out = [];
    for (var i = 0; i < n; i++) out.push(DATASETS[kind].gen(r));
    return out;
  }

  // ---------- Modele ----------
  function sigmoid(z) { return 1 / (1 + Math.exp(-z)); }
  function clampP(p) { return Math.min(1 - 1e-7, Math.max(1e-7, p)); }

  // Obwód wariacyjny: lista bramek; kąt = w*x[feat] + b
  function Circuit(nq, ops, nParams) { this.nq = nq; this.ops = ops; this.nParams = nParams; }
  Circuit.prototype.angle = function (op, P, x) {
    var a = op.b >= 0 ? P[op.b] : 0;
    if (op.w >= 0) a += P[op.w] * x[op.f];
    return a + (op.shift || 0);
  };
  Circuit.prototype.expect = function (P, x, shiftIdx, shift) {
    var s = new Q.State(this.nq);
    for (var k = 0; k < this.ops.length; k++) {
      var op = this.ops[k];
      if (op.g === 'CZ') { Q.apply1(s, op.t, Q.G.Z, op.c); continue; }
      var a = this.angle(op, P, x) + (k === shiftIdx ? shift : 0);
      Q.apply1(s, op.q, op.g === 'RY' ? Q.RY(a) : op.g === 'RZ' ? Q.RZ(a) : Q.RX(a));
    }
    return Q.expZ(s, 0);
  };

  function buildVQC(kind, L) {
    var ops = [], p = 0;
    if (kind === 'vqc1') {
      // 1 kubit, data re-uploading: [RY(a·x1+b) RZ(c·x2+d)] × L, na końcu RY(e)
      for (var l = 0; l < L; l++) {
        ops.push({ g: 'RY', q: 0, w: p, f: 0, b: p + 1 }); p += 2;
        ops.push({ g: 'RZ', q: 0, w: p, f: 1, b: p + 1 }); p += 2;
      }
      ops.push({ g: 'RY', q: 0, w: -1, b: p }); p += 1;
      return new Circuit(1, ops, p);
    }
    // 2 kubity: [RY(a·x1+b) ⊗ RY(c·x2+d), CZ, RY(e) ⊗ RY(f)] × L
    for (var m = 0; m < L; m++) {
      ops.push({ g: 'RY', q: 0, w: p, f: 0, b: p + 1 }); p += 2;
      ops.push({ g: 'RY', q: 1, w: p, f: 1, b: p + 1 }); p += 2;
      ops.push({ g: 'CZ', c: 0, t: 1 });
      ops.push({ g: 'RY', q: 0, w: -1, b: p }); p += 1;
      ops.push({ g: 'RY', q: 1, w: -1, b: p }); p += 1;
      if (m < L - 1) ops.push({ g: 'CZ', c: 0, t: 1 });
    }
    return new Circuit(2, ops, p);
  }

  function initParams(n, r, scale, ones) {
    var P = new Float64Array(n);
    for (var i = 0; i < n; i++) P[i] = (r() * 2 - 1) * scale;
    if (ones) ones.forEach(function (i) { P[i] = 1 + 0.5 * (r() * 2 - 1); });
    return P;
  }

  function Model(kind, layers, seed) {
    var r = rng(seed || 7);
    this.kind = kind;
    this.quantum = kind === 'vqc1' || kind === 'vqc2';
    this.evals = 0;
    if (this.quantum) {
      this.circ = buildVQC(kind, layers);
      var wIdx = [];
      this.circ.ops.forEach(function (op) { if (op.w >= 0) wIdx.push(op.w); });
      this.P = initParams(this.circ.nParams, r, Math.PI, null);
      // wagi danych startują w okolicy 1..π (rozsądna "częstotliwość" kodowania)
      for (var i = 0; i < wIdx.length; i++) this.P[wIdx[i]] = 1.5 + r() * 1.5;
    } else if (kind === 'logreg') {
      this.P = initParams(3, r, 0.1);
    } else {
      this.H = 8;
      this.P = initParams(this.H * 4 + 1, r, 0.9);
    }
    this.m = new Float64Array(this.P.length);
    this.v = new Float64Array(this.P.length);
    this.t = 0;
  }

  Model.prototype.nParams = function () { return this.P.length; };

  Model.prototype.predict = function (x) {
    var P = this.P;
    if (this.quantum) return clampP((1 - this.circ.expect(P, x, -1, 0)) / 2);
    if (this.kind === 'logreg') return clampP(sigmoid(P[0] * x[0] + P[1] * x[1] + P[2]));
    var H = this.H, z = P[4 * H];
    for (var j = 0; j < H; j++) z += P[3 * H + j] * Math.tanh(P[j] * x[0] + P[H + j] * x[1] + P[2 * H + j]);
    return clampP(sigmoid(z));
  };

  // Strata (entropia krzyżowa) i gradient
  Model.prototype.lossGrad = function (data) {
    var P = this.P, g = new Float64Array(P.length), loss = 0, N = data.length, self = this;
    data.forEach(function (d) {
      var x = d, y = d[2];
      if (self.quantum) {
        var c = self.circ;
        var f = c.expect(P, x, -1, 0);
        var p = clampP((1 - f) / 2);
        loss += -(y * Math.log(p) + (1 - y) * Math.log(1 - p));
        var dLdf = (-(y / p) + (1 - y) / (1 - p)) * -0.5;
        self.evals += 1;
        for (var k = 0; k < c.ops.length; k++) {
          var op = c.ops[k];
          if (op.g === 'CZ') continue;
          // reguła przesunięcia parametru: df/dkąt = [f(kąt+π/2) − f(kąt−π/2)] / 2
          var dfda = (c.expect(P, x, k, HALF_PI) - c.expect(P, x, k, -HALF_PI)) / 2;
          self.evals += 2;
          var gk = dLdf * dfda;
          if (op.b >= 0) g[op.b] += gk;
          if (op.w >= 0) g[op.w] += gk * x[op.f];
        }
      } else if (self.kind === 'logreg') {
        var pl = clampP(sigmoid(P[0] * x[0] + P[1] * x[1] + P[2]));
        loss += -(y * Math.log(pl) + (1 - y) * Math.log(1 - pl));
        var e = pl - y;
        g[0] += e * x[0]; g[1] += e * x[1]; g[2] += e;
      } else {
        var H = self.H, hs = new Float64Array(H), z = P[4 * H];
        for (var j = 0; j < H; j++) { hs[j] = Math.tanh(P[j] * x[0] + P[H + j] * x[1] + P[2 * H + j]); z += P[3 * H + j] * hs[j]; }
        var pm = clampP(sigmoid(z));
        loss += -(y * Math.log(pm) + (1 - y) * Math.log(1 - pm));
        var dz = pm - y;
        g[4 * H] += dz;
        for (var jj = 0; jj < H; jj++) {
          g[3 * H + jj] += dz * hs[jj];
          var dh = dz * P[3 * H + jj] * (1 - hs[jj] * hs[jj]);
          g[jj] += dh * x[0]; g[H + jj] += dh * x[1]; g[2 * H + jj] += dh;
        }
      }
    });
    for (var i = 0; i < g.length; i++) g[i] /= N;
    return { loss: loss / N, grad: g };
  };

  Model.prototype.step = function (data, lr) {
    var lg = this.lossGrad(data), g = lg.grad;
    var b1 = 0.9, b2 = 0.999, eps = 1e-8;
    this.t++;
    for (var i = 0; i < this.P.length; i++) {
      this.m[i] = b1 * this.m[i] + (1 - b1) * g[i];
      this.v[i] = b2 * this.v[i] + (1 - b2) * g[i] * g[i];
      var mh = this.m[i] / (1 - Math.pow(b1, this.t)), vh = this.v[i] / (1 - Math.pow(b2, this.t));
      this.P[i] -= lr * mh / (Math.sqrt(vh) + eps);
    }
    return lg.loss;
  };

  Model.prototype.accuracy = function (data) {
    var ok = 0, self = this;
    data.forEach(function (d) { if ((self.predict(d) > 0.5 ? 1 : 0) === d[2]) ok++; });
    return data.length ? ok / data.length : 0;
  };

  Model.prototype.circuitsPerEpoch = function (N) {
    if (!this.quantum) return 0;
    var rot = this.circ.ops.filter(function (o) { return o.g !== 'CZ'; }).length;
    return N * (1 + 2 * rot);
  };

  window.QMLModels = { Model: Model, makeData: makeData, DATASETS: DATASETS };

  // ---------- Widok ----------
  if (!window.App) return;
  var A = App;
  var Model = window.QMLModels.Model, DATASETS = window.QMLModels.DATASETS;

  var MODEL_INFO = {
    vqc1: {
      name: 'VQC · 1 kubit',
      md: 'Jeden kubit, **data re‑uploading**: dane są wczytywane w każdej warstwie.\n\n$$U(\\mathbf x)=R_Y(\\theta_f)\\prod_{l=1}^{L} R_Z(c_l x_2+d_l)\\,R_Y(a_l x_1+b_l)$$\n\n$$p(y{=}1\\mid\\mathbf x)=\\tfrac12\\big(1-\\langle 0|U^\\dagger Z\\,U|0\\rangle\\big)$$\n\nGradient liczony **regułą parameter‑shift**: każda bramka wymaga 2 dodatkowych obwodów na próbkę. Więcej warstw = wyższe częstotliwości w szeregu Fouriera modelu, czyli bardziej złożone granice decyzyjne.'
    },
    vqc2: {
      name: 'VQC · 2 kubity',
      md: 'Kodowanie kątowe: $x_1$ na kubicie 0, $x_2$ na kubicie 1 (z powtórnym wczytywaniem w każdej warstwie), bramka splątująca **CZ** i trenowalne rotacje $R_Y$. Odczyt: $\\langle Z_0\\rangle$.\n\nTo typowy „hardware‑efficient ansatz” w miniaturze — splątanie pozwala modelowi łączyć cechy (np. iloczyn $x_1x_2$ potrzebny dla XOR).'
    },
    logreg: {
      name: 'Regresja logistyczna',
      md: '$$p(y{=}1\\mid\\mathbf x)=\\sigma(w_1x_1+w_2x_2+b)$$\n\nGranica decyzyjna jest **prostą**. Na danych „Okrąg” i „XOR” model musi przegrać — to dobra ilustracja, po co nam nieliniowe cechy, kernele lub sieci.'
    },
    mlp: {
      name: 'Sieć neuronowa (MLP)',
      md: 'Perceptron wielowarstwowy 2 → 8 (tanh) → 1 (sigmoida), 33 parametry, gradient z **propagacji wstecznej** (jeden przebieg w przód i w tył — tanio, w przeciwieństwie do parameter‑shift).'
    }
  };

  A.route(/^\/lab\/train$/, function (el) {
    A.header({ title: 'VQC vs klasyczne ML', tab: 'lab', parent: '#/lab' });
    var cfg = { data: 'circle', model: 'vqc1', layers: 3, lr: 0.1 };
    var train, test, model, running = false, raf = null, epoch = 0, losses = [], lastLoss = NaN;
    var MAX_EPOCHS = 400;

    function seg(id, items, cur) {
      return '<div class="seg" id="' + id + '">' + items.map(function (it) {
        return '<button data-v="' + it[0] + '"' + (String(it[0]) === String(cur) ? ' class="on"' : '') + '>' + it[1] + '</button>';
      }).join('') + '</div>';
    }

    el.innerHTML =
      '<div class="card"><div class="canvas-wrap"><canvas id="plot"></canvas></div>' +
      '<div class="legend"><span><i style="background:#4d9bff"></i>klasa 0</span><span><i style="background:#ff7a59"></i>klasa 1</span><span>● trening · ○ test</span></div>' +
      '<div class="metrics" id="metrics"></div>' +
      '<div class="btn-row"><button class="btn" id="run">▶ Trenuj</button><button class="btn ghost" id="reset">↺ Od nowa</button></div>' +
      '<canvas id="lossc"></canvas></div>' +
      '<div class="card"><div class="field-label">Zbiór danych</div>' +
      seg('ds', Object.keys(DATASETS).map(function (k) { return [k, DATASETS[k].name]; }), cfg.data) +
      '<div class="field-label">Model</div>' +
      seg('md', [['vqc1', '⚛ VQC 1q'], ['vqc2', '⚛ VQC 2q'], ['logreg', 'Logistyczna'], ['mlp', 'MLP']], cfg.model) +
      '<div id="layersBox"><div class="field-label">Warstwy obwodu (L)</div>' + seg('ly', [[1, '1'], [2, '2'], [3, '3'], [4, '4'], [6, '6']], cfg.layers) + '</div>' +
      '<div class="field-label">Krok uczenia (Adam)</div>' + seg('lr', [[0.02, '0.02'], [0.05, '0.05'], [0.1, '0.1'], [0.2, '0.2']], cfg.lr) +
      '</div><div class="card md" id="explain"></div>';

    var plot = A.$('#plot', el), lossc = A.$('#lossc', el);
    var pctx, lctx, size;

    function layout() {
      size = Math.min(plot.parentNode.clientWidth, 460);
      plot.style.width = size + 'px'; plot.style.margin = '0 auto';
      pctx = Lab.setupCanvas(plot, size, size);
      lossc.style.width = '100%';
      lctx = Lab.setupCanvas(lossc, lossc.parentNode.clientWidth - 32, 90);
      draw();
    }

    function reset() {
      stop();
      var seedBase = { circle: 11, xor: 23, moons: 5, linear: 3 }[cfg.data];
      train = QMLModels.makeData(cfg.data, 100, seedBase);
      test = QMLModels.makeData(cfg.data, 60, seedBase + 1000);
      model = new Model(cfg.model, cfg.layers, 42 + cfg.layers);
      epoch = 0; losses = []; lastLoss = NaN;
      A.$('#layersBox', el).style.display = model.quantum ? '' : 'none';
      var info = MODEL_INFO[cfg.model];
      A.$('#explain', el).innerHTML = '<h3>' + info.name + '</h3>' + MD.render(info.md);
      draw();
    }

    var heat = null;
    function draw() {
      if (!pctx || !model) return;
      var G = 36, cell = size / G;
      var col = Lab.colors();
      if (!heat) heat = new Float64Array(G * G);
      for (var i = 0; i < G; i++) for (var j = 0; j < G; j++) {
        var x = -1.2 + 2.4 * (i + 0.5) / G, y = 1.2 - 2.4 * (j + 0.5) / G;
        heat[j * G + i] = model.predict([x, y]);
      }
      pctx.clearRect(0, 0, size, size);
      Lab.heatmap(pctx, size, G, heat, function (p) {
        var s = Math.abs(p - 0.5) * 2, a = 0.12 + 0.5 * s;
        return p > 0.5 ? [255, 122, 89, a] : [77, 155, 255, a];
      });
      Lab.contour(pctx, size, G, heat, 0.5, col.light ? 'rgba(20,20,40,0.7)' : 'rgba(255,255,255,0.85)', 2);
      function pt(d, hollow) {
        var px = (d[0] + 1.2) / 2.4 * size, py = (1.2 - d[1]) / 2.4 * size;
        pctx.beginPath(); pctx.arc(px, py, hollow ? 4 : 4.2, 0, 2 * Math.PI);
        var c = d[2] ? '#ff7a59' : '#4d9bff';
        if (hollow) { pctx.strokeStyle = c; pctx.lineWidth = 2; pctx.stroke(); }
        else { pctx.fillStyle = c; pctx.fill(); pctx.strokeStyle = col.light ? '#fff' : 'rgba(0,0,0,0.6)'; pctx.lineWidth = 1; pctx.stroke(); }
      }
      train.forEach(function (d) { pt(d, false); });
      test.forEach(function (d) { pt(d, true); });
      drawLoss();
      metrics();
    }

    function drawLoss() {
      var w = lossc.clientWidth || 300, h = 90, col = Lab.colors();
      lctx.clearRect(0, 0, w, h);
      lctx.fillStyle = col.muted; lctx.font = '11px system-ui'; lctx.textAlign = 'left';
      lctx.fillText('strata (entropia krzyżowa) w kolejnych epokach', 4, 12);
      if (losses.length < 2) return;
      var mx = Math.max.apply(null, losses.slice(0, 5).concat([0.7]));
      lctx.strokeStyle = col.accent2; lctx.lineWidth = 2; lctx.beginPath();
      losses.forEach(function (l, i) {
        var x = 4 + (w - 8) * i / Math.max(MAX_EPOCHS, losses.length), y = h - 6 - (h - 24) * Math.min(1, l / mx);
        if (i) lctx.lineTo(x, y); else lctx.moveTo(x, y);
      });
      lctx.stroke();
    }

    function metrics() {
      var cpe = model.circuitsPerEpoch(train.length);
      A.$('#metrics', el).innerHTML =
        '<div><b>' + epoch + '</b>epoka</div>' +
        '<div><b>' + (isNaN(lastLoss) ? '–' : lastLoss.toFixed(3)) + '</b>strata</div>' +
        '<div><b>' + model.nParams() + '</b>parametrów</div>' +
        '<div><b>' + Math.round(100 * model.accuracy(train)) + '%</b>trafność (trening)</div>' +
        '<div><b>' + Math.round(100 * model.accuracy(test)) + '%</b>trafność (test)</div>' +
        '<div><b>' + (cpe ? cpe.toLocaleString('pl-PL') : '–') + '</b>' + (cpe ? 'obwodów / epokę' : 'obwodów (klasyczny)') + '</div>';
    }

    function loop() {
      var t0 = performance.now();
      var steps = 0;
      do {
        lastLoss = model.step(train, cfg.lr);
        losses.push(lastLoss);
        epoch++; steps++;
      } while (performance.now() - t0 < 14 && epoch < MAX_EPOCHS && steps < 10);
      draw();
      if (epoch >= MAX_EPOCHS) { stop(); A.toast('Koniec treningu (' + MAX_EPOCHS + ' epok)'); return; }
      if (running) raf = requestAnimationFrame(loop);
    }

    function start() {
      if (epoch >= MAX_EPOCHS) reset();
      running = true;
      A.$('#run', el).textContent = '⏸ Pauza';
      raf = requestAnimationFrame(loop);
    }
    function stop() {
      running = false;
      if (raf) cancelAnimationFrame(raf);
      raf = null;
      var b = A.$('#run', el);
      if (b) b.textContent = '▶ Trenuj';
    }

    A.$('#run', el).addEventListener('click', function () { if (running) stop(); else start(); });
    A.$('#reset', el).addEventListener('click', reset);
    function bindSeg(id, key, num) {
      A.$$('#' + id + ' button', el).forEach(function (b) {
        b.addEventListener('click', function () {
          A.$$('#' + id + ' button', el).forEach(function (x) { x.classList.toggle('on', x === b); });
          cfg[key] = num ? +b.getAttribute('data-v') : b.getAttribute('data-v');
          if (key !== 'lr') reset();
        });
      });
    }
    bindSeg('ds', 'data'); bindSeg('md', 'model'); bindSeg('ly', 'layers', true); bindSeg('lr', 'lr', true);

    window.addEventListener('resize', layout);
    reset();
    layout();
    return function () { stop(); window.removeEventListener('resize', layout); };
  });

})();
