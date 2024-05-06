package com.project.seniorpal;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.ListPreference;

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
        }
    }
}
