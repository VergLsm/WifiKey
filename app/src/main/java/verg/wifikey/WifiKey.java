package verg.wifikey;

import android.app.Application;

import verg.lib.VolleyRequestQueue;

/**
 * WifiKey Application
 * Created by verg on 16-5-13.
 */
public class WifiKey extends Application {

//    private static RequestQueue VRQ = null;

    @Override
    public void onCreate() {
        super.onCreate();
        VolleyRequestQueue.getVRQ(this.getApplicationContext());
//        VRQ = Volley.newRequestQueue(getApplicationContext());
    }

//    public static <T> void add2RQ(Request<T> request) {
//        VRQ.add(request);
//    }
}
