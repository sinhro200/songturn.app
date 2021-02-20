package com.sinhro.songturn.app.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Looper
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.sinhro.songturn.app.R
import com.sinhro.songturn.rest.core.CommonError
import java.lang.StringBuilder

class SnackBarHelper {

    class SnackBarBuilder(
        private val view: View,
        private var text: String,
    ) {
        private var snackBarBuilderExtended: SnackBarBuilderExtended? = null
        private var length_type = Snackbar.LENGTH_LONG

        fun extendedWith(context: Context): SnackBarBuilderExtended = SnackBarBuilderExtended(context)

        fun withText(text: String): SnackBarBuilder {
            this.text = text
            return this
        }

        /**
         * length : Snackbar.LENGTH_LONG
         */
        fun withLength(
            length: Int
        ): SnackBarBuilder {
            this.length_type = length
            return this
        }

        fun withExtended(
            snackBarBuilderExtended: SnackBarBuilderExtended
        ): SnackBarBuilder {
            this.snackBarBuilderExtended = snackBarBuilderExtended
            return this
        }

        fun build(): Snackbar {
            val snackBar = Snackbar.make(view, text, length_type)
            snackBar.view.setBackgroundResource(R.drawable.snackbar_error_custom)

            this.snackBarBuilderExtended?.let { snackBarBuilderExtended ->
                snackBar.setAction(
                    snackBarBuilderExtended.context.getString(R.string.error_show_detail),
                    {
                        if (Looper.myLooper() == null)
                            Looper.prepare()
                        snackBarBuilderExtended.createDialog().show()
                    }
                )
            }
            return snackBar
        }

        inner class SnackBarBuilderExtended(
            internal val context: Context
        ) {
            private var onClose: (() -> Unit)? = null
            private var message: String? = null
            private var title: String? = null

            fun withMessageFromError(
                error: CommonError
            ): SnackBarBuilderExtended {
                message = buildMessageFromCommonError(error)
                return this
            }

            fun withOnCloseAction(onClose: (() -> Unit)?): SnackBarBuilderExtended {
                this.onClose = onClose
                return this
            }

            fun withMessage(message: String): SnackBarBuilderExtended {
                this.message = message
                return this
            }

            fun withTitle(title: String): SnackBarBuilderExtended {
                this.title = title
                return this
            }

            fun buildExtend(): SnackBarBuilder {
                return this@SnackBarBuilder.withExtended(this)
            }

            private fun buildMessageFromCommonError(ce: CommonError): String {
                with(StringBuilder()) {
                    append(ce.message).append("\n")
                    append(ce.errorCode).append("\n")
                    append(ce.description).append("\n")
                    append(ce.extra)
                    return toString()
                }
            }

            internal fun createDialog(): AlertDialog {

                return AlertDialog.Builder(context, R.style.AppExtendedErrorDialogTheme)
                    .setNegativeButton(
                        context.getString(R.string.error_close_alert_dialog_button_text)
                    ) { dialogInterface: DialogInterface?, i: Int ->
                        dialogInterface?.cancel()
                        onClose?.invoke()
                    }
                    .setMessage(message)
                    .setTitle(title ?: this@SnackBarBuilder.text)
                    .create()
            }
        }
    }

    companion object {
        var APPLICATION_ROOT_VIEW: View? = null
        fun initRootView(view: View) {
            APPLICATION_ROOT_VIEW = view;
        }

        fun build(
            view: View,
            defaultText: String
        ): SnackBarBuilder {
            return SnackBarBuilder(view, defaultText)
        }

        fun commonErrorSnackBar(
            view: View,
            text: String
        ): Snackbar {
            val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG)
            snackbar.view.setBackgroundResource(R.drawable.snackbar_error_custom)
            return snackbar
        }

        fun extendedErrorSnackBar(
            view: View,
            context: Context,
            commonErrorTitle: String,
            ce: CommonError,
            onClose: (() -> Unit)? = null
        ): Snackbar {
            with(StringBuilder()) {
                append(commonErrorTitle)
                append("\n")
                ce.message?.let {
                    append(" $it")
                    append(" .")
                }

                return commonErrorSnackBar(view, toString())
                    .setAction(
                        "_Подробно"
                    ) { alertDialogForCommonError(context, ce, onClose).show() }
            }
        }

        private fun alertDialogForCommonError(
            context: Context,
            ce: CommonError,
            onClose: (() -> Unit)? = null
        ): AlertDialog {
            val sb = StringBuilder().apply {
                append(ce.message).append("\n")
                append(ce.errorCode).append("\n")
                append(ce.description).append("\n")
                append(ce.extra)
            }

            val alertDialog = AlertDialog.Builder(context, R.style.AppExtendedErrorDialogTheme)
                .setNegativeButton(
                    context.getString(R.string.error_close_alert_dialog_button_text)
                ) { dialogInterface: DialogInterface?, i: Int ->
                    dialogInterface?.cancel()
                    onClose?.invoke()
                }
                .setMessage(sb.toString())
                .setTitle(context.getString(R.string.ShowError))
                .create()

            return alertDialog
        }
    }
}