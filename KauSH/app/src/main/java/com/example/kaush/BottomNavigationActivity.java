package com.example.kaush;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class BottomNavigationActivity extends AppCompatActivity {

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private FragmentMusicList fragmentMusicList = new FragmentMusicList();
    private FragmentGraph1 fragmentGraph1 = new FragmentGraph1();
    private FragmentGraph2 fragmentGraph2 = new FragmentGraph2();

    HashMap<String, Float> negative_emotion_map = new HashMap<String, Float>();
    HashMap<String, Float> positive_emotion_map = new HashMap<String, Float>();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser user = firebaseAuth.getCurrentUser();
    DatabaseReference mDBReference = FirebaseDatabase.getInstance().getReference(); // 데이터베이스 접근 객체
    int emotion_count = 0; // 그동안 몇번의 감정이 있었는지 카운팅
    float negative_emotion_total;
    float positive_emotion_total;
    float negative_emotion_avg;
    float positive_emotion_avg;

    Bundle bundle = new Bundle(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frameLayout, fragmentMusicList).commitAllowingStateLoss();

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new ItemSelectedListener());

        mDBReference.addValueEventListener(new ValueEventListener() {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for (DataSnapshot snapshot : dataSnapshot.child("account").child(user.getUid()).child("Emotion").getChildren())
                {
                    emotion_count++;
                    negative_emotion_total = negative_emotion_total + Float.parseFloat(snapshot.child("negative").getValue().toString());
                    positive_emotion_total = positive_emotion_total + Float.parseFloat(snapshot.child("positive").getValue().toString());
                }
                for (DataSnapshot snapshot : dataSnapshot.child("account").child(user.getUid()).child("Emotion").getChildren())
                {
                    negative_emotion_map.put(snapshot.getKey().toString(),
                            Float.parseFloat(snapshot.child("negative").getValue().toString()));
                    positive_emotion_map.put(snapshot.getKey().toString(),
                            Float.parseFloat(snapshot.child("positive").getValue().toString()));
                }
                negative_emotion_avg = negative_emotion_total / emotion_count;
                positive_emotion_avg = positive_emotion_total / emotion_count;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    class ItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener{
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            switch(menuItem.getItemId())
            {
                case R.id.navigation_music_list:
                    transaction.replace(R.id.frameLayout, fragmentMusicList).commitAllowingStateLoss();

                    break;
                case R.id.navigation_graph1:
                    fragmentGraph1.setEmotionMap(positive_emotion_map, negative_emotion_map);
                    transaction.replace(R.id.frameLayout, fragmentGraph1).commitAllowingStateLoss();
                    break;
                case R.id.navigation_graph2:
                    bundle.putFloat("negative_emotion_avg", negative_emotion_avg);
                    bundle.putFloat("positive_emotion_avg", positive_emotion_avg);
                    fragmentGraph2.setArguments(bundle);
                    transaction.replace(R.id.frameLayout, fragmentGraph2).commitAllowingStateLoss();
                    break;
            }
            return true;
        }
    }
}
