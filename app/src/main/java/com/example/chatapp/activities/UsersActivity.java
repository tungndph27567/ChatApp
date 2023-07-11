package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chatapp.R;
import com.example.chatapp.adapters.UsersAdapter;
import com.example.chatapp.listener.UserListener;
import com.example.chatapp.models.User;
import com.example.chatapp.utilitie.Constants;
import com.example.chatapp.utilitie.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity implements UserListener {
    private List<User> list;
    private UsersAdapter adapter;
    private AppCompatImageView imgBack;
    private RecyclerView recUser;
    private ProgressBar progressbar;
    private PreferenceManager preferenceManager;
    private TextView tv_err;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        findId();
        preferenceManager = new PreferenceManager(this);
        imgBack.setOnClickListener(view -> {
            onBackPressed();
        });
        getUser();
    }

    private void showErrosMessage() {
        tv_err.setText(String.format("%s", "No user availble"));
        tv_err.setVisibility(View.VISIBLE);
    }
    private void getUser(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USER).get().addOnCompleteListener(task -> {
            loading(false);
            String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
            if(task.isSuccessful() && task.getResult()!=null){
                list = new ArrayList<>();
                for(QueryDocumentSnapshot snapshot: task.getResult()){
                    if(currentUserId.equals(snapshot.getId())){
                        continue;
                    }
                    User objUser = new User();
                    objUser.setName(snapshot.getString(Constants.KEY_NAME));
                    objUser.setEmail(snapshot.getString(Constants.KEY_EMAIL));
                    objUser.setImg(snapshot.getString(Constants.KEY_IMAGE));
                    objUser.setToken(snapshot.getString(Constants.KEY_FCM_TOKEN));
                    objUser.setId(snapshot.getId());
                    list.add(objUser);
                }
                if(list.size()>0){
                    adapter = new UsersAdapter(list,this);
                    recUser.setVisibility(View.VISIBLE);
                    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
                    recUser.setLayoutManager(linearLayoutManager);
                    recUser.addItemDecoration(dividerItemDecoration);
                    recUser.setAdapter(adapter);
                }else {
                    showErrosMessage();
                }
            }else {
                showErrosMessage();
            }
        });
    }

    private void findId() {
        imgBack = (AppCompatImageView) findViewById(R.id.img_back);
        recUser = (RecyclerView) findViewById(R.id.rec_user);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        tv_err = findViewById(R.id.tv_err);
    }

    @Override
    public void onClickedUser(User objUser) {
        Intent i = new Intent(getApplicationContext(),ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.KEY_USER,objUser);
        i.putExtras(bundle);
        startActivity(i);
        finish();

    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            progressbar.setVisibility(View.VISIBLE);
        } else {
            progressbar.setVisibility(View.INVISIBLE);
        }
    }
}