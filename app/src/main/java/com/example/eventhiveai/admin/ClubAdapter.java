package com.example.eventhiveai.admin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.R;
import com.example.eventhiveai.models.ClubModel;

import java.util.ArrayList;

public class ClubAdapter extends RecyclerView.Adapter<ClubAdapter.ClubViewHolder> {

    private ArrayList<ClubModel> clubList;
    private String userRole = "ADMIN";

    public ClubAdapter(ArrayList<ClubModel> clubList, String userRole) {
        this.clubList = clubList;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public ClubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.club_item, parent, false);

        return new ClubViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClubViewHolder holder, int position) {

        ClubModel club = clubList.get(position);
        Context context = holder.itemView.getContext();

        // Safely handle null Firebase values
        String name = club.getName() != null ? club.getName() : "";
        String description = club.getDescription() != null
                ? club.getDescription()
                : "";
        String email = club.getContactEmail() != null
                ? club.getContactEmail()
                : "";
        String phone = club.getContactPhone() != null
                ? club.getContactPhone()
                : "";
        String coordinatorId = club.getCoordinatorId() != null
                ? club.getCoordinatorId()
                : "";

        // Club name
        holder.tvClubName.setText(name);

        // Description
        holder.tvClubDescription.setText(description);

        // Contact
        holder.tvClubContact.setText(
                "✉️ " + (email.isEmpty() ? "No email" : email)
                        + "  📞 " + (phone.isEmpty() ? "No phone" : phone)
        );

        // Avatar
        if (!name.isEmpty()) {
            holder.tvClubAvatar.setText(
                    name.substring(0, 1).toUpperCase()
            );
        } else {
            holder.tvClubAvatar.setText("C");
        }

        // Coordinator
        String coordinatorText =
                "Coordinator ID: "
                        + (coordinatorId.isEmpty()
                        ? "Unassigned"
                        : coordinatorId);

        holder.tvClubCoordinator.setText(coordinatorText);

        // Status
        String status = club.getStatus() != null
                ? club.getStatus().toUpperCase()
                : "ACTIVE";

        holder.tvClubStatus.setText(status);

        if ("ACTIVE".equals(status)) {

            holder.tvClubStatus.setBackgroundColor(
                    Color.parseColor("#11382A")
            );

            holder.tvClubStatus.setTextColor(
                    Color.parseColor("#10B981")
            );

        } else {

            holder.tvClubStatus.setBackgroundColor(
                    Color.parseColor("#3D1616")
            );

            holder.tvClubStatus.setTextColor(
                    Color.parseColor("#EF4444")
            );
        }

        // Open club details
        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(
                    context,
                    ClubDetailsActivity.class
            );

            intent.putExtra("CLUB_ID", club.getClubId());
            intent.putExtra("CLUB_NAME", name);
            intent.putExtra("USER_ROLE", userRole);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return clubList.size();
    }

    public static class ClubViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvClubAvatar;
        TextView tvClubName;
        TextView tvClubCoordinator;
        TextView tvClubStatus;
        TextView tvClubDescription;
        TextView tvClubContact;

        public ClubViewHolder(@NonNull View itemView) {
            super(itemView);

            tvClubAvatar =
                    itemView.findViewById(R.id.tvClubAvatar);

            tvClubName =
                    itemView.findViewById(R.id.tvClubName);

            tvClubCoordinator =
                    itemView.findViewById(R.id.tvClubCoordinator);

            tvClubStatus =
                    itemView.findViewById(R.id.tvClubStatus);

            tvClubDescription =
                    itemView.findViewById(R.id.tvClubDescription);

            tvClubContact =
                    itemView.findViewById(R.id.tvClubContact);
        }
    }
}