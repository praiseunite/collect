/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.odk.collect.android.external

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.analytics.AnalyticsUtils
import org.odk.collect.android.formlist.FormListItem
import org.odk.collect.android.formlist.FormListViewModel
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.settings.SettingsProvider
import javax.inject.Inject

/**
 * Allows the user to create desktop shortcuts to any form currently available to Collect
 */
class AndroidShortcutsActivity : AppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: FormListViewModel.Factory

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    private val viewModel: FormListViewModel by viewModels { viewModelFactory }

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        DaggerUtils.getComponent(this).inject(this)

        viewModel.forms.observe(this) { (value) -> showFormListDialog(value) }
    }

    private fun showFormListDialog(forms: List<FormListItem>) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.select_odk_shortcut)
            .setItems(
                forms
                    .map { it.formName }
                    .toTypedArray()
            ) { _: DialogInterface?, item: Int ->
                AnalyticsUtils.logServerEvent(
                    AnalyticsEvents.CREATE_SHORTCUT,
                    settingsProvider.getUnprotectedSettings()
                )
                val intent = getShortcutIntent(forms, item)
                setResult(RESULT_OK, intent)
                finish()
            }
            .setOnCancelListener {
                setResult(RESULT_CANCELED)
                finish()
            }
            .create()
            .show()
    }

    private fun getShortcutIntent(forms: List<FormListItem>, item: Int): Intent {
        val shortcutIntent = Intent(Intent.ACTION_EDIT).apply {
            data = FormsContract.getUri(
                currentProjectProvider.getCurrentProject().uuid,
                forms[item].databaseId
            )
        }

        return Intent().apply {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            putExtra(Intent.EXTRA_SHORTCUT_NAME, forms[item].formName)
            val iconResource: Parcelable =
                Intent.ShortcutIconResource.fromContext(this@AndroidShortcutsActivity, R.drawable.notes)
            putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource)
        }
    }
}
