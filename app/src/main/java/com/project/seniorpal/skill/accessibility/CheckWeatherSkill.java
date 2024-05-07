package com.project.seniorpal.skill.accessibility;

import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

public final class CheckWeatherSkill extends AccessibilitySkill {

    private static final Map<String, String> argsDesc;

    static {
        argsDesc = new HashMap<>();
        argsDesc.put("packageName", "Package name of the weather app to use.");
        argsDesc.put("city", "The city to check the weather for.");
    }

    public CheckWeatherSkill(AccessibilityOperator operator) {
        super("xyz.magicalstone.touchcontrol.CheckWeather", "Check today's weather in a specified city.", argsDesc,
                operator);
    }

    @Override
    protected Map<String, String> active(Map<String, String> optimizedArgs) {
        try {
            checkWeather(optimizedArgs.get("packageName"), optimizedArgs.get("city"));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void checkWeather(String packageName, String city) throws InterruptedException {
        Intent intent = new Intent();
        intent.setPackage(packageName); // Set the package name of the weather app to use
        // Additional logic to check weather using the specified weather app
        System.out.println("Checking weather for " + city + " using " + packageName);
    }
}
