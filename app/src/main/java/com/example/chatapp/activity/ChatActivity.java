package com.example.chatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.chatapp.Network.ApiClient;
import com.example.chatapp.Network.ApiService;
import com.example.chatapp.R;
import com.example.chatapp.adapters.ChatAdapter;
import com.example.chatapp.databinding.ActivityChatBinding;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.user;
import com.example.chatapp.utilities.Constant;
import com.example.chatapp.utilities.PreferenceManager;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.PendingResult;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.PrimitiveIterator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {
    ActivityChatBinding binding;
    private user receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore firebaseFirestore;
    private Boolean isReceiverAvailable = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        init();
        listenMessage();
        binding.imageBack.setOnClickListener(v -> {
            onBackPressed();
        });
        binding.sendLayout.setOnClickListener(v -> {
            sendMessage();
        });


    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, getBitMapFromEncodeString(receiverUser.image), preferenceManager.getString(Constant.KEY_USER_ID));
        binding.chatRecycleView.setAdapter(chatAdapter);
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constant.KEY_SENDER_ID, preferenceManager.getString(Constant.KEY_USER_ID));
        message.put(Constant.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constant.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constant.KEY_TIMESTAMP, new Date());
        firebaseFirestore.collection(Constant.KEY_COLLECTION_CHAT).add(message);
        binding.inputMessage.setText(null);

        if (!isReceiverAvailable){
            try {
                JSONArray tokens=new JSONArray();
                tokens.put(receiverUser.token);
                JSONObject data=new JSONObject();
                data.put(Constant.KEY_USER_ID, preferenceManager.getString(Constant.KEY_USER_ID));
                data.put(Constant.KEY_NAME, preferenceManager.getString(Constant.KEY_NAME));
                data.put(Constant.KEY_FCM_TOKEN, preferenceManager.getString(Constant.KEY_FCM_TOKEN));
                data.put(Constant.KEY_MESSAGE,binding.inputMessage.getText().toString());
                JSONObject body=new JSONObject();
                body.put(Constant.REMOTE_MSG_DATA,data);
                body.put(Constant.REMOTE_MSG_REGISTRATION_IDS,tokens);

                sendNotification(body.toString());



            } catch (Exception e) {
                showToast(e.getMessage());
            }
        }
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(Constant.getRemoteMsgHeaders(), messageBody).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray result = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) result.get(0);
                                showToast(error.getString("error"));
                                return;

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    showToast("Notification Sent Successfully");

                } else {
                    showToast("Error: " + response.code());

                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void listenAvailabilityOfReceiver() {
        firebaseFirestore.collection(Constant.KEY_COLLECTION_USERS)
                .document(receiverUser.id).addSnapshotListener(ChatActivity.this, ((value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                if (value.getLong(Constant.KEY_AVAILABILITY) != null) {
                    int availability = Objects.requireNonNull(
                            value.getLong(Constant.KEY_AVAILABILITY))
                            .intValue();
                    isReceiverAvailable = availability == 1;
                }
                receiverUser.token = value.getString(Constant.KEY_FCM_TOKEN);
                if(receiverUser.image==null){
                    receiverUser.image=value.getString(Constant.KEY_IMAGE);
                    chatAdapter.setReceiverProfileImage(getBitMapFromEncodeString(receiverUser.image));
                    chatAdapter.notifyItemRangeInserted(0,chatMessages.size());

                }
            }
            if (isReceiverAvailable) {
                binding.textAvailability.setVisibility(View.VISIBLE);
            } else {
                binding.textAvailability.setVisibility(View.GONE);
            }


        }));
    }

    private void listenMessage() {
        firebaseFirestore.collection(Constant.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constant.KEY_SENDER_ID, preferenceManager.getString(Constant.KEY_USER_ID))
                .whereEqualTo(Constant.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        firebaseFirestore.collection(Constant.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constant.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constant.KEY_RECEIVER_ID, preferenceManager.getString(Constant.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constant.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constant.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constant.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constant.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constant.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecycleView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecycleView.setVisibility(View.VISIBLE);
        }
        binding.chatLoading.setVisibility(View.GONE);

    };


    private Bitmap getBitMapFromEncodeString(String encodedImage) {
        if (encodedImage!=null){
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }else {
            return null;
        }

    }

    private void loadReceiverDetails() {
        receiverUser = (user) getIntent().getSerializableExtra(Constant.KEY_USER);
        binding.textName.setText(receiverUser.name);


    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);

    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}