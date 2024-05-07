package com.project.seniorpal.skill.common;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FindPackageNameSkill extends AccessibilitySkill {

    private static final Map<String, String> argsDesc;

    static {
        argsDesc = new HashMap<>();
        argsDesc.put("appName", "The name of the application to find the package for.");
    }

    public FindPackageNameSkill(AccessibilityOperator operator) {
        super("xyz.magicalstone.touchcontrol.FindPackage", "Find the package name of a specified application.",
                argsDesc, operator);
    }

    @Override
    protected Map<String, String> active(Map<String, String> optimizedArgs) {
        try {
            String appName = optimizedArgs.getOrDefault("appName", "");
            return findPackageName(appName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> findPackageName(String appName) {
        PackageManager pm = operator.getContext().getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        Map<String, String> result = new HashMap<>();

        for (ApplicationInfo app : apps) {
            String name = pm.getApplicationLabel(app).toString();
            if (appName.equalsIgnoreCase(name)) {
                result.put("packageName", app.packageName);
                System.out.println("Found package: " + app.packageName + " for application: " + name);
                return result;
            }
        }

        System.out.println("No package found for application: " + appName);
        result.put("packageName", "No package found");
        return result;
    }
}
