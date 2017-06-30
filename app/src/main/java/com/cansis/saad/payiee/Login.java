package com.cansis.saad.payiee;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import shem.com.materiallogin.DefaultLoginView;
import shem.com.materiallogin.DefaultRegisterView;
import shem.com.materiallogin.MaterialLoginView;

public class Login extends AppCompatActivity {


    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();

        final MaterialLoginView login = (MaterialLoginView) findViewById(R.id.login);
        ((DefaultLoginView)login.getLoginView()).setListener(new DefaultLoginView.DefaultLoginViewListener() {
            @Override
            public void onLogin(TextInputLayout loginUser, TextInputLayout loginPass) {
                //Handle login

                mAuth.signInWithEmailAndPassword(loginUser.getEditText().getText().toString(),
                        loginPass.getEditText().getText().toString())

                        .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(Login.this , task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Intent i = new Intent(Login.this,Home.class);
                                    startActivity(i);

                                }

                                // ...
                            }
                        });


            }
        });

        ((DefaultRegisterView)login.getRegisterView()).setListener(new DefaultRegisterView.DefaultRegisterViewListener() {
            @Override
            public void onRegister(TextInputLayout registerUser, TextInputLayout registerPass, TextInputLayout registerPassRep) {

                //Handle register

                mAuth.createUserWithEmailAndPassword(registerUser.getEditText().getText().toString(),
                        registerPass.getEditText().getText().toString())
                        .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Login.this, "Registered",Toast.LENGTH_SHORT).show();

                                    Intent i = new Intent(Login.this, Login.class);
                                    startActivity(i);
                                    // Sign in success, update UI with the signed-in user's information

                                  //  Firebase ref=new Firebase("https://car-sales-f4f9c.firebaseio.com/");

                                    //String key=ref.child("Users").push().getKey();
                                    //ref.child("Users").child(key).child("Name").setValue(Name);
                                    //ref.child("Users").child(key).child("Email").setValue(email);
                                   // ref.child("Users").child(key).child("Contact info").setValue(contactinfo);



                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(Login.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }

                                // ...
                            }
                        });
            }
        });
    }
}
