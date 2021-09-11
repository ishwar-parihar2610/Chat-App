package com.example.chatapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivityChatBinding;
import com.example.chatapp.model.user;
import com.example.chatapp.utilities.Constant;

import java.util.PrimitiveIterator;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    private user receiverUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        binding.imageBack.setOnClickListener(v -> {
            onBackPressed();
        });


    }
    private void loadReceiverDetails() {
        receiverUser = (user) getIntent().getSerializableExtra(Constant.KEY_USER);
        binding.textName.setText(receiverUser.name);


    }

}