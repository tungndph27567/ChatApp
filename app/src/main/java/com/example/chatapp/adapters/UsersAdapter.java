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
import com.example.chatapp.listener.UserListener;
import com.example.chatapp.models.User;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersAdapterViewHolder> {
    private List<User> list;
    private  final UserListener userListener;

    public UsersAdapter(List<User> list, UserListener userListener) {
        this.list = list;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UsersAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user,parent,false);
        return new UsersAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapterViewHolder holder, int position) {
        User objUser  = list.get(position);
        holder.tvNamee.setText(objUser.getName());
        holder.tvEmail.setText(objUser.getEmail());
        holder.imgProfile.setImageBitmap(getUserImage(objUser.getImg()));
        holder.itemView.setOnClickListener(view -> {
            userListener.onClickedUser(objUser);
        });


    }

    @Override
    public int getItemCount() {
        if(list!=null){
            return list.size();
        }
        return 0;
    }
    private Bitmap getUserImage(String encodedImg){
        byte[] bytes = Base64.decode(encodedImg,Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        return bitmap;
    }
    class UsersAdapterViewHolder extends RecyclerView.ViewHolder {
        private RoundedImageView imgProfile;
        private View viewSupporter;
        private TextView tvNamee;
        private TextView tvEmail;
        public UsersAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = (RoundedImageView) itemView.findViewById(R.id.imgProfile);
            viewSupporter = (View) itemView.findViewById(R.id.viewSupporter);
            tvNamee = (TextView) itemView.findViewById(R.id.tv_namee);
            tvEmail = (TextView) itemView.findViewById(R.id.tvEmail);
        }
    }
}
