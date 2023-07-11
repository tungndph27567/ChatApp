package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chatapp.R;
import com.example.chatapp.adapters.ChatAdapter;
import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.models.User;
import com.example.chatapp.utilitie.Constants;
import com.example.chatapp.utilitie.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    private View view;
    private AppCompatImageView imgBack;
    private AppCompatImageView imgInfor;
    private TextView tvName;
    private RecyclerView recyChat;
    private ProgressBar progressbar;
    private FrameLayout layoutSend;
    private AppCompatImageView imgSend;
    private EditText edTextMessage;
    private User receivedUser;
    private List<ChatMessage> list;
    private ChatAdapter adapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversationId = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        findId();

        imgBack.setOnClickListener(view1 -> {
            onBackPressed();
        });
        loadReceivedDetail();
        list = new ArrayList<>();
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
        adapter = new ChatAdapter(list, getBitmapFromEncodedString(receivedUser.getImg()),
                preferenceManager.getString(Constants.KEY_USER_ID));
        recyChat.setAdapter(adapter);
        imgSend.setOnClickListener(view1 -> {
            sendMessage();
        });
        listenMessager();
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receivedUser.getId());
        message.put(Constants.KEY_MESSAGE, edTextMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if (conversationId != null) {
            updateConversion(edTextMessage.getText().toString());

        } else {
            HashMap<String, Object> coversation = new HashMap<>();
            coversation.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            coversation.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            coversation.put(Constants.KEY_SENDER_IMG, preferenceManager.getString(Constants.KEY_IMAGE));
            coversation.put(Constants.KEY_RECEIVER_ID, receivedUser.getId());
            coversation.put(Constants.KEY_RECEIVERNAME, receivedUser.getName());
            coversation.put(Constants.KEY_RECEIVER_IMG, receivedUser.getImg());
            coversation.put(Constants.KEY_LAST_MESSAGE, edTextMessage.getText().toString());
            coversation.put(Constants.KEY_TIMESTAMP, new Date());
            addConversation(coversation);
        }
        edTextMessage.setText(null);

    }

    private void listenMessager() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receivedUser.getId())
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receivedUser.getId())
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

    }

    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = list.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setSenderId(documentChange.getDocument().getString(Constants.KEY_SENDER_ID));
                    chatMessage.setReceiverId(documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID));
                    chatMessage.setMessage(documentChange.getDocument().getString(Constants.KEY_MESSAGE));
                    chatMessage.setDateTime(getDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP)));
                    chatMessage.setDateObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    list.add(chatMessage);
                }
            }
            Collections.sort(list, (obj1, obj2) -> obj1.getDateObject().compareTo(obj2.dateObject));
            if (count == 0) {
                adapter.notifyDataSetChanged();
            } else {
                adapter.notifyItemRangeInserted(list.size(), list.size());
                recyChat.smoothScrollToPosition(list.size() - 1);
            }
            recyChat.setVisibility(View.VISIBLE);
        }
        progressbar.setVisibility(View.GONE);
        if (conversationId == null) {
            checkConversation();
        }

    });

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public String getDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void findId() {
        view = findViewById(R.id.view);
        imgBack = (AppCompatImageView) findViewById(R.id.img_back);
        imgInfor = (AppCompatImageView) findViewById(R.id.img_infor);
        tvName = (TextView) findViewById(R.id.tv_name);
        recyChat = (RecyclerView) findViewById(R.id.recy_chat);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        layoutSend = (FrameLayout) findViewById(R.id.layout_send);
        imgSend = (AppCompatImageView) findViewById(R.id.img_send);
        edTextMessage = (EditText) findViewById(R.id.ed_textMessage);

    }

    private void loadReceivedDetail() {
        receivedUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        tvName.setText(receivedUser.getName());
    }

    private void addConversation(HashMap<String, Object> conversation) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> {
                    conversationId = documentReference.getId();
                });
    }

    private void updateConversion(String message) {
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .document(conversationId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date()
        );

    }

    private void checkConversation() {
        if (list.size() != 0) {
            checkForConversationRemotely(preferenceManager.getString(Constants.KEY_USER_ID)
                    , receivedUser.getId());
            checkForConversationRemotely(receivedUser.getId()
                    , preferenceManager.getString(Constants.KEY_USER_ID));
        }
    }

    private void checkForConversationRemotely(String senderId, String receiverId) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionCompleteListener);

    }

    private final OnCompleteListener<QuerySnapshot> conversionCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

    }
}