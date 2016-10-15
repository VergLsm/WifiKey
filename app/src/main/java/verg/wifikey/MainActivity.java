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
        try {
            JSONObject jsonObject = new JSONObject(response);
            String retCd = jsonObject.optString("retCd");
            if (retCd != null){
                if (retCd.equals("0")){
                    //init
                    JSONObject qryapwd = jsonObject.optJSONObject("qryapwd");
                    if (qryapwd != null) {
                        mWifiMasterKey.setSalt(jsonObject.getString("retSn"));
                        if (qryapwd.optString("retCd").equals("0")){
                            //{"retSn":"6d64e0ade80f45f187bac05925904367",
                            // "qryapwd":{  "retCd":"0",
                            //              "psws":{"a4:56:02:cb:07:22":{"bssid":"a4:56:02:cb:07:22","pwd":"3EDD2758F1B4AF0511825F61E8A856DC9B31DF981425BE590354A24DEA8B9EC6","hid":"B845611ECFEF0865B15FE8151610F061","xJs":"","ssid":"禅信律师","xUser":"","type":"internet","xPwd":"","securityLevel":"2"}},
                            //              "topn":{"04:02:1f:5e:70:fcLRWL1802":0,"28:2c:b2:e9:13:baFSSOFT3":0,"8c:21:0a:a2:b9:f2TP-LINK_A2B9F2":0,"dc:9c:9f:e0:32:7aChinaNet-sejU":0,"e0:46:9a:57:b6:5dNETGEAR":168,"8a:25:93:af:98:4fGP":0,"90:94:e4:3a:f2:b8LJ30624300":0,"8c:f2:28:ad:8c:3eMERCURY_888":0,"a4:56:02:cb:07:22禅信律师":0,"20:0c:c8:4a:fd:44LRWL1801":0,"04:02:1f:5e:71:00LRWL1802_5G":0,"a4:56:02:bc:de:8f禅信律师（宋）":0,"00:1f:a3:4f:0d:6bLRWL1803":0},
                            //              "qid":"6d64e0ad-e80f-45f1-87ba-c05925904367",
                            //              "sysTime":"1476525363881"},
                            // "retCd":"0",
                            // "commonswitch":{"retCd":"0","switchFlag":"false"}}
//                            mWifiMasterKey.setSalt(jsonObject.getString("retSn"));
                            parsingResponse(jsonObject);
                        }else if (qryapwd.optString("retCd").equals("-9998")){
                            //{"retSn":"a99306b638ec4f4ba115a6dcc5665287",
                            // "qryapwd":{  "retCd":"-9998",
                            //              "retMsg":"亲，不能这样，太频繁啦！歇会，歇会。",
                            //              "qid":"a99306b6-38ec-4f4b-a115-a6dcc5665287",
                            //              "sysTime":"1476525368106"},
                            // "retCd":"0",
                            // "commonswitch":{"retCd":"0","switchFlag":"false"}}
//                            mWifiMasterKey.setSalt(jsonObject.optString("retSn"));
                            Snackbar.make(mFab, jsonObject.optJSONObject("qryapwd").optString("retMsg"), Snackbar.LENGTH_SHORT).show();
                        }
                    }else{
                        //{"initdev":{"retCd":"0","dhid":"a9ab25997d794da99e0311b11cdec364"},
                        // "retCd":"0",
                        // "commonswitch":{"retCd":"0","switchFlag":"true"}}
                        JSONObject initdev = jsonObject.optJSONObject("initdev");
                        if (initdev != null) {
                            String dhid = initdev.optString("dhid");
                            if (dhid != null) {
                                mWifiMasterKey.setDhid(dhid);
                            }
                        }
                    }
                }else if(retCd.equals("-1111")){
                    //{"retCd":"-1111",
                    // "retMsg":"商户数字签名错误，请联系请求发起方！",
                    // "retSn":"02f414f42b3c408fb9ec5bb352609420"}
                    Snackbar.make(mFab, jsonObject.optString("retMsg"), Snackbar.LENGTH_SHORT).show();
//                        mWifiMasterKey.setSigned(false);
                    mWifiMasterKey.setSalt(jsonObject.optString("retSn"));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        mFab.setVisibility(View.VISIBLE);
    }

    @Background
    protected void parsingResponse(JSONObject jsonObject) {
        LinkedHashMap<String, Map<String, String>> apList = WifiMasterKey.parsingResponse(jsonObject);
        mApListAdapter.addKeys(apList);
    }

}
