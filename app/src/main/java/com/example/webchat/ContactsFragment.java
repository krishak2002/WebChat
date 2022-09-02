package com.example.webchat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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


public class ContactsFragment extends Fragment {

    private View contactsView;
    RecyclerView myContactsList;
    private DatabaseReference contactsRef,usersRef;
    FirebaseAuth mAuth;
    String currentUserID;
    public ContactsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        mAuth =FirebaseAuth.getInstance();
        currentUserID =mAuth.getCurrentUser().getUid().toString();
        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
          usersRef =FirebaseDatabase.getInstance().getReference().child("users");



        // Inflate the layout for this fragment
        contactsView =inflater.inflate(R.layout.fragment_contacts, container, false);
        myContactsList =(RecyclerView)contactsView.findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return  contactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<contacts>().setQuery(contactsRef,contacts.class).build();
        FirebaseRecyclerAdapter<contacts,contactsViewHolder>adapter =new FirebaseRecyclerAdapter<contacts, contactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ContactsFragment.contactsViewHolder holder, int position, @NonNull  contacts model) {
               String userIDs = getRef(position).getKey();
               usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {

                       if(snapshot.exists()){
                           if(snapshot.exists() && snapshot.child("userState").hasChild("state")){

                               String state = snapshot.child("userState").child("state").getValue().toString();
                               String time = snapshot.child("userState").child("time").getValue().toString();
                               String date = snapshot.child("userState").child("date").getValue().toString();

                               if (state.equals("online")){
                                   holder.onlineIcon.setVisibility(View.VISIBLE);

                               }
                               else if (state.equals("offline")){
                                   holder.onlineIcon.setVisibility(View.INVISIBLE);
                               }
                           }
                           else{
                               holder.onlineIcon.setVisibility(View.INVISIBLE);
                           }

                       }

                       if((snapshot.exists()) && (snapshot.hasChild("image"))){

                           String userImage = snapshot.child("image").getValue().toString();
                           String profileName = snapshot.child("name").getValue().toString();
                           String profileStatus = snapshot.child("status").getValue().toString();

                           holder.userName.setText(profileName);
                           holder.userStatus.setText(profileStatus);
                           Picasso.get().load(userImage).placeholder(R.drawable.profileimage).into(holder.profileImage);
                       }
                       else if(snapshot.exists()){
                           String profileName = snapshot.child("name").getValue().toString();
                           String profileStatus = snapshot.child("status").getValue().toString();

                           holder.userName.setText(profileName);
                           holder.userStatus.setText(profileStatus);

                       }
                   }

                   @Override
                   public void onCancelled(@NonNull  DatabaseError error) {

                   }
               });
            }

            @NonNull

            @Override
            public contactsViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {

                View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent,false);
                contactsViewHolder viewHolder = new contactsViewHolder(view);
                return  viewHolder;
            }
        };
myContactsList.setAdapter(adapter);
adapter.startListening();

    }

    public  static  class  contactsViewHolder extends RecyclerView.ViewHolder{

        TextView userName ,userStatus;
        ImageView onlineIcon;
        CircleImageView profileImage;
        public contactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName =itemView.findViewById(R.id.user_profile_name);
            userStatus =itemView.findViewById(R.id.user_status);
            profileImage =itemView.findViewById(R.id.users_profile_image);
            onlineIcon = itemView.findViewById(R.id.user_online_status);

        }
    }
}