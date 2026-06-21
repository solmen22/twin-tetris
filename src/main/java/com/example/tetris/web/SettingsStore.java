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
    private static final String KEY_PREFIX_SCORES = "tt.scores.";
    private static final String KEY_USERNAME = "tt.username";
    private static final String KEY_SOUND_ENABLED = "tt.sound.enabled";
    private static final String KEY_SOUND_VOLUME = "tt.sound.volume";
    private static final String KEY_DAS = "tt.das";
    private static final String KEY_ARR = "tt.arr";
    private static final String KEY_SDF = "tt.sdf";
    private static final String KEY_GHOST = "tt.ghost";
    private static final String KEY_REDUCE_MOTION = "tt.reduceMotion";
    private static final String KEY_VISITED = "tt.visited";
    public static final int USERNAME_MAX_LENGTH = 16;

    public static final int DAS_MIN = 0;
    public static final int DAS_MAX = 500;
    public static final int ARR_MIN = 0;
    public static final int ARR_MAX = 200;
    public static final int SDF_MIN = 0;
    public static final int SDF_MAX = 200;
    public static final int VOLUME_MIN = 0;
    public static final int VOLUME_MAX = 100;
    public static final int DEFAULT_VOLUME = 45;
    public static final int MAX_HIGH_SCORES = 10;

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

    public String username() {
        if (storage == null) {
            return "";
        }
        String raw = storage.getItem(KEY_USERNAME);
        return raw == null ? "" : raw;
    }

    public void setUsername(String username) {
        String sanitized = sanitizeUsername(username);
        if (storage != null) {
            if (sanitized.isEmpty()) {
                storage.removeItem(KEY_USERNAME);
            } else {
                storage.setItem(KEY_USERNAME, sanitized);
            }
        }
    }

    public static String sanitizeUsername(String raw) {
        if (raw == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length() && sb.length() < USERNAME_MAX_LENGTH; i++) {
            char c = raw.charAt(i);
            if (c == '\n' || c == '\r' || c == '\t' || c == '\0') {
                continue;
            }
            if (c < 0x20) {
                continue;
            }
            sb.append(c);
        }
        return sb.toString().trim();
    }

    // ---------- 音・操作・表示の設定 ----------

    public boolean soundEnabled() {
        return getBool(KEY_SOUND_ENABLED, true);
    }

    public void setSoundEnabled(boolean enabled) {
        setBool(KEY_SOUND_ENABLED, enabled);
    }

    public int soundVolume() {
        return getInt(KEY_SOUND_VOLUME, DEFAULT_VOLUME, VOLUME_MIN, VOLUME_MAX);
    }

    public void setSoundVolume(int volume) {
        setInt(KEY_SOUND_VOLUME, clamp(volume, VOLUME_MIN, VOLUME_MAX));
    }

    public int das() {
        return getInt(KEY_DAS, WebConstants.DEFAULT_DAS_MS, DAS_MIN, DAS_MAX);
    }

    public void setDas(int ms) {
        setInt(KEY_DAS, clamp(ms, DAS_MIN, DAS_MAX));
    }

    public int arr() {
        return getInt(KEY_ARR, WebConstants.DEFAULT_ARR_MS, ARR_MIN, ARR_MAX);
    }

    public void setArr(int ms) {
        setInt(KEY_ARR, clamp(ms, ARR_MIN, ARR_MAX));
    }

    public int softDropInterval() {
        return getInt(KEY_SDF, WebConstants.DEFAULT_SOFT_DROP_MS, SDF_MIN, SDF_MAX);
    }

    public void setSoftDropInterval(int ms) {
        setInt(KEY_SDF, clamp(ms, SDF_MIN, SDF_MAX));
    }

    public boolean ghostEnabled() {
        return getBool(KEY_GHOST, true);
    }

    public void setGhostEnabled(boolean enabled) {
        setBool(KEY_GHOST, enabled);
    }

    public boolean reduceMotion() {
        return getBool(KEY_REDUCE_MOTION, false);
    }

    public void setReduceMotion(boolean reduce) {
        setBool(KEY_REDUCE_MOTION, reduce);
    }

    public boolean isFirstVisit() {
        return !getBool(KEY_VISITED, false);
    }

    public void markVisited() {
        setBool(KEY_VISITED, true);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private boolean getBool(String key, boolean def) {
        if (storage == null) {
            return def;
        }
        String v = storage.getItem(key);
        if (v == null) {
            return def;
        }
        return "1".equals(v) || "true".equalsIgnoreCase(v);
    }

    private void setBool(String key, boolean v) {
        if (storage != null) {
            storage.setItem(key, v ? "1" : "0");
        }
    }

    private int getInt(String key, int def, int min, int max) {
        if (storage == null) {
            return def;
        }
        String v = storage.getItem(key);
        if (v == null) {
            return def;
        }
        try {
            return clamp(Integer.parseInt(v.trim()), min, max);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private void setInt(String key, int v) {
        if (storage != null) {
            storage.setItem(key, Integer.toString(v));
        }
    }

    // ---------- ハイスコア表(モード別 Top 10, SPEC 11.1) ----------

    public record ScoreEntry(long score, int level, int lines, long dateMs, String name) {
    }

    public List<ScoreEntry> highScores(String mode) {
        List<ScoreEntry> list = new ArrayList<>();
        if (storage == null) {
            return list;
        }
        String raw = storage.getItem(KEY_PREFIX_SCORES + mode);
        if (raw == null || raw.isEmpty()) {
            return list;
        }
        for (String token : raw.split(";")) {
            ScoreEntry entry = parseEntry(token);
            if (entry != null) {
                list.add(entry);
            }
        }
        list.sort((a, b) -> Long.compare(b.score(), a.score()));
        return list;
    }

    /**
     * スコアをモード別の Top 10 に挿入する。
     * @return 1 始まりの順位(Top 10 入りした場合)、入らなかった場合は 0。
     */
    public int recordHighScore(String mode, long score, int level, int lines, String name) {
        if (score <= 0L || storage == null) {
            return 0;
        }
        List<ScoreEntry> list = highScores(mode);
        ScoreEntry entry = new ScoreEntry(score, level, lines, System.currentTimeMillis(), sanitizeName(name));
        list.add(entry);
        list.sort((a, b) -> Long.compare(b.score(), a.score()));
        while (list.size() > MAX_HIGH_SCORES) {
            list.remove(list.size() - 1);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(';');
            }
            sb.append(serializeEntry(list.get(i)));
        }
        storage.setItem(KEY_PREFIX_SCORES + mode, sb.toString());
        if (!list.isEmpty()) {
            recordBestScore(mode, list.get(0).score());
        }
        int idx = list.indexOf(entry);
        return idx >= 0 ? idx + 1 : 0;
    }

    public void clearHighScores() {
        if (storage == null) {
            return;
        }
        for (java.util.Map.Entry<String, Long> e : bestScores.entrySet()) {
            storage.removeItem(KEY_PREFIX_SCORES + e.getKey());
            storage.removeItem(KEY_PREFIX_BEST + e.getKey());
        }
        bestScores.clear();
    }

    private static String sanitizeName(String name) {
        String n = sanitizeUsername(name);
        return n.replace(';', ' ').replace('|', ' ').trim();
    }

    private static String serializeEntry(ScoreEntry e) {
        return e.score() + "|" + e.level() + "|" + e.lines() + "|" + e.dateMs() + "|" + e.name();
    }

    private static ScoreEntry parseEntry(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        String[] parts = token.split("\\|", 5);
        if (parts.length < 4) {
            return null;
        }
        try {
            long score = Long.parseLong(parts[0].trim());
            int level = Integer.parseInt(parts[1].trim());
            int lines = Integer.parseInt(parts[2].trim());
            long dateMs = Long.parseLong(parts[3].trim());
            String name = parts.length >= 5 ? parts[4] : "";
            return new ScoreEntry(score, level, lines, dateMs, name);
        } catch (NumberFormatException e) {
            return null;
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
