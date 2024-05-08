package com.project.seniorpal.skill.common;

import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.util.Log;
import com.project.seniorpal.skill.ContextSkill;
import com.project.seniorpal.skill.accessibility.AccessibilityOperator;
import com.project.seniorpal.skill.accessibility.AccessibilitySkill;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public final class ScheduleMeetingSkill extends ContextSkill {

    private static final Map<String, String> argsDesc;

    static {
        argsDesc = new HashMap<>();
        argsDesc.put("title", "Title of the meeting.");
        argsDesc.put("location", "Location of the meeting.");
        argsDesc.put("description", "Description of the meeting.");
        argsDesc.put("daysAfter", "Days after today (the day the user asks for scheduling a meeting, 0 if the meeting is on today) (number).");
        argsDesc.put("startTime", "The time in hours and minutes the meet starts in the format of (hh:mm). For example, a meet at starts at 15:25 starts at (15:25).");
        argsDesc.put("interval", "Interval of the meeting in hours (number). For example, if the meeting lasts for one hour and 30 minutes, its interval is 1.5. Optional.");
    }

    public ScheduleMeetingSkill(Context context) {
        super("com.project.seniorpal.ScheduleMeeting", "Schedule a meeting with a title, a location, a description and an interval.", argsDesc,
                context);
    }

    @Override
    protected Map<String, String> active(Map<String, String> optimizedArgs) {
        try {
            scheduleMeeting(optimizedArgs.get("title"), optimizedArgs.get("location"),
                    optimizedArgs.get("description"), optimizedArgs.get("daysAfter"),
                    optimizedArgs.get("startTime"), optimizedArgs.get("interval"));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void scheduleMeeting(String title, String location, String description,
            String daysAfter, String startTime, String interval) throws InterruptedException {
        Log.i("Skill", "Scheduling meeting.");
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        if (title != null) {
            intent.putExtra(CalendarContract.Events.TITLE, title);
        }
        if (location != null) {
            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);
        }
        if (description != null) {
            intent.putExtra(CalendarContract.Events.DESCRIPTION, description);
        }
        try {
            LocalDate targetDate = LocalDate.now().plusDays(Integer.parseInt(daysAfter));
            String[] splitStartTime = startTime.split("[(:)]");
            LocalTime startExactTime = LocalTime.of(Integer.parseInt(splitStartTime[splitStartTime.length - 2]), Integer.parseInt(splitStartTime[splitStartTime.length - 1]));
            long startExactTimeMillis = LocalDateTime.of(targetDate, startExactTime).toEpochSecond(ZoneOffset.of(TimeZone.getDefault().getID()));
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startExactTimeMillis);
            try {
                long endExactTime = startExactTimeMillis + ((long) (Double.parseDouble(interval) * 60)) * 60L * 1000;
                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endExactTime);
            } catch (NumberFormatException | NullPointerException ignored) {

            }
        } catch (NumberFormatException | NullPointerException ignored) {
            intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
        }

        context.startActivity(intent);
        Log.i("Skill", "Scheduling meeting.");
    }
}
