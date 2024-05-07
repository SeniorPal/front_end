package com.project.seniorpal.skill.common;

import android.content.Intent;
import android.net.Uri;
import com.project.seniorpal.skill.accessibility.AccessibilityOperator;
import com.project.seniorpal.skill.accessibility.AccessibilitySkill;

import java.util.HashMap;
import java.util.Map;

public final class SendSmsSkill extends AccessibilitySkill {

    private static final Map<String, String> argsDesc;

    static {
        argsDesc = new HashMap<>();
        argsDesc.put("packageName", "Package name of the SMS application to use.");
        argsDesc.put("phoneNumber", "Phone number to send the SMS to.");
        argsDesc.put("message", "Message content of the SMS.");
    }

    public SendSmsSkill(AccessibilityOperator operator) {
        super("xyz.magicalstone.touchcontrol.SendSms", "Send an SMS with given parameters.", argsDesc, operator);
    }

    @Override
    protected Map<String, String> active(Map<String, String> optimizedArgs) {
        try {
            sendSms(optimizedArgs.get("packageName"), optimizedArgs.get("phoneNumber"), optimizedArgs.get("message"));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void sendSms(String packageName, String phoneNumber, String message) throws InterruptedException {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("sms:" + phoneNumber));
        intent.putExtra("sms_body", message);

        intent.setPackage(packageName); // Set the package name of the SMS app to use

        System.out.println("Sending SMS.");
        operator.startActivity(intent);
        System.out.println("SMS sent.");
    }
}
