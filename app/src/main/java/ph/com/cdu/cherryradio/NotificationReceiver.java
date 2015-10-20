package ph.com.cdu.cherryradio;


import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.ImageButton;

import ph.com.cdu.cherryradio.RadioStreaming.State;

public class NotificationReceiver extends BroadcastReceiver {

	private static ImageButton notif_play;
	private static int clickCount = 0;
	private static NotificationCompat.Builder mBuilder;

    
    public void onReceive(Context context, Intent intent) {
    	
        String action = intent.getAction();
        if ("ACTION_PLAY".equals(action)) {
            if ( RadioStreaming.mState == State.Paused ) {
            	RadioStreaming.mState = State.Playing;
            	RadioStreaming.mediaPlayer.seekTo(RadioStreaming.length);
                  RadioStreaming.mediaPlayer.start();
                MainActivity.playResources(); 
            } else
            { 
                RadioStreaming.mediaPlayer.pause();
                RadioStreaming.mState = State.Paused;
                RadioStreaming.length = RadioStreaming.mediaPlayer.getCurrentPosition();
                MainActivity.stopResources();
            }

        } else if ("ACTION_CLOSE".equals(action)) {
            MainActivity.cancelNotification();
            RadioStreaming.mediaPlayer.reset();
            System.exit(0);
        }


    }
    
	private int getImageToSet() {
		clickCount++;
		return clickCount % 2 == 0 ? R.drawable.play_small : R.drawable.stop_small;
	}
    
    public static void cancelNotification(Context ctx, int notif_id) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(notif_id);
    }


}