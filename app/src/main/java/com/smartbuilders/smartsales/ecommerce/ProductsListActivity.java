package com.smartbuilders.smartsales.ecommerce;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartbuilders.smartsales.ecommerce.data.MainPageProductSectionDB;
import com.smartbuilders.smartsales.ecommerce.data.ProductCategoryDB;
import com.smartbuilders.smartsales.ecommerce.data.ProductRecentlySeenDB;
import com.smartbuilders.smartsales.ecommerce.data.ProductSubCategoryDB;
import com.smartbuilders.smartsales.ecommerce.model.MainPageProductSection;
import com.smartbuilders.smartsales.ecommerce.model.ProductCategory;
import com.smartbuilders.smartsales.ecommerce.model.ProductSubCategory;
import com.smartbuilders.synchronizer.ids.model.User;
import com.smartbuilders.smartsales.ecommerce.adapters.ProductsListAdapter;
import com.smartbuilders.smartsales.ecommerce.data.ProductDB;
import com.smartbuilders.smartsales.ecommerce.model.Product;
import com.smartbuilders.smartsales.ecommerce.utils.Utils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Jesus Sarco
 */
public class ProductsListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DialogSortProductListOptions.Callback {

    public static final String KEY_PRODUCT_CATEGORY_ID = "KEY_PRODUCT_CATEGORY_ID";
    public static final String KEY_PRODUCT_SUBCATEGORY_ID = "KEY_PRODUCT_SUBCATEGORY_ID";
    public static final String KEY_PRODUCT_BRAND_ID = "KEY_PRODUCT_BRAND_ID";
    public static final String KEY_PRODUCT_NAME = "KEY_PRODUCT_NAME";
    public static final String KEY_PRODUCT_REFERENCE = "KEY_PRODUCT_REFERENCE";
    public static final String KEY_PRODUCT_PURPOSE = "KEY_PRODUCT_PURPOSE";
    public static final String KEY_PRODUCT_ID_SHOW_RELATED_SHOPPING_PRODUCTS = "KEY_PRODUCT_ID_SHOW_RELATED_SHOPPING_PRODUCTS";
    public static final String KEY_SHOW_PRODUCTS_RECENTLY_SEEN = "KEY_SHOW_PRODUCTS_RECENTLY_SEEN";
    public static final String KEY_MAIN_PAGE_PRODUCT_SECTION_ID = "KEY_MAIN_PAGE_PRODUCT_SECTION_ID";

    private static final String STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION = "STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION";
    private static final String STATE_CURRENT_PRODUCTS_LIST_ADAPTER_MASK = "STATE_CURRENT_PRODUCTS_LIST_ADAPTER_MASK";
    private static final String STATE_CURRENT_SORT_OPTION = "STATE_CURRENT_SORT_OPTION";
    private static final String STATE_CURRENT_FILTER_TEXT = "STATE_CURRENT_FILTER_TEXT";
    private static final String STATE_SPINNER_SELECTED_ITEM_POSITION = "STATE_SPINNER_SELECTED_ITEM_POSITION";
    private static final String STATE_PRODUCT_ID_SHOW_RELATED_SHOPPING_PRODUCTS = "STATE_PRODUCT_ID_SHOW_RELATED_SHOPPING_PRODUCTS";
    private static final String STATE_SHOW_PRODUCTS_RECENTLY_SEEN = "STATE_SHOW_PRODUCTS_RECENTLY_SEEN";
    private static final String STATE_MAIN_PAGE_PRODUCT_SECTION_ID = "STATE_MAIN_PAGE_PRODUCT_SECTION_ID";

