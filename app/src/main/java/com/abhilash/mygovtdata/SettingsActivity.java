package com.abhilash.mygovtdata;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import java.util.List;

public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);

        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_station_key)));
    }
    */

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_general, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return SettingsFragment.class.getName().equals(fragmentName);
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
