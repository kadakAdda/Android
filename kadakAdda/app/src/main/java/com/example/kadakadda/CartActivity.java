package com.example.kadakadda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kadakadda.Adapters.CartAdapter;
import com.example.kadakadda.Models.CartItem;
import com.example.kadakadda.Models.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {

    public static final String PRODUCT_AVAILABILITY = "availability";
    public static final String PRODUCT_DESCRIPTION = "description";
    public static final String PRODUCT_IMGPATH = "imgPath";
    public static final String PRODUCT_PRICE = "price";
    public static final String PRODUCT_TITLE = "title";
    public static final String PRODUCT_TYPE = "type";
    public static final String TAG = "CartActivity";
    double subTotal;
    double shipping;
    double total;
    String couponCode;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser user;
    Gson gson;

    CircleImageView nav1, nav2, nav3;
    TextView textViewSubTotal, textViewTotal, textViewShipping;
    Button buttonProceed, buttonApply;
    TextInputEditText editTextCoupon;
    RecyclerView recyclerView;
    CartAdapter adapter;
    ArrayList<CartItem> cartItems;
    ArrayList<Product> products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences), MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new Gson();
        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        cartItems = new ArrayList<>();
        products = new ArrayList<>();
        shipping = 50.0;

        setContentView(R.layout.activity_cart);
        nav1 = findViewById(R.id.nav1);
        nav2 = findViewById(R.id.nav2);
        nav3 = findViewById(R.id.nav3);
        textViewSubTotal = findViewById(R.id.subtotal_price);
        textViewTotal = findViewById(R.id.total_price);
        textViewShipping = findViewById(R.id.shipping_price);
        buttonApply = findViewById(R.id.buttonApply);
        buttonProceed = findViewById(R.id.buttonProceed);
        editTextCoupon = findViewById(R.id.editTextCoupon);
        recyclerView = findViewById(R.id.recycler_view);

        //TODO:Add cart items

        nav2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
                if(total>0) {
                    startActivity(new Intent(CartActivity.this, UserAddress.class));
                }else{
                    Toast.makeText(CartActivity.this, "Your cart is empty.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        nav3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(total>0) {
                    startActivity(new Intent(CartActivity.this, PaymentActivity.class));
                }else{
                    Toast.makeText(CartActivity.this, "Your cart is empty.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                couponCode = editTextCoupon.getText().toString();
                validateCoupon();
            }
        });

        loadProductListLocal();
    }


    /*
        loadProductListLocal() is called in onCreate(), though in final app, it will not be called
        here. This function tries to load Product List if it is present in local storage.
        If not present, *JsonSyntaxException* is thrown, and fetchProductsList() is called, which
        loads Product List from cloud.
     */
    public void loadProductListLocal() {
        try{
            String storedProductList = sharedPreferences.getString(getString(R.string.product_list), (new JSONObject()).toString());
            java.lang.reflect.Type type = new TypeToken<ArrayList<Product>>(){}.getType();
            products =  gson.fromJson(storedProductList, type);
            Log.i(TAG, "loadProductListLocal: "+storedProductList);
            Toast.makeText(this, "Loaded Product List from SharedPreferences!", Toast.LENGTH_SHORT).show();
            loadUserCartLocal();
        }catch(Exception e){
            e.printStackTrace();
            fetchProductsList();
        }
    }


    /*
        populateCartRecyclerView() populates the Cart RecyclerView according to the items present in cartItems.
     */
    private void populateCartRecyclerView() {
        Log.i(TAG, "populateCartRecyclerView: ");
        adapter = new CartAdapter(cartItems, this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        updateTotals();
    }

    private void createUserCartLocal() {
        cartItems.add(new CartItem(products.get(0), (long) 2));
        cartItems.add(new CartItem(products.get(1), (long) 2));
        cartItems.add(new CartItem(products.get(2), (long) 2));
        for(CartItem cartItem: cartItems){
            Log.i(TAG, "createUserCartLocal: "+cartItem.product.id);
        }
        Log.i(TAG, "createUserCartLocal: Added 3 items to cart.");
        Toast.makeText(this, "createUserCartLocal: Added 3 items to cart.", Toast.LENGTH_SHORT).show();
    }

    /*
        updateTotals() updates the totalCost values of all CartItems and
        shows subTotal and total. First the local cart gets updated, then
        the changes are sent to Cloud Cart.
     */
    public void updateTotals(){
        subTotal = 0;
        for(CartItem cartItem : cartItems) {
            cartItem.update();
            subTotal += cartItem.totalCost;
        }

        applyDiscountOnSubtotal();
        textViewSubTotal.setText("₹ " + String.valueOf(subTotal));

        total = subTotal + shipping;
        textViewTotal.setText("₹ " +String.valueOf(total));

        updateUserCartLocal();
        updateCloudCart();

        //TODO: Set ProgressBar visible
        //adapter.notifyDataSetChanged();
    }

    /*
        validateCoupon() is called on clicking Apply Button.
     */
    public void validateCoupon(){
        //TODO
    }

    /*
        applyDiscountOnSubtotal gets called on coupon validation.
     */
    private void applyDiscountOnSubtotal() {
        subTotal = subTotal * 1.0; //TODO
    }

    /*
        loadUserCartLocal() is called as soon as the product list is ready.
        It populates the cartItems ArrayList to make RecyclerView data ready.
        Chance of *JsonSyntaxException* exists if there is no local cart.
        Hence fetchCloudCart() is called, which fetches and writes
        the local cart.
     */
    public void loadUserCartLocal() {
        try{
            String storedCart = sharedPreferences.getString(getString(R.string.user_cart), (new JSONObject()).toString());
            java.lang.reflect.Type type = new TypeToken<ArrayList<CartItem>>(){}.getType();
            cartItems =  gson.fromJson(storedCart, type);
            Log.i(TAG, "loadUserCartLocal: "+storedCart);
            Toast.makeText(this, "Loaded cart (local).", Toast.LENGTH_SHORT).show();
            if(cartItems.size()==0)
                Log.i(TAG, "loadUserCartLocal: Empty Cart!");
            populateCartRecyclerView();

        }catch(Exception e){
            e.printStackTrace();
            //TODO
            //createUserCartLocal();
            fetchCloudCart();
        }
    }

    /*
        updateUserLocalCart() synchronises +/- changes with local storage.
        It usually does not throw Exceptions.
     */
    public void updateUserCartLocal(){
        String userCartLocal = gson.toJson(cartItems);
        editor.putString(getString(R.string.user_cart), userCartLocal);
        editor.commit();
        Log.i(TAG, "updateUserCartLocal: ");
        Toast.makeText(this, "Updated cart (Local).", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlusClick(int itemPosition) {

        cartItems.get(itemPosition).quantity+=1;
        updateTotals();
    }

    @Override
    public void onMinusClick(int itemPosition) {

        //Non-negative security for Quantity
        if(cartItems.get(itemPosition).quantity==1){
            Toast.makeText(this, "Sorry, can't reduce quantity below 1.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(cartItems.get(itemPosition).quantity>1)
            cartItems.get(itemPosition).quantity-=1;

        //Recalculate totalCost for item, as well as grand Subtotal and grand Total
        updateTotals();

    }

    /*
        fetchProductsList() loads product list from cloud and populates products ArrayList.
        It calls storeProductListLocal() to store the product list in local storage.
     */
    public void fetchProductsList(){
        firebaseFirestore.collection("products")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                //TODO: Set ProgressBarVisibility
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot qds: task.getResult()){
                        Map<String, Object> data = qds.getData();
                        Product product = new Product(
                                qds.getId(),
                                (Boolean)data.get(PRODUCT_AVAILABILITY),
                                (String)data.get(PRODUCT_DESCRIPTION),
                                (String)data.get(PRODUCT_IMGPATH),
                                (Long)data.get(PRODUCT_PRICE),
                                (String)data.get(PRODUCT_TITLE),
                                (String)data.get(PRODUCT_TYPE)
                        );
                        products.add(product);
                        Log.i("HI", "Added "+product.id);
                    }

                    storeProductListLocal();
                }
                else{
                    Log.i("HI", task.getException().getLocalizedMessage());
                    Toast.makeText(CartActivity.this, "Sorry, couldn't load data!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*
        storeProductListLocal() stores product list locally and calls for loadProductListLocal to
        reload list from local storage and advance to loading user cart.
     */
    private void storeProductListLocal() {
        String productList = gson.toJson(products);
        editor.putString(getString(R.string.product_list), productList);
        editor.commit();
        Toast.makeText(CartActivity.this, "Product List written to SharedPreferences!", Toast.LENGTH_SHORT).show();
        loadProductListLocal();
    }

    /*
        findProduct() is used to find product by id.
        Called by fetchCloudCart to use a Product object for creating CartItem objects.
        null return means that the cloud cart is compromised.
     */
    public Product findProduct(String id){
        for(Product product:products){
            if(product.id.equals(id))
                return product;
        }
        return null;
    }

    /*
        fetchCloudCart() fetches the cart present in the user profile document.
     */
    public void fetchCloudCart() {
        firebaseFirestore.collection("user")
                .document(user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            try {
                                Map<String, Object> docData = task.getResult().getData();
                                if (docData == null || docData.isEmpty()) {
                                    throw new NullPointerException("User profile does not exist.");
                                }
                                HashMap<String, Object> cart = (HashMap<String, Object>) docData.get("cart");
                                if (cart == null || cart.isEmpty()) {
                                    Toast.makeText(CartActivity.this, "Empty Cloud Cart!!", Toast.LENGTH_SHORT).show();
                                    throw new IllegalStateException("Cart is null or does not exist.");
                                }
                                for (HashMap.Entry<String, Object> entry : cart.entrySet()) {
                                    Product p = findProduct(entry.getKey());
                                    if (p == null) {
                                        throw new IllegalStateException("Bad product key!");
                                    }
                                    cartItems.add(new CartItem(p, (Long) entry.getValue()));
                                }
                                String userCartLocal = gson.toJson(cartItems);
                                editor.putString(getString(R.string.user_cart), userCartLocal);
                                editor.commit();
                                Log.i(TAG, "onComplete: Updating local cart");
                                Toast.makeText(CartActivity.this, "Updated cart (Local).", Toast.LENGTH_SHORT).show();
                                loadUserCartLocal();
                            }catch (Exception e){
                                e.printStackTrace();
                                Toast.makeText(CartActivity.this, "There was a problem loading the cloud cart.", Toast.LENGTH_SHORT).show();
                                Log.i(TAG, "onComplete: "+e.getLocalizedMessage());
                                if(e instanceof NullPointerException){
                                    createCloudCart();
                                }
                            }
                        }
                        else{
                            Toast.makeText(CartActivity.this, "Failed to get cloud cart.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /*
        updateCloudCart() syncs the local changes with the server.
     */
    public void updateCloudCart(){
        HashMap<String, Long> cart = createCart();
        DocumentReference documentReference = firebaseFirestore.collection("user")
                .document(user.getUid());
        documentReference.update("cart", cart)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(CartActivity.this, "Updated Cloud Cart", Toast.LENGTH_SHORT).show();
                            adapter.notifyDataSetChanged();
                        }else{
                            Toast.makeText(CartActivity.this, "Failed to update cloud cart." + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    /*
        createCart() converts the local cart to a Hashmap<String, Long> to enable the details
        to be uploaded to Firestore.
     */
    public HashMap<String, Long> createCart(){
        HashMap<String, Long> cart = new HashMap<>();
        for(CartItem cartItem:cartItems){
            cart.put(cartItem.product.id, cartItem.quantity);
        }
        return cart;
    }

    /*
        createCloudCart() is called in case there is no cloud cart available in user profile
        document.
     */
    public void createCloudCart(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("cart", new HashMap<>());
        DocumentReference documentReference = firebaseFirestore.collection("user")
                .document(user.getUid());
        documentReference.set(map, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.i(TAG, "createCloudCart: Success");
                            fetchCloudCart();
                        }else{
                            try {
                                throw task.getException();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }
}