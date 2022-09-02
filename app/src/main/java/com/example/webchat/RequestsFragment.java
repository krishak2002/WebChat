package com.example.webchat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsFragment extends Fragment {

View requestsFragmentView;
private RecyclerView myRequestsList;
private DatabaseReference chatRequestRef, usersRef,contactsRef;
private FirebaseAuth mAuth;
String currentUserId;

    public RequestsFragment() {

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment

        usersRef =FirebaseDatabase.getInstance().getReference().child("users");
        requestsFragmentView= inflater.inflate(R.layout.fragment_requests, container, false);
        contactsRef =FirebaseDatabase.getInstance().getReference().child("Contacts");
         chatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
          mAuth=FirebaseAuth.getInstance();
         currentUserId =mAuth.getCurrentUser().getUid();
         myRequestsList =(RecyclerView)requestsFragmentView.findViewById(R.id.chat_requests_list);
         myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return  requestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<contacts>options =new FirebaseRecyclerOptions.Builder<contacts>().
                setQuery(chatRequestRef.child(currentUserId),contacts.class).build();

        FirebaseRecyclerAdapter<contacts,requestsViewHolder>adapter =new FirebaseRecyclerAdapter<contacts, requestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull  RequestsFragment.requestsViewHolder holder, int position, @NonNull contacts model) {
             holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                String list_user_Id =getRef(position).getKey();
                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {


                        if(snapshot.exists()){
                            String type =snapshot.getValue().toString();
                            if(type.equals("received")){

                                usersRef.child(list_user_Id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull  DataSnapshot snapshot) {

                                        if(snapshot.exists() && snapshot.hasChild("image")){

                                            String requestUserName = snapshot.child("name").getValue().toString();
                                            String requestUserStatus = snapshot.child("status").getValue().toString();
                                            String requestUserProfileImage = snapshot.child("image").getValue().toString();

                                            holder.userName.setText(requestUserName);
                                            holder.userStatus.setText(requestUserStatus);
                                            Picasso.get().load(requestUserProfileImage).placeholder(R.drawable.profileimage).into(holder.profileImage);

                                        }
                                        else if (snapshot.exists()){

                                            String requestUserName = snapshot.child("name").getValue().toString();
                                            String requestUserStatus = snapshot.child("status").getValue().toString();


                                            holder.userName.setText(requestUserName);
                                            holder.userStatus.setText(requestUserStatus);

                                        }

                                   holder.itemView.findViewById(R.id.request_accept_btn).setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {


                         contactsRef.child(currentUserId).child(list_user_Id).child("Contact").
                        setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                          @Override
                     public void onComplete(@NonNull Task<Void> task) {

                              if(task.isSuccessful()){

                                  chatRequestRef.child(currentUserId).child(list_user_Id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                      @Override
                                      public void onComplete(@NonNull Task<Void> task) {

                                          if(task.isSuccessful()){
                                              chatRequestRef.child(list_user_Id).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                  @Override
                                                  public void onComplete(@NonNull Task<Void> task) {
                                                      Toast.makeText(getContext(), "New Contact Saved", Toast.LENGTH_SHORT).show();
                                                  }
                                              });

                                          }
                                      }
                                  });
                              }
                          }
                                });
                                       }
                                   });


                                   holder.itemView.findViewById(R.id.request_cancel_btn).setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           chatRequestRef.child(currentUserId).child(list_user_Id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                               @Override
                                               public void onComplete(@NonNull Task<Void> task) {

                                                   if(task.isSuccessful()){
                                                       chatRequestRef.child(list_user_Id).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                           @Override
                                                           public void onComplete(@NonNull Task<Void> task) {
                                                               Toast.makeText(getContext(), "Request Denied", Toast.LENGTH_SHORT).show();
                                                           }
                                                       });

                                                   }
                                               }
                                           });
                                       }
                                   });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull  DatabaseError error) {

                                    }
                                });

                            }

                            else if(type.equals("sent")){

                                Button request_sent_btn =holder.itemView.findViewById(R.id.request_accept_btn);
                                request_sent_btn.setText("Req Sent");
                                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);
                            }



                        }
                    }

                    @Override
                    public void onCancelled(@NonNull  DatabaseError error) {

                    }
                });
            }

            @NonNull

            @Override
            public requestsViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
               View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
               requestsViewHolder holder = new requestsViewHolder(view);
               return  holder;
            }
        };
        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }

    public class requestsViewHolder extends RecyclerView.ViewHolder{

        TextView userName ,userStatus;
        CircleImageView profileImage;
        Button AcceptButton ,CancelButton;
        public requestsViewHolder(@NonNull  View itemView) {
            super(itemView);

            userName =(TextView)itemView.findViewById(R.id.user_profile_name);
            userStatus=(TextView)itemView.findViewById(R.id.user_status);
            profileImage=(CircleImageView)itemView.findViewById(R.id.users_profile_image);
            AcceptButton =(Button)itemView.findViewById(R.id.request_accept_btn);
            CancelButton =(Button)itemView.findViewById(R.id.request_cancel_btn);
        }
    }
}