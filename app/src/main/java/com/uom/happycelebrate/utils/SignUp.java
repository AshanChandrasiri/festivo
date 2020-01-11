package com.uom.happycelebrate.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.uom.happycelebrate.R;
import com.uom.happycelebrate.data.QRUniqueCode;


public abstract class SignUp {


    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 007;
    private Context context;
    public static Intent signInIntent;



    public SignUp(Context context){

        this.context = context;

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("272052355427-r2633jq1ub3d7sn371bkuo265fatd8a7.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        SignUp.signInIntent = mGoogleSignInClient.getSignInIntent();

    }


    public void activityHandle(int requestCode, int resultCode, Intent data){


        if (requestCode == RC_SIGN_IN) {
            try {

                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {

//                Log.w(this.getLocalClassName(), "Google sign in failed",e);
                Toast.makeText(context, "Google sign in failed "+e.getMessage(),Toast.LENGTH_LONG).show();

            }

            return;
        }




    }



    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

//        Log.d(this.getLocalClassName(), "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            Toast.makeText(context, "Successfullyregistred",Toast.LENGTH_LONG).show();
//                            Log.d(this.getClass().getName(), "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            QRUniqueCode.firebaseUser = user;

                            System.out.println("image ::::::::: " + user.getPhotoUrl());



                        } else {

//                            Log.w(this.getClass().getName(), "signInWithCredential:failure", task.getException());
                            Toast.makeText(context,"Auth fail",Toast.LENGTH_LONG).show();


                        }

                    }
                });

    }


    public void signOut() {

        mAuth.signOut();

    }


    public void currentUser(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            System.out.println("nooooooooooooo userrrrrrrrrrrrr");
        } else {

            QRUniqueCode.firebaseUser = user;

            System.out.println("user is " + user.getDisplayName());
            System.out.println("user email "+ user.getEmail());


        }
    }


    public abstract void setData(FirebaseUser user);









}
