package com.dm6801.framework.ui

import android.text.InputFilter
import android.widget.EditText

object EmoticonsFilter {

    operator fun invoke() = arrayOf(filter)

    val filter by lazy {
        InputFilter { source, start, end, _, _, _ ->
            for (index in start until end) {
                val type = Character.getType(source[index])

                if (type == Character.SURROGATE.toInt()
                    || type == Character.NON_SPACING_MARK.toInt()
                    || type == Character.OTHER_SYMBOL.toInt()
                )
                    return@InputFilter ""
            }
            null
        }
    }
}

fun EditText.filterEmoticons() {
    filters += EmoticonsFilter()
}