package com.smartbuilders.smartsales.ecommerceandroidapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jasgcorp.ids.model.User;
import com.smartbuilders.smartsales.ecommerceandroidapp.R;
import com.smartbuilders.smartsales.ecommerceandroidapp.data.OrderLineDB;
import com.smartbuilders.smartsales.ecommerceandroidapp.model.Order;

import java.util.ArrayList;

/**
 * Created by Alberto on 7/4/2016.
 */
public class OrdersListAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Order> mDataset;
    private OrderLineDB orderLineDB;

    public OrdersListAdapter(Context context, ArrayList<Order> data, User user) {
        mContext = context;
        mDataset = data;
        orderLineDB = new OrderLineDB(context, user);
    }

    @Override
    public int getCount() {
        return mDataset.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataset.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mDataset.get(position).getId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.order_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        viewHolder.orderNumber.setText(mContext.getString(R.string.order_number, mDataset.get(position).getOrderNumber()));
        viewHolder.orderDate.setText(mContext.getString(R.string.order_date, mDataset.get(position).getCreated().toString()));
        viewHolder.orderLinesNumber.setText(mContext.getString(R.string.order_lines_numer,
                String.valueOf(mDataset.get(position).getOrderLineNumbers())));

        view.setTag(viewHolder);
        return view;
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        // each data item is just a string in this case
        public TextView orderNumber;
        public TextView orderDate;
        public TextView orderLinesNumber;

        public ViewHolder(View v) {
            orderNumber = (TextView) v.findViewById(R.id.order_number_tv);
            orderDate = (TextView) v.findViewById(R.id.order_date_tv);
            orderLinesNumber = (TextView) v.findViewById(R.id.order_lines_number_tv);
        }
    }
}