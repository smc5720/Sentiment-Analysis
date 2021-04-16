package com.example.kaush;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class EmotionActivity2 extends AppCompatActivity {

    String userText;
    MusicInfo sampleMusic;
    MusicInfo sampleMusic2;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    Random random = new Random();
    String rand, rand2, musicDate,musicTitle,musicUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotion2);

        Intent intent = getIntent();

        userText = intent.getExtras().getString("text");

        Button btnSolaceMusic = findViewById(R.id.btn_music_solace);
        btnSolaceMusic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                rand = Integer.toString(random.nextInt(32)+1);
                rand2 = Integer.toString(random.nextInt(32)+1);
                setMusicFromDatabase("solace");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent mintent = new Intent(getApplicationContext(),MusicActivity.class);
                        mintent.putExtra("emotion", "solace");
                        mintent.putExtra("MUSIC1",sampleMusic);
                        mintent.putExtra("MUSIC2",sampleMusic2);
                        mintent.putExtra("random1",rand);
                        mintent.putExtra("random2",rand2);
                        startActivityForResult(mintent,1);
                    }
                },1000);
            }
        });

        Button btnCryMusic = findViewById(R.id.btn_music_cry);
        btnCryMusic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                rand = Integer.toString(random.nextInt(35)+1);
                rand2 = Integer.toString(random.nextInt(35)+1);
                setMusicFromDatabase("cry");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent mintent = new Intent(getApplicationContext(),MusicActivity.class);
                        mintent.putExtra("emotion", "cry");
                        mintent.putExtra("MUSIC1",sampleMusic);
                        mintent.putExtra("MUSIC2",sampleMusic2);
                        mintent.putExtra("random1",rand);
                        mintent.putExtra("random2",rand2);
                        startActivityForResult(mintent,1);
                    }
                },1000);

            }
        });
    }

    public void setMusicFromDatabase(String emot){
        final String emotion = emot;
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            int i = 0;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.child(emotion).child(rand).getChildren()) {
                    i++;
                    if(i == 1){musicDate = snapshot.getValue().toString();}
                    if(i == 2){musicTitle = snapshot.getValue().toString();}
                    if(i == 3){musicUrl = snapshot.getValue().toString();
                        sampleMusic = new MusicInfo(musicTitle, musicDate, musicUrl,emotion); i = 0;}
                    Log.d("MainActivity", "Single ValueEventListener : " + snapshot.getValue());
                }
                for (DataSnapshot snapshot : dataSnapshot.child(emotion).child(rand2).getChildren()) {
                    i++;
                    if(i==1){musicDate = snapshot.getValue().toString();}
                    if(i==2){musicTitle = snapshot.getValue().toString();}
                    if(i==3){musicUrl = snapshot.getValue().toString();
                        sampleMusic2 = new MusicInfo(musicTitle, musicDate, musicUrl,emotion); i = 0;}
                    Log.d("MainActivity", "Single ValueEventListener : " + snapshot.getValue());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
