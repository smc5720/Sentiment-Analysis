package com.example.googlestt_0;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
////////////////////////////
public class MainActivity extends Activity {
    private static final int REQUEST_CODE = 1234;
    Button Start;
    TextView Speech;
    Dialog match_text_dialog;
    ListView textlist;
    ArrayList<String> matches_text;
    public String text_data; // 사용자가 말하는 음성데이터 **
    FileOutputStream outputStream; // 파일 입출력을 위한 파일객체 **
    private TransferUtility transferUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Start = (Button)findViewById(R.id.start_reg); // 이걸 클릭하고 녹음을 시작한다.
        Speech = (TextView)findViewById(R.id.speech);

        // AMAZONS3CLIENT 객체 생성
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-2:86ccb826-e4c8-4de2-b167-199c79848012", // Identity Pool ID
                Regions.US_EAST_2 // Region
        );
        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        transferUtility = new TransferUtility(s3, getApplicationContext());

        s3.setRegion(Region.getRegion(Regions.US_EAST_2));
        s3.setEndpoint("s3.us-east-2.amazonaws.com");


        // 시작하자마자 녹음하기
        if(isConnected()){
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            startActivityForResult(intent, REQUEST_CODE); //
        }
        else{
            Toast.makeText(getApplicationContext(), "Plese Connect to Internet", Toast.LENGTH_LONG).show();
        }
        // 여기까지

        //*** 추가작업) 혹시 못했을 경우 버튼을 눌러서 녹음을 시작할 수 있게 했음
        // 일단 시작은 바로 녹음 할 수 있고, 혹시 인식 못하면 탭 해서 다시 하게 해주거나 아니면 talk to me 버튼 눌러서 녹음 할 수 있게 해줘
        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnected()){
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    startActivityForResult(intent, REQUEST_CODE); //
                }
                else{
                    Toast.makeText(getApplicationContext(), "Plese Connect to Internet", Toast.LENGTH_LONG).show();
                }}

        });


    }
    public  boolean isConnected()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        if (net!=null && net.isAvailable() && net.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    // 파일화 만들기
    /*
    public void InternalStorageSave(View view)
    {
        internalStorageSaveFile();

        TransferObserver observer = (TransferObserver) transferUtility.upload(
                "noding/text_data",
                "myfile.txt",
                new File("/data/data/com.example.googlestt_0/files/myfile")
        );
    }

    public void internalStorageSaveFile()
    {

        String filename = "myfile";
        String string = text_data;
        // String string = text_data;
        // 처음 나타나는 데이터가 가장 높은 확룰의 매칭을 보여주는거 같음.


        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();

            Toast.makeText(this, "this is internal storage save success.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(this, "this is internal storage save fail.", Toast.LENGTH_LONG).show();
        }
    }
    */


    // ~ 파일화 만들기

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            match_text_dialog = new Dialog(MainActivity.this);
            match_text_dialog.setContentView(R.layout.dialog_matches_frag);
            match_text_dialog.setTitle("Select Matching Text");
            textlist = (ListView)match_text_dialog.findViewById(R.id.list);
            matches_text = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, matches_text);
            textlist.setAdapter(adapter);

            //선택안하고 첫번째 인덱스의 텍스트를 바로 출력
            // 처음 나타나는 데이터가 가장 높은 확룰의 매칭을 보여주는거 같음.
            text_data = matches_text.get(0);
            Speech.setText("<  "+text_data+"  >");

            String filename = "myfile";
            String string = text_data;
            // String string = text_data;


            // 여기서 부터 s3에 저장하는 코드 (아무런 사용자의 activity 없이... 클릭도 안하고... )
            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();

                Toast.makeText(this, "this is internal storage save success.", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();

                Toast.makeText(this, "this is internal storage save fail.", Toast.LENGTH_LONG).show();
            }
            TransferObserver observer = (TransferObserver) transferUtility.upload(
                    "noding",
                    "myfile.txt",
                    new File("/data/data/com.example.googlestt_0/files/myfile")
            );
            // 여기까지


            /*
            textlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Speech.setText("You have said " + "\n" + matches_text.get(position));
                    text_data = matches_text.get(position); // 말하는 데이터를 저장한다.
                    match_text_dialog.hide();

                }
            });
            */
            //match_text_dialog.show();

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}