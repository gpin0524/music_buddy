package com.github.pwittchen.neurosky.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class IntroBrainWaveActivity extends AppCompatActivity {
    private ProgressDialog MonitorBainwaver;
    private Button btn_EquippedBainwaver;
    private static Toast toast;

    public String situation;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brain_wave_intro);

        textView = findViewById(R.id.textView);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());

        MonitorBainwaver = new ProgressDialog(this);
        btn_EquippedBainwaver = findViewById(R.id.equippedbainwaver);
        btn_EquippedBainwaver.setClickable(false);


        // 從SituationSelect.class 接 situation 資料
        Intent intent  = getIntent();
        situation = intent.getStringExtra("situation");

        btn_EquippedBainwaver.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(IntroBrainWaveActivity.this, YoutubeAllListActivity.class);
                intent.putExtra("situation", situation);
                startActivity(intent);
            }
        });



    }

}
