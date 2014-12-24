package ph.com.cherrymobile.radio;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RadioStreaming implements OnPreparedListener, OnCompletionListener, OnErrorListener {
    enum State {
        Retrieving,
        Stopped,
        Preparing,
        Playing,
        Paused
    };

    public static State mState = State.Retrieving;
    public static int length;

    public MetaData metadata;

    int ctr = 0;
    int songSize = 0;

    List < String > playList = new ArrayList < String > ();

    public static MediaPlayer mediaPlayer;
    public String currentUrl;

    public interface OnUpdateMetaDataListener {
        public void onUpdateMetaData(int pointer);
    }

    private OnUpdateMetaDataListener mListener;

    public void setOnUpdateMetaDataListener(OnUpdateMetaDataListener listener) {
        mListener = listener;
    }

    public void setPlaylist(List < String > playlistURL) {
        playList = playlistURL;
        songSize = playList.size();
    }

    public void playradio(int currentPointer) {
        ctr = currentPointer;

        if (ctr < songSize) {
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnCompletionListener(this);

                currentUrl = playList.get(ctr);
                mediaPlayer.setDataSource(currentUrl);
                mediaPlayer.prepareAsync();
                mState = State.Playing;
                new MetaDataTask().execute(currentUrl);
            } catch (IllegalArgumentException e) {} catch (SecurityException e) {} catch (IllegalStateException e) {} catch (IOException e) {
                e.printStackTrace();
            }
        } else {
        	mState = State.Stopped;
        } 
    }

    public void stopradio() {
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
        mState = State.Stopped;
    }

    private void getMetaData(String url) {
        metadata = new MetaData(url);
    }

    @
    Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
    }

    @
    Override
    public void onCompletion(MediaPlayer mp) {
        ctr++;
        if (ctr >= songSize)
            ctr = 0;

        playradio(ctr);
    }

    @
    Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    public class MetaDataTask extends AsyncTask < String, String, String > {@
        Override
        protected void onPostExecute(String result) {
            mListener.onUpdateMetaData(ctr);
        }

        @
        Override
        protected String doInBackground(String...params) {
            getMetaData(params[0]);
            return null;
        }
    }

}