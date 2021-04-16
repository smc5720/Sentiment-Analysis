package com.example.kaush;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MusicAdapter extends BaseAdapter {
    public ArrayList<MusicListItem> listViewItemList = new ArrayList<MusicListItem>() ;

    public MusicAdapter() { }

    @Override
    public int getCount() {
        return listViewItemList.size() ;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.yet_music_list_item, parent, false);
        }

        TextView musicTitleYetView = (TextView) convertView.findViewById(R.id.text_music_title_yet) ;
        TextView musicDateYetView = (TextView) convertView.findViewById(R.id.text_music_date_yet) ;
        TextView musicEmotionYetView = (TextView) convertView.findViewById(R.id.text_music_emotion_yet) ;

        MusicListItem listViewItem = listViewItemList.get(position);

        musicTitleYetView.setText(listViewItem.musicTitleYet);
        musicDateYetView.setText(listViewItem.musicDateYet);
        musicEmotionYetView.setText(listViewItem.musicEmotionYet);

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position ;
    }

    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }

    public void addItem(String musicTitle, String userDate, String userEmotion) {
        MusicListItem item = new MusicListItem(musicTitle,userDate,userEmotion);

        item.musicTitleYet = musicTitle;
        item.musicDateYet = userDate;
        item.musicEmotionYet = userEmotion;

        listViewItemList.add(item);
    }

    public void clearItem(){
        listViewItemList.clear();
    }
}

