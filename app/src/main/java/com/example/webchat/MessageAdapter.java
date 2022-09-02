package com.example.webchat;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


     public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.messageViewholder>
     {


     private List<Messages> userMessageList ;
     private FirebaseAuth mAuth;
     private DatabaseReference usersRef;

         public MessageAdapter(List<Messages> userMessageList) {
             this.userMessageList = userMessageList;
         }

         @NonNull

         @Override
         public messageViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
             View view = LayoutInflater.from(parent.getContext()).
                     inflate(R.layout.custom_messages_layout,parent,false);
          mAuth =FirebaseAuth.getInstance();
             return new messageViewholder(view);

         }

         @Override
         public void onBindViewHolder(@NonNull  messageViewholder holder, int position) {
           String messageSenderID =mAuth.getCurrentUser().getUid();
           Messages messages =userMessageList.get(position);
           String fromUserID=messages.getFrom();
           String fromMessageType = messages.getType();

           usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(fromUserID);
           usersRef.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull  DataSnapshot snapshot) {

                   if(snapshot.exists() && snapshot.hasChild("image")){
                       String receiveImage = snapshot.child("image").getValue().toString();

                       Picasso.get().load(receiveImage).placeholder(R.drawable.profileimage).into(holder.receiverProfileImage);

                   }

               }

               @Override
               public void onCancelled(@NonNull  DatabaseError error) {

               }
           });

             holder.receiverMessageText.setVisibility(View.INVISIBLE);
             holder.receiverProfileImage.setVisibility(View.INVISIBLE);
             holder.senderMessageText.setVisibility(View.INVISIBLE);
             holder.messageSenderPicture.setVisibility(View.INVISIBLE);
             holder.messageReceiverPicture.setVisibility(View.INVISIBLE);

             if(fromMessageType.equals("text")){

                 if(fromUserID.equals(messageSenderID)){

                     holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                     holder.senderMessageText.setTextColor(Color.BLACK);
                     holder.senderMessageText.setText(messages.getMessage() +"\n  \n"+ messages.getTime()+" - "+messages.getDate());
                     holder.senderMessageText.setVisibility(View.VISIBLE);

                 }

                 else{
                     holder.senderMessageText.setVisibility(View.INVISIBLE);

                     holder.receiverMessageText.setVisibility(View.VISIBLE);
                     holder.receiverProfileImage.setVisibility(View.VISIBLE);

                     holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                     holder.receiverMessageText.setTextColor(Color.BLACK);
                     holder.receiverMessageText.setText(messages.getMessage() +"\n  \n"+ messages.getTime()+" - "+messages.getDate());

                 }
             }
             else if (fromMessageType.equals("image")){

                 if(fromUserID.equals(messageSenderID)){
                    /* holder.receiverProfileImage.setVisibility(View.VISIBLE);
                     holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                     holder.messageReceiverPicture.setVisibility(View.VISIBLE);*/
                     holder.messageSenderPicture.setVisibility(View.VISIBLE);

                     Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);

                 }

                 else{

                     holder.receiverProfileImage.setVisibility(View.VISIBLE);
                     holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                     Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);

                 }
             }

             else{

             }


         }

         @Override
         public int getItemCount() {
             return userMessageList.size();
         }


         public class messageViewholder extends RecyclerView.ViewHolder{

           public   TextView senderMessageText,receiverMessageText;
           public   CircleImageView receiverProfileImage;
           ImageView messageSenderPicture,messageReceiverPicture;

             public messageViewholder(@NonNull  View itemView) {
                 super(itemView);

                 senderMessageText =(TextView)itemView.findViewById(R.id.sender_message_text);
                 receiverMessageText =(TextView)itemView.findViewById(R.id. receiver_message_text);
                 receiverProfileImage =(CircleImageView)itemView.findViewById(R.id.message_profile_image);
                 messageSenderPicture =(ImageView)itemView.findViewById(R.id.message_sender_image_view);
                 messageReceiverPicture =(ImageView)itemView.findViewById(R.id.message_receiver_image_view);
             }
         }
     }


