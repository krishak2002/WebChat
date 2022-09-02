package com.example.webchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {


    Button CreateAccountButton;
    EditText UserEmail,UserPassword;
    TextView AlreadyHaveAccountLink;
    private FirebaseAuth mAuth;
    DatabaseReference RootRef;
    ProgressBar bar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        RootRef =FirebaseDatabase.getInstance().getReference();
        initializeFields();
        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUsertoLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              signuphere();


            }});}

 public void signuphere (){

     bar.setVisibility(View.VISIBLE);
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        mAuth = FirebaseAuth.getInstance();


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                             String currentUserId =mAuth.getCurrentUser().getUid();
                             RootRef.child("users").child(currentUserId).setValue("");

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            RootRef.child(currentUserId).child("device_token").setValue(deviceToken);

                            bar.setVisibility(View.INVISIBLE);

                            UserEmail.setText("");
                            UserPassword.setText("");
                            Toast.makeText(RegisterActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                            sendUsertoMainActivity();
                        } else {
                            bar.setVisibility(View.INVISIBLE);
                            UserEmail.setText("");
                            UserPassword.setText("");
                            Toast.makeText(RegisterActivity.this, "Process Error", Toast.LENGTH_SHORT).show();
                        }
                    }

                });

    }

    private void sendUsertoMainActivity() {
        Intent mainintent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(mainintent);
        finish();
    }

    private void sendUsertoLoginActivity () {
            Intent loginintent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(loginintent);
            finish();
        }

        private void  initializeFields() {
            CreateAccountButton = (Button) findViewById(R.id.register_button);
            UserEmail = (EditText) findViewById(R.id.register_email);
            UserPassword = (EditText) findViewById(R.id.register_password);
            AlreadyHaveAccountLink = (TextView) findViewById(R.id.already_have_id_link);
            bar=(ProgressBar)findViewById(R.id.progress_bar);
        }
    }

