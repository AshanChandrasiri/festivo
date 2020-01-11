package com.uom.happycelebrate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.andremion.floatingnavigationview.FloatingNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//import com.koushikdutta.async.future.FutureCallback;
//import com.koushikdutta.ion.Ion;
import com.uom.happycelebrate.adapters.CustomAdapter;
import com.uom.happycelebrate.adapters.CustomVehicleAdapter;
import com.uom.happycelebrate.data.QRUniqueCode;
import com.uom.happycelebrate.models.Card;
import com.uom.happycelebrate.models.DataModel;
import com.uom.happycelebrate.utils.VideoFinder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CardsListPage extends AppCompatActivity {

    ArrayList<DataModel> dataModels;
    ListView listView;
    private static CustomAdapter adapter;
    private static CustomVehicleAdapter customVehicleAdapter;

    private ArrayList<Card> messageList;

    private Button btnSend;

    private FloatingNavigationView mFloatingNavigationView;

    private DatabaseReference mDatabase;
    private DatabaseReference mMessageReference;
    private ChildEventListener mMessageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards_list_page);

        mDatabase = FirebaseDatabase.getInstance().getReference();


        mMessageReference = FirebaseDatabase.getInstance().getReference("Cards_Repository");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.list);


        messageList = new ArrayList<>();

        mFloatingNavigationView = findViewById(R.id.floating_navigation_view);

        mFloatingNavigationView.setOnClickListener(view -> doAction(view));


        mFloatingNavigationView.setNavigationItemSelectedListener(item -> {

            if (item.getTitle().equals("AR Demo")) {

                Intent intent = new Intent(CardsListPage.this, BarcordReader.class);
                intent.putExtra("redirect_page", "AR_PAGE");
                startActivity(intent);

            } else if (item.getTitle().equals("Create Card")) {

                Intent intent = new Intent(CardsListPage.this, BarcordReader.class);
                intent.putExtra("redirect_page", "CREATE_CARD");
                startActivity(intent);

            } else {

                Intent intent = new Intent(CardsListPage.this, SignUp.class);
//                intent.putExtra("redirect_page", "CREATE_CARD");
                startActivity(intent);

            }

            mFloatingNavigationView.close();
//                CardsListPage.this.finish();


            return true;
        });


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("card");

        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        collectAllCards((Map<String, Object>) dataSnapshot.getValue());

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

    }

    private void doAction(View view) {


//        if (QRUniqueCode.firebaseUser != null) {

            mFloatingNavigationView.open();

//



//        }


    }

    private void collectAllCards(Map<String, Object> cards) {

        for (Map.Entry<String, Object> entry : cards.entrySet()) {
            Map singleCard = (Map) entry.getValue();
            Card card = new Card();
            messageList.add(card.toCard(singleCard));
        }

        customVehicleAdapter = new CustomVehicleAdapter(messageList, getApplicationContext());

        listView.setAdapter(customVehicleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Card card = messageList.get(position);

                Snackbar.make(view, card.getId() + "\n" + card.getDesigner_id() + " API: " + card.getVatagory(), Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mFloatingNavigationView.isOpened()) {
            mFloatingNavigationView.close();
        } else {
            super.onBackPressed();
        }
    }

}



//    private void writeNewMessage() {
//
//        Card card = new Card();
//        card.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text");
//        card.setDesigner_id("00001D");
//        card.setId("00001A");
//        card.setImage_url("https://asset.holidaycardsapp.com/assets/card/j_newad_124-8487ccf02839a10684b2bbff9b599b63.png");
//        card.setVatagory("BIRTHDAY");
//
//        Map<String, Object> messageValues = card.toMap();
//        Map<String, Object> childUpdates = new HashMap<>();
//
//        String key = mDatabase.child("card").push().getKey();
//
//        childUpdates.put("/card/" + key, messageValues);
//
//        mDatabase.updateChildren(childUpdates);
//    }