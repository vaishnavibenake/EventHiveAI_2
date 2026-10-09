package com.example.eventhiveai.student;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.EventDetailsActivity;
import com.example.eventhiveai.R;
import com.example.eventhiveai.admin.EventModel;

import java.util.ArrayList;

public class StudentEventAdapter extends RecyclerView.Adapter<StudentEventAdapter.ViewHolder> {

    private ArrayList<EventModel> eventList;

    public StudentEventAdapter(ArrayList<EventModel> eventList) {
        this.eventList = eventList;
    }

    public void updateList(ArrayList<EventModel> newList) {
        this.eventList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_event_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventModel event = eventList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvStudentEventName.setText(event.getEventName());

        String pType = event.getParticipationType();
        if (TextUtils.isEmpty(pType)) {
            pType = "INDIVIDUAL";
        }
        holder.tvStudentParticipationType.setText(pType.toUpperCase());

        String club = event.getClubName();
        if (TextUtils.isEmpty(club)) {
            club = "College Club";
        }
        holder.tvStudentClubName.setText("Organized by " + club);

        String category = event.getCategory();
        if (!TextUtils.isEmpty(category)) {
            holder.tvStudentCategory.setText("• " + category);
            holder.tvStudentCategory.setVisibility(View.VISIBLE);
        } else {
            holder.tvStudentCategory.setVisibility(View.GONE);
        }

        String date = event.getDate();
        holder.tvStudentDate.setText("📅 " + (TextUtils.isEmpty(date) ? "TBA" : date));

        String venue = event.getVenue();
        holder.tvStudentVenue.setText("📍 " + (TextUtils.isEmpty(venue) ? "Campus" : venue));

        String dept = event.getDepartment();
        holder.tvStudentDepartment.setText("🏛️ Dept: " + (TextUtils.isEmpty(dept) ? "All" : dept));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra("EVENT_ID", event.getEventId());
            intent.putExtra("USER_ROLE", "STUDENT");
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentEventName, tvStudentParticipationType, tvStudentClubName,
                tvStudentCategory, tvStudentDate, tvStudentVenue, tvStudentDepartment;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentEventName = itemView.findViewById(R.id.tvStudentEventName);
            tvStudentParticipationType = itemView.findViewById(R.id.tvStudentParticipationType);
            tvStudentClubName = itemView.findViewById(R.id.tvStudentClubName);
            tvStudentCategory = itemView.findViewById(R.id.tvStudentCategory);
            tvStudentDate = itemView.findViewById(R.id.tvStudentDate);
            tvStudentVenue = itemView.findViewById(R.id.tvStudentVenue);
            tvStudentDepartment = itemView.findViewById(R.id.tvStudentDepartment);
        }
    }
}
