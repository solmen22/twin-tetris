package com.example.tetris.web;

import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLInputElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class SettingsView {

    private final HTMLDocument document;
    private final HTMLElement screen;
    private final HTMLElement prefsContainer;
    private final HTMLElement listContainer;
    private final HTMLElement captureBanner;
    private final HTMLElement captureLabel;
    private final SettingsStore settings;
    private final Runnable onBack;
    private final SoundManager sound;
    private final CanvasRenderer renderer;

    private Action awaitingAction;
    private final EventListener<KeyboardEvent> captureListener;

    public SettingsView(
        HTMLDocument document,
        SettingsStore settings,
        Runnable onBack,
        SoundManager sound,
        CanvasRenderer renderer
    ) {
        this.document = document;
        this.settings = settings;
        this.onBack = onBack;
        this.sound = sound;
        this.renderer = renderer;
        this.screen = document.getElementById("settings");
        this.prefsContainer = document.getElementById("settings-prefs");
        this.listContainer = document.getElementById("settings-list");
        this.captureBanner = document.getElementById("capture-banner");
        this.captureLabel = document.getElementById("capture-action-label");

        HTMLElement backBtn = document.getElementById("settings-back");
        HTMLElement resetBtn = document.getElementById("settings-reset");
        HTMLElement captureCancel = document.getElementById("capture-cancel");

        if (backBtn != null) {
            backBtn.addEventListener("click", (EventListener<Event>) e -> onBack.run());
        }
        if (resetBtn != null) {
            resetBtn.addEventListener("click", (EventListener<Event>) e -> {
                settings.resetBindingsToDefault();
                renderList();
            });
        }
        if (captureCancel != null) {
            captureCancel.addEventListener("click", (EventListener<Event>) e -> exitCaptureMode());
        }
        this.captureListener = this::handleCapture;
    }

    public void show() {
        screen.setClassName(stripClass(screen.getClassName(), "hidden"));
        renderPreferences();
        renderList();
    }

    public void hide() {
        exitCaptureMode();
        addClass(screen, "hidden");
    }

    public boolean isCapturing() {
        return awaitingAction != null;
    }

    public boolean isVisible() {
        String cls = screen.getClassName();
        return cls == null || !(" " + cls + " ").contains(" hidden ");
    }

    // ---------- 環境設定(音・操作・表示) ----------

    private void renderPreferences() {
        if (prefsContainer == null) {
            return;
        }
        prefsContainer.setInnerHTML("");

        prefsContainer.appendChild(sectionTitle("サウンド"));
        prefsContainer.appendChild(toggleRow("効果音", settings.soundEnabled(), on -> {
            settings.setSoundEnabled(on);
            sound.refresh();
            if (on) {
                sound.unlock();
                sound.play("rotate");
            }
        }));
        prefsContainer.appendChild(sliderRow("効果音の音量", SettingsStore.VOLUME_MIN, SettingsStore.VOLUME_MAX, 5, "%",
            settings::soundVolume, v -> {
                settings.setSoundVolume(v);
                sound.refresh();
            }, true));
        prefsContainer.appendChild(toggleRow("BGM", settings.bgmEnabled(), on -> {
            settings.setBgmEnabled(on);
            sound.refresh();
            if (on) {
                sound.unlock();
                sound.startBgm();
            } else {
                sound.stopBgm();
            }
        }));
        prefsContainer.appendChild(sliderRow("BGM の音量", SettingsStore.VOLUME_MIN, SettingsStore.VOLUME_MAX, 5, "%",
            settings::bgmVolume, v -> {
                settings.setBgmVolume(v);
                sound.refresh();
            }, false));

        prefsContainer.appendChild(sectionTitle("操作(連射)"));
        prefsContainer.appendChild(sliderRow("DAS(初動の遅延)", SettingsStore.DAS_MIN, SettingsStore.DAS_MAX, 1, "ms",
            settings::das, settings::setDas, false));
        prefsContainer.appendChild(sliderRow("ARR(連射間隔)", SettingsStore.ARR_MIN, SettingsStore.ARR_MAX, 1, "ms",
            settings::arr, settings::setArr, false));
        prefsContainer.appendChild(sliderRow("ソフトドロップ間隔", SettingsStore.SDF_MIN, SettingsStore.SDF_MAX, 1, "ms",
            settings::softDropInterval, settings::setSoftDropInterval, false));

        prefsContainer.appendChild(sectionTitle("表示"));
        prefsContainer.appendChild(toggleRow("ゴースト(着地予測)", settings.ghostEnabled(), on -> {
            settings.setGhostEnabled(on);
            renderer.setGhostEnabled(on);
        }));
        prefsContainer.appendChild(toggleRow("演出を控えめにする", settings.reduceMotion(), on -> {
            settings.setReduceMotion(on);
            renderer.setReducedMotion(on);
        }));
    }

    private HTMLElement sectionTitle(String text) {
        HTMLElement h = document.createElement("h3");
        h.setClassName("settings-section-title");
        h.setInnerText(text);
        return h;
    }

    private HTMLElement toggleRow(String label, boolean initial, java.util.function.Consumer<Boolean> onChange) {
        HTMLElement row = document.createElement("div");
        row.setClassName("settings-row pref-row");

        HTMLElement labelCol = document.createElement("div");
        labelCol.setClassName("settings-action");
        labelCol.setInnerText(label);
        row.appendChild(labelCol);

        HTMLElement controlCol = document.createElement("div");
        controlCol.setClassName("pref-control");
        HTMLElement btn = document.createElement("button");
        applyToggleButton(btn, initial);
        btn.addEventListener("click", (EventListener<Event>) e -> {
            boolean next = !"on".equals(btn.getAttribute("data-state"));
            applyToggleButton(btn, next);
            onChange.accept(next);
        });
        controlCol.appendChild(btn);
        row.appendChild(controlCol);
        return row;
    }

    private static void applyToggleButton(HTMLElement btn, boolean on) {
        btn.setClassName(on ? "pref-toggle on" : "pref-toggle");
        btn.setAttribute("data-state", on ? "on" : "off");
        btn.setAttribute("role", "switch");
        btn.setAttribute("aria-checked", on ? "true" : "false");
        btn.setInnerText(on ? "ON" : "OFF");
    }

    private HTMLElement sliderRow(String label, int min, int max, int step, String unit,
                                  IntSupplier getter, IntConsumer setter, boolean liveSound) {
        HTMLElement row = document.createElement("div");
        row.setClassName("settings-row pref-row");

        HTMLElement labelCol = document.createElement("div");
        labelCol.setClassName("settings-action");
        labelCol.setInnerText(label);
        row.appendChild(labelCol);

        HTMLElement controlCol = document.createElement("div");
        controlCol.setClassName("pref-control");

        HTMLInputElement slider = (HTMLInputElement) document.createElement("input");
        slider.setAttribute("type", "range");
        slider.setAttribute("min", Integer.toString(min));
        slider.setAttribute("max", Integer.toString(max));
        slider.setAttribute("step", Integer.toString(step));
        int current = getter.getAsInt();
        slider.setValue(Integer.toString(current));

        HTMLElement valueLabel = document.createElement("span");
        valueLabel.setClassName("pref-value");
        valueLabel.setInnerText(current + unit);

        slider.addEventListener("input", (EventListener<Event>) e -> {
            int v = parseIntSafe(slider.getValue(), current);
            setter.accept(v);
            valueLabel.setInnerText(v + unit);
            if (liveSound) {
                sound.refresh();
            }
        });

        controlCol.appendChild(slider);
        controlCol.appendChild(valueLabel);
        row.appendChild(controlCol);
        return row;
    }

    private static int parseIntSafe(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    // ---------- キーバインド ----------

    private void renderList() {
        if (listContainer == null) {
            return;
        }
        listContainer.setInnerHTML("");
        for (Action action : Action.values()) {
            listContainer.appendChild(buildRow(action));
        }
    }

    private HTMLElement buildRow(Action action) {
        HTMLElement row = document.createElement("div");
        row.setClassName("settings-row");

        HTMLElement labelCol = document.createElement("div");
        labelCol.setClassName("settings-action");
        labelCol.setInnerText(action.label());
        row.appendChild(labelCol);

        HTMLElement chipsCol = document.createElement("div");
        chipsCol.setClassName("settings-chips");
        List<String> codes = settings.codesFor(action);
        if (codes != null) {
            for (String code : codes) {
                chipsCol.appendChild(buildChip(action, code));
            }
        }
        HTMLElement addBtn = document.createElement("button");
        addBtn.setClassName("settings-add");
        addBtn.setInnerText("+ 追加");
        addBtn.addEventListener("click", (EventListener<Event>) e -> enterCaptureMode(action));
        chipsCol.appendChild(addBtn);
        row.appendChild(chipsCol);

        return row;
    }

    private HTMLElement buildChip(Action action, String code) {
        HTMLElement chip = document.createElement("span");
        chip.setClassName("settings-chip");

        HTMLElement label = document.createElement("span");
        label.setClassName("chip-label");
        label.setInnerText(SettingsStore.displayLabel(code));
        chip.appendChild(label);

        HTMLElement remove = document.createElement("button");
        remove.setClassName("chip-remove");
        remove.setInnerText("×");
        remove.addEventListener("click", (EventListener<Event>) e -> {
            List<String> current = new ArrayList<>(settings.codesFor(action));
            current.remove(code);
            settings.setBinding(action, current);
            renderList();
        });
        chip.appendChild(remove);

        return chip;
    }

    private void enterCaptureMode(Action action) {
        awaitingAction = action;
        if (captureLabel != null) {
            captureLabel.setInnerText(action.label());
        }
        if (captureBanner != null) {
            captureBanner.setClassName(stripClass(captureBanner.getClassName(), "hidden"));
        }
        document.addEventListener("keydown", castKeyListener(captureListener), true);
    }

    private void exitCaptureMode() {
        if (awaitingAction == null) {
            return;
        }
        awaitingAction = null;
        if (captureBanner != null) {
            addClass(captureBanner, "hidden");
        }
        document.removeEventListener("keydown", castKeyListener(captureListener), true);
    }

    private void handleCapture(KeyboardEvent event) {
        if (awaitingAction == null) {
            return;
        }
        event.preventDefault();
        event.stopPropagation();
        String code = event.getCode();
        if (code == null || code.isEmpty()) {
            return;
        }
        if ("Escape".equals(code)) {
            exitCaptureMode();
            return;
        }
        Action target = awaitingAction;
        List<String> current = new ArrayList<>(settings.codesFor(target));
        for (Action other : Action.values()) {
            if (other == target) continue;
            List<String> otherCodes = new ArrayList<>(settings.codesFor(other));
            if (otherCodes.remove(code)) {
                settings.setBinding(other, otherCodes);
            }
        }
        if (!current.contains(code)) {
            current.add(code);
            settings.setBinding(target, current);
        }
        exitCaptureMode();
        renderList();
    }

    @SuppressWarnings("unchecked")
    private static EventListener<Event> castKeyListener(EventListener<KeyboardEvent> l) {
        return (EventListener<Event>) (EventListener<?>) l;
    }

    private static String stripClass(String current, String target) {
        if (current == null || current.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String part : current.split(" ")) {
            if (!part.equals(target) && !part.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(part);
            }
        }
        return sb.toString();
    }

    private static void addClass(HTMLElement el, String cls) {
        if (el == null) {
            return;
        }
        String current = el.getClassName();
        if (current == null || current.isEmpty()) {
            el.setClassName(cls);
        } else if (!(" " + current + " ").contains(" " + cls + " ")) {
            el.setClassName(current + " " + cls);
        }
    }
}
