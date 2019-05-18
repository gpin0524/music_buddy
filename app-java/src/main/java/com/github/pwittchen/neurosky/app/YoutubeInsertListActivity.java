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
import android.os.Parcelable;
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
import com.google.common.collect.Lists;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reactivex.annotations.NonNull;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.content.ContentValues.TAG;


public class YoutubeInsertListActivity extends YouTubeBaseActivity implements EasyPermissions.PermissionCallbacks{

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { YouTubeScopes.YOUTUBE };

    private YouTube mService = null;

    private static final String TAG = "YoutubeInsertList";

    //Button btnJumpToRec;
    Button btnNew;
    Button btnCancel;
    //TextView mOutputText;
    ProgressDialog mProgress;
    GoogleAccountCredential mCredential;

    private ListView listview;
    private ArrayList<VideoDetails> videoDetailsArrayList;
    private MyVideoAdapter myVideoAdapter;

    private String situation;
    private String originPlaylistId; // 舊的playtListId , 使用者原本的歌單(用來篩選的)
    private String userId;
    private String selectedAccount; // 已選擇的帳號
    private ArrayList<Integer> newList; // 篩選後的歌曲Index (從Playrt傳值過來)
    private ArrayList<MusicDetails> dbNullList = new ArrayList<>(); // db沒有此歌曲資料(tag)
    private ArrayList<String> musicIdArrayList = new ArrayList<>(); // 從db抓下來所有歌曲的id

