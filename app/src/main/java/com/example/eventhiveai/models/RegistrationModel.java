package com.example.eventhiveai.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;

public class RegistrationModel {
    private String registrationId;
    private String eventId;
    private String eventName;
    private String eventDate;
    private String eventVenue;
    private String studentId;       // leader or individual student
    private String studentName;
    private String studentEmail;
    private String status;          // "registered", "cancelled", "attended"
    private String coordinatorId;

    // Team / participation fields
    private String participationType; // "Individual", "Pair", "Team"
    private String teamId;
    private String teamName;
    private List<String> memberIds;
    private List<String> memberNames;
    private int memberCount;
    private String department;

    @ServerTimestamp
    private Date registeredAt;

    public RegistrationModel() {
        // Required for Firestore
    }

    public RegistrationModel(String registrationId, String eventId, String eventName,
                             String eventDate, String eventVenue, String studentId,
                             String studentName, String studentEmail, String status) {
        this.registrationId = registrationId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventVenue = eventVenue;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.status = status;
    }

    public String getRegistrationId() { return registrationId != null ? registrationId : ""; }
    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }

    public String getEventId() { return eventId != null ? eventId : ""; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName != null ? eventName : ""; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getEventDate() { return eventDate != null ? eventDate : ""; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    public String getEventVenue() { return eventVenue != null ? eventVenue : ""; }
    public void setEventVenue(String eventVenue) { this.eventVenue = eventVenue; }

    public String getStudentId() { return studentId != null ? studentId : ""; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName != null ? studentName : ""; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentEmail() { return studentEmail != null ? studentEmail : ""; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public String getStatus() { return status != null ? status : "registered"; }
    public void setStatus(String status) { this.status = status; }

    public String getCoordinatorId() { return coordinatorId != null ? coordinatorId : ""; }
    public void setCoordinatorId(String coordinatorId) { this.coordinatorId = coordinatorId; }

    public Date getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(Date registeredAt) { this.registeredAt = registeredAt; }

    // Team fields
    public String getParticipationType() { return participationType != null ? participationType : "Individual"; }
    public void setParticipationType(String participationType) { this.participationType = participationType; }

    public String getTeamId() { return teamId != null ? teamId : ""; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName != null ? teamName : ""; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public List<String> getMemberIds() { return memberIds; }
    public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }

    public List<String> getMemberNames() { return memberNames; }
    public void setMemberNames(List<String> memberNames) { this.memberNames = memberNames; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public String getDepartment() { return department != null ? department : ""; }
    public void setDepartment(String department) { this.department = department; }
}
