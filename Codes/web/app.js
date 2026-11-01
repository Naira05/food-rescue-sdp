/* Food Rescue — tiny SPA over the Java HTTP layer */
const $ = (s, r = document) => r.querySelector(s);
const root = $('#root');
const S = { token: localStorage.getItem('fr_token'), user: null, tab: null };

const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const LOGO = `<svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.5 19 2c1 2 2 4.2 2 8 0 5.5-4.8 10-10 10z"/><path d="M2 21c0-3 1.9-5.5 6-7.5"/></svg>`;
const ICON = { DONOR: '🍲', VOLUNTEER: '🚲', BENEFICIARY: '🤲', ADMIN: '🛡️' };
const LABEL = { DONOR: 'Donor', VOLUNTEER: 'Volunteer', BENEFICIARY: 'Beneficiary', ADMIN: 'Admin' };

async function api(path, method = 'GET', body) {
  const res = await fetch('/api/' + path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(S.token ? { Authorization: 'Bearer ' + S.token } : {}) },
    body: body ? JSON.stringify(body) : undefined
  });
  const data = await res.json().catch(() => ({}));
  if (res.status === 401 && S.user) { logout(true); }
  if (!res.ok) throw new Error(data.error || 'Something went wrong');
  return data;
}

function toast(msg, bad) {
  const t = document.createElement('div');
  t.className = 'toast' + (bad ? ' bad' : '');
  t.textContent = msg;
  document.body.append(t);
  setTimeout(() => t.remove(), 2800);
}
const guard = fn => async (...a) => { try { await fn(...a); } catch (e) { toast(e.message, true); } };

function countUp(el, to) {
  const t0 = performance.now(), d = 1100;
  const tick = t => { const p = Math.min((t - t0) / d, 1); el.textContent = Math.round(to * (1 - Math.pow(1 - p, 3))); if (p < 1) requestAnimationFrame(tick); };
  requestAnimationFrame(tick);
}

/* ---------------------------------------------------------------- landing */
async function landing() {
  root.innerHTML = `
  <nav class="nav" id="nav">
    <a class="brand"><span class="logo">${LOGO}</span>Food Rescue</a>
    <div class="nav-actions">
      <button class="btn btn-ghost" data-auth="login">Log in</button>
      <button class="btn btn-primary" data-auth="register">Join us</button>
    </div>
  </nav>
  <header class="hero">
    <div>
      <span class="pill"><b></b> Surplus food, matched in minutes</span>
      <h1>Good food deserves a <em>second</em> table.</h1>
      <p class="lead">Restaurants, volunteers and families — one gentle platform that turns today’s surplus into tonight’s dinner.</p>
      <div class="cta">
        <button class="btn btn-accent" data-auth="register">Start giving →</button>
        <button class="btn btn-ghost" onclick="document.getElementById('how').scrollIntoView()">How it works</button>
      </div>
    </div>
    <div class="hero-art">
      <div class="plate"><svg viewBox="0 0 200 200"><defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#e2703a"/><stop offset="1" stop-color="#e9c46a"/></linearGradient></defs>
        <circle cx="100" cy="100" r="82" fill="none" stroke="#123524" stroke-opacity=".12" stroke-width="2" stroke-dasharray="3 7"/>
        <path d="M100 168C58 140 44 108 62 82c12-18 30-10 38 6 8-16 26-24 38-6 18 26 4 58-38 86z" fill="url(#g)"/>
        <path d="M100 88c0-22 10-36 30-42-2 20-10 34-30 42z" fill="#1e4d36"/><path d="M100 88C98 68 86 56 68 54c2 18 12 30 32 34z" fill="#2f7a52"/></svg></div>
      <div class="float-card fc1"><span class="ic">🍲</span><div><small>New donation</small><strong>40 hot meals</strong></div></div>
      <div class="float-card fc2"><span class="ic">🚲</span><div><small>Volunteer on the way</small><strong>12 min away</strong></div></div>
      <div class="float-card fc3"><span class="ic">💚</span><div><small>Delivered</small><strong>Thank you!</strong></div></div>
    </div>
  </header>
  <section class="stats" id="stats">
    ${['donations:Donations shared', 'available:Ready to collect', 'delivered:Delivered to tables', 'members:Community members'].map(x => `<div class="stat"><div class="n" data-k="${x.split(':')[0]}">0</div><div class="l">${x.split(':')[1]}</div></div>`).join('')}
  </section>
  <section class="section"><h2>One platform, three ways to help</h2><p>Pick the role that fits you. You can feel the difference from the very first day.</p>
    <div class="roles">
      ${[['DONOR', 'Donate', 'Post surplus meals, produce or packaged food in under a minute and get a receipt instantly.'],
         ['VOLUNTEER', 'Volunteer', 'Claim nearby pickups, follow your assignments and mark them delivered.'],
         ['BENEFICIARY', 'Receive', 'Browse what’s available, claim it, or tell us what your community needs.']]
         .map(([r, t, d]) => `<div class="role-card"><div class="ic">${ICON[r]}</div><h3>${t}</h3><p>${d}</p><button class="btn btn-ghost btn-sm" data-auth="register" data-role="${r}">Join as ${LABEL[r].toLowerCase()}</button></div>`).join('')}
    </div></section>
  <section class="section" id="how"><h2>From kitchen to table, in three steps</h2>
    <div class="steps">
      <div class="step"><h3>Post it</h3><p>A donor lists what’s left — type, quantity and expiry date.</p></div>
      <div class="step"><h3>Claim it</h3><p>A volunteer or a beneficiary claims it, and the donor is notified at once.</p></div>
      <div class="step"><h3>Share it</h3><p>Picked up, delivered, eaten. The status updates for everyone.</p></div>
    </div></section>
  <footer class="footer"><span>© ${new Date().getFullYear()} Food Rescue</span><span>Made with care, so nothing good goes to waste.</span></footer>`;

  const nav = $('#nav');
  addEventListener('scroll', () => nav.classList.toggle('scrolled', scrollY > 8), { passive: true });
  root.querySelectorAll('[data-auth]').forEach(b => b.onclick = () => authModal(b.dataset.auth, b.dataset.role));
  api('stats').then(s => root.querySelectorAll('[data-k]').forEach(el => countUp(el, s[el.dataset.k]))).catch(() => { });
}

