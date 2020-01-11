package com.uom.happycelebrate;


import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.ModelRenderable;

import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.uom.happycelebrate.data.QRUniqueCode;
import com.uom.happycelebrate.utils.CustomArFragment;
import com.uom.happycelebrate.utils.GooeyMenu;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.android.material.snackbar.Snackbar.*;
import static com.google.android.material.snackbar.Snackbar.LENGTH_LONG;

public class ArActionPage extends AppCompatActivity implements GooeyMenu.GooeyMenuInterface {

    private ExternalTexture texture;
    private MediaPlayer mediaPlayer;
    private CustomArFragment arFragment;
    private Scene scene;
    private ModelRenderable renderable;
    private boolean isImageDetected = false;
    private ConstraintLayout mainContainer;
    private ProgressBar progressBar;
    private ScheduledExecutorService mExecutor;
    private Runnable mSeekBarPositionUpdateTask;
    private GooeyMenu mGooeyMenu;

    private BottomNavigationView bottomNavigationView;

    private boolean mediaPlayerState;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_action_page);

        mediaPlayer = new MediaPlayer();

        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();


        mGooeyMenu = (GooeyMenu) findViewById(R.id.gooey_menu);
        mGooeyMenu.setOnMenuListener(this);

        mediaPlayerState = false;


        new Thread(() -> {

            mainContainer = findViewById(R.id.mainContainer);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fg1, new LoadingAssetsFragment());
            ft.commit();

            String skoda = "https://firebasestorage.googleapis.com/v0/b/awsomemurals.appspot.com/o/markers%2Fimage.jpg?alt=media&token=5dc9afb8-e04d-4cbf-95ef-534b8112e5e5";
            String qr = "https://firebasestorage.googleapis.com/v0/b/awsomemurals.appspot.com/o/markers%2Fqr.jpg?alt=media&token=a1ff7b6f-95ee-4c17-a9c7-cc1ab74ac09f";
            String danceMonkey = "https://firebasestorage.googleapis.com/v0/b/awsomemurals.appspot.com/o/markers%2Fmaxresdefault.jpg?alt=media&token=6fb5700d-0e9f-48b6-ae03-1f94242a8a3f";

            Ion.with(ArActionPage.this)
                    .load(QRUniqueCode.qr_image_url)
                    .asBitmap()
                    .setCallback((e, result) -> {

                        Ion.with(ArActionPage.this)
                                .load(QRUniqueCode.video_url)
                                .progress((downloaded, total) -> System.out.println("" + downloaded + " / " + total))
                                .write(new File(directory, "video" + ".mp4"))
                                .setCallback((e1, file) -> {
//
                                    try {

                                        mediaPlayer.setDataSource(ArActionPage.this, Uri.fromFile(new File(directory, "video" + ".mp4")));
//                                        mediaPlayer.setDataSource(ArActionPage.this, Uri.parse("https://firebasestorage.googleapis.com/v0/b/awsomemurals.appspot.com/o/markers%2Fvideoplayback.mp4?alt=media&token=2720204e-0839-4e4f-9f81-3529e76b27e5"));

                                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                        mediaPlayer.prepareAsync();
                                    } catch (IOException ex) {
                                        System.out.println("errrr ex : "+ex.getMessage());
                                        System.out.println("errrr e : "+e1.getMessage());

                                    }


                                    make(mainContainer, "Scan the marker for few seconds, video will start playing soon after", LENGTH_LONG).show();
                                    arFragment = new CustomArFragment(result);
                                    arFragment.setOnSessionInitializationListener(session -> {
                                        texture = new ExternalTexture();
                                        mediaPlayer.setSurface(texture.getSurface());
                                        mediaPlayer.setLooping(true);

                                        ModelRenderable
                                                .builder()
                                                .setSource(ArActionPage.this, Uri.parse("video_screen.sfb"))
                                                .build()
                                                .thenAccept(modelRenderable -> {
                                                    modelRenderable.getMaterial().setExternalTexture("videoTexture",
                                                            texture);
                                                    modelRenderable.getMaterial().setFloat4("keyColor",
                                                            new Color(0.01843f, 1f, 0.098f));
                                                    renderable = modelRenderable;

                                                });

                                        scene = arFragment.getArSceneView().getScene();
                                        scene.addOnUpdateListener(ArActionPage.this::onUpdate);

                                    });

                                    runOnUiThread(getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.fg1, arFragment)::commit);







                                });







                    });


        }).start();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onUpdate(FrameTime frameTime) {

        if (isImageDetected)
            return;

        Frame frame = arFragment.getArSceneView().getArFrame();
        Collection<AugmentedImage> augmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage image : augmentedImages) {

            if (image.getTrackingState() == TrackingState.TRACKING) {

                if (image.getName().equals("image")) {

                    isImageDetected = true;


                    renderVhristmas(image);


                    break;
                }

            }

        }

    }

    private void renderVhristmas(AugmentedImage image) {

        float[] pos = {0, 0, -0.1f};
        float[] pos1 = {0.1f, 0, 0f};

        playVideo(image.createAnchor(image.getCenterPose()), image.getExtentX() * 2, image.getExtentZ());   // if marker is a square
        placeObject(arFragment, image.createAnchor(Pose.makeTranslation(image.getCenterPose().transformPoint(pos))), Uri.parse("tree1.sfb"));
        placeObject(arFragment, image.createAnchor(Pose.makeTranslation(image.getCenterPose().transformPoint(pos1))), Uri.parse("untitled4obj.sfb"));
//                    placeObject(arFragment,image.createAnchor(Pose.makeTranslation(image.getCenterPose().transformPoint(pos2))),Uri.parse(""));


    }

    private void playVideo(Anchor anchor, float extentX, float extentZ) {

        progressBar = findViewById(R.id.progressBar2);

        mediaPlayer.setOnBufferingUpdateListener((mediaPlayer, i) -> progressBar.setSecondaryProgress(i));

        if (mExecutor == null) {
            mExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        if (mSeekBarPositionUpdateTask == null) {
            mSeekBarPositionUpdateTask = new Runnable() {
                @Override
                public void run() {
//                    updateProgressCallbackTask();
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//                        int currentPosition = mediaPlayer.getCurrentPosition();
                        runOnUiThread(() -> {
                            progressBar.setProgress((mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration()) * 100);
                        });
                    }
                }
            };
        }
        mExecutor.scheduleAtFixedRate(
                mSeekBarPositionUpdateTask,
                0,
                300,
                TimeUnit.MILLISECONDS
        );


        AnchorNode anchorNode = new AnchorNode(anchor);
        texture.getSurfaceTexture().setOnFrameAvailableListener(surfaceTexture -> {
            anchorNode.setRenderable(renderable);
            texture.getSurfaceTexture().setOnFrameAvailableListener(null);
        });


        anchorNode.setWorldScale(new Vector3(extentX, 1f, extentZ));

        scene.addChild(anchorNode);

        mediaPlayer.start();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void placeObject(ArFragment arFragment, Anchor anchor, Uri uri) {

        ModelRenderable.builder()
//                .setSource(arFragment.getContext(), RenderableSource.builder().setSource(this,
//                        Uri.parse("http://192.168.1.8/3d/aventador.glb"),
//                .setSource(arFragment.getContext(), RenderableSource.builder().setSource(this,
//                        Uri.parse("untitled4obj.sfb"),
//                        RenderableSource.SourceType.GLB).build())
                .setSource(ArActionPage.this, uri)
                .build()
                .thenAccept(modelRenderable -> addNodeToScene(arFragment, anchor, modelRenderable))
                .exceptionally(throwable -> {
                            Toast.makeText(arFragment.getContext(), "Error:" + throwable.getMessage(), Toast.LENGTH_LONG).show();
                            return null;
                        }
                );
    }

    private void addNodeToScene(ArFragment arFragment, Anchor anchor, Renderable renderable) {

        AnchorNode anchorNode = new AnchorNode(anchor);

        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());

//        node.getTransformationSystem().getSelectionVisualizer().removeSelectionVisual(node);
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void menuOpen() {
//        showToast("Menu Open");
//
    }

    @Override
    public void menuClose() {

    }

    @Override
    public void menuItemClicked(int menuNumber) {

        Toast.makeText(ArActionPage.this, "clicked " + menuNumber, Toast.LENGTH_LONG).show();
        doTask(menuNumber);
    }

    private void doTask(int menuNumber) {

        if (menuNumber == 1) {

            if (mediaPlayerState) {
                mediaPlayer.start();
            } else {
                mediaPlayer.pause();
            }
            mediaPlayerState = !mediaPlayerState;

        } else if (menuNumber == 2) {

            mediaPlayer.stop();
            mediaPlayer.release();

            Intent intent = new Intent(ArActionPage.this, CardsListPage.class);
            startActivity(intent);
            ArActionPage.this.finish();

        }


    }


}
