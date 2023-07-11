package com.example.chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.listener.ConversationListener;
import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.models.User;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class RecentConversionAdapter extends RecyclerView.Adapter<RecentConversionAdapter.RecentConversionViewHolder> {
    private List<ChatMessage> list;
    private final ConversationListener conversationListener;

    public RecentConversionAdapter(List<ChatMessage> list, ConversationListener conversationListener) {
        this.list = list;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public RecentConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_conversion, parent, false);
        return new RecentConversionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentConversionViewHolder holder, int position) {
        ((RecentConversionViewHolder) holder).setData(list.get(position));
        User user = new User();
        user.setId(list.get(position).getConversionId());
        user.setName(list.get(position).getConversionName());
        user.setImg(list.get(position).getConversionImg());

        holder.itemView.setOnClickListener(view -> {
            conversationListener.onConverSationListener(user);
        });
    }

    @Override
    public int getItemCount() {

        if (list.size() != 0) {
            return list.size();
        }
        return 0;
    }

    class RecentConversionViewHolder extends RecyclerView.ViewHolder {
        private RoundedImageView imgProfile;
        private View viewSupporter;
        private TextView tvNamee;
        private TextView tvRecentMessage;


        public RecentConversionViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = (RoundedImageView) itemView.findViewById(R.id.imgProfile);
            viewSupporter = (View) itemView.findViewById(R.id.viewSupporter);
            tvNamee = (TextView) itemView.findViewById(R.id.tv_namee);
            tvRecentMessage = (TextView) itemView.findViewById(R.id.tv_recentMessage);
        }

        void setData(ChatMessage chatMessage) {
            imgProfile.setImageBitmap(getConversionImg(chatMessage.getConversionImg()));
            tvNamee.setText(chatMessage.getConversionName());
            tvRecentMessage.setText(chatMessage.getMessage());
        }
    }

    private Bitmap getConversionImg(String encodedImg) {
        byte[] bytes = Base64.decode(encodedImg, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
