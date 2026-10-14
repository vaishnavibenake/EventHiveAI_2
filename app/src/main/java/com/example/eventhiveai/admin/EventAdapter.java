
package com.example.eventhiveai.admin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.EventDetailsActivity;
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

    public EventAdapter(Context context, List<EventModel> eventList,
                        OnEventActionListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.listener = listener;
    }

    public EventAdapter(List<EventModel> eventList, String userRole,
                        boolean isReadOnly, Runnable onRefreshCallback) {
        this.eventList = eventList;
        this.userRole = userRole;
        this.isReadOnly = isReadOnly;
        this.onRefreshCallback = onRefreshCallback;
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

        String title = event.getEventName();
        holder.tvTitle.setText(title == null || title.trim().isEmpty()
                ? "Untitled Event" : title);

        String club = event.getClubName();
        holder.tvClub.setText(club == null || club.trim().isEmpty()
                ? "College Club" : club);

        String date = event.getDate() != null ? event.getDate() : "Date TBA";
        String venue = event.getVenue() != null ? event.getVenue() : "Venue TBA";

        holder.tvDate.setText("📅 " + date);
        holder.tvVenue.setText("📍 " + venue);

        String status = event.getStatus() == null
                ? "pending" : event.getStatus().toLowerCase();

        if ("approved".equals(status)) {
            holder.tvStatus.setText("APPROVED");
            holder.tvStatus.setBackgroundColor(Color.parseColor("#132A1C"));
            holder.tvStatus.setTextColor(Color.parseColor("#4ADE80"));
        } else if ("rejected".equals(status)) {
            holder.tvStatus.setText("REJECTED");
            holder.tvStatus.setBackgroundColor(Color.parseColor("#451A1A"));
            holder.tvStatus.setTextColor(Color.parseColor("#F87171"));
        } else if ("active".equals(status)) {
            holder.tvStatus.setText("ACTIVE NOW");
            holder.tvStatus.setBackgroundColor(Color.parseColor("#1E3A8A"));
            holder.tvStatus.setTextColor(Color.parseColor("#60A5FA"));
        } else {
            holder.tvStatus.setText("PENDING");
            holder.tvStatus.setBackgroundColor(Color.parseColor("#3D2F12"));
            holder.tvStatus.setTextColor(Color.parseColor("#F59E0B"));
        }

        boolean showActions = "pending".equals(status)
                && listener != null && !isReadOnly;

        holder.btnApprove.setVisibility(showActions ? View.VISIBLE : View.GONE);
        holder.btnReject.setVisibility(showActions ? View.VISIBLE : View.GONE);

        holder.btnApprove.setOnClickListener(v -> {
            if (System.currentTimeMillis() - lastClickTime < 1000) return;
            lastClickTime = System.currentTimeMillis();

            if (listener != null) {
                listener.onApprove(event);
            }
        });

        holder.btnReject.setOnClickListener(v -> {
            if (System.currentTimeMillis() - lastClickTime < 1000) return;
            lastClickTime = System.currentTimeMillis();

            if (listener != null) {
                listener.onReject(event);
            }
        });

        // Show the completion button only where coordinator code controls it.
        if (holder.btnMarkCompleted != null) {
            holder.btnMarkCompleted.setVisibility(View.GONE);
        }

        View.OnClickListener openDetails = v -> {
            Context itemContext = context != null
                    ? context : holder.itemView.getContext();

            Intent intent = new Intent(itemContext, EventDetailsActivity.class);
            intent.putExtra("EVENT_ID", event.getEventId());
            intent.putExtra("USER_ROLE", userRole);
            itemContext.startActivity(intent);
        };

        holder.itemView.setOnClickListener(openDetails);
    }

    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvClub, tvStatus, tvDate, tvVenue;
        Button btnApprove, btnReject, btnMarkCompleted;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvEventName);
            tvClub = itemView.findViewById(R.id.tvClubName);
            tvStatus = itemView.findViewById(R.id.tvStatusBadge);
            tvDate = itemView.findViewById(R.id.tvEventDate);
            tvVenue = itemView.findViewById(R.id.tvVenue);

            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnMarkCompleted = itemView.findViewById(R.id.btnMarkCompleted);
        }
    }
}
