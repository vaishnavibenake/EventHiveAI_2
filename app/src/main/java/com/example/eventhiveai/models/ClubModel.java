package com.example.eventhiveai.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class ClubModel {

    private String clubId;
    private String name;
    private String description;
    private String coordinatorId;
    private String contactEmail;
    private String contactPhone;
    private String status;

    // New fields for Club Profile
    private String goal;
    private String department;
    private String focusAreas;

    @ServerTimestamp
    private Date createdAt;


    // =========================================================
    // 1. EMPTY CONSTRUCTOR
    // Required by Firebase Firestore
    // =========================================================
    public ClubModel() {
        // Empty constructor
    }


    // =========================================================
    // 2. OLD CONSTRUCTOR
    // Keeps existing FirebaseDataHelper and old code working
    // =========================================================
    public ClubModel(String clubId,
                     String name,
                     String description,
                     String coordinatorId,
                     String contactEmail,
                     String contactPhone,
                     String status) {

        this.clubId = clubId;
        this.name = name;
        this.description = description;
        this.coordinatorId = coordinatorId;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.status = status;

        // New fields
        this.goal = "";
        this.department = "";
        this.focusAreas = "";
    }


    // =========================================================
    // 3. NEW CONSTRUCTOR
    // Includes Goal, Department and Focus Areas
    // =========================================================
    public ClubModel(String clubId,
                     String name,
                     String description,
                     String coordinatorId,
                     String contactEmail,
                     String contactPhone,
                     String status,
                     String goal,
                     String department,
                     String focusAreas) {

        this.clubId = clubId;
        this.name = name;
        this.description = description;
        this.coordinatorId = coordinatorId;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.status = status;
        this.goal = goal;
        this.department = department;
        this.focusAreas = focusAreas;
    }


    // =========================================================
    // GETTERS AND SETTERS
    // =========================================================

    public String getClubId() {
        return clubId;
    }

    public void setClubId(String clubId) {
        this.clubId = clubId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getCoordinatorId() {
        return coordinatorId;
    }

    public void setCoordinatorId(String coordinatorId) {
        this.coordinatorId = coordinatorId;
    }


    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }


    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    // =========================================================
    // NEW FIELDS
    // =========================================================

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }


    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }


    public String getFocusAreas() {
        return focusAreas;
    }

    public void setFocusAreas(String focusAreas) {
        this.focusAreas = focusAreas;
    }


    // =========================================================
    // CREATED AT
    // =========================================================

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}