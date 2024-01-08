package com.dicoding.picodiploma.loginwithanimation.view.myeditbutton

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.dicoding.picodiploma.loginwithanimation.R

class MyButton : AppCompatButton {

    private lateinit var enabledBackground: Drawable
    private lateinit var disabledBackground: Drawable
    private var txtColor: Int = 0

    private var isEnabledText: CharSequence = ""
    private var isDisabledText: CharSequence = ""

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        background = if(isEnabled) enabledBackground else disabledBackground
        setTextColor(txtColor)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        gravity = Gravity.CENTER
        text = if(isEnabled) isEnabledText else isDisabledText
    }

    private fun init() {
        txtColor = ContextCompat.getColor(context, android.R.color.background_light)
        enabledBackground = ContextCompat.getDrawable(context, R.drawable.bg_button) as Drawable
        disabledBackground = ContextCompat.getDrawable(context, R.drawable.bg_button_disable) as Drawable
        isEnabledText = context.getString(R.string.enabled_text)
        isDisabledText = context.getString(R.string.button_text_disabled)
    }

    fun setEnabledText(text: String) {
        isEnabledText = text
    }

    fun setDisabledText(text: String) {
        isDisabledText = text
    }
}
