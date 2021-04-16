package com.example.kaush;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FragmentMusicList extends Fragment {

    String uid, musicDate, musicTitle, musicUrl, musicEmotion;
    DatabaseReference mDBReference = FirebaseDatabase.getInstance().getReference();

    MusicInfo musicInfo;
    ArrayList<MusicInfo> musicInfoList = new ArrayList<MusicInfo>();

    ListView musicYetList;
    MusicListItem musicListItem;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){

        View v = inflater.inflate(R.layout.fragment_music_list, container, false);

        musicYetList = v.findViewById(R.id.list_view_music_list_fragment);
        final MusicAdapter musicAdapter = new MusicAdapter();
        musicYetList.setAdapter(musicAdapter);

        mDBReference.addValueEventListener(new ValueEventListener() {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                musicAdapter.clearItem();
                for (DataSnapshot snapshot : dataSnapshot.child("account").child(user.getUid()).child("MusicList").getChildren()) {
                    musicDate = snapshot.child("date").getValue().toString();
                    musicEmotion = snapshot.child("emotion").getValue().toString();
                    musicTitle = snapshot.child("title").getValue().toString();
                    musicUrl = snapshot.child("url").getValue().toString();
                    if(musicEmotion.equals("love")){musicEmotion="사랑";}
                    if(musicEmotion.equals("cry")){musicEmotion="울음";}
                    if(musicEmotion.equals("solace")){musicEmotion="위로";}
                    musicListItem = new MusicListItem(musicTitle, musicDate, musicEmotion);
                    musicInfo = new MusicInfo(musicTitle,musicDate,musicUrl,musicEmotion);
                    musicInfoList.add(musicInfo);
                    musicAdapter.addItem(musicListItem.musicTitleYet, musicListItem.musicDateYet, musicListItem.musicEmotionYet);
                    musicAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        musicYetList.setAdapter(musicAdapter);

        musicYetList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                startActivity(new Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse(musicInfoList.get(position).url)) // edit this url
                        .setPackage("com.google.android.youtube"));
            }
        });

        return v;
    }

}
