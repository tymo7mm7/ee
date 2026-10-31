/* Zapis postępów (localStorage) + algorytm powtórek (wariant SM-2). */
(function () {
  'use strict';

  var KEY = 'qmlakademia.v1';
  var DAY = 24 * 3600 * 1000;

  function today(ts) {
    var d = ts ? new Date(ts) : new Date();
    var m = d.getMonth() + 1, day = d.getDate();
    return d.getFullYear() + '-' + (m < 10 ? '0' : '') + m + '-' + (day < 10 ? '0' : '') + day;
  }

  function fresh() {
    return {
      v: 1,
      lessons: {},      // id -> {done: bool, at: ts}
      cards: {},        // id -> {ef, iv (dni), reps, due (ts), lapses, last}
      quiz: {},         // id -> {seen, ok, last (0/1)}
      days: {},         // 'RRRR-MM-DD' -> liczba aktywności
      newToday: { date: '', n: 0 },
      lastLesson: '',
      settings: { theme: 'dark', newPerDay: 20 }
    };
  }

  var state = fresh();
  try {
    var raw = localStorage.getItem(KEY);
    if (raw) {
      var parsed = JSON.parse(raw);
      if (parsed && parsed.v === 1) {
        state = Object.assign(fresh(), parsed);
        state.settings = Object.assign(fresh().settings, parsed.settings || {});
      }
    }
  } catch (e) { /* uszkodzone dane - zaczynamy od nowa */ }

  var saveTimer = null;
  function save() {
    clearTimeout(saveTimer);
    saveTimer = setTimeout(saveNow, 150);
  }
  function saveNow() {
    try { localStorage.setItem(KEY, JSON.stringify(state)); } catch (e) { /* brak miejsca */ }
  }
  window.addEventListener('pagehide', saveNow);
  document.addEventListener('visibilitychange', function () { if (document.hidden) saveNow(); });

  function activity() {
    var d = today();
    state.days[d] = (state.days[d] || 0) + 1;
    save();
  }

  function streak() {
    var n = 0;
    var t = Date.now();
    // dzisiejszy dzień liczy się, jeśli była aktywność; w przeciwnym razie zaczynamy od wczoraj
    if (!state.days[today(t)]) t -= DAY;
    while (state.days[today(t)]) { n++; t -= DAY; }
    return n;
  }

  // ---------- Lekcje ----------
  function lessonDone(id) { return !!(state.lessons[id] && state.lessons[id].done); }
  function setLessonDone(id, done) {
    state.lessons[id] = { done: !!done, at: Date.now() };
    if (done) activity();
    save();
  }

  // ---------- Fiszki (SM-2) ----------
  // ocena: 0 = nie pamiętam, 1 = trudne, 2 = dobrze, 3 = łatwe
  function cardState(id) { return state.cards[id] || null; }

  function schedule(cs, grade, now) {
    cs = cs ? Object.assign({}, cs) : { ef: 2.5, iv: 0, reps: 0, due: now, lapses: 0 };
    var q = [1, 3, 4, 5][grade];
    if (grade === 0) {
      cs.reps = 0;
      cs.lapses = (cs.lapses || 0) + 1;
      cs.iv = 0;
      cs.due = now + 60 * 1000; // wróci jeszcze w tej sesji
    } else {
      if (cs.reps === 0) cs.iv = grade === 3 ? 3 : 1;
      else if (cs.reps === 1) cs.iv = grade === 1 ? 2 : grade === 3 ? 6 : 4;
      else cs.iv = Math.round(cs.iv * (grade === 1 ? 1.2 : cs.ef * (grade === 3 ? 1.3 : 1)));
      cs.iv = Math.max(1, Math.min(cs.iv, 365));
      cs.reps += 1;
      cs.due = now + cs.iv * DAY - 3 * 3600 * 1000; // lekki zapas, by karta była gotowa rano
    }
    cs.ef = Math.max(1.3, cs.ef + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)));
    cs.last = now;
    return cs;
  }

  function previewInterval(id, grade) {
    var now = Date.now();
    var cs = schedule(cardState(id), grade, now);
    if (grade === 0) return '<1 min';
    return cs.iv + ' d';
  }

  function gradeCard(id, grade) {
    var wasNew = !state.cards[id];
    state.cards[id] = schedule(state.cards[id], grade, Date.now());
    if (wasNew) {
      var d = today();
      if (state.newToday.date !== d) state.newToday = { date: d, n: 0 };
      state.newToday.n++;
    }
    activity();
    save();
  }

  function isDue(id, now) {
    var cs = state.cards[id];
    return !!cs && cs.due <= (now || Date.now());
  }

  function newLeftToday() {
    var d = today();
    var used = state.newToday.date === d ? state.newToday.n : 0;
    return Math.max(0, state.settings.newPerDay - used);
  }

  // ---------- Quiz ----------
  function recordQuiz(id, ok) {
    var s = state.quiz[id] || { seen: 0, ok: 0, last: 0 };
    s.seen++;
    if (ok) s.ok++;
    s.last = ok ? 1 : 0;
    state.quiz[id] = s;
    activity();
    save();
  }

  function reset() {
    var theme = state.settings.theme;
    state = fresh();
    state.settings.theme = theme;
    saveNow();
  }

  function exportData() { return JSON.stringify(state); }
  function importData(text) {
    var obj = JSON.parse(text);
    if (!obj || obj.v !== 1 || typeof obj.cards !== 'object') throw new Error('Nieprawidłowy format');
    state = Object.assign(fresh(), obj);
    state.settings = Object.assign(fresh().settings, obj.settings || {});
    saveNow();
  }

  window.Store = {
    get state() { return state; },
    save: save,
    today: today,
    streak: streak,
    activity: activity,
    lessonDone: lessonDone,
    setLessonDone: setLessonDone,
    cardState: cardState,
    gradeCard: gradeCard,
    previewInterval: previewInterval,
    isDue: isDue,
    newLeftToday: newLeftToday,
    recordQuiz: recordQuiz,
    reset: reset,
    exportData: exportData,
    importData: importData
  };
})();
