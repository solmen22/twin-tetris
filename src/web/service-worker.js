// Bidirectional Tetris — offline cache service worker.
// Strategy: stale-while-revalidate for all same-origin GETs. The cache is served
// immediately for speed, but every request also refreshes the cache in the
// background, so a new deploy is picked up on the next load even if CACHE_VERSION
// is not bumped (this fixes the previous "permanently stale build" risk).
const CACHE_VERSION = 'v7-2026-06-21';
const CACHE_NAME = `bidirectional-tetris-${CACHE_VERSION}`;

// Critical assets must all cache for offline play to work.
const CRITICAL_ASSETS = [
    './',
    './index.html',
    './style.css',
    './sfx.js',
    './tetris.js',
    './manifest.webmanifest'
];

// Optional assets: nice to have offline, but a 404 must not abort installation.
const OPTIONAL_ASSETS = [
    './terms.html',
    './privacy.html',
    './icon.svg',
    './icon-192.png',
    './icon-512.png',
    './screenshot-wide.png',
    './screenshot-narrow.png'
];

self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME).then((cache) =>
            cache.addAll(CRITICAL_ASSETS).then(() =>
                // Best-effort: cache optional assets without failing the install.
                Promise.allSettled(OPTIONAL_ASSETS.map((url) => cache.add(url)))
            )
        )
    );
    self.skipWaiting();
});

self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then((keys) =>
            Promise.all(
                keys
                    .filter((key) => key.startsWith('bidirectional-tetris-') && key !== CACHE_NAME)
                    .map((key) => caches.delete(key))
            )
        )
    );
    self.clients.claim();
});

self.addEventListener('fetch', (event) => {
    if (event.request.method !== 'GET') {
        return;
    }
    const url = new URL(event.request.url);
    if (url.origin !== self.location.origin) {
        return;
    }
    event.respondWith(staleWhileRevalidate(event.request));
});

async function staleWhileRevalidate(request) {
    const cache = await caches.open(CACHE_NAME);
    const cached = await cache.match(request);
    const networkFetch = fetch(request)
        .then((response) => {
            if (response && response.status === 200 && response.type === 'basic') {
                cache.put(request, response.clone());
            }
            return response;
        })
        .catch(() => null);
    if (cached) {
        // Serve cache now; the network fetch updates the cache in the background.
        return cached;
    }
    const network = await networkFetch;
    if (network) {
        return network;
    }
    if (request.mode === 'navigate') {
        const fallback = await cache.match('./index.html');
        if (fallback) {
            return fallback;
        }
    }
    return Response.error();
}
