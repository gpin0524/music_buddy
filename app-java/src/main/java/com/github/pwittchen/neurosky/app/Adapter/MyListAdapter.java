package com.github.pwittchen.neurosky.app.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.pwittchen.neurosky.app.Model.ListDetails;
import com.github.pwittchen.neurosky.app.R;
import com.github.pwittchen.neurosky.app.YoutubeVideoListActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class MyListAdapter extends BaseAdapter {

    Activity activity ;
    ArrayList<ListDetails> listDetailsArrayList;
    LayoutInflater inflater;
    public MyListAdapter(Activity activity, ArrayList<ListDetails> listDetailsArrayList){
        this.activity = activity;
        this.listDetailsArrayList = listDetailsArrayList;
    }


    @Override
    public Object getItem(int position) {
        return this.listDetailsArrayList.get(position);
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
            convertView = inflater.inflate(R.layout.playlist_item,null);
        }

        ImageView imageView = (ImageView)convertView.findViewById(R.id.imageView);
        TextView textView = (TextView)convertView.findViewById(R.id.mytitle);
        LinearLayout linearLayout = (LinearLayout)convertView.findViewById(R.id.root);
        final  ListDetails listDetails = (ListDetails)this.listDetailsArrayList.get(position);

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             Intent i = new Intent(activity, YoutubeVideoListActivity.class);

             i.putExtra("listId",listDetails.getListId());
             i.putExtra("listIndex",listDetails.getIndex());
             i.putExtra("situation", listDetails.getSituation());
             i.putExtra("userId",listDetails.getUserId());
             i.putExtra("selectedAccount",listDetails.getSelectedAccount());
             activity.startActivity(i);
            }
        });
         Picasso.get().load(listDetails.getUrl()).into(imageView);
         textView.setText(listDetails.getTitle());
        Log.d(TAG, "getView: 名稱 = " + listDetails.getTitle());
        return convertView;
    }

    @Override
    public int getCount() {
          return  this.listDetailsArrayList.size();
    }


    public void clear() {
        listDetailsArrayList.clear();
    }

}
