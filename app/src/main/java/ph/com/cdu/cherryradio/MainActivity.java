package ph.com.cdu.cherryradio;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toast;

import com.github.snowdream.android.app.AbstractUpdateListener;
import com.github.snowdream.android.app.DownloadTask;
import com.github.snowdream.android.app.UpdateFormat;
import com.github.snowdream.android.app.UpdateInfo;
import com.github.snowdream.android.app.UpdateOptions;
import com.github.snowdream.android.app.UpdatePeriod;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;
import net.moraleboost.streamscraper.ScrapeException;
import net.moraleboost.streamscraper.Scraper;
import net.moraleboost.streamscraper.Stream;
import net.moraleboost.streamscraper.scraper.ShoutCastScraper;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ph.com.cdu.cherryradio.RadioStreaming.State;
import ph.com.cdu.cherryradio.parseQuery.ListViewAdapter;
import ph.com.cdu.cherryradio.parseQuery.SongListAdapter;

public class MainActivity extends Activity implements RadioStreaming.OnUpdateMetaDataListener {

    ListViewAdapter adapter;
    SongListAdapter song_adapter;

    static ProgressDialog pDialog;

    public static Timetable Timetable;

    private List <Song> songList = null;
    private List <Song> currentFullSongList = null;
    private static NotificationCompat.Builder mBuilder;
    public static RemoteViews remoteViews;
    public static RemoteViews remoteViews2;
    public TextView songName, songArtist, previous_song;
    //public static TextView songHistory1;

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

    boolean is3G = false;
    boolean isWindowFocused = true;
    boolean isPlay = false;
    boolean stopcheck = false;
    boolean isConnected = false;

    Handler mHandler = new Handler();
    boolean isRunning = true;
    boolean isConnectionSearching = false;
    String globalStreamURL = "";

    static int notif_id = 1;
    String notif_songName;
    String notif_songArtist;
    Bitmap notif_metaAlbumImage;
    Bitmap notif_resizedBitmap;

    Handler handler = new Handler();
    Timer timerTask = new Timer();

    ParseObject timeTableParseObject;

    int currentPointer = 0;

    public static boolean isStreaming = false;

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

         /* FOTA */
        // Check if user device is using Wifi or data, update if on wifi.
        if(isUsingWifi() == true)
        {
            // Check for updates now!
            updateWithWifi();
        }

        setupView();

        //checkNetwork(1);

        right.setOnClickListener(new OnClickListener() {@
                                                                Override
                                                        public void onClick(View v) {
            drawerLayout.openDrawer(listView);
        }
        });

        drawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {
            @
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
                if (RadioStreaming.mediaPlayer == null) {
                    checkNetwork(1);
                    if (isConnected == true) {
                        isConnectionSearching = true;
                        isPlay = true;
                        showLoad();
                        playradio();
                        isPlay = true;
                    }
                } else if (!RadioStreaming.mediaPlayer.isPlaying()) {
                    continueStream();
                } else if (RadioStreaming.mediaPlayer.isPlaying()) {
                    pauseStream();
                }
            }
        });

        playPrev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RadioStreaming.mediaPlayer != null) {
                    if (RadioStreaming.mediaPlayer.isPlaying()) {
                        radioStreaming.stopradio();
                        isPlay = true;
                        currentPointer--;
                        if (currentPointer < 0)
                            currentPointer = currentFullSongList.size() - 1;
                        showLoad();
                        playradio();
                    }
                }
            }
        });

        playNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RadioStreaming.mediaPlayer != null) {
                    if (RadioStreaming.mediaPlayer.isPlaying()) {
                        radioStreaming.stopradio();

                        currentPointer++;
                        if (currentPointer >= currentFullSongList.size())
                            currentPointer = 0;
                        showLoad();
                        playradio();
                        isPlay = true;
                    }
                }
            }
        });

        /*if (!isConnected == true) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("No internet connection found").setCancelable(true);

            AlertDialog alert = builder.create();
            alert.show();
            playStop.setEnabled(false);
        }*/
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (isRunning) {
                    try {
                        Thread.sleep(10000);
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                // Write your code here to update the UI.
                                checkNetwork(2);
                                if (isConnected && isPlay) {
                                    getStreamMetadata();
                                }
                            }
                        });
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
        }).start();
    }


    boolean isPauseSet = false;

    public void pauseStream(){
        //checkNetwork(1);
        //RadioStreaming.mediaPlayer.pause();
        RadioStreaming.mediaPlayer.stop();
        RadioStreaming.length = RadioStreaming.mediaPlayer.getCurrentPosition();
        stopcheck = true;

        //RadioStreaming.mState = State.Paused;
        RadioStreaming.mState = State.Stopped;
        playStop.setImageResource(R.drawable.play);

        if (isPauseSet == false) {
            remoteViews.setImageViewResource(R.id.notif_play, R.drawable.play_small);
            isPauseSet = true;
        }

        isPlay = false;
    }

    boolean isContinueSet = false;

    public void continueStream(){
        checkNetwork(1);
        if (isConnected) {
            playStop.setImageResource(R.drawable.stop);
            RadioStreaming.mState = State.Playing;
            showLoad();
            playradio();
            //RadioStreaming.mediaPlayer.seekTo(RadioStreaming.length);
            //RadioStreaming.mediaPlayer.start();
            if (isContinueSet == false) {
                remoteViews.setImageViewResource(R.id.notif_play, R.drawable.stop_small);
                isContinueSet = true;
            }

            isPlay = true;
        }


    }

    public void checkNetwork(int accessPoint) {
        //accessPoint 1 is for startup detection of internet
        //accessPoint 2 is for ongoing-play-of-stream detection of internet

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (activeNetwork != null) {
            is3G = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
        }

        if (accessPoint == 1 && !isConnected){
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage("No Internet Connection Detected! Please check your internet connection and try again")
                        /*.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                              public void onClick(DialogInterface arg0, int arg1) {
                                    //System.exit(0);
                                    //checkNetwork(2);
                                    alert.close();
                              }
                        });*/
                    .setPositiveButton("OK", null);

            AlertDialog alert = builder.create();
            alert.setCancelable(false);
            alert.show();
        }

        if (accessPoint == 2 && !isConnected && isConnectionSearching){
            if (RadioStreaming.mediaPlayer.isPlaying()) {
                pauseStream();
                isConnectionSearching = false;
            }
        }
        else if (accessPoint == 2 && isConnected && !isConnectionSearching && RadioStreaming.mediaPlayer != null){
            if (!RadioStreaming.mediaPlayer.isPlaying() ) {
                continueStream();
                isConnectionSearching = true;
            }
        }

        if (accessPoint == 3 && !isConnected) {
            //checkNetwork(3);
        }
    }

    private void setupView() {
        list_songName = (TextView) findViewById(R.id.list_songName);
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
        //songHistory1 = (TextView) findViewById(R.id.songHistory1);
        //songHistory1.setVisibility(View.GONE);
        dateToday = (DateUtils.formatDateTime(context, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_12HOUR));
        cherry_playlist = getResources().getStringArray(R.array.cherry_playlist);

        //next and previous hidden for now
        playStop = (ImageButton) findViewById(R.id.playstop);
        playNext = (ImageButton) findViewById(R.id.playnext);
        playNext.setVisibility(View.GONE);
        playPrev = (ImageButton) findViewById(R.id.playprevious);
        playPrev.setVisibility(View.GONE);
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
        //songHistory1.setSelected(true);
        //songHistory1.setEllipsize(TruncateAt.MARQUEE);
        //songHistory1.setSingleLine(true);

        album_art = (ImageView) findViewById(R.id.album_art);
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
                    ParseQuery<ParseObject> defaultTimetableQuery = ParseQuery.getQuery("Timetable");
                    defaultTimetableQuery.include("sessions");
                    defaultTimetableQuery.include("sessions.songs");
                    defaultTimetableQuery.include("sessions.songs.artist");
                    defaultTimetableQuery.whereEqualTo("name", "Default Timetable");
                    defaultTimetableQuery.getFirstInBackground(new GetCallback<ParseObject>() {
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

        playStop.setEnabled(true);

        // Today's Timetable
        Timetable.sessionList = new ArrayList<>();
        Timetable.name = timeTableObject.getString("name");
        Timetable.desc = timeTableObject.getString("desc");

//        Log.v("TimeTable", Timetable.name);

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

                if (globalStreamURL == ""){
                    globalStreamURL = song.link;
                }
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

        Collections.sort(Timetable.sessionList, new Comparator<Session>() {
            @Override
            public int compare(Session prevSession, Session nextSession) {
                return prevSession.fromTime.compareTo(nextSession.fromTime);
            }
        });

        // Load correct songs in session based on current time
        for (Session session : Timetable.sessionList) {

            if(currentDate.after(session.fromTime) && currentDate.before(session.toTime)) {

                plalistURL.clear();
                if (RadioStreaming.mState == State.Playing || RadioStreaming.mState == State.Paused) {
                    RadioStreaming.mState = State.Stopped;
                    RadioStreaming.mediaPlayer.stop();
                }

                //checkNetwork(3);

                currentFullSongList = session.songList;
                //songList = getFiveSongFromList(currentFullSongList, currentPointer);
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
                        } else {
                            plalistURL.add(song.linkLowQuality);
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

        if (!isStreaming) {
            listView2 = (ListView) findViewById(R.id.drawerList2);
            View playlist_header = getLayoutInflater().inflate(R.layout.playlist_header, null);
            listView2.addHeaderView(playlist_header);
            song_adapter = new SongListAdapter(MainActivity.this, songList);
            listView2.setAdapter(song_adapter);
        }

        if (isPlay) {
            playradio();
        }
    }

    public void playradio() {
        playStop.setImageResource(R.drawable.stop);

        //checkNetwork(1);

        //while(!isConnected) {
        //checkNetwork(1);
        Log.d("PLAY SET", "PLAY SET " + plalistURL);
        radioStreaming.setPlaylist(plalistURL);
        radioStreaming.playradio(currentPointer);
        //}
    }

    public void progresshide() {
        pDialog.hide();
        songName.setVisibility(View.VISIBLE);
        songArtist.setVisibility(View.VISIBLE);
        album_art.setVisibility(View.VISIBLE);
        list_album_art.setVisibility(View.VISIBLE);
        list_songName.setVisibility(View.VISIBLE);
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

        checkForCrashes();
        checkForUpdates();

        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
    }

    private void checkForCrashes() {
        CrashManager.register(this, "ac15f9047cf292c36308a1c19d9590ea");
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(this, "ac15f9047cf292c36308a1c19d9590ea");
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
            //if (radioStreaming.metadata.metaTitle != null)
            //notif_songName = radioStreaming.metadata.metaTitle;
            //else
            //notif_songName = currentFullSongList.get(currentPointer).name;

            //notif_songArtist = radioStreaming.metadata.metaArtist;
            //notif_metaAlbumImage = radioStreaming.metadata.metaAlbumImage;

            //Bitmap default_notif_icon = drawableToBitmap(ContextCompat.getDrawable(this, R.drawable.reflection3));

            //if (notif_metaAlbumImage != default_notif_icon)
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
            notificationView.setOnClickPendingIntent(R.drawable.play, pendingplayIntent);

            Intent stopIntent = new Intent(ACTION_STOP);
            PendingIntent pendingstopIntent = PendingIntent.getBroadcast(this, 101, stopIntent, 0);
            RemoteViews notificationView2 = new RemoteViews(getPackageName(), R.layout.listview_item);
            notificationView2.setOnClickPendingIntent(R.drawable.stop, pendingstopIntent);

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
            //songHistory1.setText(prevSong);
            //songHistory1.setVisibility(View.VISIBLE);
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
        //getStreamMetadata();

//        if (isStreaming) {
//            if (radioStreaming.metadata.metaTitle == null && radioStreaming.metadata.metaArtist == null) {
//                songName.setText("");
//                songArtist.setText("");
//                songHistory1.setText("");
//                album_art.setImageResource(R.drawable.reflection3);
//            }
//        } else {
//
//        }

        if (radioStreaming.metadata.metaTitle == null && radioStreaming.metadata.metaArtist == null && RadioStreaming.mediaPlayer.isPlaying()) {
            songName.setText("Cherry Radio");
            songArtist.setText("Live Stream");
            list_songName.setText("Unknown Song");
            list_songArtist.setText("Unknown Artist");
            //songHistory1.setText("Unknown Artist");
            album_art.setImageResource(R.drawable.reflection3);
        } else {

            if (radioStreaming.metadata.metaTitle != null) {
                //songTitle = radioStreaming.metadata.metaTitle;
            }else {
                //songTitle = currentFullSongList.get(currentPointer).name;
            }

            if (radioStreaming.metadata.metaAlbum != null) {
                //songName.setText("  " + songTitle + " - " + radioStreaming.metadata.metaAlbum);
            }else{
                //songName.setText("  " + songTitle + " - Unknown Album");
            }

            if (radioStreaming.metadata.metaArtist != null) {
                //songArtist.setText("By " + radioStreaming.metadata.metaArtist);
                notif_songArtist = radioStreaming.metadata.metaArtist;
            }else {
                //songArtist.setText("By Unknown Artist");
                notif_songArtist = "Unknown Artist";
            }

            if (radioStreaming.metadata.metaAlbumImage != null){
                album_art.setImageBitmap(radioStreaming.metadata.metaAlbumImage);
                notif_metaAlbumImage = radioStreaming.metadata.metaAlbumImage;
            }else{
                album_art.setImageResource(R.drawable.reflection3);
                Bitmap default_dp = drawableToBitmap(ContextCompat.getDrawable(this, R.drawable.reflection3));
                notif_metaAlbumImage = default_dp;
            }
            //list_songName.setText(songTitle + " - " + radioStreaming.metadata.metaArtist);
            //list_songArtist.setText("By " + radioStreaming.metadata.metaArtist);
            //list_album_art.setImageBitmap(radioStreaming.metadata.metaAlbumImage);

            //notif_songName = songTitle;
            //notif_songArtist = radioStreaming.metadata.metaArtist;

            //if (notif_metaAlbumImage != null) {
            //notif_metaAlbumImage = radioStreaming.metadata.metaAlbumImage;
            //notif_resizedBitmap = Bitmap.createScaledBitmap(notif_metaAlbumImage, 80, 80, false);
            //}
        }


        //setHistorySong();

        //prevSong = songTitle;
        if (!isWindowFocused) {
            applicationdidenterbackground();
        }

        //songList = getFiveSongFromList(currentFullSongList, pointer + 1);
        //song_adapter = new SongListAdapter(MainActivity.this, songList);
        //listView2.setAdapter(song_adapter);

    }

    public void getStreamMetadata() {
        //progresshide();

        songList.clear();

        String songTitle = "";
        String currentSong = "";
        String currentSongTitle = "";
        String currentSongArtist = "";

        String nextSong = "";
        String nextSongTitle = "";
        String nextSongArtist = "";


        //get globalStreamURL
        String[] globalStreamURLTokens = globalStreamURL.split("//");
        String[] mainURL = globalStreamURLTokens[1].split("/");
        String currentSongInfo = globalStreamURLTokens[0] + "//" + mainURL[0] + "/";
        String nextSongInfo = currentSongInfo + "nextsong?sid=#";

        Scraper scraper = new ShoutCastScraper();

        try {
            List<Stream> streams = scraper.scrape(new URI(currentSongInfo));
            for (Stream stream: streams){
                currentSong = stream.getCurrentSong();
            }

        }catch (URISyntaxException | ScrapeException e) {
            e.printStackTrace();
        }

        if (currentSong != "") {
            String[] currentSongTokens = currentSong.split("-");
            currentSongArtist = currentSongTokens[0];
            currentSongTitle = currentSongTokens[1];

            addSongToPlaylist(currentSongTitle, currentSongArtist);
        }

        if (currentSongTitle != null || currentSongInfo != "") {
            //songTitle = currentSongTitle;
            songName.setText(currentSongTitle);
        }else {
            //songTitle = currentFullSongList.get(currentPointer).name;
            //songTitle = "Unknown Title";
            songName.setText("Unknown Title");
        }

        if (currentSongArtist != null || currentSongArtist != "") {
            songArtist.setText("By " + currentSongArtist);
            notif_songArtist = currentSongArtist;
        }else {
            songArtist.setText("By Unknown Artist");
            notif_songArtist = "Unknown Artist";
        }

        notif_songName = currentSongTitle;

        nextSong = getNextSongInfo(nextSongInfo);

        if (nextSong != "") {
            String[] nextSongTokens = nextSong.split("-");
            nextSongTitle = nextSongTokens[1];
            nextSongArtist = nextSongTokens[0];

            addSongToPlaylist(nextSongTitle, nextSongArtist);
        }

        list_songName.setText(currentSongTitle + " - " + currentSongArtist);
        //list_songArtist.setText("By " + radioStreaming.metadata.metaArtist);
        list_album_art.setImageResource(R.drawable.reflection3);
            /*
            if (radioStreaming.metadata.metaAlbum != null) {
                  songName.setText("  " + songTitle + " - " + radioStreaming.metadata.metaAlbum);
            }else{
                  songName.setText("  " + songTitle + " - Unknown Album");
            }

            if (radioStreaming.metadata.metaAlbumImage != null){
                  album_art.setImageBitmap(radioStreaming.metadata.metaAlbumImage);
                  notif_metaAlbumImage = radioStreaming.metadata.metaAlbumImage;
            }else{
                  album_art.setImageResource(R.drawable.reflection3);
                  Bitmap default_dp = drawableToBitmap(ContextCompat.getDrawable(this, R.drawable.reflection3));
                  notif_metaAlbumImage = default_dp;
            }
            list_songName.setText(songTitle + " - " + radioStreaming.metadata.metaArtist);
            list_songArtist.setText("By " + radioStreaming.metadata.metaArtist);
            list_album_art.setImageBitmap(radioStreaming.metadata.metaAlbumImage);

            notif_songName = songTitle;

            setHistorySong();

            prevSong = songTitle;
			*/

        Bitmap default_dp = drawableToBitmap(ContextCompat.getDrawable(this, R.drawable.reflection3));
        notif_metaAlbumImage = default_dp;

        if (!isWindowFocused) {
            applicationdidenterbackground();
        }

        song_adapter = new SongListAdapter(MainActivity.this, songList);
        listView2.setAdapter(song_adapter);
    }

    public void addSongToPlaylist(String songTitle, String songArtist){
        ParseObject artist = new ParseObject("Artist");

        artist.put("desc", "N/A");
        artist.put("name", songArtist);

        Song song = new Song();
        song.name = songTitle;
        song.desc = "N/A";
        song.album = null;
        song.artist = artist;
        song.genre = null;
        song.link = "";
        song.linkLowQuality = "";
        songList.add(song);
    }

    public String getNextSongInfo(String nextSongInfo){

        String nextSongString = "";
        try {
            URL nextSong = new URL(nextSongInfo);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            nextSong.openStream()));

            String inputLine;

            while ((inputLine = in.readLine()) != null)
                nextSongString += inputLine;

            in.close();

        }catch (Exception e){
            e.printStackTrace();
        }

        return nextSongString;

    }


    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
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

    /* Force Over the Air Update */
    public void updateWithWifi()
    {
        // Check for OTA Updates
        com.github.snowdream.android.app.UpdateManager manager = new com.github.snowdream.android.app.UpdateManager(this);

        UpdateOptions options = new UpdateOptions.Builder(this)
                .checkUrl("http://ewarranty.cdu.com.ph/cherryradio/updates/updates.xml")
                .updateFormat(UpdateFormat.XML)
                .updatePeriod(new UpdatePeriod(UpdatePeriod.EACH_TIME))
                .checkPackageName(true)
                .build();
        manager.check(this, options, new AbstractUpdateListener() {
            /**
             * Exit the app here
             */
            @Override
            public void ExitApp() {

            }

            /**
             * show the update dialog
             *
             * @param info the info for the new app
             */
            @Override
            public void onShowUpdateUI(UpdateInfo info) {
                final UpdateInfo mInfo = info;
                Toast.makeText(getContext(), "New update is available. Install the update to get the very best of Cherry Radio.", Toast.LENGTH_LONG).show();
                informUpdate(mInfo);
            }

            /**
             * It's the latest app,or there is no need to update.
             */
            @Override
            public void onShowNoUpdateUI() {
                // Just continue with the flow..
            }


            /**
             * show the progress when downloading the new app
             *
             * @param info
             * @param task
             * @param progress
             */
            @Override
            public void onShowUpdateProgressUI(UpdateInfo info, DownloadTask task, int progress) {

            }

            /**
             * show the checking dialog
             */
            @Override
            public void onStart() {
                super.onStart();
            }

            /**
             * hide the checking dialog
             */
            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }


    // Detect if user is using Wifi or Data or etc
    public boolean isUsingWifi() {
        final ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }
    /* END */
}