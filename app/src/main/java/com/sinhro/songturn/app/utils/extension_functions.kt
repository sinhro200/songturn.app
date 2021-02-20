package com.sinhro.songturn.app.utils

import android.content.Context
import android.content.Intent
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sinhro.musicord.storage.Storage
import com.sinhro.songturn.app.R
import com.sinhro.songturn.app.storage.StorageService
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.validation.ValidationResult
import com.sinhro.songturn.rest.validation.ValidationResultType
import java.lang.StringBuilder

fun Fragment.showToast(message: String, isShort: Boolean = true) {
    Toast.makeText(this.context, message, if (isShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG)
        .show()
}

fun AppCompatActivity.showToast(message: String, isShort: Boolean = true) {
    if (Looper.myLooper() == null)
        Looper.prepare()
    Toast.makeText(this, message, if (isShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG)
        .show()
}

fun AppCompatActivity.replaceActivity(activity: AppCompatActivity) {
    val intent = Intent(this, activity::class.java)
    startActivity(intent)
    this.finish()
}

fun AppCompatActivity.replaceFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction()
        .addToBackStack(null)
        .replace(R.id.dataContainer, fragment)
        .commit()
    fragment.view?.let { hideKeyboard(it) }
}

fun AppCompatActivity.hideKeyboard(view: View) {
    hideKeyboardFrom(this, view)
}

fun Fragment.hideKeyboard() {
    if (context != null && view != null)
        hideKeyboardFrom(context!!, view!!)
}

fun Fragment.replaceFragment(fragment: Fragment) {
    parentFragmentManager
        .beginTransaction()
        .addToBackStack(null)
        .replace(R.id.dataContainer, fragment)
        .commit()
}

fun Fragment.showError(text: String) {
    SnackBarHelper.build(requireView(), text)
        .build()
        .show()
}

fun Fragment.showError(ce: CommonError, onClose: (() -> Unit)? = null) {
    SnackBarHelper.build(requireView(), getString(R.string.error_default_text))
        .extendedWith(requireActivity())
        .withMessageFromError(ce)
        .withOnCloseAction(onClose)
        .buildExtend()
        .build()

//    SnackBarHelper.extendedErrorSnackBar(requireView(), requireActivity(), "Ошибка", ce, onClose)
//        .show()
}

fun AppCompatActivity.showError(text: String) {
    SnackBarHelper.APPLICATION_ROOT_VIEW?.let {
        SnackBarHelper.build(it, text)
    }
}

fun AppCompatActivity.showError(ce: CommonError, onClose: (() -> Unit)? = null) {
    SnackBarHelper.APPLICATION_ROOT_VIEW?.let {
        SnackBarHelper.build(it, "Ошибка")
            .extendedWith(this)
            .withMessageFromError(ce)
            .withOnCloseAction(onClose)
            .buildExtend()
            .build()
            .show()

//            SnackBarHelper.extendedErrorSnackBar(it, this, "Ошибка", ce, onClose)
//            .show()
    }
}

fun AppCompatActivity.initAsRootForSnackbar(view: View) = SnackBarHelper.initRootView(view)

fun ValidationResult.toLanguagedString(context: Context): String {
    when (type) {
        ValidationResultType.MaxLengthError ->
            if (extra is Int)
                return context.getString(R.string.field_data_error_maximum_n_symbols, extra as Int)
        ValidationResultType.MinLengthError ->
            if (extra is Int)
                return context.getString(R.string.field_data_error_minimum_n_symbols, extra as Int)
        ValidationResultType.MinValueError ->
            if (extra is Int)
                return context.getString(R.string.field_data_error_minimum_value_n, extra as Int)
        ValidationResultType.MaxValueError ->
            if (extra is Int)
                return context.getString(R.string.field_data_error_maximum_value_n, extra as Int)
        ValidationResultType.MustContainsError ->
            if (extra is String)
                return context.getString(
                    R.string.field_data_error_must_contain_char,
                    extra as String
                )
        ValidationResultType.MustNotContainsError ->
            if (extra is String)
                return context.getString(
                    R.string.field_data_error_must_not_contain_char,
                    extra as String
                )
    }
    return context.getString(R.string.field_data_error_wrong_value)
}

fun CommonError.toPrettyString(): String {
    with(StringBuilder()) {
        if (!this@toPrettyString.message.isNullOrBlank())
            append(this@toPrettyString.message).append("\n")
        append(this@toPrettyString.errorCode)
        if (!this@toPrettyString.description.isNullOrBlank())
            append("\n").append(this@toPrettyString.description)
        if (this@toPrettyString.extra != null)
            append("\n").append(this@toPrettyString.extra)
        return this.toString()
    }
}
