package verg.lib;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * VolleyRequestQueue
 * Created by verg on 16/4/21.
 */
public class VolleyRequestQueue {

    private static RequestQueue mRQ = null;
    private static Context mContext = null;

    private VolleyRequestQueue() {
    }

    /**
     * 获取序列，公共用户调用，或者首先调用以获得Context
     *
     * @param context Context
     * @return RequestQueue
     */
    public static RequestQueue getVRQ(Context context) {
        if (null == mContext) {
            mContext = context.getApplicationContext();
        }
        return getVRQ();
    }

    /**
     * 获取序列，程序内调用
     *
     * @return RequestQueue
     */
    private synchronized static RequestQueue getVRQ() {
        if (mContext != null && null == mRQ) {
            // Instantiate the RequestQueue.
            mRQ = Volley.newRequestQueue(mContext);
        }
        return mRQ;
    }

    /**
     * 添加到序列，用户必须要首先调用一次这个，以获得Context
     *
     * @param context
     * @param request
     * @param <T>
     * @return
     */
    public static <T> Request<T> add(Context context, Request<T> request) {
        return getVRQ(context).add(request);
    }

    /**
     * 第二次可以调用这个函数，因为已经有Context
     *
     * @param request
     * @param <T>
     * @return
     */
    public static <T> Request<T> add(Request<T> request) {
        return getVRQ().add(request);
    }

    /**
     * 取消这个请求，已经有Context
     *
     * @param tag
     */
    public static void cancelAll(Object tag) {
        if (getVRQ() != null) {
            getVRQ().cancelAll(tag);
        }
    }

}
