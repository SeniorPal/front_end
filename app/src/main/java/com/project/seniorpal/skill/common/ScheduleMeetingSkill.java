package com.project.seniorpal.skill.common;

import android.content.Intent;
import android.provider.CalendarContract;
import com.project.seniorpal.skill.accessibility.AccessibilityOperator;
import com.project.seniorpal.skill.accessibility.AccessibilitySkill;

import java.util.HashMap;
import java.util.Map;

public final class ScheduleMeetingSkill extends AccessibilitySkill {

    private static final Map<String, String> argsDesc;

    static {
        argsDesc = new HashMap<>();
        argsDesc.put("title", "Title of the meeting.");
        argsDesc.put("location", "Location of the meeting.");
        argsDesc.put("description", "Description of the meeting.");
        argsDesc.put("startDateTime", "Start time of the meeting in milliseconds.");
        argsDesc.put("endDateTime", "End time of the meeting in milliseconds.");
    }

    public ScheduleMeetingSkill(AccessibilityOperator operator) {
        super("com.project.seniorpal.ScheduleMeeting",
                "Schedule a meeting with given title, location, description, period. Parameters can be shorten.", argsDesc,
                operator);
    }

    @Override
    protected Map<String, String> active(Map<String, String> optimizedArgs) {
        try {
            scheduleMeeting(optimizedArgs);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void scheduleMeeting(Map<String, String> args) throws InterruptedException {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE, args.get("title"));
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, args.get("location"));
        intent.putExtra(CalendarContract.Events.DESCRIPTION, args.get("description"));
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, Long.parseLong(args.get("startDateTime")));
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, Long.parseLong(args.get("endDateTime")));

        System.out.println("Scheduling meeting.");
        operator.startActivity(intent);
        System.out.println("Meeting scheduled.");
    }
}
