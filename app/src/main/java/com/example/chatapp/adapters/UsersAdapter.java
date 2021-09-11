package com.example.chatapp.adapters;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Listeners.UserListeners;
import com.example.chatapp.R;
import com.example.chatapp.databinding.UserListBinding;
import com.example.chatapp.model.user;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.viewHolder> {
    LayoutInflater inflater;
    ArrayList<user> userList;
    Context context;
    private final UserListeners userListeners;

    public UsersAdapter(ArrayList<user> userList, UserListeners userListeners) {
        this.userList = userList;
        this.userListeners = userListeners;
    }

    /*public UsersAdapter(Context context) {
        this.context=context;
    }*/

    @Override
    public UsersAdapter.viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (inflater == null) {
            inflater = LayoutInflater.from(parent.getContext());
        }
        return new viewHolder(DataBindingUtil.inflate(inflater, R.layout.user_list, parent, false));

    }

    @Override
    public void onBindViewHolder(UsersAdapter.viewHolder holder, int position) {
        user user = userList.get(position);
        holder.binding.textName.setText(user.name);
        holder.binding.imageProfile.setImageBitmap(getUserImage(user.image));
        holder.binding.textEmail.setText(user.email);
        holder.binding.getRoot().setOnClickListener(v -> userListeners.onUserClick(user));
        holder.binding.imageProfile.setOnClickListener(v -> {
            showProfileDialogue(user.image);
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        UserListBinding binding;

        public viewHolder(UserListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    private Bitmap getUserImage(String encodeImage) {
        byte[] bytes = Base64.decode(encodeImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

    }

    public void showProfileDialogue(String image) {
        AlertDialog.Builder alert = new AlertDialog.Builder(inflater.getContext());
        View mView = inflater.inflate(R.layout.image_layout, null);
        ImageView imageView = mView.findViewById(R.id.userProfileView);
        imageView.setImageBitmap(getUserImage(image));
        alert.setView(mView);
        AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();


    }
}
