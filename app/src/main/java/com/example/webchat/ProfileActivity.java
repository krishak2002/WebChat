package com.example.webchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView userProfileImage;
    TextView userProfileName,userProfileStatus;
    Button sendMessageRequestButton,declineMessageRequestButton;
    private DatabaseReference userRef,chatRequestRef,contactsRef,notificationRef;
    FirebaseAuth mAuth;

    String receiveUserId,current_state,senderUserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth=FirebaseAuth.getInstance();

        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        chatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
      notificationRef= FirebaseDatabase.getInstance().getReference().child("Notifications");


        userProfileImage =findViewById(R.id.visit_profile_image);
        userProfileName =findViewById(R.id.visit_profile_name);
        userProfileStatus=findViewById(R.id.visit_profile_status);
        sendMessageRequestButton =(Button)findViewById(R.id.send_message_request_button);
        declineMessageRequestButton=(Button)findViewById(R.id.decline_message_request_button);

        senderUserID=mAuth.getCurrentUser().getUid();
        receiveUserId =getIntent().getExtras().get("visitUserId").toString();

        current_state ="new";


        retrieveUserInfo();

    }

    private void retrieveUserInfo() {

        userRef.child(receiveUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {

                if((snapshot.exists()) &&  snapshot.child("image").exists()){

                    String userImage =snapshot.child("image").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();
                    String userName = snapshot.child("name").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profileimage).into(userProfileImage);

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequests();


                }
                else if((snapshot.exists())){
                    String userStatus = snapshot.child("status").getValue().toString();
                    String userName = snapshot.child("name").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    manageChatRequests();

                }
                else{

                    Toast.makeText(ProfileActivity.this, "Request Failed", Toast.LENGTH_SHORT).show();
                }
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void manageChatRequests() {

        chatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.hasChild(receiveUserId)){

                   String requestType = snapshot.child(receiveUserId).child("request_type").getValue().toString();

                   if(requestType.equals("Sent")){

                       sendMessageRequestButton.setText("Cancel Chat Request");
                       current_state=" request_sent";

                   }
                   else if(requestType.equals("received")){
                       current_state ="request_received";
                       sendMessageRequestButton.setText("Accept Chat Request");
                       declineMessageRequestButton.setVisibility(View.VISIBLE);
                       declineMessageRequestButton.setEnabled(true);
                       declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {

                           }
                       });
                   }

                }
                else {

                    contactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                  current_state= "new";
                        sendMessageRequestButton.setText("Send Message");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(!senderUserID.equals(receiveUserId)){

            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  //  sendMessageRequestButton.setEnabled(false);

                    if(current_state.equals("new")){

                        sendChatRequest();
                    }
                    if(current_state.equals("request_sent")){

                        cancelChatRequests();

                    }

                    if(current_state.equals("request_received")){

                        acceptChatRequests();

                    }

                    if(current_state.equals("friends")){

                       removeSpecificContact();

                    }
                }
            });

        }
        else{
           // Toast.makeText(this, "you r same", Toast.LENGTH_SHORT).show();
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void removeSpecificContact() {
        contactsRef.child(senderUserID).child(receiveUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull  Task<Void> task) {
                if(task.isSuccessful()){
                    contactsRef.child(receiveUserId).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                sendMessageRequestButton.setEnabled(true);
                                current_state = "new";
                                sendMessageRequestButton.setText("Send Message");
                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                declineMessageRequestButton.setEnabled(false);
                            }
                        }
                    });

                }
            }
        });
    }

    private void acceptChatRequests() {
contactsRef.child(senderUserID).child(receiveUserId).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
    @Override
    public void onComplete(@NonNull Task<Void> task) {

        if (task.isSuccessful()){

            contactsRef.child(receiveUserId).child(receiveUserId).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()){

                        chatRequestRef.child(senderUserID).child(receiveUserId).child("request_type").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull  Task<Void> task) {
                                sendMessageRequestButton.setText("Send Message");
                                if(task.isSuccessful()){

                                    chatRequestRef.child(receiveUserId).child(senderUserID).child("request_type").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                             sendMessageRequestButton.setEnabled(true);
                                             current_state="friends";
                                                     sendMessageRequestButton.setText("Send Message");
                                                     declineMessageRequestButton.setEnabled(false);
                                                     declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        }
    }
});

    }

    private void cancelChatRequests() {
        chatRequestRef.child(senderUserID).child(receiveUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull  Task<Void> task) {
                if(task.isSuccessful()){
                    chatRequestRef.child(receiveUserId).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                sendMessageRequestButton.setEnabled(true);
                                current_state = "new";
                                sendMessageRequestButton.setText("Send Message");
                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                declineMessageRequestButton.setEnabled(false);
                            }
                        }
                    });

                }
            }
        });
    }


    private void sendChatRequest() {

chatRequestRef.child(senderUserID).child(receiveUserId).child("request_type").setValue("Sent").addOnCompleteListener(new OnCompleteListener<Void>() {
    @Override
    public void onComplete(@NonNull Task<Void> task) {

        if(task.isSuccessful()){

            chatRequestRef.child(receiveUserId).child(senderUserID).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull  Task<Void> task) {

                    if(task.isSuccessful()){
                        HashMap<String,String> chatNotificationMap = new HashMap<>();
                        chatNotificationMap.put("from",senderUserID );
                        chatNotificationMap.put("type","request");

                        notificationRef.child(receiveUserId).push().setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull  Task<Void> task) {
                                if ((task.isSuccessful())){

                                    sendMessageRequestButton.setEnabled(true);
                                    current_state="request_sent";
                                    sendMessageRequestButton.setText("Cancel Chat Request");

                                }
                            }
                        });



                }}
            });
        }

    }
});
    }
}
