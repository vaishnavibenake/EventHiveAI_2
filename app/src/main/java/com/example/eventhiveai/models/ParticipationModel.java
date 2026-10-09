package com.example.eventhiveai.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Per-student participation record.
 * Every student who is part of a registration (individual or team) gets one of these.
 * This ensures each team member can look up their own participation even if
 * someone else registered the team.
 */
public class ParticipationModel {
    private String participationId;
    private String studentId;
    private String studentName;
    private String studentEmail;
    private String department;
    private String eventId;
    private String eventName;
    private String eventDate;
    private String eventVenue;
    private String registrationId;
    private String teamId;          // empty for individual events
    private String teamName;        // empty for individual events
    private String participationType; // "Individual", "Pair", "Team"
    private String attendance;      // "pending", "present", "absent"
    private String result;          // "none", "1st", "2nd", "3rd", "participant"
    private String status;          // "registered", "cancelled"

    @ServerTimestamp
    private Date createdAt;

    public ParticipationModel() {
        // Required for Firestore
    }

    public ParticipationModel(String participationId, String studentId, String studentName,
                              String studentEmail, String department, String eventId,
                              String eventName, String eventDate, String eventVenue,
                              String registrationId, String teamId, String teamName,
                              String participationType, String attendance,
                              String result, String status) {
        this.participationId = participationId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.department = department;
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventVenue = eventVenue;
        this.registrationId = registrationId;
        this.teamId = teamId;
        this.teamName = teamName;
        this.participationType = participationType;
        this.attendance = attendance;
        this.result = result;
        this.status = status;
    }

    // Getters and setters
    public String getParticipationId() { return participationId != null ? participationId : ""; }
    public void setParticipationId(String participationId) { this.participationId = participationId; }

    public String getStudentId() { return studentId != null ? studentId : ""; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName != null ? studentName : ""; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentEmail() { return studentEmail != null ? studentEmail : ""; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public String getDepartment() { return department != null ? department : ""; }
    public void setDepartment(String department) { this.department = department; }

    public String getEventId() { return eventId != null ? eventId : ""; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName != null ? eventName : ""; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getEventDate() { return eventDate != null ? eventDate : ""; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    public String getEventVenue() { return eventVenue != null ? eventVenue : ""; }
    public void setEventVenue(String eventVenue) { this.eventVenue = eventVenue; }

    public String getRegistrationId() { return registrationId != null ? registrationId : ""; }
    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }

    public String getTeamId() { return teamId != null ? teamId : ""; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName != null ? teamName : ""; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getParticipationType() { return participationType != null ? participationType : "Individual"; }
    public void setParticipationType(String participationType) { this.participationType = participationType; }

    public String getAttendance() { return attendance != null ? attendance : "pending"; }
    public void setAttendance(String attendance) { this.attendance = attendance; }

    public String getResult() { return result != null ? result : "none"; }
    public void setResult(String result) { this.result = result; }

    public String getStatus() { return status != null ? status : "registered"; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
