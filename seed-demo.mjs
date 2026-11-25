#!/usr/bin/env node
/*
 * SmartRecruit demo data — fills EVERY service through the public API (via the gateway), exactly like real users would.
 * Because it uses the API, Kafka events are produced too, so notifications, analytics and CV recommendations fill up.
 *
 *   node seed-demo.mjs                                  (gateway http://localhost:8080, admin from env / defaults)
 *   node seed-demo.mjs --base http://localhost:8080 --admin-email admin@admin.com --admin-password admin
 *
 * Safe to re-run: existing accounts are reused, existing roles are not duplicated.
 * Every demo account uses the password printed at the end.
 */

const args = Object.fromEntries(process.argv.slice(2).reduce((acc, value, i, all) => {
  if (value.startsWith('--')) acc.push([value.slice(2), all[i + 1] && !all[i + 1].startsWith('--') ? all[i + 1] : 'true']);
  return acc;
}, []));
const BASE = (args.base || process.env.SEED_BASE_URL || 'http://localhost:8080').replace(/\/$/, '') + '/api/v1';
const ADMIN_EMAIL = args['admin-email'] || process.env.ADMIN_EMAIL || 'admin@admin.com';
const ADMIN_PASSWORD = args['admin-password'] || process.env.ADMIN_PASSWORD || 'admin';
const PASSWORD = args.password || 'Demo@SmartRecruit2026';
const DOMAIN = 'demo.smartrecruit.test';

// ---------------------------------------------------------------- output helpers
const stats = { ok: 0, skipped: 0, failed: 0 };
const failures = [];
const log = (icon, msg) => console.log(`${icon} ${msg}`);
const step = title => console.log(`\n=== ${title}`);
const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));
// list endpoints may return a plain array or a Spring page ({ content: [...] })
const rows = value => Array.isArray(value) ? value : (value?.content ?? []);
// deterministic Google Meet-style link (the interview service accepts Zoom, Meet, Teams and Webex)
const meetLink = id => { const h = id.replace(/-/g, '').replace(/[^a-z]/g, c => 'abcdefghij'[Number(c) || 0]); return `https://meet.google.com/${h.slice(0, 3)}-${h.slice(3, 7)}-${h.slice(7, 10)}`; };

class ApiError extends Error {
  constructor(status, body, path) { super(`${status} ${path}: ${typeof body === 'string' ? body : body?.message || JSON.stringify(body)}`); this.status = status; this.body = body; }
}

async function api(method, path, { token, json, form } = {}) {
  const headers = {};
  if (token) headers.Authorization = `Bearer ${token}`;
  let body;
  if (json !== undefined) { headers['Content-Type'] = 'application/json'; body = JSON.stringify(json); }
  if (form) body = form;
  let response;
  for (let attempt = 0; ; attempt++) {
    try { response = await fetch(BASE + path, { method, headers, body }); break; }
    catch (e) { if (attempt >= 2) throw new ApiError(0, `cannot reach ${BASE} (${e.cause?.code || e.message})`, path); await sleep(1500); }
  }
  const text = await response.text();
  let data = text;
  try { data = text ? JSON.parse(text) : null; } catch { /* plain text */ }
  if (!response.ok) throw new ApiError(response.status, data, `${method} ${path}`);
  return data;
}

/** Runs one seeding action; a failure is reported but never stops the whole seed. */
async function attempt(label, fn, { quietStatuses = [] } = {}) {
  try { const result = await fn(); stats.ok++; return result; }
  catch (e) {
    if (quietStatuses.includes(e.status)) { stats.skipped++; return undefined; }
    stats.failed++; failures.push(`${label}: ${e.message}`); log('✗', `${label} — ${e.message}`);
    return undefined;
  }
}

