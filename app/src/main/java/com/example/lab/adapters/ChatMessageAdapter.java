package com.example.lab.adapters; // Your adapters package

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout; // Import LinearLayout
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab.R; // Your R file
import com.example.lab.models.ChatMessage; // Import ChatMessage model

import java.util.List;

// Adapter for displaying chat messages in a RecyclerView
public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    private Context context;
    private List<ChatMessage> messageList;

    // Constants for view types
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;

    // Constructor
    public ChatMessageAdapter(Context context, List<ChatMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        // Determine view type based on whether the message is from the user or AI
        ChatMessage message = messageList.get(position);
        return message.isUser() ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_USER) {
            // Inflate layout for user messages (e.g., right-aligned bubble)
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_chat_message, parent, false); // Using the same layout for now, will adjust styling
        } else {
            // Inflate layout for AI messages (e.g., left-aligned bubble)
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_chat_message, parent, false); // Using the same layout for now, will adjust styling
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        holder.textViewMessageContent.setText(message.getText());

        // Adjust layout parameters and background based on sender
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.textViewMessageContent.getLayoutParams();
        if (message.isUser()) {
            // Align user messages to the right
            params.gravity = android.view.Gravity.END;
            holder.textViewMessageContent.setBackgroundResource(R.drawable.background_message_user); // Set user bubble background
            holder.textViewMessageContent.setTextColor(context.getResources().getColor(android.R.color.white)); // Set text color for user bubble
        } else {
            // Align AI messages to the left
            params.gravity = android.view.Gravity.START;
            holder.textViewMessageContent.setBackgroundResource(R.drawable.background_message_ai); // Set AI bubble background
            holder.textViewMessageContent.setTextColor(context.getResources().getColor(android.R.color.black)); // Set text color for AI bubble
        }
        holder.textViewMessageContent.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder for chat message items
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessageContent;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessageContent = itemView.findViewById(R.id.textViewMessageContent);
        }
    }

    // Method to add a new message and update the RecyclerView
    public void addMessage(ChatMessage message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1); // Notify adapter about the new item
    }

    // Method to update the entire message list (if needed)
    public void setMessageList(List<ChatMessage> messageList) {
        this.messageList = messageList;
        notifyDataSetChanged();
    }
}
