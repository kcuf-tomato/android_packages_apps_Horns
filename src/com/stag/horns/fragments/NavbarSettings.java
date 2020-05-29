/*
 * Copyright (C) 2014 TeamEos
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

package com.stag.horns.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.stag.StagUtils;
import com.android.settings.R;

import com.stag.horns.preferences.SystemSettingListPreference;
import com.stag.horns.preferences.SystemSettingSwicthPreference;

public class NavbarSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String NAV_BAR_LAYOUT = "nav_bar_layout";
    private static final String SYSUI_NAV_BAR = "sysui_nav_bar";
    private static final String KEY_CATEGORY_LEFT_SWIPE    = "left_swipe";
    private static final String KEY_CATEGORY_RIGHT_SWIPE   = "right_swipe";

    private ListPreference mNavBarLayout;
    private ListPreference mLeftSwipeActions;
    private ListPreference mRightSwipeActions;

    private Preference mLeftSwipeAppSelection;
    private Preference mRightSwipeAppSelection;

    private PreferenceCategory leftSwipeCategory;
    private PreferenceCategory rightSwipeCategory;

    private SystemSettingListPreference mTimeout;
    private SystemSettingSwitchPreference mExtendedSwipe;

    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.horns_navigation);
        mResolver = getActivity().getContentResolver();

        mNavBarLayout = (ListPreference) findPreference(NAV_BAR_LAYOUT);
        mNavBarLayout.setOnPreferenceChangeListener(this);
        String navBarLayoutValue = Settings.Secure.getString(mResolver, SYSUI_NAV_BAR);
        if (navBarLayoutValue != null) {
            mNavBarLayout.setValue(navBarLayoutValue);
        } else {
            mNavBarLayout.setValueIndex(0);
        }
        leftSwipeCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_LEFT_SWIPE);
        rightSwipeCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_RIGHT_SWIPE);

        int leftSwipeActions = Settings.System.getIntForUser(resolver,
                Settings.System.LEFT_LONG_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        mLeftSwipeActions = (ListPreference) findPreference("left_swipe_actions");
        mLeftSwipeActions.setValue(Integer.toString(leftSwipeActions));
        mLeftSwipeActions.setSummary(mLeftSwipeActions.getEntry());
        mLeftSwipeActions.setOnPreferenceChangeListener(this);

        int rightSwipeActions = Settings.System.getIntForUser(resolver,
                Settings.System.RIGHT_LONG_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        mRightSwipeActions = (ListPreference) findPreference("right_swipe_actions");
        mRightSwipeActions.setValue(Integer.toString(rightSwipeActions));
        mRightSwipeActions.setSummary(mRightSwipeActions.getEntry());
        mRightSwipeActions.setOnPreferenceChangeListener(this);

        mLeftSwipeAppSelection = (Preference) findPreference("left_swipe_app_action");
        boolean isAppSelection = Settings.System.getIntForUser(resolver,
                Settings.System.LEFT_LONG_BACK_SWIPE_ACTION, 0, UserHandle.USER_CURRENT) == 5/*action_app_action*/;
        mLeftSwipeAppSelection.setEnabled(isAppSelection);

        mRightSwipeAppSelection = (Preference) findPreference("right_swipe_app_action");
        isAppSelection = Settings.System.getIntForUser(resolver,
                Settings.System.RIGHT_LONG_BACK_SWIPE_ACTION, 0, UserHandle.USER_CURRENT) == 5/*action_app_action*/;
        mRightSwipeAppSelection.setEnabled(isAppSelection);
        customAppCheck();

        mTimeout = (SystemSettingListPreference) findPreference("long_back_swipe_timeout");

        mExtendedSwipe = (SystemSettingSwitchPreference) findPreference("back_swipe_extended");
        boolean extendedSwipe = Settings.System.getIntForUser(resolver,
                Settings.System.BACK_SWIPE_EXTENDED, 0,
                UserHandle.USER_CURRENT) != 0;
        mExtendedSwipe.setChecked(extendedSwipe);
        mExtendedSwipe.setOnPreferenceChangeListener(this);
        mTimeout.setEnabled(!mExtendedSwipe.isChecked());

        mLeftSwipeAppSelection.setVisible(mLeftSwipeActions.getEntryValues()
                [leftSwipeActions].equals("5"));
        mLeftSwipeAppSelection.setVisible(mRightSwipeActions.getEntryValues()
                [rightSwipeActions].equals("5"));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNavBarLayout) {
            Settings.Secure.putString(mResolver, SYSUI_NAV_BAR, (String) newValue);
            return true;
        } else if (preference == mLeftSwipeActions) {
            int leftSwipeActions = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.LEFT_LONG_BACK_SWIPE_ACTION, leftSwipeActions,
                    UserHandle.USER_CURRENT);
            int index = mLeftSwipeActions.findIndexOfValue((String) objValue);
            mLeftSwipeActions.setSummary(
                    mLeftSwipeActions.getEntries()[index]);
            mLeftSwipeAppSelection.setEnabled(leftSwipeActions == 5);
            actionPreferenceReload();
            customAppCheck();
            return true;
        } else if (preference == mRightSwipeActions) {
            int rightSwipeActions = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.RIGHT_LONG_BACK_SWIPE_ACTION, rightSwipeActions,
                    UserHandle.USER_CURRENT);
            int index = mRightSwipeActions.findIndexOfValue((String) objValue);
            mRightSwipeActions.setSummary(
                    mRightSwipeActions.getEntries()[index]);
            mRightSwipeAppSelection.setEnabled(rightSwipeActions == 5);
            actionPreferenceReload();
            customAppCheck();
            return true;
        } else if (preference == mExtendedSwipe) {
            boolean enabled = ((Boolean) objValue).booleanValue();
            mExtendedSwipe.setChecked(enabled);
            mTimeout.setEnabled(!enabled);
        }
        return false;
    }


    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.HORNS;
    }

    @Override
    public void onResume() {
        super.onResume();
        navbarCheck();
        customAppCheck();
        actionPreferenceReload();
    }

    @Override
    public void onPause() {
        super.onPause();
        navbarCheck();
        customAppCheck();
        actionPreferenceReload();
    }

    private void customAppCheck() {
        mLeftSwipeAppSelection.setSummary(Settings.System.getStringForUser(getActivity().getContentResolver(),
                String.valueOf(Settings.System.LEFT_LONG_BACK_SWIPE_APP_FR_ACTION), UserHandle.USER_CURRENT));
        mRightSwipeAppSelection.setSummary(Settings.System.getStringForUser(getActivity().getContentResolver(),
                String.valueOf(Settings.System.RIGHT_LONG_BACK_SWIPE_APP_FR_ACTION), UserHandle.USER_CURRENT));
    }

    private void navbarCheck() {
        mTimeout.setVisible(true);
        mExtendedSwipe.setVisible(true);
        leftSwipeCategory.setVisible(true);
        rightSwipeCategory.setVisible(true);
        if (StagUtils.isThemeEnabled("com.android.internal.systemui.navbar.gestural")
                || StagUtils.isThemeEnabled("com.android.internal.systemui.navbar.gestural_nopill")
                || StagUtils.isThemeEnabled("com.android.internal.systemui.navbar.gestural_wide_back")
                || StagUtils.isThemeEnabled("com.android.internal.systemui.navbar.gestural_extra_wide_back")
                || StagUtils.isThemeEnabled("com.android.internal.systemui.navbar.gestural_extra_wide_back_nopill")
                || StagUtils.isThemeEnabled("com.android.internal.systemui.navbar.gestural_narrow_back")
                || StagUtils.isThemeEnabled("com.android.internal.systemui.navbar.gestural_narrow_back_nopill")
                || StagUtils.isThemeEnabled("com.android.internal.systemui.navbar.gestural_wide_back_nopill")) {
            mTimeout.setVisible(true);
            mExtendedSwipe.setVisible(true);
            leftSwipeCategory.setVisible(true);
            rightSwipeCategory.setVisible(true);
	} else {
            mTimeout.setVisible(false);
            mExtendedSwipe.setVisible(false);
            leftSwipeCategory.setVisible(false);
            rightSwipeCategory.setVisible(false);
	}
    }

    private void actionPreferenceReload() {
        int leftSwipeActions = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.LEFT_LONG_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        int rightSwipeActions = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.RIGHT_LONG_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);

        // Reload the action preferences
        mLeftSwipeActions.setValue(Integer.toString(leftSwipeActions));
        mLeftSwipeActions.setSummary(mLeftSwipeActions.getEntry());

        mRightSwipeActions.setValue(Integer.toString(rightSwipeActions));
        mRightSwipeActions.setSummary(mRightSwipeActions.getEntry());

        mLeftSwipeAppSelection.setVisible(mLeftSwipeActions.getEntryValues()
                [leftSwipeActions].equals("5"));
        mRightSwipeAppSelection.setVisible(mRightSwipeActions.getEntryValues()
                [rightSwipeActions].equals("5"));
    }

}
