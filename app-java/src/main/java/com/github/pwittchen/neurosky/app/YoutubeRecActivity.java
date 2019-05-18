package com.github.pwittchen.neurosky.app;

import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.github.pwittchen.neurosky.app.Adapter.MyVideoAdapter;
import com.github.pwittchen.neurosky.app.Model.MusicDetails;
import com.github.pwittchen.neurosky.app.Model.VideoDetails;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.common.collect.Lists;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.DocumentViewChange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.annotations.NonNull;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.content.ContentValues.TAG;


public class YoutubeRecActivity<cmpCntSet, cntMap> extends YouTubeBaseActivity implements EasyPermissions.PermissionCallbacks {

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {YouTubeScopes.YOUTUBE};

    private static final String TAG = "YoutubeRecActivity";

    private YouTube mService = null;
    // View
    Button btnNew;
    Button btnCancel;
    //TextView mOutputText;
    ProgressDialog mProgress;
    GoogleAccountCredential mCredential;
    // Adapter
    ListView listview;
    ArrayList<VideoDetails> videoDetailsArrayList;
    MyVideoAdapter myVideoAdapter;
    MusicDetails NewMusicTag;
    String compareMusicTag;
    String situation;
    //String originPlaylistId; // 舊的playtListId , 使用者原本的歌單(用來篩選的)
    String userId;
    ArrayList<Integer> newList; // 篩選後的歌曲Index (從Playrt傳值過來)
    ArrayList<String> recVedioIdArrayList;
    ArrayList<Integer> maxtagindex;
    //int [] maxtagindex;
    String Tag;
    AlertDialog.Builder mAlertdialog;
    private FirebaseFirestore db;
    private String newPlaylistId; // 要新增影片到這個 list
    //private String situationListId; // db 抓下來的狀態list id
    private ArrayList<String> newVedioIdArrayList; // 要新增的影片 id 們
    private ArrayList<MusicDetails> musicDetailsArrayList;
    int newmusictag = 0;

