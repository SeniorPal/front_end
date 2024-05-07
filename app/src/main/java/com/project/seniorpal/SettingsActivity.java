package com.project.seniorpal;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.ListPreference;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            // 添加偏好监听器
            SwitchPreferenceCompat voiceFeedback = findPreference("voice_feedback");
            if (voiceFeedback != null) {
                voiceFeedback.setOnPreferenceChangeListener((preference, newValue) -> {
                    getActivity().recreate(); // 重新创建Activity以刷新界面
                    return true;
                });
            }

            SwitchPreferenceCompat notifications = findPreference("notifications");
            if (notifications != null) {
                notifications.setOnPreferenceChangeListener((preference, newValue) -> {
                    getActivity().recreate(); // 重新创建Activity以刷新界面
                    return true;
                });
            }

            SeekBarPreference voiceSpeed = findPreference("voice_speed");
            if (voiceSpeed != null) {
                voiceSpeed.setOnPreferenceChangeListener((preference, newValue) -> {
                    getActivity().recreate(); // 重新创建Activity以刷新界面
                    return true;
                });
            }

            SwitchPreferenceCompat largeText = findPreference("large_text");
            if (largeText != null) {
                largeText.setOnPreferenceChangeListener((preference, newValue) -> {
                    getActivity().recreate(); // 重新创建Activity以刷新界面
                    return true;
                });
            }

            ListPreference themeSelection = findPreference("theme_selection");
            if (themeSelection != null) {
                themeSelection.setOnPreferenceChangeListener((preference, newValue) -> {
                    getActivity().recreate(); // 重新创建Activity以刷新界面
                    return true;
                });
            }

            SwitchPreferenceCompat accessibilitySwitch = findPreference("accessibility_switch");
            if (accessibilitySwitch != null) {
                accessibilitySwitch.setOnPreferenceChangeListener((preference, newValue) -> {

                    if (!requireActivity().getSharedPreferences("main", Context.MODE_PRIVATE).getBoolean("accessibilityServiceActive", false)) {
                        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    }
                    getActivity().recreate(); // 重新创建Activity以刷新界面
                    return true;
                });
            }
        }
    }
}
