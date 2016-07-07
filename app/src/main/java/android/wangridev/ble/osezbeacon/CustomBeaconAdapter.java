package android.wangridev.ble.osezbeacon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Administrator on 7/24/2015.
 */
public class CustomBeaconAdapter extends BaseAdapter {
    private ArrayList<BeaconModel> arrBeacons;
    private Context context;

    public CustomBeaconAdapter(Context con, ArrayList<BeaconModel> arr) {
        this.context = con;
        this.arrBeacons = arr;
    }

    @Override
    public int getCount() {
        return arrBeacons.size();
    }

    @Override
    public Object getItem(int position) {
        return arrBeacons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rowView = inflater.inflate(R.layout.custom_device_item, parent, false);

            holder = new ViewHolder();
            holder.txvUUID = (TextView) rowView.findViewById(R.id.txvUUID);
            holder.txvMajor = (TextView) rowView.findViewById(R.id.txvNMajor);
            holder.txvMinor = (TextView) rowView.findViewById(R.id.txvNMinor);
            holder.txvAlarm = (TextView) rowView.findViewById(R.id.txvAlarm);

            holder.txvUUID.setTypeface(GlobalScope.mainFont);
            holder.txvMajor.setTypeface(GlobalScope.mainFont);
            holder.txvMinor.setTypeface(GlobalScope.mainFont);
            holder.txvAlarm.setTypeface(GlobalScope.mainFont);

            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        BeaconModel beacon = arrBeacons.get(position);

        holder.txvUUID.setText(beacon.sUUID);
        holder.txvMajor.setText("Major: " + beacon.nMajor);
        holder.txvMinor.setText("Minor: " + beacon.nMinor);

        return rowView;
    }

    private class ViewHolder {
        TextView txvAlarm;
        TextView txvUUID;
        TextView txvMajor;
        TextView txvMinor;
    }
}
