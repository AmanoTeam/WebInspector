package com.amanoteam.webinspector;

import android.app.UiModeManager;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;



import com.amanoteam.webinspector.R;
import com.amanoteam.webinspector.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {
	
	private SharedPreferences settings;
	private PreferenceScreen preferenceScreen;
	
	private SettingsFragment settingsFragment;
	
	private final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(final SharedPreferences settings, final String key) {
			
			if (key.equals("appTheme") || key.equals("textInputStyle")) {
				recreate();
			}
			
			if (preferenceScreen != null) {
				if (key.equals("blockNetworkLoads")) {
					final CheckBoxPreference blockNetworkImage = (CheckBoxPreference) preferenceScreen.findPreference("blockNetworkImage");
					
					if (settings.getBoolean("blockNetworkLoads", false)) {
						blockNetworkImage.setEnabled(false);
					} else {
						blockNetworkImage.setEnabled(true);
					}
				} else if (key.equals("userAgent")) {
					final EditTextPreference customUserAgent = (EditTextPreference) preferenceScreen.findPreference("customUserAgent");
					
					if (settings.getString("userAgent", "default").equals("custom")) {
						customUserAgent.setEnabled(true);
					} else {
						customUserAgent.setEnabled(false);
					}
				} else if (key.equals("enableJavascript")) {
					final CheckBoxPreference allowOpeningWindowsAutomatically = (CheckBoxPreference) preferenceScreen.findPreference("allowOpeningWindowsAutomatically");
					
					if (settings.getBoolean("enableJavascript", true)) {
						allowOpeningWindowsAutomatically.setEnabled(true);
					} else {
						allowOpeningWindowsAutomatically.setEnabled(false);
					}
				}
			}
			
		}
	};
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		// Preferences stuff
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		settings.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
		
		// Dark mode stuff
		final String appTheme = settings.getString("appTheme", "follow_system");
		
		boolean isDarkMode = false;
		
		if (appTheme.equals("follow_system")) {
			// Snippet from https://github.com/Andrew67/dark-mode-toggle/blob/11c1e16071b301071be0c4715a15fcb031d0bb64/app/src/main/java/com/andrew67/darkmode/UiModeManagerUtil.java#L17
			final UiModeManager uiModeManager = ContextCompat.getSystemService(this, UiModeManager.class);
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_CAR) {
				isDarkMode = true;
			}
		} else if (appTheme.equals("dark")) {
			isDarkMode = true;
		}
		
		if (isDarkMode) {
			setTheme(R.style.DarkTheme);
		}
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.settings_activity);
		
		// Action bar
		final Toolbar settingsToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
		setSupportActionBar(settingsToolbar);
		
		settingsFragment = new SettingsFragment();
		
		// Preferences screen
		getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.frame_layout_settings, settingsFragment)
			.commit();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		preferenceScreen = settingsFragment.getPreferenceScreen();
		
		final CheckBoxPreference blockNetworkImage = (CheckBoxPreference) preferenceScreen.findPreference("blockNetworkImage");
		
		if (settings.getBoolean("blockNetworkLoads", false)) {
			blockNetworkImage.setEnabled(false);
		} else {
			blockNetworkImage.setEnabled(true);
		}
		
		final EditTextPreference customUserAgent = (EditTextPreference) preferenceScreen.findPreference("customUserAgent");
		
		if (settings.getString("userAgent", "default").equals("custom")) {
			customUserAgent.setEnabled(true);
		} else {
			customUserAgent.setEnabled(false);
		}
		
		final CheckBoxPreference allowOpeningWindowsAutomatically = (CheckBoxPreference) preferenceScreen.findPreference("allowOpeningWindowsAutomatically");
		
		if (settings.getBoolean("enableJavascript", true)) {
			allowOpeningWindowsAutomatically.setEnabled(true);
		} else {
			allowOpeningWindowsAutomatically.setEnabled(false);
		}
	
	}
	
	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}
	
}