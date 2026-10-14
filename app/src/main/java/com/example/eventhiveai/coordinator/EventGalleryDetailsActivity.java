package com.example.eventhiveai.coordinator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.eventhiveai.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shows photos for a specific event and allows the coordinator
 * to upload new photos via ImageKit (through the secure backend).
 *
 * Upload flow:
 *  1. User picks images.
 *  2. Android fetches ImageKit auth credentials from our backend (POST /gallery/auth).
 *     The backend verifies the Firebase ID token and checks club/event ownership.
 *  3. Android uploads the image directly to ImageKit using the returned credentials.
 *  4. Android calls POST /gallery/confirm with the ImageKit file ID and URL.
 *     The backend re-verifies auth, validates the file exists in ImageKit, and
 *     writes photo metadata to Firestore.
 *
 * Deletion flow:
 *  1. User long-presses a photo and confirms deletion.
 *  2. Android calls DELETE /gallery/{eventId}/{photoId} with the Firebase ID token.
 *     The backend verifies auth, deletes from ImageKit, then removes Firestore record.
 *
 * Data layout (Firestore):
 *   events/{eventId}/photos/{photoId}
 *     - photoId, eventId, clubId, imagekitFileId, downloadUrl,
 *       fileName, uploadedBy, uploadedAt, caption
 */
public class EventGalleryDetailsActivity extends AppCompatActivity {

    private static final String TAG = "GalleryDetails";

    // ── CONFIGURATION ─────────────────────────────────────────────────────────
    // Replace this placeholder with your backend URL when deployed.
    // For local testing on a physical device: use your computer's LAN IP, e.g.:
    //   http://192.168.1.X:3000
    // For an emulator: use http://10.0.2.2:3000
    // For production: use your Railway / Render / Cloud Run URL.
    // DO NOT hardcode credentials here — only the base URL belongs here.
    private static final String BACKEND_BASE_URL = "http://10.233.21.55:3000";
    // ─────────────────────────────────────────────────────────────────────────

    private ImageButton btnBack;
    private TextView tvEventName;
    private GridLayout photoGrid;
    private LinearLayout winnersContainer;
    private ProgressBar progressBar;
    private TextView tvEmptyPhotos;
    private FloatingActionButton fabAddPhotos;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String eventId;
    private String clubId;

    /** Background thread pool for network calls (never runs on main thread). */
    private final ExecutorService executor = Executors.newCachedThreadPool();

