// Renderuje ikony launchera (PNG) z SVG za pomocą Playwright/Chromium.
// Użycie: node tools/make_icons.js
const path = require('path');
const fs = require('fs');
const { chromium } = require('playwright');

const ROOT = path.resolve(__dirname, '..');
const RES = path.join(ROOT, 'android', 'res');

// Grafika w układzie 108x108 (strefa bezpieczna ikony adaptacyjnej: koło o średnicy 66).
function art(mono) {
  const stroke = mono ? '#fff' : 'url(#g)';
  const faint = mono ? 'rgba(255,255,255,.55)' : 'rgba(160,190,255,.45)';
  const fill = mono ? 'none' : 'url(#rg)';
  return `
  <defs>
    <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="#4de1ff"/><stop offset="1" stop-color="#a77bff"/>
    </linearGradient>
    <radialGradient id="rg" cx=".38" cy=".32" r=".8">
      <stop offset="0" stop-color="#3a3f8f" stop-opacity=".9"/>
      <stop offset="1" stop-color="#141838" stop-opacity=".2"/>
    </radialGradient>
    <radialGradient id="dot" cx=".5" cy=".5" r=".5">
      <stop offset="0" stop-color="#fff"/><stop offset=".45" stop-color="#ffd36e"/>
      <stop offset="1" stop-color="#ffb02e" stop-opacity="0"/>
    </radialGradient>
  </defs>
  <circle cx="54" cy="54" r="27" fill="${fill}" stroke="${stroke}" stroke-width="3"/>
  <ellipse cx="54" cy="54" rx="27" ry="8.5" fill="none" stroke="${faint}" stroke-width="1.6"/>
  <ellipse cx="54" cy="54" rx="9" ry="27" fill="none" stroke="${faint}" stroke-width="1.2"/>
  <line x1="54" y1="54" x2="69.5" y2="35.5" stroke="${mono ? '#fff' : '#ffd36e'}" stroke-width="3.2" stroke-linecap="round"/>
  <circle cx="54" cy="54" r="2.6" fill="${mono ? '#fff' : '#ffd36e'}"/>
  ${mono ? '<circle cx="70.5" cy="34.5" r="4.2" fill="#fff"/>'
         : '<circle cx="70.5" cy="34.5" r="8" fill="url(#dot)"/><circle cx="70.5" cy="34.5" r="3.3" fill="#fff"/>'}
  `;
}

function svgForeground(mono) {
  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="108" height="108">${art(mono)}</svg>`;
}

function svgLegacy() {
  // Pełna ikona (starsze Androidy): zaokrąglony kwadrat + grafika powiększona.
  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="108" height="108">
    <defs><linearGradient id="bg" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0" stop-color="#1e2360"/><stop offset="1" stop-color="#0f1226"/></linearGradient></defs>
    <rect x="4" y="4" width="100" height="100" rx="24" fill="url(#bg)"/>
    <g transform="translate(54 54) scale(1.38) translate(-54 -54)">${art(false)}</g>
  </svg>`;
}

const DENSITIES = { mdpi: 1, hdpi: 1.5, xhdpi: 2, xxhdpi: 3, xxxhdpi: 4 };

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  async function render(svg, size, file) {
    await page.setViewportSize({ width: size, height: size });
    const html = `<html><body style="margin:0;background:transparent">
      <div style="width:${size}px;height:${size}px">${svg.replace('width="108" height="108"', `width="${size}" height="${size}"`)}</div></body></html>`;
    await page.setContent(html);
    fs.mkdirSync(path.dirname(file), { recursive: true });
    await page.screenshot({ path: file, omitBackground: true, clip: { x: 0, y: 0, width: size, height: size } });
  }
  for (const [d, k] of Object.entries(DENSITIES)) {
    const dir = path.join(RES, `mipmap-${d}`);
    await render(svgLegacy(), Math.round(48 * k), path.join(dir, 'ic_launcher.png'));
    await render(svgForeground(false), Math.round(108 * k), path.join(dir, 'ic_launcher_fg.png'));
    await render(svgForeground(true), Math.round(108 * k), path.join(dir, 'ic_launcher_mono.png'));
  }
  // Ikona dla wersji przeglądarkowej.
  await render(svgLegacy(), 192, path.join(ROOT, 'web', 'icon-192.png'));
  fs.writeFileSync(path.join(ROOT, 'web', 'icon.svg'), svgLegacy());
  await browser.close();
  console.log('Ikony wygenerowane.');
})();