    private AlertDialog.Builder mAlertdialog;
    private FirebaseFirestore db;
    private String newPlaylistId; // 要新增影片到這個 list
    private String situationListId; // db 抓下來的狀態list id
    private ArrayList<String> newVedioIdArrayList; // 要新增的影片 id 們

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_insert_list);


        // 初始化
        db = FirebaseFirestore.getInstance();
        newVedioIdArrayList = new ArrayList<>();

        // 取從Player 來的資料
        Intent i_formPlayer = getIntent();
        selectedAccount = i_formPlayer.getStringExtra("selectedAccount");
        originPlaylistId = i_formPlayer.getStringExtra("listId");
        situation = i_formPlayer.getStringExtra("situation");
        newList = i_formPlayer.getIntegerArrayListExtra("newList");
        Log.d(TAG, "onCreate: 傳送過來的listId = " + originPlaylistId);
        Log.d(TAG, "onCreate: 傳送過來的situation = " + situation);
        Log.d(TAG, "onCreate傳送過來的新清單: " + newList);

        // View
        //mOutputText = (TextView) findViewById(R.id.mOutputText);
        //mOutputText.setMovementMethod(new ScrollingMovementMethod());
        btnNew = (Button) findViewById(R.id.btn_new);
        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSituatuinListId(situation, new situatuinListIdCallback() {
                    @Override
                    public void onCallback(String situatuinListId) {
                        //newPlaylistId = situatuinListId;
                        Log.d(TAG, "onCallback: situatuinListId  = " + situatuinListId);
                        playlistUpdatesUsingApi();
                    }
                });
            }
        });
        btnCancel = findViewById(R.id.btn_cancel_ins);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(YoutubeInsertListActivity.this, SituationSelect.class);
                startActivity(i);
            }
        });


        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling YouTube Data API ...");

        // Initialize credentials and service object.

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        mCredential.setSelectedAccountName(selectedAccount);


        // 先從DB取歌曲資料(call back),再取得 Api 資料
        getMusicIds(new musicIdCallback() {
            @Override
            public void onCallback(ArrayList<String> musicIdArrayList) {
                getResultsFromApi();
            }
        });


        //list資料裝載
        listview = (ListView) findViewById(R.id.insert_listview);
        videoDetailsArrayList = new ArrayList<>();
        myVideoAdapter = new MyVideoAdapter(YoutubeInsertListActivity.this,videoDetailsArrayList);

        Log.d(TAG, "getDataFromApi: videoDetailsArrayList = "+ videoDetailsArrayList);


        // 設定AlertDialog
        mAlertdialog = new AlertDialog.Builder(this);
        mAlertdialog.setMessage("已上傳清單至帳戶\nThe filtered songs have been uploaded")
                .setTitle("上傳成功\nUpload success");

        mAlertdialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dbNullList.size() == 0) {
                    Log.d(TAG, "onClick: dbNullList.size() == 0");
                    Intent i = new Intent(YoutubeInsertListActivity.this, YoutubeRecActivity.class);
                    i.putStringArrayListExtra("newVedioIdArrayList",newVedioIdArrayList); // 傳送篩選後的videoId
                    i.putExtra("newPlaylistId", newPlaylistId);
                    startActivity(i);
                }else{
                    Intent i = new Intent(YoutubeInsertListActivity.this , AddMusicTagActivity.class);
                    Bundle args = new Bundle();
                    args.putSerializable("dbNullList",(Serializable)dbNullList);
                    i.putExtra("BUNDLE",args);
                    i.putStringArrayListExtra("newVedioIdArrayList",newVedioIdArrayList); // 傳送篩選後的videoId
                    i.putExtra("newPlaylistId", newPlaylistId);
                    startActivity(i);
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
        Log.d(TAG, "getResultsFromApi: mCredential.getSelectedAccountName()  = " + mCredential.getSelectedAccountName());
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            //mOutputText.setText("No network connection available.");
        } else {
            new MakeDisplayRequestTask(mCredential).execute();
        }
    }
    private void playlistUpdatesUsingApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
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
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    //mOutputText.setText(
                    //        "This app requires Google Play Services. Please install " +
                    //                "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    Log.d(TAG, "onActivityResult: data = " + data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
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
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
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
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
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
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
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
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                YoutubeInsertListActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }
    /**
     * An asynchronous task that handles the YouTube Data API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */

    //內存會誤報，因為Android Studio沒有智能可以判斷出這是Application Context所以加上註解
    @SuppressLint("StaticFieldLeak")
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
         * @return List of Strings containing information about the channel.
         * @throws IOException
         */

        private ArrayList<VideoDetails> dispalyList() throws IOException {
            YouTube.Channels.List channelRequest = mService.channels()
                    .list("contentDetails")
                    .setMine(true)
                    .setFields("items(id,contentDetails),nextPageToken,pageInfo");
                    //.setFields("items/contentDetails,nextPageToken,pageInfo");
            ChannelListResponse channelResult = channelRequest.execute();
            List<Channel> channelsList = channelResult.getItems();

            if (channelsList != null) {
                // The user's default channel is the first item in the list.
                // Extract the playlist ID for the channel's videos from the
                // API response.
                String uploadPlaylistId = originPlaylistId; //listID
                userId = channelsList.get(0).getId();
                Log.d(TAG, "userId = " + userId);

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

                // Prints information about the results.
                for(int i=0; i < playlistItemList.size(); i++) {
                    Log.d(TAG, "dispalyList進行影片篩選: " + i);
                    if(newList.contains(i)){    // 歌單 index 從player 傳過來做轉換
                        Log.d(TAG, "有效歌曲 " + i);
                        PlaylistItem playlistItem = playlistItemList.get(i);
                        // ListAdapter data
                        String video_id = playlistItem.getContentDetails().getVideoId();
                        String video_thumbnails = playlistItem.getSnippet().getThumbnails().getMedium().getUrl();
                        VideoDetails vd = new VideoDetails();

                        vd.setVideoId(video_id);
                        vd.setTitle(playlistItem.getSnippet().getTitle());
                        vd.setDescription(playlistItem.getSnippet().getDescription());
                        vd.setUrl(video_thumbnails);
                        vd.setIndex(i);
                        vd.setListId(originPlaylistId);
                        videoDetailsArrayList.add(vd);
                        // 將篩選後Id放入newVedioId
                        newVedioIdArrayList.add(video_id);

                        // 判斷資料庫是否有此資料, 沒有就加入dbNullLIst裡
                        if (!musicIdArrayList.contains(video_id)){
                            Log.d(TAG, "dispalyList: 歌曲資料庫不包含 : " + video_id);
                            MusicDetails md = new MusicDetails();
                            md.setVideoId(video_id);
                            md.setMusicTitle(playlistItem.getSnippet().getTitle());
                            dbNullList.add(md);
                        }

                    }
                }
                Log.d(TAG, "dispalyList: dbNullList size = " + dbNullList.size());
                return videoDetailsArrayList;

            }
            else{
                return null;
            }
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
            }
            else {
                //output.add(0, "Data retrieved using the YouTube Data API:");
                //mOutputText.setText(TextUtils.join("\n", output));
                Log.d(TAG, "onPostExecute: "+videoDetailsArrayList);
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





    // db 同異步問題
    public interface situatuinListIdCallback {
        void onCallback(String situatuinListId);
    }

    private void getSituatuinListId(final String situation, situatuinListIdCallback callback){
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        situationListId = document.getData().get(situation).toString();
                        Log.d(TAG, "DocumentSnapshot data: " + situationListId);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                    callback.onCallback(situationListId);
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    // Youtube
    private String insertPlaylist(YouTube mService) throws IOException {

        // This code constructs the playlist resource that is being inserted.
        // It defines the playlist's title, description, and privacy status.
        PlaylistSnippet playlistSnippet = new PlaylistSnippet();
        //playlistSnippet.setTitle("測試清單 " + Calendar.getInstance().getTime());
        playlistSnippet.setTitle(situation);
        playlistSnippet.setDescription("由Music Buddy幫您設置的 " + situation + " 專用清單");
        PlaylistStatus playlistStatus = new PlaylistStatus();
        playlistStatus.setPrivacyStatus("public");

        Playlist youTubePlaylist = new Playlist();
        youTubePlaylist.setSnippet(playlistSnippet);
        youTubePlaylist.setStatus(playlistStatus);

        // Call the API to insert the new playlist. In the API call, the first
        // argument identifies the resource parts that the API response should
        // contain, and the second argument is the playlist being inserted.
        YouTube.Playlists.Insert playlistInsertCommand =
                mService.playlists().insert("snippet,status", youTubePlaylist);
        Playlist playlistInserted = playlistInsertCommand.execute();

        // Print data from the API response and return the new playlist's
        // unique playlist ID.
        System.out.println("New Playlist name: " + playlistInserted.getSnippet().getTitle());
        System.out.println(" - Privacy: " + playlistInserted.getStatus().getPrivacyStatus());
        System.out.println(" - Description: " + playlistInserted.getSnippet().getDescription());
        System.out.println(" - Posted: " + playlistInserted.getSnippet().getPublishedAt());
        System.out.println(" - Channel: " + playlistInserted.getSnippet().getChannelId() + "\n");
        return playlistInserted.getId();

    }
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
    private class MakeUpldateRequestTask  extends AsyncTask<Void, Void, String> {

        private Exception mLastError = null;

        MakeUpldateRequestTask (GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("YouTube Data API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call YouTube Data API.
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
                // The user's default channel is the first item in the list.
                // Extract the playlist ID for the channel's videos from the
                // API response.
                String uploadPlaylistId = originPlaylistId; //listID

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
                try {
                    // 判斷newPlaylistId是否有值(如果有就代表測過此狀態, 並加入現有歌單)
                    Log.d(TAG, "PlaylistUpdates: situationListId = " + situationListId);
                    if(situationListId == ""){
                        Log.d(TAG, "PlaylistUpdates: 沒有此狀態歌單");
                        newPlaylistId = insertPlaylist(mService);

                        // 上傳situationListId到db
                        DocumentReference docRef = db.collection("users").document(userId);
                        docRef.update(situation, newPlaylistId);
                    }else{
                        newPlaylistId = situationListId;
                        Log.d(TAG, "PlaylistUpdates: 此狀態歌單ID = " + newPlaylistId);
                    }

                    // If a valid playlist was created, add a video to that playlist.
                    Log.d(TAG, "PlaylistUpdates: newPlaylistId = " + newPlaylistId);
                    for(int i = 0; i <= newVedioIdArrayList.size(); i++){
                        insertPlaylistItem(mService, newPlaylistId, newVedioIdArrayList.get(i));
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

            }
            else{
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
            }
            else {

                //mOutputText.setText(TextUtils.join("\n", Collections.singleton(output)));
                Log.d(TAG, "onPostExecute: "+newPlaylistId);
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

    // CALL BACK : 從DB取所有music data
    public interface musicIdCallback {
        void onCallback(ArrayList<String> musicIdArrayList);
    }

    private void getMusicIds(YoutubeInsertListActivity.musicIdCallback callback) {
        Log.d(TAG, "getMusicIds: 抓db音樂Id");

        db.collection("music")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String vid = document.getData().get("videoId").toString();
                                if (vid != null){
                                    musicIdArrayList.add(document.getData().get("videoId").toString());
                                }
                            }
                            Log.d(TAG, "onComplete: musicIdArrayList" + musicIdArrayList);
                            callback.onCallback(musicIdArrayList);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


    }
}







