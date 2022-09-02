package com.example.webchat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment {
 View privateChatsView;
RecyclerView chatsList;
DatabaseReference chatsRef,usersRef;
FirebaseAuth mAuth;

    public ChatsFragment() {

    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth =FirebaseAuth.getInstance();
        String currentUserId= mAuth.getCurrentUser().getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
         usersRef =FirebaseDatabase.getInstance().getReference().child("users");

        privateChatsView= inflater.inflate(R.layout.fragment_chats, container, false);
        chatsList =(RecyclerView)privateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));


        return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<contacts>options = new FirebaseRecyclerOptions.Builder<contacts>().setQuery(chatsRef,contacts.class).build();
        FirebaseRecyclerAdapter<contacts,chatsViewHolder>adapter = new FirebaseRecyclerAdapter<contacts, chatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatsFragment.chatsViewHolder holder, int position, @NonNull  contacts model) {

                final String userIDs = getRef(position).getKey();
                final String[] retImage = {"default image"};
             usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                 @Override
                 public void onDataChange(@NonNull  DataSnapshot snapshot) {
                        if(snapshot.exists()  && snapshot.hasChild("image")){

                             retImage[0] = snapshot.child("image").getValue().toString();
                            Picasso.get().load(retImage[0]).into(holder.profileImage);
                        }

                     final String  retUserName = snapshot.child("name").getValue().toString();
                     final String  retUserStatus = snapshot.child("status").getValue().toString();
                     holder.userName.setText(retUserName);
                     holder.userStatus.setText("Last seen:" +"\n"+ "Date" +" Time" );

                        if(snapshot.exists() && snapshot.child("userState").hasChild("state")){

                            String state = snapshot.child("userState").child("state").getValue().toString();
                            String time = snapshot.child("userState").child("time").getValue().toString();
                            String date = snapshot.child("userState").child("date").getValue().toString();

                       if (state.equals("online")){
                           holder.userStatus.setText("Online" );

                       }
                       else if (state.equals("offline")){
                           holder.userStatus.setText("Last seen:" +"\n"+ date + " "+time );
                       }
                        }
                        else{
                            holder.userStatus.setText("offline" );
                        }



                     holder.itemView.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                             chatIntent.putExtra("visit_user_id",userIDs);
                             chatIntent.putExtra("visit_user_name",retUserName);
                             chatIntent.putExtra("visit_image", retImage[0]);
                             startActivity(chatIntent);
                         }
                     });
                 }

                 @Override
                 public void onCancelled(@NonNull  DatabaseError error) {

                 }
             });




            }

            @NonNull

            @Override
            public chatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View  view =LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.users_display_layout,parent,false);
 return  new chatsViewHolder(view);


            }
        };
        chatsList.setAdapter(adapter);
        adapter.startListening();
    }

    private  static  class chatsViewHolder extends RecyclerView.ViewHolder{
        TextView userName , userStatus;
        CircleImageView profileImage;
        public chatsViewHolder(@NonNull  View itemView) {
            super(itemView);



            userName =(TextView)itemView.findViewById(R.id.user_profile_name);
            userStatus =(TextView)itemView.findViewById(R.id.user_status);
            profileImage =(CircleImageView)itemView.findViewById(R.id.users_profile_image);

        }
    }

}