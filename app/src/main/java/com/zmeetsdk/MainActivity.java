package com.zmeetsdk;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 创建时间: 2020/10/14
 * coder: Alaske
 * description：
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_main);
    }

    public void onStartMeetingClick(View view) {
        EditText editText = findViewById(R.id.et_meet_id);
        String meetingId = editText.getText().toString().trim();
        if (!TextUtils.isEmpty(meetingId)){
            ZMeetSimpleActivity.startMeeting(this,meetingId);
        }

    }
}