/* ---------------------------------------------------------------- auth */
function authModal(mode = 'login', role = 'DONOR') {
  const o = document.createElement('div');
  o.className = 'overlay';
  o.onclick = e => { if (e.target === o) o.remove(); };
  o.innerHTML = `<div class="modal">
    <h2 id="mt"></h2><p class="sub" id="ms"></p>
    <div class="tabs"><button data-m="login">Log in</button><button data-m="register">Sign up</button></div>
    <form id="af" autocomplete="on"></form><div class="err" id="ae"></div></div>`;
  document.body.append(o);

  const draw = (m) => {
    $('#mt', o).textContent = m === 'login' ? 'Welcome back' : 'Join the table';
    $('#ms', o).textContent = m === 'login' ? 'Log in to continue.' : 'Create your account in a few seconds.';
    o.querySelectorAll('.tabs button').forEach(b => b.classList.toggle('on', b.dataset.m === m));
    const f = $('#af', o);
    f.innerHTML = m === 'login' ? `
      <div class="field"><label>E-mail</label><input name="email" type="email" required autofocus></div>
      <div class="field"><label>Password</label><input name="password" type="password" required></div>
      <button class="btn btn-primary">Log in</button>
      <p class="hint">Demo admin: admin@foodrescue.org / admin123</p>` : `
      <div class="role-pick">${['DONOR', 'VOLUNTEER', 'BENEFICIARY'].map(r => `<label><input type="radio" name="role" value="${r}" ${r === role ? 'checked' : ''}><span><em>${ICON[r]}</em>${LABEL[r]}</span></label>`).join('')}</div>
      <div class="field"><label>Name</label><input name="name" required></div>
      <div class="row"><div class="field"><label>E-mail</label><input name="email" type="email" required></div>
      <div class="field"><label>Password</label><input name="password" type="password" minlength="6" required></div></div>
      <div id="extra"></div>
      <button class="btn btn-primary">Create account</button>`;
    if (m === 'register') {
      const extra = () => {
        const r = f.role.value;
        $('#extra', f).innerHTML = r === 'DONOR' ? `<div class="field"><label>Address</label><input name="address" placeholder="12 Nile St, Cairo"></div>`
          : r === 'VOLUNTEER' ? `<div class="row"><div class="field"><label>Skills</label><input name="skills" placeholder="driving, lifting"></div><div class="field"><label>Availability</label><input name="availability" placeholder="Weekends"></div></div>`
          : `<div class="field"><label>Your needs</label><input name="needs" placeholder="Hot meals for 40 people"></div>`;
      };
      f.querySelectorAll('[name=role]').forEach(r => r.onchange = extra); extra();
    }
    f.onsubmit = async e => {
      e.preventDefault();
      const body = Object.fromEntries(new FormData(f));
      try {
        const r = await api(m, 'POST', body);
        S.token = r.token; S.user = r.user; localStorage.setItem('fr_token', r.token);
        o.remove(); S.tab = null; dashboard();
      } catch (er) { $('#ae', o).textContent = er.message; }
    };
  };
  o.querySelectorAll('.tabs button').forEach(b => b.onclick = () => draw(b.dataset.m));
  draw(mode);
}