    int callbacktime = 0;
    int[] sumofTag = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] finaltag = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_rec);


        // 初始化
        db = FirebaseFirestore.getInstance();
        newVedioIdArrayList = new ArrayList<>();
        recVedioIdArrayList = new ArrayList<>();
        musicDetailsArrayList = new ArrayList<>();

        // 取從 Insert 來的資料
        Intent i_formInsert = getIntent();
        newVedioIdArrayList = i_formInsert.getStringArrayListExtra("newVedioIdArrayList");
        newPlaylistId = i_formInsert.getStringExtra("newPlaylistId");
        Log.d(TAG, "onCreate: newVedioIdArrayList = " + newVedioIdArrayList);
        Log.d(TAG, "onCreate: newPlaylistId = " + newPlaylistId);

        // View
        //mOutputText = (TextView) findViewById(R.id.mOutputText);
        //mOutputText.setMovementMethod(new ScrollingMovementMethod());
        btnCancel = findViewById(R.id.btn_cancel_rec);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(YoutubeRecActivity.this, SituationSelect.class);
                startActivity(i);
            }
        });

        btnNew = (Button) findViewById(R.id.btn_new_rec);
        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playlistUpdatesUsingApi();
            }
        });


        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling YouTube Data API ...");

        // Initialize credentials and service object.

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());


        //list資料裝載
        listview = (ListView) findViewById(R.id.rec_listview);
        videoDetailsArrayList = new ArrayList<>();
        myVideoAdapter = new MyVideoAdapter(YoutubeRecActivity.this, videoDetailsArrayList);


        // 設定AlertDialog
        mAlertdialog = new AlertDialog.Builder(this);
        mAlertdialog.setMessage("已上傳清單至帳戶!!")
                .setTitle("上傳成功");

        mAlertdialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing
            }
        });

        getSumOfTag(callbacktime, newVedioIdArrayList, new SumOfTagCallback() {
            @Override
            public void onCallback(int[] SumOfTag) {
                Log.d(TAG, "onCallback: getMusicTag Out = " + SumOfTag);
                for (int i = 0; i < 16; i++) {
                    Log.d(TAG, "onCallback: getMusicTag Outttttt = " + SumOfTag[i]);
                }

                int maxtag = 0;//記錄目前最大 的值用於比較，但不知爲何放囘圈外面就會不對，但放裏面現在歌單的三首歌可以抛出正確結果，但實際邏輯仍有問題

                for (int j = 0; j < 16; j++) {
                    Log.d(TAG, "sumofTag 000   " + sumofTag[j]);
                    if (sumofTag[j] > maxtag) {

                        maxtag = sumofTag[j];
                    }
                }
                for (int k = 0; k < 16; k++) {
                    if (sumofTag[k] == maxtag) {
                        Log.d(TAG, "onCallback: k = " + k);
                        Log.d(TAG, "onCallback: sumofTag[k] = " + sumofTag[k]);
                        maxtagindex.add(k);
                    }
                }


                Log.d(TAG, "onCallback: maxtagindex444 = " + maxtagindex);
                for (int i = 0; i < finaltag.length; i++) {
                    for (int j = 0; j < maxtagindex.size(); j++) {
                        if (maxtagindex.get(j) == i) {
                            finaltag[i] = 1;

                        }
                    }

                    Log.d(TAG, "onCreate: finaltag" + finaltag[i]);
                }


                StringBuilder finaltagStringBuilder = new StringBuilder();
                for (int i = 0; i < finaltag.length; i++) {
                    finaltagStringBuilder.append(finaltag[i]);
                }
                String content = finaltagStringBuilder.toString();

                Log.d(TAG, "onCallback: content" + content);


                // 從DB取Music data (call back)
                getMusicDetails(new musicDetailsCallback() {
                    @Override
                    public void onCallback(ArrayList<MusicDetails> musicDetailsArrayList) {
                        for (int i = 0; i < musicDetailsArrayList.size(); i++) {
                            String compareContent = musicDetailsArrayList.get(i).getTag();
                            double similarity = CalculateTextSim(content, compareContent);
                            musicDetailsArrayList.get(i).setSimilarity(similarity);

                            Log.d(TAG, "onCallback: 與"+ musicDetailsArrayList.get(i).getVideoId() + "的相似度：" + similarity);
                            // Log.d(TAG, "onCallback: musicDetailsArrayList：" + musicDetailsArrayList.get(i).getSimilarity());
                        }
                        Collections.sort(musicDetailsArrayList);

                        // 判斷是否重複推薦
                        int count = 0;
                        while (recVedioIdArrayList.size() < 5){
                            if (!newVedioIdArrayList.contains(musicDetailsArrayList.get(count).getVideoId())){
                                recVedioIdArrayList.add(musicDetailsArrayList.get(count).getVideoId());
                            }
                            count += 1;
                        }

                        /*for (int i = musicDetailsArrayList.size()-1; i >= musicDetailsArrayList.size()-5 ; i--) {
                            recVedioIdArrayList.add(musicDetailsArrayList.get(i).getVideoId());
                        }*/
                        Log.d(TAG, "onCallback: recVedioIdArrayList = " + recVedioIdArrayList);
                        // 取得 Api 資料
                        getResultsFromApi();
                    }
                });

            }
        });
    }



    public interface SumOfTagCallback {
        void onCallback(int[] SumOfTag);
    }

    private void getSumOfTag(int callbacktime, final ArrayList<String> newVedioIdArrayList, SumOfTagCallback callback) {


        getMusicTag(newVedioIdArrayList, new musictagCallback() {
            @Override

            public void onCallback(MusicDetails NewMusicTag) {
                String tempNewMusicTag = "";
                Log.d(TAG, "onCallback: newmusictag = " + newmusictag);
                //newmusictag = NewMusicTag;
                Log.d(TAG, "onCallback: getMusicTag  = " + NewMusicTag.getTag());
                String[] tags = NewMusicTag.getTag().split("");
                maxtagindex = new ArrayList<>();
                for (int i = 1; i < tags.length - 1; i++) {
                    Log.d(TAG, "迴圈次數 : " + i);
                    int tag = Integer.valueOf(tags[i]); //分割後轉好int 型態的tag
                    sumofTag[i - 1] += tag;
                    Log.d(TAG, "sumofTag " + sumofTag[i - 1]);

                }
                if (tempNewMusicTag != NewMusicTag.getTag()) {
                    newmusictag++;
                    Log.d(TAG, "onCallback: newmusictag000 = " + newmusictag);
                    if (newmusictag == newVedioIdArrayList.size()) {
                        callback.onCallback(sumofTag);
                        Log.d(TAG, "onCallback: callback ");
                    }
                }
            }
        });
    }

    // 待改 : 之後拿來取Music資料

    public interface musictagCallback {
        void onCallback(MusicDetails NewMusicTag);
    }

    //, situatuinListIdCallback callback
    private void getMusicTag(final ArrayList<String> newVedioIdArrayList, musictagCallback callback) {


        for (String videoId : newVedioIdArrayList) {
            db.collection("music")
                    .whereEqualTo("videoId", videoId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Log.d(TAG, "onComplete : VideoId " + videoId);
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());

                                    NewMusicTag = document.toObject(MusicDetails.class);

                                }
                                callback.onCallback(NewMusicTag);
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }

                        }

                    });
        }
