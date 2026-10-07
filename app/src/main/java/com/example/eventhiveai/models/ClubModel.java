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
    private String status; // "active", "inactive"

    @ServerTimestamp
    private Date createdAt;

    public ClubModel() {
        // Empty constructor for Firestore
    }

    public ClubModel(String clubId, String name, String description, String coordinatorId,
                     String contactEmail, String contactPhone, String status) {
        this.clubId = clubId;
        this.name = name;
        this.description = description;
        this.coordinatorId = coordinatorId;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.status = status;
    }

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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
