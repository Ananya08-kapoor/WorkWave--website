(function () {
  var root = document.documentElement;

  // ---- Light / dark theme toggle (initial value is set inline in <head> to avoid a flash) ----
  var toggle = document.getElementById('themeToggle');
  if (toggle) {
    toggle.addEventListener('click', function () {
      var next = root.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
      root.setAttribute('data-theme', next);
      try { localStorage.setItem('theme', next); } catch (e) { /* storage blocked */ }
    });
  }

  // ---- Register page: show company field for employers, skills fields for job seekers ----
  var radios = document.querySelectorAll('input[name="role"]');
  function syncRole() {
    var chosen = document.querySelector('input[name="role"]:checked');
    if (!chosen) return;
    document.querySelectorAll('[data-role]').forEach(function (el) {
      el.classList.toggle('hidden', el.getAttribute('data-role') !== chosen.value);
    });
  }
  radios.forEach(function (r) { r.addEventListener('change', syncRole); });
  syncRole();
})();
