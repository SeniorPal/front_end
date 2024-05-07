package com.project.seniorpal.skill.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.*;

public final class WeChatCallSkill extends AccessibilitySkill {

    private static final Map<String, String> argsDesc;

    static {
        argsDesc = new HashMap<>();
        argsDesc.put("contactTo", "The contact to have a call with in Chinese Pinyin.");
    }

    public WeChatCallSkill(AccessibilityOperator operator) {
        super("com.project.seniorpal.WeChatCall", "Call a contact with WeChat.", argsDesc, operator);
    }

    @Override
    protected Map<String, String> active(Map<String, String> optimizedArgs) {
        try {
            turnOnWeChat();
            gotoMainMenu();
            scrollUp();
            gotoSearch();
            search(optimizedArgs.get("contactTo"));
            gotoTargetMem();
            call();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void turnOnWeChat() {
        System.out.println("Starting WeChat.");
        Intent intentToTurnOnWeChat = new Intent();
        intentToTurnOnWeChat.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI"));
        intentToTurnOnWeChat.addCategory(Intent.CATEGORY_LAUNCHER);
        intentToTurnOnWeChat.setAction(Intent.ACTION_MAIN);
        intentToTurnOnWeChat.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        operator.startActivity(intentToTurnOnWeChat);
        System.out.println("Started WeChat.");
    }

    private void gotoMainMenu() throws InterruptedException {
        System.out.println("Going to main menu");
        Thread.sleep(1000);
        while (true){
            Set<String> keys = new HashSet<>(Arrays.asList("微信", "通讯录", "发现", "我"));
            List<AccessibilityNodeInfo> tvs = operator.getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/icon_tv");
            if (tvs.size() == 4 && tvs.stream().allMatch(view -> view != null && keys.contains(view.getText().toString()))) {
                break;
            }
            operator.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            Thread.sleep(1000);
        }
        System.out.println("Went to main menu");
    }

    private void scrollUp() throws InterruptedException {
        System.out.println("Scrolling up.");
        AccessibilityNodeInfo top = operator.getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/gp").get(0);
        if (top != null) {
            operator.clickOn(top);
            Thread.sleep(100);
            operator.clickOn(top);
            System.out.println("Scrolled up.");
        }
        Thread.sleep(1000);
    }

    private void gotoSearch() throws InterruptedException {

        System.out.println("Going to search.");
        AccessibilityNodeInfo search = operator.getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/meb").get(0);
        if (search != null && search.isClickable()) {
            operator.clickOn(search);
            System.out.println("Went to search.");
        }
        Thread.sleep(1000);
    }

    private void search(String text) throws InterruptedException {
        System.out.println("Searching.");
        AccessibilityNodeInfo search = operator.getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/d98").get(0);
        if (search != null && search.isEditable()) {
            Bundle bundleOfText = new Bundle();
            bundleOfText.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            search.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundleOfText);
            System.out.println("Searched.");
        }
        Thread.sleep(1000);
    }

    private void gotoTargetMem() throws InterruptedException {
        System.out.println("Going to target mem.");
        operator.clickOn(operator.getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/mem").stream().filter(view -> view != null && view.getClassName().equals("android.widget.LinearLayout")).findFirst().get());
        System.out.println("Went to target mem.");
        Thread.sleep(1000);
    }

    private void call() throws InterruptedException {
        AccessibilityNodeInfo moreFunctionButton = operator.getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bjz").get(0);
        if (moreFunctionButton != null) {
            operator.clickOn(moreFunctionButton);
        }
        Thread.sleep(1000);
        AccessibilityNodeInfo videoButtonText = operator.getRootInActiveWindow().findAccessibilityNodeInfosByText("视频通话").get(0);
        if (videoButtonText != null) {
            operator.clickOn(videoButtonText);
        }
        Thread.sleep(1000);
        AccessibilityNodeInfo videoAudioSelector = operator.getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/avc").get(0);
        if (videoAudioSelector != null) {
            AccessibilityNodeInfo audioButton = videoAudioSelector.getChild(1);
            if (audioButton != null) {
                operator.clickOn(audioButton);
            }
        }
    }
}
