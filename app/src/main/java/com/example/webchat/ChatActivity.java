package com.example.webchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

     CircleImageView userImage;
    Toolbar chatToolbar;
    ProgressDialog loadingbar;
    TextView userName,userLastSeen;
    ImageButton sendMessageButton,sendFilesButton;
    String saveCurrentDate; String saveCurrentTime;
     EditText messageInputText;
         FirebaseAuth mAuth;
         DatabaseReference rootRef;
         String checker ="",myUrl="";
         StorageTask uploadTask;
         private Uri fileUri;
         private List<Messages> messagesList =new ArrayList<>();
         MessageAdapter messageAdapter;
         private LinearLayoutManager linearLayoutManager;
         RecyclerView user_MessagesList;

     String messageReceiverID,messageReceiverName,messageReceiverImage,messageSenderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        rootRef = FirebaseDatabase.getInstance().getReference();
      mAuth=FirebaseAuth.getInstance();
      messageSenderID =mAuth.getCurrentUser().getUid();

        messageReceiverImage=getIntent().getExtras().get("visit_image").toString();
        messageReceiverID =getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName= getIntent().getExtras().get("visit_user_name").toString();
      // Toast.makeText(this, messageReceiverID, Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, messageReceiverName, Toast.LENGTH_SHORT).show();

          loadingbar =new ProgressDialog(this);
        chatToolbar =(Toolbar)findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar =getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater =(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView =layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

           messageInputText =(EditText)findViewById(R.id.input_message);
           sendMessageButton =(ImageButton)findViewById(R.id.send_message_btn);
           sendFilesButton=(ImageButton)findViewById(R.id.send_files_btn);
        userImage =(CircleImageView)findViewById(R.id.custom_profile_image);
        userName =(TextView)findViewById(R.id.custom_profile_name);
        userLastSeen =(TextView)findViewById(R.id.custom_user_last_seen);


        messageAdapter = new MessageAdapter(messagesList);
        user_MessagesList =(RecyclerView)findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager =new LinearLayoutManager(this);
        user_MessagesList.setLayoutManager(linearLayoutManager);
        user_MessagesList.setAdapter(messageAdapter);


        Date c = Calendar.getInstance().getTime();


        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        saveCurrentDate = df.format(c);

        Calendar cc = Calendar.getInstance();
        int mHour = cc.get(Calendar.HOUR_OF_DAY);
        int mMinute = cc.get(Calendar.MINUTE);
        saveCurrentTime =(  String.format("%02d:%02d", mHour , mMinute));


        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profileimage).into(userImage);
        userName.setText(messageReceiverName);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              sendMessage();
            }
        });

                              displayLastSeenMessages();

         sendFilesButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 CharSequence  options [] =new CharSequence[]{

                         "Images",
                         "PDF Files",
                         "Ms Word Files"
                 };

                 AlertDialog.Builder builder =new AlertDialog.Builder(ChatActivity.this);
                 builder.setTitle("Select the File");
                 builder.setItems(options, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {

                         if(which ==0){
                               checker="image";
                               Intent intent = new Intent();
                               intent.setAction(Intent.ACTION_GET_CONTENT);
                               intent.setType("image/*");
                              startActivityForResult(intent.createChooser(intent,"Select image"),1);
                         }

                         if(which ==1){
                             checker="pdf";
                         }

                         if(which ==2){
                             checker="docx";
                         }

                     }
                 });
