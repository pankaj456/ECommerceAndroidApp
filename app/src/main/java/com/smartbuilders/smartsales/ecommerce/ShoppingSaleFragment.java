package com.smartbuilders.smartsales.ecommerce;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.smartbuilders.smartsales.salesforcesystem.DialogUpdateShoppingSaleQtyOrdered;
import com.smartbuilders.smartsales.salesforcesystem.ShoppingSaleFinalizeOptionsActivity;
import com.smartbuilders.smartsales.salesforcesystem.adapters.ShoppingSaleAdapter2;
import com.smartbuilders.synchronizer.ids.model.User;
import com.smartbuilders.smartsales.ecommerce.adapters.ShoppingSaleAdapter;
import com.smartbuilders.smartsales.ecommerce.businessRules.SalesOrderBR;
import com.smartbuilders.smartsales.ecommerce.data.BusinessPartnerDB;
import com.smartbuilders.smartsales.ecommerce.data.CurrencyDB;
import com.smartbuilders.smartsales.ecommerce.data.SalesOrderLineDB;
import com.smartbuilders.smartsales.ecommerce.model.BusinessPartner;
import com.smartbuilders.smartsales.ecommerce.model.Currency;
import com.smartbuilders.smartsales.ecommerce.session.Parameter;
import com.smartbuilders.smartsales.ecommerce.model.SalesOrderLine;
import com.smartbuilders.smartsales.ecommerce.utils.Utils;
import com.smartbuilders.smartsales.ecommerce.view.DatePickerFragment;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class ShoppingSaleFragment extends Fragment implements ShoppingSaleAdapter.Callback,
        ShoppingSaleAdapter2.Callback, DatePickerFragment.Callback, DialogUpdateShoppingSaleQtyOrdered.Callback {

    private static final String STATE_BUSINESS_PARTNER_ID = "STATE_BUSINESS_PARTNER_ID";
    private static final String STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION = "STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION";
    private static final String STATE_VALID_TO = "STATE_VALID_TO";

    private User mUser;
    private boolean mIsInitialLoad;
    private ArrayList<SalesOrderLine> mSalesOrderLines;
    private ShoppingSaleAdapter mShoppingSaleAdapter;
    private ShoppingSaleAdapter2 mShoppingSaleAdapter2;
    private View mBlankScreenView;
    private View mainLayout;
    private TextView mTotalLines;
    private TextView mSubTotalAmount;
    private TextView mTaxAmount;
    private TextView mTotalAmount;
    private int mRecyclerViewCurrentFirstPosition;
    private LinearLayoutManager mLinearLayoutManager;
    private int mCurrentBusinessPartnerId;
    private ProgressDialog waitPlease;
    private EditText mValidToEditText;
    private String mValidToText;
    private TextView mBusinessPartnerName;

    public ShoppingSaleFragment() {
    }

    public interface Callback {
        void reloadShoppingSalesList(User user);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_shopping_sale, container, false);
        mIsInitialLoad = true;

        new Thread() {
            @Override
            public void run() {
                try {
                    if(savedInstanceState!=null) {
                        if(savedInstanceState.containsKey(STATE_BUSINESS_PARTNER_ID)) {
                            mCurrentBusinessPartnerId = savedInstanceState.getInt(STATE_BUSINESS_PARTNER_ID);
                        }
                        if(savedInstanceState.containsKey(STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION)) {
                            mRecyclerViewCurrentFirstPosition = savedInstanceState.getInt(STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION);
                        }
                        if(savedInstanceState.containsKey(STATE_VALID_TO)) {
                            mValidToText = savedInstanceState.getString(STATE_VALID_TO);
                        }
                    }

                    if(getArguments()!=null){
                        if(getArguments().containsKey(ShoppingSaleActivity.KEY_BUSINESS_PARTNER_ID)){
                            mCurrentBusinessPartnerId = getArguments().getInt(ShoppingSaleActivity.KEY_BUSINESS_PARTNER_ID);
                        }
                    }else if(getActivity()!=null && getActivity().getIntent()!=null
                            && getActivity().getIntent().getExtras()!=null){
                        if(getActivity().getIntent().getExtras().containsKey(ShoppingSaleActivity.KEY_BUSINESS_PARTNER_ID)) {
                            mCurrentBusinessPartnerId = getActivity().getIntent().getExtras()
                                    .getInt(ShoppingSaleActivity.KEY_BUSINESS_PARTNER_ID);
                        }
                    }

                    mUser = Utils.getCurrentUser(getContext());

                    if (mCurrentBusinessPartnerId!=0){
                        mSalesOrderLines = (new SalesOrderLineDB(getContext(), mUser))
                                .getShoppingSaleByBusinessPartnerId(mCurrentBusinessPartnerId);
                    } else {
                        mSalesOrderLines = (new SalesOrderLineDB(getContext(), mUser)).getShoppingSale();
                    }

                    if (BuildConfig.IS_SALES_FORCE_SYSTEM) {
                        mShoppingSaleAdapter2 = new ShoppingSaleAdapter2(getContext(), ShoppingSaleFragment.this, mSalesOrderLines, mUser);
                    } else {
                        mShoppingSaleAdapter = new ShoppingSaleAdapter(getContext(), ShoppingSaleFragment.this, mSalesOrderLines, mUser);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (getActivity()!=null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mBusinessPartnerName = (TextView) view.findViewById(R.id.business_partner_commercial_name_tv);

                                mBlankScreenView = view.findViewById(R.id.company_logo_name);
                                mainLayout = view.findViewById(R.id.main_layout);

                                mTotalLines = (TextView) view.findViewById(R.id.total_lines);
                                mSubTotalAmount = (TextView) view.findViewById(R.id.subTotalAmount_tv);
                                mTaxAmount = (TextView) view.findViewById(R.id.taxesAmount_tv);
                                mTotalAmount = (TextView) view.findViewById(R.id.totalAmount_tv);

                                RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.shoppingSale_items_list);
                                // use this setting to improve performance if you know that changes
                                // in content do not change the layout size of the RecyclerView
                                recyclerView.setHasFixedSize(true);
                                mLinearLayoutManager = new LinearLayoutManager(getContext());
                                recyclerView.setLayoutManager(mLinearLayoutManager);
                                if (BuildConfig.IS_SALES_FORCE_SYSTEM) {
                                    recyclerView.setAdapter(mShoppingSaleAdapter2);
                                } else {
                                    recyclerView.setAdapter(mShoppingSaleAdapter);
                                }

                                if (mRecyclerViewCurrentFirstPosition!=0) {
                                    recyclerView.scrollToPosition(mRecyclerViewCurrentFirstPosition);
                                }

                                if (BuildConfig.IS_SALES_FORCE_SYSTEM) {
                                    view.findViewById(R.id.valid_to_layout_container).setVisibility(View.GONE);
                                    view.findViewById(R.id.proceed_to_checkout_shopping_sale_button).setVisibility(View.GONE);
                                    view.findViewById(R.id.go_to_finalize_options_button)
                                            .setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    lockScreen();
                                                    new Thread() {
                                                        @Override
                                                        public void run() {
                                                            String result = null;
                                                            try {
                                                                result = SalesOrderBR.isValidQuantityOrderedInSalesOrderLines(getContext(), mUser, mSalesOrderLines);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                                result = e.getMessage();
                                                            } finally {
                                                                unlockScreen(result);
                                                            }
                                                        }
                                                    }.start();
                                                }
                                            });

                                } else {
                                    mValidToEditText = (EditText) view.findViewById(R.id.valid_to_editText);
                                    mValidToEditText.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Date validTo = null;
                                            try {
                                                validTo = DateFormat.getDateInstance(DateFormat.MEDIUM,
                                                        new Locale("es","VE")).parse(mValidToEditText.getText().toString());
                                            } catch (ParseException e){
                                                // do nothing
                                            }
                                            catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            DialogFragment dialogFragment = DatePickerFragment.getInstance(validTo);
                                            dialogFragment.setTargetFragment(ShoppingSaleFragment.this, 1);
                                            dialogFragment.show(getFragmentManager(), DatePickerFragment.class.getSimpleName());
                                        }
                                    });
                                    mValidToEditText.setText(mValidToText);

                                    view.findViewById(R.id.go_to_finalize_options_button).setVisibility(View.GONE);
                                    view.findViewById(R.id.proceed_to_checkout_shopping_sale_button)
                                            .setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    new AlertDialog.Builder(getContext())
                                                            .setMessage(R.string.proceed_to_checkout_quoatation_question)
                                                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    Date validTo = null;
                                                                    try {
                                                                        validTo = DateFormat.getDateInstance(DateFormat.MEDIUM,
                                                                                new Locale("es", "VE")).parse(mValidToEditText.getText().toString());
                                                                    } catch (ParseException e) {
                                                                        // do nothing
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    closeSalesOrder(validTo);
                                                                }
                                                            })
                                                            .setNegativeButton(R.string.no, null)
                                                            .show();
                                                }
                                            });
                                }

                                fillFields();
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                view.findViewById(R.id.progressContainer).setVisibility(View.GONE);
                                if (mSalesOrderLines==null || mSalesOrderLines.isEmpty()) {
                                    mBlankScreenView.setVisibility(View.VISIBLE);
                                } else {
                                    mainLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    });
                }
            }
        }.start();
        return view;
    }

    @Override
    public void onStart() {
        if(mIsInitialLoad){
            mIsInitialLoad = false;
        }else{
            try {
                if (mCurrentBusinessPartnerId!=0){
                    mSalesOrderLines = (new SalesOrderLineDB(getContext(), mUser))
                            .getShoppingSaleByBusinessPartnerId(mCurrentBusinessPartnerId);
                } else {
                    mSalesOrderLines = (new SalesOrderLineDB(getContext(), mUser)).getShoppingSale();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            reloadShoppingSale();
            reloadShoppingSalesList(mUser);
        }
        super.onStart();
    }

    @Override
    public void setDate(String date) {
        mValidToText = date;
        mValidToEditText.setText(date);
    }

    private void closeSalesOrder(final Date validTo){
        lockScreen();
        new Thread() {
            @Override
            public void run() {
                String result = null;
                try {
                    result = SalesOrderBR.createSalesOrderFromShoppingSale(getContext(), mUser, validTo, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    result = e.getMessage();
                } finally {
                    unlockScreen(result);
                }
            }
        }.start();
    }

    private void lockScreen() {
        if (getActivity()!=null) {
            //Se bloquea la rotacion de la pantalla para evitar que se mate a la aplicacion
            Utils.lockScreenOrientation(getActivity());
            if (waitPlease==null || !waitPlease.isShowing()){
                waitPlease = ProgressDialog.show(getContext(), null,
                        getString(R.string.closing_sales_order_wait_please), true, false);
            }
        }
    }

    private void unlockScreen(final String message){
        if(getActivity()!=null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(message!=null){
                        new AlertDialog.Builder(getContext())
                                .setMessage(message)
                                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Utils.unlockScreenOrientation(getActivity());
                                    }
                                })
                                .setCancelable(false)
                                .show();
                        if (waitPlease!=null && waitPlease.isShowing()) {
                            waitPlease.cancel();
                            waitPlease = null;
                        }
                    } else {
                        if (BuildConfig.IS_SALES_FORCE_SYSTEM) {
                            startActivity(new Intent(getContext(), ShoppingSaleFinalizeOptionsActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                        } else {
                            startActivity(new Intent(getContext(), SalesOrdersListActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                        }
                        if (waitPlease!=null && waitPlease.isShowing()) {
                            waitPlease.cancel();
                            waitPlease = null;
                        }
                        if (getActivity()!=null) {
                            getActivity().finish();
                        }
                    }
                }
            });
        }
    }

    public void fillFields(){
        BusinessPartner businessPartner = null;
        if(mCurrentBusinessPartnerId!=0) {
            businessPartner = (new BusinessPartnerDB(getContext(), mUser))
                    .getActiveBusinessPartnerById(mCurrentBusinessPartnerId);
        } else {
            try {
                businessPartner = (new BusinessPartnerDB(getContext(), mUser))
                        .getActiveBusinessPartnerById(Utils.getAppCurrentBusinessPartnerId(getContext(), mUser));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mBusinessPartnerName.setText(getString(R.string.business_partner_name_detail,
                businessPartner!=null ? businessPartner.getName() : null));

        if (mSalesOrderLines!=null && !mSalesOrderLines.isEmpty()) {
            mTotalLines.setText(getString(R.string.order_lines_number,
                    String.valueOf(mSalesOrderLines.size())));
            Currency currency = (new CurrencyDB(getContext(), mUser))
                    .getActiveCurrencyById(Parameter.getDefaultCurrencyId(getContext(), mUser));
            mSubTotalAmount.setText(getString(R.string.sales_order_sub_total_amount,
                    currency!=null ? currency.getName() : "",
                    SalesOrderBR.getSubTotalAmountStringFormat(mSalesOrderLines)));
            mTaxAmount.setText(getString(R.string.sales_order_tax_amount,
                    currency!=null ? currency.getName() : "",
                    SalesOrderBR.getTaxAmountStringFormat(mSalesOrderLines)));
            mTotalAmount.setText(getString(R.string.sales_order_total_amount,
                    currency!=null ? currency.getName() : "",
                    SalesOrderBR.getTotalAmountStringFormat(mSalesOrderLines)));
        }
    }

    @Override
    public void updateSalesOrderLine(SalesOrderLine orderLine, int focus) {
        DialogUpdateSalesOrderLine dialogUpdateSalesOrderLine =
                DialogUpdateSalesOrderLine.newInstance(orderLine, mUser, focus);
        dialogUpdateSalesOrderLine.setTargetFragment(this, 0);
        dialogUpdateSalesOrderLine.show(getActivity().getSupportFragmentManager(),
                DialogUpdateSalesOrderLine.class.getSimpleName());
    }

    @Override
    public void reloadShoppingSalesList(User user) {
        //manda a refrescar la lista de la izquierda cuando se esta en pantalla dividida
        ((Callback) getActivity()).reloadShoppingSalesList(user);
    }

    @Override
    public void updateQtyOrdered(SalesOrderLine salesOrderLine) {
        DialogUpdateShoppingSaleQtyOrdered dialogUpdateShoppingSaleQtyOrdered =
                DialogUpdateShoppingSaleQtyOrdered.newInstance(salesOrderLine, mUser);
        dialogUpdateShoppingSaleQtyOrdered.setTargetFragment(this, 0);
        dialogUpdateShoppingSaleQtyOrdered.show(getActivity().getSupportFragmentManager(),
                DialogUpdateShoppingSaleQtyOrdered.class.getSimpleName());
    }

    @Override
    public void reloadShoppingSale(){
        if (mCurrentBusinessPartnerId!=0){
            mSalesOrderLines = (new SalesOrderLineDB(getContext(), mUser))
                    .getShoppingSaleByBusinessPartnerId(mCurrentBusinessPartnerId);
        } else {
            mSalesOrderLines = (new SalesOrderLineDB(getContext(), mUser)).getShoppingSale();
        }
        if (BuildConfig.IS_SALES_FORCE_SYSTEM) {
            mShoppingSaleAdapter2.setData(mSalesOrderLines);
        } else {
            mShoppingSaleAdapter.setData(mSalesOrderLines);
        }

        if (mSalesOrderLines==null || mSalesOrderLines.isEmpty()) {
            mBlankScreenView.setVisibility(View.VISIBLE);
            mainLayout.setVisibility(View.GONE);
        }else{
            mainLayout.setVisibility(View.VISIBLE);
            mBlankScreenView.setVisibility(View.GONE);
            fillFields();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_BUSINESS_PARTNER_ID, mCurrentBusinessPartnerId);
        outState.putString(STATE_VALID_TO, mValidToText);
        try {
            if (mLinearLayoutManager instanceof GridLayoutManager) {
                outState.putInt(STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION,
                        mLinearLayoutManager.findFirstVisibleItemPosition());
            } else {
                outState.putInt(STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION,
                        mLinearLayoutManager.findFirstCompletelyVisibleItemPosition());
            }
        } catch (Exception e) {
            outState.putInt(STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION, mRecyclerViewCurrentFirstPosition);
        }
        super.onSaveInstanceState(outState);
    }
}
