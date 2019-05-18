package com.github.pwittchen.neurosky.app.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.github.pwittchen.neurosky.app.Model.VideoDetails;
import com.github.pwittchen.neurosky.app.R;
import com.github.pwittchen.neurosky.app.YoutubePlayerActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyVideoAdapter extends BaseAdapter {

    Activity activity ;
    ArrayList<VideoDetails> videoDetailsArrayList;
    LayoutInflater inflater;
    public MyVideoAdapter(Activity activity, ArrayList<VideoDetails> videoDetailsArrayList){
        this.activity = activity;
        this.videoDetailsArrayList = videoDetailsArrayList;
    }


    @Override
    public Object getItem(int position) {
        return this.videoDetailsArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long)position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent ) {

        if (inflater == null)
        {
            inflater = this.activity.getLayoutInflater();
        }
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.video_item,null);
        }

        ImageView imageView = (ImageView)convertView.findViewById(R.id.imageView);
        TextView textView = (TextView)convertView.findViewById(R.id.mytitle);
        LinearLayout linearLayout = (LinearLayout)convertView.findViewById(R.id.root);
        final  VideoDetails videoDetails = (VideoDetails)this.videoDetailsArrayList.get(position);
        /*
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             Intent i = new Intent(activity, YoutubePlayerActivity.class);
             i.putExtra("videoIndex",videoDetails.getIndex());
             i.putExtra("playlistId", videoDetails.getListId());
             i.putExtra("situation", videoDetails.getSituation());
             i.putExtra("userId", videoDetails.getUserId());
             activity.startActivity(i);
            }
        });*/
         Picasso.get().load(videoDetails.getUrl()).into(imageView);
         textView.setText(videoDetails.getTitle());
        return convertView;
    }

    @Override
    public int getCount() {
          return  this.videoDetailsArrayList.size();
    }

}
