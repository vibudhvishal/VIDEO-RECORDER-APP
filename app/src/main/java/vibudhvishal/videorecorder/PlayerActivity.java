package vibudhvishal.videorecorder;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = "PlayerActivity";

    private VideoView playerView;
    private ProgressBar loading;
    private FirebaseStorage storage;
    private String url;
    private ImageButton btnPlay, btnPause;
    private SeekBar seekBar;
    private LinearLayout lytControls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            url = b.getString("url");
        }

        playerView = findViewById(R.id.playerView);
        loading = findViewById(R.id.loading);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        seekBar = findViewById(R.id.seekBar);
        lytControls = findViewById(R.id.lytControls);

        storage = FirebaseStorage.getInstance();

        final StorageReference videoRef = storage.getReferenceFromUrl(url);

        File localFile = null;
        try {
            localFile = File.createTempFile("vid_", "mp4");
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (playerView.isPlaying()) {
                    Log.i(TAG, "run: "+playerView.getCurrentPosition());
                    seekBar.setProgress(playerView.getCurrentPosition());
                }
            }
        });

        final File finalLocalFile = localFile;
        videoRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                loading.setVisibility(View.GONE);
                //Log.i(TAG, "onSuccess: localVidPath: "+finalLocalFile.getPath());
                playerView.setVideoPath(finalLocalFile.getPath());
                playerView.start();
                Log.i(TAG, "onSuccess: "+getVideoLength(finalLocalFile));
                seekBar.setMax(getVideoLength(finalLocalFile));
                lytControls.setVisibility(View.VISIBLE);
                t.start();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

        playerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lytControls.getVisibility() == View.GONE)
                    lytControls.setVisibility(View.VISIBLE);
                else
                    lytControls.setVisibility(View.GONE);
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (seekBar.getProgress() == seekBar.getMax()) {
                    playerView.start();
                } else {
                    playerView.resume();
                }
            }
        });
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerView.pause();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                playerView.seekTo(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });





    }

    private int getVideoLength(File file){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(PlayerActivity.this, Uri.fromFile(file));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int timeInMillisec = Integer.parseInt(time);

        retriever.release();
        return timeInMillisec;
    }
}
