package com.example.eventhiveai.admin;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class EventModel {

    private String eventId;
    private String eventName;
    private String description;
    private String date;
    private String startTime;
    private String endTime;
    private String venue;
    private String category;
    private String department;
    private double budget;
    private int maxParticipants;
    private String registrationDeadline;
    private String clubId;
    private String clubName;
    private String coordinatorId;
    private String status; // "pending", "approved", "rejected", "completed", "cancelled"
    private String rejectionReason;

    // Participation settings
    private String participationType; // "Individual", "Pair", "Team"
    private int minTeamSize;
    private int maxTeamSize;
    private int currentParticipants; // tracks current registration count

    @ServerTimestamp
    private Date createdAt;

    public EventModel() {
        // Required empty constructor for Firebase
    }

    // Constructor for quick list displays
    public EventModel(String eventId, String eventName,
                      String clubName, String date,
                      String venue) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.clubName = clubName;
        this.date = date;
        this.venue = venue;
        this.status = "pending";
    }

    // Full constructor
    public EventModel(String eventId, String eventName, String description,
                      String date, String startTime, String endTime,
                      String venue, String category, String department,
                      double budget, int maxParticipants, String registrationDeadline,
                      String clubId, String clubName, String coordinatorId,
                      String status, String rejectionReason) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.venue = venue;
        this.category = category;
        this.department = department;
        this.budget = budget;
        this.maxParticipants = maxParticipants;
        this.registrationDeadline = registrationDeadline;
        this.clubId = clubId;
        this.clubName = clubName;
        this.coordinatorId = coordinatorId;
        this.status = status;
        this.rejectionReason = rejectionReason;
    }

    public String getEventId() {
        return eventId != null ? eventId : "";
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName != null ? eventName : "";
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date != null ? date : "";
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime != null ? startTime : "";
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime != null ? endTime : "";
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getVenue() {
        return venue != null ? venue : "";
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getCategory() {
        return category != null ? category : "";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDepartment() {
        return department != null ? department : "";
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public String getRegistrationDeadline() {
        return registrationDeadline != null ? registrationDeadline : "";
    }

    public void setRegistrationDeadline(String registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public String getClubId() {
        return clubId != null ? clubId : "";
    }

    public void setClubId(String clubId) {
        this.clubId = clubId;
    }

    public String getClubName() {
        return clubName != null ? clubName : "";
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public String getCoordinatorId() {
        return coordinatorId != null ? coordinatorId : "";
    }

    public void setCoordinatorId(String coordinatorId) {
        this.coordinatorId = coordinatorId;
    }

    public String getStatus() {
        return status != null ? status : "pending";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason != null ? rejectionReason : "";
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getParticipationType() {
        return participationType != null ? participationType : "Individual";
    }

    public void setParticipationType(String participationType) {
        this.participationType = participationType;
    }

    public int getMinTeamSize() {
        return minTeamSize;
    }

    public void setMinTeamSize(int minTeamSize) {
        this.minTeamSize = minTeamSize;
    }

    public int getMaxTeamSize() {
        return maxTeamSize;
    }

    public void setMaxTeamSize(int maxTeamSize) {
        this.maxTeamSize = maxTeamSize;
    }

    public int getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(int currentParticipants) {
        this.currentParticipants = currentParticipants;
    }
}