/* Symulator wektora stanu (do ~10 kubitów). Konwencja: q0 to lewy (najstarszy) bit w kecie |q0 q1 ... >. */
(function () {
  'use strict';

  var S2 = Math.SQRT1_2;

  function State(n) {
    this.n = n;
    this.dim = 1 << n;
    this.re = new Float64Array(this.dim);
    this.im = new Float64Array(this.dim);
    this.re[0] = 1;
  }
  State.prototype.clone = function () {
    var s = new State(this.n);
    s.re.set(this.re);
    s.im.set(this.im);
    return s;
  };

  // Macierz 2x2: [a, b, c, d] jako pary [re, im]
  function M(ar, ai, br, bi, cr, ci, dr, di) { return [ar, ai, br, bi, cr, ci, dr, di]; }

  var G = {
    I: M(1, 0, 0, 0, 0, 0, 1, 0),
    X: M(0, 0, 1, 0, 1, 0, 0, 0),
    Y: M(0, 0, 0, -1, 0, 1, 0, 0),
    Z: M(1, 0, 0, 0, 0, 0, -1, 0),
    H: M(S2, 0, S2, 0, S2, 0, -S2, 0),
    S: M(1, 0, 0, 0, 0, 0, 0, 1),
    SDG: M(1, 0, 0, 0, 0, 0, 0, -1),
    T: M(1, 0, 0, 0, 0, 0, S2, S2),
    TDG: M(1, 0, 0, 0, 0, 0, S2, -S2),
    SX: M(0.5, 0.5, 0.5, -0.5, 0.5, -0.5, 0.5, 0.5)
  };
  function RX(t) { var c = Math.cos(t / 2), s = Math.sin(t / 2); return M(c, 0, 0, -s, 0, -s, c, 0); }
  function RY(t) { var c = Math.cos(t / 2), s = Math.sin(t / 2); return M(c, 0, -s, 0, s, 0, c, 0); }
  function RZ(t) { var c = Math.cos(t / 2), s = Math.sin(t / 2); return M(c, -s, 0, 0, 0, 0, c, s); }
  function P(t) { return M(1, 0, 0, 0, 0, 0, Math.cos(t), Math.sin(t)); }

  function gateMatrix(name, theta) {
    switch (name) {
      case 'RX': return RX(theta);
      case 'RY': return RY(theta);
      case 'RZ': return RZ(theta);
      case 'P': return P(theta);
      default: return G[name];
    }
  }

  function bitMask(s, q) { return 1 << (s.n - 1 - q); }

  // Bramka jednokubitowa (opcjonalnie sterowana kubitem ctrl)
  function apply1(s, q, m, ctrl) {
    var mask = bitMask(s, q);
    var cmask = ctrl === undefined || ctrl === null ? 0 : bitMask(s, ctrl);
    var re = s.re, im = s.im;
    for (var i = 0; i < s.dim; i++) {
      if (i & mask) continue;
      if (cmask && !(i & cmask)) continue;
      var j = i | mask;
      var r0 = re[i], i0 = im[i], r1 = re[j], i1 = im[j];
      re[i] = m[0] * r0 - m[1] * i0 + m[2] * r1 - m[3] * i1;
      im[i] = m[0] * i0 + m[1] * r0 + m[2] * i1 + m[3] * r1;
      re[j] = m[4] * r0 - m[5] * i0 + m[6] * r1 - m[7] * i1;
      im[j] = m[4] * i0 + m[5] * r0 + m[6] * i1 + m[7] * r1;
    }
    return s;
  }

  function swap(s, a, b) {
    var ma = bitMask(s, a), mb = bitMask(s, b);
    for (var i = 0; i < s.dim; i++) {
      if ((i & ma) && !(i & mb)) {
        var j = (i & ~ma) | mb;
        var t = s.re[i]; s.re[i] = s.re[j]; s.re[j] = t;
        t = s.im[i]; s.im[i] = s.im[j]; s.im[j] = t;
      }
    }
    return s;
  }

  // op: {g, t, c?, theta?}
  function applyOp(s, op) {
    switch (op.g) {
      case 'CNOT': return apply1(s, op.t, G.X, op.c);
      case 'CZ': return apply1(s, op.t, G.Z, op.c);
      case 'CY': return apply1(s, op.t, G.Y, op.c);
      case 'CP': return apply1(s, op.t, P(op.theta), op.c);
      case 'CRY': return apply1(s, op.t, RY(op.theta), op.c);
      case 'SWAP': return swap(s, op.c, op.t);
      default: return apply1(s, op.t, gateMatrix(op.g, op.theta));
    }
  }

  function run(n, ops) {
    var s = new State(n);
    for (var k = 0; k < ops.length; k++) applyOp(s, ops[k]);
    return s;
  }

  function probs(s) {
    var p = new Float64Array(s.dim);
    for (var i = 0; i < s.dim; i++) p[i] = s.re[i] * s.re[i] + s.im[i] * s.im[i];
    return p;
  }

  function expZ(s, q) {
    var mask = bitMask(s, q), e = 0;
    for (var i = 0; i < s.dim; i++) {
      var p = s.re[i] * s.re[i] + s.im[i] * s.im[i];
      e += (i & mask) ? -p : p;
    }
    return e;
  }

  // Wektor Blocha zredukowanego stanu kubitu q (długość < 1 oznacza splątanie)
  function bloch(s, q) {
    var mask = bitMask(s, q);
    var r00 = 0, r11 = 0, rr = 0, ri = 0;
    for (var i = 0; i < s.dim; i++) {
      if (i & mask) continue;
      var j = i | mask;
      r00 += s.re[i] * s.re[i] + s.im[i] * s.im[i];
      r11 += s.re[j] * s.re[j] + s.im[j] * s.im[j];
      // rho01 = sum a_i * conj(a_j)
      rr += s.re[i] * s.re[j] + s.im[i] * s.im[j];
      ri += s.im[i] * s.re[j] - s.re[i] * s.im[j];
    }
    return { x: 2 * rr, y: -2 * ri, z: r00 - r11 };
  }

  function sample(p, shots) {
    var cdf = new Float64Array(p.length), acc = 0;
    for (var i = 0; i < p.length; i++) { acc += p[i]; cdf[i] = acc; }
    var counts = new Array(p.length).fill(0);
    for (var k = 0; k < shots; k++) {
      var r = Math.random() * acc, lo = 0, hi = p.length - 1;
      while (lo < hi) { var mid = (lo + hi) >> 1; if (cdf[mid] < r) lo = mid + 1; else hi = mid; }
      counts[lo]++;
    }
    return counts;
  }

  // Pomiar jednego kubitu z kolapsem stanu; zwraca 0 lub 1
  function measure(s, q) {
    var mask = bitMask(s, q), p1 = 0;
    for (var i = 0; i < s.dim; i++) if (i & mask) p1 += s.re[i] * s.re[i] + s.im[i] * s.im[i];
    var r = Math.random() < p1 ? 1 : 0;
    var norm = Math.sqrt(r ? p1 : 1 - p1) || 1;
    for (var k = 0; k < s.dim; k++) {
      var bit = (k & mask) ? 1 : 0;
      if (bit !== r) { s.re[k] = 0; s.im[k] = 0; } else { s.re[k] /= norm; s.im[k] /= norm; }
    }
    return r;
  }

  function ket(i, n) {
    var b = i.toString(2);
    while (b.length < n) b = '0' + b;
    return '|' + b + '⟩';
  }

  function fmtNum(x, d) {
    d = d === undefined ? 3 : d;
    var v = Math.abs(x) < 5e-10 ? 0 : x;
    return v.toFixed(d).replace(/\.?0+$/, '') || '0';
  }

  function fmtComplex(re, im, d) {
    d = d === undefined ? 3 : d;
    var r = Math.abs(re) < 5e-4 ? 0 : re;
    var i = Math.abs(im) < 5e-4 ? 0 : im;
    if (i === 0) return fmtNum(r, d);
    if (r === 0) return (i < 0 ? '−' : '') + fmtNum(Math.abs(i), d) + 'i';
    return fmtNum(r, d) + (i < 0 ? ' − ' : ' + ') + fmtNum(Math.abs(i), d) + 'i';
  }

  // Ładny zapis kąta jako wielokrotności π
  function fmtAngle(t) {
    var k = t / Math.PI;
    var fracs = [[0, '0'], [1, 'π'], [-1, '−π'], [0.5, 'π/2'], [-0.5, '−π/2'], [0.25, 'π/4'], [-0.25, '−π/4'],
      [0.75, '3π/4'], [-0.75, '−3π/4'], [1 / 3, 'π/3'], [-1 / 3, '−π/3'], [2 / 3, '2π/3'], [-2 / 3, '−2π/3'],
      [1 / 6, 'π/6'], [-1 / 6, '−π/6'], [0.125, 'π/8'], [-0.125, '−π/8'], [2, '2π'], [1.5, '3π/2'], [-1.5, '−3π/2']];
    for (var j = 0; j < fracs.length; j++) if (Math.abs(k - fracs[j][0]) < 1e-6) return fracs[j][1];
    return fmtNum(t, 2);
  }

  window.QSim = {
    State: State, G: G, RX: RX, RY: RY, RZ: RZ, P: P, gateMatrix: gateMatrix,
    apply1: apply1, applyOp: applyOp, swap: swap, run: run, probs: probs, expZ: expZ, bloch: bloch,
    sample: sample, measure: measure, ket: ket, fmtNum: fmtNum, fmtComplex: fmtComplex, fmtAngle: fmtAngle
  };
})();
