package ph.com.cherrymobile.radio;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils.TruncateAt;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ph.com.cherrymobile.radio.RadioStreaming.State;
import ph.com.cherrymobile.radio.parseQuery.ListViewAdapter;
import ph.com.cherrymobile.radio.parseQuery.SongListAdapter;

public class MainActivity extends Activity implements RadioStreaming.OnUpdateMetaDataListener {

    ListViewAdapter adapter;
    SongListAdapter song_adapter;

    static ProgressDialog pDialog;

    public static Timetable Timetable;

    private List < Song > songList = null;
    private List <Song> currentFullSongList = null;
    private static NotificationCompat.Builder mBuilder;
    public static RemoteViews remoteViews;
    public static RemoteViews remoteViews2;
    public TextView songName, songArtist, previous_song;
    public static TextView songHistory1;

    public TextView header_date, header_album;
    public String[] cherry_playlist;
    public String dateToday;

    public ImageView notif_stop;
    private static ImageButton playStop;
    private static ImageButton playNext;
    private static ImageButton playPrev;
    private ImageButton right;
    private DrawerLayout drawerLayout;
    private ListView listView;
    private ListView listView2;
    private static TextView notplaying;
    private SlidingDrawer drawer;
    private ImageButton handle;
    private static TextView nowplaying2;
    private static Context context;
    private static NotificationManager mNotificationManager;
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_CLOSE = "ACTION_CLOSE";
    public static TextView list_songName, list_songArtist;
    public RadioStreaming radioStreaming;
    public String prevSong = "";
    public List < String > plalistURL = new ArrayList < String > ();
    public static ImageView album_art, list_album_art, headphone;
    public ImageView top_headphone;

    boolean is3G = false;
    boolean isWindowFocused = true;
    boolean isPlay = false;
    boolean stopcheck = false;

    static int notif_id = 1;
    String notif_songName;
    String notif_songArtist;
    Bitmap notif_metaAlbumImage;
    Bitmap notif_resizedBitmap;

    Handler handler = new Handler();
    Timer timerTask = new Timer();

    ParseObject timeTableParseObject;

    int currentPointer = 0;

