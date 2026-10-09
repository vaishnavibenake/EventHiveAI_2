package com.example.eventhiveai.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;

public class WinnerModel {

    private String winnerId;
    private String eventId;
    private String eventName;
    private String clubName;
    private String participationType; // "Individual", "Pair", "Team"

    // Structured winner data
    private String firstPlaceName;
    private String firstPlaceDepartment;
    private String firstPlaceTeamName;
    private List<String> firstPlaceMembers;

    private String secondPlaceName;
    private String secondPlaceDepartment;
    private String secondPlaceTeamName;
    private List<String> secondPlaceMembers;

    private String thirdPlaceName;
    private String thirdPlaceDepartment;
    private String thirdPlaceTeamName;
    private List<String> thirdPlaceMembers;

    // Legacy flat fields for backward compatibility
    private String firstPlace;
    private String secondPlace;
    private String thirdPlace;

    private String coordinatorId;

    @ServerTimestamp
    private Date publishedAt;

    public WinnerModel() {
        // Required for Firestore
    }

    // Legacy constructor for backward compatibility
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

    // Getters and setters
    public String getWinnerId() { return winnerId != null ? winnerId : ""; }
    public void setWinnerId(String winnerId) { this.winnerId = winnerId; }

    public String getEventId() { return eventId != null ? eventId : ""; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName != null ? eventName : ""; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getClubName() { return clubName != null ? clubName : ""; }
    public void setClubName(String clubName) { this.clubName = clubName; }

    public String getParticipationType() { return participationType != null ? participationType : "Individual"; }
    public void setParticipationType(String participationType) { this.participationType = participationType; }

    // Legacy flat getters (used by EventGalleryDetailsActivity)
    public String getFirstPlace() { return firstPlace != null ? firstPlace : ""; }
    public void setFirstPlace(String firstPlace) { this.firstPlace = firstPlace; }

    public String getSecondPlace() { return secondPlace != null ? secondPlace : ""; }
    public void setSecondPlace(String secondPlace) { this.secondPlace = secondPlace; }

    public String getThirdPlace() { return thirdPlace != null ? thirdPlace : ""; }
    public void setThirdPlace(String thirdPlace) { this.thirdPlace = thirdPlace; }

    // Structured winner fields
    public String getFirstPlaceName() { return firstPlaceName != null ? firstPlaceName : ""; }
    public void setFirstPlaceName(String v) { this.firstPlaceName = v; }

    public String getFirstPlaceDepartment() { return firstPlaceDepartment != null ? firstPlaceDepartment : ""; }
    public void setFirstPlaceDepartment(String v) { this.firstPlaceDepartment = v; }

    public String getFirstPlaceTeamName() { return firstPlaceTeamName != null ? firstPlaceTeamName : ""; }
    public void setFirstPlaceTeamName(String v) { this.firstPlaceTeamName = v; }

    public List<String> getFirstPlaceMembers() { return firstPlaceMembers; }
    public void setFirstPlaceMembers(List<String> v) { this.firstPlaceMembers = v; }

    public String getSecondPlaceName() { return secondPlaceName != null ? secondPlaceName : ""; }
    public void setSecondPlaceName(String v) { this.secondPlaceName = v; }

    public String getSecondPlaceDepartment() { return secondPlaceDepartment != null ? secondPlaceDepartment : ""; }
    public void setSecondPlaceDepartment(String v) { this.secondPlaceDepartment = v; }

    public String getSecondPlaceTeamName() { return secondPlaceTeamName != null ? secondPlaceTeamName : ""; }
    public void setSecondPlaceTeamName(String v) { this.secondPlaceTeamName = v; }

    public List<String> getSecondPlaceMembers() { return secondPlaceMembers; }
    public void setSecondPlaceMembers(List<String> v) { this.secondPlaceMembers = v; }

    public String getThirdPlaceName() { return thirdPlaceName != null ? thirdPlaceName : ""; }
    public void setThirdPlaceName(String v) { this.thirdPlaceName = v; }

    public String getThirdPlaceDepartment() { return thirdPlaceDepartment != null ? thirdPlaceDepartment : ""; }
    public void setThirdPlaceDepartment(String v) { this.thirdPlaceDepartment = v; }

    public String getThirdPlaceTeamName() { return thirdPlaceTeamName != null ? thirdPlaceTeamName : ""; }
    public void setThirdPlaceTeamName(String v) { this.thirdPlaceTeamName = v; }

    public List<String> getThirdPlaceMembers() { return thirdPlaceMembers; }
    public void setThirdPlaceMembers(List<String> v) { this.thirdPlaceMembers = v; }

    public String getCoordinatorId() { return coordinatorId != null ? coordinatorId : ""; }
    public void setCoordinatorId(String coordinatorId) { this.coordinatorId = coordinatorId; }

    public Date getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Date publishedAt) { this.publishedAt = publishedAt; }
}
