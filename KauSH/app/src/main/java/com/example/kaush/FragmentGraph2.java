package com.example.kaush;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
/*
public class FragmentGraph2 extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_graph2, container, false);
    }
}
*/

public class FragmentGraph2 extends Fragment
{
    PieChart pieChart;
    float negative_emotion_avg;
    float positive_emotion_avg;

    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Bundle bundle = getArguments();
        negative_emotion_avg = bundle.getFloat("negative_emotion_avg");
        positive_emotion_avg = bundle.getFloat("positive_emotion_avg");
        Log.d("Graph2", "positive and negative : " + positive_emotion_avg + negative_emotion_avg);

        View v = inflater.inflate(R.layout.fragment_graph2, container, false);
        pieChart = (PieChart) v.findViewById(R.id.piechart);

        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5,10,5,5);

        pieChart.setDragDecelerationFrictionCoef(0.95f);

        pieChart.setDrawHoleEnabled(false);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);

        ArrayList<PieEntry> yValues = new ArrayList<PieEntry>();

        yValues.add(new PieEntry(negative_emotion_avg, "부정"));
        yValues.add(new PieEntry(positive_emotion_avg,"긍정"));

        pieChart.animateY(1000, Easing.EasingOption.EaseInOutCubic); //애니메이션

        PieDataSet dataSet = new PieDataSet(yValues, "감정");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        PieData data = new PieData((dataSet));
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.YELLOW);

        pieChart.setData(data);
        pieChart.invalidate(); // 그래프 갱신

        super.onCreate(savedInstanceState);
        return v;

    }
}