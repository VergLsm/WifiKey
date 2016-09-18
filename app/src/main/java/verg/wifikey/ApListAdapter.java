package verg.wifikey;

import android.graphics.Typeface;
import android.net.wifi.ScanResult;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * ApListAdapter
 * Created by verg on 16/4/21.
 */
@EBean
public class ApListAdapter extends RecyclerView.Adapter<ApListAdapter.ViewHolder> {

    private ArrayList<APEntity> mApEntityList;
    private java.lang.String TAG = this.getClass().getSimpleName();
    private HashMap<String, Map<String, String>> mKeyMap;
    private MyItemClickListener mItemClickListener;
    private MyItemLongClickListener mItemLongClickListener;

    public ApListAdapter() {
        mApEntityList = new ArrayList<>();
        mKeyMap = new HashMap<>();
    }

    @Background
    public void changeDataSet_6(List<ScanResult> wifiList) {
        List<APEntity> nApEntity = new LinkedList<>();
        for (ScanResult s : wifiList) {
            APEntity apEntity = new APEntity(s);
            Map<String, String> key = mKeyMap.get(apEntity.getBSSID());
            if (key != null)
                apEntity.setPWD(key.get("password"));
            nApEntity.add(apEntity);
        }
        ArrayList<APEntity> temp = new ArrayList<>(nApEntity);
        Collections.sort(temp);
        mApEntityList = temp;
        notifyDataSetChanged_2();
    }

    @UiThread
    public void notifyDataSetChanged_2() {
        notifyDataSetChanged();
    }

    @UiThread
    public void notifyItemRemoved_2(int position) {
        notifyItemRemoved(position);
    }

    @UiThread
    public void notifyItemInserted_2(int position) {
        notifyItemInserted(position);
    }

    @UiThread
    public void notifyItemChanged_2(int position) {
        notifyItemChanged(position);
    }

    @Background
    public void addKeys(LinkedHashMap<String, Map<String, String>> keyList) {
        mKeyMap = keyList;
        for (int i = 0; i < mApEntityList.size(); i++) {
            APEntity apEntity = mApEntityList.get(i);
            if (apEntity.isHasKey() || apEntity.isOpen())
                continue;
            Map<String, String> key = keyList.get(apEntity.getBSSID());
            if (key != null) {
                apEntity.setPWD(key.get("password"));
                notifyItemChanged_2(i);
            }

        }
    }

    public Map<String, String> getApMap() {
        StringBuilder ssid = new StringBuilder();
        StringBuilder bssid = new StringBuilder();
        for (int i = 0; i < mApEntityList.size(); i++) {
            APEntity apEntity = mApEntityList.get(i);
            if (apEntity.isOpen() || apEntity.isHasKey())
                continue;
            ssid.append(apEntity.getSSID());
            bssid.append(apEntity.getBSSID());
            if (i < mApEntityList.size() - 1) {
                ssid.append(",");
                bssid.append(",");
            }
        }
        Map<String, String> apMap = new HashMap<>();
        apMap.put("ssid", ssid.toString());
        apMap.put("bssid", bssid.toString());
        return apMap;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {

        final TextView tvPWD;

        final TextView tvSSID;

        final TextView tvCap;

        final ImageView ivSignalLevel;
        private MyItemClickListener mItemClickListener;
        private MyItemLongClickListener mItemLongClickListener;

        public ViewHolder(View itemView, MyItemClickListener itemClickListener, MyItemLongClickListener itemLongClickListener) {
            super(itemView);
            mItemClickListener = itemClickListener;
            mItemLongClickListener = itemLongClickListener;

            CardView cardView = (CardView) itemView.findViewById(R.id.card_view);
            cardView.setOnClickListener(this);
            cardView.setOnLongClickListener(this);

            ivSignalLevel = (ImageView) itemView.findViewById(R.id.ivSignalLevel);
            tvSSID = (TextView) itemView.findViewById(R.id.ssid_text);
            tvCap = (TextView) itemView.findViewById(R.id.tvCap);
            tvPWD = (TextView) itemView.findViewById(R.id.pwd_text);
        }

        public void bind(APEntity apEntity) {
            if (apEntity.getSSID().equals("")) {
                tvSSID.setText("Hide");
                tvSSID.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
            } else {
                tvSSID.setText(apEntity.getSSID());
                tvSSID.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            }

            if (apEntity.isOnline()) {
                ivSignalLevel.setImageLevel(Math.abs(apEntity.getSignalLevel()));
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(apEntity.getBSSID());
//                stringBuilder.append(" ");
//                stringBuilder.append(apEntity.getSignalLevel());
                if (apEntity.isWPS()){
                    stringBuilder.append(" WPS");
                }
                tvCap.setText(stringBuilder.toString());
                if (apEntity.isOpen()) {
                    ivSignalLevel.setImageResource(R.drawable.wifi_sel);
                } else {
                    ivSignalLevel.setImageResource(R.drawable.wifi_sel_lock);
                }
            } else {
                ivSignalLevel.setImageResource(R.drawable.ic_signal_wifi_0_bar_black_24dp);
                tvCap.setText(apEntity.getBSSID());
            }

            if (apEntity.isHasKey()) {
                tvPWD.setVisibility(View.VISIBLE);
                tvPWD.setText(apEntity.getPWD());
            } else {
                tvPWD.setVisibility(View.GONE);
            }

        }

        @Override
        public void onClick(View v) {
            if(mItemClickListener != null){
                mItemClickListener.onItemClick(v,getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(mItemLongClickListener != null){
                mItemLongClickListener.onItemLongClick(v, getAdapterPosition());
            }
            return true;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview, parent, false);

        return new ViewHolder(v,mItemClickListener,mItemLongClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(mApEntityList.get(position));
    }

    @Override
    public int getItemCount() {
        return mApEntityList.size();
    }

    public APEntity getItem(int position){
        return mApEntityList.get(position);
    }

    public interface MyItemClickListener {
        void onItemClick(View view, int postion);
    }

    public interface MyItemLongClickListener {
        void onItemLongClick(View view, int postion);
    }

    /**
     * 设置Item点击监听
     * @param listener
     */
    public void setOnItemClickListener(MyItemClickListener listener){
        this.mItemClickListener = listener;
    }

    public void setOnItemLongClickListener(MyItemLongClickListener listener){
        this.mItemLongClickListener = listener;
    }
}
