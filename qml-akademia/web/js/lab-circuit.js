/* Laboratorium: symulator obwodów kwantowych (1-4 kubity). */
(function () {
  'use strict';
  var A = App, Q = QSim;
  var PI = Math.PI;

  var TWO = { CNOT: 1, CZ: 1, SWAP: 1, CP: 1 };
  var ROT = { RX: 1, RY: 1, RZ: 1, CP: 1 };
  var LABEL = { RX: 'Rx', RY: 'Ry', RZ: 'Rz', SDG: 'S†', TDG: 'T†', CP: 'P' };

  var PRESETS = {
    bell: {
      name: 'Stan Bella |Φ⁺⟩', n: 2,
      ops: [{ g: 'H', t: 0 }, { g: 'CNOT', c: 0, t: 1 }],
      note: 'H tworzy superpozycję, CNOT ją „kopiuje” w korelację: $(|00\\rangle+|11\\rangle)/\\sqrt2$. Każdy kubit osobno jest całkowicie losowy (wektor Blocha ma długość 0), ale wyniki są zawsze zgodne.'
    },
    ghz: {
      name: 'Stan GHZ (3 kubity)', n: 3,
      ops: [{ g: 'H', t: 0 }, { g: 'CNOT', c: 0, t: 1 }, { g: 'CNOT', c: 1, t: 2 }],
      note: '$(|000\\rangle+|111\\rangle)/\\sqrt2$ — maksymalnie splątany stan trzech kubitów.'
    },
    uniform: {
      name: 'Równomierna superpozycja', n: 3,
      ops: [{ g: 'H', t: 0 }, { g: 'H', t: 1 }, { g: 'H', t: 2 }],
      note: '$H^{\\otimes n}|0\\rangle^{\\otimes n}$ daje wszystkie $2^n$ stanów z równą amplitudą $1/\\sqrt{2^n}$ — punkt startowy wielu algorytmów.'
    },
    kickback: {
      name: 'Phase kickback', n: 2,
      ops: [{ g: 'X', t: 1 }, { g: 'H', t: 1 }, { g: 'H', t: 0 }, { g: 'CNOT', c: 0, t: 1 }, { g: 'H', t: 0 }],
      note: 'Cel w stanie $|-\\rangle$ jest wektorem własnym $X$ z wartością $-1$, więc CNOT „oddaje” fazę do kubitu kontrolnego: $q_0$ kończy w $|1\\rangle$, a cel się nie zmienia.'
    },
    dj: {
      name: 'Deutsch–Jozsa (f zrównoważona)', n: 3,
      ops: [{ g: 'X', t: 2 }, { g: 'H', t: 0 }, { g: 'H', t: 1 }, { g: 'H', t: 2 }, { g: 'CNOT', c: 0, t: 2 }, { g: 'CNOT', c: 1, t: 2 }, { g: 'H', t: 0 }, { g: 'H', t: 1 }],
      note: 'Wyrocznia $f(x)=x_0\\oplus x_1$ (CNOT-y do kubitu pomocniczego). Pomiar $q_0q_1$ daje $|11\\rangle\\neq|00\\rangle$ z pewnością, więc $f$ jest zrównoważona — po jednym wywołaniu wyroczni.'
    },
    grover: {
      name: 'Grover (2 kubity, szukamy |11⟩)', n: 2,
      ops: [{ g: 'H', t: 0 }, { g: 'H', t: 1 }, { g: 'CZ', c: 0, t: 1 },
        { g: 'H', t: 0 }, { g: 'H', t: 1 }, { g: 'X', t: 0 }, { g: 'X', t: 1 }, { g: 'CZ', c: 0, t: 1 }, { g: 'X', t: 0 }, { g: 'X', t: 1 }, { g: 'H', t: 0 }, { g: 'H', t: 1 }],
      note: 'Wyrocznia (CZ) odwraca fazę $|11\\rangle$, dyfuzja odbija amplitudy względem średniej. Dla $N=4$ jedna iteracja daje $|11\\rangle$ z prawdopodobieństwem 100%.'
    },
    teleport: {
      name: 'Teleportacja (odroczony pomiar)', n: 3,
      ops: [{ g: 'RY', t: 0, theta: 2 * PI / 3 }, { g: 'RZ', t: 0, theta: PI / 2 },
        { g: 'H', t: 1 }, { g: 'CNOT', c: 1, t: 2 },
        { g: 'CNOT', c: 0, t: 1 }, { g: 'H', t: 0 },
        { g: 'CNOT', c: 1, t: 2 }, { g: 'CZ', c: 0, t: 2 }],
      note: 'Stan $q_0$ (przygotowany rotacjami) trafia na $q_2$. Porównaj sfery Blocha: wektor $q_2$ na końcu = wektor $q_0$ po dwóch pierwszych bramkach. Pomiary i klasyczne korekty zastąpiono bramkami CNOT/CZ (zasada odroczonego pomiaru).'
    },
    qft: {
      name: 'QFT na 3 kubitach (wejście |101⟩)', n: 3,
      ops: [{ g: 'X', t: 0 }, { g: 'X', t: 2 },
        { g: 'H', t: 0 }, { g: 'CP', c: 1, t: 0, theta: PI / 2 }, { g: 'CP', c: 2, t: 0, theta: PI / 4 },
        { g: 'H', t: 1 }, { g: 'CP', c: 2, t: 1, theta: PI / 2 }, { g: 'H', t: 2 }, { g: 'SWAP', c: 0, t: 2 }],
      note: 'Kwantowa transformata Fouriera: wszystkie prawdopodobieństwa równe 1/8, a informacja o wejściu siedzi w **fazach** amplitud ($e^{2\\pi i\\, 5k/8}$).'
    }
  };

  A.route(/^\/lab\/circuit$/, function (el) {
    A.header({ title: 'Symulator obwodów', tab: 'lab', parent: '#/lab' });
    var n = 2;
    var ops = [];
    var sel = 'H';
    var pendingCtrl = null;
    var theta = PI / 2;
    var counts = null;
    var note = '';

    var gates = ['H', 'X', 'Y', 'Z', 'S', 'T', 'RX', 'RY', 'RZ', 'CNOT', 'CZ', 'SWAP', 'CP'];
    el.innerHTML =
      '<div class="card"><div class="field-label">Liczba kubitów</div><div class="seg" id="nq">' +
      [1, 2, 3, 4].map(function (k) { return '<button data-n="' + k + '"' + (k === n ? ' class="on"' : '') + '>' + k + '</button>'; }).join('') + '</div>' +
      '<div class="field-label">Gotowe przykłady</div><select id="preset" style="width:100%"><option value="">— wybierz —</option>' +
      Object.keys(PRESETS).map(function (k) { return '<option value="' + k + '">' + PRESETS[k].name + '</option>'; }).join('') + '</select>' +
      '<div class="md small" id="note"></div></div>' +
      '<div class="card"><div class="field-label">Bramka</div><div class="gate-pad five" id="pal">' +
      gates.map(function (g) { return '<button class="gate" data-g="' + g + '">' + (g === 'CP' ? 'CP(θ)' : (LABEL[g] || g)) + '</button>'; }).join('') +
      '</div><div class="slider-row" id="thrRow"><span class="small muted">θ</span><input type="range" id="th" min="-180" max="180" step="15" value="90"><output id="tho">π/2</output></div>' +
      '<p class="small muted" id="hint"></p>' +
      '<div class="circuit" id="circ"></div>' +
      '<div class="btn-row"><button class="btn ghost small" id="undo">↶ Cofnij</button><button class="btn ghost small" id="clear">Wyczyść</button><button class="btn small" id="shots">1024 pomiary</button></div></div>' +
      '<div class="card"><div class="field-label">Wynik: amplitudy i prawdopodobieństwa</div><div class="probs" id="probs"></div></div>' +
      '<div class="card"><div class="field-label">Kubity na sferach Blocha</div><div class="bloch-mini" id="minis"></div>' +
      '<p class="small muted">Krótszy wektor (|r| &lt; 1) oznacza, że kubit jest <b>splątany</b> z pozostałymi — sam w sobie jest w stanie mieszanym.</p></div>' +
      '<div class="card md small muted">' + MD.render('Konwencja: $q_0$ to **lewy** bit w kecie $|q_0q_1\\dots\\rangle$ (jak w podręcznikach i PennyLane). Qiskit numeruje odwrotnie (little‑endian). Dotknij bramki w obwodzie, aby ją usunąć.') + '</div>';

    function setHint() {
      var h;
      if (TWO[sel]) h = pendingCtrl === null ? 'Dotknij linii kubitu <b>' + (sel === 'SWAP' ? 'pierwszego' : 'kontrolnego') + '</b>.' : 'Teraz dotknij kubitu <b>' + (sel === 'SWAP' ? 'drugiego' : 'docelowego') + '</b> (kontrola: q' + pendingCtrl + ').';
      else h = 'Dotknij linii kubitu, aby dodać bramkę <b>' + (LABEL[sel] || sel) + '</b>.';
      if (n === 1 && TWO[sel]) h = 'Bramki dwukubitowe wymagają co najmniej 2 kubitów.';
      A.$('#hint', el).innerHTML = h;
      A.$('#thrRow', el).style.opacity = ROT[sel] ? 1 : 0.4;
      A.$$('#pal .gate', el).forEach(function (b) { b.classList.toggle('sel', b.getAttribute('data-g') === sel); });
    }

    function gateHtml(op, k, q) {
      var g = op.g;
      var lab = (LABEL[g] || g);
      if (g === 'CNOT') return q === op.c ? '<span class="g ctrl" data-op="' + k + '"></span>' : '<span class="g targ" data-op="' + k + '">+</span>';
      if (g === 'CZ') return '<span class="g ctrl" data-op="' + k + '"></span>';
      if (g === 'SWAP') return '<span class="g swp" data-op="' + k + '">✕</span>';
      if (g === 'CP' && q === op.c) return '<span class="g ctrl" data-op="' + k + '"></span>';
      var rot = ROT[g];
      return '<span class="g' + (rot ? ' rot' : '') + '" data-op="' + k + '">' + lab + (rot ? '<small>' + Q.fmtAngle(op.theta) + '</small>' : '') + '</span>';
    }

    function drawCircuit() {
      var html = '<table>';
      for (var q = 0; q < n; q++) {
        html += '<tr><td class="qlabel" data-q="' + q + '">q' + q + ' |0⟩</td>';
        ops.forEach(function (op, k) {
          var cls = 'wire';
          var inner = '';
          var involved = TWO[op.g] ? [op.c, op.t] : [op.t];
          var lo = Math.min.apply(null, involved), hi = Math.max.apply(null, involved);
          if (involved.indexOf(q) >= 0) inner = gateHtml(op, k, q);
          if (TWO[op.g] && q >= lo && q <= hi) cls += ' vline ' + (q === lo ? 'vtop' : q === hi ? 'vbot' : 'vmid');
          html += '<td class="' + cls + '" data-q="' + q + '">' + inner + '</td>';
        });
        html += '<td class="wire add-col target-hint" data-q="' + q + '"></td><td class="wire add-col" data-q="' + q + '"></td></tr>';
      }
      A.$('#circ', el).innerHTML = html + '</table>';
      var c = A.$('#circ', el);
      c.scrollLeft = c.scrollWidth;
    }

    function simulate() {
      var s = Q.run(n, ops);
      var p = Q.probs(s);
      var html = '';
      for (var i = 0; i < s.dim; i++) {
        var amp = Q.fmtComplex(s.re[i], s.im[i]);
        html += '<div class="prow"><code>' + Q.ket(i, n) + '</code><div class="pb"><i style="width:' + (100 * p[i]).toFixed(2) + '%"></i></div><span>' + (100 * p[i]).toFixed(1) + '%</span>' +
          '<span class="amp">amplituda: ' + amp + (counts ? ' · zliczenia: <b>' + counts[i] + '</b>' : '') + '</span></div>';
      }
      A.$('#probs', el).innerHTML = html;
      var minis = A.$('#minis', el);
      minis.innerHTML = '';
      for (var q = 0; q < n; q++) {
        var b = Q.bloch(s, q);
        var len = Math.sqrt(b.x * b.x + b.y * b.y + b.z * b.z);
        var fig = document.createElement('figure');
        var cv = document.createElement('canvas');
        fig.appendChild(cv);
        var cap = document.createElement('figcaption');
        cap.innerHTML = 'q' + q + ' · |r|=' + len.toFixed(2) + (len < 0.99 ? ' 🔗' : '');
        fig.appendChild(cap);
        minis.appendChild(fig);
        var size = Math.min(110, minis.clientWidth / Math.max(2, n) - 8) || 84;
        cv.style.width = size + 'px';
        var ctx = Lab.setupCanvas(cv, size, size);
        Lab.drawBloch(ctx, size, [b.x, b.y, b.z], { labels: false });
      }
    }

    function refresh() {
      drawCircuit();
      simulate();
      setHint();
      A.$('#note', el).innerHTML = note ? MD.render(note) : '';
    }

    function place(q) {
      counts = null;
      if (TWO[sel]) {
        if (n < 2) return;
        if (pendingCtrl === null) { pendingCtrl = q; setHint(); highlightCtrl(); return; }
        if (pendingCtrl === q) { pendingCtrl = null; setHint(); drawCircuit(); return; }
        var op = { g: sel, c: pendingCtrl, t: q };
        if (ROT[sel]) op.theta = theta;
        ops.push(op);
        pendingCtrl = null;
      } else {
        var o = { g: sel, t: q };
        if (ROT[sel]) o.theta = theta;
        ops.push(o);
      }
      note = '';
      refresh();
    }

    function highlightCtrl() {
      A.$$('#circ td.qlabel', el).forEach(function (td) {
        td.style.color = +td.getAttribute('data-q') === pendingCtrl ? 'var(--accent2)' : '';
        td.style.fontWeight = +td.getAttribute('data-q') === pendingCtrl ? '700' : '';
      });
    }

    A.$('#circ', el).addEventListener('click', function (e) {
      var g = e.target.closest('[data-op]');
      if (g) {
        ops.splice(+g.getAttribute('data-op'), 1);
        counts = null;
        A.toast('Usunięto bramkę');
        refresh();
        return;
      }
      var td = e.target.closest('td[data-q]');
      if (td) place(+td.getAttribute('data-q'));
    });
    A.$$('#pal .gate', el).forEach(function (b) {
      b.addEventListener('click', function () { sel = b.getAttribute('data-g'); pendingCtrl = null; setHint(); drawCircuit(); });
    });
    A.$$('#nq button', el).forEach(function (b) {
      b.addEventListener('click', function () {
        n = +b.getAttribute('data-n');
        A.$$('#nq button', el).forEach(function (x) { x.classList.toggle('on', x === b); });
        ops = ops.filter(function (op) { return op.t < n && (op.c === undefined || op.c < n); });
        pendingCtrl = null; counts = null; note = '';
        refresh();
      });
    });
    A.$('#preset', el).addEventListener('change', function (e) {
      var p = PRESETS[e.target.value];
      if (!p) return;
      n = p.n;
      ops = p.ops.map(function (o) { return Object.assign({}, o); });
      note = p.note;
      counts = null; pendingCtrl = null;
      A.$$('#nq button', el).forEach(function (x) { x.classList.toggle('on', +x.getAttribute('data-n') === n); });
      refresh();
    });
    A.$('#undo', el).addEventListener('click', function () { ops.pop(); counts = null; refresh(); });
    A.$('#clear', el).addEventListener('click', function () { ops = []; counts = null; note = ''; A.$('#preset', el).value = ''; refresh(); });
    A.$('#shots', el).addEventListener('click', function () {
      counts = Q.sample(Q.probs(Q.run(n, ops)), 1024);
      simulate();
      A.toast('Wykonano 1024 pomiary — zobacz zliczenia');
    });
    var th = A.$('#th', el), tho = A.$('#tho', el);
    th.addEventListener('input', function () { theta = +th.value * PI / 180; tho.textContent = Q.fmtAngle(theta); });

    function onResize() { simulate(); }
    window.addEventListener('resize', onResize);
    refresh();
    return function () { window.removeEventListener('resize', onResize); };
  });
})();
