package com.example.eventhiveai.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class WinnerModel {

    private String winnerId;
    private String eventId;
    private String eventName;
    private String firstPlace;
    private String secondPlace;
    private String thirdPlace;
    private String clubName;

    @ServerTimestamp
    private Date publishedAt;

    public WinnerModel() {
        // Required for Firestore
    }

    public WinnerModel(String winnerId, String eventId, String eventName,
                       String firstPlace, String secondPlace, String thirdPlace,
                       String clubName) {
        this.winnerId = winnerId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.firstPlace = firstPlace;
        this.secondPlace = secondPlace;
        this.thirdPlace = thirdPlace;
        this.clubName = clubName;
    }

    public String getWinnerId() {
        return winnerId != null ? winnerId : "";
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
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

    public String getFirstPlace() {
        return firstPlace != null ? firstPlace : "";
    }

    public void setFirstPlace(String firstPlace) {
        this.firstPlace = firstPlace;
    }

    public String getSecondPlace() {
        return secondPlace != null ? secondPlace : "";
    }

    public void setSecondPlace(String secondPlace) {
        this.secondPlace = secondPlace;
    }

    public String getThirdPlace() {
        return thirdPlace != null ? thirdPlace : "";
    }

    public void setThirdPlace(String thirdPlace) {
        this.thirdPlace = thirdPlace;
    }

    public String getClubName() {
        return clubName != null ? clubName : "";
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }
}
