package vibudhvishal.videorecorder;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    VideoView result_video;

    static final int REQUEST_VIDEO_CAPTURE = 1;
    private FirebaseStorage storageRef;
    private Uri videoUri;
    private FirebaseDatabase database;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        result_video = findViewById(R.id.videoView);
        storageRef = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();



    }

    public void dispatchTakeVideoIntent(View view) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            videoUri = intent.getData();
            result_video.setVideoURI(videoUri);
            result_video.start();
            Log.i(TAG, "onActivityResult: Saving at: " + getRealPathFromURI(videoUri));

        }
    }

    public void uploadvideo(View view) {
        final Video video = new Video();
        Uri file = Uri.fromFile(new File(getRealPathFromURI(videoUri)));
        final StorageReference videoRef = storageRef.getReference("videos/" + file.getLastPathSegment());
        final StorageTask<UploadTask.TaskSnapshot> uploadTask = videoRef.putFile(file);

// Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i(TAG, "onFailure: " + exception);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                video.setContentType(taskSnapshot.getMetadata().getContentType());
                video.setCreateTime(taskSnapshot.getMetadata().getCreationTimeMillis());
                video.setUpdateTime(taskSnapshot.getMetadata().getUpdatedTimeMillis());
                video.setFileName(taskSnapshot.getMetadata().getName());
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                ((Button) findViewById(R.id.uploadbtn)).setText(String.format("Uploading %s%%", String.valueOf((taskSnapshot.getBytesTransferred() * 100) / taskSnapshot.getTotalByteCount())));
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                videoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        video.setDownloadUri(uri.toString());
                        Log.i(TAG, "onSuccess: dUri: "+uri.toString());
                        addVideoToList(video);
                    }
                });

            }
        });
    }

    private void addVideoToList(Video video) {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.i(TAG, "addVideoToList: uid: " + uid);
        DatabaseReference vidListRef = database.getReference(uid + "/" + Calendar.getInstance().getTimeInMillis());
        Log.i(TAG, "addVideoToList: "+video.getFileName()+"\n"+video.getDownloadUri()+"\n"+video.getContentType()+"\n"+video.getCreateTime()+"\n"+video.getUpdateTime());
        vidListRef.setValue(video).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "onComplete: video uploaded");
                ((Button) findViewById(R.id.uploadbtn)).setText("upload");
            }
        });

    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Video.Media.DATA};
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }
}
