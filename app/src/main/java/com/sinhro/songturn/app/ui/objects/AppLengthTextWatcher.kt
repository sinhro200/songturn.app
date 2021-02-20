package com.sinhro.songturn.app.ui.objects

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import com.sinhro.songturn.app.R
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes

class AppLengthTextWatcher(
    val context: Context,
    val textFieldBoxes: TextFieldBoxes,
    val min: Int,
    val max: Int
) : TextWatcher {
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        p0?.length
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        p0?.length
    }

    override fun afterTextChanged(p0: Editable?) {
        p0?.let {
            if (it.length > max)
                textFieldBoxes.helperText = context.getString(R.string.field_data_error_maximum_n_symbols, max)
            else if (it.length < min)
                textFieldBoxes.helperText = context.getString(R.string.field_data_error_minimum_n_symbols, min)
            else
                textFieldBoxes.helperText = ""
        }
    }

}