    @
    Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);

        context = this.getApplicationContext();
        retrieveScheduleFromParse();

        radioStreaming = new RadioStreaming();
        radioStreaming.setOnUpdateMetaDataListener(this);
        remoteViews = new RemoteViews(getPackageName(), R.layout.widget);
        remoteViews2 = new RemoteViews(getPackageName(), R.layout.widget2);

        setupView();

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        is3G = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;

        right.setOnClickListener(new OnClickListener() {@
            Override
            public void onClick(View v) {
                drawerLayout.openDrawer(listView);
            }
        });

        drawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {@
            Override
            public void onDrawerOpened() {
                handle.setImageResource(R.drawable.bottom_push_down);
            }
        });

        drawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {@
            Override
            public void onDrawerClosed() {
                handle.setImageResource(R.drawable.bottom_push_up);
            }
        });

        playStop.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (radioStreaming.mediaPlayer == null) {
                    isPlay = true;
                    showLoad();
                    playradio();
                } else if (!radioStreaming.mediaPlayer.isPlaying()) {
                    playStop.setImageResource(R.drawable.stop);
                    radioStreaming.mState = State.Playing;
                    radioStreaming.mediaPlayer.seekTo(radioStreaming.length);
                    radioStreaming.mediaPlayer.start();
                    remoteViews.setImageViewResource(R.id.notif_play, R.drawable.stop_small);
                } else if (radioStreaming.mediaPlayer.isPlaying()) {
                    radioStreaming.mediaPlayer.pause();
                    radioStreaming.length = radioStreaming.mediaPlayer.getCurrentPosition();
                    stopcheck = true;
                    radioStreaming.mState = State.Paused;
                    playStop.setImageResource(R.drawable.play);
                    remoteViews.setImageViewResource(R.id.notif_play, R.drawable.play_small);
                }
            }
        });

        playPrev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioStreaming.mediaPlayer.isPlaying()) {
                    radioStreaming.stopradio();
                    isPlay = true;
                    currentPointer--;
                    if (currentPointer < 0)
                        currentPointer = currentFullSongList.size() - 1;
                    showLoad();
                    playradio();
                }
            }
        });

        playNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioStreaming.mediaPlayer.isPlaying()) {
                    radioStreaming.stopradio();
                    isPlay = true;
                    currentPointer++;
                    if (currentPointer >= currentFullSongList.size())
                        currentPointer = 0;
                    showLoad();
                    playradio();
                }
            }
        });

        if (!isConnected == true) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("No internet connection found").setCancelable(true);

            AlertDialog alert = builder.create();
            alert.show();
            playStop.setEnabled(false);
        }
    }

    private void setupView() {
        list_songName = (TextView) findViewById(R.id.list_songName);
        list_songName = (TextView) findViewById(R.id.list_songName);
        list_songArtist = (TextView) findViewById(R.id.list_songArtist);
        list_songArtist = (TextView) findViewById(R.id.list_songArtist);
        listView = (ListView) findViewById(R.id.drawerList);
        listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        listView2 = (ListView) findViewById(R.id.drawerList2);
        listView2.setSelector(new ColorDrawable(Color.TRANSPARENT));
        list_songName.setVisibility(View.GONE);
        list_songArtist.setVisibility(View.GONE);
        list_songName.setVisibility(View.GONE);
        list_songArtist.setVisibility(View.GONE);
        list_songName.setSelected(true);
        list_songName.setEllipsize(TruncateAt.MARQUEE);
        list_songName.setSingleLine(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        right = (ImageButton) findViewById(R.id.righticon);
        drawer = (SlidingDrawer) findViewById(R.id.slidingDrawer);
        header_date = (TextView) findViewById(R.id.header_date);
        listView = (ListView) findViewById(R.id.drawerList);
        View header = getLayoutInflater().inflate(R.layout.group_header_item, null);
        listView.addHeaderView(header);
        header_album = (TextView) findViewById(R.id.header_album);
        songHistory1 = (TextView) findViewById(R.id.songHistory1);
        songHistory1.setVisibility(View.GONE);
        dateToday = (DateUtils.formatDateTime(context, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_12HOUR));
        cherry_playlist = getResources().getStringArray(R.array.cherry_playlist);

        playStop = (ImageButton) findViewById(R.id.playstop);
        playNext = (ImageButton) findViewById(R.id.playnext);
        playPrev = (ImageButton) findViewById(R.id.playprevious);
        handle = (ImageButton) findViewById(R.id.handle);
        songName = (TextView) findViewById(R.id.songName);
        songName.setVisibility(View.GONE);
        songArtist = (TextView) findViewById(R.id.songArtist);
        songArtist.setVisibility(View.GONE);
        songArtist.setSelected(true);
        songArtist.setEllipsize(TruncateAt.MARQUEE);
        songArtist.setSingleLine(true);
        songName.setSelected(true);
        songName.setEllipsize(TruncateAt.MARQUEE);
        songName.setSingleLine(true);
        nowplaying2 = (TextView) findViewById(R.id.nowplaying2);
        nowplaying2.setVisibility(View.GONE);
        notplaying = (TextView) findViewById(R.id.notplaying);
        notplaying.setVisibility(View.VISIBLE);
        notplaying.setSelected(true);
        notplaying.setEllipsize(TruncateAt.MARQUEE);
        notplaying.setSingleLine(true);
        songHistory1.setSelected(true);
        songHistory1.setEllipsize(TruncateAt.MARQUEE);
        songHistory1.setSingleLine(true);

        album_art = (ImageView) findViewById(R.id.album_art);
        top_headphone = (ImageView) findViewById(R.id.top_headphone);
        top_headphone.setVisibility(View.GONE);
        list_album_art = (ImageView) findViewById(R.id.list_album_art);
        list_album_art.setVisibility(View.GONE);
        headphone = (ImageView) findViewById(R.id.headphone);
        headphone.setVisibility(View.GONE);
    }

    private void retrieveScheduleFromParse() {
        /**
         * 1. Check if today have a timetable?
         * 2. If not using Default timetable
         * 3. Store the timetable, session, song locally
         * 4. Play song randomly within the lists
         * 5. Check if move to next sessions
         */

        Date StartDay = new Date();
        StartDay.setHours(0);
        StartDay.setMinutes(0);
        StartDay.setSeconds(0);

        Date EndDay = new Date();
        EndDay.setHours(23);
        EndDay.setMinutes(60);
        EndDay.setSeconds(60);

        songList = new ArrayList<>();

        ParseQuery < ParseObject > timetableQuery = ParseQuery.getQuery("Timetable");
        timetableQuery.whereGreaterThan("date", StartDay);
        timetableQuery.whereLessThan("date", EndDay);
        timetableQuery.include("sessions");
        timetableQuery.include("sessions.songs");
        timetableQuery.include("sessions.songs.artist");

        timetableQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject timeTableObject, ParseException e) {
                Timetable = new Timetable();

                if (e == null) {
                    if (timeTableObject != null) {
                        timeTableParseObject = timeTableObject;
                        organizeTimeTableData(timeTableObject);
                    }
                } else {
                    // Get Default Timetable
                    ParseQuery < ParseObject > defaultTimetableQuery = ParseQuery.getQuery("Timetable");
                    defaultTimetableQuery.include("sessions");
                    defaultTimetableQuery.include("sessions.songs");
                    defaultTimetableQuery.include("sessions.songs.artist");
                    defaultTimetableQuery.getInBackground("aKOcAkgT8y", new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject timeTableObject, ParseException e) {
                            if (e == null)
                                if (timeTableObject != null) {
                                    timeTableParseObject = timeTableObject;
                                    organizeTimeTableData(timeTableObject);
                                }
                        }
                    });
                }
            }
        });

    }

    private void organizeTimeTableData(ParseObject timeTableObject) {
        // Today's Timetable
        Timetable.sessionList = new ArrayList<>();
        Timetable.name = timeTableObject.getString("name");
        Timetable.desc = timeTableObject.getString("desc");

        List<ParseObject> sessionObjects = timeTableObject.getList("sessions");
        for (ParseObject sessionObject : sessionObjects) {
            Session session = new Session();
            session.songList = new ArrayList<>();
            session.name = sessionObject.getString("name");
            session.desc = sessionObject.getString("desc");

            SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
            try {
                session.fromTime = formatter.parse(sessionObject.getString("fromTime"));
                session.toTime = formatter.parse(sessionObject.getString("toTime"));
            } catch (java.text.ParseException e1) {
                e1.printStackTrace();
            }

            List<ParseObject> songObjects = sessionObject.getList("songs");
            for (ParseObject songObject : songObjects) {
                Song song = new Song();
                song.name = songObject.getString("name");
                song.desc = songObject.getString("desc");
                song.album = (ParseObject)songObject.get("album");
                song.artist = (ParseObject)songObject.get("artist");
                song.genre = (ParseObject)songObject.get("genre");
                song.link = songObject.getString("link");
                song.linkLowQuality = songObject.getString("linkLowQuality");
                session.songList.add(song);
            }
            Timetable.sessionList.add(session);
        }

        playCurrentSession();
    }

    private void playCurrentSession() {
        Date currentDate = new Date();
        currentDate.setYear(70);
        currentDate.setMonth(0);
        currentDate.setDate(1);

        // Load correct songs in session based on current time
        for (Session session : Timetable.sessionList) {

            if(currentDate.after(session.fromTime) && currentDate.before(session.toTime)) {

                currentFullSongList = session.songList;
                songList = getFiveSongFromList(currentFullSongList, currentPointer);
                long timeDiff = session.toTime.getTime() - currentDate.getTime();

                for (Song song: session.songList) {
                    if (is3G) {
                        if (song.linkLowQuality.length() != 0)
                            plalistURL.add(song.linkLowQuality);
                        else
                            plalistURL.add(song.link);
                    } else {
                        if (song.link.length() != 0) {
                            plalistURL.add(song.link);
                            Log.v("song link", song.link);
                        } else {
                            plalistURL.add(song.linkLowQuality);
                            Log.v("song linkLowQuality", song.linkLowQuality);
                        }
                    }
                }

                // Schedule Timer for next session
                TimerTask doAsynchronousTask = new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            public void run() {
                                try {
                                    organizeTimeTableData(timeTableParseObject);
                                } catch (Exception e) {}
                            }
                        });
                    }
                };

                timerTask.schedule(doAsynchronousTask, timeDiff);

                break;
            }
        }

        adapter = new ListViewAdapter(MainActivity.this, Timetable.sessionList);
        listView.setAdapter(adapter);
        header_date = (TextView) findViewById(R.id.header_date);
        header_date.setText(DateUtils.formatDateTime(context, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY));

        listView2 = (ListView) findViewById(R.id.drawerList2);
        View playlist_header = getLayoutInflater().inflate(R.layout.playlist_header, null);
        listView2.addHeaderView(playlist_header);
        song_adapter = new SongListAdapter(MainActivity.this, songList);
        listView2.setAdapter(song_adapter);

        if (isPlay) {
            playradio();
        }
    }

    public void playradio() {
        playStop.setImageResource(R.drawable.stop);

        Log.d("PLAY SET", "PLAY SET " + plalistURL);
        radioStreaming.setPlaylist(plalistURL);
        radioStreaming.playradio(currentPointer);
    }

    public void progresshide() {
        pDialog.hide();
        songName.setVisibility(View.VISIBLE);
        songArtist.setVisibility(View.VISIBLE);
        album_art.setVisibility(View.VISIBLE);
        list_album_art.setVisibility(View.VISIBLE);
        list_songName.setVisibility(View.VISIBLE);
        top_headphone.setVisibility(View.VISIBLE);
        notplaying.setVisibility(View.GONE);
    }

    public void showLoad() {
        pDialog = new ProgressDialog(this, R.style.MyTheme);
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        pDialog.show();
    }

    @
    Override
    protected void onStop() {
        super.onStop();
        isWindowFocused = false;

        if (radioStreaming.mState == State.Playing || radioStreaming.mState == State.Paused || radioStreaming.mState == State.Stopped) {
            applicationdidenterbackground();
        }
    }

    @
    Override
    protected void onResume() {
        super.onResume();
        isWindowFocused = true;

        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();

        }
    }

    @
    Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void applicationdidenterbackground() {
        broadcastIntent();
    }

    public void broadcastIntent() {

        if (notif_songName != null && notif_songArtist != null && notif_metaAlbumImage != null) {
            if (radioStreaming.metadata.metaTitle != null)
                notif_songName = radioStreaming.metadata.metaTitle;
            else
                notif_songName = currentFullSongList.get(currentPointer).name;

            notif_songArtist = radioStreaming.metadata.metaArtist;
            notif_metaAlbumImage = radioStreaming.metadata.metaAlbumImage;
            notif_resizedBitmap = Bitmap.createScaledBitmap(notif_metaAlbumImage, 100, 100, false);

            remoteViews.setTextViewText(R.id.notif_songname, notif_songName);
            remoteViews.setTextViewText(R.id.notif_songartist, notif_songArtist);
            remoteViews.setImageViewBitmap(R.id.imageNotif, notif_metaAlbumImage);

            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(Intent.ACTION_MAIN);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Intent playIntent = new Intent(ACTION_PLAY);
            PendingIntent pendingplayIntent = PendingIntent.getBroadcast(this, 100, playIntent, 0);
            RemoteViews notificationView = new RemoteViews(getPackageName(), R.drawable.play);
            notificationView.setOnClickPendingIntent(R.raw.play, pendingplayIntent);

            Intent stopIntent = new Intent(ACTION_STOP);
            PendingIntent pendingstopIntent = PendingIntent.getBroadcast(this, 101, stopIntent, 0);
            RemoteViews notificationView2 = new RemoteViews(getPackageName(), R.layout.listview_item);
            notificationView2.setOnClickPendingIntent(R.raw.stop, pendingstopIntent);

            Intent closeIntent = new Intent(ACTION_CLOSE);
            PendingIntent pendingcloseIntent = PendingIntent.getBroadcast(this, 100, closeIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            RemoteViews notificationView3 = new RemoteViews(getPackageName(), R.drawable.close);

            mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.notif_icon).setContent(remoteViews);
            mBuilder.setContentIntent(contentIntent);
            mBuilder.setContentTitle(notif_songName);
            mBuilder.setContentText(notif_songArtist);
            mBuilder.setTicker(notif_songName + " - " + notif_songArtist);
            mBuilder.setAutoCancel(false);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            Intent resultIntent = new Intent(this, MainActivity.class);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
//            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//            PendingIntent closePendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);

            remoteViews.setOnClickPendingIntent(R.id.notif_play, pendingplayIntent);
            remoteViews.setOnClickPendingIntent(R.id.notif_stop, pendingstopIntent);
            remoteViews.setOnClickPendingIntent(R.id.notif_close, pendingcloseIntent);
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(notif_id, mBuilder.build());
        }
    }

    public static void cancelNotification() {
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }

    }

    private void setHistorySong() {
        if (prevSong != null) {
            songHistory1.setText(prevSong);
            songHistory1.setVisibility(View.VISIBLE);
        }
    }

    public static void playResources() {
        playStop.setImageResource(R.drawable.stop);
        remoteViews.setImageViewResource(R.id.notif_play, R.drawable.stop_small);
        mBuilder.setContent(remoteViews);
        mNotificationManager.notify(notif_id, mBuilder.build());
    }

    public static void stopResources() {
        playStop.setImageResource(R.drawable.play);
        remoteViews.setImageViewResource(R.id.notif_play, R.drawable.play_small);
        mBuilder.setContent(remoteViews);
        mNotificationManager.notify(notif_id, mBuilder.build());

    }

    @Override
    public void onUpdateMetaData(int pointer) {
        progresshide();

        String songTitle = "";

        if (radioStreaming.metadata.metaTitle == null && radioStreaming.metadata.metaArtist == null) {
            songName.setText("Unknown Song");
            songArtist.setText("Unknown Artist");
            list_songName.setText("Unknown Song");
            list_songArtist.setText("Unknown Artist");
            songHistory1.setText("Unknown Artist");
        } else {
            if (radioStreaming.metadata.metaTitle != null)
                songTitle = radioStreaming.metadata.metaTitle;
            else
                songTitle = currentFullSongList.get(currentPointer).name;

            songName.setText("  " + songTitle + " - " + radioStreaming.metadata.metaAlbum);
            songArtist.setText("By " + radioStreaming.metadata.metaArtist);
            album_art.setImageBitmap(radioStreaming.metadata.metaAlbumImage);
            list_songName.setText(songTitle + " - " + radioStreaming.metadata.metaArtist);
            list_songArtist.setText("By " + radioStreaming.metadata.metaArtist);
            list_album_art.setImageBitmap(radioStreaming.metadata.metaAlbumImage);

            notif_songName = songTitle;
            notif_songArtist = radioStreaming.metadata.metaArtist;
            notif_metaAlbumImage = radioStreaming.metadata.metaAlbumImage;
            notif_resizedBitmap = Bitmap.createScaledBitmap(notif_metaAlbumImage, 80, 80, false);
        }

        setHistorySong();

        prevSong = songTitle;
        if (isWindowFocused == false) {
            applicationdidenterbackground();
        }

        songList = getFiveSongFromList(currentFullSongList, pointer + 1);
        song_adapter = new SongListAdapter(MainActivity.this, songList);
        listView2.setAdapter(song_adapter);

    }

    private List<Song> getFiveSongFromList (List<Song> songList, int currentPointer) {
        int totalSize = 5;
        int totalSongCount = songList.size();

        List<Song> fiveSongList = new ArrayList<>();
        for (int i = currentPointer; i < totalSongCount; i++) {
            fiveSongList.add(songList.get(i));
        }

        while (fiveSongList.size() < totalSize) {
            int remainSize = totalSize - fiveSongList.size();
            for (int i = 0; i < (remainSize > totalSongCount ? totalSongCount : remainSize); i++) {
                fiveSongList.add(songList.get(i));
            }
        }

        return fiveSongList;
    }
}