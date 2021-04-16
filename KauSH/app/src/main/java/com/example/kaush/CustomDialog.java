package com.example.kaush;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;


import androidx.annotation.NonNull;

public class CustomDialog extends Dialog {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //다이얼로그 밖의 화면은 흐리게 만들어줌
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.8f;
        getWindow().setAttributes(layoutParams);

        setContentView(R.layout.custom_dialog);
    }

    //생성자 생성
    public CustomDialog(@NonNull Context context) {
        super(context);
    }
}