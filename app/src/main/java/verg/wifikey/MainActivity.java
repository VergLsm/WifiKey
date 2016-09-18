package verg.wifikey;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

import verg.lib.VolleyRequestQueue;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements Response.Listener<String>,ApListAdapter.MyItemLongClickListener, ApListAdapter.MyItemClickListener {

    private String TAG = "MainActivity";

    @SystemService
    WifiManager wm;
    @ViewById(R.id.srl)
    SwipeRefreshLayout mSrl;
    @ViewById
    RecyclerView apList;
    @ViewById(R.id.fab)
    FloatingActionButton mFab;
    private WifiBroadcastReceiver wbr;
    ApListAdapter_ mApListAdapter;
    private WifiMasterKey mWifiMasterKey;

    @AfterViews
    void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.wbr = new WifiBroadcastReceiver();

        this.mApListAdapter = ApListAdapter_.getInstance_(this);
        mApListAdapter.setOnItemClickListener(this);
        mApListAdapter.setOnItemLongClickListener(this);
        // use a linear layout manager
        apList.setLayoutManager(new LinearLayoutManager(this));
        apList.setAdapter(mApListAdapter);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        apList.setHasFixedSize(true);

        this.mSrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MainActivity.this.wm.startScan();
            }
        });
        mSrl.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

//        if (savedInstanceState != null) {
//            wifiList = savedInstanceState.getParcelableArrayList("wifilist");
//        } else {
        mSrl.post(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.wm.startScan();
                mSrl.setRefreshing(true);
            }
        });
//        }
        sendSign();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Click
    void fab(View view) {
        mFab.setVisibility(View.GONE);
        if (0 == mApListAdapter.getItemCount()) {
            Snackbar.make(view, "No find wifi APEntity.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        sentRequest();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(wbr, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
//        MainActivity.this.mApListAdapter.changeDataSet(savedInstanceState.getParcelableArrayList("wifilist"));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putParcelableArrayList("wifilist", (ArrayList<? extends Parcelable>) wifiList);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(wbr);
    }

    @Override
    public void onBackPressed() {
        Snackbar.make(mFab, "确认退出？", Snackbar.LENGTH_LONG)
                .setAction("是的", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.super.onBackPressed();
                    }
                }).show();
    }

    @Override
    public void onItemLongClick(View view, int position) {
        final APEntity item = mApListAdapter.getItem(position);
        Toast.makeText(this,item.getSSID(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(View view, int position) {
        final APEntity item = mApListAdapter.getItem(position);
        Toast.makeText(this,item.getSSID(),Toast.LENGTH_SHORT).show();
    }

    class WifiBroadcastReceiver extends BroadcastReceiver {

        public void onReceive(Context c, Intent intent) {
            if (MainActivity.this.mSrl.isRefreshing()) {
                MainActivity.this.mSrl.setRefreshing(false);
            }
            MainActivity.this.mApListAdapter.changeDataSet_6(wm.getScanResults());
        }
    }

    protected void sendSign() {
        mWifiMasterKey = new WifiMasterKey();

        VolleyRequestQueue.add(mWifiMasterKey.getSignRequest(this, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.toString());
                mFab.setEnabled(true);
                mSrl.setRefreshing(false);
            }
        }));

    }

    protected void sentRequest() {
        if (!mWifiMasterKey.isSigned()) {
            sendSign();
        }
        VolleyRequestQueue.add(mWifiMasterKey.getQuestRequest(mApListAdapter.getApMap(), this, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }));
    }

    @Override
    public void onResponse(String response) {
        Log.d(TAG, response);
        mFab.setVisibility(View.VISIBLE);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.length() == 3) {
                if (jsonObject.optString("retCd").equals("-1111")) {
                    Snackbar.make(mFab, jsonObject.optString("retMsg"), Snackbar.LENGTH_SHORT).show();
                } else {
                    String dhid = jsonObject.optJSONObject("initdev").optString("dhid");
                    mWifiMasterKey.setDhid(dhid);
//                    Log.d(TAG, dhid);
                }
            } else if (jsonObject.length() == 4) {
                //
                mWifiMasterKey.setSalt(jsonObject.getString("retSn"));
                parsingResponse(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Background
    protected void parsingResponse(JSONObject jsonObject) {
        LinkedHashMap<String, Map<String, String>> apList = WifiMasterKey.parsingResponse(jsonObject);
        mApListAdapter.addKeys(apList);
    }

}
