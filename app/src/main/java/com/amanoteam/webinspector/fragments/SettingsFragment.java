package com.amanoteam.webinspector.fragments;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import com.amanoteam.webinspector.R;

public class SettingsFragment extends PreferenceFragmentCompat {
	
	@Override
	public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
	}
	
}