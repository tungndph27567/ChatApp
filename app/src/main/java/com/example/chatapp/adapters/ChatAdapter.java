package com.example.chatapp.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.models.ChatMessage;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatMessage> list;
    private final Bitmap receiverProfileImage;
    private String senderId;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> list, Bitmap receiverProfileImage, String senderId) {
        this.list = list;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new ChatAdapterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sent_messsage, parent, false));
        } else {
            return new ReceiverMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_received_message, parent, false));

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((ChatAdapterViewHolder) holder).setData(list.get(position));
        } else {
            ((ReceiverMessageViewHolder) holder).setData(list.get(position), receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        if(list!=null){
            return list.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).getSenderId().equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class ChatAdapterViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessage;
        private TextView tvDateTime;

        public ChatAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = (TextView) itemView.findViewById(R.id.tv_message);
            tvDateTime = (TextView) itemView.findViewById(R.id.tv_dateTime);
        }

        void setData(ChatMessage chatMessage) {
            tvMessage.setText(chatMessage.getMessage());
            tvDateTime.setText(chatMessage.getDateTime());
        }
    }

    static class ReceiverMessageViewHolder extends RecyclerView.ViewHolder {
        private RoundedImageView imgProfile;
        private TextView tvReceivedMessage;
        private TextView tvDateTime;

        public ReceiverMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = (RoundedImageView) itemView.findViewById(R.id.img_profile);
            tvReceivedMessage = (TextView) itemView.findViewById(R.id.tv_receivedMessage);
            tvDateTime = (TextView) itemView.findViewById(R.id.tv_dateTime);
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage) {
            tvReceivedMessage.setText(chatMessage.getMessage());
            tvDateTime.setText(chatMessage.getDateTime());
            imgProfile.setImageBitmap(receiverProfileImage);
        }

    }
}
