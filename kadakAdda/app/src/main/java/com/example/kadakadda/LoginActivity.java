package com.example.kadakadda;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import static com.example.kadakadda.OtpActivity.PHONE_NUMBER;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth.AuthStateListener authStateListener;
    private CallbackManager mCallbackManager;
    private SpecialFbLoginButton fbLoginButton;
    private AccessTokenTracker accessTokenTracker;
    private AuthCredential credential;

    private SignInButton signInButton;
    private GoogleSignInClient googleSignInClient;
    private String TAG = "LoginActivity";

    private EditText mPhoneNumber;

    private Button mGenerateBtn;
    private ProgressBar mLoginProgress;

    private TextView mLoginFeedbackText;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private static final String FB_TAG = "FacebookAuthentication";
    private static final Integer RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Toast.makeText(this, "Hi!!", Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();


        mPhoneNumber = findViewById(R.id.phone_number_text);
        mGenerateBtn = findViewById(R.id.generate_btn);
        mLoginProgress = findViewById(R.id.login_progress_bar);
        mLoginFeedbackText = findViewById(R.id.login_form_feedback);

        //Special Facebook Login button : fbLoginButton
        fbLoginButton = (SpecialFbLoginButton) findViewById(R.id.fb_login_button);
        fbLoginButton.setReadPermissions("email", "public_profile");
        signInButton = findViewById(R.id.gmail_signin);

        mGenerateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phone_number = mPhoneNumber.getText().toString();

                String complete_phone_number = "+91"  + phone_number;

                if(phone_number.isEmpty()){
                    mLoginFeedbackText.setText("Please fill in the form to continue.");
                    mLoginFeedbackText.setVisibility(View.VISIBLE);
                } else {
                    mLoginProgress.setVisibility(View.VISIBLE);
                    mGenerateBtn.setEnabled(false);

                    /*PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            complete_phone_number,
                            60,
                            TimeUnit.SECONDS,
                            LoginActivity.this,
                            mCallbacks
                    );*/
                    Intent intent = new Intent(LoginActivity.this, OtpActivity.class);
                    intent.putExtra(PHONE_NUMBER, phone_number);
                    startActivity(intent);

                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                mLoginFeedbackText.setText("Verification Failed, please try again.");
                mLoginFeedbackText.setVisibility(View.VISIBLE);
                mLoginProgress.setVisibility(View.INVISIBLE);
                mGenerateBtn.setEnabled(true);
            }

            @Override
            public void onCodeSent(final String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                Intent otpIntent = new Intent(LoginActivity.this, OtpActivity.class);
                                otpIntent.putExtra("AuthCredentials", s);
                                startActivity(otpIntent);
                            }
                        },
                        10000);
            }
        };

        //START: Facebook Authentication Code
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        fbLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(FB_TAG, "onSuccess: " + loginResult);
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(FB_TAG, "onCancel()");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(FB_TAG, "onError:" + error);
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mCurrentUser = mAuth.getCurrentUser();
                if(mCurrentUser!=null){
                }
                else{
                }
            }
        };

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken == null){
                    mAuth.signOut();
                }
            }
        };
        //END : Facebook Authentication Code

        //START: Gmail Authentication Code
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        //END: Gmail Authentication Code



    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
        if(mCurrentUser != null){
            sendUserToHome();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendUserToHome();

                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                mLoginFeedbackText.setVisibility(View.VISIBLE);
                                Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                //mLoginFeedbackText.setText("There was an error verifying OTP");
                            }
                        }
                        mLoginProgress.setVisibility(View.INVISIBLE);
                        mGenerateBtn.setEnabled(true);
                    }
                });
    }

    private void sendUserToHome() {
        Intent homeIntent = new Intent(LoginActivity.this, CartActivity.class);
        //Intent homeIntent = new Intent(LoginActivity.this, PaymentActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }

    //START: Facebook
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleFacebookToken(AccessToken accessToken) {
        Log.d(FB_TAG, "handleFacebookToken: " + accessToken);

        credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(FB_TAG, "Successful Sign IN");
                    Toast.makeText(LoginActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                    //mCurrentUser = mAuth.getCurrentUser();
                    sendUserToHome();

                }else{
                    Log.d(FB_TAG, "Sign in Failed." + task.getException().getLocalizedMessage());
                    Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    fbLoginButton.getTheLoginManager().logOut();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener != null){
            mAuth.removeAuthStateListener(authStateListener);
        }
    }
    //END: Facebook


    //START: Google
    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try{
            GoogleSignInAccount acc = task.getResult(ApiException.class);
            Toast.makeText(this, "Signed In Successfully.", Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(acc);
        }catch (ApiException e){
            Toast.makeText(this, "Sign in Failed. " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            FirebaseGoogleAuth(null);
        }
    }

    private void FirebaseGoogleAuth(GoogleSignInAccount acct) {
        credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    //TODO : Success
                    Toast.makeText(LoginActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Success");
                    sendUserToHome();
                }else {
                    Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    //END: Google
}