async function logout(silent) {
  if (!silent) await api('logout', 'POST').catch(() => { });
  S.token = null; S.user = null; localStorage.removeItem('fr_token');
  landing();
}

/* ---------------------------------------------------------------- dashboard */
const MENUS = {
  DONOR: [['mine', '📦', 'My donations'], ['notes', '🔔', 'Notifications']],
  VOLUNTEER: [['pickups', '🧺', 'Available pickups'], ['tasks', '🚲', 'My assignments'], ['notes', '🔔', 'Notifications']],
  BENEFICIARY: [['browse', '🥗', 'Available food'], ['requests', '📝', 'My requests'], ['notes', '🔔', 'Notifications']],
  ADMIN: [['overview', '📊', 'Overview'], ['donations', '📦', 'Donations'], ['users', '👥', 'Users'], ['types', '🏷️', 'Donation types'], ['notes', '🔔', 'Notifications']]
};

function dashboard() {
  const u = S.user, menu = MENUS[u.role];
  S.tab = S.tab || menu[0][0];
  root.innerHTML = `
  <nav class="nav scrolled"><a class="brand" id="home"><span class="logo">${LOGO}</span>Food Rescue</a>
    <div class="nav-actions"><button class="btn btn-ghost btn-sm" id="out">Log out</button></div></nav>
  <div class="app">
    <aside class="side">
      <div class="me"><div class="avatar">${esc(u.name[0].toUpperCase())}</div><div><b>${esc(u.name)}</b><small>${ICON[u.role]} ${LABEL[u.role]}</small></div></div>
      <div class="menu">${menu.map(([k, i, l]) => `<button data-t="${k}" class="${k === S.tab ? 'on' : ''}"><span>${i}</span>${l}</button>`).join('')}
        <div class="sp"></div><button data-t="profile" class="${S.tab === 'profile' ? 'on' : ''}"><span>⚙️</span>Profile</button></div>
    </aside>
    <main id="main"></main>
  </div>`;
  $('#out').onclick = () => logout();
  $('#home').onclick = () => { S.tab = menu[0][0]; dashboard(); };
  root.querySelectorAll('[data-t]').forEach(b => b.onclick = () => { S.tab = b.dataset.t; dashboard(); });
  const main = $('#main');
  main.innerHTML = '<div class="cards"><div class="skeleton"></div><div class="skeleton"></div><div class="skeleton"></div></div>';
  (VIEWS[S.tab] || VIEWS.profile)(main).catch(e => { main.innerHTML = `<div class="empty"><div class="big">😕</div>${esc(e.message)}</div>`; });
}

const head = (t, p, action = '') => `<div class="head"><div><h1>${t}</h1><p>${p}</p></div>${action}</div>`;
const empty = (icon, msg) => `<div class="empty"><div class="big">${icon}</div>${msg}</div>`;
const tag = s => `<span class="tag t-${s}">${s.replace('_', ' ').toLowerCase()}</span>`;
const days = d => { const n = Math.ceil((new Date(d) - new Date().setHours(0, 0, 0, 0)) / 864e5); return n < 0 ? 'expired' : n === 0 ? 'expires today' : `${n} day${n > 1 ? 's' : ''} left`; };

function donationCard(d, actions = '', i = 0) {
  return `<article class="card" style="animation-delay:${i * 50}ms">
    <div style="display:flex;justify-content:space-between;align-items:center"><span class="pill">${esc(d.type)}</span>${tag(d.status)}</div>
    <h3>${esc(d.description)}</h3>
    <div class="qty">${d.quantity} <small>${esc(d.unit)}</small></div>
    <div class="meta"><span>⏳ ${days(d.expiryDate)}</span><span>📅 ${d.donationDate}</span><span>#${d.id}</span></div>
    ${actions ? `<div class="foot">${actions}</div>` : ''}</article>`;
}