// ---------------------------------------------------------------- tiny valid PDF generator (text CVs / proofs)
function makePdf(lines) {
  const esc = s => s.replace(/\\/g, '\\\\').replace(/\(/g, '\\(').replace(/\)/g, '\\)').replace(/[^\x20-\x7e]/g, '?');
  let y = 780;
  const content = ['BT', '/F1 11 Tf'];
  for (const [i, line] of lines.entries()) {
    content.push(`1 0 0 1 56 ${y} Tm`, `(${esc(line)}) Tj`);
    y -= i === 0 ? 24 : 16;
  }
  content.push('ET');
  const stream = content.join('\n');
  const objects = [
    '<< /Type /Catalog /Pages 2 0 R >>',
    '<< /Type /Pages /Kids [3 0 R] /Count 1 >>',
    '<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>',
    `<< /Length ${Buffer.byteLength(stream)} >>\nstream\n${stream}\nendstream`,
    '<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>'
  ];
  let pdf = '%PDF-1.4\n';
  const offsets = [];
  objects.forEach((obj, i) => { offsets.push(Buffer.byteLength(pdf)); pdf += `${i + 1} 0 obj\n${obj}\nendobj\n`; });
  const xref = Buffer.byteLength(pdf);
  pdf += `xref\n0 ${objects.length + 1}\n0000000000 65535 f \n` + offsets.map(o => `${String(o).padStart(10, '0')} 00000 n \n`).join('');
  pdf += `trailer\n<< /Size ${objects.length + 1} /Root 1 0 R >>\nstartxref\n${xref}\n%%EOF\n`;
  return Buffer.from(pdf, 'latin1');
}

// ---------------------------------------------------------------- demo content
const RECRUITERS = [
  { key: 'sarah', fullName: 'Sarah Ben Ali', city: 'Tunis', state: 'approve', plan: 'RECRUITER_PRO',
    business: { name: 'Carthage FinTech', industry: 'Financial technology', city: 'Tunis', country: 'Tunisia', website: 'https://carthage-fintech.example', description: 'Digital payments and lending for North Africa.' },
    jobs: [
      { title: 'Senior Java Developer', location: 'Tunis', contractType: 'Full-time', publish: true, description: 'Build payment microservices with Java 21, Spring Boot, PostgreSQL and Kafka. You will design APIs, review code and mentor two developers.\n\nRequirements: 5+ years Java, Spring, SQL, Docker. Kafka is a plus.' },
      { title: 'Data Analyst', location: 'Tunis (hybrid)', contractType: 'Full-time', publish: true, description: 'Turn transaction data into dashboards and insights. SQL, Python, Power BI. You will work with product and risk teams.' },
      { title: 'DevOps Engineer', location: 'Remote', contractType: 'Contract', publish: true, close: true, description: 'Kubernetes, Docker, CI/CD with GitHub Actions, monitoring with Prometheus and Grafana. 6-month contract.' },
      { title: 'Compliance Officer', location: 'Tunis', contractType: 'Full-time', publish: false, description: 'Draft — monitor regulatory requirements, run audits and maintain AML policies.' }
    ] },
  { key: 'karim', fullName: 'Karim Trabelsi', city: 'Sfax', state: 'approve', plan: 'RECRUITER_STARTER',
    business: { name: 'Medina Health', industry: 'Healthcare', city: 'Sfax', country: 'Tunisia', website: 'https://medina-health.example', description: 'Telemedicine platform connecting patients and clinics.' },
    jobs: [
      { title: 'Angular Frontend Developer', location: 'Sfax', contractType: 'Full-time', publish: true, description: 'Build our patient portal with Angular, TypeScript, RxJS and accessible design. Experience with REST APIs and testing.' },
      { title: 'Healthcare Data Scientist', location: 'Remote', contractType: 'Full-time', publish: true, description: 'Python, machine learning, pandas and SQL to predict patient no-shows and improve care pathways.' },
      { title: 'Customer Support Intern', location: 'Sfax', contractType: 'Internship', publish: true, description: 'Help clinics onboard, answer tickets in French and English, write help articles. 6-month internship.' }
    ] },
  { key: 'amira', fullName: 'Amira Haddad', city: 'Sousse', state: 'approve', plan: 'RECRUITER_PRO',
    business: { name: 'Sahel Logistics', industry: 'Logistics', city: 'Sousse', country: 'Tunisia', website: 'https://sahel-logistics.example', description: 'Smart freight and warehouse management.' },
    jobs: [
      { title: 'Full Stack Developer', location: 'Sousse', contractType: 'Full-time', publish: true, description: 'Node.js, React or Angular, PostgreSQL and Docker. Build the tracking platform used by 300 drivers.' },
      { title: 'Product Designer', location: 'Remote', contractType: 'Freelance', publish: true, description: 'Design flows for our driver and dispatcher apps with Figma. Strong UX research and prototyping skills.' },
      { title: 'Warehouse Operations Lead', location: 'Sousse', contractType: 'Full-time', publish: true, description: 'Lead a team of 12, optimise picking routes and inventory accuracy. Excel and WMS experience.' }
    ] },
  { key: 'youssef', fullName: 'Youssef Gharbi', city: 'Tunis', state: 'approve', plan: null, adminBusiness: true,
    business: null,
    jobs: [
      { title: 'Mobile Developer (Flutter)', location: 'Tunis', contractType: 'Full-time', publish: true, description: 'Build our iOS and Android app with Flutter and Dart. Firebase, REST APIs, CI/CD for mobile.' },
      { title: 'QA Automation Engineer', location: 'Tunis', contractType: 'Part-time', publish: true, description: 'Selenium, Cypress and API testing with Postman. Write automated test suites for web and mobile.' }
    ] },
  { key: 'leila', fullName: 'Leila Mansour', city: 'Bizerte', state: 'pending' },
  { key: 'omar', fullName: 'Omar Jaziri', city: 'Gabes', state: 'pending' },
  { key: 'nadia', fullName: 'Nadia Khelifi', city: 'Nabeul', state: 'reject' }
];

