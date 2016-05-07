package com.smartbuilders.smartsales.ecommerceandroidapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jasgcorp.ids.model.User;
import com.smartbuilders.smartsales.ecommerceandroidapp.BrandsListActivity;
import com.smartbuilders.smartsales.ecommerceandroidapp.CategoriesListActivity;
import com.smartbuilders.smartsales.ecommerceandroidapp.ProductsListActivity;
import com.smartbuilders.smartsales.ecommerceandroidapp.R;
import com.smartbuilders.smartsales.ecommerceandroidapp.model.Product;

import java.util.ArrayList;

/**
 * Created by Alberto on 27/3/2016.
 */
public class SearchResultAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList mDataset;
    private User mCurrentUser;

    public SearchResultAdapter(Context context, ArrayList data, User user) {
        mContext = context;
        if(data!=null && data.isEmpty()){
            data.add(context.getString(R.string.search_by_category));
            data.add(context.getString(R.string.search_by_brand));
        }
        mDataset = data;
        mCurrentUser = user;
    }

    public void setData(ArrayList data, Context context){
        if(data!=null && data.isEmpty()){
            data.add(context.getString(R.string.search_by_category));
            data.add(context.getString(R.string.search_by_brand));
        }
        mDataset = data;
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
        if(mDataset.get(position) instanceof Product){
            return ((Product) mDataset.get(position)).getId();
        }
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.search_result_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        if(mDataset.get(position) instanceof Product){
            if(!TextUtils.isEmpty(((Product) mDataset.get(position)).getName())){
                viewHolder.title.setText(((Product) mDataset.get(position)).getName());
            }else{
                viewHolder.title.setVisibility(TextView.GONE);
            }
            if(((Product) mDataset.get(position)).getProductSubCategory()!=null &&
                    !TextUtils.isEmpty(((Product) mDataset.get(position)).getProductSubCategory().getDescription())){
                viewHolder.subTitle.setText(((Product) mDataset.get(position)).getProductSubCategory().getDescription());
            }else{
                viewHolder.subTitle.setVisibility(TextView.GONE);
            }
            view.findViewById(R.id.linearLayout_container).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToProductList((Product) mDataset.get(position));
                }
            });
        }else if(mDataset.get(position) instanceof String){
            viewHolder.title.setTextSize(18);
            viewHolder.title.setPadding(0, 12, 0, 12);
            viewHolder.title.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
            viewHolder.title.setText((String) mDataset.get(position));
            viewHolder.subTitle.setVisibility(TextView.GONE);

            if(mDataset.get(position).equals(mContext.getString(R.string.search_by_category))) {
                view.findViewById(R.id.linearLayout_container).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mContext.startActivity(new Intent(mContext, CategoriesListActivity.class)
                                .putExtra(CategoriesListActivity.KEY_CURRENT_USER, mCurrentUser));
                    }
                });
            } else if (mDataset.get(position).equals(mContext.getString(R.string.search_by_brand))) {
                view.findViewById(R.id.linearLayout_container).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mContext.startActivity(new Intent(mContext, BrandsListActivity.class)
                                .putExtra(BrandsListActivity.KEY_CURRENT_USER, mCurrentUser));
                    }
                });
            }
        }

        view.setTag(viewHolder);
        return view;
    }

    private void goToProductList(Product product){
        Intent intent = new Intent(mContext, ProductsListActivity.class);
        intent.putExtra(ProductsListActivity.KEY_PRODUCT_SUBCATEGORY_ID, product.getProductSubCategory().getId());
        intent.putExtra(ProductsListActivity.KEY_PRODUCT_ID, product.getId());
        intent.putExtra(ProductsListActivity.KEY_CURRENT_USER, mCurrentUser);
        mContext.startActivity(intent);
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        // each data item is just a string in this case
        public TextView title;
        public TextView subTitle;

        public ViewHolder(View v) {
            title = (TextView) v.findViewById(R.id.title_textView);
            subTitle = (TextView) v.findViewById(R.id.subTitle_textView);
        }
    }
}
