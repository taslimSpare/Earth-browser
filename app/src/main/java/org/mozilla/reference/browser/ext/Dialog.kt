package org.mozilla.reference.browser.ext

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.ViewGroup.LayoutParams
import android.widget.EditText
import org.mozilla.reference.browser.R


fun Dialog.showEditTextDialog(
    context: Context,
    defaultText: String,
    callback: (String) -> Unit
) {
    val editText = EditText(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        setText(defaultText)
    }

    val dialog = AlertDialog.Builder(context)
        .setTitle("Create a name for this file")
        .setView(editText)
        .setPositiveButton(context.getString(R.string.download)) { _, _ ->
            val inputText = editText.text.toString().trim()
            callback(inputText)
        }
        .setNegativeButton("Cancel") { _, _ ->
            dismiss()
        }
        .create()

    dialog.show()
}
