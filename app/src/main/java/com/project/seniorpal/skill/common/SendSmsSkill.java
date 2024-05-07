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
        argsDesc.put("phoneNumber", "Phone number to send the SMS to.");
        argsDesc.put("message", "Message content of the SMS.");
    }

    public SendSmsSkill(AccessibilityOperator operator) {
        super("com.project.seniorpal.SendSms", "Send an SMS with given phone number and message content.",
                argsDesc, operator);
    }

    @Override
    protected Map<String, String> active(Map<String, String> optimizedArgs) {
        try {
            sendSms(optimizedArgs);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void sendSms(Map<String, String> args) throws InterruptedException {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("sms:" + args.get("phoneNumber")));
        intent.putExtra("sms_body", args.get("message"));

        System.out.println("Sending SMS.");
        operator.startActivity(intent);
        System.out.println("SMS sent.");
    }
}
