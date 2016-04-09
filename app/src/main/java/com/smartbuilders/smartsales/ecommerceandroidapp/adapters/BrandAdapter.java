package com.smartbuilders.smartsales.ecommerceandroidapp.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.smartbuilders.smartsales.ecommerceandroidapp.R;
import com.smartbuilders.smartsales.ecommerceandroidapp.model.ProductBrand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Alberto on 9/4/2016.
 */
public class BrandAdapter extends BaseAdapter implements SectionIndexer {

    HashMap<String, Integer> alphaIndexer;
    String[] sections;
    private Context mContext;
    private ArrayList<ProductBrand> mDataset;

    public BrandAdapter(Context context, ArrayList<ProductBrand> data) {
        mContext = context;
        mDataset = data;

        alphaIndexer = new HashMap<String, Integer>();
        int size = mDataset.size();

        for (int x = 0; x < size; x++) {
            String s = mDataset.get(x).getName(); //items.get(x);
            // get the first letter of the store
            String ch = s.substring(0, 1);
            // convert to uppercase otherwise lowercase a -z will be sorted
            // after upper A-Z
            ch = ch.toUpperCase();
            // put only if the key does not exist
            if (!alphaIndexer.containsKey(ch))
                alphaIndexer.put(ch, x);
        }

        Set<String> sectionLetters = alphaIndexer.keySet();
        // create a list from the set to sort
        ArrayList<String> sectionList = new ArrayList<String>(
                sectionLetters);
        Collections.sort(sectionList);
        sections = new String[sectionList.size()];
        sections = sectionList.toArray(sections);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.brand_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        if(!TextUtils.isEmpty(mDataset.get(position).getName())){
            viewHolder.brandName.setText(mDataset.get(position).getName());
        }else{
            viewHolder.brandName.setVisibility(TextView.GONE);
        }

        if(!TextUtils.isEmpty(mDataset.get(position).getDescription())){
            viewHolder.brandDescription.setText(mDataset.get(position).getDescription());
        }else{
            viewHolder.brandDescription.setVisibility(TextView.GONE);
        }

        if(mDataset.get(position).getImageId()>0){
            viewHolder.brandImage.setImageResource(mDataset.get(position).getImageId());
        }else{
            viewHolder.brandImage.setVisibility(ImageView.GONE);
        }

        view.setTag(viewHolder);
        return view;
    }

    @Override
    public int getPositionForSection(int section) {
        return alphaIndexer.get(sections[section]);
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        return sections;
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

    public static class ViewHolder {
        public TextView brandName;
        public TextView brandDescription;
        public ImageView brandImage;

        public ViewHolder(View v) {
            brandName = (TextView) v.findViewById(R.id.brand_name_tv);
            brandDescription = (TextView) v.findViewById(R.id.brand_description_tv);
            brandImage = (ImageView) v.findViewById(R.id.brand_imageView);
        }
    }
}