package com.example.birdsadventure;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener
{
    private FirebaseAuth fireBaseAuth;
    EditText txtName, txtEmail, txtPassword, txtPhone, txtAddress, txtConfirmPassword;
    Button btnSignUp;
    FirebaseAuth firebaseAuth;

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    boolean isLogin;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_sign_up);

        if (validateAutomaticLogin()) {
            navigateToHome();
        }

        txtName = findViewById (R.id.txtNameSignUp);
        txtEmail = findViewById (R.id.txtEmailSignUp);
        txtPassword = findViewById (R.id.txtPasswordSignUp);
        txtPhone = findViewById (R.id.txtPhoneSignUp);
        txtAddress = findViewById (R.id.txtAddressSignUp);
        txtConfirmPassword = findViewById (R.id.txtConfirmPassword);

        //Initialize  firebaseAuth
        fireBaseAuth = FirebaseAuth.getInstance();

        //SignUp Button
        btnSignUp = findViewById (R.id.btnSignUp);
        btnSignUp.setOnClickListener (this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId () == R.id.btnSignUp)
        {
            registerUser ();
        }
    }

    private boolean validateAutomaticLogin() {
        sp = getSharedPreferences(MyVariables.cacheFile, Context.MODE_PRIVATE);

        isLogin = sp.getBoolean(MyVariables.keyLoginAuth, MyVariables.defaultLoginAuth);
        currentUserID = sp.getString(MyVariables.keyUserID, MyVariables.defaultUserID);

        if (isLogin && !currentUserID.equals("")) {
            return true;
        }
        return false;
    }

    private void navigateToHome() {
        finish();
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    //Register user Using Firebase Authentication
    private  void registerUser() {

       final String name, email, phone, address;
       String password, confirmpassword;
        name = txtName.getText ().toString ().trim ();
        email = txtEmail.getText ().toString ().trim ();
        phone = txtPhone.getText ().toString ().trim ();
        address = txtAddress.getText ().toString ();
        password = txtPassword.getText ().toString ().trim ();
        confirmpassword = txtConfirmPassword.getText ().toString ();


        //check if the fields are empty
        if (TextUtils.isEmpty(name))
        {
            txtName.setError("Name is Required ");
        }
        if (TextUtils.isEmpty(email))
        {
            txtEmail.setError("Email is Required ");

        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            txtEmail.setError("Enter a valid Email");

        }
        if (TextUtils.isEmpty(phone))
        {
            txtPhone.setError("Phone is Required ");

        }
        if (TextUtils.isEmpty(address))
        {
            txtAddress.setError("Name is Required ");
        }
        if (TextUtils.isEmpty(password))
        {
            txtPassword.setError("Password is Required ");

        }
        if (password.length() < 8)
        {
            txtPassword.setError("Password must be >= 6 Characters");
        }
        if (TextUtils.isEmpty(confirmpassword)|| !confirmpassword.equals (password))
        {
            txtConfirmPassword.setError("Confirm Password is not matched with password ");
        }

        else {
            //create the user with email and password
            fireBaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("TAG", "createUserWithEmail:success");
                                Toast.makeText(getApplicationContext(), "User has Registered", Toast.LENGTH_SHORT).show();
                                FirebaseFirestore db = FirebaseFirestore.getInstance();


                                User user = new User();
                                user.setName (name);
                                user.setEmail (email);
                                user.setPhone (phone);
                                user.setAddress (address);
                                user.setStatus (true);


                                db.collection("Users").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>()
                                {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference)
                                    {
                                        Toast.makeText(SignUpActivity.this,"Added",Toast.LENGTH_SHORT).show();


                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        Toast.makeText(SignUpActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();


                                    }
                                });

                                startActivity(new Intent(SignUpActivity.this, HomeActivity.class));

                            }
                        }
                    });
        }
    }

    private void saveUserDetails() {

        editor = sp.edit();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            String userEmail = firebaseUser.getEmail();

            editor.putBoolean(MyVariables.keyLoginAuth, true);
            editor.putString(MyVariables.keyUserID, userId);
        } else {
            editor.putBoolean(MyVariables.keyLoginAuth, false);
            editor.putString(MyVariables.keyUserID, "");
        }
        editor.apply();
    }

}