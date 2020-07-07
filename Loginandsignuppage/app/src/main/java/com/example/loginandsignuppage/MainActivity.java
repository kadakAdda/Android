package com.example.loginandsignuppage;

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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    EditText emailEditText;
    EditText passwordEditText;
    EditText phoneEditText;
    EditText otpEditText;
    Button emaiLoginButton;
    Button otpGenerateButton;
    Button otpVerificationButton;
    ProgressBar progressBar;
    TextView signUp;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    public void Login(View view){
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (email.equals("")) {
            Toast.makeText(MainActivity.this, "Please Enter Your Email Id", Toast.LENGTH_SHORT).show();
        } else if (password.equals("")) {
            Toast.makeText(MainActivity.this, "Please Enter Password", Toast.LENGTH_SHORT).show();
        } else{
            progressBar.setVisibility(View.VISIBLE);
            emaiLoginButton.setEnabled(false);
            otpGenerateButton.setEnabled(false);
            otpVerificationButton.setEnabled(false);
            signUp.setEnabled(false);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                sendUserToHome();
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                emaiLoginButton.setEnabled(true);
                                otpGenerateButton.setEnabled(true);
                                otpVerificationButton.setEnabled(true);
                                signUp.setEnabled(true);
                                Toast.makeText(MainActivity.this,  task.getException().toString().substring(task.getException().toString().indexOf(" ")),
                                        Toast.LENGTH_SHORT).show();

                            }

                        }
                    });
        }
    }
    public void generateOtp(View view){
        String phone_number = phoneEditText.getText().toString();
        String complete_phone_number = "+91"  + phone_number;
        if(phone_number.isEmpty()){
            Toast.makeText(this, "Please Enter Your Phone Number", Toast.LENGTH_SHORT).show();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            emaiLoginButton.setEnabled(false);
            otpGenerateButton.setEnabled(false);
            signUp.setEnabled(false);
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    complete_phone_number,
                    60,
                    TimeUnit.SECONDS,
                    MainActivity.this,
                    mCallbacks
            );

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        phoneEditText = findViewById(R.id.editTextPhone);
        otpEditText = findViewById(R.id.editTextNumber);
        emaiLoginButton =findViewById(R.id.emailLoginbutton);
        otpGenerateButton =findViewById(R.id.button2);
        otpVerificationButton = findViewById(R.id.button3);
        progressBar = findViewById(R.id.progressBar);
        signUp = findViewById(R.id.textView4);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                emaiLoginButton.setEnabled(true);
                otpGenerateButton.setEnabled(true);
                signUp.setEnabled(true);
            }

            @Override
            public void onCodeSent(final String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                progressBar.setVisibility(View.INVISIBLE);
                otpVerificationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String otp = otpEditText.getText().toString();

                        if(otp.isEmpty()){
                            Toast.makeText(MainActivity.this, "Please Enter OTP", Toast.LENGTH_SHORT).show();


                        } else {
                            progressBar.setVisibility(View.VISIBLE);
                            otpVerificationButton.setEnabled(false);
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
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendUserToHome();

                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            emaiLoginButton.setEnabled(true);
                            otpGenerateButton.setEnabled(true);
                            otpVerificationButton.setEnabled(true);
                            signUp.setEnabled(true);
                            Toast.makeText(MainActivity.this, task.getException().toString().substring(task.getException().toString().indexOf(" ")) , Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
    public void goToSignUp(View view){
        Intent SignUpIntent = new Intent(MainActivity.this, SignUp.class);
        SignUpIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        SignUpIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(SignUpIntent);
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null){
            sendUserToHome();
        }
    }
    private void sendUserToHome() {
        Intent homeIntent = new Intent(MainActivity.this, HomePage.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }
}