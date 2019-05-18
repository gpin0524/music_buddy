package com.github.pwittchen.neurosky.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.charts.LineChart;
import com.github.pwittchen.neurosky.app.Model.DynamicLineChartManager;
import com.github.pwittchen.neurosky.app.Model.EEGdata;
import com.github.pwittchen.neurosky.library.NeuroSky;
import com.github.pwittchen.neurosky.library.exception.BluetoothNotEnabledException;
import com.github.pwittchen.neurosky.library.listener.ExtendedDeviceMessageListener;
import com.github.pwittchen.neurosky.library.message.enums.BrainWave;
import com.github.pwittchen.neurosky.library.message.enums.Signal;
import com.github.pwittchen.neurosky.library.message.enums.State;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.content.ContentValues.TAG;


public class YoutubePlayerActivity extends YouTubeBaseActivity {

    private static final String TAG = "YoutubePlayerActivity";
    private static final String TAG2 = "Calculate";
    private static final String TAG3 = "Bwd";
    private static final String[] SCOPES = {YouTubeScopes.YOUTUBE};
    private GoogleSignInClient mGoogleSignInClient;

    YouTubePlayerView youTubePlayerView;
    //Button btnPlay;
    AlertDialog.Builder endAlertdialog; // 腦波測試結束
    AlertDialog.Builder nullAlertdialog; // 無有用歌曲 newList.size() == 0
    AlertDialog.Builder smAlertdialog; // start monitoring alert dialog
    AlertDialog.Builder playAlertdialog;

    TextView tvState;
    Button connect;
    //Button disconnect;



    YouTubePlayer.OnInitializedListener mOnInitializedListener;

    int videoIndex;
    String playlistId;
    String situation;
    String userId;
    String selectedAccount;
    //ArrayList<Integer> uploadListIndex = new ArrayList<>();
    ArrayList<Integer> newList = new ArrayList<>();
    ArrayList<String> videoIdArrayList = new ArrayList<>();





    private static YouTube mService = null;

    // 腦波
    //BWData bwd = new BWData(this);

    private final static String LOG_TAG = "NeuroSky";

    private NeuroSky neuroSky;
    private FileOutputStream fileOutputStream;//以後可json建立檔案用
    private FileInputStream fileInputStream; //以後可json建立檔案用
    private FirebaseFirestore db;
    private Map<String, Object> brainWaveItem;
    private Map<String, Object> brainWaveData;
    private Map<String, Integer> brainWaveRow;
    private Map<String, Integer> bridge;//從handleBrainWavesChange傳到外面

    ArrayList<EEGdata> rowData = new ArrayList<>();
    ArrayList<EEGdata> eegData;  // 一首歌的腦波資料( Switch 哪個狀態就哪個腦波)





    private double w = 0; // 腦波計算權重初始值
    private Integer tempA;
    private Integer tempM;
    private int count_raw = 0;
    private int count_playing = 0;




    private Boolean record = false;
    private String MusicName = "Music Name";
    public  String documentID ; // documentID :宣告全域

    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd HH:mm");
    private Date dt = new Date();
    private final String date_doc  = sdf.format(dt);


    //MPandroidchart defination

