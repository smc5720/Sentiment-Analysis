package com.example.kaush;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import org.w3c.dom.Text;


public class MusicActivity extends AppCompatActivity {

    MusicInfo sampleMusic;
    MusicInfo sampleMusic2;

    TextView musicEmotionText;
    ImageView musicImageView1;
    ImageView musicImageView2;

    DatabaseReference mDBReference = FirebaseDatabase.getInstance().getReference(); // 데이터베이스 접근 객체

    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference = firebaseStorage.getReference();

    Random random = new Random();
    String rand, rand2 = "1";

    private FirebaseAuth firebaseAuth;

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date time = new Date();
    String TIME = format.format(time); // 회원가입한 날짜 기입
    String textEmotion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        musicEmotionText = findViewById(R.id.text_music_emotion);
        musicImageView1 = findViewById(R.id.image_music_1);
        musicImageView2 = findViewById(R.id.image_music_2);

        TextView musicTitleText = findViewById(R.id.text_music_title);
        TextView musicTitleText2 = findViewById(R.id.text_music_title2);
        LinearLayout musicLinearLayout = findViewById(R.id.music_linear_layout);
        LinearLayout musicLinearLayout2 = findViewById(R.id.music_linear_layout2);

        Intent intent = getIntent();

        textEmotion = intent.getExtras().getString("emotion");
        sampleMusic = intent.getExtras().getParcelable("MUSIC1");
        sampleMusic2 = intent.getExtras().getParcelable("MUSIC2");

        rand = intent.getExtras().getString("random1");
        rand2 = intent.getExtras().getString("random2");

        if(textEmotion.equals("love")) {
                setActivityLove();
            }
        else if(textEmotion.equals("solace")){
            setActivitySolace();
            }
        else if(textEmotion.equals("cry")) {
                        setActivityCry();
            }
        else { }

        musicTitleText.setText(sampleMusic.title);

        musicTitleText2.setText(sampleMusic2.title);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                switch (view.getId()){
                    case R.id.music_linear_layout:
                        startActivity(new Intent(Intent.ACTION_VIEW)
                                .setData(Uri.parse(sampleMusic.url)) // edit this url
                                .setPackage("com.google.android.youtube"));	// do not edit
                        mDBReference.child("account").child(user.getUid()).child("MusicList").child(TIME).setValue(sampleMusic);

                        break;
                    case R.id.music_linear_layout2:
                        startActivity(new Intent(Intent.ACTION_VIEW)
                                .setData(Uri.parse(sampleMusic2.url)) // edit this url
                                .setPackage("com.google.android.youtube"));	// do not edit
                        mDBReference.child("account").child(user.getUid()).child("MusicList").child(TIME).setValue(sampleMusic2);

                        break;
                }
            }
        };
        musicLinearLayout.setOnClickListener(clickListener);
        musicLinearLayout2.setOnClickListener(clickListener);
    }

    public void setActivityLove(){
        musicEmotionText.setText("같이 이 노래 들어보는거 어때요?");
        getImageFromFirebase("love",rand,rand2);
    }

    public void setActivitySolace(){
        musicEmotionText.setText("이 노래 들으면 위로가 될거에요");
        getImageFromFirebase("solace",rand,rand2);
    }

    public void setActivityCry(){
        musicEmotionText.setText("펑펑 울고 털어내세요");
        getImageFromFirebase("cry",rand,rand2);
    }

    public void getImageFromFirebase(String textEmotion, String rand, String rand2){
        Log.d("MusicActivity", storageReference + textEmotion + "/" + rand2 + ".jpg");
        Log.d("MusicActivity", storageReference + textEmotion + "/" + rand2 + ".jpg");
        Log.d("MusicActivity", storageReference + textEmotion + "/" + rand2 + ".jpg");
        StorageReference storageReference1 = storageReference.child(textEmotion + "/" + rand + ".jpg");
        storageReference1.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    // Glide 이용하여 이미지뷰에 로딩
                    Glide.with(MusicActivity.this).load(task.getResult()).into(musicImageView1);
                } else {
                    // URL을 가져오지 못하면 토스트 메세지
                    Toast.makeText(MusicActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        StorageReference storageReference2 = storageReference.child(textEmotion + "/" + rand2 + ".jpg");
        storageReference2.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    // Glide 이용하여 이미지뷰에 로딩
                    Glide.with(MusicActivity.this).load(task.getResult()).into(musicImageView2);
                } else {
                    // URL을 가져오지 못하면 토스트 메세지
                    Toast.makeText(MusicActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