    private int productCategoryId;
    private int productSubCategoryId;
    private int productBrandId;
    private String productName;
    private String productReference;
    private String productPurpose;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private int mRecyclerViewCurrentFirstPosition;
    private int mCurrentProductsListAdapterMask = ProductsListAdapter.MASK_PRODUCT_DETAILS;
    private int mCurrentSortOption;
    private String mCurrentFilterText;
    private int mSpinnerSelectedItemPosition;
    private int mProductIdShowRelatedShoppingProducts;
    private boolean mShowProductsRecentlySeen;
    private int mMainPageProductSectionId;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_list);

        final User user = Utils.getCurrentUser(this);
        final ArrayList<Product> products = new ArrayList<>();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Utils.setCustomToolbarTitle(this, toolbar, true);
        setSupportActionBar(toolbar);

        Utils.inflateNavigationView(this, this, toolbar, user);

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
                        if(savedInstanceState.containsKey(STATE_CURRENT_FILTER_TEXT)){
                            mCurrentFilterText = savedInstanceState.getString(STATE_CURRENT_FILTER_TEXT);
                        }
                        if(savedInstanceState.containsKey(STATE_SPINNER_SELECTED_ITEM_POSITION)){
                            mSpinnerSelectedItemPosition = savedInstanceState.getInt(STATE_SPINNER_SELECTED_ITEM_POSITION);
                        }
                        if(savedInstanceState.containsKey(STATE_PRODUCT_ID_SHOW_RELATED_SHOPPING_PRODUCTS)){
                            mProductIdShowRelatedShoppingProducts = savedInstanceState.getInt(STATE_PRODUCT_ID_SHOW_RELATED_SHOPPING_PRODUCTS);
                        }
                        if(savedInstanceState.containsKey(STATE_SHOW_PRODUCTS_RECENTLY_SEEN)){
                            mShowProductsRecentlySeen = savedInstanceState.getBoolean(STATE_SHOW_PRODUCTS_RECENTLY_SEEN);
                        }
                        if(savedInstanceState.containsKey(STATE_MAIN_PAGE_PRODUCT_SECTION_ID)){
                            mMainPageProductSectionId = savedInstanceState.getInt(STATE_MAIN_PAGE_PRODUCT_SECTION_ID);
                        }
                    }

                    if(getIntent()!=null && getIntent().getExtras()!=null) {
                        if(getIntent().getExtras().containsKey(KEY_PRODUCT_CATEGORY_ID)){
                            productCategoryId = getIntent().getExtras().getInt(KEY_PRODUCT_CATEGORY_ID);
                        }
                        if(getIntent().getExtras().containsKey(KEY_PRODUCT_SUBCATEGORY_ID)){
                            productSubCategoryId = getIntent().getExtras().getInt(KEY_PRODUCT_SUBCATEGORY_ID);
                        }
                        if(getIntent().getExtras().containsKey(KEY_PRODUCT_BRAND_ID)){
                            productBrandId = getIntent().getExtras().getInt(KEY_PRODUCT_BRAND_ID);
                        }
                        if(getIntent().getExtras().containsKey(KEY_PRODUCT_NAME)){
                            productName = getIntent().getExtras().getString(KEY_PRODUCT_NAME);
                        }
                        if(getIntent().getExtras().containsKey(KEY_PRODUCT_REFERENCE)){
                            productReference = getIntent().getExtras().getString(KEY_PRODUCT_REFERENCE);
                        }
                        if(getIntent().getExtras().containsKey(KEY_PRODUCT_PURPOSE)){
                            productPurpose = getIntent().getExtras().getString(KEY_PRODUCT_PURPOSE);
                        }
                        if(getIntent().getExtras().containsKey(KEY_PRODUCT_ID_SHOW_RELATED_SHOPPING_PRODUCTS)){
                            mProductIdShowRelatedShoppingProducts = getIntent().getExtras().getInt(KEY_PRODUCT_ID_SHOW_RELATED_SHOPPING_PRODUCTS);
                        }
                        if(getIntent().getExtras().containsKey(KEY_SHOW_PRODUCTS_RECENTLY_SEEN)){
                            mShowProductsRecentlySeen = getIntent().getExtras().getBoolean(KEY_SHOW_PRODUCTS_RECENTLY_SEEN);
                        }
                        if(getIntent().getExtras().containsKey(KEY_MAIN_PAGE_PRODUCT_SECTION_ID)){
                            mMainPageProductSectionId = getIntent().getExtras().getInt(KEY_MAIN_PAGE_PRODUCT_SECTION_ID);
                        }
                    }

                    if (productCategoryId != 0) {
                        products.addAll(new ProductDB(ProductsListActivity.this, user).getProductsByCategoryId(productCategoryId));
                    } else if (productSubCategoryId != 0) {
                        products.addAll(new ProductDB(ProductsListActivity.this, user).getProductsBySubCategoryId(productSubCategoryId, productName, productReference, productPurpose));
                    } else if (productBrandId != 0) {
                        products.addAll(new ProductDB(ProductsListActivity.this, user).getProductsByBrandId(productBrandId));
                    } else if (productName != null) {
                        products.addAll(new ProductDB(ProductsListActivity.this, user).getProductsByName(productName));
                    } else if (productReference != null) {
                        products.addAll(new ProductDB(ProductsListActivity.this, user).getProductsByReference(productReference));
                    } else if (productPurpose != null) {
                        products.addAll(new ProductDB(ProductsListActivity.this, user).getProductsByPurpose(productPurpose));
                    } else if (mProductIdShowRelatedShoppingProducts != 0) {
                        products.addAll(new ProductDB(ProductsListActivity.this, user)
                                .getRelatedShoppingProductsByProductId(mProductIdShowRelatedShoppingProducts, null));
                    } else if (mShowProductsRecentlySeen) {
                        products.addAll(new ProductRecentlySeenDB(ProductsListActivity.this, user)
                                .getProductsRecentlySeen(null));
                    } else if (mMainPageProductSectionId != 0) {
                        products.addAll(new MainPageProductSectionDB(ProductsListActivity.this, user)
                                .getActiveProductsByMainPageProductSectionId(mMainPageProductSectionId, null));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mRecyclerView = (RecyclerView) findViewById(R.id.product_list_result);

                            final ImageView changeLayoutImageButton = (ImageView) findViewById(R.id.change_layout_button);
                            if(changeLayoutImageButton!=null){
                                if(mCurrentProductsListAdapterMask==ProductsListAdapter.MASK_PRODUCT_LARGE_DETAILS){
                                    changeLayoutImageButton.setImageResource(R.drawable.ic_view_headline_white_24dp);
                                }else if(mCurrentProductsListAdapterMask==ProductsListAdapter.MASK_PRODUCT_DETAILS){
                                    changeLayoutImageButton.setImageResource(R.drawable.ic_dashboard_white_24dp);
                                }else{
                                    changeLayoutImageButton.setImageResource(R.drawable.ic_view_agenda_white_24dp);
                                }

                                changeLayoutImageButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (mLayoutManager instanceof GridLayoutManager) {
                                            mRecyclerViewCurrentFirstPosition = ((GridLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
                                        } else if (mLayoutManager instanceof LinearLayoutManager) {
                                            mRecyclerViewCurrentFirstPosition = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
                                        } else {
                                            int[] into = new int[getResources().getInteger(R.integer.number_of_cards_in_staggered_grid_layout)];
                                            ((StaggeredGridLayoutManager) mLayoutManager).findFirstCompletelyVisibleItemPositions(into);
                                            mRecyclerViewCurrentFirstPosition = into[0];
                                        }

                                        if(mCurrentProductsListAdapterMask==ProductsListAdapter.MASK_PRODUCT_LARGE_DETAILS){
                                            mCurrentProductsListAdapterMask = ProductsListAdapter.MASK_PRODUCT_DETAILS;
                                            if (useGridView()) {
                                                if(!(mLayoutManager instanceof GridLayoutManager)){
                                                    mLayoutManager = new GridLayoutManager(ProductsListActivity.this, getSpanCount());
                                                }
                                            }else{
                                                if(!(mLayoutManager instanceof LinearLayoutManager)){
                                                    mLayoutManager = new LinearLayoutManager(ProductsListActivity.this);
                                                }
                                            }
                                            changeLayoutImageButton.setImageResource(R.drawable.ic_dashboard_white_24dp);
                                        }else if(mCurrentProductsListAdapterMask==ProductsListAdapter.MASK_PRODUCT_DETAILS){
                                            mCurrentProductsListAdapterMask = ProductsListAdapter.MASK_PRODUCT_MIN_INFO_DYNAMIC_HEIGHT;
                                            if(!(mLayoutManager instanceof StaggeredGridLayoutManager)){
                                                mLayoutManager = new StaggeredGridLayoutManager(getResources().getInteger(R.integer.number_of_cards_in_staggered_grid_layout),
                                                        StaggeredGridLayoutManager.VERTICAL);
                                            }
                                            changeLayoutImageButton.setImageResource(R.drawable.ic_view_agenda_white_24dp);
                                        } else {
                                            mCurrentProductsListAdapterMask = ProductsListAdapter.MASK_PRODUCT_LARGE_DETAILS;
                                            if(!(mLayoutManager instanceof LinearLayoutManager)){
                                                mLayoutManager = new LinearLayoutManager(ProductsListActivity.this);
                                            }
                                            changeLayoutImageButton.setImageResource(R.drawable.ic_view_headline_white_24dp);
                                        }
                                        mRecyclerView.setLayoutManager(mLayoutManager);
                                        mRecyclerView.setAdapter(new ProductsListAdapter(ProductsListActivity.this,
                                                ProductsListActivity.this, products, mCurrentProductsListAdapterMask, mCurrentSortOption, user));
                                        mRecyclerView.scrollToPosition(mRecyclerViewCurrentFirstPosition);
                                    }
                                });
                            }

                            final ImageView sortProductsListImageButton = (ImageView) findViewById(R.id.sort_products_list_button);
                            if(sortProductsListImageButton!=null){
                                sortProductsListImageButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        DialogSortProductListOptions dialogSortProductListOptions =
                                                DialogSortProductListOptions.newInstance(user, mCurrentSortOption);
                                        dialogSortProductListOptions.show(ProductsListActivity.this.getSupportFragmentManager(),
                                                DialogUpdateShoppingCartQtyOrdered.class.getSimpleName());
                                    }
                                });
                            }

                            TextView categorySubcategoryResultsTextView = (TextView) findViewById(R.id.category_subcategory_results);
                            if(categorySubcategoryResultsTextView!=null){
                                if(!products.isEmpty()) {
                                    if (productCategoryId != 0) {
                                        ProductCategory productCategory = (new ProductCategoryDB(ProductsListActivity.this, user))
                                                .getProductCategory(productCategoryId);
                                        if(productCategory!=null){
                                            categorySubcategoryResultsTextView.setText(/*getString(R.string.category_detail,*/
                                                    productCategory.getName()/*)*/);
                                        }
                                    } else if (productSubCategoryId != 0) {
                                        ProductSubCategory productSubCategory = (new ProductSubCategoryDB(ProductsListActivity.this, user))
                                                .getProductSubCategory(productSubCategoryId);
                                        if(productSubCategory!=null){
                                            categorySubcategoryResultsTextView.setText(/*getString(R.string.subcategory_detail,*/
                                                    productSubCategory.getName()/*)*/);
                                        }
                                    } else if (productBrandId != 0) {
                                        if(products.get(0).getProductBrand()!=null
                                                && !TextUtils.isEmpty(products.get(0).getProductBrand().getName())){
                                            categorySubcategoryResultsTextView.setText(Html.fromHtml(getString(R.string.brand_detail_html,
                                                    products.get(0).getProductBrand().getName())));
                                        }
                                    } else if (productName != null) {
                                        categorySubcategoryResultsTextView.setText(getString(R.string.search_pattern_detail,
                                                productName));
                                    } else if (productReference != null) {
                                        categorySubcategoryResultsTextView.setText(getString(R.string.search_pattern_detail,
                                                productReference));
                                    } else if (productPurpose != null) {
                                        categorySubcategoryResultsTextView.setText(getString(R.string.search_pattern_detail,
                                                productPurpose));
                                    } else if (mProductIdShowRelatedShoppingProducts != 0) {
                                        categorySubcategoryResultsTextView.setText(R.string.related_shopping_products);
                                    } else if (mShowProductsRecentlySeen) {
                                        categorySubcategoryResultsTextView.setText(R.string.products_recently_seen);
                                    } else if (mMainPageProductSectionId != 0) {
                                        MainPageProductSection mainPageProductSection =
                                                (new MainPageProductSectionDB(ProductsListActivity.this, user))
                                                        .getActiveMainPageProductSectionById(mMainPageProductSectionId);
                                        if (mainPageProductSection!=null) {
                                            categorySubcategoryResultsTextView.setText(mainPageProductSection.getName());
                                        }
                                    }
                                } else {
                                    categorySubcategoryResultsTextView.setText(getString(R.string.no_products_to_show));
                                }
                            }

                            // use this setting to improve performance if you know that changes
                            // in content do not change the layout size of the RecyclerView
                            mRecyclerView.setHasFixedSize(true);
                            if (mCurrentProductsListAdapterMask==ProductsListAdapter.MASK_PRODUCT_DETAILS) {
                                if (useGridView()) {
                                    mLayoutManager = new GridLayoutManager(ProductsListActivity.this, getSpanCount());
                                } else {
                                    mLayoutManager = new LinearLayoutManager(ProductsListActivity.this);
                                }
                            }else if (mCurrentProductsListAdapterMask==ProductsListAdapter.MASK_PRODUCT_LARGE_DETAILS) {
                                mLayoutManager = new LinearLayoutManager(ProductsListActivity.this);
                            }else{
                                mLayoutManager = new StaggeredGridLayoutManager(getResources().getInteger(R.integer.number_of_cards_in_staggered_grid_layout),
                                        StaggeredGridLayoutManager.VERTICAL);
                            }
                            mRecyclerView.setLayoutManager(mLayoutManager);
                            mRecyclerView.setAdapter(new ProductsListAdapter(ProductsListActivity.this,
                                    ProductsListActivity.this, products, mCurrentProductsListAdapterMask, mCurrentSortOption, user));

                            if (mRecyclerViewCurrentFirstPosition!=0) {
                                mRecyclerView.scrollToPosition(mRecyclerViewCurrentFirstPosition);
                            }

                            if(findViewById(R.id.filter_bar_linear_layout)!=null){
                                final ImageView filterImageView = (ImageView) findViewById(R.id.filter_imageView);

                                final TextView productsListSize = (TextView) findViewById(R.id.products_list_size);
                                if(productsListSize!=null){
                                    productsListSize.setText(getString(R.string.products_list_size_details,
                                            String.valueOf(mRecyclerView.getAdapter().getItemCount())));
                                }

                                final EditText filterProduct = (EditText) findViewById(R.id.filter_product_editText);
                                if(filterProduct!=null && filterImageView!=null && productsListSize!=null) {
                                    final View.OnClickListener filterImageViewOnClickListener =
                                            new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    filterProduct.setText(null);
                                                }
                                            };
                                    filterProduct.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                                        @Override
                                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                                            if(s.length()>0){
                                                filterImageView.setImageResource(R.drawable.ic_close_black_24dp);
                                                filterImageView.setOnClickListener(filterImageViewOnClickListener);
                                            }else{
                                                filterImageView.setImageResource(R.drawable.ic_filter_list_black_24dp);
                                                filterImageView.setOnClickListener(null);
                                            }
                                            mCurrentFilterText = s.toString();
                                        }

                                        private boolean isTyping = false;
                                        private Timer timer = new Timer();
                                        private final long DELAY = 800; // milliseconds

                                        @Override
                                        public void afterTextChanged(final Editable s) {
                                            if(!isTyping) {
                                                isTyping = true;
                                            }
                                            timer.cancel();
                                            timer = new Timer();
                                            timer.schedule(
                                                    new TimerTask() {
                                                        @Override
                                                        public void run() {
                                                            isTyping = false;
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    /**************************/
                                                                    ((ProductsListAdapter) mRecyclerView.getAdapter()).filter(mCurrentFilterText);
                                                                    productsListSize.setText(getString(R.string.products_list_size_details,
                                                                            String.valueOf(mRecyclerView.getAdapter().getItemCount())));
                                                                }
                                                            });
                                                        }
                                                    },
                                                    DELAY
                                            );
                                        }
                                    });
                                    if (mCurrentFilterText!=null) {
                                        filterProduct.setText(mCurrentFilterText);
                                    }
                                    filterProduct.setSelection(filterProduct.length());
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

    @Override
    protected void onPostResume() {
        Utils.manageNotificationOnDrawerLayout(this);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer!=null && drawer.isDrawerOpen(GravityCompat.START)) {
            Utils.loadNavigationViewBadge(getApplicationContext(), Utils.getCurrentUser(this),
                    (NavigationView) findViewById(R.id.nav_view));
        }
        super.onPostResume();
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
        int i = item.getItemId();
        if (i == R.id.search) {
            startActivity(new Intent(this, SearchResultsActivity.class));
            return true;
        } else {
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
            if (mLayoutManager instanceof GridLayoutManager) {
                outState.putInt(STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION,
                        ((GridLayoutManager) mLayoutManager).findFirstVisibleItemPosition());
            } else if (mLayoutManager instanceof LinearLayoutManager) {
                outState.putInt(STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION,
                        ((LinearLayoutManager) mLayoutManager).findFirstCompletelyVisibleItemPosition());
            } else if (mLayoutManager instanceof StaggeredGridLayoutManager) {
                int[] into = new int[getResources().getInteger(R.integer.number_of_cards_in_staggered_grid_layout)];
                ((StaggeredGridLayoutManager) mLayoutManager).findFirstCompletelyVisibleItemPositions(into);
                outState.putInt(STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION, into[0]);
            }
        } catch (Exception e) {
            outState.putInt(STATE_RECYCLER_VIEW_CURRENT_FIRST_POSITION, mRecyclerViewCurrentFirstPosition);
        }
        outState.putInt(STATE_CURRENT_SORT_OPTION, mCurrentSortOption);
        if (!TextUtils.isEmpty(mCurrentFilterText)) {
            outState.putString(STATE_CURRENT_FILTER_TEXT, mCurrentFilterText);
        }
        outState.putInt(STATE_SPINNER_SELECTED_ITEM_POSITION, mSpinnerSelectedItemPosition);
        outState.putInt(STATE_PRODUCT_ID_SHOW_RELATED_SHOPPING_PRODUCTS, mProductIdShowRelatedShoppingProducts);
        outState.putBoolean(STATE_SHOW_PRODUCTS_RECENTLY_SEEN, mShowProductsRecentlySeen);
        outState.putInt(STATE_MAIN_PAGE_PRODUCT_SECTION_ID, mMainPageProductSectionId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void sortProductsList(int sortOption, User user) {
        if(mRecyclerView!=null && mRecyclerView.getAdapter() instanceof ProductsListAdapter){
            mCurrentSortOption = sortOption;
            ((ProductsListAdapter) mRecyclerView.getAdapter()).sortProductsList(mCurrentSortOption);
        }
    }
}