const CANDIDATES = [
  { key: 'ahmed', fullName: 'Ahmed Bouazizi', city: 'Tunis', skills: 'Java, Spring Boot, PostgreSQL, Kafka, Docker, REST APIs, Git', title: 'Backend developer, 6 years', plan: 'CANDIDATE_PLUS' },
  { key: 'ines', fullName: 'Ines Chaabane', city: 'Ariana', skills: 'Angular, TypeScript, RxJS, HTML, CSS, accessibility, Jest', title: 'Frontend developer, 4 years' },
  { key: 'mehdi', fullName: 'Mehdi Saidi', city: 'Sfax', skills: 'Python, pandas, SQL, machine learning, Power BI, statistics', title: 'Data analyst, 3 years', plan: 'CANDIDATE_PLUS' },
  { key: 'sana', fullName: 'Sana Rekik', city: 'Sousse', skills: 'Figma, UX research, prototyping, design systems, user testing', title: 'Product designer, 5 years' },
  { key: 'bilel', fullName: 'Bilel Hamdi', city: 'Tunis', skills: 'Kubernetes, Docker, CI/CD, Prometheus, Grafana, Linux, Terraform', title: 'DevOps engineer, 5 years' },
  { key: 'rania', fullName: 'Rania Ferchichi', city: 'Monastir', skills: 'Node.js, React, Angular, PostgreSQL, Docker, TypeScript', title: 'Full stack developer, 3 years' },
  { key: 'walid', fullName: 'Walid Ayari', city: 'Tunis', skills: 'Flutter, Dart, Firebase, REST APIs, mobile CI/CD', title: 'Mobile developer, 2 years' },
  { key: 'salma', fullName: 'Salma Jebali', city: 'Sfax', skills: 'Selenium, Cypress, Postman, API testing, Java', title: 'QA engineer, 4 years' },
  { key: 'hamza', fullName: 'Hamza Toumi', city: 'Bizerte', skills: 'Excel, inventory, logistics, team leadership, WMS', title: 'Operations supervisor, 7 years' },
  { key: 'yasmine', fullName: 'Yasmine Karray', city: 'Tunis', skills: 'customer support, French, English, Zendesk, writing', title: 'Student, support assistant', block: true }
];

