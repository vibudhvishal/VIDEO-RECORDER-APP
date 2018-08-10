package vibudhvishal.videorecorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Home extends AppCompatActivity {

    private static final String TAG = "Home";

    private ListView listFiles;
    private FirebaseDatabase database;
    private ProgressBar loading;
    private FileListAdapter fileListAdapter;
    private ArrayList<Video> videos;
    private String uid;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        user = mAuth.getCurrentUser();
        if (user == null)
            mAuth.signInAnonymously();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listFiles = findViewById(R.id.listFiles);
        loading = findViewById(R.id.loading);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        videos = new ArrayList<>();
        fileListAdapter = new FileListAdapter(this, videos);
        uid = FirebaseAuth.getInstance().getUid();

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(Home.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Home.this, MainActivity.class));
            }
        });

        listFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(Home.this,PlayerActivity.class);
                intent.putExtra("url",((Video)adapterView.getAdapter().getItem(i)).getDownloadUri());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getVideoList();
    }

    private void getVideoList() {
        listFiles.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        videos.clear();
        DatabaseReference vidListRef = database.getReference(uid);
        vidListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        videos.add(snapshot.getValue(Video.class));
                    }
                    Log.i(TAG, "onDataChange: videosSize: " + videos.size());
                    fileListAdapter.notifyDataSetChanged();
                    if (fileListAdapter.getCount() > 0) {
                        listFiles.setAdapter(fileListAdapter);
                        loading.setVisibility(View.GONE);
                        listFiles.setVisibility(View.VISIBLE);
                    }
                } else {
                    loading.setVisibility(View.GONE);
                    listFiles.setVisibility(View.VISIBLE);
                    Toast.makeText(Home.this, "No Video files found", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
