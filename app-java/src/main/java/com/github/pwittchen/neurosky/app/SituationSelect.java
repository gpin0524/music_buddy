package com.github.pwittchen.neurosky.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.pwittchen.neurosky.app.Model.MusicDetails;

import java.io.Serializable;
import java.util.ArrayList;

public class SituationSelect extends AppCompatActivity {

    ImageButton btnWork, btnRelax, btnDepress, btnSleep;
    Button  btnJump;
    TextView textView, string_work, string_relax, string_depress, string_sleep;
    Intent i;

    ArrayList<MusicDetails> testList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MusicDetails md = new MusicDetails();
        md.setVideoId("XUR8QByF2As");
        md.setMusicTitle("헤이즈 (Heize) - 저 별 (Star) MV (ENG Sub)");
        testList.add(md);



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_situation_select);
        textView = findViewById(R.id.textView);
        string_work = findViewById(R.id.string_work);
        string_relax = findViewById(R.id.string_relax);
        string_depress = findViewById(R.id.string_depress);
        string_sleep = findViewById(R.id.string_sleep);
        btnWork = findViewById(R.id.btn_work);
        btnRelax = findViewById(R.id.btn_relax);
        btnDepress = findViewById(R.id.btn_depress);
        btnSleep = findViewById(R.id.btn_sleep);
        btnJump = findViewById(R.id.btn_jump);

        btnWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = new Intent(SituationSelect.this, IntroBrainWaveActivity.class);
                i.putExtra("situation", "Work");
                startActivity(i);
            }
        });
        btnRelax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = new Intent(SituationSelect.this, IntroBrainWaveActivity.class);
                i.putExtra("situation", "Relax");
                startActivity(i);
            }
        });
        btnDepress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = new Intent(SituationSelect.this, IntroBrainWaveActivity.class);
                i.putExtra("situation", "Depress");
                startActivity(i);

            }
        });
        btnSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = new Intent(SituationSelect.this, IntroBrainWaveActivity.class);
                i.putExtra("situation", "Sleep");
                startActivity(i);

            }
        });

        btnJump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> newVedioIdArrayList = new ArrayList<>();
                String newPlaylistId = "PLilvBF0rjbWDGeOf6mtK6Qj9MZ-Tyrk0e";

                newVedioIdArrayList.add("OcaaCL6OvsU");
                newVedioIdArrayList.add("4HG_CJzyX6A");
                newVedioIdArrayList.add("P5zlmIRfohs");

                Intent i = new Intent(SituationSelect.this, YoutubeRecActivity.class);
                i.putStringArrayListExtra("newVedioIdArrayList",newVedioIdArrayList); // 傳送篩選後的videoId
                i.putExtra("newPlaylistId", newPlaylistId);
                startActivity(i);
                /*
                String originPlaylistId = "PLilvBF0rjbWBL36yOZoDsfBrtwvIhdZeN";
                String situation = "Relax";
                ArrayList<Integer> newList = new ArrayList<>();
                newList.add(1);

                Intent i = new Intent(SituationSelect.this, YoutubeInsertListActivity.class);
                i.putIntegerArrayListExtra("newList", newList);
                i.putExtra("listId", originPlaylistId);
                i.putExtra("situation", situation);
                startActivity(i);
                */

                /*
                String newPlaylistId = "PLilvBF0rjbWAfLZmsYMAkYVWxVHImG3gf";
                ArrayList<String> newVedioIdArrayList = new ArrayList<>();
                newVedioIdArrayList.add("XUR8QByF2As");
                newVedioIdArrayList.add("Cp56JdkmE9s");

                Intent in = new Intent(SituationSelect.this, AddMusicTagActivity.class);
                Bundle args = new Bundle();
                args.putSerializable("dbNullList",(Serializable)testList);
                in.putExtra("BUNDLE",args);
                in.putStringArrayListExtra("newVedioIdArrayList",newVedioIdArrayList); // 傳送篩選後的videoId
                in.putExtra("newPlaylistId", newPlaylistId);
                startActivity(in);*/

            }
        });
    }
}
