package io.github.blazevortex.web;

public final class DashboardPages {

    private DashboardPages() {
    }

    public static String html() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="utf-8"/>
                  <title>Blaze Vortex</title>
                  <style>
                    :root { color-scheme: dark; --bg:#0b1020; --card:#151b2e; --accent:#ff6a3d; --text:#f4f1ea; --muted:#9aa3b5; }
                    body { margin:0; font-family: ui-sans-serif, system-ui, sans-serif; background:radial-gradient(circle at top,#1b2340,#0b1020 45%); color:var(--text); }
                    header { padding:24px 32px; display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid #223; }
                    h1 { margin:0; font-size:22px; letter-spacing:.08em; text-transform:uppercase; }
                    main { padding:24px 32px; display:grid; grid-template-columns: 320px 1fr; gap:24px; }
                    .card { background:var(--card); border:1px solid #2a3354; border-radius:18px; padding:18px; }
                    button, input, select { background:#0f1424; color:var(--text); border:1px solid #334; border-radius:10px; padding:8px 10px; }
                    button { background:var(--accent); border:0; color:#1a0900; font-weight:700; cursor:pointer; }
                    table { width:100%; border-collapse:collapse; }
                    td, th { padding:8px; border-bottom:1px solid #2a3354; text-align:left; }
                    .muted { color:var(--muted); }
                    img.gif, img.preview { max-width:96px; max-height:96px; image-rendering:pixelated; }
                  </style>
                </head>
                <body>
                  <header>
                    <h1>Blaze Vortex</h1>
                    <div class="muted" id="statusline">Loading…</div>
                  </header>
                  <main>
                    <section class="card">
                      <h2>Status</h2>
                      <pre id="status" class="muted"></pre>
                      <p><button onclick="buildPack()">Rebuild resource pack</button></p>
                      <h3>Upload</h3>
                      <select id="category">
                        <option>emojis</option><option>images</option><option>gifs</option><option>videos</option><option>sounds</option>
                      </select>
                      <input id="file" type="file"/>
                      <button onclick="upload()">Upload</button>
                    </section>
                    <section class="card">
                      <h2>Emojis</h2>
                      <table id="emojis"><thead><tr><th>Name</th><th>Size</th><th>Source</th><th>Glyph</th><th></th></tr></thead><tbody></tbody></table>
                    </section>
                  </main>
                  <script>
                    async function load() {
                      const status = await (await fetch('/api/status')).json();
                      document.getElementById('statusline').textContent = status.version + ' · pack ' + status.pack + ' · ffmpeg ' + status.ffmpeg;
                      document.getElementById('status').textContent = JSON.stringify(status, null, 2);
                      const emojis = await (await fetch('/api/emojis')).json();
                      const body = document.querySelector('#emojis tbody');
                      body.innerHTML = '';
                      for (const e of emojis) {
                        const tr = document.createElement('tr');
                        tr.innerHTML = `<td>&lt;${e.name}&gt;</td>
                          <td><input type="number" min="1" max="100" value="${e.size}" data-name="${e.name}" style="width:72px"></td>
                          <td>${e.width}x${e.height}</td>
                          <td>${e.glyphHeight} x ${Math.round(e.glyphWidth)}</td>
                          <td><button onclick="saveSize('${e.name}', this)">Save</button></td>`;
                        body.appendChild(tr);
                      }
                    }
                    async function saveSize(name, button) {
                      const input = button.parentElement.parentElement.querySelector('input');
                      await fetch('/api/emojis', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({name, size: Number(input.value)}) });
                      load();
                    }
                    async function buildPack() { await fetch('/api/pack/build', {method:'POST'}); load(); }
                    async function upload() {
                      const file = document.getElementById('file').files[0];
                      const category = document.getElementById('category').value;
                      if (!file) return;
                      await fetch('/api/upload?category=' + category + '&filename=' + encodeURIComponent(file.name), { method:'POST', body: file });
                      load();
                    }
                    load();
                  </script>
                </body>
                </html>
                """;
    }
}
