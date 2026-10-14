package com.example.eventhiveai.student;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.EventDetailsActivity;
import com.example.eventhiveai.R;
import com.example.eventhiveai.admin.EventModel;
import com.example.eventhiveai.models.ChatMessageModel;
import com.example.eventhiveai.models.ParticipationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatbotActivity extends AppCompatActivity {

    private static final String TAG = "ChatbotActivity";

    private ImageButton btnBack;
    private RecyclerView recyclerChat;
    private LinearLayout layoutTypingIndicator;
    private TextView tvTypingText;
    private EditText etChatMessage;
    private ImageButton btnSendMessage;

    private TextView chipPromptAvailable;
    private TextView chipPromptMatch;
    private TextView chipPromptDeadline;
    private TextView chipPromptStatus;

    private List<ChatMessageModel> messageList;
    private ChatMessageAdapter chatAdapter;
    private Handler mainHandler;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // Real Live Context from Firestore
    private final List<EventModel> approvedEvents = new ArrayList<>();
    private final List<ParticipationModel> studentParticipations = new ArrayList<>();
    private boolean isContextLoaded = false;
    private boolean isLoadingContext = false;
    private String contextError = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        mainHandler = new Handler(Looper.getMainLooper());

        btnBack = findViewById(R.id.btnBack);
        recyclerChat = findViewById(R.id.recyclerChat);
        layoutTypingIndicator = findViewById(R.id.layoutTypingIndicator);
        tvTypingText = findViewById(R.id.tvTypingText);
        etChatMessage = findViewById(R.id.etChatMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        chipPromptAvailable = findViewById(R.id.chipPromptAvailable);
        chipPromptMatch = findViewById(R.id.chipPromptMatch);
        chipPromptDeadline = findViewById(R.id.chipPromptDeadline);
        chipPromptStatus = findViewById(R.id.chipPromptStatus);

        setupRecyclerView();
        setupClickListeners();
        addWelcomeMessage();
        loadLiveContext();
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        chatAdapter = new ChatMessageAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerChat.setLayoutManager(layoutManager);
        recyclerChat.setAdapter(chatAdapter);

        recyclerChat.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom && !messageList.isEmpty()) {
                recyclerChat.post(() -> recyclerChat.scrollToPosition(messageList.size() - 1));
            }
        });
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // On-screen Send button
        if (btnSendMessage != null) {
            btnSendMessage.setOnClickListener(v -> handleUserSubmit());
        }

        // Soft Keyboard IME Send Action
        if (etChatMessage != null) {
            etChatMessage.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                    handleUserSubmit();
                    return true;
                }
                return false;
            });

            etChatMessage.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && !messageList.isEmpty()) {
                    recyclerChat.postDelayed(() -> recyclerChat.scrollToPosition(messageList.size() - 1), 200);
                }
            });

            etChatMessage.setOnClickListener(v -> {
                if (!messageList.isEmpty()) {
                    recyclerChat.postDelayed(() -> recyclerChat.scrollToPosition(messageList.size() - 1), 200);
                }
            });
        }

        // Quick Prompt Chips
        chipPromptAvailable.setOnClickListener(v -> sendUserMessage("What events are available?"));
        chipPromptMatch.setOnClickListener(v -> sendUserMessage("Which events match CSE?"));
        chipPromptDeadline.setOnClickListener(v -> sendUserMessage("When is registration deadline?"));
        chipPromptStatus.setOnClickListener(v -> sendUserMessage("Have I registered?"));
    }

    private void handleUserSubmit() {
        String text = etChatMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        etChatMessage.setText("");
        sendUserMessage(text);
    }

    private void addWelcomeMessage() {
        String welcome = "Hello! I am your EventHive AI Assistant 🤖.\nAsk me about upcoming campus events, eligibility, deadlines, or your registered passes!";
        messageList.add(new ChatMessageModel(welcome, false));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
    }

    private void loadLiveContext() {
        if (isLoadingContext) return;
        isLoadingContext = true;
        contextError = null;

        if (tvTypingText != null) tvTypingText.setText("Syncing live campus records...");
        layoutTypingIndicator.setVisibility(View.VISIBLE);

        // Query 1: Approved Events from /events
        db.collection("events").whereEqualTo("status", "approved").get()
                .addOnSuccessListener(eventSnapshots -> {
                    if (isFinishing() || isDestroyed()) return;

                    approvedEvents.clear();
                    Log.d(TAG, "Approved events query snapshot size: " + eventSnapshots.size());

                    for (QueryDocumentSnapshot doc : eventSnapshots) {
                        EventModel event = doc.toObject(EventModel.class);
                        if (event == null) event = new EventModel();
                        event.setEventId(doc.getId());

                        if (doc.contains("eventName") && doc.getString("eventName") != null) event.setEventName(doc.getString("eventName"));
                        if (doc.contains("clubName") && doc.getString("clubName") != null) event.setClubName(doc.getString("clubName"));

                        String dateVal = doc.contains("date") ? doc.getString("date") : doc.getString("eventDate");
                        if (dateVal != null) event.setDate(dateVal);

                        if (doc.contains("venue") && doc.getString("venue") != null) event.setVenue(doc.getString("venue"));
                        if (doc.contains("department") && doc.getString("department") != null) event.setDepartment(doc.getString("department"));
                        if (doc.contains("category") && doc.getString("category") != null) event.setCategory(doc.getString("category"));
                        if (doc.contains("participationType") && doc.getString("participationType") != null) event.setParticipationType(doc.getString("participationType"));
                        if (doc.contains("registrationDeadline") && doc.getString("registrationDeadline") != null) event.setRegistrationDeadline(doc.getString("registrationDeadline"));
                        if (doc.contains("description") && doc.getString("description") != null) event.setDescription(doc.getString("description"));

                        approvedEvents.add(event);
                        Log.d(TAG, "Fetched event doc ID=" + doc.getId() + ", name=" + event.getEventName() + ", status=" + doc.getString("status") + ", date=" + event.getDate());
                    }

                    Log.d(TAG, "Total approved events parsed: " + approvedEvents.size());

                    // Query 2: Signed-in student's participations
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        db.collection("participations")
                                .whereEqualTo("studentId", user.getUid())
                                .get()
                                .addOnSuccessListener(partSnapshots -> {
                                    if (isFinishing() || isDestroyed()) return;

                                    studentParticipations.clear();
                                    for (QueryDocumentSnapshot doc : partSnapshots) {
                                        ParticipationModel p = doc.toObject(ParticipationModel.class);
                                        if (p == null) p = new ParticipationModel();
                                        p.setParticipationId(doc.getId());
                                        studentParticipations.add(p);
                                    }

                                    isContextLoaded = true;
                                    isLoadingContext = false;
                                    layoutTypingIndicator.setVisibility(View.GONE);
                                    Log.d(TAG, "Loaded " + studentParticipations.size() + " student participations");
                                })
                                .addOnFailureListener(e -> {
                                    if (isFinishing() || isDestroyed()) return;
                                    isContextLoaded = true;
                                    isLoadingContext = false;
                                    layoutTypingIndicator.setVisibility(View.GONE);
                                    Log.e(TAG, "Error loading participations: " + e.getMessage(), e);
                                });
                    } else {
                        isContextLoaded = true;
                        isLoadingContext = false;
                        layoutTypingIndicator.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) return;
                    isLoadingContext = false;
                    contextError = e.getMessage();
                    layoutTypingIndicator.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading approved events from Firestore: " + e.getMessage(), e);
                });
    }

    private void sendUserMessage(String query) {
        if (TextUtils.isEmpty(query.trim())) return;

        // Add User Message
        messageList.add(new ChatMessageModel(query, true));
        int userPos = messageList.size() - 1;
        chatAdapter.notifyItemInserted(userPos);
        recyclerChat.smoothScrollToPosition(userPos);

        if (tvTypingText != null) tvTypingText.setText("AI is processing response...");
        layoutTypingIndicator.setVisibility(View.VISIBLE);

        mainHandler.postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            layoutTypingIndicator.setVisibility(View.GONE);

            CharSequence answer = generateGroundedAnswer(query);
            messageList.add(new ChatMessageModel(answer, false));

            int aiPos = messageList.size() - 1;
            chatAdapter.notifyItemInserted(aiPos);
            recyclerChat.smoothScrollToPosition(aiPos);
        }, 500);
    }

    private CharSequence generateGroundedAnswer(String rawQuery) {
        if (contextError != null) {
            Log.e(TAG, "generateGroundedAnswer: Context error -> " + contextError);
            return "⚠️ Unable to verify live event data from campus database: " + contextError + "\nPlease check your network connection and try again!";
        }

        if (!isContextLoaded && isLoadingContext) {
            Log.d(TAG, "generateGroundedAnswer: Still loading context...");
            return "⏳ Syncing live campus event records... Please try again in a moment!";
        }

        String q = rawQuery.toLowerCase().trim();
        Log.d(TAG, "generateGroundedAnswer: Processing query -> '" + q + "'");

        // 1. Retrieve filtered future approved events
        List<EventModel> futureEvents = getApprovedFutureEvents();
        Log.d(TAG, "generateGroundedAnswer: Number of approved future events -> " + futureEvents.size());

        // FIX B: EMPTY RESULT CHECK BEFORE EVENT MATCHING OR FALLBACK
        if (futureEvents.isEmpty()) {
            Log.d(TAG, "generateGroundedAnswer: Branch -> No upcoming approved events found");
            return "There are no upcoming approved events right now.";
        }

        // 2. GENERAL EVENT LIST QUERY CHECK FIRST
        if (isGeneralEventListQuery(q)) {
            Log.d(TAG, "generateGroundedAnswer: Branch -> General Upcoming Events List");
            SpannableStringBuilder ssb = new SpannableStringBuilder("📅 Upcoming Approved Campus Events (Tap event name for details):\n\n");
            for (EventModel e : futureEvents) {
                int start = ssb.length();
                ssb.append("• ").append(e.getEventName());
                int end = ssb.length();

                addClickableEventSpan(ssb, start + 2, end, e.getEventId());

                ssb.append("\n  🏢 ").append(e.getClubName().isEmpty() ? "College Club" : e.getClubName()).append(" | 🏷️ ").append(e.getCategory()).append("\n");
                ssb.append("  📝 ").append(e.getDescription().isEmpty() ? "No description provided." : e.getDescription()).append("\n");
                ssb.append("  📅 ").append(e.getDate().isEmpty() ? "TBA" : e.getDate()).append(" | 📍 ").append(e.getVenue().isEmpty() ? "Campus" : e.getVenue()).append("\n\n");
            }
            return ssb;
        }

        // 3. STUDENT PERSONAL REGISTRATIONS / ATTENDANCE
        if (q.contains("my pass") || q.contains("my registration") || q.contains("my status") || q.equals("have i registered") || q.contains("have i registered?") || q.contains("my enrolled")) {
            Log.d(TAG, "generateGroundedAnswer: Branch -> Student Registrations");
            if (studentParticipations.isEmpty()) {
                return "🎟️ You have no active event passes recorded yet.\n\nTo register for an event, select any approved event from your Student Dashboard and tap 'Register For Event'!";
            }

            StringBuilder sb = new StringBuilder("🎟️ Your Registered Passes & Status:\n\n");
            for (ParticipationModel p : studentParticipations) {
                sb.append("• ").append(p.getEventName()).append("\n");
                sb.append("  📍 ").append(p.getEventVenue().isEmpty() ? "Campus" : p.getEventVenue());
                sb.append(" | 📅 ").append(p.getEventDate().isEmpty() ? "TBA" : p.getEventDate()).append("\n");
                sb.append("  👤 Mode: ").append(p.getParticipationType());
                if (!p.getTeamName().isEmpty()) {
                    sb.append(" (Team: ").append(p.getTeamName()).append(")");
                }
                sb.append("\n  📋 Status: ").append(p.getStatus().toUpperCase()).append("\n\n");
            }
            return sb.toString().trim();
        }

        if (q.contains("attendance") || q.contains("present") || q.contains("result") || q.contains("winner") || q.contains("award") || q.contains("certificate")) {
            Log.d(TAG, "generateGroundedAnswer: Branch -> Attendance/Results");
            if (studentParticipations.isEmpty()) {
                return "🏅 You have no verified attendance or award records in your profile yet.";
            }

            StringBuilder sb = new StringBuilder("🏅 Your Verified Attendance & Award Records:\n\n");
            for (ParticipationModel p : studentParticipations) {
                sb.append("• ").append(p.getEventName()).append("\n");
                sb.append("  📋 Attendance: ").append(p.getAttendance().toUpperCase()).append("\n");
                if (!p.getResult().isEmpty() && !"none".equalsIgnoreCase(p.getResult())) {
                    sb.append("  🏆 Award / Result: ").append(p.getResult()).append("\n");
                }
                sb.append("\n");
            }
            return sb.toString().trim();
        }

        // 4. SPECIFIC EVENT QUERY MATCHING
        boolean isSpecificQuestion = isSpecificEventQuery(q);
        Log.d(TAG, "generateGroundedAnswer: isSpecificQuestion -> " + isSpecificQuestion);

        EventModel specificEvent = findBestMatchingEvent(q, futureEvents);
        Log.d(TAG, "generateGroundedAnswer: Matched specific event -> " + (specificEvent != null ? specificEvent.getEventName() : "NONE"));

        if (specificEvent != null) {
            Log.d(TAG, "generateGroundedAnswer: Branch -> Matched specific event for " + specificEvent.getEventName());

            // A. Registration status for THIS specific event
            if (q.contains("register") || q.contains("pass") || q.contains("have i") || q.contains("my status") || q.contains("enrolled")) {
                ParticipationModel studentPart = null;
                for (ParticipationModel p : studentParticipations) {
                    if (p.getEventId().equals(specificEvent.getEventId()) ||
                        p.getEventName().toLowerCase().contains(specificEvent.getEventName().toLowerCase())) {
                        studentPart = p;
                        break;
                    }
                }

                if (studentPart != null) {
                    StringBuilder sb = new StringBuilder("🎟️ Registration Status for " + specificEvent.getEventName() + ":\n\n");
                    sb.append("• Status: ").append(studentPart.getStatus().toUpperCase()).append("\n");
                    sb.append("• Mode: ").append(studentPart.getParticipationType());
                    if (!studentPart.getTeamName().isEmpty()) {
                        sb.append(" (Team: ").append(studentPart.getTeamName()).append(")");
                    }
                    sb.append("\n• Date: ").append(specificEvent.getDate().isEmpty() ? "TBA" : specificEvent.getDate());
                    sb.append("\n• Venue: ").append(specificEvent.getVenue().isEmpty() ? "Campus" : specificEvent.getVenue());
                    return sb.toString();
                } else {
                    return "🎟️ You have NOT registered for " + specificEvent.getEventName() + " yet.\n\nTo register, go to your Student Dashboard, tap on '" + specificEvent.getEventName() + "', and tap 'Register For Event'!";
                }
            }

            // B. Date & Time for THIS specific event
            if (q.contains("when") || q.contains("date") || q.contains("time") || q.contains("schedule") || q.contains("start") || q.contains("end")) {
                StringBuilder sb = new StringBuilder("📅 Date & Time for " + specificEvent.getEventName() + ":\n\n");
                sb.append("• Date: ").append(specificEvent.getDate().isEmpty() ? "TBA" : specificEvent.getDate()).append("\n");
                if (!specificEvent.getStartTime().isEmpty()) {
                    sb.append("• Time: ").append(specificEvent.getStartTime());
                    if (!specificEvent.getEndTime().isEmpty()) {
                        sb.append(" - ").append(specificEvent.getEndTime());
                    }
                    sb.append("\n");
                }
                return sb.toString().trim();
            }

            // C. Venue / Location for THIS specific event
            if (q.contains("where") || q.contains("venue") || q.contains("location") || q.contains("hall") || q.contains("ground") || q.contains("lab")) {
                return "📍 Venue for " + specificEvent.getEventName() + ":\n\n• Location: " + (specificEvent.getVenue().isEmpty() ? "Campus Ground" : specificEvent.getVenue());
            }

            // D. Deadline for THIS specific event
            if (q.contains("deadline") || q.contains("last date") || q.contains("close") || q.contains("expiry")) {
                String dl = specificEvent.getRegistrationDeadline();
                return "⏳ Registration Deadline for " + specificEvent.getEventName() + ":\n\n• Deadline: " + (dl.isEmpty() ? "Open until event date" : dl);
            }

            // E. Eligibility / Department for THIS specific event
            if (q.contains("eligible") || q.contains("dept") || q.contains("department") || q.contains("who can") || q.contains("category") || q.contains("mode")) {
                StringBuilder sb = new StringBuilder("🏛️ Details for " + specificEvent.getEventName() + ":\n\n");
                sb.append("• Department: ").append(specificEvent.getDepartment().isEmpty() ? "All Departments" : specificEvent.getDepartment()).append("\n");
                sb.append("• Category: ").append(specificEvent.getCategory().isEmpty() ? "General" : specificEvent.getCategory()).append("\n");
                sb.append("• Participation Mode: ").append(specificEvent.getParticipationType()).append("\n");
                sb.append("• Organized by: ").append(specificEvent.getClubName().isEmpty() ? "College Club" : specificEvent.getClubName());
                return sb.toString();
            }

            // F. Full Overview for THIS specific event (Student-safe: No Budget!)
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            int titleStart = ssb.length();
            ssb.append("📌 ").append(specificEvent.getEventName());
            int titleEnd = ssb.length();

            addClickableEventSpan(ssb, titleStart + 2, titleEnd, specificEvent.getEventId());

            ssb.append("\n\n🏢 Organized by: ").append(specificEvent.getClubName().isEmpty() ? "College Club" : specificEvent.getClubName()).append("\n");
            ssb.append("📝 ").append(specificEvent.getDescription().isEmpty() ? "No description provided." : specificEvent.getDescription()).append("\n");
            ssb.append("📅 Date: ").append(specificEvent.getDate().isEmpty() ? "TBA" : specificEvent.getDate());
            if (!specificEvent.getStartTime().isEmpty()) ssb.append(" (").append(specificEvent.getStartTime()).append(")");
            ssb.append("\n📍 Venue: ").append(specificEvent.getVenue().isEmpty() ? "Campus" : specificEvent.getVenue()).append("\n");
            ssb.append("🏷️ Category: ").append(specificEvent.getCategory().isEmpty() ? "General" : specificEvent.getCategory()).append(" | 🏛️ Dept: ").append(specificEvent.getDepartment()).append("\n");
            ssb.append("👤 Mode: ").append(specificEvent.getParticipationType()).append("\n");
            ssb.append("⏳ Deadline: ").append(specificEvent.getRegistrationDeadline().isEmpty() ? "Open" : specificEvent.getRegistrationDeadline());
            return ssb;
        } else if (isSpecificQuestion) {
            // FIX A: Specific event requested but no match found in approved future events
            Log.d(TAG, "generateGroundedAnswer: Branch -> Specific event requested but no match found");
            return "I couldn't find that event among the upcoming approved events. Please check the event name or ask to see upcoming events.";
        }

        // 5. How to Register Instructions
        if (q.contains("how to register") || q.contains("how do i join") || q.contains("registration step") || q.contains("how can i register")) {
            Log.d(TAG, "generateGroundedAnswer: Branch -> How to register instructions");
            return "📋 How to Register for Events:\n\n1. Open your Student Dashboard.\n2. Tap on any event card to view full details.\n3. Tap 'Register For Event' (for Individual) or 'Register Team' (for Pair/Team events).\n\nYour pass ID will automatically appear in 'My Passes' upon registration!";
        }

        // 6. Category / Department Filter
        if (q.contains("cse") || q.contains("it") || q.contains("e&tc") || q.contains("technical") ||
            q.contains("cultural") || q.contains("sports") || q.contains("workshop") || q.contains("hackathon") || q.contains("seminar")) {

            Log.d(TAG, "generateGroundedAnswer: Branch -> Category/Dept filter");
            List<EventModel> matches = new ArrayList<>();
            for (EventModel e : futureEvents) {
                String cat = e.getCategory().toLowerCase();
                String dept = e.getDepartment().toLowerCase();
                String name = e.getEventName().toLowerCase();

                if (q.contains(cat) || q.contains(dept) || name.contains(q) || (cat.length() > 2 && q.contains(cat)) || (dept.length() > 2 && q.contains(dept))) {
                    matches.add(e);
                }
            }

            if (!matches.isEmpty()) {
                SpannableStringBuilder ssb = new SpannableStringBuilder("🔍 Events matching your criteria (Tap event name for details):\n\n");
                for (EventModel e : matches) {
                    int start = ssb.length();
                    ssb.append("• ").append(e.getEventName());
                    int end = ssb.length();

                    addClickableEventSpan(ssb, start + 2, end, e.getEventId());

                    ssb.append("\n  🏢 ").append(e.getClubName().isEmpty() ? "College Club" : e.getClubName()).append(" | 🏷️ ").append(e.getCategory()).append(" | 🏛️ ").append(e.getDepartment()).append("\n");
                    ssb.append("  📝 ").append(e.getDescription().isEmpty() ? "No description provided." : e.getDescription()).append("\n");
                    ssb.append("  📅 ").append(e.getDate().isEmpty() ? "TBA" : e.getDate()).append(" | 📍 ").append(e.getVenue().isEmpty() ? "Campus" : e.getVenue()).append("\n\n");
                }
                return ssb;
            } else {
                return "I couldn't find any upcoming approved events matching that category or department. Please ask to see upcoming events.";
            }
        }

        // 7. Conversational Fallback
        Log.d(TAG, "generateGroundedAnswer: Branch -> Conversational Fallback");
        return "👋 Hello! I am your EventHive AI Assistant.\n\nI can answer questions about campus events, dates, venues, registration deadlines, and your passes!\n\nTry asking:\n• \"What events are available?\"\n• \"When is the AI Hackathon?\"\n• \"Where is the Drone Race?\"\n• \"Have I registered?\"\n• \"Which events match CSE?\"";
    }

    private boolean isGeneralEventListQuery(String q) {
        return q.contains("upcoming events") || q.contains("available events") ||
               q.contains("what events") || q.contains("all events") ||
               q.contains("list events") || q.contains("show events") ||
               q.contains("show me events") || q.contains("show me upcoming") ||
               q.contains("list upcoming") || q.contains("show upcoming") ||
               q.contains("get events");
    }

    private boolean isSpecificEventQuery(String q) {
        if (isGeneralEventListQuery(q)) return false;
        return q.contains("when is") || q.contains("where is") ||
               q.contains("tell me about") || q.contains("details of") ||
               q.contains("about ") || q.contains("info on") || q.contains("venue for") ||
               q.contains("date for") || q.contains("time for") || q.contains("tell me") ||
               q.contains("what is") || q.contains("is there");
    }

    private EventModel findBestMatchingEvent(String query, List<EventModel> events) {
        String cleanQ = query.toLowerCase()
                .replaceAll("\\b(when|where|tell|me|about|details|of|is|the|for|a|an|in|what|how|show|info|venue|date|time|happening|there|any)\\b", "")
                .trim();

        EventModel bestEvent = null;
        int maxScore = 0;

        for (EventModel event : events) {
            String fullEventName = event.getEventName().toLowerCase();
            if (fullEventName.isEmpty()) continue;

            int score = 0;

            // Direct substring match
            if (query.contains(fullEventName) || (cleanQ.length() >= 3 && fullEventName.contains(cleanQ))) {
                score += 50;
            }

            // Word-by-word match
            String[] words = fullEventName.split("[\\s&\\-]+");
            for (String word : words) {
                String cleanWord = word.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                if (cleanWord.length() >= 2 && !isStopWord(cleanWord)) {
                    if (query.contains(cleanWord)) {
                        score += 10;
                    }
                }
            }

            if (score > maxScore) {
                maxScore = score;
                bestEvent = event;
            }
        }

        return maxScore >= 10 ? bestEvent : null;
    }

    private boolean isStopWord(String word) {
        return "when".equals(word) || "where".equals(word) || "tell".equals(word) ||
               "about".equals(word) || "this".equals(word) || "that".equals(word) ||
               "event".equals(word) || "events".equals(word) || "show".equals(word) ||
               "2026".equals(word) || "annual".equals(word) || "college".equals(word) ||
               "club".equals(word) || "fest".equals(word) || "contest".equals(word) ||
               "summit".equals(word) || "meet".equals(word) || "what".equals(word) ||
               "is".equals(word) || "are".equals(word) || "for".equals(word) ||
               "the".equals(word) || "in".equals(word) || "at".equals(word) || "on".equals(word);
    }

    private List<EventModel> getApprovedFutureEvents() {
        List<EventModel> futureList = new ArrayList<>();
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        for (EventModel e : approvedEvents) {
            String normDate = normalizeToDbDate(e.getDate());
            if (normDate.isEmpty() || normDate.compareTo(todayStr) >= 0) {
                futureList.add(e);
            }
        }
        Collections.sort(futureList, (e1, e2) -> normalizeToDbDate(e1.getDate()).compareTo(normalizeToDbDate(e2.getDate())));
        return futureList;
    }

    private String normalizeToDbDate(String inputDate) {
        if (TextUtils.isEmpty(inputDate)) return "";
        if (inputDate.matches("\\d{4}-\\d{2}-\\d{2}")) return inputDate;
        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d = displayFormat.parse(inputDate);
            if (d != null) return dbFormat.format(d);
        } catch (ParseException ignored) {}
        return inputDate;
    }

    private void addClickableEventSpan(SpannableStringBuilder ssb, int start, int end, String eventId) {
        ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Context ctx = widget.getContext();
                Intent intent = new Intent(ctx, EventDetailsActivity.class);
                intent.putExtra("EVENT_ID", eventId);
                intent.putExtra("USER_ROLE", "STUDENT");
                ctx.startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#A78BFA"));
                ds.setUnderlineText(true);
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
