package com.personal.ioskeyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.graphics.Typeface
import android.content.res.Configuration

class IOSKeyboardService : InputMethodService() {

    private var isShifted = false
    private var isSymbols = false
    private lateinit var keyboardView: View

    private val letters = arrayOf(
        arrayOf("q","w","e","r","t","y","u","i","o","p"),
        arrayOf("a","s","d","f","g","h","j","k","l"),
        arrayOf("⇧","z","x","c","v","b","n","m","⌫"),
        arrayOf("123","space","return")
    )

    private val symbols = arrayOf(
        arrayOf("1","2","3","4","5","6","7","8","9","0"),
        arrayOf("-","/",":",";","(",")","\$","&","@","\""),
        arrayOf("#+=",".","_","!","?","\\","|","~","<",">","⌫"),
        arrayOf("ABC","space","return")
    )

    private val symbols2 = arrayOf(
        arrayOf("[","]","{","}","#","%","^","*","+","="),
        arrayOf("_","\\","|","~","<",">","€","£","¥","•"),
        arrayOf("123",".","_","!","?","\\","|","~","⌫"),
        arrayOf("ABC","space","return")
    )

    override fun onCreateInputView(): View {
        return buildKeyboard()
    }

    private fun isDarkMode(): Boolean {
        return (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    private fun buildKeyboard(): View {
        val dark = isDarkMode()
        val bgColor = if (dark) 0xFF1C1C1E.toInt() else 0xFFD1D5DB.toInt()
        val keyColor = if (dark) 0xFF3A3A3C.toInt() else 0xFFFFFFFF.toInt()
        val textColor = if (dark) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
        val specialKeyColor = if (dark) 0xFF2C2C2E.toInt() else 0xFFADB5BD.toInt()

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgColor)
            setPadding(8, 8, 8, 8)
        }

        val currentKeys = when {
            isSymbols -> symbols
            else -> letters
        }

        for (row in currentKeys) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 4, 0, 4) }
            }

            for (key in row) {
                val btn = Button(this).apply {
                    text = if (!isShifted) key else key.uppercase()
                    textSize = 16f
                    setTextColor(textColor)
                    setPadding(4, 12, 4, 12)

                    try {
                        val sf = Typeface.createFromAsset(assets, "fonts/sf_pro.ttf")
                        typeface = sf
                    } catch (e: Exception) {
                        try {
                            val mont = Typeface.createFromAsset(assets, "fonts/montserrat.ttf")
                            typeface = mont
                        } catch (e2: Exception) {
                            typeface = Typeface.DEFAULT
                        }
                    }

                    val isSpecial = key in listOf("⇧","⌫","123","ABC","#+=","return","space")
                    setBackgroundColor(if (isSpecial) specialKeyColor else keyColor)

                    val params = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        when (key) {
                            "space" -> 4f
                            "return" -> 2f
                            "⇧", "⌫" -> 1.5f
                            else -> 1f
                        }
                    ).apply { setMargins(4, 0, 4, 0) }
                    layoutParams = params

                    setOnClickListener { handleKey(key) }
                }
                rowLayout.addView(btn)
            }
            mainLayout.addView(rowLayout)
        }

        keyboardView = mainLayout
        return mainLayout
    }

    private fun handleKey(key: String) {
        val ic = currentInputConnection ?: return
        when (key) {
            "⌫" -> ic.deleteSurroundingText(1, 0)
            "return" -> ic.commitText("\n", 1)
            "space" -> ic.commitText(" ", 1)
            "⇧" -> { isShifted = !isShifted; setInputView(buildKeyboard()) }
            "123" -> { isSymbols = true; setInputView(buildKeyboard()) }
            "ABC" -> { isSymbols = false; setInputView(buildKeyboard()) }
            else -> {
                val char = if (isShifted) key.uppercase() else key.lowercase()
                ic.commitText(char, 1)
                if (isShifted) { isShifted = false; setInputView(buildKeyboard()) }
            }
        }
    }
}
