package com.example.tetris.web;

import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

import java.util.ArrayList;
import java.util.List;

public final class SettingsView {

    private final HTMLDocument document;
    private final HTMLElement screen;
    private final HTMLElement listContainer;
    private final HTMLElement captureBanner;
    private final HTMLElement captureLabel;
    private final SettingsStore settings;
    private final Runnable onBack;

    private Action awaitingAction;
    private final EventListener<KeyboardEvent> captureListener;

    public SettingsView(HTMLDocument document, SettingsStore settings, Runnable onBack) {
        this.document = document;
        this.settings = settings;
        this.onBack = onBack;
        this.screen = document.getElementById("settings");
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
