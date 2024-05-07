package com.project.seniorpal.skill.accessibility;

import android.content.ComponentName;
import android.content.Intent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class CheckWeatherSkill extends AccessibilitySkill {

    public CheckWeatherSkill(AccessibilityOperator operator) {
        super("com.project.seniorpal.CheckWeather", "Check today's weather.", Collections.EMPTY_MAP, operator);
    }

    @Override
    protected Map<String, String> active(Map<String, String> optimizedArgs) {
        try {
            checkWeather();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void checkWeather() throws InterruptedException {
        openWeatherApp();
        String weatherInfo = getWeatherInfo();
        System.out.println("Today's weather: " + weatherInfo);
    }

    private void openWeatherApp() throws InterruptedException {
        System.out.println("Opening weather application.");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.example.weather", "com.example.weather.MainActivity"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        operator.startActivity(intent);
        System.out.println("Weather application opened.");
    }

    private String getWeatherInfo() throws InterruptedException {
        Thread.sleep(2000); // Assuming it takes some time for the app to load the weather data
        System.out.println("Fetching weather information.");
        AccessibilityNodeInfo rootNode = operator.getRootInActiveWindow();
        List<AccessibilityNodeInfo> weatherNodes = rootNode
                .findAccessibilityNodeInfosByViewId("com.example.weather:id/weather_info");

        if (!weatherNodes.isEmpty() && weatherNodes.get(0) != null) {
            String weatherInfo = weatherNodes.get(0).getText().toString();
            System.out.println("Weather information fetched.");
            return weatherInfo;
        } else {
            System.out.println("Failed to fetch weather information.");
            return "No weather information available";
        }
    }
}
