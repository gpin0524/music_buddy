package com.github.pwittchen.neurosky.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.pwittchen.neurosky.app.Model.MusicDetails;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddMusicTagActivity extends AppCompatActivity {

    private static final String TAG = "AddMusicTagActivity";
    private FirebaseFirestore db;

    private ArrayList<String> newVedioIdArrayList; // 要新增的影片 id 們
    private String newPlaylistId; // 要新增影片到這個 list
    private ArrayList<Integer> mSelectedItems;
    private ArrayList<MusicDetails> dbNullList;
    private AlertDialog.Builder mAlertdialog;
    private AlertDialog.Builder addFinAlertdialog;
    private AlertDialog.Builder cancelAlertdialog;
    private AlertDialog.Builder tagAddingDialog;
    private String[] tagItems;
    private boolean[] checkedItems;
    private int count;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_music_tag);

        tagItems = getResources().getStringArray(R.array.tag_item);
        checkedItems = new boolean[tagItems.length];
        mSelectedItems = new ArrayList();
        count = 0;

        // 取值
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        dbNullList = (ArrayList<MusicDetails>) args.getSerializable("dbNullList");
        newVedioIdArrayList = intent.getStringArrayListExtra("newVedioIdArrayList");
        newPlaylistId = intent.getStringExtra("newPlaylistId");
        Log.d(TAG, "onCreate: newVedioIdArrayList = " + newVedioIdArrayList);
        Log.d(TAG, "onCreate: newPlaylistId = " + newPlaylistId);

        for (int i = 0; i < dbNullList.size(); i++ ){
            Log.d(TAG, "onCreate: dbNullList    videoId = " + dbNullList.get(i).getVideoId());
        }

        // 設定AlertDialog
        mAlertdialog = new AlertDialog.Builder(this);
        mAlertdialog.setMessage("是否為歌曲添加呢??\n( 如歌曲無標籤, 則無法提供推薦功能!)")
                .setTitle("該歌曲分類標籤不存在");
        mAlertdialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 呼叫添加funtion
                TagAdding();
            }
        });
        mAlertdialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(AddMusicTagActivity.this, SituationSelect.class);
                startActivity(i);
            }
        });
        mAlertdialog.show();

        // Add finish
        addFinAlertdialog = new AlertDialog.Builder(this);
        addFinAlertdialog.setTitle("標籤添加完成");
        addFinAlertdialog.setCancelable(false);
        addFinAlertdialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                uploadToDb();
                Intent i = new Intent(AddMusicTagActivity.this, YoutubeRecActivity.class);
                i.putStringArrayListExtra("newVedioIdArrayList",newVedioIdArrayList); // 傳送篩選後的videoId
                i.putExtra("newPlaylistId", newPlaylistId);
                startActivity(i);
            }
        });

        // 放棄添加的貼心小提醒:)))
        cancelAlertdialog = new AlertDialog.Builder(this);
        cancelAlertdialog.setTitle("確定要放棄添加標籤嗎??")
                .setMessage("放棄添加則無法使用推薦, 並將回到主頁面");
        cancelAlertdialog.setCancelable(false);
        cancelAlertdialog.setPositiveButton("確認放棄添加", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(AddMusicTagActivity.this, SituationSelect.class);
                startActivity(i);
            }
        });
        cancelAlertdialog.setNegativeButton("繼續添加", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        if (count == dbNullList.size()){
            addFinAlertdialog.show();
        }


    }

    private void TagAdding(){
        for (int i = 0; i < dbNullList.size(); i++){
            mSelectedItems = new ArrayList<>();
            tagAddingDialog = new AlertDialog.Builder(this);
            MusicDetails musicDetails = dbNullList.get(i);
            tagAddingDialog.setTitle("為" + musicDetails.getMusicTitle() + "添加分類標籤");
            tagAddingDialog.setMultiChoiceItems(tagItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                    if (isChecked) {
                        mSelectedItems.add(which);
                    } else if (mSelectedItems.contains(which)) {
                        mSelectedItems.remove(Integer.valueOf(which));
                    }
                    Log.d(TAG, "onClick: item = " + mSelectedItems);
                }
            });
            tagAddingDialog.setCancelable(false);
            // 確認
            tagAddingDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    String tag = "";
                    for (int i = 0; i <= 16; i++){
                        if (mSelectedItems.contains(i)){
                            tag += 1;
                        }else{
                            tag += 0;
                        }
                    }
                    musicDetails.setTag(tag);
                    Log.d(TAG, "onClick: tag = " + tag);
                    count += 1;
                    if (count == dbNullList.size()){
                        addFinAlertdialog.show();
                    }
                }
            });

            //離開
            tagAddingDialog.setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            tagAddingDialog.show();
            Log.d(TAG, "TagAdding: count = " + count);
        }

    }

    private void uploadToDb(){
        Log.d(TAG, "uploadToDb");
        for (int i = 0; i < dbNullList.size(); i++){
            Map<String, Object> music = new HashMap<>();
            music.put("musicTitle", dbNullList.get(i).getMusicTitle());
            music.put("tag", dbNullList.get(i).getTag());
            music.put("videoId", dbNullList.get(i).getVideoId());

            db = FirebaseFirestore.getInstance();
            db.collection("music")
                    .add(music)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                        }
                    });
        }
    }
}
