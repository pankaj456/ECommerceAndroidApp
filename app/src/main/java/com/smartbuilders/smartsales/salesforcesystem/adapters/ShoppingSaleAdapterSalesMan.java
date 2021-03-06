package com.smartbuilders.smartsales.salesforcesystem.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.smartbuilders.smartsales.ecommerce.BuildConfig;
import com.smartbuilders.smartsales.ecommerce.ProductDetailActivity;
import com.smartbuilders.smartsales.ecommerce.R;
import com.smartbuilders.smartsales.ecommerce.businessRules.SalesOrderSalesManBR;
import com.smartbuilders.smartsales.ecommerce.businessRules.SalesOrderLineBR;
import com.smartbuilders.smartsales.ecommerce.data.ProductDB;
import com.smartbuilders.smartsales.ecommerce.data.SalesOrderLineDB;
import com.smartbuilders.smartsales.ecommerce.model.Product;
import com.smartbuilders.smartsales.ecommerce.model.SalesOrderLine;
import com.smartbuilders.smartsales.ecommerce.session.Parameter;
import com.smartbuilders.smartsales.ecommerce.utils.Utils;
import com.smartbuilders.synchronizer.ids.model.User;

import java.util.ArrayList;

/**
 * Created by Jesus Sarco, 2/10/2016.
 */
public class ShoppingSaleAdapterSalesMan extends RecyclerView.Adapter<ShoppingSaleAdapterSalesMan.ViewHolder> {

    private Context mContext;
    private Fragment mFragment;
    private ArrayList<SalesOrderLine> mDataset;
    private User mUser;
    private SalesOrderLineDB mSalesOrderLineDB;
    private boolean mShowProductPrice;
    private boolean mShowProductTax;
    private boolean mShowProductTotalPrice;
    private boolean mShowSubTotalLineAmount;
    private boolean mShowTotalLineAmount;
    private View mParentLayout;

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView productImage;
        public TextView productInternalCode;
        public ImageView deleteItem;
        public TextView productName;
        public TextView productPrice;
        public TextView productTax;
        public TextView productTotalPrice;
        public TextView subTotalLine;
        public TextView totalLine;
        public EditText qtyOrdered;
        public View containerLayout;

