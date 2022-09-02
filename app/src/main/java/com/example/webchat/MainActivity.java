package com.example.webchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
Toolbar mToolbar;
ViewPager myViewPager;
TabLayout myTabLayout;
    private FirebaseAuth mAuth;
TabsAccessorMethod myTabsAccessorMethod;
     String saveCurrentDate; String saveCurrentTime;

  //FirebaseUser currentFirebaseUser;
String currentUserID;
DatabaseReference RootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
RootRef = FirebaseDatabase.getInstance().getReference();
        mAuth =FirebaseAuth.getInstance();

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimarydark));
        }

        setContentView(R.layout.activity_main);
                 
        mToolbar=(Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle("WebChat");

        myViewPager =(ViewPager)findViewById(R.id.main_tabs_pager);
        myTabsAccessorMethod=new TabsAccessorMethod(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorMethod);

        myTabLayout =(TabLayout)findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();

      FirebaseUser  currentFirebaseUser=mAuth.getCurrentUser();

        if(currentFirebaseUser == null){
            sendUserToLoginActivity();
        }
        else{
            updateUserStatus("online");
                VerifyUserExistence();

    }
    }

    @Override
    protected void onStop() {
        super.onStop();

       FirebaseUser currentFirebaseUser=mAuth.getCurrentUser();

        if(currentFirebaseUser != null){
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

      FirebaseUser  currentFirebaseUser=mAuth.getCurrentUser();

        if(currentFirebaseUser != null){

            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistence() {
        String CurrentUserid = mAuth.getCurrentUser().getUid();
        RootRef.child("users").child(CurrentUserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
              if((snapshot.child("name").exists())){
                  Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
              }
              else{
                  Toast.makeText(MainActivity.this, "Please Add Your Name", Toast.LENGTH_SHORT).show();
                  sendUserToSettingsActivity();
              }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void sendUserToLoginActivity() {
        Toast.makeText(this, "yes", Toast.LENGTH_SHORT).show();
        Intent Loginintent =new Intent(MainActivity.this,LoginActivity.class);
               startActivity(Loginintent);
               finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);

         if(item.getItemId()==R.id.main_logout_option){

             updateUserStatus("offline");
             FirebaseAuth.getInstance().signOut();
             Intent loginintent =new Intent(MainActivity.this,LoginActivity.class);
             startActivity(loginintent);
             finish();
         }

        if(item.getItemId()==R.id.main_find_friends_option){
sendUserToFindFriendsActivity();
        }

        if(item.getItemId()==R.id.main_create_group_option){
            requestNewGroup();

        }

        if(item.getItemId()==R.id.main_settings_option){
                  sendUserToSettingsActivity();
        }
        return true;
    }

    private void requestNewGroup() {


        AlertDialog.Builder builder =new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");
        final EditText groupNameField =new EditText(MainActivity.this);

        groupNameField.setHint("eg: WebChat group");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String groupName =groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName)){

                    Toast.makeText(MainActivity.this, "Please write the group Name", Toast.LENGTH_SHORT).show();

                }
                else{
                          createNewGroup(groupName);
                }
            }
        });


        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


             dialog.dismiss();
            }
        });
       // builder.show();
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimarydark));
        dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimarydark));

    }

    private void createNewGroup(String groupName) {
        RootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull  Task<Void> task) {

                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, groupName+": created successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendUserToSettingsActivity() {

        Intent settingsintent =new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingsintent);


    }
    private void sendUserToFindFriendsActivity() {

        Intent FindFriendsintent =new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(FindFriendsintent);


    }

    private void updateUserStatus(String state){
        Date c = Calendar.getInstance().getTime();


        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        saveCurrentDate = df.format(c);

        Calendar cc = Calendar.getInstance();
        int mHour = cc.get(Calendar.HOUR_OF_DAY);
        int mMinute = cc.get(Calendar.MINUTE);
         saveCurrentTime =(  String.format("%02d:%02d", mHour , mMinute));

        HashMap<String,Object> onlineState =new HashMap<>();
        onlineState.put("time",saveCurrentTime);
        onlineState.put("date",saveCurrentDate);
        onlineState.put("state",state);

        currentUserID =mAuth.getCurrentUser().getUid();
        RootRef.child("users").child(currentUserID).child("userState").updateChildren(onlineState);

    }
}