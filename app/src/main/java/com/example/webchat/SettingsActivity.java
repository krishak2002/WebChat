package com.example.webchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
Button updateAccountsSettings;
EditText userName,userStatus;
CircleImageView userProfileImage;
DatabaseReference RootRef;
String currentUserID;
FirebaseAuth mAuth;
StorageReference userProfileImageRef;
ProgressDialog loadingbar;
private static final  int galleryCode=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
          RootRef= FirebaseDatabase.getInstance().getReference();
          mAuth=FirebaseAuth.getInstance();
          userProfileImageRef = FirebaseStorage.getInstance().getReference().child(" profile images");
          currentUserID=mAuth.getCurrentUser().getUid();
            initializeFields();


            updateAccountsSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateSettings();
                }
            });

            RetrieveUserInfo();


            userProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        Intent galleryIntent =new Intent();
                        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                        galleryIntent.setType("image/*");
                        startActivityForResult(galleryIntent,galleryCode);
                }
            });


    }

    private void RetrieveUserInfo() {
        RootRef.child("users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.exists())&& (snapshot.hasChild("name")) && (snapshot.hasChild("image")))
                {
                  String retrieveUserName= snapshot.child("name").getValue().toString();
                    String retrieveStatus= snapshot.child("status").getValue().toString();
                    String retrieveImageUrl= snapshot.child("image").getValue().toString();

                    userStatus.setText(retrieveStatus);
                    userName.setText(retrieveUserName);
                    Picasso.get().load(retrieveImageUrl).into(userProfileImage);
                   // Glide.with(SettingsActivity.this).load(retrieveImageUrl).into(userProfileImage);
                }
                else if((snapshot.exists())&& (snapshot.hasChild("name"))){
                    String retrieveUserName= snapshot.child("name").getValue().toString();
                    userName.setText(retrieveUserName);
                    String retrieveStatus= snapshot.child("status").getValue().toString();
                    userStatus.setText(retrieveStatus);
                }
                else{
                    Toast.makeText(SettingsActivity.this, "Please fill your user Data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateSettings() {

        String setUserName = userName.getText().toString();
        String setUserStatus=userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setUserStatus)){

            Toast.makeText(this, "Please enter your status", Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String,Object> profilemap= new HashMap<>();
              profilemap.put("userID",currentUserID);
              profilemap.put("name",setUserName);
              profilemap.put("status",setUserStatus);

              RootRef.child("users").child(currentUserID).updateChildren(profilemap).addOnCompleteListener(new OnCompleteListener<Void>() {
                  @Override
                  public void onComplete(@NonNull Task<Void> task) {
                      if (task.isSuccessful()) {
                          sendUserToMainActivity();
                          Toast.makeText(SettingsActivity.this, "Profile updated Successfully", Toast.LENGTH_SHORT).show();
                      } else {
                                String message =task.getException().toString();
                          Toast.makeText(SettingsActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                      }
                  }
              });
        }
    }
    private void sendUserToMainActivity() {
        Intent Mainintent =new Intent(SettingsActivity.this,MainActivity.class);
        startActivity(Mainintent);

    }
    private void initializeFields() {

        updateAccountsSettings =(Button)findViewById(R.id.update_settings_button);
        userName =(EditText)findViewById(R.id.set_user_name);
        userStatus=(EditText)findViewById(R.id.set_profile_status);
        userProfileImage =(CircleImageView)findViewById(R.id.set_profile_image);
        loadingbar =new ProgressDialog(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode== galleryCode && resultCode ==RESULT_OK && data!=null){
            Uri imageUri =data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                loadingbar.setTitle("Set profile image");
                loadingbar.setMessage("Please wait while your profile image is uplaoading....");
                loadingbar.setCanceledOnTouchOutside(false);
                loadingbar.show();
                Uri resultUri = result.getUri();
                StorageReference filepath=userProfileImageRef.child(currentUserID+" .jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
           if(task.isSuccessful()){
         Toast.makeText(SettingsActivity.this, "Profile image uploaded to storage", Toast.LENGTH_SHORT).show();

            final  String downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();

            RootRef.child("users").child(currentUserID).child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull  Task<Void> task) {

                    if(task.isSuccessful()){
                        loadingbar.dismiss();
                        Toast.makeText(SettingsActivity.this, "Image saved in database successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        loadingbar.dismiss();
                        String message =task.getException().getMessage().toString();
                        Toast.makeText(SettingsActivity.this, "Eroor:"+ message, Toast.LENGTH_SHORT).show();
                    }

                }
            });
}
else{
    String message =task.getException().getMessage().toString();
    Toast.makeText(SettingsActivity.this, "Error :" + message, Toast.LENGTH_SHORT).show();
}
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}