    private DynamicLineChartManager dynamicLineChartManager1;
    private DynamicLineChartManager dynamicLineChartManager2;
    private List<Integer> listgraphdata = new ArrayList<>(); //数据集合
    private List<String> names = new ArrayList<>(); //折线名字集合
    private List<Integer> colour = new ArrayList<>();//折线颜色集合

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_player);
        // 腦波
        neuroSky = createNeuroSky();
        // 認證
        GoogleAccountCredential mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new YouTube.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("YouTube Data API Android Quickstart")
                .build();
        // 接值 from MyVideoAdapter
        Intent i = getIntent();
        playlistId = i.getStringExtra("playlistId");
        videoIndex = -1;
        situation = i.getStringExtra("situation");
        videoIdArrayList = i.getStringArrayListExtra("videoIdArrayList");
        userId = i.getStringExtra("userId");
        selectedAccount = i.getStringExtra("selectedAccount");
        Log.d(TAG, "onCreat: playlistId: " + playlistId);
        Log.d(TAG, "onCreate: videoIdArrayList = " + videoIdArrayList);

        // 介面宣告
        tvState = findViewById(R.id.tv_state);
        connect = findViewById(R.id.btn_connect);
        //disconnect = findViewById(R.id.btn_disconnect);

        //MPandroidchart
        MPandroidchart();

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    neuroSky.connect();

                    // startMonitoring(); // 一開始連上腦波儀 開始測raw data

                } catch (BluetoothNotEnabledException e) {
                    Toast.makeText(YoutubePlayerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, e.getMessage());
                }
            }
        });
        youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youTubePlayerView);

        // 設定 AlertDialog
        // 腦波測試結束
        endAlertdialog = new AlertDialog.Builder(this);
        endAlertdialog.setMessage(R.string.dialog_message)
                .setTitle(R.string.dialog_title);
        endAlertdialog.setCancelable(false);
        endAlertdialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                neuroSky.disconnect();
                // Activity 跳至 YoutubeInsertListActivity
                Intent i = new Intent(YoutubePlayerActivity.this, YoutubeInsertListActivity.class);
                i.putExtra("selectedAccount", selectedAccount);
                i.putIntegerArrayListExtra("newList", newList);
                i.putExtra("listId", playlistId);
                i.putExtra("videoIndex", videoIndex);
                i.putExtra("situation", situation);
                Log.d(TAG, "Player 情境狀態 situation: " + situation);
                startActivity(i);
            }
        });
        // start monitoring alert dialog
        smAlertdialog = new AlertDialog.Builder(this);
        smAlertdialog.setMessage("將開始測RawData\nStart recording Raw Data")
                     .setTitle("腦波儀連接完成\nEEG has been connected");
        smAlertdialog.setCancelable(false);
        smAlertdialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startMonitoring();
            }
        });
        playAlertdialog = new AlertDialog.Builder(this);
        playAlertdialog.setMessage("將播放音樂\nMusic will start playing immediately")
                .setTitle("RawData測試完成\nRawData has been recorded");
        playAlertdialog.setCancelable(false);
        playAlertdialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PlayMusic();
            }
        });
        // 無有用歌曲
        nullAlertdialog = new AlertDialog.Builder(this);
        nullAlertdialog.setMessage("是否選擇其他歌單?\nDo you want to choose another play list?")
                .setTitle("此歌單無適合歌曲\nSeems like this play list doesn't fits you");
        nullAlertdialog.setCancelable(false);
        nullAlertdialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                neuroSky.disconnect();
                Intent i = new Intent(YoutubePlayerActivity.this, YoutubeAllListActivity.class);
                i.putExtra("situation", situation);
                startActivity(i);
            }
        });
        nullAlertdialog.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(YoutubePlayerActivity.this, SituationSelect.class);
                startActivity(i);
            }
        });

        // YoutubePlayer 初始化
        mOnInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                Log.d(TAG, "onClick: Done initializing.");


                //List<String> vidioList = new ArrayList<>();
                // vidioList.add("H8NCOA2bK6k");
                // vidioList.add("pHtxTSiPh5I");
                //youTubePlayer.loadVideos(vidioList);

                youTubePlayer.loadPlaylist(playlistId);

                youTubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                    @Override
                    public void onLoading() {
                    }

                    @Override
                    public void onLoaded(String s) {

                    }

                    @Override
                    public void onAdStarted() {

                    }

                    @Override
                    public void onVideoStarted() {
                        // 開始上傳腦波資料
                        //bwd.startMonitoring();
                        startMonitoring();
                        w = 0; // W值重設
                        videoIndex += 1;
                        tempA = -1;
                        tempM = -1;
                        count_playing = 0;
                        Log.d(TAG, "onVideoStarted: 正在播放第" + videoIndex + "首");
                    }

                    @Override
                    public void onVideoEnded() {
                        // 結束上傳腦波資料
                        //bwd.stopMonitoring();
                        stopMonitoring();
                    }

                    @Override
                    public void onError(YouTubePlayer.ErrorReason errorReason) {

                    }
                });
                youTubePlayer.setPlaylistEventListener(new YouTubePlayer.PlaylistEventListener() {
                    @Override
                    public void onPrevious() {

                    }

                    @Override
                    public void onNext() {

                    }

                    @Override
                    public void onPlaylistEnded() {
                        Log.d(TAG2, "最終newList: " + newList);
                        if(newList.size() == 0){
                            nullAlertdialog.show();
                        }
                        else{
                            endAlertdialog.show();    //跳到 Insert Activity
                        }

                    }
                });
                /*
                youTubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
                    @Override
                    public void onPlaying() {
                        startMonitoring();
                    }

                    @Override
                    public void onPaused() {
                        stopMonitoring();
                    }

                    @Override
                    public void onStopped() {
                        stopMonitoring();
                    }

                    @Override
                    public void onBuffering(boolean b) {
                        Toast.makeText(YoutubePlayerActivity.this, "緩衝中....", Toast.LENGTH_LONG).show();
                        stopMonitoring();
                    }

                    @Override
                    public void onSeekTo(int i) {

                    }
                });*/


            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.d(TAG, "onClick: Failed to initializing.");

            }
        };
        /*
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Initializing youtube player.");
                youTubePlayerView.initialize(YoutubeConfig.getApiKey(), mOnInitializedListener);
                Log.d(TAG, "onClick: Done initializing.");

            }
        });*/
    }
    @NonNull
    public NeuroSky createNeuroSky() {
        return new NeuroSky(new ExtendedDeviceMessageListener() {

            @Override
            public void onStateChange(State state) {
                handleStateChange(state);
            }

            @Override
            public void onSignalChange(Signal signal) {
                handleSignalChange(signal);
            }


            @Override
            public void onBrainWavesChange(Set<BrainWave> brainWaves) {
                handleBrainWavesChange(brainWaves);
            }
        });
    }

    public void handleStateChange(final State state) {
        if (neuroSky != null && state.equals(State.CONNECTED)) {
            neuroSky.start();
            smAlertdialog.show();
        }

        tvState.setText(state.toString());
        Log.d(LOG_TAG, state.toString());
    }

    Integer attention = -1;
    Integer mediatation = -1;
    Integer poorSignal = -1;


    public void handleBrainWavesChange(final Set<BrainWave> brainWaves) {
        brainWaveRow = new HashMap<>();
        for (BrainWave brainWave : brainWaves) {
            brainWaveRow.put(brainWave.toString(),  brainWave.getValue());
            //Log.d(LOG_TAG, String.valueOf(brainWaveRow)+"測試加入隊列");
            //Log.d(LOG_TAG, String.format("%s: %d", brainWave.toString(), brainWave.getValue())+"原始全部");
            //brainWaveRowData.putAll(brainWaveRow);
            Log.d(LOG_TAG, String.valueOf(brainWaveRow+"測試是否成功全部加入"));
        }

        bridge = brainWaveRow;


    }




    public void handleSignalChange(final Signal signal) {

        switch (signal) {
            case ATTENTION:
                //tvAttention.setText(getFormattedMessage("attention: %d", signal));
                Log.d(TAG, "handleSignalChange: " + getFormattedMessage("attention: %d", signal));
                attention = signal.getValue();
                //Log.d(LOG_TAG, "attention: %d" + signal);
                break;
            case MEDITATION:
                //tvMeditation.setText(getFormattedMessage("meditation: %d", signal));
                Log.d(TAG, "handleSignalChange: " + getFormattedMessage("attention: %d", signal));
                mediatation = signal.getValue();
                //Log.d(LOG_TAG, "meditation: %d" + signal);
                break;
            case POOR_SIGNAL:
                Log.d(TAG, "handleSignalChange: " + getFormattedMessage("blink: %d", signal));
                //tvBlink.setText(getFormattedMessage("blink: %d", signal));
                poorSignal = signal.getValue();
                break;
        }



        @SuppressLint("SimpleDateFormat")
        Date date_bwd = new Date();
        brainWaveItem = new HashMap<>();
        //brainWaveData = new HashMap<>();

        brainWaveItem.put("ATTENTION", attention);
        brainWaveItem.put("MEDITATION", mediatation);
        brainWaveItem.put("ROW",bridge);
        //brainWaveData.put(date_bwd.toString(),brainWaveItem);




        //brainWaveData.put(date.toString(),brainWaveItem);

        Log.d(LOG_TAG, "中"+String.valueOf(brainWaveItem));


        //  為什麼要把db update寫在這裡呢?
        //  因為要資料要一筆一筆輸入,所以要寫在 handleSignalChange裡:))


        if (record){

            int num = 120;
            Integer value = 0;

            if (eegData.size() < num ){
                // 抓raw data
                if(videoIndex == -1){
                    if (tempA != attention && tempM != mediatation && poorSignal == 0) {
                        tempA = attention;
                        tempM = mediatation;


                        Log.d(TAG2, "Attention: " + tempA + " ,    Meditation: " + tempM);


                        EEGdata data = new EEGdata();
                        data.setATTENTION(tempA);
                        data.setMEDITATION(tempM);
                        //data.setHIGH_ALPHA((Integer)bridge.get("HIGH_ALPHA"));

                        eegData.add(data);
                        Log.d(TAG2, "加入腦波值: " + attention + "," + mediatation + "  目前size: " + eegData.size());

                        switch(situation) {
                            case "Work":

                                if(bridge.get("HIGH_ALPHA") == null){
                                    Log.d(TAG, "初值為null");
                                }
                                else{
                                    value = bridge.get("HIGH_ALPHA");
                                    Log.d(TAG2, "比較HIGH_ALPHA = " + bridge.get("HIGH_ALPHA") + " LOW_ALPHA = " + bridge.get("LOW_ALPHA"));
                                    if(value >= 15000000 && eegData.size()>=20){
                                        count_raw += 1;

                                        Log.d(TAG2, "目前Count Row: " + count_raw);
                                    }
                                }
                                break;
                            case "Relax":
                                //value = bridge.get("LOW_ALPHA");
                                if(bridge.get("LOW_ALPHA") == null){
                                    Log.d(TAG, "初值為null");
                                }
                                else{
                                    value = bridge.get("LOW_ALPHA");
                                    Log.d(TAG2, "LOW_ALPHA = " + bridge.get("LOW_ALPHA"));
                                    if(value >= 15000000 && eegData.size()>=20){
                                        count_raw += 1;
                                        Log.d(TAG2, "目前Count Row: " + count_raw);
                                    }
                                }

                                break;
                            case "Depress":
                                //value = bridge.get("HIGH_GAMMA");
                                if(bridge.get("MID_GAMMA") == null){
                                    Log.d(TAG, "初值為null");
                                }
                                else{
                                    value = bridge.get("MID_GAMMA");
                                    Log.d(TAG2, "MID_GAMMA = " + bridge.get("MID_GAMMA"));

                                    if(value >= 15000000 && eegData.size()>=20){
                                        count_raw += 1;
                                        Log.d(TAG2, "目前Count Row: " + count_raw);
                                    }
                                }

                                break;
                            case "Sleep":
                                //value = bridge.get("HIGH_GAMMA");
                                if(bridge.get("DELTA") == null){
                                    Log.d(TAG, "初值為null");
                                }
                                else{
                                    value = bridge.get("DELTA");
                                    Log.d(TAG2, "DELTA = " + bridge.get("DELTA"));

                                    if(value >= 15000000 && eegData.size()>=20){
                                        count_raw += 1;
                                        Log.d(TAG2, "目前Count Row: " + count_raw);
                                    }
                                }

                                break;
                        }
                    }

                }
                // 當抓完 raw data 開始記錄每首歌腦波值
                else{
                    // 紀錄 A M 值 以及計算W , 過濾掉訊號不良
                    if (tempA != attention && tempM != mediatation && poorSignal == 0) {

                        tempA = attention;
                        tempM = mediatation;


                        Log.d(TAG2, "Attention: " + tempA + " ,    Meditation: " + tempM);


                        EEGdata data = new EEGdata();
                        data.setATTENTION(tempA);
                        data.setMEDITATION(tempM);
                        //data.setHIGH_ALPHA((Integer)bridge.get("HIGH_ALPHA"));

                        eegData.add(data);
                        Log.d(TAG2, "加入腦波值記錄每首歌腦波: " + attention + "," + mediatation + "  目前size: " + eegData.size());

                        if (eegData.size() > 1) {
                            w = Weight(w, eegData.get(eegData.size() - 2), eegData.get(eegData.size() - 1));
                            Log.d(TAG2, "迴圈裡的W =  " + w);
                        }
                        switch(situation) {
                            case "Work":
                                if(bridge.get("HIGH_ALPHA") == null){
                                    Log.d(TAG, "初值為null");
                                }
                                else{
                                    value = bridge.get("HIGH_ALPHA");
                                    Log.d(TAG2, "HIGH_ALPHA = " + value);
                                    if(value >= 15000000 && eegData.size()>=20){
                                        count_playing += 1;
                                        Log.d(TAG2, "目前Count  Playing: " + count_playing);
                                    }
                                }
                                break;
                            case "Relax":
                                if(bridge.get("LOW_ALPHA") == null){
                                    Log.d(TAG, "初值為null");
                                }
                                else{
                                    value = bridge.get("LOW_ALPHA");
                                    Log.d(TAG3, "LOW_ALPHA = " + value);
                                    if(value >= 16000000 && eegData.size()>=20){
                                        count_playing += 1;
                                        Log.d(TAG2, "目前Count Playing: " + count_playing);
                                    }
                                }
                                break;
                            case "Depress":
                                if(bridge.get("MID_GAMMA") == null){
                                    Log.d(TAG, "初值為null");
                                }
                                else{
                                    value = bridge.get("MID_GAMMA");
                                    Log.d(TAG3, " MID_GAMMA = " + value);
                                    if(value >= 15000000 && eegData.size()>=20){
                                        count_playing += 1;
                                        Log.d(TAG2, "目前Count Playing: " + count_playing);
                                    }
                                }
                                break;
                            case "Sleep":
                                //value = bridge.get("HIGH_GAMMA");
                                if(bridge.get("DELTA") == null){
                                    Log.d(TAG, "初值為null");
                                }
                                else{
                                    value = bridge.get("DELTA");
                                    Log.d(TAG2, "DELTA = " + bridge.get("DELTA"));

                                    if(value >= 15000000 && eegData.size()>=20){
                                        count_playing += 1;
                                        Log.d(TAG2, "目前Count Playing: " + count_playing);
                                    }
                                }

                                break;
                        }
                    }

                }


            }


            // 紀錄結束  計算在這裡喔喔喔喔喔喔

            if(eegData.size() == num){
                stopMonitoring();
                if(videoIndex != -1){
                    Log.d(TAG2, "計算結束 : W =  " + w + "Count playing = " + count_playing);
                    Log.d(TAG2, "handleSignalChange: 東西在下面~~~~哈哈哈哈哈");
                    for (EEGdata d:eegData){
                        Log.d(TAG2, "AM: ("+d.getATTENTION() + ", " + d.getMEDITATION() + ")");

                    }
                    Log.d(TAG2, "目前音樂: "+videoIndex);
                    Log.d(TAG2, "最後Count Playing = " + count_playing + "最後Count Row = " + count_raw);
                    if(w > 0 && count_playing > count_raw){
                        newList.add(videoIndex);
                        Toast.makeText(YoutubePlayerActivity.this, "有效歌曲,已加入新歌單\nThis song works, add to your play list", Toast.LENGTH_LONG).show();
                        Log.d(TAG2, "影片 " + videoIndex + " 有用喔喔喔, 已加入新歌單");
                    }else{
                        Toast.makeText(YoutubePlayerActivity.this, "無效歌曲\nThis song doesn't work.", Toast.LENGTH_LONG).show();
                        Log.d(TAG2, "影片 " + videoIndex + " 沒用呢");
                    }
                }else {
                    Log.d(TAG2, "最後Count Row = " + count_raw);
                    playAlertdialog.show();
                }




            }
            if(videoIndex != -1){
                db = FirebaseFirestore.getInstance();
                db.collection("brainWaveData").document(situation).collection(videoIdArrayList.get(videoIndex)).document(date_bwd.toString())
                        .set(brainWaveItem)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(LOG_TAG, "文件已新增DocumentSnapshot added with ID: " + situation + "    in集合 : " + videoIdArrayList.get(videoIndex));

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(LOG_TAG, "文件新增失敗Error adding document", e);
                            }
                        });
            }

