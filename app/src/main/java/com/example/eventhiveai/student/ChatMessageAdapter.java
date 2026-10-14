package com.example.eventhiveai.student;

import android.graphics.Color;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.R;
import com.example.eventhiveai.models.ChatMessageModel;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;

    private final List<ChatMessageModel> messageList;

    public ChatMessageAdapter(List<ChatMessageModel> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_ai, parent, false);
            return new AiViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessageModel item = messageList.get(position);
        if (holder instanceof UserViewHolder) {
            UserViewHolder userHolder = (UserViewHolder) holder;
            userHolder.tvUserMessage.setText(item.getMessage());
            userHolder.tvUserTime.setText(item.getFormattedTime());
        } else if (holder instanceof AiViewHolder) {
            AiViewHolder aiHolder = (AiViewHolder) holder;
            aiHolder.tvAiMessage.setText(item.getMessage());
            aiHolder.tvAiMessage.setMovementMethod(LinkMovementMethod.getInstance());
            aiHolder.tvAiMessage.setHighlightColor(Color.TRANSPARENT);
            aiHolder.tvAiTime.setText(item.getFormattedTime());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserMessage;
        TextView tvUserTime;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserMessage = itemView.findViewById(R.id.tvUserMessage);
            tvUserTime = itemView.findViewById(R.id.tvUserTime);
        }
    }

    static class AiViewHolder extends RecyclerView.ViewHolder {
        TextView tvAiMessage;
        TextView tvAiTime;

        AiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAiMessage = itemView.findViewById(R.id.tvAiMessage);
            tvAiTime = itemView.findViewById(R.id.tvAiTime);
        }
    }
}
