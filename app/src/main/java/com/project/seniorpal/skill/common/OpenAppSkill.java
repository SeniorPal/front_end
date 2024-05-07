package com.project.seniorpal.skill.common;

import android.content.ComponentName;
import android.content.Intent;
import com.project.seniorpal.skill.accessibility.AccessibilityOperator;
import com.project.seniorpal.skill.accessibility.AccessibilitySkill;

import java.util.HashMap;
import java.util.Map;

public final class OpenAppSkill extends AccessibilitySkill {

    private static final Map<String, String> argsDesc;

    static {
        argsDesc = new HashMap<>();
        argsDesc.put("packageName", "Package name of the application to open.");
        argsDesc.put("className", "Class name of the main activity to open.");
    }

    public OpenAppSkill(AccessibilityOperator operator) {
        super("com.project.seniorpal.OpenApp",
                "Open an application with given parameters.", argsDesc,
                operator);
    }

    @Override
    protected Map<String, String> active(Map<String, String> optimizedArgs) {
        try {
            openApp(optimizedArgs);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void openApp(Map<String, String> args) throws InterruptedException {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(args.get("packageName"), args.get("className")));

        System.out.println("Opening application.");
        operator.startActivity(intent);
        System.out.println("Application opened.");
    }
}
