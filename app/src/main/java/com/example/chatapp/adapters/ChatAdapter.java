package com.example.chatapp.adapters;

import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.databinding.ReceivedMessageLayoutBinding;
import com.example.chatapp.databinding.SendMessageLayoutBinding;
import com.example.chatapp.model.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final List<ChatMessage> chatMessages;
    private final Bitmap receiverProfileImage;
    private final String senderID;
    private LayoutInflater inflater;
    public static final int VIEW_TYPE_SENT=1;
    public static final int VIEW_TYPE_RECEIVED=2;


    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderID) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderID = senderID;
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType==VIEW_TYPE_SENT){
            if (inflater==null){
                inflater=LayoutInflater.from(parent.getContext());
            }
            return new sentMessageViewHolder(DataBindingUtil.inflate(inflater, R.layout.send_message_layout,parent,false));
        }else {
            if (inflater==null){
                inflater=LayoutInflater.from(parent.getContext());
            }

        }
        return new ReceiverMessageViewHolder(DataBindingUtil.inflate(inflater,R.layout.received_message_layout,parent,false));
    }

    @Override
    public void onBindViewHolder( RecyclerView.ViewHolder holder, int position) {
    if (getItemViewType(position)==VIEW_TYPE_SENT){
        ((sentMessageViewHolder) holder).setData(chatMessages.get(position));
    }else {
        ((ReceiverMessageViewHolder) holder).setData(chatMessages.get(position),receiverProfileImage);
    }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderID)){
            return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class sentMessageViewHolder extends RecyclerView.ViewHolder {

        SendMessageLayoutBinding binding;

        public sentMessageViewHolder(SendMessageLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage chatMessage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);

        }
    }

    static class ReceiverMessageViewHolder extends RecyclerView.ViewHolder {
        ReceivedMessageLayoutBinding binding;

        public ReceiverMessageViewHolder(ReceivedMessageLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverImageProfile) {
            binding.textReceiveMessage.setText(chatMessage.message);
            binding.textReceiveMessageDateAndTime.setText(chatMessage.dateTime);
            binding.senderImageProfile.setImageBitmap(receiverImageProfile);
        }
    }

}
