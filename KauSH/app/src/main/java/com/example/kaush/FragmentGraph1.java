package com.example.kaush;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class FragmentGraph1 extends Fragment
{
    HashMap<String, Float> negative_emotion_map = new HashMap<String, Float>();
    HashMap<String, Float> positive_emotion_map = new HashMap<String, Float>(); //  날짜와 그때의 감정을 저장하는 해시
    int count = 0;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    DatabaseReference mDBReference = FirebaseDatabase.getInstance().getReference(); // 데이터베이스 접근 객체

    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_graph1, container, false);
        LineChart lineChart = (LineChart) v.findViewById(R.id.chart);

        Set<String> negative_keySet = negative_emotion_map.keySet();
        Set<String> positive_keySet = positive_emotion_map.keySet();
        ArrayList<Entry> negative_entries = new ArrayList<>();
        ArrayList<Entry> positive_entries = new ArrayList<>();

        ArrayList<String> key_list = new ArrayList<>();

        for (String key : negative_keySet) {
            count++;
            key_list.add(key); //key_list: 날짜들이 들어있음 (float)
        }
        Collections.sort(key_list); // 날짜들이 순서대로 들어가지 않아 날짜들을 기준으로 sort하고 다시 대입
        for(String key : key_list)
        {
            negative_entries.add(new Entry(Integer.parseInt(key), (negative_emotion_map.get(key))));
            positive_entries.add(new Entry(Integer.parseInt(key), (positive_emotion_map.get(key))));
        }

        System.out.println(negative_entries);
        System.out.println(positive_entries);

        LineDataSet negative_linedataset = new LineDataSet(negative_entries, "부정");
        negative_linedataset.setLineWidth(2); // 선 굵기
        negative_linedataset.setColors(Color.MAGENTA);
        LineDataSet positive_linedataset = new LineDataSet(positive_entries, "긍정");
        positive_linedataset.setLineWidth(2); // 선 굵기
        negative_linedataset.setColors(Color.YELLOW);

        XAxis xAxis = lineChart.getXAxis(); // x 축 설정
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); //x 축 표시에 대한 위치 설정
        xAxis.setLabelCount(count, true);

        YAxis yAxisRight = lineChart.getAxisRight(); //Y축의 오른쪽면 설정
        yAxisRight.setDrawLabels(false);
        yAxisRight.setDrawAxisLine(false);
        yAxisRight.setDrawGridLines(false);
        //y축의 활성화를 제거함

        LineData chardata = new LineData();
        chardata.addDataSet(negative_linedataset);
        chardata.addDataSet(positive_linedataset);
        lineChart.setData(chardata);
        lineChart.animateY(2000);
        lineChart.invalidate();

        return v;
    }
    public void setEmotionMap(HashMap<String, Float> positive_emotion_map, HashMap<String, Float> negative_emotion_map){
        this.positive_emotion_map = positive_emotion_map;
        this.negative_emotion_map = negative_emotion_map;
    }
}