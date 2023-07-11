package com.example.chatapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.example.chatapp.utilitie.Constants;
import com.example.chatapp.utilitie.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private RoundedImageView imgProfile;
    private EditText edName;
    private EditText edEmail;
    private EditText edPasswd;
    private EditText edConfirmPasswd;
    private Button btnSignUp;
    private TextView tvLogin, tv_selectImg;
    FrameLayout layoutImage;
    private ProgressBar progressBar;
    private String encodeImage;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        findID();
        preferenceManager = new PreferenceManager(getApplicationContext());
        tvLogin.setOnClickListener(view -> {
            onBackPressed();
        });
        layoutImage.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            launcher.launch(i);
        });
        btnSignUp.setOnClickListener(view -> {
            if (isValidate()) {
                signUp();
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void signUp() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, edName.getText().toString());
        user.put(Constants.KEY_EMAIL, edEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, edPasswd.getText().toString());
        user.put(Constants.KEY_IMAGE, encodeImage);
        database.collection(Constants.KEY_COLLECTION_USER)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SINGED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, edName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodeImage);
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);


                })
                .addOnFailureListener(e -> {
                    loading(false);
                    showToast(e.getMessage());
                });

    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() + previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) {
                Uri uri = result.getData().getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imgProfile.setImageBitmap(bitmap);
                    tv_selectImg.setVisibility(View.GONE);
                    encodeImage = encodeImage(bitmap);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    });

    private void loading(Boolean isloading) {
        if (isloading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSignUp.setVisibility(View.INVISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            btnSignUp.setVisibility(View.VISIBLE);
        }
    }

    private Boolean isValidate() {
        if (imgProfile == null) {
            showToast("Select profile image");
            return false;
        } else if (edName.getText().toString().trim().isEmpty()) {
            showToast("Enter Name");
            return false;
        } else if (edEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter Email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(edEmail.getText().toString()).matches()) {
            showToast("Enter Valid Email");
            return false;
        } else if (edPasswd.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        } else if (edConfirmPasswd.getText().toString().trim().isEmpty()) {
            showToast("Enter confirm password");
            return false;
        } else if (!edConfirmPasswd.getText().toString().trim().equals(edPasswd.getText().toString().trim())) {
            showToast("Password confirm is incorrect");
            return false;
        } else {
            return true;
        }
    }

    private void findID() {

        imgProfile = (RoundedImageView) findViewById(R.id.img_profile);
        edName = (EditText) findViewById(R.id.ed_name);
        edEmail = (EditText) findViewById(R.id.ed_email);
        edPasswd = (EditText) findViewById(R.id.ed_passwd);
        edConfirmPasswd = (EditText) findViewById(R.id.ed_confirmPasswd);
        btnSignUp = (Button) findViewById(R.id.btn_signUp);
        tvLogin = (TextView) findViewById(R.id.tv_Login);
        progressBar = findViewById(R.id.progressbar);
        tv_selectImg = findViewById(R.id.tv_addImg);
        layoutImage = findViewById(R.id.layoutImage);

    }
}