package com.example.chatapp.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivitySignUpBinding;
import com.example.chatapp.utilities.Constant;
import com.example.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    ActivitySignUpBinding binding;
    String encodedImage;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        preferenceManager=new PreferenceManager(getApplicationContext());
        binding.textViewSign.setOnClickListener(v -> startActivity(new Intent(SignUpActivity.this, SignInActivity.class)));
        binding.layoutImage.setOnClickListener(v->{
            Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
        binding.buttonSignUp.setOnClickListener(v -> {
            if (isValidSignUpDetails()) {
                signUp();
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void signUp() {
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        HashMap<String,Object> user=new HashMap<>();
        user.put(Constant.KEY_NAME,binding.inputName.getText().toString());
        user.put(Constant.KEY_EMAIL,binding.inputEmail.getText().toString());
        user.put(Constant.KEY_PASSWORD,binding.inputPassword.getText().toString());
        user.put(Constant.KEY_IMAGE,encodedImage);
        database.collection(Constant.KEY_COLLECTION_USERS).add(user).addOnSuccessListener(DocumentReference ->{
        loading(false);
        preferenceManager.putBoolean(Constant.KEY_IS_SIGNED_IN,true);
        preferenceManager.putString(Constant.KEY_USER_ID,DocumentReference.getId());
        preferenceManager.putString(Constant.KEY_NAME,binding.inputName.getText().toString());
        preferenceManager.putString(Constant.KEY_IMAGE,encodedImage);
        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        }).addOnFailureListener(exception ->{
        loading(false);
        showToast(exception.getMessage());
        });

    }
    private String encodedImage(Bitmap bitmap){
        int previewWidth=150;
        int previewHeight=bitmap.getHeight()+previewWidth/bitmap.getWidth();
        Bitmap previewBitmap=Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes=byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);

    }
    private final ActivityResultLauncher<Intent> pickImage=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {
       if (result.getResultCode()==RESULT_OK){
           if (result.getData()!=null){
               Uri imageUri=result.getData().getData();
               try {
                   InputStream inputStream=getContentResolver().openInputStream(imageUri);
                   Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                   binding.profileImage.setImageBitmap(bitmap);
                   binding.addImageTv.setVisibility(View.GONE);
                   encodedImage=encodedImage(bitmap);
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }
       }
    });

    private Boolean isValidSignUpDetails() {
        if (encodedImage == null) {
            showToast("Select Profile Picture");
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Enter Name");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter Email");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter Valid Email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter Password");
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirm Your Password");
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Password & Confirm Password must be same");
            return false;
        } else {
            return true;
        }
        return true;

    }
    private void loading(Boolean isLoading){
        if (isLoading){
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.buttonSignUp.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}