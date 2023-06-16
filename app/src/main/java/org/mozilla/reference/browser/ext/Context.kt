/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ext

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_SUBJECT
import android.content.Intent.EXTRA_TEXT
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Dimension
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import mozilla.components.support.base.log.Log
import mozilla.components.support.base.log.Log.Priority.WARN
import org.mozilla.reference.browser.BrowserApplication
import org.mozilla.reference.browser.Components
import org.mozilla.reference.browser.R

/**
 * Get the BrowserApplication object from a context.
 */
val Context.application: BrowserApplication
    get() = applicationContext as BrowserApplication

/**
 * Get the requireComponents of this application.
 */
val Context.components: Components
    get() = application.components

fun Context.getPreferenceKey(@StringRes resourceId: Int): String =
    resources.getString(resourceId)

/**
 *  Shares content via [ACTION_SEND] intent.
 *
 * @param text the data to be shared  [EXTRA_TEXT]
 * @param subject of the intent [EXTRA_TEXT]
 * @return true it is able to share false otherwise.
 */
fun Context.share(text: String, subject: String = ""): Boolean {
    return try {
        val intent = Intent(ACTION_SEND).apply {
            type = "text/plain"
            putExtra(EXTRA_SUBJECT, subject)
            putExtra(EXTRA_TEXT, text)
            flags = FLAG_ACTIVITY_NEW_TASK
        }

        val shareIntent = Intent.createChooser(intent, getString(R.string.menu_share_with)).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
        }

        startActivity(shareIntent)
        true
    } catch (e: ActivityNotFoundException) {
        Log.log(WARN, message = "No activity to share to found", throwable = e, tag = "Reference-Browser")
        false
    }
}

/**
 * Hide the soft keyboard.
 */
fun Context.hideSoftKeyboard(view: View) {
    val inputMethodManager = this.getSystemService(
        Activity.INPUT_METHOD_SERVICE
    ) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(
        view.windowToken, 0
    )
}

/**
 *  Displays an [AlertDialog].
 *
 * @param [title] to be displayed on the [AlertDialog]
 * @param [message] to be displayed on the [AlertDialog]
 * @param [callback] determines the action of the [AlertDialog] buttons
 */
fun Context.showAlertDialog(
    title: String,
    message: String,
    callback: () -> Unit
) {
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(getString(R.string.proceed)) { _, _ -> callback() }
        .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
        .show()
}


/**
 *  Displays an [AlertDialog] that contains an [EditText] view.
 *
 * @param [title] to be displayed on the [AlertDialog]
 * @param [defaultText] to be displayed on the [EditText]
 * @param [callback] determines of the positive action button of the [AlertDialog]
 */
fun Context.showEditTextDialogForFileName(
    title: String,
    defaultText: String,
    callback: (String) -> Unit
) {

    // Create edittext to set as view
    val editText = EditText(this).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        setPadding(40, 60, 40, 60)
        setText(defaultText)
    }

    // Create and show AlertDialog
    AlertDialog.Builder(this)
        .setTitle(title)
        .setView(editText)
        .setPositiveButton(getString(R.string.proceed)) { _, _ ->
            val inputText = editText.text.toString().trim()
            hideSoftKeyboard(editText)
            callback(inputText)
        }
        .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
        .show()
}

/**
 *  Displays an [AlertDialog] that contains an progress layout.
 *
 * @param [message] to be displayed on the [AlertDialog]
 * @return [AlertDialog]
 */
fun Context.showProgressDialog(message: String): AlertDialog {
    return AlertDialog.Builder(this)
        .setView(LinearLayout(this).apply {
            setPadding(40, 60, 40, 60)
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER or Gravity.START
            addView(ProgressBar(this@showProgressDialog).apply {
                indeterminateDrawable.setTint(
                    ContextCompat.getColor(
                        this@showProgressDialog,
                        R.color.photonWhite
                    )
                )
            })
            addView(TextView(this@showProgressDialog).apply {
                text = message
                gravity = Gravity.CENTER_VERTICAL
                setTextColor(ContextCompat.getColor(this@showProgressDialog, R.color.photonBlack))
                setTextSize(Dimension.SP, 14f)
                setPadding(30, 0, 0, 0)
            })
        })
        .create()
}


/**
 *  Displays an [Toast] for a short time.
 *
 * @param [message] to be displayed
 */
fun Context.showToast(
    message: String
) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
