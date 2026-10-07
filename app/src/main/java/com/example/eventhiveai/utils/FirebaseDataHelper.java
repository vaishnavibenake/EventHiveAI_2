package com.example.eventhiveai.utils;

import android.content.Context;
import android.util.Log;

import com.example.eventhiveai.admin.EventModel;
import com.example.eventhiveai.models.ClubModel;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseDataHelper {

    private static final String TAG = "FirebaseDataHelper";

    /**
     * Seeds initial clubs, events, registrations (attendance), winners, notifications, and users
     * into Firestore if the database is unpopulated or missing data.
     */
    public static void initializeAllSampleDataIfEmpty(Context context) {
        Log.d(TAG, "Automatic sample-data seeding is disabled; configure trusted Firebase profiles and create production data through the app.");
    }

    /**
     * Backward-compatible alias for existing callers.
     */
    public static void initializeSampleClubsIfEmpty(Context context) {
        initializeAllSampleDataIfEmpty(context);
    }

    private static void seedSampleClubs(FirebaseFirestore db) {
        List<ClubModel> clubs = new ArrayList<>();
        clubs.add(new ClubModel("club_coding", "Coding & AI Club",
                "Dedicated to algorithmic problem solving, hackathons, and AI/ML workshops.",
                "coord_coding", "codingclub@college.edu", "9876543210", "active"));

        clubs.add(new ClubModel("club_robotics", "Robotics & IoT Club",
                "Building autonomous robots, drone technology, and embedded systems.",
                "coord_robotics", "robotics@college.edu", "9876543211", "active"));

        clubs.add(new ClubModel("club_cultural", "Cultural Arts Club",
                "Nurturing dance, music, theatrical drama, and literature talents.",
                "coord_cultural", "cultural@college.edu", "9876543212", "active"));

        clubs.add(new ClubModel("club_sports", "College Sports Council",
                "Organizing annual athletic meets, football, cricket, and indoor games.",
                "coord_sports", "sports@college.edu", "9876543213", "active"));

        clubs.add(new ClubModel("club_acm", "ACM Student Chapter",
                "International computing society for seminars and competitive programming.",
                "coord_acm", "acm@college.edu", "9876543214", "active"));

        for (ClubModel club : clubs) {
            Map<String, Object> data = new HashMap<>();
            data.put("clubId", club.getClubId());
            data.put("name", club.getName());
            data.put("description", club.getDescription());
            data.put("coordinatorId", club.getCoordinatorId());
            data.put("contactEmail", club.getContactEmail());
            data.put("contactPhone", club.getContactPhone());
            data.put("status", club.getStatus());
            data.put("createdAt", FieldValue.serverTimestamp());

            db.collection("clubs").document(club.getClubId()).set(data);
        }
    }

    private static void seedSampleEvents(FirebaseFirestore db) {
        // Event 1: AI Hackathon (Approved, upcoming)
        Map<String, Object> e1 = new HashMap<>();
        e1.put("eventId", "event_hackathon_2026");
        e1.put("eventName", "AI & Machine Learning Hackathon 2026");
        e1.put("clubName", "Coding & AI Club");
        e1.put("clubId", "club_coding");
        e1.put("description", "24-hour campus hackathon building Generative AI and autonomous agent solutions. Cash prizes, industry mentors, and internship opportunities for top teams.");
        e1.put("date", "2026-10-25");
        e1.put("startTime", "09:00 AM");
        e1.put("endTime", "05:00 PM");
        e1.put("venue", "Main Auditorium & Computing Lab 1");
        e1.put("category", "Hackathon");
        e1.put("department", "CSE");
        e1.put("budget", 25000.0);
        e1.put("maxParticipants", 100);
        e1.put("registrationDeadline", "2026-10-24");
        e1.put("coordinatorId", "coord_coding");
        e1.put("status", "approved");
        e1.put("rejectionReason", "");
        e1.put("createdAt", FieldValue.serverTimestamp());
        db.collection("events").document("event_hackathon_2026").set(e1);

        // Event 2: Robotics Drone Racing (Approved, upcoming)
        Map<String, Object> e2 = new HashMap<>();
        e2.put("eventId", "event_drone_race");
        e2.put("eventName", "Robotics Drone Racing & Obstacle Expo");
        e2.put("clubName", "Robotics & IoT Club");
        e2.put("clubId", "club_robotics");
        e2.put("description", "High-speed FPV drone obstacle course and autonomous line-follower showcase. Open to all engineering departments.");
        e2.put("date", "2026-10-28");
        e2.put("startTime", "10:00 AM");
        e2.put("endTime", "04:00 PM");
        e2.put("venue", "Campus Open Grounds");
        e2.put("category", "Technical");
        e2.put("department", "E&TC");
        e2.put("budget", 30000.0);
        e2.put("maxParticipants", 80);
        e2.put("registrationDeadline", "2026-10-27");
        e2.put("coordinatorId", "coord_robotics");
        e2.put("status", "approved");
        e2.put("rejectionReason", "");
        e2.put("createdAt", FieldValue.serverTimestamp());
        db.collection("events").document("event_drone_race").set(e2);

        // Event 3: Cultural Fest Symphony 2026 (Approved, upcoming)
        Map<String, Object> e3 = new HashMap<>();
        e3.put("eventId", "event_cultural_fest");
        e3.put("eventName", "Inter-College Cultural Fest - Symphony 2026");
        e3.put("clubName", "Cultural Arts Club");
        e3.put("clubId", "club_cultural");
        e3.put("description", "Annual mega cultural extravaganza featuring battle of the bands, classical & western dance, street play, and fine arts exhibition.");
        e3.put("date", "2026-11-05");
        e3.put("startTime", "05:00 PM");
        e3.put("endTime", "10:00 PM");
        e3.put("venue", "College Open Amphitheatre");
        e3.put("category", "Cultural");
        e3.put("department", "All Departments");
        e3.put("budget", 65000.0);
        e3.put("maxParticipants", 350);
        e3.put("registrationDeadline", "2026-11-04");
        e3.put("coordinatorId", "coord_cultural");
        e3.put("status", "approved");
        e3.put("rejectionReason", "");
        e3.put("createdAt", FieldValue.serverTimestamp());
        db.collection("events").document("event_cultural_fest").set(e3);

        // Event 4: Annual Sports Championship (Approved, upcoming)
        Map<String, Object> e4 = new HashMap<>();
        e4.put("eventId", "event_sports_meet");
        e4.put("eventName", "Annual Inter-Department Sports Championship");
        e4.put("clubName", "College Sports Council");
        e4.put("clubId", "club_sports");
        e4.put("description", "Inter-department football, cricket, badminton, and track & field tournaments. Trophies and certificates for all department champions.");
        e4.put("date", "2026-11-12");
        e4.put("startTime", "08:00 AM");
        e4.put("endTime", "06:00 PM");
        e4.put("venue", "College Sports Complex");
        e4.put("category", "Sports");
        e4.put("department", "All Departments");
        e4.put("budget", 40000.0);
        e4.put("maxParticipants", 250);
        e4.put("registrationDeadline", "2026-11-10");
        e4.put("coordinatorId", "coord_sports");
        e4.put("status", "approved");
        e4.put("rejectionReason", "");
        e4.put("createdAt", FieldValue.serverTimestamp());
        db.collection("events").document("event_sports_meet").set(e4);

        // Event 5: Pending proposal 1 for Faculty Admin review
        Map<String, Object> e5 = new HashMap<>();
        e5.put("eventId", "event_cloud_bootcamp");
        e5.put("eventName", "Cloud Computing & AWS DevOps Masterclass");
        e5.put("clubName", "Coding & AI Club");
        e5.put("clubId", "club_coding");
        e5.put("description", "Hands-on weekend bootcamp covering Docker containers, Kubernetes orchestration, and AWS EC2/S3 cloud deployment pipelines.");
        e5.put("date", "2026-11-18");
        e5.put("startTime", "11:00 AM");
        e5.put("endTime", "03:00 PM");
        e5.put("venue", "Seminar Hall 1");
        e5.put("category", "Workshop");
        e5.put("department", "Information Technology");
        e5.put("budget", 15000.0);
        e5.put("maxParticipants", 60);
        e5.put("registrationDeadline", "2026-11-17");
        e5.put("coordinatorId", "coord_coding");
        e5.put("status", "pending");
        e5.put("rejectionReason", "");
        e5.put("createdAt", FieldValue.serverTimestamp());
        db.collection("events").document("event_cloud_bootcamp").set(e5);

        // Event 6: Pending proposal 2 for Faculty Admin review
        Map<String, Object> e6 = new HashMap<>();
        e6.put("eventId", "event_cyber_ctf");
        e6.put("eventName", "Capture The Flag: Cyber Defense Challenge");
        e6.put("clubName", "ACM Student Chapter");
        e6.put("clubId", "club_acm");
        e6.put("description", "Competitive ethical hacking, web vulnerabilities exploitation, cryptography puzzles, and reverse engineering challenge.");
        e6.put("date", "2026-11-22");
        e6.put("startTime", "01:00 PM");
        e6.put("endTime", "06:00 PM");
        e6.put("venue", "Computer Lab 3");
        e6.put("category", "Technical");
        e6.put("department", "CSE");
        e6.put("budget", 12000.0);
        e6.put("maxParticipants", 50);
        e6.put("registrationDeadline", "2026-11-21");
        e6.put("coordinatorId", "coord_acm");
        e6.put("status", "pending");
        e6.put("rejectionReason", "");
        e6.put("createdAt", FieldValue.serverTimestamp());
        db.collection("events").document("event_cyber_ctf").set(e6);

        // Event 7: Completed event with winners
        Map<String, Object> e7 = new HashMap<>();
        e7.put("eventId", "event_code_quest_past");
        e7.put("eventName", "CodeQuest Algorithm Challenge (Spring 2026)");
        e7.put("clubName", "Coding & AI Club");
        e7.put("clubId", "club_coding");
        e7.put("description", "Speed algorithm contest solving dynamic programming and graph theory problems under timed constraints.");
        e7.put("date", "2026-09-15");
        e7.put("startTime", "10:00 AM");
        e7.put("endTime", "01:00 PM");
        e7.put("venue", "Computer Lab 1");
        e7.put("category", "Technical");
        e7.put("department", "CSE");
        e7.put("budget", 10000.0);
        e7.put("maxParticipants", 80);
        e7.put("registrationDeadline", "2026-09-14");
        e7.put("coordinatorId", "coord_coding");
        e7.put("status", "completed");
        e7.put("rejectionReason", "");
        e7.put("createdAt", FieldValue.serverTimestamp());
        db.collection("events").document("event_code_quest_past").set(e7);
    }

    private static void seedSampleRegistrations(FirebaseFirestore db) {
        // Attendance records: mixing 'attended' (Present) and 'registered' (Awaiting verification)
        addRegistrationDoc(db, "reg_1", "event_hackathon_2026", "AI & Machine Learning Hackathon 2026",
                "2026-10-25", "Main Auditorium & Computing Lab 1", "std_aarav", "Aarav Sharma", "aarav.sharma@college.edu", "attended");

        addRegistrationDoc(db, "reg_2", "event_hackathon_2026", "AI & Machine Learning Hackathon 2026",
                "2026-10-25", "Main Auditorium & Computing Lab 1", "std_priya", "Priya Patel", "priya.patel@college.edu", "registered");

        addRegistrationDoc(db, "reg_3", "event_drone_race", "Robotics Drone Racing & Obstacle Expo",
                "2026-10-28", "Campus Open Grounds", "std_rohan", "Rohan Kulkarni", "rohan.kulkarni@college.edu", "attended");

        addRegistrationDoc(db, "reg_4", "event_cultural_fest", "Inter-College Cultural Fest - Symphony 2026",
                "2026-11-05", "College Open Amphitheatre", "std_sneha", "Sneha Deshmukh", "sneha.deshmukh@college.edu", "registered");

        addRegistrationDoc(db, "reg_5", "event_sports_meet", "Annual Inter-Department Sports Championship",
                "2026-11-12", "College Sports Complex", "std_aditya", "Aditya Verma", "aditya.verma@college.edu", "attended");

        addRegistrationDoc(db, "reg_6", "event_code_quest_past", "CodeQuest Algorithm Challenge (Spring 2026)",
                "2026-09-15", "Computer Lab 1", "std_neha", "Neha Joshi", "neha.joshi@college.edu", "attended");
    }

    private static void addRegistrationDoc(FirebaseFirestore db, String id, String eventId, String eventName,
                                          String eventDate, String venue, String studentId, String studentName,
                                          String studentEmail, String status) {
        Map<String, Object> r = new HashMap<>();
        r.put("registrationId", id);
        r.put("eventId", eventId);
        r.put("eventName", eventName);
        r.put("eventDate", eventDate);
        r.put("eventVenue", venue);
        r.put("studentId", studentId);
        r.put("studentName", studentName);
        r.put("studentEmail", studentEmail);
        r.put("status", status); // "attended" or "registered"
        r.put("registeredAt", FieldValue.serverTimestamp());
        db.collection("registrations").document(id).set(r);
    }

    private static void seedSampleWinners(FirebaseFirestore db) {
        Map<String, Object> w1 = new HashMap<>();
        w1.put("winnerId", "win_codequest_2026");
        w1.put("eventId", "event_code_quest_past");
        w1.put("eventName", "CodeQuest Algorithm Challenge (Spring 2026)");
        w1.put("clubName", "Coding & AI Club");
        w1.put("firstPlace", "🥇 Aarav Sharma (CSE - 3rd Year)");
        w1.put("secondPlace", "🥈 Vikram Singh (IT - 2nd Year)");
        w1.put("thirdPlace", "🥉 Ananya Rao (AI & ML - 3rd Year)");
        w1.put("publishedAt", FieldValue.serverTimestamp());
        db.collection("winners").document("win_codequest_2026").set(w1);

        Map<String, Object> w2 = new HashMap<>();
        w2.put("winnerId", "win_drone_expo");
        w2.put("eventId", "event_drone_race");
        w2.put("eventName", "Robotics Drone Racing & Obstacle Expo");
        w2.put("clubName", "Robotics & IoT Club");
        w2.put("firstPlace", "🥇 Aerial Hawks (Tanmay Patil & Dev K)");
        w2.put("secondPlace", "🥈 SkyForce (Manish Rao)");
        w2.put("thirdPlace", "🥉 AeroBotics (Siddharth Nair)");
        w2.put("publishedAt", FieldValue.serverTimestamp());
        db.collection("winners").document("win_drone_expo").set(w2);

        Map<String, Object> w3 = new HashMap<>();
        w3.put("winnerId", "win_hackathon_pre");
        w3.put("eventId", "event_hackathon_2026");
        w3.put("eventName", "AI & Machine Learning Hackathon");
        w3.put("clubName", "Coding & AI Club");
        w3.put("firstPlace", "🥇 Team ByteMasters (Priya Patel & Rohan K)");
        w3.put("secondPlace", "🥈 Team NeuralNet (Sneha Deshmukh)");
        w3.put("thirdPlace", "🥉 Team AlgoX (Karan Joshi & Dev)");
        w3.put("publishedAt", FieldValue.serverTimestamp());
        db.collection("winners").document("win_hackathon_pre").set(w3);
    }

    private static void seedSampleNotifications(FirebaseFirestore db) {
        Map<String, Object> n1 = new HashMap<>();
        n1.put("notificationId", "notif_1");
        n1.put("title", "🏆 Winners Announced: CodeQuest Algorithm Challenge");
        n1.put("message", "Congratulations to Aarav Sharma, Vikram Singh, and Ananya Rao for their stellar performance!");
        n1.put("recipientRole", "ALL");
        n1.put("timestamp", FieldValue.serverTimestamp());
        db.collection("notifications").document("notif_1").set(n1);

        Map<String, Object> n2 = new HashMap<>();
        n2.put("notificationId", "notif_2");
        n2.put("title", "📢 Registrations Open: AI & ML Hackathon 2026");
        n2.put("message", "Campus registrations are officially open for the 24-hr Hackathon happening on Oct 25th in Main Auditorium.");
        n2.put("recipientRole", "ALL");
        n2.put("timestamp", FieldValue.serverTimestamp());
        db.collection("notifications").document("notif_2").set(n2);

        Map<String, Object> n3 = new HashMap<>();
        n3.put("notificationId", "notif_3");
        n3.put("title", "🎉 Symphony Cultural Fest Passes Available");
        n3.put("message", "Register early to secure passes for the annual inter-college music and dance competitions.");
        n3.put("recipientRole", "STUDENT");
        n3.put("timestamp", FieldValue.serverTimestamp());
        db.collection("notifications").document("notif_3").set(n3);
    }

    private static void seedSampleUsers(FirebaseFirestore db) {
        // Coordinator 1: Coding Club
        Map<String, Object> u1 = new HashMap<>();
        u1.put("name", "Prof. Rajesh Sharma");
        u1.put("email", "codingclub@college.edu");
        u1.put("department", "CSE");
        u1.put("role", "COORDINATOR");
        u1.put("clubId", "Coding & AI Club");
        u1.put("createdAt", FieldValue.serverTimestamp());
        db.collection("users").document("coord_coding").set(u1);

        // Coordinator 2: Robotics Club
        Map<String, Object> u2 = new HashMap<>();
        u2.put("name", "Prof. Sunita Deshpande");
        u2.put("email", "robotics@college.edu");
        u2.put("department", "E&TC");
        u2.put("role", "COORDINATOR");
        u2.put("clubId", "Robotics & IoT Club");
        u2.put("createdAt", FieldValue.serverTimestamp());
        db.collection("users").document("coord_robotics").set(u2);

        // Coordinator 3: Cultural Club
        Map<String, Object> u3 = new HashMap<>();
        u3.put("name", "Prof. Meera Joshi");
        u3.put("email", "cultural@college.edu");
        u3.put("department", "Humanities");
        u3.put("role", "COORDINATOR");
        u3.put("clubId", "Cultural Arts Club");
        u3.put("createdAt", FieldValue.serverTimestamp());
        db.collection("users").document("coord_cultural").set(u3);
    }

    /**
     * Detects venue conflicts:
     * Counts pairs of events that share the same venue and the same date.
     */
    public static int countVenueConflicts(List<EventModel> events) {
        if (events == null || events.size() < 2) {
            return 0;
        }

        int conflicts = 0;
        for (int i = 0; i < events.size(); i++) {
            EventModel e1 = events.get(i);
            if ("rejected".equalsIgnoreCase(e1.getStatus()) || "cancelled".equalsIgnoreCase(e1.getStatus())) {
                continue;
            }

            for (int j = i + 1; j < events.size(); j++) {
                EventModel e2 = events.get(j);
                if ("rejected".equalsIgnoreCase(e2.getStatus()) || "cancelled".equalsIgnoreCase(e2.getStatus())) {
                    continue;
                }

                boolean sameVenue = !e1.getVenue().isEmpty() &&
                        e1.getVenue().trim().equalsIgnoreCase(e2.getVenue().trim());
                boolean sameDate = !e1.getDate().isEmpty() &&
                        e1.getDate().trim().equalsIgnoreCase(e2.getDate().trim());

                if (sameVenue && sameDate) {
                    conflicts++;
                }
            }
        }
        return conflicts;
    }

    public static List<ClubModel> getDefaultClubs() {
        List<ClubModel> clubs = new ArrayList<>();
        clubs.add(new ClubModel("club_coding", "Coding & AI Club",
                "Dedicated to algorithmic problem solving, hackathons, and AI/ML workshops.",
                "coord_coding", "codingclub@college.edu", "9876543210", "active"));
        clubs.add(new ClubModel("club_robotics", "Robotics & IoT Club",
                "Building autonomous robots, drone technology, and embedded systems.",
                "coord_robotics", "robotics@college.edu", "9876543211", "active"));
        clubs.add(new ClubModel("club_cultural", "Cultural Arts Club",
                "Nurturing dance, music, theatrical drama, and literature talents.",
                "coord_cultural", "cultural@college.edu", "9876543212", "active"));
        clubs.add(new ClubModel("club_sports", "College Sports Council",
                "Organizing annual athletic meets, football, cricket, and indoor games.",
                "coord_sports", "sports@college.edu", "9876543213", "active"));
        clubs.add(new ClubModel("club_acm", "ACM Student Chapter",
                "International computing society for seminars and competitive programming.",
                "coord_acm", "acm@college.edu", "9876543214", "active"));
        return clubs;
    }

    public static List<EventModel> getDefaultEvents() {
        List<EventModel> events = new ArrayList<>();
        events.add(new EventModel("event_hackathon_2026", "AI & Machine Learning Hackathon 2026",
                "24-hour campus hackathon building Generative AI and autonomous agent solutions. Cash prizes, industry mentors, and internship opportunities for top teams.",
                "2026-10-25", "09:00 AM", "05:00 PM", "Main Auditorium & Computing Lab 1", "Hackathon", "CSE",
                25000.0, 100, "2026-10-24", "club_coding", "Coding & AI Club", "coord_coding", "approved", ""));

        events.add(new EventModel("event_drone_race", "Robotics Drone Racing & Obstacle Expo",
                "High-speed FPV drone obstacle course and autonomous line-follower showcase. Open to all engineering departments.",
                "2026-10-28", "10:00 AM", "04:00 PM", "Campus Open Grounds", "Technical", "E&TC",
                30000.0, 80, "2026-10-27", "club_robotics", "Robotics & IoT Club", "coord_robotics", "approved", ""));

        events.add(new EventModel("event_cultural_fest", "Inter-College Cultural Fest - Symphony 2026",
                "Annual mega cultural extravaganza featuring battle of the bands, classical & western dance, street play, and fine arts exhibition.",
                "2026-11-05", "05:00 PM", "10:00 PM", "College Open Amphitheatre", "Cultural", "All Departments",
                65000.0, 350, "2026-11-04", "club_cultural", "Cultural Arts Club", "coord_cultural", "approved", ""));

        events.add(new EventModel("event_sports_meet", "Annual Inter-Department Sports Championship",
                "Inter-department football, cricket, badminton, and track & field tournaments. Trophies and certificates for all department champions.",
                "2026-11-12", "08:00 AM", "06:00 PM", "College Sports Complex", "Sports", "All Departments",
                40000.0, 250, "2026-11-10", "club_sports", "College Sports Council", "coord_sports", "approved", ""));

        events.add(new EventModel("event_cloud_bootcamp", "Cloud Computing & AWS DevOps Masterclass",
                "Hands-on weekend bootcamp covering Docker containers, Kubernetes orchestration, and AWS EC2/S3 cloud deployment pipelines.",
                "2026-11-18", "11:00 AM", "03:00 PM", "Seminar Hall 1", "Workshop", "Information Technology",
                15000.0, 60, "2026-11-17", "club_coding", "Coding & AI Club", "coord_coding", "pending", ""));

        events.add(new EventModel("event_cyber_ctf", "Capture The Flag: Cyber Defense Challenge",
                "Competitive ethical hacking, web vulnerabilities exploitation, cryptography puzzles, and reverse engineering challenge.",
                "2026-11-22", "01:00 PM", "06:00 PM", "Computer Lab 3", "Technical", "CSE",
                12000.0, 50, "2026-11-21", "club_acm", "ACM Student Chapter", "coord_acm", "pending", ""));

        events.add(new EventModel("event_code_quest_past", "CodeQuest Algorithm Challenge (Spring 2026)",
                "Speed algorithm contest solving dynamic programming and graph theory problems under timed constraints.",
                "2026-09-15", "10:00 AM", "01:00 PM", "Computer Lab 1", "Technical", "CSE",
                10000.0, 80, "2026-09-14", "club_coding", "Coding & AI Club", "coord_coding", "completed", ""));

        return events;
    }

    public static EventModel getDefaultEventById(String eventId) {
        for (EventModel e : getDefaultEvents()) {
            if (e.getEventId().equalsIgnoreCase(eventId)) {
                return e;
            }
        }
        return getDefaultEvents().get(0);
    }
    public static List<com.example.eventhiveai.models.RegistrationModel> getDefaultRegistrations() {
        List<com.example.eventhiveai.models.RegistrationModel> list = new ArrayList<>();
        list.add(new com.example.eventhiveai.models.RegistrationModel("reg_1", "event_hackathon_2026",
                "AI & Machine Learning Hackathon 2026", "2026-10-25", "Main Auditorium & Computing Lab 1",
                "std_aarav", "Aarav Sharma", "aarav.sharma@college.edu", "attended"));
        list.add(new com.example.eventhiveai.models.RegistrationModel("reg_2", "event_hackathon_2026",
                "AI & Machine Learning Hackathon 2026", "2026-10-25", "Main Auditorium & Computing Lab 1",
                "std_priya", "Priya Patel", "priya.patel@college.edu", "registered"));
        list.add(new com.example.eventhiveai.models.RegistrationModel("reg_3", "event_drone_race",
                "Robotics Drone Racing & Obstacle Expo", "2026-10-28", "Campus Open Grounds",
                "std_rohan", "Rohan Kulkarni", "rohan.kulkarni@college.edu", "attended"));
        list.add(new com.example.eventhiveai.models.RegistrationModel("reg_4", "event_cultural_fest",
                "Inter-College Cultural Fest - Symphony 2026", "2026-11-05", "College Open Amphitheatre",
                "std_sneha", "Sneha Deshmukh", "sneha.deshmukh@college.edu", "registered"));
        list.add(new com.example.eventhiveai.models.RegistrationModel("reg_5", "event_sports_meet",
                "Annual Inter-Department Sports Championship", "2026-11-12", "College Sports Complex",
                "std_aditya", "Aditya Verma", "aditya.verma@college.edu", "attended"));
        list.add(new com.example.eventhiveai.models.RegistrationModel("reg_6", "event_code_quest_past",
                "CodeQuest Algorithm Challenge (Spring 2026)", "2026-09-15", "Computer Lab 1",
                "std_neha", "Neha Joshi", "neha.joshi@college.edu", "attended"));
        return list;
    }

    public static List<com.example.eventhiveai.models.WinnerModel> getDefaultWinners() {
        List<com.example.eventhiveai.models.WinnerModel> list = new ArrayList<>();
        list.add(new com.example.eventhiveai.models.WinnerModel("win_codequest_2026", "event_code_quest_past",
                "CodeQuest Algorithm Challenge (Spring 2026)", "🥇 Aarav Sharma (CSE - 3rd Year)",
                "🥈 Vikram Singh (IT - 2nd Year)", "🥉 Ananya Rao (AI & ML - 3rd Year)", "Coding & AI Club"));
        list.add(new com.example.eventhiveai.models.WinnerModel("win_drone_expo", "event_drone_race",
                "Robotics Drone Racing & Obstacle Expo", "🥇 Aerial Hawks (Tanmay Patil & Dev K)",
                "🥈 SkyForce (Manish Rao)", "🥉 AeroBotics (Siddharth Nair)", "Robotics & IoT Club"));
        list.add(new com.example.eventhiveai.models.WinnerModel("win_hackathon_pre", "event_hackathon_2026",
                "AI & Machine Learning Hackathon", "🥇 Team ByteMasters (Priya Patel & Rohan K)",
                "🥈 Team NeuralNet (Sneha Deshmukh)", "🥉 Team AlgoX (Karan Joshi & Dev)", "Coding & AI Club"));
        return list;
    }

    public static List<com.example.eventhiveai.models.NotificationModel> getDefaultNotifications() {
        List<com.example.eventhiveai.models.NotificationModel> list = new ArrayList<>();
        list.add(new com.example.eventhiveai.models.NotificationModel("notif_1",
                "🏆 Winners Announced: CodeQuest Algorithm Challenge",
                "Congratulations to Aarav Sharma, Vikram Singh, and Ananya Rao for their stellar performance!",
                "ALL", ""));
        list.add(new com.example.eventhiveai.models.NotificationModel("notif_2",
                "📢 Registrations Open: AI & ML Hackathon 2026",
                "Campus registrations are officially open for the 24-hr Hackathon happening on Oct 25th in Main Auditorium.",
                "ALL", ""));
        list.add(new com.example.eventhiveai.models.NotificationModel("notif_3",
                "🎉 Symphony Cultural Fest Passes Available",
                "Register early to secure passes for the annual inter-college music and dance competitions.",
                "STUDENT", ""));
        return list;
    }

    public static List<com.example.eventhiveai.models.UserModel> getDefaultUsers() {
        List<com.example.eventhiveai.models.UserModel> list = new ArrayList<>();
        list.add(new com.example.eventhiveai.models.UserModel("coord_coding", "Prof. Rajesh Sharma",
                "codingclub@college.edu", "FAC-01", "CSE", "Faculty", "COORDINATOR", "Coding & AI Club"));
        list.add(new com.example.eventhiveai.models.UserModel("coord_robotics", "Prof. Sunita Deshpande",
                "robotics@college.edu", "FAC-02", "E&TC", "Faculty", "COORDINATOR", "Robotics & IoT Club"));
        list.add(new com.example.eventhiveai.models.UserModel("coord_cultural", "Prof. Meera Joshi",
                "cultural@college.edu", "FAC-03", "Humanities", "Faculty", "COORDINATOR", "Cultural Arts Club"));
        return list;
    }
}
