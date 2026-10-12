package com.example.eventhiveai.admin;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.R;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnEventActionListener {
        void onApprove(EventModel event);
        void onReject(EventModel event);
    }

    private Context context;
    private List<EventModel> eventList;
    private OnEventActionListener listener;

    private String userRole = "ADMIN";
    private boolean isReadOnly = false;
    private Runnable onRefreshCallback;
    private long lastClickTime = 0;

    public EventAdapter(Context context, List<EventModel> eventList, OnEventActionListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.listener = listener;
    }

    public EventAdapter(List<EventModel> eventList, String userRole, boolean isReadOnly, Runnable onRefreshCallback) {
        this.eventList = eventList;
        this.userRole = userRole;
        this.isReadOnly = isReadOnly;
        this.onRefreshCallback = onRefreshCallback;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventModel event = eventList.get(position);

        String title = event.getEventName();
        holder.tvTitle.setText(title != null && !title.trim().isEmpty() ? title : "Untitled Event");

        String club = event.getClubName();
        holder.tvClub.setText("🏛️ " + (club != null && !club.trim().isEmpty() ? club : "College Club"));

        String date = event.getDate() != null ? event.getDate() : "Date TBA";
        String time = event.getStartTime() != null ? event.getStartTime() : "";
        String venue = event.getVenue() != null ? event.getVenue() : "Venue TBA";
        holder.tvDateVenue.setText("📅 " + date + (!time.isEmpty() ? " • " + time : "") + " | 📍 " + venue);

        String dept = event.getDepartment();
        if (holder.tvDepartment != null) {
            holder.tvDepartment.setText("Dept: " + (dept != null && !dept.isEmpty() ? dept : "All"));
        }

        if (holder.tvBudget != null) {
            double budget = event.getBudget();
            if (budget > 0) {
                holder.tvBudget.setVisibility(View.VISIBLE);
                holder.tvBudget.setText("💰 ₹" + (int) budget);
            } else {
                holder.tvBudget.setVisibility(View.GONE);
            }
        }

        if (holder.tvCapacity != null) {
            int maxP = event.getMaxParticipants();
            if (maxP > 0) {
                holder.tvCapacity.setVisibility(View.VISIBLE);
                holder.tvCapacity.setText("👥 " + maxP + " Seats");
            } else {
                holder.tvCapacity.setVisibility(View.GONE);
            }
        }

        String rawStatus = event.getStatus() != null ? event.getStatus().toLowerCase() : "pending";
        if ("approved".equals(rawStatus)) {
            holder.tvStatus.setText("APPROVED");
            holder.tvStatus.setBackgroundColor(Color.parseColor("#132A1C"));
            holder.tvStatus.setTextColor(Color.parseColor("#4ADE80"));
        } else if ("rejected".equals(rawStatus)) {
            holder.tvStatus.setText("REJECTED");
            holder.tvStatus.setBackgroundColor(Color.parseColor("#451A1A"));
            holder.tvStatus.setTextColor(Color.parseColor("#F87171"));
        } else if ("active".equals(rawStatus)) {
            holder.tvStatus.setText("ACTIVE NOW");
            holder.tvStatus.setBackgroundColor(Color.parseColor("#1E3A8A"));
            holder.tvStatus.setTextColor(Color.parseColor("#60A5FA"));
        } else {
            holder.tvStatus.setText("PENDING REVIEW");
            holder.tvStatus.setBackgroundColor(Color.parseColor("#2E1C4D"));
            holder.tvStatus.setTextColor(Color.parseColor("#C084FC"));
        }

        // Show action buttons only if pending review and in review queue mode
        boolean isPending = "pending".equals(rawStatus);
        if (isPending && listener != null && !isReadOnly) {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
        } else {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        }

        // Debounce tap protection
        holder.btnApprove.setOnClickListener(v -> {
            if (System.currentTimeMillis() - lastClickTime < 1000) return;
            lastClickTime = System.currentTimeMillis();
            if (listener != null) listener.onApprove(event);
        });

        holder.btnReject.setOnClickListener(v -> {
            if (System.currentTimeMillis() - lastClickTime < 1000) return;
            lastClickTime = System.currentTimeMillis();
            if (listener != null) listener.onReject(event);
        });

        View.OnClickListener openDetailsListener = v -> {
            Context itemCtx = context != null ? context : holder.itemView.getContext();
            android.content.Intent intent = new android.content.Intent(itemCtx, com.example.eventhiveai.EventDetailsActivity.class);
            intent.putExtra("EVENT_ID", event.getEventId());
            intent.putExtra("USER_ROLE", userRole);
            itemCtx.startActivity(intent);
        };

        if (holder.btnViewDetails != null) {
            holder.btnViewDetails.setOnClickListener(openDetailsListener);
        }
        holder.itemView.setOnClickListener(openDetailsListener);
    }

    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvClub, tvDateVenue, tvStatus, tvDepartment, tvBudget, tvCapacity, tvConflictBadge, tvSubmissionDate;
        Button btnApprove, btnReject, btnViewDetails;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvClub = itemView.findViewById(R.id.tvClubName);
            tvDateVenue = itemView.findViewById(R.id.tvDateVenue);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvBudget = itemView.findViewById(R.id.tvBudget);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
            tvConflictBadge = itemView.findViewById(R.id.tvConflictBadge);
            tvSubmissionDate = itemView.findViewById(R.id.tvSubmissionDate);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}