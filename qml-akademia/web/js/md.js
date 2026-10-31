/* Prosty renderer Markdown z obsługą wzorów KaTeX ($...$ i $$...$$). */
(function () {
  'use strict';

  var mathCache = {};

  function esc(s) {
    return String(s)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function tex(src, display) {
    var key = (display ? 'D' : 'I') + src;
    if (mathCache[key]) return mathCache[key];
    var html;
    try {
      html = window.katex
        ? katex.renderToString(src, { displayMode: display, throwOnError: false, strict: 'ignore' })
        : '<code>' + esc(src) + '</code>';
    } catch (e) {
      html = '<code>' + esc(src) + '</code>';
    }
    if (display) html = '<div class="math-block">' + html + '</div>';
    mathCache[key] = html;
    return html;
  }

  // Formatowanie w linii: wzory i kod chronione placeholderami.
  function inline(text) {
    var slots = [];
    function keep(html) {
      slots.push(html);
      return '\u0000' + (slots.length - 1) + '\u0000';
    }
    text = text.replace(/`([^`]+)`/g, function (_, c) { return keep('<code>' + esc(c) + '</code>'); });
    text = text.replace(/\$\$([^$]+)\$\$/g, function (_, m) { return keep(tex(m, true)); });
    text = text.replace(/\$([^$\n]+?)\$/g, function (_, m) { return keep(tex(m, false)); });
    text = esc(text);
    text = text.replace(/\[([^\]]+)\]\(([^)\s]+)\)/g, function (_, label, href) {
      var ext = /^https?:/.test(href);
      return '<a href="' + href + '"' + (ext ? ' target="_blank" rel="noopener"' : '') + '>' + label + '</a>';
    });
    text = text.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
    text = text.replace(/(^|[^*\w])\*([^*\n]+)\*(?!\w)/g, '$1<em>$2</em>');
    text = text.replace(/==([^=]+)==/g, '<mark>$1</mark>');
    text = text.replace(/\u0000(\d+)\u0000/g, function (_, i) { return slots[+i]; });
    return text;
  }

  var CALLOUTS = {
    tip: ['tip', 'Wskazówka'],
    info: ['info', 'Info'],
    warn: ['warn', 'Uwaga'],
    key: ['key', 'Najważniejsze'],
    ex: ['ex', 'Przykład'],
    code: ['code', 'W kodzie'],
    exam: ['exam', 'Na egzaminie']
  };

  function render(src) {
    var lines = String(src || '').replace(/\r/g, '').split('\n');
    var out = [];
    var i = 0;
    var para = [];

    function flushPara() {
      if (para.length) {
        out.push('<p>' + inline(para.join(' ')) + '</p>');
        para = [];
      }
    }

    while (i < lines.length) {
      var line = lines[i];
      var t = line.trim();

      if (!t) { flushPara(); i++; continue; }

      // Blok kodu
      if (/^```/.test(t)) {
        flushPara();
        var lang = t.slice(3).trim();
        var code = [];
        i++;
        while (i < lines.length && !/^```/.test(lines[i].trim())) { code.push(lines[i]); i++; }
        i++;
        out.push('<pre class="code"' + (lang ? ' data-lang="' + esc(lang) + '"' : '') + '><code>' + esc(code.join('\n')) + '</code></pre>');
        continue;
      }

      // Wzór blokowy $$ ... $$ (może zajmować wiele linii)
      if (/^\$\$/.test(t)) {
        flushPara();
        var buf = t.slice(2);
        if (/\$\$$/.test(buf) && buf.length >= 2) {
          out.push(tex(buf.slice(0, -2), true));
          i++;
          continue;
        }
        i++;
        while (i < lines.length && !/\$\$\s*$/.test(lines[i])) { buf += '\n' + lines[i]; i++; }
        if (i < lines.length) buf += '\n' + lines[i].replace(/\$\$\s*$/, '');
        i++;
        out.push(tex(buf, true));
        continue;
      }

      // Nagłówki
      var h = /^(#{1,4})\s+(.*)$/.exec(t);
      if (h) {
        flushPara();
        var lvl = Math.min(4, h[1].length + 1);
        out.push('<h' + lvl + '>' + inline(h[2]) + '</h' + lvl + '>');
        i++;
        continue;
      }

      // Linia pozioma
      if (/^(-{3,}|\*{3,})$/.test(t)) { flushPara(); out.push('<hr>'); i++; continue; }

      // Ramka (callout) / cytat
      if (/^>/.test(t)) {
        flushPara();
        var q = [];
        while (i < lines.length && /^>/.test(lines[i].trim())) {
          q.push(lines[i].trim().replace(/^>\s?/, ''));
          i++;
        }
        var kind = /^\[!(\w+)\]\s*(.*)$/.exec(q[0] || '');
        if (kind && CALLOUTS[kind[1]]) {
          var c = CALLOUTS[kind[1]];
          var title = kind[2] || c[1];
          out.push('<div class="callout callout-' + c[0] + '"><div class="callout-title">' + inline(title) + '</div>' + render(q.slice(1).join('\n')) + '</div>');
        } else {
          out.push('<blockquote>' + render(q.join('\n')) + '</blockquote>');
        }
        continue;
      }

      // Tabela
      if (/^\|/.test(t) && i + 1 < lines.length && /^\|?\s*:?-{2,}/.test(lines[i + 1].trim())) {
        flushPara();
        var rows = [];
        while (i < lines.length && /^\|/.test(lines[i].trim())) { rows.push(lines[i].trim()); i++; }
        var cells = function (r) {
          return r.replace(/^\|/, '').replace(/\|$/, '').split(/(?<!\\)\|/).map(function (c) { return c.trim().replace(/\\\|/g, '|'); });
        };
        var head = cells(rows[0]);
        var html = '<div class="table-wrap"><table><thead><tr>' + head.map(function (c) { return '<th>' + inline(c) + '</th>'; }).join('') + '</tr></thead><tbody>';
        rows.slice(2).forEach(function (r) {
          html += '<tr>' + cells(r).map(function (c) { return '<td>' + inline(c) + '</td>'; }).join('') + '</tr>';
        });
        out.push(html + '</tbody></table></div>');
        continue;
      }

      // Listy
      var ul = /^[-*]\s+/.test(t);
      var ol = /^\d+[.)]\s+/.test(t);
      if (ul || ol) {
        flushPara();
        var items = [];
        var re = ul ? /^[-*]\s+/ : /^\d+[.)]\s+/;
        while (i < lines.length) {
          var l = lines[i];
          var lt = l.trim();
          if (re.test(lt) && !/^\s{2,}/.test(l)) {
            items.push([lt.replace(re, '')]);
          } else if (lt && /^\s{2,}/.test(l) && items.length) {
            items[items.length - 1].push(lt);
          } else {
            break;
          }
          i++;
        }
        var tag = ul ? 'ul' : 'ol';
        out.push('<' + tag + '>' + items.map(function (it) {
          var first = inline(it[0]);
          var rest = it.slice(1);
          if (!rest.length) return '<li>' + first + '</li>';
          // zagnieżdżona lista lub kontynuacja tekstu
          if (rest.every(function (r) { return /^[-*]\s+/.test(r); })) {
            return '<li>' + first + '<ul>' + rest.map(function (r) { return '<li>' + inline(r.replace(/^[-*]\s+/, '')) + '</li>'; }).join('') + '</ul></li>';
          }
          return '<li>' + first + ' ' + inline(rest.join(' ')) + '</li>';
        }).join('') + '</' + tag + '>');
        continue;
      }

      para.push(t);
      i++;
    }
    flushPara();
    return out.join('\n');
  }

  function plain(src) {
    return String(src || '')
      .replace(/```[\s\S]*?```/g, ' ')
      .replace(/\$\$[\s\S]*?\$\$/g, ' ')
      .replace(/\$([^$]+)\$/g, '$1')
      .replace(/[#>*`|_=]|\[!\w+\]/g, ' ')
      .replace(/\\[a-zA-Z]+/g, ' ')
      .replace(/\s+/g, ' ')
      .trim();
  }

  window.MD = { render: render, inline: inline, esc: esc, tex: tex, plain: plain };
})();
