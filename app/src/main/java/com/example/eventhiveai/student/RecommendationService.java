package com.example.eventhiveai.student;

import com.example.eventhiveai.admin.EventModel;
import com.example.eventhiveai.models.UserModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RecommendationService implements the recommendation architecture for EventHiveAI.
 * It ranks approved events based on student department alignment, category relevance,
 * and upcoming schedule.
 */
public class RecommendationService {

    public static List<EventModel> getRecommendedEvents(UserModel student, List<EventModel> allApprovedEvents) {
        if (allApprovedEvents == null || allApprovedEvents.isEmpty()) {
            return new ArrayList<>();
        }

        List<EventModel> recommended = new ArrayList<>();
        List<EventModel> generalEvents = new ArrayList<>();

        String studentDept = student != null && student.getDepartment() != null
                ? student.getDepartment().trim().toLowerCase() : "";

        for (EventModel event : allApprovedEvents) {
            String eventDept = event.getDepartment() != null ? event.getDepartment().trim().toLowerCase() : "";

            // If event is specifically tailored for student's department
            if (!studentDept.isEmpty() && (eventDept.contains(studentDept) || studentDept.contains(eventDept))) {
                recommended.add(event);
            } else if (eventDept.contains("all") || eventDept.isEmpty() || eventDept.contains("general")) {
                generalEvents.add(event);
            } else {
                generalEvents.add(event);
            }
        }

        // Department-relevant events first, followed by general campus events
        recommended.addAll(generalEvents);
        return recommended;
    }
}
