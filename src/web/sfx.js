// Bidirectional Tetris — synthesized audio (Web Audio API).
// No audio asset files: every effect AND the background music are generated
// procedurally so the app stays fully offline and zero-download.
// Exposes window.TetrisSfx, used by the TeaVM build.
(function () {
    'use strict';

    var AC = window.AudioContext || window.webkitAudioContext;
    var ctx = null;
    var master = null;   // SFX bus
    var enabled = true;
    var volume = 0.45;

    function ensure() {
        if (!AC) {
            return null;
        }
        if (!ctx) {
            try {
                ctx = new AC();
                master = ctx.createGain();
                master.gain.value = volume;
                master.connect(ctx.destination);
            } catch (e) {
                ctx = null;
                return null;
            }
        }
        if (ctx.state === 'suspended' && ctx.resume) {
            ctx.resume();
        }
        return ctx;
    }

    function tone(freq, startOffset, dur, type, peak, bus) {
        if (!ctx) {
            return;
        }
        var dest = bus || master;
        if (!dest) {
            return;
        }
        var t0 = ctx.currentTime + startOffset;
        var osc = ctx.createOscillator();
        var g = ctx.createGain();
        osc.type = type || 'square';
        osc.frequency.setValueAtTime(freq, t0);
        g.gain.setValueAtTime(0.0001, t0);
        g.gain.exponentialRampToValueAtTime(Math.max(0.0001, peak || 0.3), t0 + 0.008);
        g.gain.exponentialRampToValueAtTime(0.0001, t0 + dur);
        osc.connect(g);
        g.connect(dest);
        osc.start(t0);
        osc.stop(t0 + dur + 0.02);
    }

    function sweep(f1, f2, dur, type, peak) {
        if (!ctx || !master) {
            return;
        }
        var t0 = ctx.currentTime;
        var osc = ctx.createOscillator();
        var g = ctx.createGain();
        osc.type = type || 'sawtooth';
        osc.frequency.setValueAtTime(f1, t0);
        osc.frequency.exponentialRampToValueAtTime(Math.max(1, f2), t0 + dur);
        g.gain.setValueAtTime(0.0001, t0);
        g.gain.exponentialRampToValueAtTime(peak || 0.3, t0 + 0.01);
        g.gain.exponentialRampToValueAtTime(0.0001, t0 + dur);
        osc.connect(g);
        g.connect(master);
        osc.start(t0);
        osc.stop(t0 + dur + 0.02);
    }

    function arp(freqs, step, dur, type, peak) {
        for (var i = 0; i < freqs.length; i++) {
            tone(freqs[i], i * step, dur, type, peak);
        }
    }

    // ---------- Background music (procedural chiptune loop) ----------
    var bgm = {
        playing: false,
        enabled: true,
        volume: 0.22,
        gain: null,
        timer: null,
        nextNoteTime: 0,
        step: 0
    };

    var TEMPO = 126;
    var STEP_DUR = 60 / TEMPO / 2; // eighth notes

    // A natural-minor-ish loop (16 steps). null = rest.
    var LEAD = [440, null, 523, 587, 659, null, 587, 523, 440, null, 392, 440, 330, null, 392, 440];
    var BASS = [110, null, 110, null, 131, null, 131, null, 98, null, 98, null, 110, null, 110, null];

    function bgmBus() {
        if (!bgm.gain) {
            bgm.gain = ctx.createGain();
            bgm.gain.gain.value = bgm.volume;
            bgm.gain.connect(ctx.destination);
        }
        return bgm.gain;
    }

    function bgmScheduler() {
        if (!ctx || !bgm.playing) {
            return;
        }
        while (bgm.nextNoteTime < ctx.currentTime + 0.15) {
            var i = bgm.step % 16;
            var bus = bgmBus();
            if (LEAD[i]) {
                tone(LEAD[i], bgm.nextNoteTime - ctx.currentTime, STEP_DUR * 0.95, 'square', 0.18, bus);
            }
            if (BASS[i]) {
                tone(BASS[i], bgm.nextNoteTime - ctx.currentTime, STEP_DUR * 1.6, 'triangle', 0.28, bus);
            }
            bgm.nextNoteTime += STEP_DUR;
            bgm.step++;
        }
    }

    function startBgm() {
        if (!bgm.enabled || !ensure() || bgm.playing) {
            return;
        }
        bgm.playing = true;
        bgm.step = 0;
        bgm.nextNoteTime = ctx.currentTime + 0.08;
        if (bgm.gain) {
            bgm.gain.gain.value = bgm.volume;
        }
        bgmScheduler();
        bgm.timer = window.setInterval(bgmScheduler, 30);
    }

    function stopBgm() {
        bgm.playing = false;
        if (bgm.timer) {
            window.clearInterval(bgm.timer);
            bgm.timer = null;
        }
        if (bgm.gain) {
            // 残りスケジュール分を素早くフェードアウト
            try {
                bgm.gain.gain.setTargetAtTime(0.0001, ctx.currentTime, 0.05);
            } catch (e) { /* ignore */ }
        }
    }

    window.TetrisSfx = {
        unlock: function () {
            ensure();
        },
        setEnabled: function (on) {
            enabled = !!on;
        },
        setVolume: function (v) {
            volume = Math.max(0, Math.min(1, v));
            if (master) {
                master.gain.value = volume;
            }
        },
        setBgmEnabled: function (on) {
            bgm.enabled = !!on;
            if (!bgm.enabled) {
                stopBgm();
            }
        },
        setBgmVolume: function (v) {
            bgm.volume = Math.max(0, Math.min(1, v));
            if (bgm.gain) {
                bgm.gain.gain.value = bgm.volume;
            }
        },
        startBgm: startBgm,
        stopBgm: stopBgm,
        play: function (name, arg) {
            if (!enabled || !ensure()) {
                return;
            }
            switch (name) {
                case 'click': tone(660, 0, 0.03, 'square', 0.10); break;
                case 'move': tone(200, 0, 0.04, 'triangle', 0.10); break;
                case 'rotate': tone(330, 0, 0.05, 'triangle', 0.14); break;
                case 'harddrop': sweep(170, 70, 0.10, 'square', 0.20); break;
                case 'hold': tone(440, 0, 0.05, 'square', 0.14); break;
                case 'lock': tone(120, 0, 0.05, 'square', 0.16); break;
                case 'lineclear': {
                    var n = Math.max(1, arg || 1);
                    var base = [523, 659, 784, 988, 1175];
                    arp(base.slice(0, Math.min(base.length, n + 1)), 0.045, 0.12, 'square', 0.18);
                    break;
                }
                case 'center': arp([587, 880, 1175], 0.05, 0.15, 'triangle', 0.22); break;
                case 'tetris': arp([523, 659, 784, 1047], 0.05, 0.18, 'sawtooth', 0.20); break;
                case 'levelup': arp([659, 880, 1109, 1318], 0.06, 0.16, 'triangle', 0.20); break;
                case 'gameover': arp([392, 311, 262, 196], 0.12, 0.32, 'sawtooth', 0.20); break;
                case 'success': arp([523, 659, 784], 0.06, 0.14, 'triangle', 0.20); break;
                default: break;
            }
        }
    };

    // Resume the AudioContext on the first user gesture (browser autoplay policy).
    function unlockOnce() {
        ensure();
        window.removeEventListener('pointerdown', unlockOnce);
        window.removeEventListener('keydown', unlockOnce);
        window.removeEventListener('touchstart', unlockOnce);
    }
    window.addEventListener('pointerdown', unlockOnce);
    window.addEventListener('keydown', unlockOnce);
    window.addEventListener('touchstart', unlockOnce);
})();
