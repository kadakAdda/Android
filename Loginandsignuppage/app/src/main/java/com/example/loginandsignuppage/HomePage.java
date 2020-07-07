package com.example.loginandsignuppage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

public class HomePage extends AppCompatActivity {
    TextView userName;
    TextView useremail;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    public void signOut(View view){
        mAuth.signOut();
        sendUserToLogin();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        userName = findViewById(R.id.textView7);
        useremail = findViewById(R.id.textView9);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {



                String name = profile.getDisplayName();
                String email = profile.getEmail();
               
                if(name==null ){
                    userName.setText("USER");
                }else if(name.equals("")){
                    userName.setText("USER");
                } else{
                    userName.setText(name);
                }

                if (email!=null) {
                    useremail.setText(email);
                }
            }

        }


    }
    private void sendUserToLogin() {
        Intent loginIntent = new Intent(HomePage.this, MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}