// candidate key -> [job title, final application status, interview?]
const APPLICATIONS = [
  ['ahmed', 'Senior Java Developer', 'INTERVIEW', 'upcoming'], ['ahmed', 'Full Stack Developer', 'REVIEWING'], ['ahmed', 'QA Automation Engineer', 'REJECTED'],
  ['ines', 'Angular Frontend Developer', 'HIRED', 'completed'], ['ines', 'Full Stack Developer', 'INTERVIEW', 'upcoming'],
  ['mehdi', 'Data Analyst', 'INTERVIEW', 'upcoming'], ['mehdi', 'Healthcare Data Scientist', 'REVIEWING'],
  ['sana', 'Product Designer', 'INTERVIEW', 'cancelled'], ['sana', 'Angular Frontend Developer', 'REJECTED'],
  ['bilel', 'DevOps Engineer', 'REVIEWING'], ['bilel', 'Senior Java Developer', 'PENDING'],
  ['rania', 'Full Stack Developer', 'HIRED'], ['rania', 'Angular Frontend Developer', 'PENDING'], ['rania', 'Senior Java Developer', 'PENDING'],
  ['walid', 'Mobile Developer (Flutter)', 'INTERVIEW', 'upcoming'],
  ['salma', 'QA Automation Engineer', 'REVIEWING'], ['salma', 'Mobile Developer (Flutter)', 'PENDING'],
  ['hamza', 'Warehouse Operations Lead', 'INTERVIEW', 'upcoming'],
  ['yasmine', 'Customer Support Intern', 'PENDING']
];
const STAGES = ['PENDING', 'REVIEWING', 'INTERVIEW', 'HIRED'];
const email = key => `${key}@${DOMAIN}`;

// ---------------------------------------------------------------- flows
async function login(address, password) {
  return (await api('POST', '/auth/login', { json: { email: address, password } })).accessToken;
}

async function ensureCandidate(c) {
  const body = { email: email(c.key), password: PASSWORD, fullName: c.fullName, city: c.city, country: 'Tunisia', phone: '+216 20 000 000' };
  await attempt(`register candidate ${c.fullName}`, () => api('POST', '/auth/register/candidate', { json: body }), { quietStatuses: [409] });
}

async function ensureRecruiter(r) {
  const form = new FormData();
  form.append('registration', new Blob([JSON.stringify({ email: email(r.key), password: PASSWORD, fullName: r.fullName, city: r.city, country: 'Tunisia' })], { type: 'application/json' }));
  const proof = makePdf([`Employment certificate`, `This certifies that ${r.fullName} is employed as Talent Acquisition Manager.`, `Issued for SmartRecruit demo purposes.`]);
  form.append('employmentProof', new Blob([proof], { type: 'application/pdf' }), `${r.key}-employment-proof.pdf`);
  await attempt(`register recruiter ${r.fullName}`, () => api('POST', '/auth/register/recruiter', { form }), { quietStatuses: [409] });
}

