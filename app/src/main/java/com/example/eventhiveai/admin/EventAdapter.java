package com.example.eventhiveai.admin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.EventDetailsActivity;
import com.example.eventhiveai.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private ArrayList<EventModel> eventList;
    private String userRole = "ADMIN";
    private boolean showActionButtons = true;

    public interface OnEventActionListener {
        void onEventStatusChanged();
    }

    private OnEventActionListener listener;

    public EventAdapter(ArrayList<EventModel> eventList) {
        this.eventList = eventList;
        this.showActionButtons = true;
    }

    public EventAdapter(ArrayList<EventModel> eventList, String userRole, boolean showActionButtons, OnEventActionListener listener) {
        this.eventList = eventList;
        this.userRole = userRole;
        this.showActionButtons = showActionButtons;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventModel event = eventList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvEventName.setText(event.getEventName());
        holder.tvClubName.setText(event.getClubName().isEmpty() ? "College Club" : event.getClubName());
        holder.tvEventDate.setText("📅 " + (event.getDate().isEmpty() ? "TBA" : event.getDate()));
        holder.tvVenue.setText("📍 " + (event.getVenue().isEmpty() ? "Campus" : event.getVenue()));

        // Status badge
        String status = event.getStatus().toUpperCase();
        if (holder.tvStatusBadge != null) {
            holder.tvStatusBadge.setText(status);
            if ("APPROVED".equals(status)) {
                holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#11382A"));
                holder.tvStatusBadge.setTextColor(Color.parseColor("#10B981"));
            } else if ("REJECTED".equals(status)) {
                holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#3D1616"));
                holder.tvStatusBadge.setTextColor(Color.parseColor("#EF4444"));
            } else {
                holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#3D2F12"));
                holder.tvStatusBadge.setTextColor(Color.parseColor("#F59E0B"));
            }
        }

        // Action buttons visibility
        if (holder.layoutActionButtons != null) {
            if ("ADMIN".equals(userRole) && showActionButtons && "pending".equalsIgnoreCase(event.getStatus())) {
                holder.layoutActionButtons.setVisibility(View.VISIBLE);
            } else {
                holder.layoutActionButtons.setVisibility(View.GONE);
            }
        }

        // APPROVE EVENT
        holder.btnApprove.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("events")
                    .document(event.getEventId())
                    .update("status", "approved", "rejectionReason", "")
                    .addOnSuccessListener(unused -> {
                        Map<String, Object> notification = new HashMap<>();
                        notification.put("title", "Event approved: " + event.getEventName());
                        notification.put("message", "Your event is approved and is now visible to students.");
                        notification.put("recipientRole", "STUDENT");
                        notification.put("timestamp", FieldValue.serverTimestamp());
                        FirebaseFirestore.getInstance().collection("notifications").add(notification);
                        Toast.makeText(context, "Event Approved!", Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onEventStatusChanged();
                        } else {
                            eventList.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // REJECT EVENT WITH REASON DIALOG
        holder.btnReject.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Reject Event: " + event.getEventName());
            builder.setMessage("Please enter the reason for rejecting this event proposal:");

            final EditText input = new EditText(context);
            input.setHint("Rejection reason (required)...");
            input.setPadding(40, 30, 40, 30);
            builder.setView(input);

            builder.setPositiveButton("Reject", (dialog, which) -> {
                String reason = input.getText().toString().trim();
                if (TextUtils.isEmpty(reason)) {
                    Toast.makeText(context, "Rejection reason cannot be empty.", Toast.LENGTH_LONG).show();
                    return;
                }

                FirebaseFirestore.getInstance()
                        .collection("events")
                        .document(event.getEventId())
                        .update("status", "rejected", "rejectionReason", reason)
                        .addOnSuccessListener(unused -> {
                            Map<String, Object> notification = new HashMap<>();
                            notification.put("title", "Event proposal declined: " + event.getEventName());
                            notification.put("message", "Reason: " + reason);
                            notification.put("recipientRole", "COORDINATOR");
                            notification.put("targetUid", event.getCoordinatorId());
                            notification.put("timestamp", FieldValue.serverTimestamp());
                            FirebaseFirestore.getInstance().collection("notifications").add(notification);
                            Toast.makeText(context, "Event Rejected.", Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onEventStatusChanged();
                            } else {
                                eventList.remove(holder.getAdapterPosition());
                                notifyItemRemoved(holder.getAdapterPosition());
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.show();
        });

        // CLICK ITEM -> EVENT DETAILS ACTIVITY
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra("EVENT_ID", event.getEventId());
            intent.putExtra("USER_ROLE", userRole);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView tvEventName;
        TextView tvClubName;
        TextView tvEventDate;
        TextView tvVenue;
        TextView tvStatusBadge;
        View layoutActionButtons;
        Button btnApprove;
        Button btnReject;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvClubName = itemView.findViewById(R.id.tvClubName);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvVenue = itemView.findViewById(R.id.tvVenue);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            layoutActionButtons = itemView.findViewById(R.id.layoutActionButtons);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
