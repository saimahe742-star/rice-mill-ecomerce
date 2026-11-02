const FOOTER_HTML = `
<footer class="footer-mill py-4 mt-auto">
  <div class="container">
    <div class="row g-4">
      <div class="col-md-4">
        <h6 class="text-white mb-2">Sri Devi Modern Rice Mill</h6>
        <p class="small mb-0">294, Anthiyur Main Road, Thotipalayam, Bhavani – 638312</p>
      </div>
      <div class="col-md-4">
        <h6 class="text-white mb-2">Contact</h6>
        <p class="small mb-1"><i class="bi bi-telephone me-1"></i> <a href="tel:9965749999">9965749999</a>, <a href="tel:9865639999">9865639999</a></p>
        <p class="small mb-0"><i class="bi bi-envelope me-1"></i> <a href="mailto:deviricemill@gmail.com">deviricemill@gmail.com</a></p>
      </div>
      <div class="col-md-4">
        <h6 class="text-white mb-2">Hours</h6>
        <p class="small mb-0">6:30 AM – 8:00 PM (daily)</p>
      </div>
    </div>
    <hr class="border-secondary my-3" />
    <p class="small text-center mb-0">&copy; ${new Date().getFullYear()} Sri Devi Modern Rice Mill — Online Delivery</p>
  </div>
</footer>
`;

async function fetchMe() {
  try {
    const r = await fetch("/api/auth/me", { credentials: "same-origin" });
    if (!r.ok) return { authenticated: false };
    return await r.json();
  } catch {
    return { authenticated: false };
  }
}

function navLink(href, text, activePath) {
  const active = window.location.pathname.endsWith(href) ? " active" : "";
  return `<li class="nav-item"><a class="nav-link${active}" href="${href}">${text}</a></li>`;
}

async function renderNavbar(brandHref = "/index.html") {
  const el = document.getElementById("nav-root");
  if (!el) return;
  const me = await fetchMe();
  const admin =
    me.authenticated && me.role === "ROLE_ADMIN"
      ? navLink("/admin.html", "Admin", "/admin.html")
      : "";
  const shop =
    me.authenticated && me.role === "ROLE_USER"
      ? `${navLink("/products.html", "Products")}
          ${navLink("/cart.html", "Cart")}
          ${navLink("/checkout.html", "Checkout")}
          ${navLink("/tracking.html", "Track Order")}
          ${navLink("/feedback.html", "Returns & Feedback")}`
      : "";
  const auth = me.authenticated
    ? `${me.role === "ROLE_USER" ? navLink("/account.html", "Account") : ""}
       <li class="nav-item"><span class="nav-link disabled">${me.phone ? me.phone : me.name}</span></li>
       <li class="nav-item"><a class="nav-link" href="/logout">Logout</a></li>`
    : `${navLink("/customer-login.html", "Customer login")}${navLink("/login.html", "Admin login")}`;

  el.innerHTML = `
  <nav class="navbar navbar-expand-lg navbar-dark bg-success shadow-sm">
    <div class="container">
      <a class="navbar-brand" href="${brandHref}"><i class="bi bi-leaf-fill me-2"></i>Sri Devi Modern Rice Mill</a>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navMain">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navMain">
        <ul class="navbar-nav ms-auto align-items-lg-center">
          ${navLink("/index.html", "Home")}
          ${shop}
          ${admin}
          ${auth}
        </ul>
      </div>
    </div>
  </nav>`;
}

function renderFooter() {
  const el = document.getElementById("footer-root");
  if (el) el.innerHTML = FOOTER_HTML;
}

document.addEventListener("DOMContentLoaded", () => {
  const iconHref = "/images/sample-rice.svg?v=2";
  const icon = document.querySelector("link[rel='icon']") || document.createElement("link");
  icon.type = "image/svg+xml";
  icon.rel = "icon";
  icon.href = iconHref;
  if (!document.head.contains(icon)) document.head.appendChild(icon);

  const shortcut = document.querySelector("link[rel='shortcut icon']") || document.createElement("link");
  shortcut.type = "image/svg+xml";
  shortcut.rel = "shortcut icon";
  shortcut.href = iconHref;
  if (!document.head.contains(shortcut)) document.head.appendChild(shortcut);

  renderNavbar();
  renderFooter();
});

/** Call after profile updates so the nav shows the latest name without a full reload. */
window.refreshNavbar = () => {
  renderNavbar();
};
