package com.example.webchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
Toolbar mToolbar;
RecyclerView FindFriendsRecyclerList;
private DatabaseReference usersRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        FindFriendsRecyclerList =(RecyclerView)findViewById(R.id.find_friends_recycler_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        mToolbar= (Toolbar)findViewById(R.id.find_friends_toolbar);

        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<contacts> options =new FirebaseRecyclerOptions.Builder<contacts>()
                .setQuery(usersRef,contacts.class)
                .build();

FirebaseRecyclerAdapter<contacts,FindFriendsViewHolder> adapter = new FirebaseRecyclerAdapter<contacts, FindFriendsViewHolder>(options) {
    @Override
    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull contacts model) {

        holder.userName.setText(model.getName());
        holder.userStatus.setText(model.getStatus());
        Picasso.get().load(model.getImage()).placeholder(R.drawable.profileimage).into(holder.profileImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String visitUserId = getRef(position).getKey();

                Intent profileIntent =new Intent(FindFriendsActivity.this,ProfileActivity.class);
               profileIntent.putExtra("visitUserId",visitUserId);
                startActivity(profileIntent);

            }
        });
    }

    @NonNull

    @Override
    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
        FindFriendsViewHolder viewHolder =new FindFriendsViewHolder(view);
        return  viewHolder;
    }
};
FindFriendsRecyclerList.setAdapter(adapter);
adapter.startListening();
    }

    public class FindFriendsViewHolder extends RecyclerView.ViewHolder {


        TextView userName,userStatus;
        CircleImageView profileImage;
        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName =itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage =itemView.findViewById(R.id.users_profile_image);
        }
    }
}