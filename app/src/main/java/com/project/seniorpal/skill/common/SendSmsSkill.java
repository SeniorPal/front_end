package com.project.seniorpal.skill.common;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.project.seniorpal.skill.ContextSkill;
import com.project.seniorpal.skill.accessibility.AccessibilityOperator;
import com.project.seniorpal.skill.accessibility.AccessibilitySkill;

import java.util.HashMap;
import java.util.Map;

public final class SendSmsSkill extends ContextSkill {

    private static final Map<String, String> argsDesc;

    static {
        argsDesc = new HashMap<>();
        argsDesc.put("phoneNumber", "Phone number to send the SMS to. (number)");
        argsDesc.put("message", "Message content of the SMS.");
    }

    public SendSmsSkill(Context operator) {
        super("com.project.seniorpal.SendSms", "Send an SMS with a phone number and a message.", argsDesc, operator);
    }

    @Override
    protected Map<String, String> active(Map<String, String> optimizedArgs) {
        try {
            sendSms(optimizedArgs.get("phoneNumber"), optimizedArgs.get("message"));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void sendSms(String phoneNumber, String message) throws InterruptedException {
        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.setData(Uri.parse("smsto:" + phoneNumber));
        sendIntent.putExtra("sms_body", message);
        context.startActivity(sendIntent);
    }
}