/*
        db.collection("music")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                MusicDetails md = document.toObject(MusicDetails.class);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
*/

    }

    // 從DB取所有music data
    public interface musicDetailsCallback {
        void onCallback(ArrayList<MusicDetails> musicDetailsArrayList);
    }

    private void getMusicDetails(musicDetailsCallback callback) {

        db.collection("music")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                MusicDetails md = document.toObject(MusicDetails.class);
                                musicDetailsArrayList.add(md);
                            }
                            callback.onCallback(musicDetailsArrayList);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


    }


    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            //mOutputText.setText("No network connection available.");
        } else {
            new MakeDisplayRequestTask(mCredential).execute();
        }
    }

    private void playlistUpdatesUsingApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            //mOutputText.setText("No network connection available.");
        } else {
            new MakeUpldateRequestTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
                Log.d(TAG, "chooseAccount: accountName = " + accountName);
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
            Log.d(TAG, "chooseAccount: else55555");
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    //mOutputText.setText(
                    //       "This app requires Google Play Services. Please install " +
                    //                "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                YoutubeRecActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the YouTube Data API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */

    //內存會誤報，因為Android Studio沒有智能可以判斷出這是Application Context所以加上註解
    //@SuppressLint("StaticFieldLeak")
    private class MakeDisplayRequestTask extends AsyncTask<Void, Void, ArrayList<VideoDetails>> {

        private Exception mLastError = null;

        MakeDisplayRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("YouTube Data API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call YouTube Data API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected ArrayList<VideoDetails> doInBackground(Void... params) {
            try {
                return dispalyList();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch information about the "GoogleDevelopers" YouTube channel.
         *
         * @return List of Strings containing information about the channel.
         * @throws IOException
         */

        private ArrayList<VideoDetails> dispalyList() throws IOException {
            YouTube.Videos.List videoRequest = mService.videos()
                    .list("snippet,contentDetails,statistics");
            for (int i = 0; i < recVedioIdArrayList.size(); i++) {
                VideoListResponse videoResult = videoRequest.setId(recVedioIdArrayList.get(i)).execute();
                List<Video> videos = videoResult.getItems();
                if (videos != null) {
                    // ListAdapter data
                    //String video_id = videos.get(i).getContentDetails().getVideoId();
                    String video_thumbnails = videos.get(0).getSnippet().getThumbnails().getMedium().getUrl();
                    VideoDetails vd = new VideoDetails();
                    //vd.setVideoId(video_id);
                    vd.setTitle(videos.get(0).getSnippet().getTitle());
                    vd.setDescription(videos.get(0).getSnippet().getDescription());
                    vd.setUrl(video_thumbnails);
                    //vd.setIndex(i);
                    //vd.setListId(originPlaylistId);
                    videoDetailsArrayList.add(vd);
                }
            }
            return videoDetailsArrayList;
        }

        @Override
        protected void onPreExecute() {
            //mOutputText.setText("onPreExecute");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(ArrayList<VideoDetails> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                //mOutputText.setText("No results returned.");
            } else {
                //output.add(0, "Data retrieved using the YouTube Data API:");
                //mOutputText.setText(TextUtils.join("\n", output));
                Log.d(TAG, "onPostExecute: " + videoDetailsArrayList);
                listview.setAdapter(myVideoAdapter);
                myVideoAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            YoutubeVideoListActivity.REQUEST_AUTHORIZATION);
                } else {
                    //mOutputText.setText("The following error occurred:\n"
                    //        + mLastError.getMessage());
                }
            } else {
                //mOutputText.setText("Request cancelled.");
            }
        }
    }


    // Youtube 新增
    private String insertPlaylistItem(YouTube mService, String playlistId, String videoId) throws IOException {

        // Define a resourceId that identifies the video being added to the
        // playlist.
        ResourceId resourceId = new ResourceId();
        resourceId.setKind("youtube#video");
        resourceId.setVideoId(videoId);

        // Set fields included in the playlistItem resource's "snippet" part.
        PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
        playlistItemSnippet.setTitle("First video in the test playlist");
        playlistItemSnippet.setPlaylistId(playlistId);
        playlistItemSnippet.setResourceId(resourceId);

        // Create the playlistItem resource and set its snippet to the
        // object created above.
        PlaylistItem playlistItem = new PlaylistItem();
        playlistItem.setSnippet(playlistItemSnippet);

        // Call the API to add the playlist item to the specified playlist.
        // In the API call, the first argument identifies the resource parts
        // that the API response should contain, and the second argument is
        // the playlist item being inserted.
        YouTube.PlaylistItems.Insert playlistItemsInsertCommand =
                mService.playlistItems().insert("snippet,contentDetails", playlistItem);
        PlaylistItem returnedPlaylistItem = playlistItemsInsertCommand.execute();

        // Print data from the API response and return the new playlist
        // item's unique playlistItem ID.

        System.out.println("New PlaylistItem name: " + returnedPlaylistItem.getSnippet().getTitle());
        System.out.println(" - Video id: " + returnedPlaylistItem.getSnippet().getResourceId().getVideoId());
        System.out.println(" - Posted: " + returnedPlaylistItem.getSnippet().getPublishedAt());
        System.out.println(" - Channel: " + returnedPlaylistItem.getSnippet().getChannelId());
        return returnedPlaylistItem.getId();

    }

    private class MakeUpldateRequestTask extends AsyncTask<Void, Void, String> {

        private Exception mLastError = null;

        MakeUpldateRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("YouTube Data API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call YouTube Data API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected String doInBackground(Void... params) {
            try {
                return update();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch information about the "GoogleDevelopers" YouTube channel.
         *
         * @return List of Strings containing information about the channel.
         * @throws IOException
         */

        private String update() throws IOException {
            YouTube.Channels.List channelRequest = mService.channels()
                    .list("contentDetails")
                    .setMine(true)
                    .setFields("items(id,contentDetails),nextPageToken,pageInfo");
            //.setFields("items/contentDetails,nextPageToken,pageInfo");
            ChannelListResponse channelResult = channelRequest.execute();
            List<Channel> channelsList = channelResult.getItems();

            if (channelsList != null) {
                Log.d(TAG, "update: if (channelsList != null)");
                // The user's default channel is the first item in the list.
                // Extract the playlist ID for the channel's videos from the
                // API response.
                String uploadPlaylistId = newPlaylistId; //listID
                Log.d(TAG, "PlaylistUpdates: newPlaylistId = " + uploadPlaylistId);
                // Define a list to store items in the list of uploaded videos.
                List<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();


                // Retrieve the playlist of the channel's uploaded videos.
                YouTube.PlaylistItems.List playlistItemRequest =
                        mService.playlistItems().list("id,contentDetails,snippet");
                playlistItemRequest.setPlaylistId(uploadPlaylistId);

                // Only retrieve data used in this application, thereby making
                // the application more efficient. See:
                // https://developers.google.com/youtube/v3/getting-started#partial
                playlistItemRequest.setFields(
                        "items(contentDetails/videoId,snippet/title,snippet/publishedAt,snippet/thumbnails),nextPageToken,pageInfo");

                String nextToken = "";

                // Call the API one or more times to retrieve all items in the
                // list. As long as the API response returns a nextPageToken,
                // there are still more items to retrieve.
                do {
                    playlistItemRequest.setPageToken(nextToken);
                    PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();

                    playlistItemList.addAll(playlistItemResult.getItems());


                    nextToken = playlistItemResult.getNextPageToken();
                } while (nextToken != null);

                // 上傳歌曲
                Log.d(TAG, "PlaylistUpdates: newPlaylistId = " + uploadPlaylistId);
                try {
                    for (int i = 0; i < recVedioIdArrayList.size(); i++) {
                        insertPlaylistItem(mService, uploadPlaylistId, recVedioIdArrayList.get(i));
                        Log.d(TAG, "PlaylistUpdates:上傳歌曲  " + recVedioIdArrayList.get(i));
                    }
                } catch (GoogleJsonResponseException e) {
                    System.err.println("There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    System.err.println("IOException: " + e.getMessage());
                    e.printStackTrace();
                } catch (Throwable t) {
                    System.err.println("Throwable: " + t.getMessage());
                    t.printStackTrace();
                }
                return newPlaylistId;

            } else {
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            //mOutputText.setText("onPreExecute");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(String output) {
            mProgress.hide();
            if (output == null || output.length() == 0) {
                //mOutputText.setText("No results returned.");
            } else {

                //mOutputText.setText(TextUtils.join("\n", Collections.singleton(output)));
                Log.d(TAG, "onPostExecute: " + newPlaylistId);
                // 上傳成功提示
                mAlertdialog.show();
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            YoutubeVideoListActivity.REQUEST_AUTHORIZATION);
                } else {
                    //mOutputText.setText("The following error occurred:\n"
                    //        + mLastError.getMessage());
                }
            } else {
                //mOutputText.setText("Request cancelled.");
            }
        }
    }

    public boolean codeFilter(int code) {
        if ((code < 19968 || code > 40869) && !(code >= '0' && code <= '9') && !(code >= 'a' && code <= 'z')
                && !(code >= 'A' && code <= 'Z')) {
            return false;
        }
        return true;
    }

    public double CalculateTextSim(String content, String compareContent) {
        if (null == content || null == compareContent)
            return 0.0;
        Map<String, Integer> cntMap = new HashMap<String, Integer>();
        Set<String> cntSet = new HashSet<String>();
        Map<String, Integer> cmpCntMap = new HashMap<String, Integer>();
        Set<String> cmpCntSet = new HashSet<String>();

        Integer countM11 = 0;
        Integer countM10 = 0;
        Integer countM01 = 0;

        String[] compareContentlist = compareContent.split("");
        String[] contentlist = content.split("");
        for (int i = 0; i < contentlist.length; i++) {
            if (compareContentlist[i].equals("1") && contentlist[i].equals("1")) {
                countM11++;
            }
            if (compareContentlist[i].equals("1") && contentlist[i].equals("0")) {
                countM01++;
            }
            if (compareContentlist[i].equals("0") && contentlist[i].equals("1")) {
                countM10++;
            }
        }

        double sumofcount = countM01 + countM10 + countM11;

        double outcome = countM11 / sumofcount;
        //Log.d(TAG, "CalculateTextSim: "+ outcome);
        return outcome;

        /*
        for (int i = 0; i != content.length(); i++) {
            int k = 0;
            if (codeFilter(content.codePointAt(i))) {
                if (cntMap.containsKey("" + content.charAt(i))) {
                    Integer count = cntMap.get("" + content.charAt(i));
                    count = count + 1;
                    cntMap.put("" + content.charAt(i), count);
                    k = count;
                } else {
                    cntMap.put("" + content.charAt(i), new Integer(1));
                    k = 1;
                }
                String tmpString = content.charAt(i) + "" + k;
                cntSet.add(tmpString);
            }
        }

        for (int i = 0; i != compareContent.length(); i++) {
            int k = 0;
            if (codeFilter(compareContent.codePointAt(i))) {
                if (cmpCntMap.containsKey("" + compareContent.charAt(i))) {
                    Integer count = cmpCntMap.get("" + compareContent.charAt(i));
                    count = count + 1;
                    cmpCntMap.put("" + compareContent.charAt(i), count);
                    k = count;
                } else {
                    cmpCntMap.put("" + compareContent.charAt(i), new Integer(1));
                    k = 1;
                }

                String tmpString = compareContent.charAt(i) + "" + k;
                cmpCntSet.add(tmpString);
            }
        }

        Set<String> tmpSet = new HashSet<String>();
        tmpSet.addAll(cntSet);
        cntSet.retainAll(cmpCntSet);
        double intCount = cntSet.size();

        tmpSet.addAll(cmpCntSet);

        if (tmpSet.size() == 0) {
            return 0;
        }
        double uniCount = tmpSet.size();
*/

    }
}