        public ViewHolder(View v) {
            super(v);
            productImage = (ImageView) v.findViewById(R.id.product_image);
            productInternalCode = (TextView) v.findViewById(R.id.product_internal_code);
            productName = (TextView) v.findViewById(R.id.product_name);
            productPrice = (TextView) v.findViewById(R.id.product_price);
            productTax = (TextView) v.findViewById(R.id.product_tax);
            productTotalPrice = (TextView) v.findViewById(R.id.product_total_price);
            subTotalLine = (TextView) v.findViewById(R.id.sub_total_line_textView);
            totalLine = (TextView) v.findViewById(R.id.total_line_textView);
            deleteItem = (ImageView) v.findViewById(R.id.delete_item_button_img);
            qtyOrdered = (EditText) v.findViewById(R.id.qty_ordered);
            containerLayout = v.findViewById(R.id.container_layout);
        }
    }

    public interface Callback {
        void updateQtyOrdered(SalesOrderLine salesOrderLine);
        void reloadShoppingSale(ArrayList<SalesOrderLine> salesOrderLines, boolean setData);
    }

    public ShoppingSaleAdapterSalesMan(Context context, Fragment shoppingCartFragment,
                                       ArrayList<SalesOrderLine> data, User user) {
        mContext = context;
        mFragment = shoppingCartFragment;
        mDataset = data;
        mUser = user;
        SalesOrderSalesManBR.validateQuantityOrderedInSalesOrderLines(context, user, data);
        mSalesOrderLineDB = new SalesOrderLineDB(context, user);
        mShowProductPrice = Parameter.showProductPrice(context, user);
        mShowProductTax = Parameter.showProductTax(context, user);
        mShowProductTotalPrice = Parameter.showProductTotalPrice(context, user);
        mShowSubTotalLineAmount = Parameter.showSubTotalLineAmountInOrderLine(context, user);
        mShowTotalLineAmount = Parameter.showTotalLineAmountInOrderLine(context, user);
    }

    public void setParentLayout(View parentLayout) {
        mParentLayout = parentLayout;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ShoppingSaleAdapterSalesMan.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shopping_sale_item_sales_man, parent, false);

        return new ViewHolder(v);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        try {
            return mDataset.size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public long getItemId(int position) {
        try {
            return mDataset.get(position).getId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //esto es para que se use el precio actual del producto
        SalesOrderLineBR.fillSalesOrderLine(mDataset.get(position).getQuantityOrdered(),
                mDataset.get(position).getProduct(), mDataset.get(position));

        if (BuildConfig.USE_PRODUCT_IMAGE) {
            Utils.loadThumbImageByFileName(mContext, mUser,
                    mDataset.get(position).getProduct().getImageFileName(), holder.productImage);
        } else {
            holder.productImage.setVisibility(View.GONE);
        }

        holder.productName.setText(mDataset.get(position).getProduct().getName());

        if(mDataset.get(position).getProduct().getInternalCode()!=null){
            holder.productInternalCode.setText(mContext.getString(R.string.product_internalCode,
                    mDataset.get(position).getProduct().getInternalCodeMayoreoFormat()));
            holder.productInternalCode.setVisibility(View.VISIBLE);
        } else {
            holder.productInternalCode.setVisibility(View.GONE);
        }

        if (mShowProductPrice) {
            //holder.productPrice.setText(mContext.getString(R.string.price_detail,
            //        mDataset.get(position).getProduct().getProductPriceAvailability().getCurrency().getName(),
            //        mDataset.get(position).getProduct().getProductPriceAvailability().getProductPriceStringFormat()));
            holder.productPrice.setText(mContext.getString(R.string.product_price_label,
                    mDataset.get(position).getProduct().getProductPriceAvailability().getPriceStringFormat()));
            holder.productPrice.setVisibility(View.VISIBLE);
        } else {
            holder.productPrice.setVisibility(View.GONE);
        }

        if (mShowProductTax) {
            //holder.productTax.setText(mContext.getString(R.string.product_tax_detail,
            //        mDataset.get(position).getProduct().getProductPriceAvailability().getCurrency().getName(),
            //        mDataset.get(position).getProduct().getProductPriceAvailability().getTaxStringFormat()));
            holder.productTax.setText(mContext.getString(R.string.product_tax,
                    mDataset.get(position).getProduct().getProductPriceAvailability().getTaxStringFormat()));
            holder.productTax.setVisibility(View.VISIBLE);
        } else {
            holder.productTax.setVisibility(View.GONE);
        }

        if (mShowProductTotalPrice) {
            holder.productTotalPrice.setText(mContext.getString(R.string.product_total_price_detail,
                    mDataset.get(position).getProduct().getProductPriceAvailability().getCurrency().getName(),
                    mDataset.get(position).getProduct().getProductPriceAvailability().getTotalPriceStringFormat()));
            holder.productTotalPrice.setVisibility(View.VISIBLE);
        } else {
            holder.productTotalPrice.setVisibility(View.GONE);
        }

        if (mShowSubTotalLineAmount) {
            holder.subTotalLine.setText(mContext.getString(R.string.sales_order_sub_total_line_amount,
                    mDataset.get(position).getProduct().getProductPriceAvailability().getCurrency().getName(),
                    mDataset.get(position).getSubTotalLineAmountStringFormat()));
            holder.subTotalLine.setVisibility(View.VISIBLE);
        } else {
            holder.subTotalLine.setVisibility(View.GONE);
        }

        if (mShowTotalLineAmount) {
            holder.totalLine.setText(mContext.getString(R.string.sales_order_total_line_amount,
                    mDataset.get(position).getProduct().getProductPriceAvailability().getCurrency().getName(),
                    mDataset.get(position).getTotalLineAmountStringFormat()));
            holder.totalLine.setVisibility(View.VISIBLE);
        } else {
            holder.totalLine.setVisibility(View.GONE);
        }

        holder.deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(mContext)
                        .setMessage(mContext.getString(R.string.delete_from_shopping_sale_question,
                                mDataset.get(holder.getAdapterPosition()).getProduct().getName()))
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final int itemPosition = holder.getAdapterPosition();
                                final SalesOrderLine salesOrderLine = mDataset.get(holder.getAdapterPosition());

                                String result = mSalesOrderLineDB.deactivateSalesOrderLine(mDataset.get(holder.getAdapterPosition()).getId());
                                if(result == null){
                                    removeItem(holder.getAdapterPosition());
                                    if (mParentLayout!=null) {
                                        Snackbar.make(mParentLayout, R.string.product_removed, Snackbar.LENGTH_LONG)
                                                .setAction(R.string.undo, new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        String result = mSalesOrderLineDB.restoreSalesOrderLine(salesOrderLine.getId());
                                                        if (result == null) {
                                                            addItem(itemPosition, salesOrderLine);
                                                        } else {
                                                            Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }).show();
                                    }
                                } else {
                                    Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });

        if (mDataset.get(position).isQuantityOrderedInvalid()) {
            holder.qtyOrdered.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorQtyOrderedError));
        } else {
            holder.qtyOrdered.setBackgroundColor(ContextCompat.getColor(mContext, R.color.golden_medium));
        }

        holder.qtyOrdered.setText(String.valueOf(mDataset.get(position).getQuantityOrdered()));

        holder.qtyOrdered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Callback) mFragment).updateQtyOrdered(mDataset.get(holder.getAdapterPosition()));
            }
        });

        holder.containerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Product product = (new ProductDB(mContext, mUser))
                        .getProductById(mDataset.get(holder.getAdapterPosition()).getProductId());
                if (product!=null) {
                    mContext.startActivity(new Intent(mContext, ProductDetailActivity.class)
                            .putExtra(ProductDetailActivity.KEY_PRODUCT, product));
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.no_product_details), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public ArrayList<SalesOrderLine> getData() {
        return mDataset;
    }

    public void setData(ArrayList<SalesOrderLine> salesOrderLines) {
        mDataset = salesOrderLines;
        SalesOrderSalesManBR.validateQuantityOrderedInSalesOrderLines(mContext, mUser, mDataset);
        notifyDataSetChanged();
    }

    public SalesOrderLine getItem(int position) {
        try {
            return mDataset.get(position);
        } catch (Exception e) {
            return null;
        }
    }

    public void removeItem(int position) {
        try {
            mDataset.remove(position);
            notifyItemRemoved(position);
            ((Callback) mFragment).reloadShoppingSale(mDataset, false);
        } catch (Exception e) {
            //do nothing
        }
    }

    public void addItem(int position, SalesOrderLine item) {
        mDataset.add(position, item);
        notifyItemInserted(position);
        ((Callback) mFragment).reloadShoppingSale(mDataset, false);
    }
}