async function main() {
  console.log(`SmartRecruit demo seed → ${BASE}`);

  step('Administrator');
  let admin;
  try { admin = await login(ADMIN_EMAIL, ADMIN_PASSWORD); log('✓', `signed in as ${ADMIN_EMAIL}`); }
  catch (e) {
    console.error(`\nCannot sign in as the administrator (${e.message}).\n` +
      `Pass the right credentials:  node seed-demo.mjs --admin-email you@x --admin-password ...\n` +
      `or set ADMIN_RESET_PASSWORD=true in run-all.bat once so ADMIN_PASSWORD becomes the admin password.`);
    process.exit(1);
  }

  step('Accounts (identity service)');
  for (const c of CANDIDATES) await ensureCandidate(c);
  for (const r of RECRUITERS) await ensureRecruiter(r);
  const users = await api('GET', '/admin/users', { token: admin });
  const byEmail = Object.fromEntries(users.map(u => [u.email, u]));
  log('✓', `${users.length} accounts on the platform`);

  step('Recruiter review (admin)');
  for (const r of RECRUITERS) {
    const user = byEmail[email(r.key)];
    if (!user) continue;
    if (r.state === 'approve' && user.status !== 'ACTIVE') await attempt(`approve ${r.fullName}`, () => api('POST', `/admin/recruiters/${user.id}/approve`, { token: admin }));
    if (r.state === 'reject' && user.status === 'PENDING') await attempt(`reject ${r.fullName}`, () => api('POST', `/admin/recruiters/${user.id}/reject`, { token: admin }));
    log('·', `${r.fullName}: ${r.state === 'approve' ? 'approved' : r.state === 'reject' ? 'rejected' : 'left pending for you to review'}`);
  }

  step('Businesses and job offers (recruitment service)');
  const tokens = {};
  const jobsByTitle = {};
  for (const r of RECRUITERS.filter(x => x.state === 'approve')) {
    const token = tokens[r.key] = await attempt(`login ${r.fullName}`, () => login(email(r.key), PASSWORD));
    if (!token) continue;
    let businesses = await api('GET', '/recruiter/businesses', { token });
    if (!businesses.length) {
      if (r.adminBusiness) {
        const owner = byEmail[email(r.key)];
        await attempt(`admin creates business for ${r.fullName}`, () => api('POST', '/admin/businesses', { token: admin, json: { ownerUserId: owner.id, name: 'Djerba Digital Studio', industry: 'Mobile & web agency', city: 'Tunis', country: 'Tunisia', website: 'https://djerba-digital.example', description: 'Agency building mobile apps for retail and tourism.' } }));
      } else {
        await attempt(`create business ${r.business.name}`, () => api('POST', '/recruiter/businesses', { token, json: r.business }));
      }
      businesses = await api('GET', '/recruiter/businesses', { token });
    }
    const business = businesses[0];
    if (!business) continue;
    const existing = rows(await api('GET', '/recruiter/jobs?size=200', { token }));
    for (const j of r.jobs) {
      let job = existing.find(x => x.title === j.title && x.status !== 'DELETED');
      if (!job) {
        job = await attempt(`create job ${j.title}`, () => api('POST', '/recruiter/jobs', { token, json: { businessId: business.id, title: j.title, description: j.description, location: j.location, contractType: j.contractType,
          expiresAt: new Date(Date.now() + 45 * 86400000).toISOString() } }));
        if (!job) continue;
      }
      // jobs are published (OPEN) on creation; roles marked publish:false are kept closed
      if (!j.publish && job.status === 'OPEN') job = (await attempt(`keep ${j.title} closed`, () => api('POST', `/recruiter/jobs/${job.id}/close`, { token }))) || job;
      jobsByTitle[j.title] = { ...job, recruiterKey: r.key, close: j.close };
    }
    log('✓', `${r.fullName}: ${business.name}, ${r.jobs.length} roles`);
  }
  // a second, deactivated business so the admin sees both states
  const sarah = byEmail[email('sarah')];
  if (sarah) {
    const all = await attempt('list businesses', () => api('GET', '/admin/businesses', { token: admin })) || [];
    if (!all.some(b => b.name === 'Carthage Insurance (closed)')) {
      const old = await attempt('create old business', () => api('POST', '/admin/businesses', { token: admin, json: { ownerUserId: sarah.id, name: 'Carthage Insurance (closed)', industry: 'Insurance', city: 'Tunis', country: 'Tunisia' } }));
      if (old) await attempt('deactivate old business', () => api('DELETE', `/admin/businesses/${old.id}`, { token: admin }));
    }
  }

  step('Subscriptions (subscription service)');
  for (const r of RECRUITERS.filter(x => x.plan && tokens[x.key])) {
    await attempt(`${r.fullName} subscribes to ${r.plan}`, () => api('POST', '/recruiter/subscription', { token: tokens[r.key], json: { planCode: r.plan, autoRenew: true } }), { quietStatuses: [409] });
  }

  step('Candidates: CVs, ATS scans, applications (candidate-ai + recruitment)');
  const candidateTokens = {};
  const cvIds = {};
  for (const c of CANDIDATES) {
    if (byEmail[email(c.key)]?.status === 'INACTIVE') { log('·', `${c.fullName} is blocked — skipped (already seeded)`); continue; }
    const token = candidateTokens[c.key] = await attempt(`login ${c.fullName}`, () => login(email(c.key), PASSWORD));
    if (!token) continue;
    await attempt(`${c.fullName} default plan`, () => api('GET', '/candidate/subscription', { token }));
    if (c.plan) await attempt(`${c.fullName} upgrades to ${c.plan}`, () => api('POST', '/candidate/subscription/upgrade', { token, json: { planCode: c.plan, autoRenew: true } }), { quietStatuses: [409, 400] });
    let cvs = await attempt(`${c.fullName} list CVs`, () => api('GET', '/candidate/cvs', { token })) || [];
    let cv = cvs.find(x => x.status === 'ACTIVE');
    if (!cv) {
      const pdf = makePdf([`${c.fullName}`, `${c.title} - ${c.city}, Tunisia - ${email(c.key)}`, '', 'PROFILE', `Experienced professional: ${c.title}.`, '', 'SKILLS', c.skills, '', 'EXPERIENCE',
        '2021 - today: delivered projects using ' + c.skills.split(', ').slice(0, 3).join(', ') + '.', '2018 - 2021: earlier role in the same field.', '', 'EDUCATION', 'Bachelor degree, University of Tunis']);
      const form = new FormData();
      form.append('file', new Blob([pdf], { type: 'application/pdf' }), `CV-${c.fullName.replace(/\s+/g, '-')}.pdf`);
      cv = await attempt(`${c.fullName} uploads CV`, () => api('POST', '/candidate/cvs', { token, form }));
      if (cv) await attempt(`${c.fullName} ATS scan`, () => api('POST', `/candidate/cvs/${cv.id}/ats-scan`, { token, json: { jobDescription: c.skills } }));
    }
    cvIds[c.key] = cv?.id;
  }

  const applied = [];
  const mine = {};
  for (const c of CANDIDATES) mine[c.key] = candidateTokens[c.key] ? rows(await attempt(`${c.key} applications`, () => api('GET', '/candidate/applications?size=200', { token: candidateTokens[c.key] }))) : [];
  for (const [key, title, finalStatus, interview] of APPLICATIONS) {
    const job = jobsByTitle[title];
    const token = candidateTokens[key];
    if (!job || !token) continue;
    let app = mine[key].find(a => a.jobId === job.id);
    if (!app) {
      if (!cvIds[key]) { failures.push(`${key} → ${title}: no CV (CV service unavailable?)`); continue; }
      const c = CANDIDATES.find(x => x.key === key);
      app = await attempt(`${key} applies to ${title}`, () => api('POST', '/applications', { token, json: { jobId: job.id, candidateCvId: cvIds[key],
        message: `Hello, I'm ${c.fullName}. My background (${c.skills.split(', ').slice(0, 3).join(', ')}) fits this role well and I can start within a month.` } }), { quietStatuses: [400] });
    }
    if (app) applied.push({ app, job, finalStatus, interview, candidateKey: key });
  }
  log('✓', `${applied.length} applications`);

  step('Recruiters review applications and schedule interviews (recruitment + interview)');
  let interviewCount = 0;
  for (const { app, job, finalStatus, interview, candidateKey } of applied) {
    const token = tokens[job.recruiterKey];
    if (!token) continue;
    // walk the pipeline step by step so the candidate gets a realistic history of notifications
    const target = finalStatus === 'REJECTED' ? ['REVIEWING', 'REJECTED'] : STAGES.slice(1, STAGES.indexOf(finalStatus) + 1);
    let current = app.status;
    for (const status of target) {
      if (status === current || STAGES.indexOf(current) > STAGES.indexOf(status) || current === 'REJECTED' || current === 'HIRED') continue;
      const updated = await attempt(`${job.title}: ${candidateKey} → ${status}`, () => api('PATCH', `/recruiter/applications/${app.id}/status`, { token, json: { status } }));
      if (updated) current = updated.status;
    }
    if (interview) {
      const existing = await attempt('list interviews', () => api('GET', '/recruiter/interviews', { token })) || [];
      if (existing.some(i => i.applicationId === app.id)) continue;
      const days = 2 + (interviewCount++ % 6);
      const when = new Date(Date.now() + days * 86400000); when.setHours(10 + (interviewCount % 6), 0, 0, 0);
      const c = CANDIDATES.find(x => x.key === candidateKey);
      const created = await attempt(`interview ${candidateKey} for ${job.title}`, () => api('POST', '/recruiter/interviews', { token, json: {
        applicationId: app.id, candidateId: app.candidateId, jobId: job.id, jobTitle: job.title, candidateName: c.fullName,
        scheduledAt: when.toISOString(), durationMinutes: 45, externalMeetingUrl: meetLink(app.id),
        notes: 'Technical discussion with the hiring manager, then 15 minutes for your questions.' } }));
      if (created && interview === 'completed') await attempt('complete interview', () => api('POST', `/recruiter/interviews/${created.id}/done`, { token }));
      if (created && interview === 'cancelled') await attempt('cancel interview', () => api('POST', `/recruiter/interviews/${created.id}/cancel`, { token }));
    }
  }
  log('✓', `${interviewCount} new interviews scheduled`);

  step('Closing roles (recruiter and admin moderation)');
  for (const job of Object.values(jobsByTitle)) {
    if (job.close && job.status === 'OPEN') await attempt(`close ${job.title}`, () => api('POST', `/recruiter/jobs/${job.id}/close`, { token: tokens[job.recruiterKey] }), { quietStatuses: [400] });
  }
  // the admin closes one offer from the console's moderation tab (shows an admin-closed role)
  const moderated = jobsByTitle['Customer Support Intern'];
  if (moderated && moderated.status === 'OPEN') await attempt('admin closes Customer Support Intern', () => api('POST', `/admin/jobs/${moderated.id}/close`, { token: admin }), { quietStatuses: [400, 409] });

  step('Account moderation (admin)');
  for (const c of CANDIDATES.filter(x => x.block)) {
    const user = byEmail[email(c.key)];
    if (user && user.status === 'ACTIVE') await attempt(`block ${c.fullName}`, () => api('PATCH', `/admin/users/${user.id}`, { token: admin, json: { status: 'INACTIVE' } }));
  }

  step('Waiting for events to reach notifications and analytics (Kafka)');
  await sleep(8000);
  const probes = [
    ['notifications (Ahmed)', () => api('GET', '/notifications', { token: candidateTokens.ahmed }).then(list => `${rows(list).length} notifications`)],
    ['analytics (Sarah)', () => api('GET', '/recruiter/analytics/overview', { token: tokens.sarah }).then(o => `${o.applications} applications, ${o.publishedJobs} published offers`)],
    ['recommendations (Ahmed)', () => api('GET', '/candidate/recommendations', { token: candidateTokens.ahmed }).then(r => `${r.length} recommended roles`)],
    ['admin stats', () => api('GET', '/admin/recruitment/stats', { token: admin }).then(s => `${s.jobsOPEN} open, ${s.applications} applications, ${s.businesses} businesses`)],
    ['company subscriptions', () => api('GET', '/admin/subscription/company-subscriptions', { token: admin }).then(p => `${p.totalElements ?? p.content?.length} recruiter subscriptions`)]
  ];
  for (const [name, probe] of probes) {
    if (!probe) continue;
    try { log('✓', `${name}: ${await probe()}`); }
    catch (e) { log('!', `${name}: ${e.message}`); }
  }

  console.log(`\n────────────────────────────────────────────────────────────`);
  console.log(`Done: ${stats.ok} actions succeeded, ${stats.skipped} already existed, ${stats.failed} failed.`);
  if (failures.length) { console.log('\nProblems (the rest of the data was still created):'); failures.forEach(f => console.log('  - ' + f)); }
  console.log(`\nDemo accounts — password for all: ${PASSWORD}`);
  console.log(`  Admin       ${ADMIN_EMAIL} (your admin password)`);
  console.log(`  Recruiters  ${RECRUITERS.filter(r => r.state === 'approve').map(r => email(r.key)).join(', ')}`);
  console.log(`  Pending     ${RECRUITERS.filter(r => r.state === 'pending').map(r => email(r.key)).join(', ')}  (approve them in the admin console)`);
  console.log(`  Rejected    ${email('nadia')}`);
  console.log(`  Candidates  ${CANDIDATES.filter(c => !c.block).map(c => email(c.key)).join(', ')}`);
  console.log(`  Blocked     ${email('yasmine')}  (unblock in Admin console → Users)`);
  process.exit(stats.failed ? 2 : 0);
}

main().catch(e => { console.error('Seed stopped:', e.message); process.exit(1); });
