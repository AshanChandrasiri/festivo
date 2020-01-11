package com.uom.happycelebrate;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.uom.happycelebrate.data.QRUniqueCode;
import com.uom.happycelebrate.utils.GooeyMenu;

import java.io.IOException;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.VideoView;

public class CreateCard extends AppCompatActivity implements GooeyMenu.GooeyMenuInterface {

    FirebaseStorage storage;
    StorageReference storageReference;
    DatabaseReference mDatabase ;

    private Button btnChoose, btnUpload;
    private ImageView imageView;

    private VideoView videoView;

    private Uri filePath;

    private final int PICK_IMAGE_REQUEST = 171;

    private GooeyMenu mGooeyMenu;
    private Toast mToast;





    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_card);


        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference("qrcodes");

        mGooeyMenu = (GooeyMenu) findViewById(R.id.gooey_menu);
        mGooeyMenu.setOnMenuListener(this);

        videoView = findViewById(R.id.imgView);

        final MediaController mediacontroller = new MediaController(this);
        mediacontroller.setAnchorView(videoView);


        videoView.setMediaController(mediacontroller);
        videoView.requestFocus();

        videoView.setOnCompletionListener(mp -> Toast.makeText(getApplicationContext(), "Video over", Toast.LENGTH_SHORT).show());

        videoView.setOnErrorListener((mp, what, extra) -> false);
    }


    private void chooseImage() {
        Intent intent = new Intent();
//        intent.setType("image/*");
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                videoView.setVideoURI(filePath);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final String path = "videos/" + UUID.randomUUID().toString();
            final StorageReference ref = storageReference.child(path);

            ref.putFile(filePath)
                    .addOnSuccessListener(taskSnapshot -> {

                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            Toast.makeText(CreateCard.this, "Uploaded "+uri.toString(), Toast.LENGTH_LONG).show();
                            QRUniqueCode.uploaded_video_url = uri.toString();
                            saveInDB();
                        });


                        progressDialog.dismiss();

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();

                        }
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage("Uploaded "+(int)progress+"%");
                    });
        }
    }


    private void saveInDB(){

        mDatabase.child(QRUniqueCode.qr_code).setValue(QRUniqueCode.uploaded_video_url);

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void menuOpen() {
        showToast("Menu Open");

    }

    @Override
    public void menuClose() {
        showToast( "Menu Close");
    }

    @Override
    public void menuItemClicked(int menuNumber) {

            doTask(menuNumber);
    }

    private void doTask(int menuNumber) {

        if(menuNumber == 1){

            chooseImage();

        }else if(menuNumber ==2){
            uploadImage();

        }else if(menuNumber == 3){
            videoView.start();
        }


    }

    private void showToast(String msg){
        if(mToast!=null){
            mToast.cancel();
        }

        mToast= Toast.makeText(this,msg,Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.CENTER,0,0);
        mToast.show();

    }


}
