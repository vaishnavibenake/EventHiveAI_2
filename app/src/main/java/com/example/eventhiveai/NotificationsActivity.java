package com.example.eventhiveai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.models.NotificationModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class NotificationsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView recyclerNotifications;
    private ProgressBar progressBar;
    private LinearLayout emptyView;

    private ArrayList<NotificationModel> notificationList;
    private NotificationAdapter adapter;
    private FirebaseFirestore db;
    private String userRole = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        db = FirebaseFirestore.getInstance();

        String roleExtra = getIntent().getStringExtra("USER_ROLE");
        if (roleExtra != null && !roleExtra.isEmpty()) {
            userRole = roleExtra.toUpperCase();
        }

        btnBack = findViewById(R.id.btnBack);
        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        recyclerNotifications.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadNotifications();
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        db.collection("notifications").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    notificationList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String recipientRole = doc.getString("recipientRole");
                        String recipientCoordinator = doc.getString("targetUid");
                        String currentUid = FirebaseAuth.getInstance().getUid();
                        boolean targetedElsewhere = recipientCoordinator != null && !recipientCoordinator.isEmpty()
                                && !recipientCoordinator.equals(currentUid);
                        if (!targetedElsewhere && (recipientRole == null || "ALL".equalsIgnoreCase(recipientRole) ||
                                recipientRole.equalsIgnoreCase(userRole))) {
                            NotificationModel notif = doc.toObject(NotificationModel.class);
                            if (notif == null) notif = new NotificationModel();
                            notif.setNotificationId(doc.getId());
                            if (doc.contains("title")) notif.setTitle(doc.getString("title"));
                            if (doc.contains("message")) notif.setMessage(doc.getString("message"));
                            notificationList.add(notif);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    if (notificationList.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(NotificationsActivity.this, "Failed to load notifications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        private final ArrayList<NotificationModel> list;

        NotificationAdapter(ArrayList<NotificationModel> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            NotificationModel item = list.get(position);
            holder.tvNotifTitle.setText(item.getTitle());
            holder.tvNotifMessage.setText(item.getMessage());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNotifTitle;
            TextView tvNotifMessage;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNotifTitle = itemView.findViewById(R.id.tvNotifTitle);
                tvNotifMessage = itemView.findViewById(R.id.tvNotifMessage);
            }
        }
    }
}
