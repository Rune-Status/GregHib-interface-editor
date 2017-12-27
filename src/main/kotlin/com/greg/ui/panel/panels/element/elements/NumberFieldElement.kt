package com.greg.ui.panel.panels.element.elements

import com.greg.settings.Settings
import com.greg.settings.SettingsKey
import com.greg.ui.panel.panels.element.Element
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.util.converter.IntegerStringConverter


class NumberFieldElement(private var default: Int?) : TextField(), Element {

    override var links: MutableList<(value: Any?) -> Unit> = mutableListOf()

    init {
        textFormatter = TextFormatter(IntegerStringConverter())
        this.text = default.toString()

        // Key press
        // ---------------------------------------------------------------
        addEventFilter<KeyEvent>(KeyEvent.ANY, { e -> handleKeyEvent(e)})

        // Focus listener
        // ---------------------------------------------------------------
        focusedProperty().addListener({ _, _, focus -> handleFocusListener(focus) })
    }

    private fun handleKeyEvent(e: KeyEvent) {
        when(e.eventType) {
            KeyEvent.KEY_PRESSED -> { handleKeyPress(e.code) }
        }
    }

    private fun handleKeyPress(code: KeyCode) {
        when(code.ordinal) {
            Settings.getInt(SettingsKey.ACCEPT_KEY_CODE) -> { accept(text) }
            Settings.getInt(SettingsKey.CANCEL_KEY_CODE) -> { cancel() }
        }
    }

    private fun handleFocusListener(focused: Boolean) {
        if(!focused) {
            if(Settings.getBoolean(SettingsKey.CANCEL_ON_UNFOCUSED))
                cancel()
            else
                accept(text)
        }
    }

    override fun refresh(value: Any?) {
        this.text = (value as Double).toInt().toString()
    }

    private fun accept(text: String?) {
        if(text != null) {
            val value = text.toIntOrNull()
            if(value != null)
                for(action in links)
                    action(value)
        }
    }

    private fun cancel() {
        text = default.toString()
    }
}