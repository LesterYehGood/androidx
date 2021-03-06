/*
 * Copyright 2019 The Android Open Source Project
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

package androidx.core.view;

import static android.content.Context.DISPLAY_SERVICE;
import static android.content.Context.UI_MODE_SERVICE;
import static android.content.Context.WINDOW_SERVICE;
import static android.content.res.Configuration.UI_MODE_TYPE_NORMAL;
import static android.content.res.Configuration.UI_MODE_TYPE_TELEVISION;

import static com.google.common.truth.Truth.assertThat;

import static org.robolectric.Shadows.shadowOf;

import android.app.UiModeManager;
import android.content.ContextWrapper;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.shadows.ShadowDisplayManager;
import org.robolectric.shadows.ShadowPackageManager;
import org.robolectric.shadows.ShadowSystemProperties;
import org.robolectric.shadows.ShadowUIModeManager;
import org.robolectric.util.ReflectionHelpers;

import java.util.Optional;

@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
@Config(
        sdk = {Build.VERSION_CODES.LOLLIPOP, Build.VERSION_CODES.P},
        // sets the display size for the tests (http://robolectric.org/device-configuration/)
        qualifiers = "w2048dp-h4096dp"
)
public final class DisplayCompatTest {
    private static final int DISPLAY_WIDTH = 2048;
    private static final int DISPLAY_HEIGHT = 4096;
    // Set SystemProperties to a different value s.t. the result can be distinguished
    private static final int DISPLAY_WIDTH_VENDOR = DISPLAY_WIDTH + 1;
    private static final int DISPLAY_HEIGHT_VENDOR = DISPLAY_HEIGHT + 1;
    private static final int DISPLAY_WIDTH_VENDOR_P = DISPLAY_WIDTH + 2;
    private static final int DISPLAY_HEIGHT_VENDOR_P = DISPLAY_HEIGHT + 2;
    private final ContextWrapper mContext = ApplicationProvider.getApplicationContext();
    private ShadowUIModeManager mUiModeManagerShadow;
    private DisplayManager mDisplayManager;
    private WindowManager mWindowManager;
    private Display mDefaultDisplay;

    private Optional<DisplayCompat.ModeCompat> findNativeMode(DisplayCompat.ModeCompat[] modes) {
        for (DisplayCompat.ModeCompat modeCompat : modes) {
            if (modeCompat.isNative()) {
                return Optional.of(modeCompat);
            }
        }
        return Optional.empty();
    }

    @Before
    @SuppressWarnings("deprecation") /* defaultDisplay */
    public void setup() {
        mUiModeManagerShadow = shadowOf((UiModeManager) mContext.getSystemService(UI_MODE_SERVICE));
        mUiModeManagerShadow.currentModeType = UI_MODE_TYPE_NORMAL;
        mDisplayManager = (DisplayManager) mContext.getSystemService(DISPLAY_SERVICE);
        mWindowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        mDefaultDisplay = mWindowManager.getDefaultDisplay();
        ShadowSystemProperties.reset();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            // before treble
            ShadowSystemProperties.override(
                    "sys.display-size",
                    String.format("%dx%d", DISPLAY_WIDTH_VENDOR, DISPLAY_HEIGHT_VENDOR));
        } else {
            ShadowSystemProperties.override(
                    "vendor.display-size",
                    String.format("%dx%d", DISPLAY_WIDTH_VENDOR_P, DISPLAY_HEIGHT_VENDOR_P));
        }
    }

    @Test
    public void defaultDisplay_sizeFromSystemProperty() {
        Optional<DisplayCompat.ModeCompat> nativeMode =
                findNativeMode(DisplayCompat.getSupportedModes(mContext, mDefaultDisplay));
        assertThat(nativeMode.isPresent());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            assertThat(nativeMode.get().getPhysicalWidth()).isEqualTo(DISPLAY_WIDTH_VENDOR);
            assertThat(nativeMode.get().getPhysicalHeight()).isEqualTo(DISPLAY_HEIGHT_VENDOR);
        } else {
            assertThat(nativeMode.get().getPhysicalWidth()).isEqualTo(DISPLAY_WIDTH_VENDOR_P);
            assertThat(nativeMode.get().getPhysicalHeight()).isEqualTo(DISPLAY_HEIGHT_VENDOR_P);
        }
    }

    @Test
    public void secondDisplay_sizeFromAccessorFunction() {
        int displayWidth = 100;
        int displayHeight = 200;
        String displayQualifierString = String.format("w%ddp-h%ddp", displayWidth, displayHeight);
        int secondDisplayId = ShadowDisplayManager.addDisplay(displayQualifierString);
        Display secondDisplay = mDisplayManager.getDisplay(secondDisplayId);
        Optional<DisplayCompat.ModeCompat> nativeMode =
                findNativeMode(DisplayCompat.getSupportedModes(mContext, secondDisplay));
        assertThat(nativeMode.isPresent());
        assertThat(nativeMode.get().getPhysicalWidth()).isEqualTo(displayWidth);
        assertThat(nativeMode.get().getPhysicalHeight()).isEqualTo(displayHeight);
    }

    @Test
    public void emptySystemProperties_sizeFromAccessorFunction() {
        ShadowSystemProperties.override("sys.display-size", "");
        ShadowSystemProperties.override("vendor.display-size", "");
        Optional<DisplayCompat.ModeCompat> nativeMode =
                findNativeMode(DisplayCompat.getSupportedModes(mContext, mDefaultDisplay));
        assertThat(nativeMode.isPresent());
        assertThat(nativeMode.get().getPhysicalWidth()).isEqualTo(DISPLAY_WIDTH);
        assertThat(nativeMode.get().getPhysicalHeight()).isEqualTo(DISPLAY_HEIGHT);
    }

    @Test
    public void tvModeSonyBraviaSpecialCase_hardcodedSize() {
        mUiModeManagerShadow.currentModeType = UI_MODE_TYPE_TELEVISION;
        ReflectionHelpers.setStaticField(android.os.Build.class, "MANUFACTURER", "Sony");
        ReflectionHelpers.setStaticField(android.os.Build.class, "MODEL", "BRAVIA-EX550");
        ShadowPackageManager packageManagerShadow = new ShadowPackageManager();
        packageManagerShadow.setSystemFeature("com.sony.dtv.hardware.panel.qfhd", true);
        ShadowSystemProperties.override("sys.display-size", "");
        ShadowSystemProperties.override("vendor.display-size", "");
        Optional<DisplayCompat.ModeCompat> nativeMode =
                findNativeMode(DisplayCompat.getSupportedModes(mContext, mDefaultDisplay));
        assertThat(nativeMode.isPresent());
        // assert that the returned displaySize is equal to the 4k display size
        assertThat(nativeMode.get().getPhysicalWidth()).isEqualTo(3840);
        assertThat(nativeMode.get().getPhysicalHeight()).isEqualTo(2160);
    }
}