const notesView = async main => {
  const n = await api('notifications');
  main.innerHTML = head('Notifications', 'Everything that happened to your donations.') +
    (n.length ? `<div class="list">${n.map((x, i) => { const [t, ...m] = x.split(' - '); return `<div class="item" style="animation-delay:${i * 30}ms"><div>${esc(m.join(' - '))}<small>${esc(t.replace('T', ' ').slice(0, 16))}</small></div><span>🔔</span></div>`; }).join('')}</div>` : empty('🔕', 'Nothing yet — you’re all caught up.'));
};

const profileView = async main => {
  const u = await api('me');
  main.innerHTML = head('Profile', 'Keep your details up to date.') + `<div class="panel" style="max-width:520px"><form id="pf">
    <div class="field"><label>Name</label><input name="name" value="${esc(u.name)}"></div>
    <div class="field"><label>E-mail</label><input name="email" type="email" value="${esc(u.email)}"></div>
    <button class="btn btn-primary" style="width:100%">Save changes</button></form></div>`;
  $('#pf').onsubmit = guard(async e => { e.preventDefault(); S.user = await api('me', 'PUT', Object.fromEntries(new FormData(e.target))); toast('Profile updated'); dashboard(); });
};

const VIEWS = {
  notes: notesView, profile: profileView,

  /* ---- donor */
  async mine(main) {
    const l = await api('donations');
    const total = l.reduce((a, d) => a + d.quantity, 0);
    main.innerHTML = head(`Hello, ${esc(S.user.name.split(' ')[0])} 👋`, 'Thank you for feeding your community.', `<button class="btn btn-accent" id="new">＋ New donation</button>`) +
      `<div class="mini"><div class="stat"><div class="n">${l.length}</div><div class="l">Donations</div></div>
       <div class="stat"><div class="n">${l.filter(d => d.status === 'AVAILABLE').length}</div><div class="l">Waiting for pickup</div></div>
       <div class="stat"><div class="n">${l.filter(d => d.status === 'PICKED_UP').length}</div><div class="l">Delivered</div></div>
       <div class="stat"><div class="n">${Math.round(total)}</div><div class="l">Total units shared</div></div></div>` +
      (l.length ? `<div class="cards">${l.slice().reverse().map((d, i) => donationCard(d, '', i)).join('')}</div>` : empty('🍲', 'No donations yet. Your first one takes under a minute.'));
    $('#new').onclick = donationModal;
  },

  /* ---- volunteer */
  async pickups(main) {
    const l = await api('donations');
    main.innerHTML = head('Available pickups', 'Claim one and it becomes your assignment.') +
      (l.length ? `<div class="cards">${l.map((d, i) => donationCard(d, `<span></span><button class="btn btn-primary btn-sm" data-claim="${d.id}">Claim pickup</button>`, i)).join('')}</div>` : empty('🧺', 'No pickups right now. Check back soon.'));
    bindClaim(main);
  },
  async tasks(main) {
    const t = await api('assignments');
    main.innerHTML = head('My assignments', 'Mark each one once the food is delivered.') +
      (t.length ? `<div class="list">${t.slice().reverse().map((x, i) => { const id = (x.match(/#(\d+)/) || [])[1]; return `<div class="item" style="animation-delay:${i * 40}ms"><div>${esc(x)}</div>${id ? `<button class="btn btn-primary btn-sm" data-done="${id}">Mark picked up</button>` : ''}</div>`; }).join('')}</div>` : empty('🚲', 'No assignments yet — claim a pickup to begin.'));
    main.querySelectorAll('[data-done]').forEach(b => b.onclick = guard(async () => { await api('donations/' + b.dataset.done, 'PUT', { status: 'PICKED_UP' }); toast('Marked as picked up 🎉'); dashboard(); }));
  },

  /* ---- beneficiary */
  async browse(main) {
    const l = await api('donations');
    main.innerHTML = head('Available food', 'Claim what your community needs.') +
      (l.length ? `<div class="cards">${l.map((d, i) => donationCard(d, `<span></span><button class="btn btn-primary btn-sm" data-claim="${d.id}">Claim</button>`, i)).join('')}</div>` : empty('🥗', 'Nothing available at the moment.'));
    bindClaim(main);
  },
  async requests(main) {
    const r = await api('requests');
    main.innerHTML = head('My requests', 'Tell donors what you’re looking for.') +
      `<div class="panel"><h3>New request</h3><form class="inline" id="rf"><input name="item" placeholder="e.g. Rice, bread, hot meals" required><input name="quantity" type="number" min="0.1" step="any" placeholder="Quantity" required style="max-width:160px"><button class="btn btn-accent">Send request</button></form></div>` +
      (r.length ? `<div class="list">${r.slice().reverse().map(x => `<div class="item"><div><b>${esc(x.item)}</b><small>${x.requestedOn}</small></div><span class="pill">${x.quantity}</span></div>`).join('')}</div>` : empty('📝', 'No requests yet.'));
    $('#rf').onsubmit = guard(async e => { e.preventDefault(); const f = Object.fromEntries(new FormData(e.target)); await api('requests', 'POST', f); toast('Request sent'); dashboard(); });
  },

  /* ---- admin */
  async overview(main) {
    const [d, u, st] = await Promise.all([api('donations'), api('users'), api('stats')]);
    const by = {}; d.forEach(x => by[x.status] = (by[x.status] || 0) + 1);
    const statuses = ['AVAILABLE', 'CLAIMED', 'PICKED_UP', 'EXPIRED'], max = Math.max(1, ...Object.values(by));
    const roles = {}; u.forEach(x => roles[x.role] = (roles[x.role] || 0) + 1);
    const qty = {}; d.forEach(x => qty[x.type + ' (' + x.unit + ')'] = (qty[x.type + ' (' + x.unit + ')'] || 0) + x.quantity);
    main.innerHTML = head('Overview', 'A live look at the whole platform.') +
      `<div class="mini"><div class="stat"><div class="n">${d.length}</div><div class="l">Donations</div></div><div class="stat"><div class="n">${u.length}</div><div class="l">Users</div></div>
       <div class="stat"><div class="n">${st.available}</div><div class="l">Available now</div></div><div class="stat"><div class="n">${st.delivered}</div><div class="l">Delivered</div></div></div>
       <div class="row"><div class="panel"><h3>Donations by status</h3>${statuses.map(s => `<div style="margin-bottom:14px">${tag(s)} <b style="float:right">${by[s] || 0}</b><div class="bar"><i style="width:0" data-w="${(by[s] || 0) / max * 100}"></i></div></div>`).join('')}</div>
       <div class="panel"><h3>Quantity by type</h3>${Object.entries(qty).map(([k, v]) => `<div class="item" style="margin-bottom:8px"><span>${esc(k)}</span><b>${v}</b></div>`).join('') || '<p class="hint">No data yet.</p>'}
       <h3 style="margin-top:20px">Members</h3><div class="chips">${Object.entries(roles).map(([r, n]) => `<span class="chip" style="padding-right:16px">${ICON[r]} ${LABEL[r]} · ${n}</span>`).join('')}</div></div></div>`;
    requestAnimationFrame(() => main.querySelectorAll('[data-w]').forEach(b => b.style.width = b.dataset.w + '%'));
  },
  async donations(main) {
    const l = await api('donations');
    main.innerHTML = head('All donations', 'Change a status or remove a record.') +
      (l.length ? `<div class="panel tw"><table><tr><th>#</th><th>Item</th><th>Qty</th><th>Expires</th><th>Status</th><th></th></tr>${l.slice().reverse().map(d => `<tr>
        <td>${d.id}</td><td><b>${esc(d.description)}</b><br><small style="color:var(--muted)">${esc(d.type)} · donor ${d.donorId}</small></td><td>${d.quantity} ${esc(d.unit)}</td><td>${d.expiryDate}</td>
        <td><select data-st="${d.id}" class="inline" style="padding:8px 12px;border-radius:12px;border:1.5px solid var(--line);background:var(--cream)">${['AVAILABLE', 'CLAIMED', 'PICKED_UP', 'EXPIRED'].map(s => `<option ${s === d.status ? 'selected' : ''}>${s}</option>`).join('')}</select></td>
        <td><button class="btn btn-danger btn-sm" data-del="${d.id}">Delete</button></td></tr>`).join('')}</table></div>` : empty('📦', 'No donations yet.'));
    main.querySelectorAll('[data-st]').forEach(s => s.onchange = guard(async () => { await api('donations/' + s.dataset.st, 'PUT', { status: s.value }); toast('Status updated'); }));
    main.querySelectorAll('[data-del]').forEach(b => b.onclick = guard(async () => { if (!confirm('Delete donation #' + b.dataset.del + '?')) return; await api('donations/' + b.dataset.del, 'DELETE'); toast('Deleted'); dashboard(); }));
  },
  async users(main) {
    const u = await api('users');
    main.innerHTML = head('Users', `${u.length} members on the platform.`) +
      `<div class="panel tw"><table><tr><th>Name</th><th>E-mail</th><th>Role</th><th></th></tr>${u.map(x => `<tr><td><b>${esc(x.name)}</b></td><td>${esc(x.email)}</td><td>${ICON[x.role]} ${LABEL[x.role]}</td>
      <td>${x.id === S.user.id ? '<small style="color:var(--muted)">you</small>' : `<button class="btn btn-danger btn-sm" data-rm="${x.id}">Remove</button>`}</td></tr>`).join('')}</table></div>`;
    main.querySelectorAll('[data-rm]').forEach(b => b.onclick = guard(async () => { if (!confirm('Remove this user?')) return; await api('users/' + b.dataset.rm, 'DELETE'); toast('User removed'); dashboard(); }));
  },
  async types(main) {
    const t = await api('types');
    main.innerHTML = head('Donation types', 'What donors can choose from.') +
      `<div class="panel"><div class="chips">${t.map(x => `<span class="chip">${esc(x)}<button data-rt="${esc(x)}" title="Remove">✕</button></span>`).join('')}</div>
      <form class="inline" id="tf"><input name="name" placeholder="New type, e.g. Bakery" required><button class="btn btn-accent">Add type</button></form></div>`;
    $('#tf').onsubmit = guard(async e => { e.preventDefault(); const r = await api('types', 'POST', Object.fromEntries(new FormData(e.target))); toast(r.added ? 'Type added' : 'Already exists'); dashboard(); });
    main.querySelectorAll('[data-rt]').forEach(b => b.onclick = guard(async () => { await api('types/' + encodeURIComponent(b.dataset.rt), 'DELETE'); toast('Type removed'); dashboard(); }));
  }
};

