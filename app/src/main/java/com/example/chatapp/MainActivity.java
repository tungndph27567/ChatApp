package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.activities.ChatActivity;
import com.example.chatapp.activities.LogInActivity;
import com.example.chatapp.activities.UsersActivity;
import com.example.chatapp.adapters.RecentConversionAdapter;
import com.example.chatapp.listener.ConversationListener;
import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.models.User;
import com.example.chatapp.utilitie.Constants;
import com.example.chatapp.utilitie.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ConversationListener {
    private RoundedImageView imgProfile;
    private AppCompatImageView imgLogout;
    private TextView tv_nameUser;
    private PreferenceManager preferenceManager;
    FloatingActionButton fab_newChat;
    private List<ChatMessage> list;
    private RecentConversionAdapter adapter;
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private RecyclerView recy_recentConversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferenceManager = new PreferenceManager(this);
        findId();
        loadDetailUser();
        getToken();
        imgLogout.setOnClickListener(view -> {
            logOut();
        });
        fab_newChat.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), UsersActivity.class));

        });
        list = new ArrayList<>();
        adapter = new RecentConversionAdapter(list, this);
        recy_recentConversation.setAdapter(adapter);
        listenerConversation();


    }

    private void loadDetailUser() {
        tv_nameUser.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        imgProfile.setImageBitmap(bitmap);

    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USER)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(unused -> {
                })
                .addOnFailureListener(e -> {
                });


    }

    private void listenerConversation() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setSenderId(senderId);
                    chatMessage.setReceiverId(receiverId);
                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.setConversionId(documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID));
                        chatMessage.setConversionImg(documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMG));
                        chatMessage.setConversionName(documentChange.getDocument().getString(Constants.KEY_RECEIVERNAME));
                    } else {
                        chatMessage.setConversionId(documentChange.getDocument().getString(Constants.KEY_SENDER_ID));
                        chatMessage.setConversionImg(documentChange.getDocument().getString(Constants.KEY_SENDER_IMG));
                        chatMessage.setConversionName(documentChange.getDocument().getString(Constants.KEY_SENDER_NAME));
                    }
                    chatMessage.setMessage(documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE));
                    chatMessage.setDateObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    list.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < list.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (list.get(i).getSenderId().equals(senderId) && list.get(i).getReceiverId().equals(receiverId)) {
                            list.get(i).setMessage(documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE));
                            list.get(i).setDateObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                            break;
                        }
                    }
                }
            }
            Collections.sort(list, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            adapter.notifyDataSetChanged();
            recy_recentConversation.smoothScrollToPosition(0);
            recy_recentConversation.setVisibility(View.VISIBLE);
        }
    };

    private void logOut() {
        showToast("Signing out");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USER)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        HashMap<String, Object> update = new HashMap<>();
        update.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(update).addOnSuccessListener(unused -> {
            preferenceManager.clear();
            startActivity(new Intent(this, LogInActivity.class));
            findId();
        }).addOnFailureListener(e -> {
            showToast("Unable to to sign out");
        });


    }


    private void findId() {
        imgProfile = (RoundedImageView) findViewById(R.id.imgProfile);
        imgLogout = (AppCompatImageView) findViewById(R.id.img_logout);
        tv_nameUser = findViewById(R.id.tv_nameUser);
        fab_newChat = findViewById(R.id.fab_newChat);
        recy_recentConversation = findViewById(R.id.recy_recentConversation);
    }

    @Override
    public void onConverSationListener(User user) {
        Intent i = new Intent(getApplicationContext(), ChatActivity.class);
        i.putExtra(Constants.KEY_USER, user);
        startActivity(i);
    }
}