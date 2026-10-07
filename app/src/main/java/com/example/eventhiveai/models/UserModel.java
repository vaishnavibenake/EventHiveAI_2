package com.example.eventhiveai.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class UserModel {
    private String uid;
    private String name;
    private String email;
    private String rollNumber;
    private String department;
    private String year;
    private String role; // "ADMIN", "COORDINATOR", "STUDENT"
    private String clubId;

    @ServerTimestamp
    private Date createdAt;

    public UserModel() {
        // Required empty constructor for Firestore
    }

    public UserModel(String uid, String name, String email, String rollNumber,
                     String department, String year, String role, String clubId) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.rollNumber = rollNumber;
        this.department = department;
        this.year = year;
        this.role = role;
        this.clubId = clubId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getClubId() {
        return clubId;
    }

    public void setClubId(String clubId) {
        this.clubId = clubId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
