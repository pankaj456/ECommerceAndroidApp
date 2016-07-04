package com.smartbuilders.smartsales.ecommerceandroidapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.jasgcorp.ids.model.User;
import com.smartbuilders.smartsales.ecommerceandroidapp.adapters.ProductsListAdapter;
import com.smartbuilders.smartsales.ecommerceandroidapp.data.ProductDB;
import com.smartbuilders.smartsales.ecommerceandroidapp.model.Product;
import com.smartbuilders.smartsales.ecommerceandroidapp.utils.Utils;
import com.smartbuilders.smartsales.ecommerceandroidapp.febeca.R;

import java.util.ArrayList;

/**
 * Jesus Sarco
 */
public class ProductsListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DialogSortProductListOptions.Callback {

    public static final String KEY_PRODUCT_CATEGORY_ID = "KEY_PRODUCT_CATEGORY_ID";
    public static final String KEY_PRODUCT_SUBCATEGORY_ID = "KEY_PRODUCT_SUBCATEGORY_ID";
    public static final String KEY_PRODUCT_BRAND_ID = "KEY_PRODUCT_BRAND_ID";
    public static final String KEY_PRODUCT_NAME = "KEY_PRODUCT_NAME";
    public static final String KEY_SEARCH_PATTERN = "KEY_SEARCH_PATTERN";
    private static final String STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION = "STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION";
    private static final String STATE_CURRENT_PRODUCTS_LIST_ADAPTER_MASK = "STATE_CURRENT_PRODUCTS_LIST_ADAPTER_MASK";
    private static final String STATE_CURRENT_SORT_OPTION = "STATE_CURRENT_SORT_OPTION";

