package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.example.chatapp.utilitie.Constants;
import com.example.chatapp.utilitie.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LogInActivity extends AppCompatActivity {
    private EditText edEmail;
    private EditText edPasswd;
    private Button btnLogin;
    private TextView tvCreateAcount;
    private ProgressBar progressBar;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        findID();
        preferenceManager = new PreferenceManager(getApplicationContext());
        tvCreateAcount.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
        });
        btnLogin.setOnClickListener(view -> {
            if (isValidate()) {
                LogIn();
            }
        });
        if(preferenceManager.getBoolean(Constants.KEY_IS_SINGED_IN)){
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void LogIn() {
        isLoading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_EMAIL, edEmail.getText().toString().trim())
                .whereEqualTo(Constants.KEY_PASSWORD, edPasswd.getText().toString().trim())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
                        Log.d("infor", snapshot.getId());
                        preferenceManager.putBoolean(Constants.KEY_IS_SINGED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, snapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, snapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, snapshot.getString(Constants.KEY_IMAGE));
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);

                    } else {
                        isLoading(false);
                        showToast("Unable to sign in");
                    }
                });
    }

    private Boolean isValidate() {
        if (edEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter Email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(edEmail.getText().toString().trim()).matches()) {
            showToast("Enter valid Email");
            return false;
        } else if (edPasswd.getText().toString().isEmpty()) {
            showToast("Enter password");
            return false;
        } else {
            return true;
        }

    }

    private void isLoading(Boolean isloading) {
        if (isloading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.INVISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            btnLogin.setVisibility(View.VISIBLE);
        }
    }

    private void findID() {

        progressBar = findViewById(R.id.progressbar);
        edEmail = (EditText) findViewById(R.id.ed_email);
        edPasswd = (EditText) findViewById(R.id.ed_passwd);
        btnLogin = (Button) findViewById(R.id.btn_login);
        tvCreateAcount = (TextView) findViewById(R.id.tv_createAcount);

    }
}