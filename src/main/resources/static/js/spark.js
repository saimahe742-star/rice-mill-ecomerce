/**
 * Spark burst at click position (relative to element).
 */
function playSpark(container, clientX, clientY) {
  if (!container) return;
  const rect = container.getBoundingClientRect();
  const x = clientX - rect.left;
  const y = clientY - rect.top;
  const layer = document.createElement("div");
  layer.className = "spark-layer";
  container.appendChild(layer);
  const count = 14;
  for (let i = 0; i < count; i++) {
    const p = document.createElement("span");
    p.className = "spark-particle";
    const angle = (Math.PI * 2 * i) / count + Math.random() * 0.4;
    const dist = 40 + Math.random() * 70;
    const dx = Math.cos(angle) * dist;
    const dy = Math.sin(angle) * dist;
    p.style.left = `${x}px`;
    p.style.top = `${y}px`;
    p.style.setProperty("--spark-transform", `translate(${dx}px, ${dy}px) scale(0.2)`);
    p.style.animationDelay = `${i * 0.02}s`;
    layer.appendChild(p);
  }
  setTimeout(() => layer.remove(), 700);
}