builder.show();
             }
         });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        loadingbar.setTitle("Sending file");
        loadingbar.setMessage("Please wait .....");
        loadingbar.setCanceledOnTouchOutside(false);
        loadingbar.show();

                    if (requestCode ==1  && data!=null && data.getData()!=null){
                      //  loadingbar.dismiss();
                        fileUri=data.getData();
                        if(!checker.equals("image")){
                            Toast.makeText(this, " object selected is not a image", Toast.LENGTH_SHORT).show();
                        }
                      else  if(checker.equals("image")){
                            // cc
                            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                            String messageSenderRef = "Message/"+messageSenderID+"/"+ messageReceiverID;
                            String messageReceiverRef = "Message/"+messageReceiverID+"/"+ messageSenderID;

                            DatabaseReference userMessageKeyRef = rootRef.child(("Message")).child(messageSenderID).
                                    child(messageReceiverID).push();

                            String messagePushID =userMessageKeyRef.getKey();

                            StorageReference filePath = storageReference.child(messagePushID+".jpg");
                            uploadTask =filePath.putFile(fileUri);
                            uploadTask.continueWithTask(new Continuation() {
                                @Override
                                public Object then(@NonNull  Task task) throws Exception {

                                    if(!task.isSuccessful()){
                                        loadingbar.dismiss();
                                        throw task.getException();
                                    }
                                    return filePath.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener <Uri>() {
                                @Override
                                public void onComplete(@NonNull  Task<Uri> task) {

                                    if(task.isSuccessful()){
                                        loadingbar.dismiss();
                                        Uri downloadUrl =  task.getResult();
                                        myUrl =downloadUrl.toString();

                                        Map messagePictureBody =new HashMap();
                                        messagePictureBody.put("message",myUrl);
                                        messagePictureBody.put("name",fileUri.getLastPathSegment());
                                        messagePictureBody.put("type",checker);
                                        messagePictureBody.put("from",messageSenderID);
                                        messagePictureBody.put("to",messageReceiverID);
                                        messagePictureBody.put("messageID",messagePushID);
                                        messagePictureBody.put("time",saveCurrentTime);
                                        messagePictureBody.put("date",saveCurrentDate);


                                        Map messageBodyDetails = new HashMap();

                                        messageBodyDetails.put(messageSenderRef+"/"+ messagePushID,messagePictureBody);
                                        messageBodyDetails.put(messageReceiverRef+"/"+messagePushID,messagePictureBody);

                                        rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull  Task task) {
                                                if(task.isSuccessful()){
                                                    loadingbar.dismiss();
                                                    Toast.makeText(ChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                                                }
                                                else{
                                                    loadingbar.dismiss();
                                                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                }

                                                messageInputText.setText("");
                                            }
                                        });
                                    }
                                }
                            });
                        }
                      else {
                            loadingbar.dismiss();
                            Toast.makeText(this, "No item selected", Toast.LENGTH_SHORT).show();
                        }
}
                    else{
                        loadingbar.dismiss();
                        Toast.makeText(this, "request failed", Toast.LENGTH_SHORT).show();
                    }

    }

    @Override
    protected void onStart() {
        super.onStart();

        rootRef.child("Message").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull  DataSnapshot snapshot, @Nullable  String previousChildName) {

                 Messages messages =snapshot.getValue(Messages.class);
                 messagesList.add(messages);
                 messageAdapter.notifyDataSetChanged();
                 user_MessagesList.smoothScrollToPosition(user_MessagesList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull  DataSnapshot snapshot, @Nullable  String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull  DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull  DataSnapshot snapshot, @Nullable  String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage() {

        String messageText = messageInputText.getText().toString();

        if(!TextUtils.isEmpty(messageText)){

             String messageSenderRef = "Message/"+messageSenderID+"/"+ messageReceiverID;
            String messageReceiverRef = "Message/"+messageReceiverID+"/"+ messageSenderID;

            DatabaseReference userMessageKeyRef = rootRef.child(("Message")).child(messageSenderID).
                    child(messageReceiverID).push();

            String messagePushID =userMessageKeyRef.getKey();

            Map messageTextBody =new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderID);
            messageTextBody.put("to",messageReceiverID);
            messageTextBody.put("messageID",messagePushID);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);


           Map messageBodyDetails = new HashMap();

           messageBodyDetails.put(messageSenderRef+"/"+ messagePushID,messageTextBody);
           messageBodyDetails.put(messageReceiverRef+"/"+messagePushID,messageTextBody);

         rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
             @Override
             public void onComplete(@NonNull  Task task) {
                 if(task.isSuccessful()){
                     Toast.makeText(ChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                 }
                 else{
                     Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                 }

                 messageInputText.setText("");
             }
         });
        }
    }

    private  void displayLastSeenMessages(){

        rootRef.child("users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.child("userState").hasChild("state")){

                    String state = snapshot.child("userState").child("state").getValue().toString();
                    String time = snapshot.child("userState").child("time").getValue().toString();
                    String date = snapshot.child("userState").child("date").getValue().toString();

                    if (state.equals("online")){
                       userLastSeen.setText("Online" );

                    }
                    else if (state.equals("offline")){
                       userLastSeen.setText("Last seen:" +"\n"+ date + " "+time );
                    }
                }
                else{
                    userLastSeen.setText("offline" );
                }

            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }
}