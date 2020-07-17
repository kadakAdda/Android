package com.example.kadakadda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kadakadda.Models.Product;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/*
    Must have an "address" field available.
 */
public class AddressActivity extends AppCompatActivity {
    EditText pincodeEditText;
    EditText houseNoEditText;
    EditText colonyEditText;
    EditText cityEditText;
    EditText stateEditText;
    ProgressBar progressBar;
    Button saveButton;
    String value;

    String uid;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private FirebaseFirestore mfirestore;

    Map<String ,ArrayList<String>> map;
    ArrayList<String> addressMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mfirestore = FirebaseFirestore.getInstance();
        map = new HashMap<>();
        addressMap = new ArrayList<>();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        value = bundle.getString("value");
        Boolean isPrevPresent = bundle.getBoolean("PrevPresent");
        if(isPrevPresent){
            Gson gson = new Gson();
            String prevAdd = bundle.getString("PreviousAddresses");
            java.lang.reflect.Type type = new TypeToken<ArrayList<String>>(){}.getType();
            addressMap = gson.fromJson(prevAdd, type);
        }

        uid = currentUser.getUid();

        pincodeEditText = findViewById(R.id.editTextNumber3);
        houseNoEditText = findViewById(R.id.editTextTextPersonName);
        colonyEditText = findViewById(R.id.editTextTextPersonName2);
        cityEditText = findViewById(R.id.editTextTextPersonName3);
        stateEditText = findViewById(R.id.editTextTextPersonName4);
        progressBar =findViewById(R.id.progressBar3);
        saveButton= findViewById(R.id.button7);
    }
    public void saveAddress(View view){
        String pincode =pincodeEditText.getText().toString();
        String houseNo =houseNoEditText.getText().toString();
        String colony =colonyEditText.getText().toString();
        String city = cityEditText.getText().toString();
        String state = stateEditText.getText().toString();
        String fullAddress = pincode+", "+houseNo+", "+colony+", "+city+", "+state;

        if (pincode.equals("")||houseNo.equals("")||colony.equals("")||city.equals("")||state.equals("")){
            Toast.makeText(this, "Please fill all the blocks", Toast.LENGTH_SHORT).show();
        }else{
            progressBar.setVisibility(View.VISIBLE);
            saveButton.setEnabled(false);
            if (value.equals("1")){
                addressMap.add(fullAddress);
            }else if (value.equals("2")){
                addressMap.add(fullAddress);
            }else if (value.equals("3")){
                addressMap.add(fullAddress);
            }

            map.put("address", addressMap);

            mfirestore
                    .collection("user")
                    .document(uid)
                    .update("address", addressMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AddressActivity.this, "Address Added", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            saveButton.setEnabled(false);
                            onBackPressed();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(AddressActivity.this, "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            saveButton.setEnabled(true);
                        }
                    });

        }
    }

}