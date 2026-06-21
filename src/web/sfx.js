// Bidirectional Tetris — synthesized sound effects (Web Audio API).
// No audio asset files: every effect is generated procedurally so the app stays
// fully offline and zero-download. Exposes window.TetrisSfx used by the TeaVM build.
(function () {
    'use strict';

    var AC = window.AudioContext || window.webkitAudioContext;
    var ctx = null;
    var master = null;
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

    function tone(freq, startOffset, dur, type, peak) {
        if (!ctx || !master) {
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
        g.connect(master);
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
        play: function (name, arg) {
            if (!enabled || !ensure()) {
                return;
            }
            switch (name) {
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
