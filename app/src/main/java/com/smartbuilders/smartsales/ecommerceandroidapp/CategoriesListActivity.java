package com.smartbuilders.smartsales.ecommerceandroidapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.smartbuilders.smartsales.ecommerceandroidapp.model.ProductCategory;

public class CategoriesListActivity extends AppCompatActivity implements
        CategoriesListFragment.Callback {

    private static final String TAG = CategoriesListActivity.class.getSimpleName();
    private static final String SUBCATEGORYFRAGMENT_TAG = "SUBCATEGORYFRAGMENT_TAG";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(findViewById(R.id.subcategory_list_container) != null){
            // If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if(savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.subcategory_list_container, new SubCategoriesListFragment(),
                                SUBCATEGORYFRAGMENT_TAG)
                        .commit();
            }
        }else{
            mTwoPane = false;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onItemSelected(ProductCategory productCategory) {
        if(mTwoPane){
            Bundle args = new Bundle();
            args.putInt(SubCategoriesListFragment.KEY_CATEGORY_ID, productCategory.getId());

            SubCategoriesListFragment fragment = new SubCategoriesListFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.subcategory_list_container, fragment, SUBCATEGORYFRAGMENT_TAG)
                    .commit();
        }else{
            Intent intent = new Intent(CategoriesListActivity.this, SubCategoriesListActivity.class);
            intent.putExtra(SubCategoriesListFragment.KEY_CATEGORY_ID, productCategory.getId());
            startActivity(intent);
            finish();
        }
    }
}
