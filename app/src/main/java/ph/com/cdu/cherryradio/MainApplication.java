package ph.com.cdu.cherryradio;

import android.app.Application;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.SaveCallback;

/**
 * Created by chiboon on 12/24/14.
 */
public class MainApplication extends Application {
    public static final String FLURRY_APIKEY = "B46YF5T5VY955FN3TW6G";

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this, "MkgXxS78FX6R0s73t2kwVqjy6WBCXY93WAbgaOP3", "toBoWpG29mfyM43ofpANR25XKk812Oe8q47yLEyD");

        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.v("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.v("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });

        FlurryAgent.init(this, FLURRY_APIKEY);
    }

}
