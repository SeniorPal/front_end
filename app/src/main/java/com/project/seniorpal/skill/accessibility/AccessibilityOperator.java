package com.project.seniorpal.skill.accessibility;

import android.accessibilityservice.AccessibilityGestureEvent;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class AccessibilityOperator extends AccessibilityService {

    private static volatile AccessibilityOperator serviceInstance = null;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        serviceInstance = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceInstance = null;
    }

    @Override
    public boolean onGesture(AccessibilityGestureEvent gestureEvent) {

        return super.onGesture(gestureEvent);
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
                AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED |
                AccessibilityEvent.TYPE_VIEW_LONG_CLICKED |
                AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START |
                AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END |
                AccessibilityEvent.TYPE_GESTURE_DETECTION_START |
                AccessibilityEvent.TYPE_GESTURE_DETECTION_END;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL;
        info.notificationTimeout = 100;
        this.setServiceInfo(info);
    }

    /**
     * Find all children of the RootInActiveWindow (From method getRootInActiveWindow()) fulfill the predicate.
     * @param predicate The predicate to use. Return true if you want the view in return.
     * @return A list of views fulfill the predicate.
     */
    public List<AccessibilityNodeInfo> findViewsByPredicate(Predicate<AccessibilityNodeInfo> predicate) {
        AccessibilityNodeInfo window = getRootInActiveWindow();
        if (window == null) {
            return new ArrayList<>();
        }
        return findViewsByPredicate(predicate, true, window);
    }

    /**
     * Find all children of a view fulfill the predicate.
     * @param predicate The predicate to use. Return true if you want the view in return.
     * @return A list of views fulfill the predicate.
     */
    public List<AccessibilityNodeInfo> findViewsByPredicate(Predicate<AccessibilityNodeInfo> predicate, AccessibilityNodeInfo view) {
        return findViewsByPredicate(predicate, true, view);
    }


    protected List<AccessibilityNodeInfo> findViewsByPredicate(Predicate<AccessibilityNodeInfo> predicate, boolean recursive, AccessibilityNodeInfo view) {
        List<AccessibilityNodeInfo> viewsWithText = new ArrayList<>();
        for (int i = 0, length = view.getChildCount(); i < length; i++) {
            AccessibilityNodeInfo child = view.getChild(i);
            if (child == null) {
                continue;
            }
            if (predicate.test(child)) {
                viewsWithText.add(child);
            }
            if (recursive && child.getChildCount() > 0) {
                viewsWithText.addAll(findViewsByPredicate(predicate, true, child));
            }
        }
        return viewsWithText;
    }

    /**
     * Click onto the center of the view by simulating gestures.
     * @param view The view to click onto.
     */
    public void clickOn(AccessibilityNodeInfo view) {
        Rect bounds = new Rect();
        view.getBoundsInScreen(bounds);
        Path gesture = new Path();
        gesture.moveTo(bounds.centerX(), bounds.centerY());
        performanceGesture(gesture);
    }

    /**
     * Perform a gesture described by a Path.
     * @param gesture The Path for the gesture.
     */
    public void performanceGesture(Path gesture) {
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(gesture, 0, 100));
        dispatchGesture(builder.build(), null, null);
    }

    public static boolean isServiceActive() {
        return serviceInstance != null;
    }

    public static AccessibilityOperator getServiceInstance() {
        return serviceInstance;
    }

}
