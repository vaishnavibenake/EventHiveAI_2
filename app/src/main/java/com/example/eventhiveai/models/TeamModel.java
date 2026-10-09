package com.example.eventhiveai.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;

public class TeamModel {
    private String teamId;
    private String teamName;
    private String eventId;
    private String eventName;
    private String leaderId;
    private String leaderName;
    private List<String> memberIds;
    private List<String> memberNames;
    private List<String> memberEmails;
    private int memberCount;
    private String status; // "active", "cancelled"

    @ServerTimestamp
    private Date createdAt;

    public TeamModel() {
        // Required for Firestore
    }

    public TeamModel(String teamId, String teamName, String eventId, String eventName,
                     String leaderId, String leaderName,
                     List<String> memberIds, List<String> memberNames,
                     List<String> memberEmails, int memberCount, String status) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.eventId = eventId;
        this.eventName = eventName;
        this.leaderId = leaderId;
        this.leaderName = leaderName;
        this.memberIds = memberIds;
        this.memberNames = memberNames;
        this.memberEmails = memberEmails;
        this.memberCount = memberCount;
        this.status = status;
    }

    public String getTeamId() { return teamId != null ? teamId : ""; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName != null ? teamName : ""; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getEventId() { return eventId != null ? eventId : ""; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName != null ? eventName : ""; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getLeaderId() { return leaderId != null ? leaderId : ""; }
    public void setLeaderId(String leaderId) { this.leaderId = leaderId; }

    public String getLeaderName() { return leaderName != null ? leaderName : ""; }
    public void setLeaderName(String leaderName) { this.leaderName = leaderName; }

    public List<String> getMemberIds() { return memberIds; }
    public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }

    public List<String> getMemberNames() { return memberNames; }
    public void setMemberNames(List<String> memberNames) { this.memberNames = memberNames; }

    public List<String> getMemberEmails() { return memberEmails; }
    public void setMemberEmails(List<String> memberEmails) { this.memberEmails = memberEmails; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public String getStatus() { return status != null ? status : "active"; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
