/* Fiszki (powtórki rozłożone w czasie) i quizy. */
(function () {
  'use strict';
  var A = App, C = App.C, esc = MD.esc;

  function secChip(lessonId) {
    var s = A.sectionOf(lessonId);
    return s ? '<span class="chip-tag" style="--c:' + s.color + '">' + s.icon + ' ' + esc(s.short) + '</span>' : '';
  }

  // =====================================================================
  // FISZKI
  // =====================================================================
  A.route(/^\/cards$/, { keepScroll: true }, function (el) {
    A.header({ title: 'Fiszki', tab: 'cards', parent: '#/' });
    var now = Date.now();
    var due = A.dueCards().length;
    var newLeft = Store.newLeftToday();
    var html = '<div class="card big-cta">' +
      '<div><h2>' + due + ' ' + A.plural(due, 'fiszka', 'fiszki', 'fiszek') + ' do powtórki</h2>' +
      '<p class="muted small">+ do ' + newLeft + ' nowych dzisiaj (zmienisz w Ustawieniach)</p></div>' +
      '<a class="btn" href="#/cards/run?deck=due">Rozpocznij powtórkę</a></div>';
    html += '<p class="muted small">Oceniaj szczerze, jak dobrze pamiętasz odpowiedź. Algorytm SM‑2 zaplanuje kolejną powtórkę: trudne karty wrócą szybciej, łatwe — za kilka dni lub tygodni.</p>';
    html += '<h2 class="h-sec">Talie według działów</h2><div class="list">';
    C.sections.forEach(function (s) {
      var cards = A.cardsOfSection(s.id);
      var d = cards.filter(function (c) { return Store.isDue(c.id, now); }).length;
      var learned = cards.filter(function (c) { return Store.cardState(c.id); }).length;
      html += '<div class="row deck-row" style="--c:' + s.color + '"><span class="sec-icon">' + s.icon + '</span>' +
        '<a class="grow" href="#/cards/run?section=' + s.id + '"><b>' + esc(s.title) + '</b>' +
        '<small>' + d + ' do powtórki · ' + (cards.length - learned) + ' nowych · ' + cards.length + ' łącznie</small>' +
        A.progressBar(A.pct(learned, cards.length), s.color) + '</a>' +
        '<a class="icon-link" href="#/cards/browse?section=' + s.id + '" aria-label="Przeglądaj">☰</a></div>';
    });
    html += '</div>';
    html += '<div class="btn-row"><a class="btn ghost" href="#/cards/run?deck=all">🎲 Losowe 20 (wkuwanie)</a>' +
      '<a class="btn ghost" href="#/cards/browse">☰ Przeglądaj wszystkie</a></div>';
    el.innerHTML = html;
  });

  // Przeglądanie fiszek jako lista
  A.route(/^\/cards\/browse$/, { keepScroll: true }, function (el, _, q) {
    var s = q.section ? A.sectionById[q.section] : null;
    A.header({ title: s ? 'Fiszki: ' + s.short : 'Wszystkie fiszki', tab: 'cards', parent: '#/cards' });
    var lessons = s ? s.lessons : C.lessons.map(function (l) { return l.id; });
    var html = '<div class="btn-row"><button class="btn ghost small" id="open-all">Rozwiń wszystkie</button></div>';
    lessons.forEach(function (lid) {
      var cards = A.cardsByLesson[lid] || [];
      if (!cards.length) return;
      html += '<h2 class="h-sec">' + esc(A.lessonById[lid].title) + '</h2><div class="list">';
      cards.forEach(function (c) {
        html += '<details class="row card-result"><summary><span class="grow md">' + MD.inline(c.q) + '</span></summary><div class="md">' + MD.render(c.a) + '</div></details>';
      });
      html += '</div>';
    });
    el.innerHTML = html;
    A.$('#open-all', el).addEventListener('click', function (e) {
      var open = e.target.textContent.indexOf('Rozwiń') === 0;
      A.$$('details', el).forEach(function (d) { d.open = open; });
      e.target.textContent = open ? 'Zwiń wszystkie' : 'Rozwiń wszystkie';
    });
  });

  // Sesja powtórek
  A.route(/^\/cards\/run$/, function (el, _, q) {
    var now = Date.now();
    var title = 'Powtórka';
    var parent = '#/cards';
    var queue = [];
    var newLeft = Store.newLeftToday();

    function addNew(pool) {
      pool.forEach(function (c) {
        if (!Store.cardState(c.id) && newLeft > 0) { queue.push(c); newLeft--; }
      });
    }

    if (q.lesson && A.lessonById[q.lesson]) {
      var l = A.lessonById[q.lesson];
      title = 'Fiszki: ' + l.title;
      parent = '#/l/' + l.id;
      queue = (A.cardsByLesson[l.id] || []).slice();
    } else if (q.section && A.sectionById[q.section]) {
      var s = A.sectionById[q.section];
      title = 'Fiszki: ' + s.short;
      var pool = A.cardsOfSection(s.id);
      queue = A.shuffle(pool.filter(function (c) { return Store.isDue(c.id, now); }));
      addNew(pool);
      if (!queue.length) queue = A.shuffle(pool).slice(0, 15); // wszystko powtórzone - tryb treningu
    } else if (q.deck === 'all') {
      title = 'Losowe fiszki';
      queue = A.shuffle(C.cards).slice(0, 20);
    } else {
      title = 'Powtórka dnia';
      queue = A.shuffle(A.dueCards());
      // nowe karty: po kolei według lekcji, z pierwszeństwem dla ostatnio czytanego działu
      var lastSec = A.sectionOf(Store.state.lastLesson);
      var ordered = C.cards.slice();
      if (lastSec) ordered.sort(function (a, b) { return (A.sectionOf(a.lesson) === lastSec ? 0 : 1) - (A.sectionOf(b.lesson) === lastSec ? 0 : 1); });
      // nowe karty tylko z lekcji już przeczytanych, a jeśli takich brak - z dowolnych
      var read = ordered.filter(function (c) { return Store.lessonDone(c.lesson); });
      addNew(read.length ? read : ordered);
    }
    A.header({ title: title, tab: 'cards', parent: parent });

    var total = queue.length;
    var reviewed = 0, good = 0, again = 0;
    var current = null, flipped = false;

    if (!total) {
      el.innerHTML = '<div class="card center empty"><div class="big-emoji">🎉</div><h2>Brak fiszek na teraz</h2>' +
        '<p class="muted">Wszystkie powtórki na dziś zrobione (lub limit nowych kart wyczerpany).</p>' +
        '<div class="btn-row center"><a class="btn" href="#/cards/run?deck=all">Losowe 20</a><a class="btn ghost" href="#/learn">Czytaj lekcje</a></div></div>';
      return;
    }

    function show() {
      if (!queue.length) return finish();
      current = queue.shift();
      flipped = false;
      var done = total - queue.length - 1;
      var cs = Store.cardState(current.id);
      el.innerHTML = '<div class="session-top"><span>' + Math.min(done + 1, total) + ' / ' + total + '</span>' + A.progressBar(A.pct(done, total)) + '</div>' +
        '<div class="flashcard" id="fc">' +
        '<div class="fc-meta">' + secChip(current.lesson) + (cs ? '' : '<span class="chip-tag new">nowa</span>') + '</div>' +
        '<div class="fc-q md">' + MD.render(current.q) + '</div>' +
        '<div class="fc-a md" hidden>' + MD.render(current.a) + '</div>' +
        '<div class="fc-hint muted small">Dotknij, aby zobaczyć odpowiedź</div></div>' +
        '<div class="grade-bar" id="grades" hidden>' +
        grade(0, 'Nie pamiętam', 'again') + grade(1, 'Trudne', 'hard') + grade(2, 'Dobrze', 'good') + grade(3, 'Łatwe', 'easy') +
        '</div><div class="center"><button class="btn block" id="flip">Pokaż odpowiedź</button></div>' +
        '<p class="center small"><a href="#/l/' + current.lesson + '" class="muted">Lekcja: ' + esc(A.lessonById[current.lesson].title) + '</a></p>';
      A.$('#fc', el).addEventListener('click', flip);
      A.$('#flip', el).addEventListener('click', flip);
      A.$$('#grades button', el).forEach(function (b) {
        b.addEventListener('click', function () { rate(+b.getAttribute('data-g')); });
      });
    }

    function grade(g, label, cls) {
      return '<button class="grade ' + cls + '" data-g="' + g + '"><b>' + label + '</b><small>' + Store.previewInterval(current.id, g) + '</small></button>';
    }

    function flip() {
      if (flipped) return;
      flipped = true;
      A.$('.fc-a', el).hidden = false;
      A.$('.fc-hint', el).hidden = true;
      A.$('#fc', el).classList.add('flipped');
      A.$('#grades', el).hidden = false;
      A.$('#flip', el).parentNode.hidden = true;
    }

    function rate(g) {
      Store.gradeCard(current.id, g);
      reviewed++;
      if (g === 0) {
        again++;
        queue.splice(Math.min(queue.length, 3 + Math.floor(Math.random() * 3)), 0, current);
        total++;
      } else {
        good++;
      }
      show();
    }

    function finish() {
      A.updateBadge();
      var nextDue = Object.keys(Store.state.cards).map(function (k) { return Store.state.cards[k].due; })
        .filter(function (d) { return d > Date.now(); }).sort(function (a, b) { return a - b; })[0];
      var when = nextDue ? new Date(nextDue).toLocaleDateString('pl-PL', { weekday: 'long', day: 'numeric', month: 'long' }) : '';
      el.innerHTML = '<div class="card center empty"><div class="big-emoji">✅</div><h2>Sesja zakończona</h2>' +
        '<p>Ocenione karty: <b>' + reviewed + '</b> · zapamiętane: <b>' + good + '</b> · do poprawy: <b>' + again + '</b></p>' +
        (when ? '<p class="muted small">Następna powtórka: ' + when + '</p>' : '') +
        '<div class="btn-row center"><a class="btn" href="#/cards">Talie</a><a class="btn ghost" href="#/quiz/run?n=10">Szybki quiz</a></div></div>';
    }

    function onKey(e) {
      if (e.target && /INPUT|TEXTAREA|SELECT/.test(e.target.tagName)) return;
      if (!flipped && (e.key === ' ' || e.key === 'Enter')) { e.preventDefault(); flip(); }
      else if (flipped && /^[1-4]$/.test(e.key)) rate(+e.key - 1);
    }
    document.addEventListener('keydown', onKey);
    show();
    return function () { document.removeEventListener('keydown', onKey); };
  });

  // =====================================================================
  // QUIZ
  // =====================================================================
  var lastWrong = [];

  A.route(/^\/quiz$/, function (el) {
    A.header({ title: 'Quiz', tab: 'quiz', parent: '#/' });
    var st = Store.state;
    var html = '<p class="muted intro">Pytania wielokrotnego wyboru z wyjaśnieniami. Wybierz działy i tryb.</p>';
    html += '<div class="card"><h3>Działy</h3><div class="chips" id="secs">';
    C.sections.forEach(function (s) {
      var n = A.quizOfSection(s.id).length;
      html += '<button class="chip on" data-s="' + s.id + '" style="--c:' + s.color + '">' + s.icon + ' ' + esc(s.short) + ' <small>' + n + '</small></button>';
    });
    html += '</div><h3>Liczba pytań</h3><div class="chips single" id="num">' +
      [5, 10, 20, 40].map(function (n) { return '<button class="chip' + (n === 10 ? ' on' : '') + '" data-n="' + n + '">' + n + '</button>'; }).join('') +
      '<button class="chip" data-n="0">wszystkie</button></div>' +
      '<h3>Tryb</h3><div class="chips single" id="mode">' +
      '<button class="chip on" data-m="random">🎲 Losowe</button>' +
      '<button class="chip" data-m="new">✨ Niewidziane</button>' +
      '<button class="chip" data-m="wrong">🎯 Moje błędy</button></div>' +
      '<button class="btn block" id="start">Rozpocznij quiz</button></div>';

    html += '<h2 class="h-sec">Wyniki w działach</h2><div class="list">';
    C.sections.forEach(function (s) {
      var qs = A.quizOfSection(s.id), seen = 0, ok = 0, uniq = 0;
      qs.forEach(function (x) { var r = st.quiz[x.id]; if (r) { seen += r.seen; ok += r.ok; uniq++; } });
      html += '<a class="row" href="#/quiz/run?section=' + s.id + '&n=10" style="--c:' + s.color + '"><span class="sec-icon">' + s.icon + '</span><span class="grow"><b>' + esc(s.title) + '</b>' +
        '<small>' + (seen ? 'Trafność ' + A.pct(ok, seen) + '% · ' : '') + uniq + '/' + qs.length + ' pytań widzianych</small>' + A.progressBar(seen ? A.pct(ok, seen) : 0, s.color) + '</span></a>';
    });
    html += '</div>';
    el.innerHTML = html;

    A.$$('#secs .chip', el).forEach(function (b) { b.addEventListener('click', function () { b.classList.toggle('on'); }); });
    ['#num', '#mode'].forEach(function (id) {
      A.$$(id + ' .chip', el).forEach(function (b) {
        b.addEventListener('click', function () {
          A.$$(id + ' .chip', el).forEach(function (x) { x.classList.remove('on'); });
          b.classList.add('on');
        });
      });
    });
    A.$('#start', el).addEventListener('click', function () {
      var secs = A.$$('#secs .chip.on', el).map(function (b) { return b.getAttribute('data-s'); });
      if (!secs.length) { A.toast('Wybierz co najmniej jeden dział'); return; }
      var n = A.$('#num .chip.on', el).getAttribute('data-n');
      var m = A.$('#mode .chip.on', el).getAttribute('data-m');
      A.go('#/quiz/run?sections=' + secs.join(',') + '&n=' + n + '&mode=' + m);
    });
  });

  A.route(/^\/quiz\/run$/, function (el, _, q) {
    var pool, title = 'Quiz', parent = '#/quiz';
    var st = Store.state;
    if (q.lesson && A.lessonById[q.lesson]) {
      pool = (A.quizByLesson[q.lesson] || []).slice();
      title = 'Mini‑quiz';
      parent = '#/l/' + q.lesson;
    } else if (q.mode === 'lastwrong') {
      pool = lastWrong.slice();
      title = 'Poprawa błędów';
    } else {
      var secs = q.section ? [q.section] : q.sections ? q.sections.split(',') : C.sections.map(function (s) { return s.id; });
      pool = [].concat.apply([], secs.map(A.quizOfSection));
      if (q.section) title = 'Quiz: ' + A.sectionById[q.section].short;
    }
    if (q.mode === 'new') pool = pool.filter(function (x) { return !st.quiz[x.id]; });
    if (q.mode === 'wrong') pool = pool.filter(function (x) { return st.quiz[x.id] && st.quiz[x.id].last === 0; });
    A.header({ title: title, tab: 'quiz', parent: parent });

    var n = +(q.n || 0);
    var items = A.shuffle(pool);
    if (n > 0) items = items.slice(0, n);
    if (!items.length) {
      el.innerHTML = '<div class="card center empty"><div class="big-emoji">🤔</div><h2>Brak pytań</h2><p class="muted">' +
        (q.mode === 'wrong' ? 'Nie masz zapisanych błędnych odpowiedzi — świetnie!' : q.mode === 'new' ? 'Wszystkie pytania z tych działów już widziałeś/aś.' : 'Brak pytań w wybranym zakresie.') +
        '</p><a class="btn" href="#/quiz">Wróć</a></div>';
      return;
    }

    var idx = 0, score = 0, wrong = [], answered = false;

    function show() {
      var it = items[idx];
      answered = false;
      var order = A.shuffle(it.options.map(function (_, i) { return i; }));
      var html = '<div class="session-top"><span>Pytanie ' + (idx + 1) + ' / ' + items.length + '</span>' + A.progressBar(A.pct(idx, items.length)) + '</div>' +
        '<div class="card quiz-card">' + secChip(it.lesson) + '<div class="quiz-q md">' + MD.render(it.q) + '</div><div class="options">';
      order.forEach(function (oi, k) {
        html += '<button class="option" data-o="' + oi + '"><span class="letter">' + 'ABCDEF'[k] + '</span><span class="md">' + MD.inline(it.options[oi]) + '</span></button>';
      });
      html += '</div><div id="explain"></div></div><div id="next-wrap"></div>';
      el.innerHTML = html;
      A.$$('.option', el).forEach(function (b) {
        b.addEventListener('click', function () { answer(+b.getAttribute('data-o')); });
      });
    }

    function answer(o) {
      if (answered) return;
      answered = true;
      var it = items[idx];
      var ok = o === it.answer;
      if (ok) score++; else wrong.push(it);
      Store.recordQuiz(it.id, ok);
      A.$$('.option', el).forEach(function (b) {
        var bo = +b.getAttribute('data-o');
        b.disabled = true;
        if (bo === it.answer) b.classList.add('correct');
        else if (bo === o) b.classList.add('wrong');
      });
      A.$('#explain', el).innerHTML = '<div class="callout callout-' + (ok ? 'tip' : 'warn') + '"><div class="callout-title">' + (ok ? '✓ Dobrze!' : '✗ Niestety') + '</div>' +
        (it.explain ? '<div class="md">' + MD.render(it.explain) + '</div>' : '') +
        '<a class="small" href="#/l/' + it.lesson + '">→ Lekcja: ' + esc(A.lessonById[it.lesson].title) + '</a></div>';
      var last = idx === items.length - 1;
      A.$('#next-wrap', el).innerHTML = '<button class="btn block" id="next">' + (last ? 'Zobacz wynik' : 'Dalej →') + '</button>';
      A.$('#next', el).addEventListener('click', next);
      A.$('#next', el).scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    function next() {
      idx++;
      if (idx < items.length) { show(); window.scrollTo(0, 0); } else finish();
    }

    function finish() {
      lastWrong = wrong.slice();
      var p = A.pct(score, items.length);
      var msg = p === 100 ? 'Perfekcyjnie! 🏆' : p >= 80 ? 'Świetny wynik! 💪' : p >= 60 ? 'Nieźle — powtórz błędy.' : 'Warto wrócić do lekcji 📚';
      var html = '<div class="card center result"><div class="ring" style="--p:' + p + '"><b>' + p + '%</b></div>' +
        '<h2>' + score + ' / ' + items.length + '</h2><p>' + msg + '</p><div class="btn-row center">' +
        (wrong.length ? '<a class="btn" href="#/quiz/run?mode=lastwrong">Powtórz błędne (' + wrong.length + ')</a>' : '') +
        '<a class="btn ghost" href="#/quiz">Nowy quiz</a></div></div>';
      if (wrong.length) {
        html += '<h2 class="h-sec">Do poprawy</h2>';
        wrong.forEach(function (it) {
          html += '<div class="card review">' + secChip(it.lesson) + '<div class="md">' + MD.render(it.q) + '</div>' +
            '<p class="ok-answer">✓ ' + MD.inline(it.options[it.answer]) + '</p>' +
            (it.explain ? '<div class="md muted small">' + MD.render(it.explain) + '</div>' : '') + '</div>';
        });
      }
      el.innerHTML = html;
      window.scrollTo(0, 0);
    }

    function onKey(e) {
      if (!answered && /^[1-6]$/.test(e.key)) {
        var b = A.$$('.option', el)[+e.key - 1];
        if (b) b.click();
      } else if (answered && e.key === 'Enter') {
        var nb = A.$('#next', el);
        if (nb) nb.click();
      }
    }
    document.addEventListener('keydown', onKey);
    show();
    return function () { document.removeEventListener('keydown', onKey); };
  });
})();