function bindClaim(main) {
  main.querySelectorAll('[data-claim]').forEach(b => b.onclick = guard(async () => {
    await api(`donations/${b.dataset.claim}/claim`, 'POST'); toast('Claimed — thank you! 💚'); dashboard();
  }));
}

async function donationModal() {
  const types = await api('types');
  const tomorrow = new Date(Date.now() + 864e5).toISOString().slice(0, 10);
  const o = document.createElement('div');
  o.className = 'overlay';
  o.onclick = e => { if (e.target === o) o.remove(); };
  o.innerHTML = `<div class="modal"><h2>New donation</h2><p class="sub">Tell us what you’d like to share.</p>
    <form id="df"><div class="field"><label>Type</label><select name="type">${types.map(t => `<option>${esc(t)}</option>`).join('')}</select></div>
    <div class="field"><label>Description</label><input name="description" placeholder="e.g. Grilled chicken with rice" required></div>
    <div class="row"><div class="field"><label>Quantity</label><input name="quantity" type="number" min="0.1" step="any" required></div>
    <div class="field"><label>Unit</label><input name="unit" placeholder="kg / meals / boxes" required></div></div>
    <div class="field"><label>Expiry date</label><input name="expiryDate" type="date" min="${new Date().toISOString().slice(0, 10)}" value="${tomorrow}" required></div>
    <button class="btn btn-accent">Share this donation</button></form><div class="err" id="de"></div></div>`;
  document.body.append(o);
  $('#df', o).onsubmit = async e => {
    e.preventDefault();
    try { await api('donations', 'POST', Object.fromEntries(new FormData(e.target))); o.remove(); toast('Donation shared. Thank you! 💚'); dashboard(); }
    catch (er) { $('#de', o).textContent = er.message; }
  };
}

/* ---------------------------------------------------------------- boot */
(async function () {
  if (S.token) {
    try { S.user = await api('me'); return dashboard(); } catch { localStorage.removeItem('fr_token'); S.token = null; }
  }
  landing();
})();
