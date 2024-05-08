package com.project.seniorpal.skill.common;

import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import com.project.seniorpal.skill.ContextSkill;
import com.project.seniorpal.skill.accessibility.AccessibilityOperator;
import com.project.seniorpal.skill.accessibility.AccessibilitySkill;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public final class ScheduleMeetingSkill extends ContextSkill {

    private static final Map<String, String> argsDesc;

    static {
        argsDesc = new HashMap<>();
        argsDesc.put("packageName", "Package name of the calendar app to use.");
        argsDesc.put("title", "Title of the meeting.");
        argsDesc.put("location", "Location of the meeting.");
        argsDesc.put("description", "Description of the meeting.");
        argsDesc.put("startDateTime", "Start time of the meeting in milliseconds.");
        argsDesc.put("endDateTime", "End time of the meeting in milliseconds.");
    }

    public ScheduleMeetingSkill(Context operator) {
        super("com.project.seniorpal.ScheduleMeeting", "Schedule a meeting with given parameters.", argsDesc,
                operator);
    }

    @Override
    protected Map<String, String> active(Map<String, String> optimizedArgs) {
        try {
            scheduleMeeting(optimizedArgs.get("packageName"), optimizedArgs.get("title"), optimizedArgs.get("location"),
                    optimizedArgs.get("description"), optimizedArgs.get("startDateTime"),
                    optimizedArgs.get("endDateTime"));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void scheduleMeeting(String packageName, String title, String location, String description,
            String startDateTime, String endDateTime) throws InterruptedException {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE, title);
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, description);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, Long.parseLong(startDateTime));
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, Long.parseLong(endDateTime));

        intent.setPackage(packageName); // Set the package name of the calendar app to use

        System.out.println("Scheduling meeting.");
        context.startActivity(intent);
        System.out.println("Meeting scheduled.");
    }
}