    // Image picker launcher — multi-select
    private ActivityResultLauncher<String[]> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.example.eventhiveai.SettingsActivity.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_gallery_details);

        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnBack         = findViewById(R.id.btnBack);
        tvEventName     = findViewById(R.id.tvEventName);
        winnersContainer= findViewById(R.id.winnersContainer);
        photoGrid       = findViewById(R.id.photoGrid);
        progressBar     = findViewById(R.id.progressBarPhotos);
        tvEmptyPhotos   = findViewById(R.id.tvEmptyPhotos);
        fabAddPhotos    = findViewById(R.id.fabAddPhotos);

        eventId = getIntent().getStringExtra("EVENT_ID");
        clubId  = getIntent().getStringExtra("CLUB_ID");
        String eventName = getIntent().getStringExtra("EVENT_NAME");
        if (eventName == null || eventName.isEmpty()) eventName = "Event Gallery";
        tvEventName.setText(eventName);

        btnBack.setOnClickListener(v -> finish());

        // Register image picker — multi-select (accepts images only)
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        uploadPhotos(uris);
                    }
                }
        );

        if (fabAddPhotos != null) {
            fabAddPhotos.setOnClickListener(v -> openImagePicker());
        }

        loadCoordinatorClubId();
        loadPhotos();
        loadWinners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Auth helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Load coordinator's clubId from Firestore profile (if not already supplied). */
    private void loadCoordinatorClubId() {
        if (auth.getCurrentUser() == null) return;
        if (clubId != null && !clubId.isEmpty()) return;

        db.collection("users").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String cId = doc.getString("clubId");
                        if (cId != null) clubId = cId;
                    }
                });
    }

    /**
     * Gets a fresh Firebase ID token on the calling thread (must NOT be main thread).
     * Returns null if the user is not signed in.
     */
    private String getIdTokenSync() throws Exception {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return null;

        // getIdToken(false) uses a cached token; pass true to force-refresh
        com.google.android.gms.tasks.Task<com.google.firebase.auth.GetTokenResult> task =
                user.getIdToken(false);

        // Block until token is available (we are already on a background thread)
        com.google.android.gms.tasks.Tasks.await(task);
        if (task.isSuccessful() && task.getResult() != null) {
            return task.getResult().getToken();
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Image picker
    // ─────────────────────────────────────────────────────────────────────────

    private void openImagePicker() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Event ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (BACKEND_BASE_URL.contains("YOUR_BACKEND_URL_HERE")) {
            Toast.makeText(this,
                    "Backend URL is not configured. Open EventGalleryDetailsActivity.java and set BACKEND_BASE_URL.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        imagePickerLauncher.launch(new String[]{"image/*"});
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Upload flow
    // ─────────────────────────────────────────────────────────────────────────

    private void uploadPhotos(List<Uri> uris) {
        if (uris == null || uris.isEmpty()) return;

        setUploadingState(true);

        final int total = uris.size();
        final int[] uploaded = {0};
        final int[] failed   = {0};
        final int[] pending  = {total};

        for (Uri uri : uris) {
            executor.execute(() -> {
                boolean success = false;
                try {
                    success = uploadSinglePhoto(uri);
                } catch (Exception e) {
                    Log.e(TAG, "Upload exception for URI: " + uri, e);
                }

                synchronized (pending) {
                    if (success) uploaded[0]++;
                    else         failed[0]++;
                    pending[0]--;
                    if (pending[0] <= 0) {
                        final int u = uploaded[0];
                        final int f = failed[0];
                        runOnUiThread(() -> onAllUploadsFinished(u, f));
                    }
                }
            });
        }
    }

    /**
     * Full upload sequence for a single image.
     * 1. Fetch backend auth credentials.
     * 2. Upload image bytes to ImageKit.
     * 3. Confirm metadata with the backend (which writes to Firestore).
     *
     * @return true on full success, false on any failure.
     */
    private boolean uploadSinglePhoto(Uri uri) throws Exception {
        // ── Step 1: Get backend auth credentials ────────────────────────────
        String idToken = getIdTokenSync();
        if (idToken == null) {
            Log.w(TAG, "No authenticated user — cannot upload.");
            return false;
        }

        JSONObject authBody = new JSONObject();
        authBody.put("eventId", eventId);

        JSONObject authResponse = httpPost(
                BACKEND_BASE_URL + "/gallery/auth",
                authBody.toString(),
                idToken
        );

        if (authResponse == null || authResponse.has("error") || !authResponse.has("signature")) {
            String serverError = authResponse != null
                    ? authResponse.optString("error", "unknown error from server")
                    : "no response from server";
            Log.e(TAG, "[upload/auth] Backend returned error: " + serverError);
            runOnUiThread(() -> Toast.makeText(this,
                    "Upload auth failed: " + serverError, Toast.LENGTH_LONG).show());
            return false;
        }

        String ikToken     = authResponse.getString("token");
        String ikExpire    = String.valueOf(authResponse.getLong("expire"));
        String ikSignature = authResponse.getString("signature");
        String publicKey   = authResponse.getString("publicKey");
        String urlEndpoint = authResponse.getString("urlEndpoint");
        String uploadFolder= authResponse.optString("uploadFolder", "/eventhive/events/" + eventId + "/photos");
        Log.d(TAG, "[Step1/auth] OK — received auth credentials from backend (signature present)");

        // ── Step 2: Upload to ImageKit directly ────────────────────────────
        String fileName = "photo_" + UUID.randomUUID().toString().replace("-", "") + ".jpg";

        // Read image bytes
        byte[] imageBytes;
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            if (is == null) throw new IOException("Cannot open image stream");
            imageBytes = readAllBytes(is);
        }

        // Enforce client-side size check (backend enforces this too)
        if (imageBytes.length > 10 * 1024 * 1024) {
            runOnUiThread(() -> Toast.makeText(this,
                    "Image is too large (max 10 MB).", Toast.LENGTH_SHORT).show());
            return false;
        }

        // Multipart upload to ImageKit upload endpoint
        JSONObject ikResponse = uploadToImageKit(
                imageBytes, fileName, uploadFolder,
                publicKey, ikToken, ikExpire, ikSignature
        );

        Log.d(TAG, "[Step2/imagekit] uploadToImageKit returned: " + (ikResponse != null ? "non-null response" : "null (network/connection failure)"));

        if (ikResponse == null) {
            Log.e(TAG, "[Step2/imagekit] ImageKit upload returned null response.");
            return false;
        }

        String ikFileId     = ikResponse.optString("fileId", "");
        String ikUrl        = ikResponse.optString("url", "");

        if (ikFileId.isEmpty() || ikUrl.isEmpty()) {
            Log.e(TAG, "ImageKit response missing fileId or url: " + ikResponse);
            return false;
        }

        // ── Step 3: Confirm with backend → writes Firestore record ──────────
        // Re-fetch a fresh token (the upload may have taken time)
        idToken = getIdTokenSync();
        if (idToken == null) return false;

        JSONObject confirmBody = new JSONObject();
        confirmBody.put("eventId",         eventId);
        confirmBody.put("imagekitFileId",  ikFileId);
        confirmBody.put("downloadUrl",     ikUrl);
        confirmBody.put("fileName",        fileName);
        // mimeType / fileSize are optional hints for the backend validator
        confirmBody.put("mimeType",        "image/jpeg");
        confirmBody.put("fileSize",        imageBytes.length);

        JSONObject confirmResponse = httpPost(
                BACKEND_BASE_URL + "/gallery/confirm",
                confirmBody.toString(),
                idToken
        );

        Log.d(TAG, "[Step3/confirm] confirmResponse: " + (confirmResponse != null ? confirmResponse.toString() : "null"));

        if (confirmResponse == null || !confirmResponse.optBoolean("success", false)) {
            String confirmError = confirmResponse != null
                    ? confirmResponse.optString("error", "unknown confirm error")
                    : "no response from backend (connection failure?)";
            Log.e(TAG, "[Step3/confirm] Backend confirm failed: " + confirmError);
            return false;
        }

        Log.i(TAG, "[Step3/confirm] Photo uploaded and confirmed: " + confirmResponse.optString("photoId"));
        return true;
    }

    /**
     * Upload raw image bytes to ImageKit using multipart/form-data.
     * Does NOT include the private key — only the signed auth params from the backend.
     */
    private JSONObject uploadToImageKit(
            byte[] imageBytes, String fileName, String folder,
            String publicKey, String token, String expire, String signature
    ) {
        try {
            // ImageKit upload endpoint
            URL url = new URL("https://upload.imagekit.io/api/v1/files/upload");
            String boundary = "----EventHiveBoundary" + UUID.randomUUID().toString().replace("-", "");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30_000);
            conn.setReadTimeout(60_000);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream os = conn.getOutputStream()) {
                // publicKey
                writeFormField(os, boundary, "publicKey", publicKey);
                // token, expire, signature (from backend)
                writeFormField(os, boundary, "token",     token);
                writeFormField(os, boundary, "expire",    expire);
                writeFormField(os, boundary, "signature", signature);
                // fileName
                writeFormField(os, boundary, "fileName",  fileName);
                // folder
                writeFormField(os, boundary, "folder",    folder);
                // file bytes
                writeFormFile(os, boundary, "file", fileName, imageBytes);
                // closing boundary
                os.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int code = conn.getResponseCode();
            String body = readStream(code < 400 ? conn.getInputStream() : conn.getErrorStream());
            conn.disconnect();

            if (code == 200 || code == 201) {
                return new JSONObject(body);
            }
            Log.e(TAG, "ImageKit upload HTTP " + code + ": " + body);
            return null;

        } catch (Exception e) {
            Log.e(TAG, "ImageKit upload exception", e);
            return null;
        }
    }

    private void onAllUploadsFinished(int uploaded, int failed) {
        if (isFinishing() || isDestroyed()) return;
        setUploadingState(false);

        if (uploaded > 0) {
            String msg = uploaded + " photo" + (uploaded > 1 ? "s" : "") + " uploaded!"
                    + (failed > 0 ? " " + failed + " failed." : "");
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            loadPhotos(); // Refresh grid
        } else {
            Toast.makeText(this,
                    "Upload failed. Check your connection and try again.",
                    Toast.LENGTH_LONG).show();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Load & display photos
    // ─────────────────────────────────────────────────────────────────────────

    private void loadPhotos() {
        if (eventId == null || eventId.isEmpty()) return;

        if (photoGrid   != null) photoGrid.removeAllViews();
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (tvEmptyPhotos != null) tvEmptyPhotos.setVisibility(View.GONE);

        db.collection("events").document(eventId).collection("photos")
                .get()
                .addOnSuccessListener(querySnap -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    if (querySnap.isEmpty()) {
                        if (tvEmptyPhotos != null) tvEmptyPhotos.setVisibility(View.VISIBLE);
                        return;
                    }
                    if (tvEmptyPhotos != null) tvEmptyPhotos.setVisibility(View.GONE);

                    for (QueryDocumentSnapshot doc : querySnap) {
                        String url       = doc.getString("downloadUrl");
                        String photoDocId= doc.getId();
                        if (url != null && !url.isEmpty()) {
                            addPhotoToGrid(url, photoDocId);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load photos: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void addPhotoToGrid(String downloadUrl, String photoDocId) {
        if (photoGrid == null) return;

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int itemSize    = (screenWidth - dpToPx(48)) / 2;

        ImageView imageView = new ImageView(this);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width  = itemSize;
        lp.height = itemSize;
        lp.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        imageView.setLayoutParams(lp);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(this)
                .load(downloadUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(imageView);

        imageView.setOnClickListener(v   -> viewFullScreenPhoto(downloadUrl));
        imageView.setOnLongClickListener(v -> {
            confirmDeletePhoto(photoDocId);
            return true;
        });

        photoGrid.addView(imageView);
    }

    private void viewFullScreenPhoto(String downloadUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(downloadUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open image viewer.", Toast.LENGTH_SHORT).show();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Deletion flow
    // ─────────────────────────────────────────────────────────────────────────

    private void confirmDeletePhoto(String photoDocId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Photo")
                .setMessage("Delete this photo permanently? This cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> deletePhoto(photoDocId))
                .show();
    }

    private void deletePhoto(String photoDocId) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (BACKEND_BASE_URL.contains("YOUR_BACKEND_URL_HERE")) {
            Toast.makeText(this, "Backend URL is not configured.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            try {
                String idToken = getIdTokenSync();
                if (idToken == null) {
                    runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Authentication error. Please log in again.",
                                Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                String deleteUrl = BACKEND_BASE_URL + "/gallery/" + eventId + "/" + photoDocId;
                int code = httpDelete(deleteUrl, idToken);

                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (code == 200) {
                        Toast.makeText(this, "Photo deleted.", Toast.LENGTH_SHORT).show();
                        loadPhotos();
                    } else if (code == 403) {
                        Toast.makeText(this,
                                "You are not authorized to delete this photo.",
                                Toast.LENGTH_SHORT).show();
                    } else if (code == 404) {
                        // Already gone — refresh anyway
                        Toast.makeText(this, "Photo already removed.", Toast.LENGTH_SHORT).show();
                        loadPhotos();
                    } else {
                        Toast.makeText(this,
                                "Delete failed (server error " + code + "). Try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Delete exception", e);
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Delete failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Winners (unchanged)
    // ─────────────────────────────────────────────────────────────────────────

    private void loadWinners() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Event information is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events").document(eventId).collection("winners").get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        displayWinners(snap);
                    } else {
                        db.collection("winners").whereEqualTo("eventId", eventId).get()
                                .addOnSuccessListener(this::displayWinners)
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Failed to load winners: " + e.getMessage(), e));
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load event winners subcollection: " + e.getMessage(), e));
    }

    private void displayWinners(com.google.firebase.firestore.QuerySnapshot querySnap) {
        if (winnersContainer == null) return;
        winnersContainer.removeAllViews();

        if (querySnap.isEmpty()) {
            addSimpleTextView(winnersContainer, "No winners announced yet.", "#94A3B8");
            return;
        }

        for (QueryDocumentSnapshot doc : querySnap) {
            String pType = doc.getString("participationType");

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(android.graphics.Color.parseColor("#171A27"));
            card.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, dpToPx(8), 0, 0);
            card.setLayoutParams(lp);

            if (pType != null) addSimpleTextView(card, "🏷️ " + pType + " Event", "#A78BFA");

            addWinnerRow(card, "🥇 1st Place", doc.getString("firstPlace"),
                    doc.getString("firstPlaceName"), doc.getString("firstPlaceDepartment"),
                    doc.getString("firstPlaceTeamName"), "#F59E0B");
            addWinnerRow(card, "🥈 2nd Place", doc.getString("secondPlace"),
                    doc.getString("secondPlaceName"), doc.getString("secondPlaceDepartment"),
                    doc.getString("secondPlaceTeamName"), "#94A3B8");
            addWinnerRow(card, "🥉 3rd Place", doc.getString("thirdPlace"),
                    doc.getString("thirdPlaceName"), doc.getString("thirdPlaceDepartment"),
                    doc.getString("thirdPlaceTeamName"), "#CD7F32");

            winnersContainer.addView(card);
        }
    }

    private void addWinnerRow(LinearLayout container, String label, String legacyText,
                               String name, String dept, String teamName, String color) {
        String display;
        if (name != null && !name.isEmpty()) {
            StringBuilder sb = new StringBuilder(name);
            if (dept != null && !dept.isEmpty()) sb.append(" (").append(dept).append(")");
            if (teamName != null && !teamName.isEmpty()) sb.append("\n   Team: ").append(teamName);
            display = sb.toString();
        } else if (legacyText != null && !legacyText.isEmpty()) {
            display = legacyText;
        } else {
            return;
        }

        TextView labelTv = new TextView(this);
        labelTv.setText(label);
        labelTv.setTextColor(android.graphics.Color.parseColor(color));
        labelTv.setTextSize(13);
        labelTv.setPadding(0, dpToPx(6), 0, dpToPx(2));
        container.addView(labelTv);

        TextView valueTv = new TextView(this);
        valueTv.setText(display);
        valueTv.setTextColor(android.graphics.Color.WHITE);
        valueTv.setTextSize(15);
        valueTv.setPadding(0, 0, 0, dpToPx(2));
        container.addView(valueTv);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HTTP helpers (all run on executor threads, never on main thread)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * HTTP POST with JSON body and Firebase Bearer token.
     * Returns parsed JSONObject or null on any failure.
     */
    private JSONObject httpPost(String urlStr, String jsonBody, String idToken) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(20_000);
            conn.setReadTimeout(30_000);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + idToken);

            byte[] body = jsonBody.getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Length", String.valueOf(body.length));
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body);
            }

            int code = conn.getResponseCode();
            String response = readStream(code < 400 ? conn.getInputStream() : conn.getErrorStream());
            return new JSONObject(response);

        } catch (Exception e) {
            Log.e(TAG, "httpPost failed: " + urlStr, e);
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * HTTP DELETE with Firebase Bearer token.
     * Returns the HTTP status code, or -1 on connection failure.
     */
    private int httpDelete(String urlStr, String idToken) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setConnectTimeout(20_000);
            conn.setReadTimeout(20_000);
            conn.setRequestProperty("Authorization", "Bearer " + idToken);
            return conn.getResponseCode();
        } catch (Exception e) {
            Log.e(TAG, "httpDelete failed: " + urlStr, e);
            return -1;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Multipart helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void writeFormField(OutputStream os, String boundary, String name, String value)
            throws IOException {
        String header = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n"
                + value + "\r\n";
        os.write(header.getBytes(StandardCharsets.UTF_8));
    }

    private void writeFormFile(OutputStream os, String boundary, String fieldName,
                                String fileName, byte[] data) throws IOException {
        String header = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + fieldName
                + "\"; filename=\"" + fileName + "\"\r\n"
                + "Content-Type: image/jpeg\r\n\r\n";
        os.write(header.getBytes(StandardCharsets.UTF_8));
        os.write(data);
        os.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private byte[] readAllBytes(InputStream is) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int read;
        while ((read = is.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }

    private String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void setUploadingState(boolean uploading) {
        if (progressBar  != null) progressBar.setVisibility(uploading ? View.VISIBLE : View.GONE);
        if (fabAddPhotos != null) fabAddPhotos.setEnabled(!uploading);
    }

    private void addSimpleTextView(LinearLayout container, String text, String hexColor) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(android.graphics.Color.parseColor(hexColor));
        tv.setTextSize(14);
        tv.setPadding(dpToPx(8), dpToPx(16), dpToPx(8), dpToPx(8));
        container.addView(tv);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}