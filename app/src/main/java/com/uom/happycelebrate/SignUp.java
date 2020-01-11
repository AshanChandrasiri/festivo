package com.uom.happycelebrate;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;



public class SignUp extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 007;
    com.uom.happycelebrate.utils.SignUp sU;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

//        mAuth = FirebaseAuth.getInstance();
//
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken("272052355427-r2633jq1ub3d7sn371bkuo265fatd8a7.apps.googleusercontent.com")
//                .requestEmail()
//                .requestProfile()
//                .build();
//
//        mGoogleSignInClient = GoogleSignIn.getClient(SignUp.this, gso);
//
        SignInButton googleLogin = findViewById(R.id.sign_in_button);
        Button logout = findViewById(R.id.button);
        Button current = findViewById(R.id.button2);

        sU = new com.uom.happycelebrate.utils.SignUp(SignUp.this){
            @Override
            public void setData(FirebaseUser user) {

            }

        };



//        googleLogin.setOnClickListener(v -> {
//
//            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//            startActivityForResult(signInIntent, RC_SIGN_IN);
//
//        });

        googleLogin.setOnClickListener(v -> {

            startActivityForResult(sU.signInIntent, RC_SIGN_IN);

        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sU.signOut();
            }
        });

        current.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sU.currentUser();
            }
        });


    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == RC_SIGN_IN) {
//            try {
//
//                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                firebaseAuthWithGoogle(account);
//
//            } catch (ApiException e) {
//
//                Log.w(this.getLocalClassName(), "Google sign in failed",e);
//                Toast.makeText(SignUp.this, "Google sign in failed "+e.getMessage(),Toast.LENGTH_LONG).show();
//
//            }
//
//            return;
//        }
//
//    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        sU.activityHandle(requestCode,resultCode,data);

    }


//    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
//
//        Log.d(this.getLocalClassName(), "firebaseAuthWithGoogle:" + acct.getId());
//
//        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//
//                            Toast.makeText(SignUp.this, "Successfullyregistred",Toast.LENGTH_LONG).show();
//                            Log.d(this.getClass().getName(), "signInWithCredential:success");
//                            FirebaseUser user = mAuth.getCurrentUser();
//
//                            System.out.println("image ::::::::: " + user.getPhotoUrl());
//
//
//
//                        } else {
//
//                            Log.w(this.getClass().getName(), "signInWithCredential:failure", task.getException());
//                            Toast.makeText(SignUp.this,"Auth fail",Toast.LENGTH_LONG).show();
//
//
//                        }
//                    }
//                });
//    }





}
