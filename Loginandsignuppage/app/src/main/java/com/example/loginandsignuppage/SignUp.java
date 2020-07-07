package com.example.loginandsignuppage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SignUp extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    EditText emailEditText;
    EditText passwordEditText;
    EditText confirmPasswordEditText;
    EditText phoneEditText;
    EditText otpEditText;
    ProgressBar progressBar;
    Button otpButton;
    Button signInButton;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    public void otpGeneration(View view){
        String phone_number = phoneEditText.getText().toString();
        String complete_phone_number = "+91"  + phone_number;
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmedPassword = confirmPasswordEditText.getText().toString();

        if(phone_number.isEmpty()|| email.isEmpty()||password.isEmpty()||confirmedPassword.isEmpty()){
            Toast.makeText(this, "Please fill every block to continue", Toast.LENGTH_SHORT).show();
        }else if ( !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Please enter valid email id", Toast.LENGTH_SHORT).show();
        }
        else if (!confirmedPassword.equals(password)){
            Toast.makeText(this, "Password do not match with confirmed password ", Toast.LENGTH_SHORT).show();
        }else if (confirmedPassword.length()<6){
            Toast.makeText(this, "Password length is too short.\n Password must be greater than 5 digits", Toast.LENGTH_SHORT).show();
        }
        else {

            progressBar.setVisibility(View.VISIBLE);
           otpButton.setEnabled(false);
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    complete_phone_number,
                    60,
                    TimeUnit.SECONDS,
                    SignUp.this,
                    mCallbacks
            );

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextTextPassword2);
        confirmPasswordEditText = findViewById(R.id.editTextTextPassword3);
        phoneEditText = findViewById(R.id.editTextPhone2);
        otpEditText = findViewById(R.id.editTextNumber2);
        progressBar = findViewById(R.id.progressBar2);
        otpButton = findViewById(R.id.button4);
        signInButton = findViewById(R.id.button5);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(SignUp.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);

                otpButton.setEnabled(true);

            }

            @Override
            public void onCodeSent(final String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                progressBar.setVisibility(View.INVISIBLE);
                signInButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String otp = otpEditText.getText().toString();

                        if(otp.isEmpty()){
                            Toast.makeText(SignUp.this, "Please Enter OTP", Toast.LENGTH_SHORT).show();


                        } else {
                            progressBar.setVisibility(View.VISIBLE);
                            signInButton.setEnabled(false);
                            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(s, otp);
                            signInWithPhoneAuthCredential(credential);
                        }
                    }
                });


            }
        };

    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String email = emailEditText.getText().toString();
                            String password = passwordEditText.getText().toString();

                            AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                            mAuth.getCurrentUser().linkWithCredential(credential)
                                    .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                sendUserToHome();
                                            } else {

                                                Toast.makeText(SignUp.this, task.getException().toString().substring(task.getException().toString().indexOf(" ")) , Toast.LENGTH_SHORT).show();
                                                sendUserToHome();

                                            }

                                        }
                                    });


                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                           signInButton.setEnabled(true);
                            otpButton.setEnabled(true);

                            Toast.makeText(SignUp.this, task.getException().toString().substring(task.getException().toString().indexOf(" ")) , Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
    private void sendUserToHome() {
        Intent homeIntent = new Intent(SignUp.this, HomePage.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }
}