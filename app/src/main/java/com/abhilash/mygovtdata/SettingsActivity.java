package com.abhilash.mygovtdata;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String stringValue = newValue.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int preferenceIndex = listPreference.findIndexOfValue(stringValue);
            if (preferenceIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[preferenceIndex]);
            }
        } else {
            preference.setSummary(stringValue);
        }
        return true;
    }
}
