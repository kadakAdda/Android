package com.example.kadakadda;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

public class UserAddress extends AppCompatActivity {
    TextView address1;
    TextView address2;
    TextView address3;
    Button button1;
    Button button2;
    Button button3;
    String uid, add1, add2, add3;
    ArrayList<String> list;

    private FirebaseFirestore mfirestore;
    DocumentReference docRef;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    public void changeAddress(View view){
        String tappedButton = view.getTag().toString();
        if(tappedButton.equals("2") && add1 == null){
            Toast.makeText(this, "Please fill Address1 before adding this address.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(tappedButton.equals("3") && (add1 == null || add2 == null)){
            Toast.makeText(this, "Please fill the previous addresses before adding this address.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent changeAddressIntent = new Intent(UserAddress.this, AddressActivity.class);
        changeAddressIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        changeAddressIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        changeAddressIntent.putExtra("value",tappedButton);
        switch(tappedButton){
            case "1":
                if(add2!=null)
                    list.add(add2);
                if(add3!=null)
                    list.add(add3);
                break;
            case "2":
                if(add1!=null)
                    list.add(add1);
                if(add3!=null)
                    list.add(add3);
                break;
            case "3":
                if(add1!=null)
                    list.add(add1);
                if(add2!=null)
                    list.add(add2);
                break;
        }
        Gson gson = new Gson();
        String prevAdd = gson.toJson(list);
        changeAddressIntent.putExtra("PreviousAddresses", prevAdd);
        changeAddressIntent.putExtra("PrevPresent", !(list.size()==0));
        Log.i("UserAddress", "changeAddress: Sending"+list.size());
        startActivity(changeAddressIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_address);
        address1 = findViewById(R.id.textView8);
        address2 = findViewById(R.id.textView12);
        address3 = findViewById(R.id.textView13);
        button1 = findViewById(R.id.button11);
        button2 =findViewById(R.id.button12);
        button3 =findViewById(R.id.button13);

        list = new ArrayList<>();

        mfirestore = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        uid = user.getUid();
        docRef = mfirestore.collection("user").document(uid);


    }

    @Override
    protected void onStart() {
        super.onStart();
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.getData().get("address")!=null){
                            ArrayList<String> Address = (ArrayList<String>) document.getData().get("address");

                            add1 = null; add2 = null; add3 = null;
                            if(Address.size()>0)
                                add1 = Address.get(0);
                            if(Address.size()>1)
                                add2 = Address.get(1);
                            if(Address.size()>2)
                                add3 = Address.get(2);

                           if (add1!=null){
                             address1.setText(add1);
                             button1.setText("Change Address");
                           }
                          if (add2!=null){
                                address2.setText(add2);
                                button2.setText("Change Address");
                          }
                          if (add3!=null){
                                address3.setText(add3);
                                button3.setText("Change Address");
                          }

                        }
                    }

                } else {
                    Toast.makeText(UserAddress.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        list = new ArrayList<>();
    }
}