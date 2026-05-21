// Bidirectional Tetris — offline cache service worker
// Bump CACHE_VERSION whenever any cached asset changes so that returning users
// pick up the new build on next activation.
const CACHE_VERSION = 'v6-2026-05-21';
const CACHE_NAME = `bidirectional-tetris-${CACHE_VERSION}`;

const STATIC_ASSETS = [
    './',
    './index.html',
    './terms.html',
    './privacy.html',
    './style.css',
    './tetris.js',
    './manifest.webmanifest',
    './icon.svg',
    './icon-192.png',
    './icon-512.png',
    './screenshot-wide.png',
    './screenshot-narrow.png'
];

self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME).then((cache) => cache.addAll(STATIC_ASSETS))
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
    event.respondWith(
        caches.match(event.request).then((cached) => {
            if (cached) {
                return cached;
            }
            return fetch(event.request)
                .then((response) => {
                    if (response && response.status === 200 && response.type === 'basic') {
                        const clone = response.clone();
                        caches.open(CACHE_NAME).then((cache) => cache.put(event.request, clone));
                    }
                    return response;
                })
                .catch(() => {
                    if (event.request.mode === 'navigate') {
                        return caches.match('./index.html');
                    }
                    return Response.error();
                });
        })
    );
});
