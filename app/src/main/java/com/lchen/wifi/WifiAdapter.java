package com.lchen.wifi;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lchen.wifi.core.AccessPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenlei on 2018/6/4.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class WifiAdapter extends RecyclerView.Adapter<WifiAdapter.ViewHolder> {

    private List<AccessPoint> accessPoints;

    public WifiAdapter(List<AccessPoint> accessPoints) {
        if (accessPoints == null) {
            accessPoints = new ArrayList<>();
        }
        this.accessPoints = accessPoints;
    }

    @Override
    public WifiAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wifi_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(accessPoints.get(position));
    }

    @Override
    public int getItemCount() {
        return accessPoints.size();
    }

    public void setData(List<AccessPoint> aps) {
        if (aps != null && aps.size() > 0) {
            accessPoints.clear();
            accessPoints.addAll(aps);
            notifyDataSetChanged();
        }
    }

    public void addItemData(AccessPoint accessPoint) {
        accessPoints.add(accessPoint);
        notifyDataSetChanged();
    }

    public void addItemDataAll(List<AccessPoint> aps) {
        if (aps != null && aps.size() > 0) {
            accessPoints.addAll(aps);
            notifyDataSetChanged();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvWifiName;
        TextView tvWifiState;
        TextView tvWifilevel;

        StringBuilder builder = new StringBuilder();

        public ViewHolder(View itemView) {
            super(itemView);
            tvWifiName = itemView.findViewById(R.id.tv_wifi_name);
            tvWifiState = itemView.findViewById(R.id.tv_wifi_state);
            tvWifilevel = itemView.findViewById(R.id.tv_wifi_level);
        }

        public void bind(AccessPoint accessPoint) {
            tvWifiName.setText(accessPoint.getConfigName());

            builder.setLength(0);
            if (accessPoint.isSaved()) {
                builder.append("saved");
            }
            if (accessPoint.isActive()) {
                if (!TextUtils.isEmpty(builder.toString())) {
                    builder.append(',');
                }
                builder.append("active");
            }
            if (accessPoint.isConnectable()) {
                if (!TextUtils.isEmpty(builder.toString())) {
                    builder.append(',');
                }
                builder.append("connectable");
            }
            tvWifiState.setText(builder.toString());
            tvWifilevel.setText("Wifi : " + accessPoint.getLevel());
        }
    }
}
