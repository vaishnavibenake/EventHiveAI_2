package com.example.eventhiveai.admin;

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

    private ArrayList<UserModel> userList;

    public UserAdapter(ArrayList<UserModel> userList) {
        this.userList = userList;
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

        holder.tvUserName.setText(user.getName() != null && !user.getName().isEmpty() ? user.getName() : "Faculty Coordinator");
        holder.tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "");

        if (user.getName() != null && !user.getName().isEmpty()) {
            holder.tvUserAvatar.setText(user.getName().substring(0, 1).toUpperCase());
        }

        String dept = user.getDepartment() != null && !user.getDepartment().isEmpty() ? user.getDepartment() : "General";
        holder.tvUserDepartment.setText("🏛️ Dept: " + dept);

        String club = user.getClubId() != null && !user.getClubId().isEmpty() ? user.getClubId() : "General Clubs";
        holder.tvUserClub.setText("🎯 Club: " + club);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserAvatar;
        TextView tvUserName;
        TextView tvUserEmail;
        TextView tvUserDepartment;
        TextView tvUserClub;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserAvatar = itemView.findViewById(R.id.tvUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserDepartment = itemView.findViewById(R.id.tvUserDepartment);
            tvUserClub = itemView.findViewById(R.id.tvUserClub);
        }
    }
}