    private int productCategoryId;
    private int productSubCategoryId;
    private int productBrandId;
    private String productName;
    private String mSearchPattern;
    private User mUser;
    private final ArrayList<Product> products = new ArrayList<>();
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView mRecyclerView;
    private int mRecyclerViewCurrentFirstPosition;
    private int mCurrentProductsListAdapterMask = ProductsListAdapter.MASK_PRODUCT_DETAILS;
    private int mCurrentSortOption;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_list);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mUser = Utils.getCurrentUser(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Utils.setCustomToolbarTitle(ProductsListActivity.this, toolbar, true);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(ProductsListActivity.this,
                drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(ProductsListActivity.this);
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.user_name))
                .setText(getString(R.string.welcome_user, mUser.getUserName()));

        new Thread() {
            @Override
            public void run() {
                try {
                    if(savedInstanceState != null) {
                        if(savedInstanceState.containsKey(STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION)){
                            mRecyclerViewCurrentFirstPosition = savedInstanceState.getInt(STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION);
                        }
                        if(savedInstanceState.containsKey(STATE_CURRENT_PRODUCTS_LIST_ADAPTER_MASK)){
                            mCurrentProductsListAdapterMask = savedInstanceState.getInt(STATE_CURRENT_PRODUCTS_LIST_ADAPTER_MASK);
                        }
                        if(savedInstanceState.containsKey(STATE_CURRENT_SORT_OPTION)){
                            mCurrentSortOption = savedInstanceState.getInt(STATE_CURRENT_SORT_OPTION);
                        }
                    }

                    if(getIntent()!=null && getIntent().getExtras()!=null) {
                        if(getIntent().getExtras().containsKey(ProductsListActivity.KEY_PRODUCT_CATEGORY_ID)){
                            productCategoryId = getIntent().getExtras().getInt(ProductsListActivity.KEY_PRODUCT_CATEGORY_ID);
                        }
                        if(getIntent().getExtras().containsKey(ProductsListActivity.KEY_PRODUCT_SUBCATEGORY_ID)){
                            productSubCategoryId = getIntent().getExtras().getInt(ProductsListActivity.KEY_PRODUCT_SUBCATEGORY_ID);
                        }
                        if(getIntent().getExtras().containsKey(ProductsListActivity.KEY_PRODUCT_BRAND_ID)){
                            productBrandId = getIntent().getExtras().getInt(ProductsListActivity.KEY_PRODUCT_BRAND_ID);
                        }
                        if(getIntent().getExtras().containsKey(ProductsListActivity.KEY_PRODUCT_NAME)){
                            productName = getIntent().getExtras().getString(ProductsListActivity.KEY_PRODUCT_NAME);
                        }
                        if(getIntent().getExtras().containsKey(ProductsListActivity.KEY_SEARCH_PATTERN)){
                            mSearchPattern = getIntent().getExtras().getString(ProductsListActivity.KEY_SEARCH_PATTERN);
                        }
                    }

                    if (productCategoryId != 0) {
                        products.addAll(new ProductDB(ProductsListActivity.this, mUser).getProductsByCategoryId(productCategoryId));
                    } else if (productSubCategoryId != 0) {
                        products.addAll(new ProductDB(ProductsListActivity.this, mUser).getProductsBySubCategoryId(productSubCategoryId, mSearchPattern));
                    } else if (productBrandId != 0) {
                        products.addAll(new ProductDB(ProductsListActivity.this, mUser).getProductsByBrandId(productBrandId));
                    } else if (productName != null) {
                        products.addAll(new ProductDB(ProductsListActivity.this, mUser).getProductsByName(productName));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mRecyclerView = (RecyclerView) findViewById(R.id.product_list_result);

                            final ImageButton changeLayoutImageButton = (ImageButton) findViewById(R.id.change_layout_button);
                            if(changeLayoutImageButton!=null){
                                if(mCurrentProductsListAdapterMask==ProductsListAdapter.MASK_PRODUCT_LARGE_DETAILS){
                                    changeLayoutImageButton.setImageResource(R.drawable.ic_view_module_black_24dp);
                                }else{
                                    changeLayoutImageButton.setImageResource(R.drawable.ic_view_headline_black_24dp);
                                }

                                changeLayoutImageButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mRecyclerViewCurrentFirstPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
                                        if(mCurrentProductsListAdapterMask==ProductsListAdapter.MASK_PRODUCT_LARGE_DETAILS){
                                            mCurrentProductsListAdapterMask = ProductsListAdapter.MASK_PRODUCT_DETAILS;
                                            if (useGridView()) {
                                                if(!(mLinearLayoutManager instanceof GridLayoutManager)){
                                                    mLinearLayoutManager = new GridLayoutManager(ProductsListActivity.this, getSpanCount());
                                                }
                                            }else{
                                                if(mLinearLayoutManager instanceof GridLayoutManager){
                                                    mLinearLayoutManager = new LinearLayoutManager(ProductsListActivity.this);
                                                }
                                            }
                                            changeLayoutImageButton.setImageResource(R.drawable.ic_view_headline_black_24dp);
                                        }else{
                                            mCurrentProductsListAdapterMask = ProductsListAdapter.MASK_PRODUCT_LARGE_DETAILS;
                                            if(mLinearLayoutManager instanceof GridLayoutManager){
                                                mLinearLayoutManager = new LinearLayoutManager(ProductsListActivity.this);
                                            }
                                            changeLayoutImageButton.setImageResource(R.drawable.ic_view_module_black_24dp);
                                        }
                                        mRecyclerView.setLayoutManager(mLinearLayoutManager);
                                        mRecyclerView.setAdapter(new ProductsListAdapter(ProductsListActivity.this,
                                                ProductsListActivity.this, products, mCurrentProductsListAdapterMask, mCurrentSortOption, mUser));
                                        mRecyclerView.scrollToPosition(mRecyclerViewCurrentFirstPosition);
                                    }
                                });
                            }

                            final ImageButton sortProductsListImageButton = (ImageButton) findViewById(R.id.sort_products_list_button);
                            if(sortProductsListImageButton!=null){
                                sortProductsListImageButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        DialogSortProductListOptions dialogSortProductListOptions =
                                                DialogSortProductListOptions.newInstance(mUser, mCurrentSortOption);
                                        dialogSortProductListOptions.show(ProductsListActivity.this.getSupportFragmentManager(),
                                                DialogUpdateShoppingCartQtyOrdered.class.getSimpleName());
                                    }
                                });
                            }

                            TextView categorySubcategoryResultsTextView = (TextView) findViewById(R.id.category_subcategory_results);
                            if(categorySubcategoryResultsTextView!=null){
                                if(!products.isEmpty()) {
                                    if (productCategoryId != 0) {
                                        Spannable word = new SpannableString(products.get(0).getProductCategory().getDescription() + " ");
                                        word.setSpan(new ForegroundColorSpan(Utils.getColor(ProductsListActivity.this, R.color.product_category)), 0,
                                                word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        categorySubcategoryResultsTextView.setText(word);
                                        categorySubcategoryResultsTextView.append(new SpannableString(" ("+products.size()+" Resultados) "));
                                    } else if (productSubCategoryId != 0) {
                                        Spannable word = new SpannableString(products.get(0).getProductCategory().getDescription() + " >> ");
                                        word.setSpan(new ForegroundColorSpan(Utils.getColor(ProductsListActivity.this, R.color.product_category)), 0,
                                                word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        categorySubcategoryResultsTextView.setText(word);
                                        Spannable wordTwo = new SpannableString(" "+products.get(0).getProductSubCategory().getDescription()+" ");
                                        wordTwo.setSpan(new ForegroundColorSpan(Utils.getColor(ProductsListActivity.this, R.color.product_subcategory)),
                                                0, wordTwo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        categorySubcategoryResultsTextView.append(wordTwo);
                                        categorySubcategoryResultsTextView.append(new SpannableString(" ("+products.size()+" Resultados) "));
                                    } else if (productBrandId != 0) {
                                        Spannable word = new SpannableString(products.get(0).getProductBrand().getDescription() + " ");
                                        word.setSpan(new ForegroundColorSpan(Utils.getColor(ProductsListActivity.this, R.color.product_category)), 0,
                                                word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        categorySubcategoryResultsTextView.setText(word);
                                        categorySubcategoryResultsTextView.append(new SpannableString("("+products.size()+" Resultados) "));
                                    } else if (productName != null) {
                                        Spannable word = new SpannableString("Búsqueda: \""+productName+"\" ");
                                        word.setSpan(new ForegroundColorSpan(Utils.getColor(ProductsListActivity.this, R.color.product_category)), 0,
                                                word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        categorySubcategoryResultsTextView.setText(word);
                                        categorySubcategoryResultsTextView.append(new SpannableString("("+products.size()+" Resultados) "));
                                    }
                                } else {
                                    Spannable word = new SpannableString(getString(R.string.no_products_to_show));
                                    word.setSpan(new ForegroundColorSpan(Utils.getColor(ProductsListActivity.this, R.color.black)), 0,
                                            word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    categorySubcategoryResultsTextView.append(word);
                                }
                            }

                            // use this setting to improve performance if you know that changes
                            // in content do not change the layout size of the RecyclerView
                            mRecyclerView.setHasFixedSize(true);
                            if (mCurrentProductsListAdapterMask!=ProductsListAdapter.MASK_PRODUCT_LARGE_DETAILS
                                    && useGridView()) {
                                mLinearLayoutManager = new GridLayoutManager(ProductsListActivity.this, getSpanCount());
                            }else{
                                mLinearLayoutManager = new LinearLayoutManager(ProductsListActivity.this);
                            }
                            mRecyclerView.setLayoutManager(mLinearLayoutManager);
                            mRecyclerView.setAdapter(new ProductsListAdapter(ProductsListActivity.this,
                                    ProductsListActivity.this, products, mCurrentProductsListAdapterMask, mCurrentSortOption, mUser));

                            if (mRecyclerViewCurrentFirstPosition!=0) {
                                mRecyclerView.scrollToPosition(mRecyclerViewCurrentFirstPosition);
                            }

                            if(findViewById(R.id.filter_bar_linear_layout)!=null){
                                final Spinner filterByOptionsSpinner = (Spinner) findViewById(R.id.filter_by_options_spinner);
                                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                                        ProductsListActivity.this, R.array.filter_by_options, R.layout.search_by_option_prompt_item);
                                if(filterByOptionsSpinner!=null && adapter!=null){
                                    adapter.setDropDownViewResource(R.layout.search_by_option_item);
                                    filterByOptionsSpinner.setAdapter(adapter);
                                    filterByOptionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                            //String selectedOption = (String) parent.getItemAtPosition(position);
                                            //if(selectedOption!=null){
                                            //    if(selectedOption.equals(getString(R.string.categories))){
                                            //        startActivity(new Intent(ProductsListActivity.this, CategoriesListActivity.class));
                                            //    }else if(selectedOption.equals(getString(R.string.brands))){
                                            //        startActivity(new Intent(ProductsListActivity.this, BrandsListActivity.class));
                                            //    }
                                            //}
                                            //filterByOptionsSpinner.setSelection(0);
                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> parent) { }
                                    });
                                }

                                final EditText filterProduct = (EditText) findViewById(R.id.filter_product_editText);
                                if(filterProduct!=null) {
                                    filterProduct.setFocusable(true);
                                    filterProduct.setFocusableInTouchMode(true);
                                    filterProduct.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                        }

                                        @Override
                                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                                            ((ProductsListAdapter) mRecyclerView.getAdapter()).filter(s.toString(), 0);
                                        }

                                        @Override
                                        public void afterTextChanged(Editable s) {

                                        }
                                    });
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            findViewById(R.id.product_list_result).setVisibility(View.VISIBLE);
                            findViewById(R.id.progressContainer).setVisibility(View.GONE);
                        }
                    }
                });
            }
        }.start();
    }

    private boolean useGridView(){
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            return true;
        }else {
            switch(getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
                case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                    switch(getResources().getDisplayMetrics().densityDpi) {
                        case DisplayMetrics.DENSITY_LOW:
                            break;
                        case DisplayMetrics.DENSITY_MEDIUM:
                        case DisplayMetrics.DENSITY_HIGH:
                        case DisplayMetrics.DENSITY_XHIGH:
                            return  true;
                    }
                    break;
                //case Configuration.SCREENLAYOUT_SIZE_LARGE:
                //    break;
                //case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                //    break;
                //case Configuration.SCREENLAYOUT_SIZE_SMALL:
                //    break;
            }
        }
        return false;
    }

    private int getSpanCount() {
        switch(getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                switch(getResources().getDisplayMetrics().densityDpi) {
                    case DisplayMetrics.DENSITY_LOW:
                        break;
                    case DisplayMetrics.DENSITY_MEDIUM:
                    case DisplayMetrics.DENSITY_HIGH:
                    case DisplayMetrics.DENSITY_XHIGH:
                        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                            return 3;
                        }
                        break;
                }
                break;
            //case Configuration.SCREENLAYOUT_SIZE_LARGE:
            //    break;
            //case Configuration.SCREENLAYOUT_SIZE_NORMAL:
            //    break;
            //case Configuration.SCREENLAYOUT_SIZE_SMALL:
            //    break;
        }
        return 2;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Utils.navigationItemSelectedBehave(item.getItemId(), this);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer!=null){
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_products_list, menu);
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
        if (drawer!=null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CURRENT_PRODUCTS_LIST_ADAPTER_MASK, mCurrentProductsListAdapterMask);
        try {
            if (mLinearLayoutManager instanceof GridLayoutManager
                    || mCurrentProductsListAdapterMask==ProductsListAdapter.MASK_PRODUCT_LARGE_DETAILS) {
                outState.putInt(STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION,
                        mLinearLayoutManager.findFirstVisibleItemPosition());
            } else {
                outState.putInt(STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION,
                        mLinearLayoutManager.findFirstCompletelyVisibleItemPosition());
            }
        } catch (Exception e) {
            outState.putInt(STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION, mRecyclerViewCurrentFirstPosition);
        }
        outState.putInt(STATE_CURRENT_SORT_OPTION, mCurrentSortOption);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void sortProductsList(int sortOption) {
        if(mRecyclerView!=null && mRecyclerView.getAdapter() instanceof ProductsListAdapter){
            mCurrentSortOption = sortOption;
            mRecyclerView.setAdapter(new ProductsListAdapter(ProductsListActivity.this,
                    ProductsListActivity.this, products, mCurrentProductsListAdapterMask, mCurrentSortOption, mUser));
        }
    }
}