package com.example.tetris.web;

import org.teavm.jso.browser.Storage;
import org.teavm.jso.browser.Window;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SettingsStore {

    private static final String KEY_PREFIX_BIND = "tt.bind.";
    private static final String KEY_PREFIX_BEST = "tt.best.";

    private final Storage storage;
    private final EnumMap<Action, List<String>> bindings = new EnumMap<>(Action.class);
    private final Map<String, Long> bestScores = new HashMap<>();

    public SettingsStore() {
        this.storage = Window.current().getLocalStorage();
        loadAll();
    }

    private void loadAll() {
        for (Action action : Action.values()) {
            String raw = storage != null ? storage.getItem(KEY_PREFIX_BIND + action.name()) : null;
            if (raw == null || raw.isEmpty()) {
                bindings.put(action, new ArrayList<>(defaultBinding(action)));
            } else {
                bindings.put(action, parseCsv(raw));
            }
        }
        if (storage != null) {
            int length = storage.getLength();
            for (int i = 0; i < length; i++) {
                String key = storage.key(i);
                if (key != null && key.startsWith(KEY_PREFIX_BEST)) {
                    String mode = key.substring(KEY_PREFIX_BEST.length());
                    String value = storage.getItem(key);
                    if (value != null) {
                        try {
                            bestScores.put(mode, Long.parseLong(value));
                        } catch (NumberFormatException ignore) {
                        }
                    }
                }
            }
        }
    }

    public List<String> codesFor(Action action) {
        return bindings.get(action);
    }

    public Action actionForCode(String code) {
        if (code == null) {
            return null;
        }
        for (Map.Entry<Action, List<String>> e : bindings.entrySet()) {
            if (e.getValue().contains(code)) {
                return e.getKey();
            }
        }
        return null;
    }

    public void setBinding(Action action, List<String> codes) {
        bindings.put(action, new ArrayList<>(codes));
        if (storage != null) {
            storage.setItem(KEY_PREFIX_BIND + action.name(), String.join(",", codes));
        }
    }

    public void resetBindingsToDefault() {
        for (Action action : Action.values()) {
            setBinding(action, defaultBinding(action));
        }
    }

    public long bestScore(String mode) {
        return bestScores.getOrDefault(mode, 0L);
    }

    public void recordBestScore(String mode, long score) {
        if (score <= bestScore(mode)) {
            return;
        }
        bestScores.put(mode, score);
        if (storage != null) {
            storage.setItem(KEY_PREFIX_BEST + mode, Long.toString(score));
        }
    }

    public static List<String> defaultBinding(Action action) {
        return switch (action) {
            case MOVE_LEFT -> List.of("ArrowLeft");
            case MOVE_RIGHT -> List.of("ArrowRight");
            case SOFT_DROP -> List.of("ArrowDown");
            case HARD_DROP -> List.of("Space");
            case ROTATE_CW -> List.of("ArrowUp", "KeyX");
            case ROTATE_CCW -> List.of("KeyZ");
            case HOLD -> List.of("KeyC", "ShiftLeft", "ShiftRight");
            case SELECT_DOWN -> List.of("KeyW");
            case SELECT_UP -> List.of("KeyS");
            case PAUSE -> List.of("Escape", "KeyP");
            case RESET -> List.of("KeyR");
        };
    }

    private static List<String> parseCsv(String raw) {
        List<String> result = new ArrayList<>();
        for (String s : raw.split(",")) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    public static String displayLabel(String code) {
        if (code == null || code.isEmpty()) {
            return "(未割当)";
        }
        return switch (code) {
            case "ArrowLeft" -> "←";
            case "ArrowRight" -> "→";
            case "ArrowUp" -> "↑";
            case "ArrowDown" -> "↓";
            case "Space" -> "Space";
            case "Enter" -> "Enter";
            case "Escape" -> "Esc";
            case "Tab" -> "Tab";
            case "ShiftLeft" -> "Shift(左)";
            case "ShiftRight" -> "Shift(右)";
            case "ControlLeft" -> "Ctrl(左)";
            case "ControlRight" -> "Ctrl(右)";
            case "AltLeft" -> "Alt(左)";
            case "AltRight" -> "Alt(右)";
            case "Backquote" -> "`";
            case "Minus" -> "-";
            case "Equal" -> "=";
            case "BracketLeft" -> "[";
            case "BracketRight" -> "]";
            case "Backslash" -> "\\";
            case "Semicolon" -> ";";
            case "Quote" -> "'";
            case "Comma" -> ",";
            case "Period" -> ".";
            case "Slash" -> "/";
            default -> {
                if (code.startsWith("Key") && code.length() == 4) {
                    yield code.substring(3);
                }
                if (code.startsWith("Digit") && code.length() == 6) {
                    yield code.substring(5);
                }
                if (code.startsWith("Numpad")) {
                    yield "Num " + code.substring(6);
                }
                if (code.startsWith("F") && code.length() <= 3) {
                    yield code;
                }
                yield code;
            }
        };
    }

    public static String formatCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return "(未割当)";
        }
        List<String> labels = new ArrayList<>();
        for (String c : codes) {
            labels.add(displayLabel(c));
        }
        return String.join(" / ", labels);
    }
}
