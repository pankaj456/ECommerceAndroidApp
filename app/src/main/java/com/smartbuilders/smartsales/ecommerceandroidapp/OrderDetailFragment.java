package com.smartbuilders.smartsales.ecommerceandroidapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jasgcorp.ids.model.User;
import com.smartbuilders.smartsales.ecommerceandroidapp.adapters.OrderLineAdapter;
import com.smartbuilders.smartsales.ecommerceandroidapp.data.OrderLineDB;
import com.smartbuilders.smartsales.ecommerceandroidapp.model.Order;
import com.smartbuilders.smartsales.ecommerceandroidapp.model.OrderLine;
import com.smartbuilders.smartsales.ecommerceandroidapp.providers.CachedFileProvider;
import com.smartbuilders.smartsales.ecommerceandroidapp.utils.OrderDetailPDFCreator;
import com.smartbuilders.smartsales.ecommerceandroidapp.febeca.R;
import com.smartbuilders.smartsales.ecommerceandroidapp.utils.Utils;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class OrderDetailFragment extends Fragment {

    private static final String STATE_RECYCLERVIEW_CURRENT_FIRST_POSITION = "STATE_LISTVIEW_CURRENT_FIRST_POSITION";

    private User mCurrentUser;
    private Order mOrder;
    private LinearLayoutManager mLinearLayoutManager;
    private int mRecyclerViewCurrentFirstPosition;
    private ShareActionProvider mShareActionProvider;
    private ArrayList<OrderLine> mOrderLines;

    public OrderDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState!=null) {
            if (savedInstanceState.containsKey(STATE_RECYCLERVIEW_CURRENT_FIRST_POSITION)) {
                mRecyclerViewCurrentFirstPosition = savedInstanceState.getInt(STATE_RECYCLERVIEW_CURRENT_FIRST_POSITION);
            }
        }

        View rootView = inflater.inflate(R.layout.fragment_order_detail, container, false);

        setHasOptionsMenu(true);

        mCurrentUser = Utils.getCurrentUser(getContext());

        if (getArguments() != null) {
            if (getArguments().containsKey(OrderDetailActivity.KEY_ORDER)) {
                mOrder = getArguments().getParcelable(OrderDetailActivity.KEY_ORDER);
            }
        } else if (getActivity().getIntent() != null && getActivity().getIntent().getExtras() != null) {
            if (getActivity().getIntent().getExtras().containsKey(OrderDetailActivity.KEY_ORDER)) {
                mOrder = getActivity().getIntent().getExtras().getParcelable(OrderDetailActivity.KEY_ORDER);
            }
        }

        if (mOrder != null) {
            mOrderLines = new OrderLineDB(getContext(), mCurrentUser)
                    .getActiveFinalizedOrderLinesByOrderId(mOrder.getId());

            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.order_lines);
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            recyclerView.setHasFixedSize(true);
            mLinearLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(mLinearLayoutManager);
            recyclerView.setAdapter(new OrderLineAdapter(mOrderLines, mCurrentUser));

            if (mRecyclerViewCurrentFirstPosition!=0) {
                recyclerView.scrollToPosition(mRecyclerViewCurrentFirstPosition);
            }

            ((TextView) rootView.findViewById(R.id.order_lines_number_tv))
                    .setText(getContext().getString(R.string.order_lines_number, String.valueOf(mOrderLines.size())));

            ((TextView) rootView.findViewById(R.id.order_number_tv))
                    .setText(getContext().getString(R.string.order_number, mOrder.getOrderNumber()));

            ((TextView) rootView.findViewById(R.id.order_date_tv))
                    .setText(getContext().getString(R.string.order_date, mOrder.getCreatedStringFormat()));
        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_order_detail_fragment, menu);

        // Retrieve the share menu item
        MenuItem item = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        // Attach an intent to this ShareActionProvider. You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mOrderLines != null && mOrderLines.size() > 0) {
            new CreateShareIntentThread().start();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            if (mOrderLines != null && mOrderLines.size() > 0) {
                mShareActionProvider.setShareIntent(createShareOrderIntent());
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Intent createShareOrderIntent(){
        String fileName = "Pedido";
        String subject = "";
        String message = "";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        // need this to prompts email client only
        shareIntent.setType("message/rfc822");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);

        try{
            new OrderDetailPDFCreator().generatePDF(mOrderLines, fileName+".pdf", getContext(), mCurrentUser);
        }catch(Exception e){
            e.printStackTrace();
        }

        //Add the attachment by specifying a reference to our custom ContentProvider
        //and the specific file of interest
        shareIntent.putExtra(Intent.EXTRA_STREAM,  Uri.parse("content://"
                + CachedFileProvider.AUTHORITY + "/" + fileName + ".pdf"));
        return shareIntent;
    }

    class CreateShareIntentThread extends Thread {
        public void run() {
            final Intent shareIntent = createShareOrderIntent();
            try {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mShareActionProvider.setShareIntent(shareIntent);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mLinearLayoutManager!=null) {
            if (mLinearLayoutManager instanceof GridLayoutManager) {
                outState.putInt(STATE_RECYCLERVIEW_CURRENT_FIRST_POSITION,
                        mLinearLayoutManager.findFirstVisibleItemPosition());
            } else {
                outState.putInt(STATE_RECYCLERVIEW_CURRENT_FIRST_POSITION,
                        mLinearLayoutManager.findFirstCompletelyVisibleItemPosition());
            }
        } else {
            outState.putInt(STATE_RECYCLERVIEW_CURRENT_FIRST_POSITION, mRecyclerViewCurrentFirstPosition);
        }
        super.onSaveInstanceState(outState);
    }
}
