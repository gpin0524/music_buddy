package com.github.pwittchen.neurosky.app;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.github.pwittchen.neurosky.library.NeuroSky;
import com.github.pwittchen.neurosky.library.exception.BluetoothNotEnabledException;
import com.github.pwittchen.neurosky.library.listener.ExtendedDeviceMessageListener;
import com.github.pwittchen.neurosky.library.message.enums.BrainWave;
import com.github.pwittchen.neurosky.library.message.enums.Signal;
import com.github.pwittchen.neurosky.library.message.enums.State;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;

public class BWData {

    private final static String LOG_TAG = "NeuroSky";

    private NeuroSky neuroSky;
    private FileOutputStream fileOutputStream;//以後可json建立檔案用
    private FileInputStream fileInputStream; //以後可json建立檔案用
    private FirebaseFirestore db;
    private Map<String, Object> brainWaveItem;
    private Map<String, Object> brainWaveData;
    private Map<String, Object> brainWaveRow;
    private Map<String, Object> brainWaveRowData;
    private Map<String, Object> bridge;//從handleBrainWavesChange傳到外面



    private Boolean record = false;
    private String MusicName = "Music Name";
    public  String documentID ; // documentID :宣告全域

    public BWData(Activity activity){
        //ButterKnife.bind(this);
        neuroSky = createNeuroSky();
        connect(activity);
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
        }

        // tvState.setText(state.toString());
        Log.d(LOG_TAG, state.toString());
    }

    Integer attention = -1;
    Integer mediatation = -1;

    public void handleBrainWavesChange(final Set<BrainWave> brainWaves) {
        brainWaveRowData = new HashMap<>();
        brainWaveRow = new HashMap<>();
        for (BrainWave brainWave : brainWaves) {
            brainWaveRow.put(brainWave.toString(),  brainWave.getValue());
            //Log.d(LOG_TAG, String.valueOf(brainWaveRow)+"測試加入隊列");
            //Log.d(LOG_TAG, String.format("%s: %d", brainWave.toString(), brainWave.getValue())+"原始全部");
            brainWaveRowData.putAll(brainWaveRow);
            Log.d(LOG_TAG, String.valueOf(brainWaveRowData+"測試是否成功全部加入"));
        }

        bridge = brainWaveRowData;
        //Log.d(LOG_TAG,String.valueOf(brainWaveData));
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
            case BLINK:
                Log.d(TAG, "handleSignalChange: " + getFormattedMessage("blink: %d", signal));
                //tvBlink.setText(getFormattedMessage("blink: %d", signal));
                break;
        }
    /*
    Date date = new Date();
    Log.d(LOG_TAG, String.format("%s: %d  %s", signal.toString(), signal.getValue(), date.toString()));
    array.add(signal.toString() + signal.getValue());
    //Log.d(LOG_TAG, array.toString());
    */
        Date date = new Date();
        brainWaveData = new HashMap<>();
        brainWaveItem = new HashMap<>();



        brainWaveItem.put("專注: ", attention);
        brainWaveItem.put("冥想: ", mediatation);
        brainWaveItem.put("腦波",bridge);
        brainWaveData.put(date.toString(),brainWaveItem);
        // Log.d(LOG_TAG, "下"+String.valueOf(brainWaveData));


        //Log.d(LOG_TAG, "上"+String.valueOf(brainWaveData));
        Log.d(LOG_TAG, "中"+String.valueOf(brainWaveData));
        // brainWaveData2.put(date.toString(),bridge);
        //brainWaveItem.putAll(bridge);


        //  為什麼要把db update寫在這裡呢?
        //  因為要資料要一筆一筆輸入,所以要寫在 handleSignalChange裡:))
        if (record){
            db = FirebaseFirestore.getInstance();

            db.collection("brainWaveData").document(documentID)
                    .update(brainWaveData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(LOG_TAG, "文件已新增DocumentSnapshot added with ID: " + documentID);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(LOG_TAG, "文件新增失敗Error adding document", e);
                        }
                    });

        }

    }


    public String getFormattedMessage(String messageFormat, Signal signal) {
        return String.format(Locale.getDefault(), messageFormat, signal.getValue());
    }

    public void connect(Activity activity) {
        try {
            neuroSky.connect();
        } catch (BluetoothNotEnabledException e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, e.getMessage());
        }
    }
    public void disconnect() {
        neuroSky.disconnect();
    }
    public void startMonitoring() {
        neuroSky.start();
        documentID = "test"; //  documentID : 接全域
        Map<String, Object> music = new HashMap<>();
        music.put("name", MusicName);
        db = FirebaseFirestore.getInstance();
        db.collection("brainWaveData")
                .add(music)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(LOG_TAG, "DocumentSnapshot written with ID: " + documentReference.getId());

                        documentID = documentReference.getId(); //  documentID : 接function  = 全域


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(LOG_TAG, "Error adding document", e);
                    }
                });
        record = true; // 開始記錄腦波資料
        Log.d(LOG_TAG, "開始記錄腦波record stat : " + record.toString());
    }
    public void stopMonitoring () {
        record = false;// 終止紀錄
        Log.d(LOG_TAG, "停止記錄腦波record stat : " + record.toString());
    }
    public void stopStreaming (){
        neuroSky.stop();
    }

}
