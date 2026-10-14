package com.example.eventhiveai.admin;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.R;
import com.example.eventhiveai.models.UserModel;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(UserModel user);
    }

    private ArrayList<UserModel> userList;
    private OnUserClickListener listener;

    public UserAdapter(ArrayList<UserModel> userList) {
        this(userList, null);
    }

    public UserAdapter(ArrayList<UserModel> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);

        String role = user.getRole() != null ? user.getRole().toUpperCase() : "STUDENT";
        holder.tvUserName.setText(user.getName() != null && !user.getName().isEmpty() ? user.getName() : "Campus User");
        holder.tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "");

        if (user.getName() != null && !user.getName().isEmpty()) {
            holder.tvUserAvatar.setText(user.getName().substring(0, 1).toUpperCase());
        } else {
            holder.tvUserAvatar.setText("U");
        }

        if (holder.tvUserRoleBadge != null) {
            holder.tvUserRoleBadge.setText(role);
            if ("COORDINATOR".equals(role)) {
                holder.tvUserRoleBadge.setBackgroundColor(Color.parseColor("#2E1C4D"));
                holder.tvUserRoleBadge.setTextColor(Color.parseColor("#A78BFA"));
            } else {
                holder.tvUserRoleBadge.setBackgroundColor(Color.parseColor("#1E3A8A"));
                holder.tvUserRoleBadge.setTextColor(Color.parseColor("#60A5FA"));
            }
        }

        String dept = user.getDepartment() != null && !user.getDepartment().isEmpty() ? user.getDepartment() : "General";
        holder.tvUserDepartment.setText("🏛️ Dept: " + dept);

        if ("STUDENT".equals(role)) {
            String roll = user.getRollNumber() != null && !user.getRollNumber().isEmpty() ? user.getRollNumber() : "Student";
            holder.tvUserClub.setText("🎓 ID: " + roll);
        } else {
            String club = user.getClubId() != null && !user.getClubId().isEmpty() ? user.getClubId() : "Assigned Club";
            holder.tvUserClub.setText("🎯 Club: " + club);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onUserClick(user);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserAvatar;
        TextView tvUserName;
        TextView tvUserEmail;
        TextView tvUserRoleBadge;
        TextView tvUserDepartment;
        TextView tvUserClub;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserAvatar = itemView.findViewById(R.id.tvUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRoleBadge = itemView.findViewById(R.id.tvUserRoleBadge);
            tvUserDepartment = itemView.findViewById(R.id.tvUserDepartment);
            tvUserClub = itemView.findViewById(R.id.tvUserClub);
        }
    }
}
