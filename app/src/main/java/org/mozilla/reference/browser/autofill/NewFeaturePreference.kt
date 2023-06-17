/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.autofill

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import org.mozilla.reference.browser.R

class NewFeaturePreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : Preference(context, attrs) {
    private var imageView: AppCompatImageView? = null

    init {
        widgetLayoutResource = R.layout.preference_new_label
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        imageView = holder.findViewById(R.id.image_widget) as AppCompatImageView
    }
}
