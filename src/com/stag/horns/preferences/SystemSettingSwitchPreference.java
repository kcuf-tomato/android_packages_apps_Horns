/*
 * Copyright (C) 2013 The CyanogenMod project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stag.horns.preferences;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.AndroidResources;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceViewHolder;

import android.widget.Switch;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.util.AttributeSet;


import android.util.Log;

import com.android.settings.R;

public class SystemSettingSwitchPreference extends SwitchPreference {

    static final String TAG = "SystemSettingSwitchPreference";
    private final Listener mListener = new Listener();


    public SystemSettingSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SystemSettingSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SystemSettingSwitchPreference(Context context) {
        super(context, null);
    }

    @Override
    protected boolean persistBoolean(boolean value) {
        if (shouldPersist()) {
            if (value == getPersistedBoolean(!value)) {
                // It's already there, so the same as persisting
                return true;
            }
            Settings.System.putInt(getContext().getContentResolver(), getKey(), value ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    protected boolean getPersistedBoolean(boolean defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        return Settings.System.getInt(getContext().getContentResolver(),
                getKey(), defaultReturnValue ? 1 : 0) != 0;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setChecked(Settings.System.getString(getContext().getContentResolver(), getKey()) != null ? getPersistedBoolean(isChecked())
                : (Boolean) defaultValue);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View switchView = holder.findViewById(android.R.id.switch_widget);
        syncSwitchView(switchView);
    }

    private void syncSwitchView(View view) {
	if (view instanceof Switch) {
            final Switch switchView = (Switch) view;
	    switchView.setOnLongClickListener(mListener);
	}
    }

    private class Listener implements OnLongClickListener {
        Listener() {}

        @Override
	public boolean onLongClick(View v) {
	    Log.w(TAG, "Preference with key " + SystemSettingSwitchPreference.this.getKey() + " LongClicked");
	    return true;
	}
    }
}
