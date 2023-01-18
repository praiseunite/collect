package org.odk.collect.android.regression

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.BuildConfig
import org.odk.collect.android.R
import org.odk.collect.android.application.Collect
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.FileUtils
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import org.odk.collect.androidtest.RecordedIntentsRule
import java.io.File

class ImageWidgetTest {
    var rule = CollectTestRule()

    @get:Rule
    var copyFormChain: RuleChain = TestRuleChain.chain()
        .around(RecordedIntentsRule())
        .around(rule)

    @Test // https://github.com/getodk/collect/issues/4819
    @Ignore("Fails on ARM virtual devices")
    fun attachingGifsShouldBePossible() {
        intending(not(IntentMatchers.isInternal())).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK,
                gifFileResultIntent()
            )
        )

        rule.startAtMainMenu()
            .copyForm("image_widget.xml")
            .startBlankForm("image_widget")
            .clickOnString(R.string.choose_image)

        onView(withTagValue(`is`("ImageView")))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    private fun gifFileResultIntent(): Intent {
        val resultIntent = Intent()
        val file = File.createTempFile("file", "gif", File(StoragePathProvider().odkRootDirPath))
        FileUtils.copyFileFromAssets("media" + File.separator + "file.gif", file.path)
        resultIntent.data = FileProvider.getUriForFile(
            Collect.getInstance(),
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )
        return resultIntent
    }
}
