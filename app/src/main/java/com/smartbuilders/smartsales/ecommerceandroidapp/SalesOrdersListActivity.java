package com.smartbuilders.smartsales.ecommerceandroidapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.jasgcorp.ids.model.User;
import com.smartbuilders.smartsales.ecommerceandroidapp.model.SalesOrder;
import com.smartbuilders.smartsales.ecommerceandroidapp.utils.Utils;
import com.smartbuilders.smartsales.ecommerceandroidapp.febeca.R;

/**
 * Jesus Sarco, 12.05.2016
 */
public class SalesOrdersListActivity extends AppCompatActivity
        implements SalesOrdersListFragment.Callback, NavigationView.OnNavigationItemSelectedListener {

    private static final String SALES_ORDERDETAIL_FRAGMENT_TAG = "SALES_ORDERDETAIL_FRAGMENT_TAG";

    private static final String STATE_CURRENT_SELECTED_ITEM_POSITION = "STATE_CURRENT_SELECTED_ITEM_POSITION";

    private boolean mTwoPane;
    private User mCurrentUser;
    private int mCurrentSelectedItemPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_orders_list);

        if( savedInstanceState != null ) {
            if(savedInstanceState.containsKey(STATE_CURRENT_SELECTED_ITEM_POSITION)){
                mCurrentSelectedItemPosition = savedInstanceState.getInt(STATE_CURRENT_SELECTED_ITEM_POSITION);
            }
        }

        mCurrentUser = Utils.getCurrentUser(this);

        if(findViewById(R.id.title_textView) != null){
            ((TextView) findViewById(R.id.title_textView))
                    .setTypeface(Typeface.createFromAsset(getAssets(), "MyriadPro-Bold.otf"));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Utils.setCustomToolbarTitle(this, toolbar, true);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.user_name))
                .setText(getString(R.string.welcome_user, mCurrentUser.getUserName()));

        mTwoPane = findViewById(R.id.sales_order_detail_container) != null;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mTwoPane) {
            ListView lv = (ListView) findViewById(R.id.sales_orders_list);
            if (lv != null && lv.getAdapter()!=null
                    && lv.getAdapter().getCount() > mCurrentSelectedItemPosition
                    && mCurrentSelectedItemPosition != ListView.INVALID_POSITION) {
                lv.performItemClick(lv.getAdapter().getView(mCurrentSelectedItemPosition, null, null),
                        mCurrentSelectedItemPosition, mCurrentSelectedItemPosition);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ListView lv = (ListView) findViewById(R.id.sales_orders_list);
        if(lv != null && (lv.getAdapter()==null || lv.getAdapter().getCount()==0)){
            findViewById(R.id.company_logo_name).setVisibility(View.VISIBLE);
            if(mTwoPane){
                findViewById(R.id.fragment_sales_order_list).setVisibility(View.GONE);
                findViewById(R.id.sales_order_detail_container).setVisibility(View.GONE);
            }else{
                findViewById(R.id.sales_orders_list).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sales_orders_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                startActivity(new Intent(this, SearchResultsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Utils.navigationItemSelectedBehave(item.getItemId(), this);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onItemSelected(SalesOrder salesOrder, int selectedItemPosition) {
        mCurrentSelectedItemPosition = selectedItemPosition;
        if(mTwoPane){
            Bundle args = new Bundle();
            args.putParcelable(SalesOrderDetailActivity.KEY_SALES_ORDER, salesOrder);

            SalesOrderDetailFragment fragment = new SalesOrderDetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.sales_order_detail_container, fragment, SALES_ORDERDETAIL_FRAGMENT_TAG)
                    .commit();
        }else{
            Intent intent = new Intent(SalesOrdersListActivity.this, SalesOrderDetailActivity.class);
            intent.putExtra(SalesOrderDetailActivity.KEY_SALES_ORDER, salesOrder);
            startActivity(intent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CURRENT_SELECTED_ITEM_POSITION, mCurrentSelectedItemPosition);
        super.onSaveInstanceState(outState);
    }
}