 package com.example.chatapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.chatapp.Listeners.UserListeners;
import com.example.chatapp.R;
import com.example.chatapp.adapters.UsersAdapter;
import com.example.chatapp.databinding.ActivityMainBinding;
import com.example.chatapp.databinding.UserListBinding;
import com.example.chatapp.model.user;
import com.example.chatapp.utilities.Constant;
import com.example.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends BaseActivity implements UserListeners {
    ActivityMainBinding binding;
    private PreferenceManager preferenceManager;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=DataBindingUtil.setContentView(this, R.layout.activity_main);
        preferenceManager=new PreferenceManager(getApplicationContext());
        loadUserDetails();
        getToken();
        getUser();

        binding.imageSignOut.setOnClickListener(v -> {
            signOut();
        });
       /* binding.imageProfile.setOnClickListener(v -> {
            adapter.showProfileDialogue(Constant.KEY_IMAGE);
        });*/
    }

    private void loadUserDetails(){
        binding.textName.setText(preferenceManager.getString(Constant.KEY_NAME));
        byte[] bytes= Base64.decode(preferenceManager.getString(Constant.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);

        
    }
    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void updateToken(String token){
        preferenceManager.putString(Constant.KEY_FCM_TOKEN,token);
        FirebaseFirestore dataBase=FirebaseFirestore.getInstance();
        DocumentReference documentReference=dataBase.collection(Constant.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constant.KEY_USER_ID));
        documentReference.update(Constant.KEY_FCM_TOKEN,token).addOnSuccessListener(unused -> {
           showToast("Token updated successFully");
        }).addOnFailureListener(e -> {
           showToast("unable to update token");
        });
    }

    private void signOut(){
        showToast("Signing out...");
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        DocumentReference documentReference=database.collection(Constant.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constant.KEY_USER_ID));
        HashMap<String,Object> update=new HashMap<>();
        update.put(Constant.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(update)
                .addOnSuccessListener(unused -> {
                   preferenceManager.Clear();
                   startActivity(new Intent(getApplicationContext(),SignInActivity.class));
                }).addOnFailureListener(e ->{
           showToast("Unable to Sign Out");
        });

    }

    private void loading(Boolean isLoading){
        if (isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
    private void getUser(){
        loading(true);
        FirebaseFirestore dataBase=FirebaseFirestore.getInstance();
        dataBase.collection(Constant.KEY_COLLECTION_USERS).get().addOnCompleteListener(task -> {
          loading(false);
          String currentUser=preferenceManager.getString(Constant.KEY_USER_ID);
          if (task.isSuccessful() && task.getResult() !=null){
              ArrayList<user> users=new ArrayList<>();
              for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                  if (currentUser.equals(queryDocumentSnapshot.getId())){
                      continue;
                  }
                  user user=new user();
                  user.name=queryDocumentSnapshot.getString(Constant.KEY_NAME);
                  user.email=queryDocumentSnapshot.getString(Constant.KEY_EMAIL);
                  user.image=queryDocumentSnapshot.getString(Constant.KEY_IMAGE);
                  user.token=queryDocumentSnapshot.getString(Constant.KEY_FCM_TOKEN);
                  user.id=queryDocumentSnapshot.getId();
                  users.add(user);

              }
              if (users.size()>0){
                  UsersAdapter usersAdapter=new UsersAdapter(users,this);
                  binding.userListRecycleView.setAdapter(usersAdapter);
                  binding.userListRecycleView.setVisibility(View.VISIBLE);
              }
          }
        });
    }

    @Override
    public void onUserClick(user user) {
        Intent intent=new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constant.KEY_USER,user);
        startActivity(intent);
       
    }
}