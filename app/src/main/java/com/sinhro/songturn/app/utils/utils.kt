package com.sinhro.songturn.app.utils

import android.app.Activity
import android.content.Context
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.sinhro.songturn.app.R
import com.sinhro.songturn.rest.validation.ValidationResult
import studio.carbonylgroup.textfieldboxes.ExtendedEditText
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes

fun SetErrorHelpers(
    fieldNameToValidationResultsMap: Map<String, List<ValidationResult>>,
    fieldNameToExtendedEditTextMap: Map<String, TextFieldBoxes>,
    context: Context
) {
    fieldNameToValidationResultsMap.keys.forEach { fieldName ->
        val validationResult = fieldNameToValidationResultsMap[fieldName]
        validationResult?.let { validResults ->
            if (validResults.isNotEmpty())
                fieldNameToExtendedEditTextMap[fieldName]?.setError(
                    validResults[0].toLanguagedString(
                        context
                    ), false
                )
        }
    }
}

fun OnEyeClickListener(
    extendedEditText: ExtendedEditText,
    textFieldBoxes: TextFieldBoxes,
) = object : View.OnClickListener {
    private var canShowPass: Boolean =
        (extendedEditText.inputType and InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) == 0

    override fun onClick(v: View?) {
        canShowPass = !canShowPass
        val selectionPosition = extendedEditText.selectionEnd
        if (canShowPass) {
            setInputTypeVisible(true)
            textFieldBoxes.setEndIcon(R.drawable.ic_eye_open)
        } else {
            setInputTypeVisible(false)
            textFieldBoxes.setEndIcon(R.drawable.ic_eye_closed)
        }
        extendedEditText.setSelection(selectionPosition)
    }

    private fun setInputTypeVisible(isVisible: Boolean) {
        extendedEditText.inputType =
            InputType.TYPE_CLASS_TEXT or (
                    if (isVisible)
                        InputType.TYPE_TEXT_VARIATION_NORMAL
                    else
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
                    )
    }
}

fun hideKeyboardFrom(context: Context, view: View) {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}