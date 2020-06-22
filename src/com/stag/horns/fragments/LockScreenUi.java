/*
 *  Copyright (C) 2018 StagOS Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.stag.horns.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreference;


import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.stag.horns.preferences.CustomSeekBarPreference;
import com.stag.horns.preferences.SystemSettingSwitchPreference;
import com.stag.horns.preferences.SystemSettingListPreference;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class LockScreenUi extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String LOCKSCREEN_CLOCK_SELECTION = "lockscreen_clock_selection";
    private static final String TEXT_CLOCK_ALIGNMENT = "text_clock_alignment";
    private static final String TEXT_CLOCK_PADDING = "text_clock_padding";

    private SystemSettingListPreference mLockClockSelection;
    private ListPreference mTextClockAlign;
    private CustomSeekBarPreference mTextClockPadding;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.horns_lockscreen_ui);

        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        Resources resources = getResources();

        // Lockscreen Clock
        mLockClockSelection = (SystemSettingListPreference) findPreference(LOCKSCREEN_CLOCK_SELECTION);
        boolean mClockSelection = Settings.System.getIntForUser(resolver,
                Settings.System.LOCKSCREEN_CLOCK_SELECTION, 0, UserHandle.USER_CURRENT) == 12
                || Settings.System.getIntForUser(resolver,
                Settings.System.LOCKSCREEN_CLOCK_SELECTION, 0, UserHandle.USER_CURRENT) == 13;
        if (mLockClockSelection == null) {
            Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_CLOCK_SELECTION, 0, UserHandle.USER_CURRENT);
        }
        mLockClockSelection.setOnPreferenceChangeListener(this);

        // Text Clock Alignment
        mTextClockAlign = (ListPreference) findPreference(TEXT_CLOCK_ALIGNMENT);
        mTextClockAlign.setEnabled(mClockSelection);
        mTextClockAlign.setOnPreferenceChangeListener(this);

        // Text Clock Padding
        mTextClockPadding = (CustomSeekBarPreference) findPreference(TEXT_CLOCK_PADDING);
        boolean mTextClockAlignx = Settings.System.getIntForUser(resolver,
                    Settings.System.TEXT_CLOCK_ALIGNMENT, 0, UserHandle.USER_CURRENT) == 1;
        mTextClockPadding.setEnabled(!mTextClockAlignx);

    }

    @Override
    public void onResume() {
        super.onResume();
        updateClock();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mLockClockSelection) {
            updateClock();
        }

        return super.onPreferenceTreeClick(preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
	if (preference == mLockClockSelection) {
            int value = Integer.parseInt((String) newValue);
            String[] defaultClock = getResources().getStringArray(R.array.lockscreen_clock_selection_entries);
            String summary = defaultClock[value];
            mLockClockSelection.setSummary(summary);
            boolean val = Integer.valueOf((String) newValue) == 12
                    || Integer.valueOf((String) newValue) == 13;
            mTextClockAlign.setEnabled(val);
            return true;
        } else if (preference == mTextClockAlign) {
            boolean val = Integer.valueOf((String) newValue) == 1;
            mTextClockPadding.setEnabled(!val);
            return true;
        }
        return false;
    }

    private void updateClock() {
        ContentResolver resolver = getActivity().getContentResolver();
        String currentClock = Settings.Secure.getString(
            resolver, Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_FACE);
        final boolean mIsDefaultClock = currentClock != null && currentClock.contains("DefaultClock") ? true : false;
        String[] defaultClock = getResources().getStringArray(R.array.lockscreen_clock_selection_entries);
        String[] defaultClockValues = getResources().getStringArray(R.array.lockscreen_clock_selection_values);
        String[] pluginClock = getResources().getStringArray(R.array.lockscreen_clock_plugin_entries);
        String[] pluginClockValues = getResources().getStringArray(R.array.lockscreen_clock_plugin_values);
        if (mIsDefaultClock) {
            mLockClockSelection.setEntries(defaultClock);
            mLockClockSelection.setEntryValues(defaultClockValues);
        } else {
            mLockClockSelection.setEntries(pluginClock);
            mLockClockSelection.setEntryValues(pluginClockValues);
            Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_CLOCK_SELECTION, 0, UserHandle.USER_CURRENT);
        }
    }

/*    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_HIDE_CLOCK, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_CLOCK_SELECTION, 2, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCK_CLOCK_FONT_STYLE, 4, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCK_DATE_FONT_STYLE, 14, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_DATE_SELECTION, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCK_DATE_FONT_SIZE, 18, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCK_CLOCK_FONT_SIZE , 50, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCK_OWNERINFO_FONTS, 4, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKOWNER_FONT_SIZE, 18, UserHandle.USER_CURRENT);
    }
*/
    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.HORNS;
    }
}

