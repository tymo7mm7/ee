/* Rdzeń aplikacji: router, nagłówek, ekran startowy, wyszukiwarka, ustawienia. */
(function () {
  'use strict';

  var C = window.QA_CONTENT || { sections: [], lessons: [], cards: [], quiz: [], cheatsheet: '' };
  var esc = MD.esc;

  // ---------- Indeksy treści ----------
  var lessonById = {}, sectionById = {}, cardsByLesson = {}, quizByLesson = {}, cardById = {};
  C.sections.forEach(function (s) { sectionById[s.id] = s; });
  C.lessons.forEach(function (l, i) { l.index = i; lessonById[l.id] = l; cardsByLesson[l.id] = []; quizByLesson[l.id] = []; });
  C.cards.forEach(function (c) { cardById[c.id] = c; (cardsByLesson[c.lesson] = cardsByLesson[c.lesson] || []).push(c); });
  C.quiz.forEach(function (q) { (quizByLesson[q.lesson] = quizByLesson[q.lesson] || []).push(q); });

  function sectionOf(lessonId) { var l = lessonById[lessonId]; return l ? sectionById[l.section] : null; }
  function cardsOfSection(sid) {
    var s = sectionById[sid];
    return s ? [].concat.apply([], s.lessons.map(function (id) { return cardsByLesson[id] || []; })) : [];
  }
  function quizOfSection(sid) {
    var s = sectionById[sid];
    return s ? [].concat.apply([], s.lessons.map(function (id) { return quizByLesson[id] || []; })) : [];
  }

  // ---------- Narzędzia DOM ----------
  var view = document.getElementById('view');
  var titleEl = document.getElementById('title');
  var backBtn = document.getElementById('btn-back');
  var brand = document.getElementById('brand');

  function $(sel, root) { return (root || document).querySelector(sel); }
  function $$(sel, root) { return Array.prototype.slice.call((root || document).querySelectorAll(sel)); }

  var toastTimer;
  function toast(msg) {
    var t = document.getElementById('toast');
    t.textContent = msg;
    t.hidden = false;
    t.classList.add('show');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(function () { t.classList.remove('show'); setTimeout(function () { t.hidden = true; }, 250); }, 2200);
  }

  function shuffle(a) {
    a = a.slice();
    for (var i = a.length - 1; i > 0; i--) {
      var j = Math.floor(Math.random() * (i + 1));
      var t = a[i]; a[i] = a[j]; a[j] = t;
    }
    return a;
  }

  function pct(a, b) { return b ? Math.round(100 * a / b) : 0; }

  function progressBar(value, color) {
    return '<div class="bar"><i style="width:' + Math.max(0, Math.min(100, value)) + '%;' + (color ? 'background:' + color : '') + '"></i></div>';
  }

  function plural(n, one, few, many) {
    if (n === 1) return one;
    var d = n % 10, h = n % 100;
    if (d >= 2 && d <= 4 && (h < 12 || h > 14)) return few;
    return many;
  }

  // ---------- Motyw ----------
  function applyTheme() {
    var theme = Store.state.settings.theme === 'light' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', theme);
    var meta = document.querySelector('meta[name=theme-color]');
    if (meta) meta.setAttribute('content', theme === 'light' ? '#f4f5fb' : '#0f1226');
    if (window.AndroidApp && AndroidApp.setTheme) AndroidApp.setTheme(theme);
  }

  // ---------- Router ----------
  var routes = [];
  var cleanup = null;
  var scrollPos = {};
  var navStack = [];       // historia przejść w aplikacji (do przycisku Wstecz)
  var replacing = false;
  var currentHash = null;
  var currentParent = '#/';

  function route(pattern, opts, fn) {
    if (typeof opts === 'function') { fn = opts; opts = {}; }
    routes.push({ re: pattern, fn: fn, opts: opts });
  }

  function parseHash() {
    var h = location.hash || '#/';
    var q = {};
    var qi = h.indexOf('?');
    if (qi >= 0) {
      h.slice(qi + 1).split('&').forEach(function (kv) {
        var p = kv.split('=');
        if (p[0]) q[decodeURIComponent(p[0])] = decodeURIComponent(p[1] || '');
      });
      h = h.slice(0, qi);
    }
    return { path: h.replace(/^#/, '') || '/', query: q };
  }

  function header(opts) {
    titleEl.textContent = opts.title || 'QML Akademia';
    var top = !!opts.top;
    backBtn.hidden = top;
    brand.classList.toggle('with-logo', top);
    currentParent = opts.parent || '#/';
    document.title = (opts.title ? opts.title + ' · ' : '') + 'QML Akademia';
    var tab = opts.tab || '';
    $$('#tabbar a').forEach(function (a) { a.classList.toggle('active', a.getAttribute('data-tab') === tab); });
  }

  function render() {
    if (currentHash !== null) scrollPos[currentHash] = window.scrollY;
    if (cleanup) { try { cleanup(); } catch (e) { console.error(e); } cleanup = null; }
    var info = parseHash();
    currentHash = location.hash || '#/';
    var found = null, m = null;
    for (var i = 0; i < routes.length; i++) {
      m = routes[i].re.exec(info.path);
      if (m) { found = routes[i]; break; }
    }
    view.className = '';
    view.innerHTML = '';
    if (!found) { location.replace('#/'); return; }
    try {
      var res = found.fn(view, m.slice(1).map(decodeURIComponent), info.query);
      if (typeof res === 'function') cleanup = res;
    } catch (e) {
      console.error(e);
      view.innerHTML = '<div class="card"><h2>Ups, coś poszło nie tak</h2><p class="muted">' + esc(e.message) + '</p><a class="btn" href="#/">Wróć na start</a></div>';
    }
    var y = found.opts.keepScroll && scrollPos[currentHash] ? scrollPos[currentHash] : 0;
    window.scrollTo(0, y);
    updateBadge();
  }

  function go(hash, replace) {
    if (replace) { replacing = true; location.replace(hash); }
    else location.hash = hash;
  }

  function track() {
    var h = location.hash || '#/';
    if (replacing) { navStack[navStack.length - 1] = h; replacing = false; }
    else if (navStack.length > 1 && navStack[navStack.length - 2] === h) navStack.pop();
    else navStack.push(h);
  }

  window.addEventListener('hashchange', function () { track(); render(); });
  backBtn.addEventListener('click', function () {
    if (navStack.length > 1) history.back(); else go(currentParent, true);
  });
  document.getElementById('btn-search').addEventListener('click', function () { go('#/search'); });
  document.getElementById('btn-more').addEventListener('click', function () { go('#/more'); });
  brand.addEventListener('click', function () { if (brand.classList.contains('with-logo')) go('#/'); });

  // ---------- Fiszki do powtórki ----------
  function dueCards() {
    var now = Date.now();
    return C.cards.filter(function (c) { return Store.isDue(c.id, now); });
  }
  function updateBadge() {
    var n = dueCards().length;
    var b = document.getElementById('due-badge');
    b.hidden = n === 0;
    b.textContent = n > 99 ? '99+' : String(n);
  }

  // ---------- Następna lekcja ----------
  function nextLesson() {
    var last = lessonById[Store.state.lastLesson];
    var order = C.lessons;
    if (last) {
      var sec = sectionById[last.section];
      for (var i = 0; i < sec.lessons.length; i++) if (!Store.lessonDone(sec.lessons[i])) return lessonById[sec.lessons[i]];
    }
    for (var j = 0; j < order.length; j++) if (!Store.lessonDone(order[j].id)) return order[j];
    return null;
  }

  function sectionStats(s) {
    var done = s.lessons.filter(Store.lessonDone).length;
    return { done: done, total: s.lessons.length, pct: pct(done, s.lessons.length) };
  }

  function quizAccuracy() {
    var seen = 0, ok = 0;
    Object.keys(Store.state.quiz).forEach(function (k) { seen += Store.state.quiz[k].seen; ok += Store.state.quiz[k].ok; });
    return { seen: seen, ok: ok, pct: pct(ok, seen) };
  }

  // ---------- Ekran startowy ----------
  route(/^\/$/, { keepScroll: true }, function (el) {
    header({ top: true, tab: 'home' });
    var st = Store.state;
    var due = dueCards().length;
    var done = C.lessons.filter(function (l) { return Store.lessonDone(l.id); }).length;
    var qa = quizAccuracy();
    var streak = Store.streak();
    var next = nextLesson();
    var learned = Object.keys(st.cards).length;
    var hour = new Date().getHours();
    var hello = hour < 5 ? 'Dobranoc' : hour < 12 ? 'Dzień dobry' : hour < 18 ? 'Cześć' : 'Dobry wieczór';
    var tipCard = C.cards.length ? C.cards[Math.floor(Math.random() * C.cards.length)] : null;

    var html = '<section class="hero">' +
      '<div><h1>' + hello + '!</h1><p class="muted">Informatyka kwantowa · QML · uczenie maszynowe</p></div>' +
      '<div class="streak' + (streak ? ' on' : '') + '" title="Dni nauki z rzędu"><b>' + streak + '</b><span>' + plural(streak, 'dzień', 'dni', 'dni') + '<br>z rzędu</span></div>' +
      '</section>';

    html += '<div class="tiles">' +
      '<a class="tile" href="#/cards/run?deck=due"><b>' + due + '</b><span>fiszek do powtórki</span></a>' +
      '<a class="tile" href="#/learn"><b>' + done + '/' + C.lessons.length + '</b><span>lekcji ukończonych</span></a>' +
      '<a class="tile" href="#/quiz"><b>' + (qa.seen ? qa.pct + '%' : '–') + '</b><span>trafności w quizach</span></a>' +
      '</div>';

    if (next) {
      var ns = sectionById[next.section];
      html += '<a class="card continue" href="#/l/' + next.id + '" style="--c:' + ns.color + '">' +
        '<div class="eyebrow">' + (st.lastLesson ? 'Kontynuuj naukę' : 'Zacznij tutaj') + ' · ' + esc(ns.short) + '</div>' +
        '<h3>' + esc(next.title) + '</h3><p class="muted">' + esc(next.summary) + '</p>' +
        '<span class="go">Czytaj · ' + next.minutes + ' min →</span></a>';
    } else if (C.lessons.length) {
      html += '<div class="card"><h3>Wszystkie lekcje ukończone! 🎉</h3><p class="muted">Utrwalaj wiedzę fiszkami i quizami.</p></div>';
    }

    html += '<div class="actions">' +
      '<a class="action" href="#/cards/run?deck=due"><i>🔁</i><span>Powtórka dnia</span></a>' +
      '<a class="action" href="#/quiz/run?n=10"><i>⚡</i><span>Szybki quiz</span></a>' +
      '<a class="action" href="#/s/qml"><i>🧠</i><span>Ścieżka QML</span></a>' +
      '<a class="action" href="#/cheat"><i>📐</i><span>Ściąga wzorów</span></a>' +
      '</div>';

    html += '<h2 class="h-sec">Działy</h2><div class="list">';
    C.sections.forEach(function (s) {
      var ss = sectionStats(s);
      html += '<a class="row section-row" href="#/s/' + s.id + '" style="--c:' + s.color + '">' +
        '<span class="sec-icon">' + s.icon + '</span>' +
        '<span class="grow"><b>' + esc(s.title) + '</b><small>' + ss.done + '/' + ss.total + ' lekcji</small>' + progressBar(ss.pct, s.color) + '</span></a>';
    });
    html += '</div>';

    html += '<h2 class="h-sec">Laboratorium</h2><div class="lab-strip">' +
      '<a href="#/lab/bloch">🌐<span>Sfera Blocha</span></a>' +
      '<a href="#/lab/circuit">🔌<span>Obwody</span></a>' +
      '<a href="#/lab/train">🤖<span>VQC vs ML</span></a>' +
      '<a href="#/lab/kernel">🧩<span>Kernele</span></a>' +
      '<a href="#/lab/shift">📈<span>Parameter‑shift</span></a>' +
      '</div>';

    if (tipCard) {
      var sec = sectionOf(tipCard.lesson);
      html += '<h2 class="h-sec">Sprawdź się</h2><div class="card tip" id="tip"><div class="eyebrow" style="color:' + sec.color + '">' + esc(sec.short) + '</div>' +
        '<div class="md">' + MD.render(tipCard.q) + '</div><div class="tip-answer md" hidden>' + MD.render(tipCard.a) + '</div>' +
        '<button class="btn ghost small" id="tip-show">Pokaż odpowiedź</button></div>';
    }

    html += '<p class="muted small center foot">Nauczono się ' + learned + ' z ' + C.cards.length + ' fiszek · ' + C.quiz.length + ' pytań w bazie quizu</p>';
    el.innerHTML = html;
    var tipBtn = $('#tip-show', el);
    if (tipBtn) tipBtn.addEventListener('click', function () {
      $('.tip-answer', el).hidden = false;
      tipBtn.hidden = true;
    });
  });

  // ---------- Wyszukiwarka ----------
  route(/^\/search$/, function (el, _, q) {
    header({ title: 'Szukaj', parent: '#/' });
    el.innerHTML = '<div class="search-box"><input type="search" id="q" placeholder="np. splątanie, kernel, gradient…" autocomplete="off" value="' + esc(q.q || '') + '"></div><div id="results"></div>';
    var input = $('#q', el), results = $('#results', el);
    var lessonText = C.lessons.map(function (l) { return (l.title + ' ' + l.summary + ' ' + MD.plain(l.body)).toLowerCase(); });
    function norm(s) { return s.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/ł/g, 'l'); }
    var lessonNorm = lessonText.map(norm);
    var cardNorm = C.cards.map(function (c) { return norm(c.q + ' ' + MD.plain(c.a)); });

    function snippet(text, term) {
      var t = MD.plain(text);
      var i = norm(t).indexOf(term);
      if (i < 0) return esc(t.slice(0, 110)) + '…';
      var a = Math.max(0, i - 50);
      return (a ? '…' : '') + esc(t.slice(a, i)) + '<mark>' + esc(t.slice(i, i + term.length)) + '</mark>' + esc(t.slice(i + term.length, i + term.length + 70)) + '…';
    }

    function doSearch() {
      var term = norm(input.value.trim());
      history.replaceState(null, '', '#/search' + (term ? '?q=' + encodeURIComponent(input.value.trim()) : ''));
      if (term.length < 2) { results.innerHTML = '<p class="muted center">Wpisz co najmniej 2 znaki.</p>'; return; }
      var html = '';
      var ls = [];
      C.lessons.forEach(function (l, i) {
        var idx = lessonNorm[i].indexOf(term);
        if (idx >= 0) ls.push({ l: l, score: (norm(l.title).indexOf(term) >= 0 ? 0 : 1) });
      });
      ls.sort(function (a, b) { return a.score - b.score; });
      if (ls.length) {
        html += '<h2 class="h-sec">Lekcje (' + ls.length + ')</h2><div class="list">';
        ls.slice(0, 30).forEach(function (r) {
          var s = sectionById[r.l.section];
          html += '<a class="row" href="#/l/' + r.l.id + '" style="--c:' + s.color + '"><span class="dot"></span><span class="grow"><b>' + esc(r.l.title) + '</b><small>' + snippet(r.l.body, term) + '</small></span></a>';
        });
        html += '</div>';
      }
      var cs = C.cards.filter(function (c, i) { return cardNorm[i].indexOf(term) >= 0; });
      if (cs.length) {
        html += '<h2 class="h-sec">Fiszki (' + cs.length + ')</h2><div class="list">';
        cs.slice(0, 40).forEach(function (c) {
          html += '<details class="row card-result"><summary><span class="grow md">' + MD.inline(c.q) + '</span></summary><div class="md">' + MD.render(c.a) + '</div>' +
            '<a class="small" href="#/l/' + c.lesson + '">→ ' + esc(lessonById[c.lesson].title) + '</a></details>';
        });
        html += '</div>';
      }
      results.innerHTML = html || '<p class="muted center">Brak wyników dla „' + esc(input.value) + '”.</p>';
    }
    var timer;
    input.addEventListener('input', function () { clearTimeout(timer); timer = setTimeout(doSearch, 160); });
    if (q.q) doSearch(); else results.innerHTML = '<p class="muted center">Przeszukuje lekcje i fiszki.</p>';
    setTimeout(function () { input.focus(); }, 50);
  });

  // ---------- Więcej / ustawienia ----------
  route(/^\/more$/, function (el) {
    header({ title: 'Więcej', parent: '#/' });
    var st = Store.state;
    var light = st.settings.theme === 'light';
    var html = '<div class="list">' +
      '<a class="row" href="#/cheat"><span class="sec-icon">📐</span><span class="grow"><b>Ściąga ze wzorów</b><small>Najważniejsze wzory w jednym miejscu</small></span></a>' +
      '<a class="row" href="#/stats"><span class="sec-icon">📊</span><span class="grow"><b>Statystyki</b><small>Postęp w działach, słabe punkty</small></span></a>' +
      '<a class="row" href="#/search"><span class="sec-icon">🔎</span><span class="grow"><b>Szukaj</b><small>Lekcje i fiszki</small></span></a>' +
      '</div>';
    html += '<h2 class="h-sec">Ustawienia</h2><div class="card settings">' +
      '<label class="switch-row"><span>Jasny motyw</span><input type="checkbox" id="theme"' + (light ? ' checked' : '') + '></label>' +
      '<label class="switch-row"><span>Nowe fiszki dziennie</span><select id="newPerDay">' +
      [5, 10, 20, 30, 50, 100].map(function (n) { return '<option' + (n === st.settings.newPerDay ? ' selected' : '') + '>' + n + '</option>'; }).join('') +
      '</select></label></div>';
    html += '<h2 class="h-sec">Kopia postępów</h2><div class="card">' +
      '<p class="muted small">Postęp zapisuje się automatycznie na urządzeniu. Możesz go skopiować (np. przed reinstalacją) i wkleić z powrotem.</p>' +
      '<div class="btn-row"><button class="btn ghost" id="exp">Eksportuj</button><button class="btn ghost" id="imp">Importuj</button></div>' +
      '<textarea id="io" rows="4" placeholder="Tu pojawi się / wklej kopię postępów (JSON)" hidden></textarea>' +
      '<button class="btn" id="imp-go" hidden>Wczytaj kopię</button></div>';
    html += '<h2 class="h-sec">Strefa ryzyka</h2><div class="card"><button class="btn danger" id="reset">Wyzeruj cały postęp</button></div>';
    html += '<div class="card about"><h3>O aplikacji</h3><p class="muted small">QML Akademia 1.0 — aplikacja do nauki i powtórek: informatyka kwantowa, algorytmy kwantowe, kwantowe uczenie maszynowe (QML) i klasyczne ML. ' +
      C.lessons.length + ' lekcji, ' + C.cards.length + ' fiszek, ' + C.quiz.length + ' pytań. Działa w pełni offline.</p>' +
      '<p class="muted small">Wzory renderuje KaTeX (licencja MIT). Symulator kwantowy, trening modeli i powtórki (algorytm SM‑2) działają lokalnie na telefonie.</p></div>';
    el.innerHTML = html;

    $('#theme', el).addEventListener('change', function (e) {
      st.settings.theme = e.target.checked ? 'light' : 'dark';
      Store.save();
      applyTheme();
    });
    $('#newPerDay', el).addEventListener('change', function (e) {
      st.settings.newPerDay = +e.target.value;
      Store.save();
    });
    var io = $('#io', el), impGo = $('#imp-go', el);
    $('#exp', el).addEventListener('click', function () {
      var data = Store.exportData();
      io.hidden = false;
      impGo.hidden = true;
      io.value = data;
      io.select();
      if (window.AndroidApp && AndroidApp.shareText) { AndroidApp.copyText(data); }
      else if (navigator.clipboard) { navigator.clipboard.writeText(data).then(function () { toast('Skopiowano do schowka'); }, function () {}); }
    });
    $('#imp', el).addEventListener('click', function () {
      io.hidden = false;
      impGo.hidden = false;
      io.value = '';
      io.focus();
    });
    impGo.addEventListener('click', function () {
      try {
        Store.importData(io.value.trim());
        applyTheme();
        toast('Wczytano postęp');
        go('#/');
      } catch (e) {
        toast('Nie udało się: ' + e.message);
      }
    });
    $('#reset', el).addEventListener('click', function () {
      if (confirm('Na pewno usunąć cały postęp (lekcje, fiszki, quizy)?')) {
        Store.reset();
        toast('Postęp wyzerowany');
        go('#/');
      }
    });
  });

  // ---------- Statystyki ----------
  route(/^\/stats$/, function (el) {
    header({ title: 'Statystyki', parent: '#/more' });
    var st = Store.state;
    var html = '<div class="tiles">' +
      '<div class="tile"><b>' + Store.streak() + '</b><span>dni z rzędu</span></div>' +
      '<div class="tile"><b>' + Object.keys(st.days).length + '</b><span>dni nauki łącznie</span></div>' +
      '<div class="tile"><b>' + Object.keys(st.cards).length + '</b><span>fiszek w powtórkach</span></div></div>';
    // kalendarz ostatnich 10 tygodni
    html += '<h2 class="h-sec">Aktywność (10 tygodni)</h2><div class="card"><div class="heat">';
    var DAY = 864e5, now = Date.now();
    for (var i = 69; i >= 0; i--) {
      var d = Store.today(now - i * DAY), n = st.days[d] || 0;
      var lvl = n === 0 ? 0 : n < 5 ? 1 : n < 15 ? 2 : n < 40 ? 3 : 4;
      html += '<i class="l' + lvl + '" title="' + d + ': ' + n + '"></i>';
    }
    html += '</div></div>';
    html += '<h2 class="h-sec">Działy</h2><div class="list">';
    C.sections.forEach(function (s) {
      var ss = sectionStats(s);
      var cards = cardsOfSection(s.id), learned = cards.filter(function (c) { return st.cards[c.id]; }).length;
      var qs = quizOfSection(s.id), seen = 0, ok = 0;
      qs.forEach(function (q) { var r = st.quiz[q.id]; if (r) { seen += r.seen; ok += r.ok; } });
      html += '<div class="row stat-row" style="--c:' + s.color + '"><span class="sec-icon">' + s.icon + '</span><span class="grow"><b>' + esc(s.title) + '</b>' +
        '<small>Lekcje ' + ss.done + '/' + ss.total + ' · fiszki ' + learned + '/' + cards.length + ' · quiz ' + (seen ? pct(ok, seen) + '% (' + seen + ' odp.)' : '–') + '</small>' +
        progressBar(ss.pct, s.color) + '</span></div>';
    });
    html += '</div>';
    // najtrudniejsze fiszki
    var hard = C.cards.filter(function (c) { return st.cards[c.id] && st.cards[c.id].lapses > 0; })
      .sort(function (a, b) { return st.cards[b.id].lapses - st.cards[a.id].lapses; }).slice(0, 8);
    if (hard.length) {
      html += '<h2 class="h-sec">Najtrudniejsze fiszki</h2><div class="list">';
      hard.forEach(function (c) {
        html += '<details class="row card-result"><summary><span class="grow md">' + MD.inline(c.q) + '</span><span class="pill">' + st.cards[c.id].lapses + '×</span></summary><div class="md">' + MD.render(c.a) + '</div></details>';
      });
      html += '</div>';
    }
    var wrong = C.quiz.filter(function (q) { var r = st.quiz[q.id]; return r && r.last === 0; });
    if (wrong.length) {
      html += '<a class="btn block" href="#/quiz/run?mode=wrong">Powtórz ' + wrong.length + ' ' + plural(wrong.length, 'błędne pytanie', 'błędne pytania', 'błędnych pytań') + '</a>';
    }
    el.innerHTML = html;
  });

  // ---------- Start ----------
  function start() {
    applyTheme();
    navStack.push(location.hash || '#/');
    render();
  }

  window.App = {
    C: C,
    lessonById: lessonById,
    sectionById: sectionById,
    cardsByLesson: cardsByLesson,
    quizByLesson: quizByLesson,
    cardById: cardById,
    cardsOfSection: cardsOfSection,
    quizOfSection: quizOfSection,
    sectionOf: sectionOf,
    route: route,
    header: header,
    go: go,
    toast: toast,
    shuffle: shuffle,
    pct: pct,
    plural: plural,
    progressBar: progressBar,
    dueCards: dueCards,
    updateBadge: updateBadge,
    $: $,
    $$: $$,
    start: start
  };
})();
