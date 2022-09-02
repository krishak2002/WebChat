package com.example.webchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;


public class GroupChatActivity extends AppCompatActivity {
    DatabaseReference userRef,groupRef,groupMessageKeyRef;
    FirebaseAuth mAuth;
    String currentUserID,currentUserName;

Toolbar mToolbar;
TextView displayTextMessage;
ScrollView myScrollView;
EditText userMessageInput;
ImageButton sendMessageButton;
String currentGroupName,currentTime,currentDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        mAuth=FirebaseAuth.getInstance();
        currentUserID= mAuth.getCurrentUser().getUid();

        userRef=FirebaseDatabase.getInstance().getReference().child("users");
        currentGroupName =getIntent().getExtras().get("groupName").toString();
groupRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimarydark));
        }


        initializeFields();
        getUserInfo();
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInfoToDatabase();
                userMessageInput.setText("");
            }
        });


    }

    private void saveUserInfoToDatabase() {

        String message = userMessageInput.getText().toString();
        String messageKey = groupRef.push().getKey();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please write your message", Toast.LENGTH_SHORT).show();
        } else {
            Date c = Calendar.getInstance().getTime();
            currentTime = c.toString();

            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
            currentDate = df.format(c);

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupRef.updateChildren(groupMessageKey);
            groupMessageKeyRef=groupRef.child(messageKey);
            HashMap<String,Object>messageInfoMap=new HashMap<>();
            messageInfoMap.put("Username",currentUserName);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("message",message);
            messageInfoMap.put("time",currentTime);
groupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

    private void getUserInfo() {
        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currentUserName=snapshot.child("name").getValue().toString();

                }

            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull  DataSnapshot snapshot, @Nullable String previousChildName) {

                if(snapshot.exists()){
                    displayMessages(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    displayMessages(snapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayMessages(DataSnapshot snapshot) {

        Iterator iterator = snapshot.getChildren().iterator();

        while(iterator.hasNext()){

            String chatuserName  = ((DataSnapshot)iterator.next()).getValue().toString();

            String chatDate  = ((DataSnapshot)iterator.next()).getValue().toString();
            String chatMessage  = ((DataSnapshot)iterator.next()).getValue().toString();
            String chatTime  = ((DataSnapshot)iterator.next()).getValue().toString();
            displayTextMessage.append(chatuserName+ ":\n"+ chatMessage+"\n"+ chatDate+"     "+chatTime+"\n\n\n");
        }
    }

    private void initializeFields() {
        mToolbar=(Toolbar)findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);
        mToolbar.setTitleTextColor(Color.WHITE);
        displayTextMessage =(TextView)findViewById(R.id.group_chat_text_display);
        myScrollView=(ScrollView)findViewById(R.id.my_scroll_view);
        userMessageInput=(EditText)findViewById(R.id.input_group_message);
        sendMessageButton=(ImageButton)findViewById(R.id.send_message_button);

    }
}