package com.example.webchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    Button LoginButton,phoneLoginButton;
    EditText UserEmail,UserPassword;
    TextView NeedNewAccountink,ForgetPasswordLink;
    private FirebaseAuth mAuth;
    FirebaseUser currentFirebaseUser;
    DatabaseReference usersRef;
    ProgressBar bar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth =FirebaseAuth.getInstance();
        currentFirebaseUser=mAuth.getCurrentUser();
       usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        intializeFields();

phoneLoginButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent phoneLoginIntent =new Intent(LoginActivity.this,phoneLoginActivity.class);
        startActivity(phoneLoginIntent);
        if(currentFirebaseUser!=null){
            finish();
        }
    }
});
        NeedNewAccountink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUsertoRegisterActivity();
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            signinhere(v);
            }
        });
    }



        public void signinhere(View view) {



            bar.setVisibility(View.VISIBLE);
            String email= UserEmail.getText().toString();
            String password =UserPassword.getText().toString();
            mAuth = FirebaseAuth.getInstance();

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                String currentUserID = mAuth.getCurrentUser().getUid();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                usersRef.child(currentUserID).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull  Task<Void> task) {


                                        bar.setVisibility(View.INVISIBLE);
                                        startActivity(new Intent(LoginActivity.this,MainActivity.class));
                                        UserEmail.setText("");
                                        UserPassword.setText("");
                                        Toast.makeText(LoginActivity.this, "login Successful", Toast.LENGTH_SHORT).show();
                                    }
                                });



                            } else {
                                bar.setVisibility(View.INVISIBLE);
                                UserEmail.setText("");
                               UserPassword.setText("");
                                Toast.makeText(LoginActivity.this, "Invalid email/password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }


    private void sendUsertoRegisterActivity() {
        Intent registerintent =new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerintent);
        finish();
    }

    private void intializeFields() {
        LoginButton=(Button)findViewById(R.id.login_button);
        phoneLoginButton=(Button)findViewById(R.id.phone_login_button);

        UserEmail=(EditText)findViewById(R.id.login_email);
        UserPassword=(EditText)findViewById(R.id.login_password);

        NeedNewAccountink=(TextView)findViewById(R.id.need_new_account_link);
        ForgetPasswordLink =(TextView)findViewById(R.id.forget_password_link);

        bar=(ProgressBar)findViewById(R.id.progressbar_2);
    }



    private void sendUserToMainActivity() {
        Intent Mainintent =new Intent(LoginActivity.this,MainActivity.class);
        startActivity(Mainintent);
        finish();
    }
}