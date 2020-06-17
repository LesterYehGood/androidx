/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.ui.material.textfield

import android.os.Build
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import androidx.test.screenshot.AndroidXScreenshotTestRule
import androidx.test.screenshot.assertAgainstGolden
import androidx.ui.core.Modifier
import androidx.ui.core.semantics.semantics
import androidx.ui.core.testTag
import androidx.ui.foundation.Box
import androidx.ui.foundation.Text
import androidx.ui.material.FilledTextField
import androidx.ui.material.GOLDEN_MATERIAL
import androidx.ui.material.OutlinedTextField
import androidx.ui.material.setMaterialContent
import androidx.ui.test.captureToBitmap
import androidx.ui.test.createComposeRule
import androidx.ui.test.doClick
import androidx.ui.test.findByTag
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@LargeTest
@RunWith(JUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
class TextFieldScreenshotTest {

    private val TextFieldTag = "textField"

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val screenshotRule = AndroidXScreenshotTestRule(GOLDEN_MATERIAL)

    @Test
    fun outlinedTextField_withInput() {
        composeTestRule.setMaterialContent {
            Box(Modifier.semantics(mergeAllDescendants = true).testTag(TextFieldTag)) {
                OutlinedTextField(
                    value = "Text",
                    onValueChange = {},
                    label = { Text("Label") }
                )
            }
        }

        assertAgainstGolden("outlined_textField_withInput")
    }

    @Test
    fun outlinedTextField_notFocused() {
        composeTestRule.setMaterialContent {
            Box(Modifier.semantics(mergeAllDescendants = true).testTag(TextFieldTag)) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Label") }
                )
            }
        }

        assertAgainstGolden("outlined_textField_not_focused")
    }

    @Test
    fun outlinedTextField_focused() {
        composeTestRule.setMaterialContent {
            Box(Modifier.semantics(mergeAllDescendants = true).testTag(TextFieldTag)) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Label") }
                )
            }
        }

        findByTag(TextFieldTag)
            .doClick()

        assertAgainstGolden("outlined_textField_focused")
    }

    @Test
    fun filledTextField_withInput() {
        composeTestRule.setMaterialContent {
            Box(Modifier.semantics(mergeAllDescendants = true).testTag(TextFieldTag)) {
                FilledTextField(
                    value = "Text",
                    onValueChange = {},
                    label = { Text("Label") }
                )
            }
        }

        assertAgainstGolden("filled_textField_withInput")
    }

    @Test
    fun filledTextField_notFocused() {
        composeTestRule.setMaterialContent {
            Box(Modifier.semantics(mergeAllDescendants = true).testTag(TextFieldTag)) {
                FilledTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Label") }
                )
            }
        }

        assertAgainstGolden("filled_textField_not_focused")
    }

    @Test
    fun filledTextField_focused() {
        composeTestRule.setMaterialContent {
            Box(Modifier.semantics(mergeAllDescendants = true).testTag(TextFieldTag)) {
                FilledTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Label") }
                )
            }
        }

        findByTag(TextFieldTag)
            .doClick()

        assertAgainstGolden("filled_textField_focused")
    }

    private fun assertAgainstGolden(goldenIdentifier: String) {
        findByTag(TextFieldTag)
            .captureToBitmap()
            .assertAgainstGolden(screenshotRule, goldenIdentifier)
    }
}