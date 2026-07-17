/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2020 The LineageOS Project
 *               2022-2024 The BlissRoms Project
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

package org.blissroms.settings.asusparts;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;
import androidx.preference.TwoStatePreference;
import org.blissroms.settings.asusparts.doze.DozeSettingsActivity;

public class AsusParts extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String KEY_GLOVE_SWITCH = "glove";
    public static final String GLOVE_PATH = "/proc/driver/glove";
    private TwoStatePreference mGloveSwitch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check system property and remove the category if necessary
        removeMotorCategoryIfNeeded();
    }

    private void removeMotorCategoryIfNeeded() {
        String blissDevice = SystemProperties.get("ro.bliss.device", "");

        if ("I001D".equals(blissDevice)) {
            PreferenceCategory motorCategory = findPreference("category_motor");
            if (motorCategory != null) {
                getPreferenceScreen().removePreference(motorCategory);
            }
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.asusparts, rootKey);

        Preference mDozePref = findPreference("doze");
        mDozePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getContext(), DozeSettingsActivity.class);
                startActivity(intent);
                return true;
            }
        });

        mGloveSwitch = findPreference(KEY_GLOVE_SWITCH);
        mGloveSwitch.setChecked(Settings.System.getInt(getContext().getContentResolver(),
                KEY_GLOVE_SWITCH, 1) != 0);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mGloveSwitch) {
            Settings.System.putInt(getContext().getContentResolver(), KEY_GLOVE_SWITCH, mGloveSwitch.isChecked() ? 1 : 0);
            FileUtils.setValue(GLOVE_PATH, mGloveSwitch.isChecked() ? "1" : "0");
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }
}
