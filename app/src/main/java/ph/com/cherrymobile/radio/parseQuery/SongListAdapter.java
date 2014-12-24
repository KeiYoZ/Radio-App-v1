package ph.com.cherrymobile.radio.parseQuery;

import java.util.ArrayList;
import java.util.List;

import ph.com.cherrymobile.radio.R;
import ph.com.cherrymobile.radio.Song;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SongListAdapter extends BaseAdapter {
	 
    // Declare Variables
    Context mContext;
    LayoutInflater inflater;
    private List<Song> songList = null;
    private ArrayList<Song> arraylist;
 
    public SongListAdapter(Context context,
            List<Song> songList) {
        mContext = context;
        this.songList = songList;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<Song>();
        this.arraylist.addAll(songList);
    }
 
    public class ViewHolder {
    	
    	//Song CLASS
    	TextView name;
    	TextView artist;
    }
 
    @Override
    public int getCount() {
        return songList.size();
    }
 
    @Override
    public Song getItem(int position) {
        return songList.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.song_item, null);
            
            // Song CLASS - Locate the TextViews in listview_item.xml
            holder.name = (TextView) view.findViewById(R.id.songName_parse);
            holder.artist = (TextView) view.findViewById(R.id.songArtist_parse);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Song CLASS - Set the results into TextViews
        holder.name.setText(songList.get(position).name);
        holder.artist.setText(songList.get(position).artist.getString("name"));
        
        return view;
    }
}