/*

            //db = FirebaseFirestore.getInstance();
            if(videoIndex == -1){
                db = FirebaseFirestore.getInstance();
                Log.d(TAG, "handleSignalChange: 資料庫上傳raw");
                db.collection("users").document(userId).collection(situation+date_doc).document("rawData")
                        .update(brainWaveData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(LOG_TAG, "文件已新增DocumentSnapshot added with ID: rawData");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(LOG_TAG, "文件新增失敗Error adding document", e);
                            }
                        });
            }else{
                db = FirebaseFirestore.getInstance();
                Log.d(TAG, "handleSignalChange: videoIndex = " + videoIndex + ", videoId" + videoIdArrayList.get(videoIndex));
                db.collection("users").document(userId).collection(situation+date_doc).document(videoIdArrayList.get(videoIndex))
                        .update(brainWaveData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(LOG_TAG, "文件已新增DocumentSnapshot added with ID: " + videoIdArrayList.get(videoIndex));
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(LOG_TAG, "文件新增失敗Error adding document", e);
                            }
                        });
            }*/
        }

    }


    public String getFormattedMessage(String messageFormat, Signal signal) {
        return String.format(Locale.getDefault(), messageFormat, signal.getValue());
    }

    private double Weight(double w, EEGdata data1, EEGdata data2){
        int a1 = data1.getATTENTION();
        int m1 = data1.getMEDITATION();
        int a2 = data2.getATTENTION();
        int m2 = data2.getMEDITATION();

        Log.d(TAG2, "Weight: a1 = " + a1 );
        Log.d(TAG2, "Weight: m1 =  " + m1 );
        Log.d(TAG2, "Weight: a2  = " + a2 );
        Log.d(TAG2, "Weight: m2 = " + m2 );

        double distance = Math.sqrt(Math.pow((a1-a2),2) + Math.pow((m1-m2),2))/100;
        Log.d(TAG2, "distance = " + distance);

        if(a2 > a1 && m2 > m1){
            Log.d(TAG2, "Weight: 第一象限");
            w = w + (1.0 + distance);
        }
        else if(a2 < a1 && m2 > m1){
            Log.d(TAG2, "Weight: 第二象限");
            w = w - (0.5 + distance);
        }
        else if(a2 < a1 && m2 < m1){
            Log.d(TAG2, "Weight: 第三象限");
            w = w - (1.0 + distance);
        }
        else if(a2 > a1 && m2 < m1){
            Log.d(TAG2, "Weight: 第四象限");
            w = w + (0.5 + distance);
        }
        else{
            Log.d(TAG, "Weight: 什麼都沒有哈哈");
        }
        Log.d(TAG2, "Weight:  W = " + w);
        return w;

    }
    private void startMonitoring(){
        neuroSky.start();
        Map<String, Object> d = new HashMap<>();
        d.put("situation", situation);
        d.put("playlistId", playlistId);

        db = FirebaseFirestore.getInstance();
        /*

        db.collection("users").document(userId).collection(situation+date_doc).document("description")
                .set(d)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(LOG_TAG, "文件已新增DocumentSnapshot added with ID: description");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(LOG_TAG, "文件新增失敗Error adding document", e);
                    }
                });

        if(videoIndex == -1){
            db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId).collection(situation+date_doc).document("rawData")
                    .set(brainWaveData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(LOG_TAG, "文件已新增DocumentSnapshot added with ID: rawData");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(LOG_TAG, "文件新增失敗Error adding document", e);
                        }
                    });
        }else{
            db = FirebaseFirestore.getInstance();
            Log.d(TAG, "handleSignalChange: videoIndex = " + videoIndex + ", videoId" + videoIdArrayList.get(videoIndex));
            db.collection("users").document(userId).collection(situation+date_doc).document(videoIdArrayList.get(videoIndex))
                    .set(brainWaveData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(LOG_TAG, "文件已新增DocumentSnapshot added with ID: " + videoIdArrayList.get(videoIndex));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(LOG_TAG, "文件新增失敗Error adding document", e);
                        }
                    });
        }*/
        record = true; // 開始記錄腦波資料

        eegData = new ArrayList<>();
    }

    private void stopMonitoring(){
    record = false;// 終止紀錄
    }

    // Youtube Play
    private void PlayMusic(){
        Log.d(TAG, "onClick: Initializing youtube player.");
        youTubePlayerView.initialize(YoutubeConfig.getApiKey(), mOnInitializedListener);
        Log.d(TAG, "onClick: Done initializing.");
    }

    //MPandroidchart
    private void MPandroidchart(){
        //LineChart mChart1 = (LineChart) findViewById(R.id.dynamic_chart1);
        LineChart mChart2 = (LineChart) findViewById(R.id.dynamic_chart2);
        //兩條線的名子
        names.add("專注 Attention");
        names.add("冥想 Meditation");
        //names.add("其他");
        //線的顏色
        colour.add(Color.CYAN);
        colour.add(Color.GREEN);
        colour.add(Color.BLUE);

        // dynamicLineChartManager1 = new DynamicLineChartManager(mChart1, names.get(0), colour.get(0));
        dynamicLineChartManager2 = new DynamicLineChartManager(mChart2, names, colour);

        //dynamicLineChartManager1.setYAxis(100, 0, 10);
        dynamicLineChartManager2.setYAxis(100, 0, 10);

        //死循環添加數據
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listgraphdata.add(attention);
                            listgraphdata.add(mediatation);
                            //listgraphdata.add((int) (Math.random() * 100));
                            dynamicLineChartManager2.addEntry(listgraphdata);
                            listgraphdata.clear();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        neuroSky.stop();
        neuroSky.disconnect();
    }
}
