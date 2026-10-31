/* Widoki nauki: lista działów, dział, lekcja, ściąga. */
(function () {
  'use strict';
  var A = App, C = App.C, esc = MD.esc;

  var LEVELS = { podstawowy: 'podstawowy', 'średni': 'średni', zaawansowany: 'zaawansowany' };
  var LAB_NAMES = {
    bloch: 'Sfera Blocha', circuit: 'Symulator obwodów', train: 'Trening: VQC vs klasyczne ML',
    shift: 'Reguła parameter-shift', kernel: 'Kernele kwantowe'
  };

  // ---------- Lista działów ----------
  A.route(/^\/learn$/, { keepScroll: true }, function (el) {
    A.header({ title: 'Nauka', top: false, tab: 'learn', parent: '#/' });
    var html = '<p class="muted intro">Wybierz dział. Każda lekcja kończy się fiszkami i mini‑quizem. Dział <b>QML</b> jest najobszerniejszy.</p>';
    C.sections.forEach(function (s) {
      var done = s.lessons.filter(Store.lessonDone).length;
      var nCards = A.cardsOfSection(s.id).length, nQuiz = A.quizOfSection(s.id).length;
      html += '<a class="card section-card' + (s.id === 'qml' ? ' featured' : '') + '" href="#/s/' + s.id + '" style="--c:' + s.color + '">' +
        '<div class="sc-head"><span class="sec-icon big">' + s.icon + '</span><div><h3>' + esc(s.title) + '</h3>' +
        '<small class="muted">' + s.lessons.length + ' lekcji · ' + nCards + ' fiszek · ' + nQuiz + ' pytań</small></div></div>' +
        '<div class="md muted small">' + MD.render(s.desc) + '</div>' +
        A.progressBar(A.pct(done, s.lessons.length), s.color) +
        '<small class="muted">' + done + '/' + s.lessons.length + ' ukończonych</small></a>';
    });
    el.innerHTML = html;
  });

  // ---------- Dział ----------
  A.route(/^\/s\/([\w-]+)$/, { keepScroll: true }, function (el, p) {
    var s = A.sectionById[p[0]];
    if (!s) { A.go('#/learn', true); return; }
    A.header({ title: s.short, tab: 'learn', parent: '#/learn' });
    var done = s.lessons.filter(Store.lessonDone).length;
    var html = '<section class="sec-hero" style="--c:' + s.color + '"><span class="sec-icon big">' + s.icon + '</span>' +
      '<div><h1>' + esc(s.title) + '</h1><div class="md muted">' + MD.render(s.desc) + '</div></div></section>' +
      A.progressBar(A.pct(done, s.lessons.length), s.color) +
      '<div class="btn-row">' +
      '<a class="btn" href="#/cards/run?section=' + s.id + '">🃏 Fiszki działu</a>' +
      '<a class="btn ghost" href="#/quiz/run?section=' + s.id + '&n=10">❓ Quiz działu</a></div>';
    html += '<ol class="lesson-list">';
    s.lessons.forEach(function (id, i) {
      var l = A.lessonById[id];
      var d = Store.lessonDone(id);
      html += '<li><a class="row lesson-row' + (d ? ' done' : '') + '" href="#/l/' + id + '" style="--c:' + s.color + '">' +
        '<span class="num">' + (d ? '✓' : i + 1) + '</span>' +
        '<span class="grow"><b>' + esc(l.title) + '</b><small>' + esc(l.summary) + '</small>' +
        '<small class="meta">' + l.minutes + ' min · ' + (LEVELS[l.level] || l.level) +
        (l.nCards ? ' · ' + l.nCards + ' fiszek' : '') + (l.lab ? ' · 🧪 lab' : '') + '</small></span></a></li>';
    });
    html += '</ol>';
    el.innerHTML = html;
  });

  // ---------- Lekcja ----------
  A.route(/^\/l\/([\w-]+)$/, { keepScroll: true }, function (el, p) {
    var l = A.lessonById[p[0]];
    if (!l) { A.go('#/learn', true); return; }
    var s = A.sectionById[l.section];
    A.header({ title: s.short, tab: 'learn', parent: '#/s/' + s.id });
    Store.state.lastLesson = l.id;
    Store.save();

    var pos = s.lessons.indexOf(l.id);
    var prev = pos > 0 ? A.lessonById[s.lessons[pos - 1]] : null;
    var next = pos < s.lessons.length - 1 ? A.lessonById[s.lessons[pos + 1]] : null;
    var done = Store.lessonDone(l.id);

    var html = '<div class="read-progress"><i id="rp" style="background:' + s.color + '"></i></div>' +
      '<article class="lesson" style="--c:' + s.color + '">' +
      '<div class="eyebrow">' + s.icon + ' ' + esc(s.short) + ' · lekcja ' + (pos + 1) + '/' + s.lessons.length + ' · ' + l.minutes + ' min · ' + (LEVELS[l.level] || l.level) + '</div>' +
      '<h1>' + esc(l.title) + '</h1>' +
      (l.summary ? '<p class="lead">' + esc(l.summary) + '</p>' : '') +
      '<div class="md">' + MD.render(l.body) + '</div>';
    if (l.lab && LAB_NAMES[l.lab]) {
      html += '<a class="card lab-link" href="#/lab/' + l.lab + '"><span>🧪</span><span class="grow"><b>Wypróbuj w laboratorium</b><small>' + LAB_NAMES[l.lab] + '</small></span><span>→</span></a>';
    }
    html += '<div id="end-marker"></div></article>';
    html += '<div class="lesson-foot">' +
      '<button class="btn ' + (done ? 'ok' : 'ghost') + ' block" id="done-btn">' + (done ? '✓ Lekcja ukończona' : 'Oznacz jako ukończoną') + '</button>' +
      '<div class="btn-row">' +
      (l.nCards ? '<a class="btn" href="#/cards/run?lesson=' + l.id + '">🃏 Fiszki (' + l.nCards + ')</a>' : '') +
      (l.nQuiz ? '<a class="btn" href="#/quiz/run?lesson=' + l.id + '">❓ Mini‑quiz (' + l.nQuiz + ')</a>' : '') +
      '</div><div class="pager">' +
      (prev ? '<a class="row" href="#/l/' + prev.id + '"><span class="grow"><small>← Poprzednia</small><b>' + esc(prev.title) + '</b></span></a>' : '<span></span>') +
      (next ? '<a class="row right" href="#/l/' + next.id + '"><span class="grow"><small>Następna →</small><b>' + esc(next.title) + '</b></span></a>' : '<a class="row right" href="#/s/' + s.id + '"><span class="grow"><small>Koniec działu</small><b>Wróć do listy</b></span></a>') +
      '</div></div>';
    el.innerHTML = html;

    var btn = A.$('#done-btn', el);
    function setDone(v, silent) {
      Store.setLessonDone(l.id, v);
      btn.className = 'btn block ' + (v ? 'ok' : 'ghost');
      btn.textContent = v ? '✓ Lekcja ukończona' : 'Oznacz jako ukończoną';
      if (v && !silent) A.toast('Lekcja ukończona ✓');
    }
    btn.addEventListener('click', function () { setDone(!Store.lessonDone(l.id)); });

    // Pasek postępu czytania + automatyczne ukończenie po dotarciu do końca
    var rp = A.$('#rp', el);
    var marker = A.$('#end-marker', el);
    var autoDone = Store.lessonDone(l.id);
    function onScroll() {
      var h = document.documentElement.scrollHeight - window.innerHeight;
      rp.style.width = (h > 0 ? Math.min(100, 100 * window.scrollY / h) : 100) + '%';
      if (!autoDone) {
        var r = marker.getBoundingClientRect();
        if (r.top < window.innerHeight - 40 && window.scrollY > 200) {
          autoDone = true;
          setDone(true);
        }
      }
    }
    window.addEventListener('scroll', onScroll, { passive: true });
    setTimeout(onScroll, 50);
    return function () { window.removeEventListener('scroll', onScroll); };
  });

  // ---------- Ściąga ----------
  A.route(/^\/cheat$/, { keepScroll: true }, function (el) {
    A.header({ title: 'Ściąga ze wzorami', parent: '#/' });
    var src = C.cheatsheet || '';
    var heads = [];
    src.replace(/^##\s+(.+)$/gm, function (_, t) { heads.push(t); return _; });
    var html = '<div class="chips toc">' + heads.map(function (h, i) { return '<button class="chip" data-i="' + i + '">' + MD.inline(h) + '</button>'; }).join('') + '</div>';
    html += '<article class="lesson cheat"><div class="md">' + MD.render(src) + '</div></article>';
    el.innerHTML = html;
    var hs = A.$$('.cheat h3', el);
    A.$$('.toc .chip', el).forEach(function (b) {
      b.addEventListener('click', function () {
        var h = hs[+b.getAttribute('data-i')];
        if (h) window.scrollTo({ top: h.getBoundingClientRect().top + window.scrollY - 70, behavior: 'smooth' });
      });
    });
  });
})();
