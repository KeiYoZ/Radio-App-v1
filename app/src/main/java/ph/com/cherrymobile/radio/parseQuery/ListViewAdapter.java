package ph.com.cherrymobile.radio.parseQuery;


import ph.com.cherrymobile.radio.R;
import ph.com.cherrymobile.radio.Session;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ListViewAdapter extends BaseAdapter {
	 
    // Declare Variables
    Context mContext;
    LayoutInflater inflater;
    private List<Session> sessionList = null;
    private ArrayList<Session> arraylist;
 
    public ListViewAdapter(Context context,
            List<Session> sessionList) {
        mContext = context;
        this.sessionList = sessionList;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<Session>();
        this.arraylist.addAll(sessionList);
    }
 
    public class ViewHolder {
    	
    	//SESSION CLASS
    	TextView Schedule;
        TextView fromTime;
        TextView toTime;
        TextView sessionDesc;
        //TextView songName;
        
    }
 
    @Override
    public int getCount() {
        return sessionList.size();
    }
 
    @Override
    public Session getItem(int position) {
        return sessionList.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.listview_item, null);
            
            // SESSION CLASS - Locate the TextViews in listview_item.xml
            holder.fromTime = (TextView) view.findViewById(R.id.fromTime);
            holder.toTime = (TextView) view.findViewById(R.id.toTime);
            holder.sessionDesc = (TextView) view.findViewById(R.id.sessionDesc);
            //holder.songName = (TextView) view.findViewById(R.id.songName);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // SESSION CLASS - Set the results into TextViews

        String fromTimeString = new SimpleDateFormat("h:mm a").format(sessionList.get(position).fromTime);
        String toTimeString = new SimpleDateFormat("h:mm a").format(sessionList.get(position).toTime);

        holder.fromTime.setText(fromTimeString);
        holder.toTime.setText(toTimeString);
        holder.sessionDesc.setText(sessionList.get(position).name);
 
        return view;
    }
}