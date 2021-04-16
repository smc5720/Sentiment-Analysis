package com.example.kaush;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1234;
    Button Start;
    TextView Speech;
    Dialog match_text_dialog;
    ListView textlist;
    ArrayList<String> matches_text;
    public String text_data = "입력받지 않음"; // 사용자가 말하는 음성데이터 **
    FileOutputStream outputStream; // 파일 입출력을 위한 파일객체 **
    private TransferUtility transferUtility;
    String probability_list;
    String[] probabilities = new String[3]; // 확률값들만 저장하는 배열
    private CustomDialog customDialog;

    MusicListItem musicListItem;

    String uid, musicDate, musicTitle, musicUrl, musicEmotion;
    DatabaseReference mDBReference = FirebaseDatabase.getInstance().getReference();

    ListView musicYetList;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Start = (Button) findViewById(R.id.btn_google_stt);

        uid = getIntent().getExtras().getString("uid");

        musicYetList = findViewById(R.id.list_view_music_yet);
        final MusicAdapter musicAdapter = new MusicAdapter();
        musicYetList.setAdapter(musicAdapter);

        mDBReference.addValueEventListener(new ValueEventListener() {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                musicAdapter.clearItem();
                for (DataSnapshot snapshot : dataSnapshot.child("account").child(user.getUid()).child("MusicList").getChildren()) {
                    Log.d("MainActivity", "Single ValueEventListener : " + snapshot.getValue());
                    musicDate = snapshot.child("date").getValue().toString();
                    musicEmotion = snapshot.child("emotion").getValue().toString();
                    musicTitle = snapshot.child("title").getValue().toString();
                    musicUrl = snapshot.child("url").getValue().toString();
                    if(musicEmotion.equals("love")){musicEmotion="사랑";}
                    if(musicEmotion.equals("cry")){musicEmotion="울음";}
                    if(musicEmotion.equals("solace")){musicEmotion="위로";}
                    musicListItem = new MusicListItem(musicTitle, musicDate, musicEmotion);
                    musicAdapter.addItem(musicListItem.musicTitleYet, musicListItem.musicDateYet, musicListItem.musicEmotionYet);
                    musicAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        musicYetList.setAdapter(musicAdapter);
        musicYetList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), BottomNavigationActivity.class);
                startActivityForResult(intent,REQUEST_CODE);
            }
        });
        // AMAZONS3CLIENT 객체 생성
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-2:cfe39edd-07c9-4e30-aff8-d313684eb2a3", // Identity Pool ID
                Regions.US_EAST_2 // Region
        );
        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        transferUtility = new TransferUtility(s3, getApplicationContext());

        s3.setRegion(Region.getRegion(Regions.US_EAST_2));
        s3.setEndpoint("s3.us-east-2.amazonaws.com");


        new Thread() {
            public void run() {
                String nodingHtml = getNodingHtml();

                Bundle bun = new Bundle();
                bun.putString("NODING_HTML", nodingHtml);
                Message msg = handler.obtainMessage();
                msg.setData(bun);
                handler.sendMessage(msg);
            }
        }.start();

        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String a = "1";

                if (isConnected()) {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    startActivityForResult(intent, REQUEST_CODE); //
                } else {
                    Toast.makeText(getApplicationContext(), "인터넷에 연결해주세요", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        if (net != null && net.isAvailable() && net.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bun = msg.getData();
            String nodingHtml = bun.getString("NODING_HTML");
            probability_list = nodingHtml;
            dividedProbability();
        }
    };

    private String getNodingHtml() {
        String nodingHtml = "";

        URL url = null;
        HttpURLConnection http = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            url = new URL("https://43sisn313b.execute-api.us-east-2.amazonaws.com/sage-function");
            http = (HttpURLConnection) url.openConnection();
            http.setConnectTimeout(3 * 1000);
            http.setReadTimeout(3 * 1000);

            isr = new InputStreamReader(http.getInputStream());
            br = new BufferedReader(isr);

            String str = null;
            while ((str = br.readLine()) != null) {
                nodingHtml += str + "\n";
            }

        } catch (Exception e) {
            Log.e("Exception", e.toString());
        } finally {
            if (http != null) {
                try {
                    http.disconnect();
                } catch (Exception e) {
                }
            }

            if (isr != null) {
                try {
                    isr.close();
                } catch (Exception e) {
                }
            }

            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                }
            }
        }
        return nodingHtml;
    }

    // [] 를 포함한 확률 리스트에서 확률값들만 뽑아내는 함수
    private void dividedProbability() {
        probabilities = probability_list.split(",");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            match_text_dialog = new Dialog(MainActivity.this);
            match_text_dialog.setContentView(R.layout.dialog_matches_frag);
            match_text_dialog.setTitle("Select Matching Text");
            textlist = (ListView) match_text_dialog.findViewById(R.id.list);
            matches_text = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, matches_text);
            textlist.setAdapter(adapter);

            //선택안하고 첫번째 인덱스의 텍스트를 바로 출력
            // 처음 나타나는 데이터가 가장 높은 확룰의 매칭을 보여주는거 같음.
            text_data = matches_text.get(0);

            String filename = "myfile";
            String string = text_data;

            // 여기서 부터 s3에 저장하는 코드 (아무런 사용자의 activity 없이... 클릭도 안하고... )
            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            TransferObserver observer = (TransferObserver) transferUtility.upload(
                    "noding2",
                    "myfile.txt",
                    new File("/data/data/com.example.kaush/files/myfile")
            );

            //여기서부터 dialog실행 5초 대기 및 다음 액티비티로 넘어가는 과정 해보기
            customDialog = new CustomDialog(MainActivity.this);
            customDialog.show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    customDialog.cancel();

                    Thread t1 = new Thread() {
                        public void run() {
                            String nodingHtml = getNodingHtml();
                            Bundle bun = new Bundle();
                            bun.putString("NODING_HTML", nodingHtml);
                            probability_list = nodingHtml;
                            Message msg = handler.obtainMessage();
                            msg.setData(bun);
                            handler.sendMessage(msg);
                        }
                    };

                    t1.start();
                    try {
                        t1.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    dividedProbability();

                    SimpleDateFormat format = new SimpleDateFormat("MMdd");
                    Date time = new Date();
                    String TIME = format.format(time); // 회원가입한 날짜 기입


                    firebaseAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    DatabaseReference mDBReference = FirebaseDatabase.getInstance().getReference(); // 데이터베이스 접근 객체

                    Float probability1 = Float.parseFloat(probabilities[1]);
                    Float probability2 = Float.parseFloat(probabilities[2]);

                    ArrayList<Float> prob = new ArrayList<Float>();
                    for(int i = 0; i < 3; i++) {
                        prob.add(Float.parseFloat(probabilities[i]));
                    }
                    float max = prob.get(0);
                    for(int i = 0; i < 3; i++) {
                        if(max < prob.get(i)){
                            max = prob.get(i);
                        }
                    }

                    if (max == prob.get(2)) {
                        Intent eintent = new Intent(getApplicationContext(), EmotionActivity.class);
                        eintent.putExtra("text", text_data);
                        startActivityForResult(eintent, REQUEST_CODE);
                        mDBReference.child("account").child(user.getUid()).child("Emotion").child(TIME).child("positive").setValue(probability2);
                        mDBReference.child("account").child(user.getUid()).child("Emotion").child(TIME).child("negative").setValue(probability1);
                    } else if(max == prob.get(1)){
                        Intent eintent = new Intent(getApplicationContext(), EmotionActivity2.class);
                        eintent.putExtra("text", text_data);
                        startActivityForResult(eintent, REQUEST_CODE);
                        mDBReference.child("account").child(user.getUid()).child("Emotion").child(TIME).child("positive").setValue(probability2);
                        mDBReference.child("account").child(user.getUid()).child("Emotion").child(TIME).child("negative").setValue(probability1);
                    } else if(max == prob.get(0)){
                        Toast.makeText(getApplicationContext(), "부적합한 말입니다 다시 말해주세요",Toast.LENGTH_SHORT).show();
                    }
                }
            }, 5000);
        }super.onActivityResult(requestCode,resultCode,data);
    }
}

