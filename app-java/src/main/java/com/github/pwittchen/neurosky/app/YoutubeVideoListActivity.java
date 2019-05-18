package com.github.pwittchen.neurosky.app;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import com.github.pwittchen.neurosky.app.Model.VideoDetails;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.annotations.NonNull;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.content.ContentValues.TAG;

public class YoutubeVideoListActivity extends YouTubeBaseActivity implements EasyPermissions.PermissionCallbacks{

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { YouTubeScopes.YOUTUBE };


    //TextView mOutputText;
    ProgressDialog mProgress;
    GoogleAccountCredential mCredential;

    ListView listview;
    ArrayList<VideoDetails> videoDetailsArrayList;
    MyVideoAdapter myVideoAdapter;
    String playlistId;
    String situation;
    String userId;
    String selectedAccount;
    ArrayList<String> videoIdArrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_video_list);


        // 取從AllList 來的值
        Intent i_formAllList = getIntent();
        playlistId = i_formAllList.getStringExtra("listId");
        userId = i_formAllList.getStringExtra("userId");
        selectedAccount = i_formAllList.getStringExtra("selectedAccount");
        Log.d(TAG, "onCreate: selectedAccount = " + selectedAccount);
        Log.d(TAG, "onCreate: 選擇的listId = " + playlistId + ",  userId = " + userId);

        Button playlistButton = findViewById(R.id.btn_playlist);
        playlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(YoutubeVideoListActivity.this, YoutubePlayerActivity.class);
                i.putExtra("selectedAccount", selectedAccount);
                i.putExtra("playlistId", playlistId);
                i.putExtra("situation", situation);
                i.putExtra("userId", userId);
                i.putStringArrayListExtra("videoIdArrayList",videoIdArrayList);
                startActivity(i);
            }
        });

        //mOutputText = (TextView) findViewById(R.id.mOutputText);
        //mOutputText.setMovementMethod(new ScrollingMovementMethod());

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling YouTube Data API ...");

        // Initialize credentials and service object.

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        mCredential.setSelectedAccountName(selectedAccount);

        getResultsFromApi();

        //list資料裝載
        listview = (ListView) findViewById(R.id.video_listview);
        videoDetailsArrayList = new ArrayList<>();
        myVideoAdapter = new MyVideoAdapter(YoutubeVideoListActivity.this,videoDetailsArrayList);

        Log.d(TAG, "getDataFromApi: videoDetailsArrayList = "+ videoDetailsArrayList);

        // 從YoutubeAllList  接 situation 資料
        Intent i = getIntent();
        situation = i.getStringExtra("situation");
        Log.d(TAG, "Video list 情境狀態 situation: " + situation);


    }


    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            //mOutputText.setText("No network connection available.");
            Log.d(TAG, "getResultsFromApi   ERROR : No network connection available.");
        } else {
            new MakeRequestTask(mCredential).execute();
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
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);

            //String accountName = ;
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
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
                    Log.d(TAG, "onActivityResult: ERORR : This app requires Google Play Services. Please install. Google Play Services on your device and relaunch this app.");
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
                YoutubeVideoListActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }
    /**
     * An asynchronous task that handles the YouTube Data API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, ArrayList<VideoDetails>> {
        private YouTube mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
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
                    .setFields("items/contentDetails,nextPageToken,pageInfo");
            ChannelListResponse channelResult = channelRequest.execute();
            List<Channel> channelsList = channelResult.getItems();

            if (channelsList != null) {
                // The user's default channel is the first item in the list.
                // Extract the playlist ID for the channel's videos from the
                // API response.
                String uploadPlaylistId = playlistId; //listID

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
                    PlaylistItem playlistItem = playlistItemList.get(i);
                    //listAdapter data

                    String video_id = playlistItem.getContentDetails().getVideoId();
                    String video_thumbnails = playlistItem.getSnippet().getThumbnails().getMedium().getUrl();
                    VideoDetails vd = new VideoDetails();
                    videoIdArrayList.add(video_id);
                    vd.setVideoId(video_id);
                    vd.setTitle(playlistItem.getSnippet().getTitle());
                    vd.setDescription(playlistItem.getSnippet().getDescription());
                    vd.setUrl(video_thumbnails);
                    vd.setIndex(i);
                    vd.setListId(playlistId);
                    vd.setSituation(situation);
                    vd.setUserId(userId);
                    videoDetailsArrayList.add(vd);
                    Log.d(TAG, "vd"+vd);
                    Log.d(TAG, "videoDetailsArrayList"+videoDetailsArrayList);

                }

                // 新增清單

                // insertPlaylist() 方法 : 新增清單
                /*
                // This code constructs the playlist resource that is being inserted.
                // It defines the playlist's title, description, and privacy status.
                PlaylistSnippet playlistSnippet = new PlaylistSnippet();
                playlistSnippet.setTitle("Test Playlist " + Calendar.getInstance().getTime());
                playlistSnippet.setDescription("A private playlist created with the YouTube API v3");
                PlaylistStatus playlistStatus = new PlaylistStatus();
                playlistStatus.setPrivacyStatus("private");

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
                Log.d(TAG, "New Playlist name: " + playlistInserted.getSnippet().getTitle());
                Log.d(TAG, " - Privacy: " + playlistInserted.getStatus().getPrivacyStatus());
                Log.d(TAG, " - Description: " + playlistInserted.getSnippet().getDescription());
                Log.d(TAG, " - Posted: " + playlistInserted.getSnippet().getPublishedAt());
                Log.d(TAG, " - Channel: " + playlistInserted.getSnippet().getChannelId() + "\n");

                // Create a new, private playlist in the authorized user's channel.
                String playlistId = playlistInserted.getId(); // insertPlaylist()


                //  insertPlaylistItem() 方法 : 新增影片

                String videoId = "SZj6rAYkYOg";
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

                Log.d(TAG, "New PlaylistItem name: " + returnedPlaylistItem.getSnippet().getTitle());
                Log.d(TAG, " - Video id: " + returnedPlaylistItem.getSnippet().getResourceId().getVideoId());
                Log.d(TAG, " - Posted: " + returnedPlaylistItem.getSnippet().getPublishedAt());
                Log.d(TAG, " - Channel: " + returnedPlaylistItem.getSnippet().getChannelId());
                Log.d(TAG, "dispalyList: "+returnedPlaylistItem.getId());
                */

                // If a valid playlist was created, add a video to that playlist.
                //insertPlaylistItem(playlistId, VIDEO_ID);

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
            mProgress.dismiss();
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
            mProgress.dismiss();
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
}

