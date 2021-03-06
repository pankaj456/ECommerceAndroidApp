package com.smartbuilders.smartsales.ecommerce.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.smartbuilders.smartsales.ecommerce.BuildConfig;
import com.smartbuilders.smartsales.ecommerce.ChatMessagesActivity;
import com.smartbuilders.smartsales.ecommerce.ChatContactsListActivity;
import com.smartbuilders.smartsales.ecommerce.NotificationsListActivity;
import com.smartbuilders.smartsales.ecommerce.OrdersTrackingListActivity;
import com.smartbuilders.smartsales.ecommerce.SettingsDataSync;
import com.smartbuilders.smartsales.ecommerce.SettingsImagesManagement;
import com.smartbuilders.smartsales.ecommerce.WelcomeScreenSlideActivity;
import com.smartbuilders.smartsales.ecommerce.data.ChatMessageDB;
import com.smartbuilders.smartsales.ecommerce.data.NotificationHistoryDB;
import com.smartbuilders.smartsales.ecommerce.data.SalesRepDB;
import com.smartbuilders.smartsales.ecommerce.data.UserBusinessPartnerDB;
import com.smartbuilders.smartsales.ecommerce.model.NotificationHistory;
import com.smartbuilders.smartsales.salesforcesystem.PricesListActivity;
import com.smartbuilders.smartsales.salesforcesystem.SalesForceSystemMainActivity;
import com.smartbuilders.synchronizer.ids.model.User;
import com.smartbuilders.synchronizer.ids.model.UserProfile;
import com.smartbuilders.synchronizer.ids.syncadapter.model.AccountGeneral;
import com.smartbuilders.synchronizer.ids.utils.ApplicationUtilities;
import com.smartbuilders.smartsales.ecommerce.BusinessPartnersListActivity;
import com.smartbuilders.smartsales.ecommerce.CompanyActivity;
import com.smartbuilders.smartsales.ecommerce.ContactUsActivity;
import com.smartbuilders.smartsales.ecommerce.MainActivity;
import com.smartbuilders.smartsales.ecommerce.OrdersListActivity;
import com.smartbuilders.smartsales.ecommerce.R;
import com.smartbuilders.smartsales.ecommerce.RecommendedProductsListActivity;
import com.smartbuilders.smartsales.ecommerce.ShoppingSaleActivity;
import com.smartbuilders.smartsales.ecommerce.ShoppingSalesListActivity;
import com.smartbuilders.smartsales.ecommerce.data.BusinessPartnerDB;
import com.smartbuilders.smartsales.ecommerce.SalesOrdersListActivity;
import com.smartbuilders.smartsales.ecommerce.ShoppingCartActivity;
import com.smartbuilders.smartsales.ecommerce.WishListActivity;
import com.smartbuilders.smartsales.ecommerce.data.OrderLineDB;
import com.smartbuilders.smartsales.ecommerce.data.RecommendedProductDB;
import com.smartbuilders.smartsales.ecommerce.model.BusinessPartner;
import com.smartbuilders.smartsales.ecommerce.model.Product;
import com.smartbuilders.smartsales.ecommerce.providers.CachedFileProvider;
import com.smartbuilders.smartsales.ecommerce.session.Parameter;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Alberto on 26/3/2016.
 */
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();
    private static Typeface typefaceMedium;

    /**
     *
     * @return
     */
    public static boolean isExternalStorageReadOnly() {
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
    }

    /**
     *
     * @return
     */
    public static boolean isExternalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     *
     * @param ctx
     * @throws Throwable
     */
    public static void showPromptShareApp(Context ctx) throws Throwable {
        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, ctx.getString(R.string.checkout_my_app,
                    ctx.getString(R.string.company_name), ctx.getPackageName()));
            sendIntent.setType("text/plain");
            ctx.startActivity(sendIntent);
        } catch(android.content.ActivityNotFoundException ex){
            Toast.makeText(ctx, ctx.getString(R.string.no_app_installed_to_share), Toast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     *
     * @param activity
     * @param context
     * @param user
     * @param product
     * @return
     */
    public static Intent createShareProductIntentFromView(final Activity activity, final Context context,
                                                          final User user, final Product product){
        if(activity==null || context==null || user==null || product==null){
            return null;
        }

        /****************************************************************************************/
        final View view = activity.getLayoutInflater().inflate(R.layout.product_share_layout, null);
        if (BuildConfig.USE_PRODUCT_IMAGE) {
            if(!TextUtils.isEmpty(product.getImageFileName())){
                Bitmap productImage = Utils.getImageFromOriginalDirByFileName(context, product.getImageFileName());
                if(productImage==null){
                    //Si el archivo no existe entonces se descarga
                    try {
                        productImage = (new GetFileFromServlet(product.getImageFileName(), false, user, context)).execute().get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                //Si no se tiene el archivo en tamano original se recurre al de miniatura
                if(productImage==null){
                    productImage = Utils.getImageFromThumbDirByFileName(context, product.getImageFileName());
                }
                if(productImage!=null){
                    ((ImageView) view.findViewById(R.id.product_image)).setImageBitmap(productImage);
                }
            }
        } else {
            view.findViewById(R.id.product_image).setVisibility(View.GONE);
        }

        if(!TextUtils.isEmpty(product.getName())){
            ((TextView) view.findViewById(R.id.product_name)).setText(product.getName());
        }else{
            view.findViewById(R.id.product_name).setVisibility(View.GONE);
        }

        ((TextView) view.findViewById(R.id.product_internal_code)).setText(product.getInternalCodeMayoreoFormat());

        ((TextView) view.findViewById(R.id.product_reference)).setText(product.getReference());

        if(product.getProductBrand()!=null
                && !TextUtils.isEmpty(product.getProductBrand().getName())){
            ((TextView) view.findViewById(R.id.product_brand)).setText(Html.fromHtml(context.getString(R.string.brand_detail_html,
                    product.getProductBrand().getName())));
        }else{
            view.findViewById(R.id.product_brand).setVisibility(TextView.GONE);
        }
        if(!TextUtils.isEmpty(product.getDescription())){
            ((TextView) view.findViewById(R.id.product_description)).setText(Html.fromHtml(context.getString(R.string.product_description_detail_html,
                    product.getDescription())));
        }else{
            view.findViewById(R.id.product_description).setVisibility(View.GONE);
        }
        if(!TextUtils.isEmpty(product.getPurpose())){
            ((TextView) view.findViewById(R.id.product_purpose)).setText(Html.fromHtml(context.getString(R.string.product_purpose_detail_html,
                    product.getPurpose())));
        }else{
            view.findViewById(R.id.product_purpose).setVisibility(View.GONE);
        }

        if(product.getProductCommercialPackage()!=null
                && !TextUtils.isEmpty(product.getProductCommercialPackage().getUnitDescription())){
            ((TextView) view.findViewById(R.id.product_commercial_package)).setText(Html.fromHtml(context.getString(R.string.commercial_package_label_detail_html,
                    product.getProductCommercialPackage().getUnitDescription(), product.getProductCommercialPackage().getUnits())));
        }else{
            view.findViewById(R.id.product_commercial_package).setVisibility(TextView.GONE);
        }

        if (Parameter.isManagePriceInOrder(context, user)
                && Parameter.showProductPriceInShareProductCard(context, user)
                && product.getProductPriceAvailability().getAvailability()>0
                && product.getProductPriceAvailability().getPrice()>0) {
            if (Parameter.showProductTotalPrice(context, user)) {
                ((TextView) view.findViewById(R.id.product_price))
                        .setText(context.getString(R.string.product_total_price_detail,
                                product.getProductPriceAvailability().getCurrency().getName(),
                                product.getProductPriceAvailability().getTotalPriceStringFormat()));
                view.findViewById(R.id.product_price).setVisibility(View.VISIBLE);
            } else if (Parameter.showProductPrice(context, user)) {
                ((TextView) view.findViewById(R.id.product_price))
                        .setText(context.getString(R.string.product_price_detail,
                                product.getProductPriceAvailability().getCurrency().getName(),
                                product.getProductPriceAvailability().getPriceStringFormat()));
                view.findViewById(R.id.product_price).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.product_price).setVisibility(View.GONE);
            }
        } else {
            view.findViewById(R.id.product_price).setVisibility(View.GONE);
        }
        /****************************************************************************************/
        final String fileName = "productShared.png";
        createFileInCacheDir(fileName, getBitmapFromView(view), context);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        //shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_TEXT, product.getName() + " - http://"
                + context.getString(R.string.company_host_name) + "/?product="+product.getInternalCode());
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://"
                + CachedFileProvider.AUTHORITY + File.separator + fileName));
        return shareIntent;
    }

    public static Bitmap getBitmapFromView(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.draw(canvas);
        return bitmap;
    }

    /**
     *
     * @param fileName
     * @param resId
     * @param context
     */
    public static void createFileInCacheDir(String fileName, int resId, Context context){
        //check if external storage is available so that we can dump our PDF file there
        if (!Utils.isExternalStorageAvailable() || Utils.isExternalStorageReadOnly()) {
            Toast.makeText(context, context.getString(R.string.external_storage_unavailable), Toast.LENGTH_SHORT).show();
        } else if (!TextUtils.isEmpty(fileName) && context!=null){
            //path for the image file in the external storage
            File imageFile = new File(context.getCacheDir() + File.separator + fileName);
            try {
                imageFile.createNewFile();
                FileOutputStream fo = new FileOutputStream(imageFile);
                Bitmap image = BitmapFactory.decodeResource(context.getResources(), resId);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                image.compress(fileName.contains(".png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, bytes);
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (IOException e1) {
                e1.printStackTrace();
                Toast.makeText(context, e1.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param fileName
     * @param image
     * @param context
     */
    public static void createFileInCacheDir(String fileName, Bitmap image, Context context){
        //check if external storage is available so that we can dump our PDF file there
        if (!Utils.isExternalStorageAvailable() || Utils.isExternalStorageReadOnly()) {
            Toast.makeText(context, context.getString(R.string.external_storage_unavailable), Toast.LENGTH_SHORT).show();
        } else if (!TextUtils.isEmpty(fileName) && image!=null && context!=null){
            //path for the image file in the external storage
            File imageFile = new File(context.getCacheDir() + File.separator + fileName);
            try {
                imageFile.createNewFile();
                FileOutputStream fo = new FileOutputStream(imageFile);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                image.compress(fileName.contains(".png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, bytes);
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (IOException e1) {
                e1.printStackTrace();
                Toast.makeText(context, e1.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param fileName
     * @param image
     * @param context
     */
    public static void createFileInThumbDir(String fileName, Bitmap image, Context context){
        if (!saveImagesInDevice(context)) {
            return;
        }
        //check if external storage is available so that we can dump our PDF file there
        if (!Utils.isExternalStorageAvailable() || Utils.isExternalStorageReadOnly()) {
            Log.e(TAG, context.getString(R.string.external_storage_unavailable));
        } else if (!TextUtils.isEmpty(fileName) && image!=null && context!=null
                && context.getExternalFilesDir(null)!=null){
            //path for the image file in the external storage
            File imageFile = new File(getImagesThumbFolderPath(context), fileName);
            try {
                try {
                    imageFile.createNewFile();
                } catch (IOException e) {
                    new File(getImagesThumbFolderPath(context)).mkdirs();
                    imageFile = new File(getImagesThumbFolderPath(context), fileName);
                    imageFile.createNewFile();
                }
                FileOutputStream fo = new FileOutputStream(imageFile);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                image.compress(fileName.contains(".png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, bytes);
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (IOException | NullPointerException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     *
     * @param fileName
     * @param image
     * @param context
     */
    public static void createFileInOriginalDir(String fileName, Bitmap image, Context context){
        if (!saveOriginalImagesInDevice(context)) {
            return;
        }
        //check if external storage is available so that we can dump our PDF file there
        if (!Utils.isExternalStorageAvailable() || Utils.isExternalStorageReadOnly()) {
            Log.e(TAG, context.getString(R.string.external_storage_unavailable));
        } else if (!TextUtils.isEmpty(fileName) && image!=null && context!=null
                && context.getExternalFilesDir(null)!=null){
            //path for the image file in the external storage
            File imageFile = new File(getImagesOriginalFolderPath(context), fileName);
            try {
                try {
                    imageFile.createNewFile();
                } catch (IOException e) {
                    new File(getImagesOriginalFolderPath(context)).mkdirs();
                    imageFile = new File(getImagesOriginalFolderPath(context), fileName);
                    imageFile.createNewFile();
                }
                FileOutputStream fo = new FileOutputStream(imageFile);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                image.compress(fileName.contains(".png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, bytes);
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (IOException | NullPointerException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     *
     * @param context
     * @param isLandscape
     * @param fileName
     * @param image
     */
    public static void createFileInBannerDir(Context context, boolean isLandscape, String fileName, Bitmap image){
        //check if external storage is available so that we can dump our PDF file there
        if (!Utils.isExternalStorageAvailable() || Utils.isExternalStorageReadOnly()) {
            Log.e(TAG, context.getString(R.string.external_storage_unavailable));
        } else if (!TextUtils.isEmpty(fileName) && image!=null && context!=null
                && context.getExternalFilesDir(null)!=null){
            //path for the image file in the external storage
            File imageFile = new File(isLandscape ? getImagesBannerLandscapeFolderPath(context)
                    : getImagesBannerPortraitFolderPath(context), fileName);
            try {
                try {
                    imageFile.createNewFile();
                } catch (IOException e) {
                    new File(isLandscape ? getImagesBannerLandscapeFolderPath(context)
                            : getImagesBannerPortraitFolderPath(context)).mkdirs();
                    imageFile = new File(isLandscape ? getImagesBannerLandscapeFolderPath(context)
                            : getImagesBannerPortraitFolderPath(context), fileName);
                    imageFile.createNewFile();
                }
                FileOutputStream fo = new FileOutputStream(imageFile);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                image.compress(fileName.contains(".png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, bytes);
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (IOException | NullPointerException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     *
     * @param fileName
     * @param image
     * @param context
     */
    public static void createFileInProductBrandPromotionalDir(Context context, boolean isLandscape, String fileName, Bitmap image){
        //check if external storage is available so that we can dump our PDF file there
        if (!Utils.isExternalStorageAvailable() || Utils.isExternalStorageReadOnly()) {
            Log.e(TAG, context.getString(R.string.external_storage_unavailable));
        } else if (!TextUtils.isEmpty(fileName) && image!=null && context!=null
                && context.getExternalFilesDir(null)!=null){
            //path for the image file in the external storage
            File imageFile = new File(isLandscape ? getImagesProductBrandPromotionalLandscapeFolderPath(context)
                    : getImagesProductBrandPromotionalPortraitFolderPath(context), fileName);
            try {
                try {
                    imageFile.createNewFile();
                } catch (IOException e) {
                    new File(isLandscape ? getImagesProductBrandPromotionalLandscapeFolderPath(context)
                            : getImagesProductBrandPromotionalPortraitFolderPath(context)).mkdirs();
                    imageFile = new File(isLandscape ? getImagesProductBrandPromotionalLandscapeFolderPath(context)
                            : getImagesProductBrandPromotionalPortraitFolderPath(context), fileName);
                    imageFile.createNewFile();
                }
                FileOutputStream fo = new FileOutputStream(imageFile);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                image.compress(fileName.contains(".png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, bytes);
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (IOException | NullPointerException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static File getFileInOriginalDirByFileName(Context context, String fileName){
        try {
            File imgFile = new File(getImagesOriginalFolderPath(context), fileName);
            if(imgFile.exists()){
                return imgFile;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void loadOriginalImageByFileName(final Context context, final User user,
                                                   final String fileName, final ImageView imageView){
        try {
            if(!TextUtils.isEmpty(fileName)){
                Drawable placeHolderDrawable = Drawable.createFromPath(getImagesThumbFolderPath(context) + fileName);
                if (placeHolderDrawable==null) {
                    placeHolderDrawable = getLoadingImageDrawable(context);
                }

                Drawable onErrorDrawable = Drawable.createFromPath(getImagesThumbFolderPath(context) + fileName);
                if (onErrorDrawable==null) {
                    onErrorDrawable = getNoImageAvailableDrawable(context);
                }

                File img = Utils.getFileInOriginalDirByFileName(context, fileName);
                if(img!=null && img.exists()){
                    Picasso.with(context).load(img).placeholder(placeHolderDrawable).error(onErrorDrawable)
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(imageView);
                    Picasso.with(context).invalidate(img);
                }else{
                    Picasso.with(context)
                            .load(user.getServerAddress() + "/IntelligentDataSynchronizer/GetOriginalImage?fileName=" + fileName)
                            .placeholder(placeHolderDrawable)
                            .error(onErrorDrawable)
                            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .into(imageView, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    Utils.createFileInOriginalDir(fileName,
                                            ((BitmapDrawable)imageView.getDrawable()).getBitmap(), context);
                                    Picasso.with(context)
                                            .invalidate(user.getServerAddress() + "/IntelligentDataSynchronizer/GetOriginalImage?fileName=" + fileName);
                                }

                                @Override
                                public void onError() { }
                            });
                }
            }else{
                Picasso.with(context).load(R.drawable.no_image_available)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(imageView);
            }
        } catch (Exception e) {
            if(imageView!=null){
                Picasso.with(context).load(R.drawable.no_image_available)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(imageView);
            }
            e.printStackTrace();
        }
    }

    public static File getFileInBannerDirByFileName(Context context, boolean isLandscape, String fileName){
        try {
            File imgFile = new File(isLandscape ? getImagesBannerLandscapeFolderPath(context)
                    : getImagesBannerPortraitFolderPath(context), fileName);
            if(imgFile.exists()){
                return imgFile;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File getFileInProductBrandPromotionalDirByFileName(Context context, boolean isLandscape, String fileName){
        try {
            File imgFile = new File(isLandscape ? getImagesProductBrandPromotionalLandscapeFolderPath(context)
                    : getImagesProductBrandPromotionalPortraitFolderPath(context), fileName);
            if(imgFile.exists()){
                return imgFile;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File getFileInThumbDirByFileName(Context context, String fileName){
        try {
            File imgFile = new File(getImagesThumbFolderPath(context), fileName);
            if(imgFile.exists()){
                return imgFile;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void loadThumbImageByFileName(final Context context, final User user,
                                                final String fileName, final ImageView imageView){
        try {
            if(!TextUtils.isEmpty(fileName)){
                File imgFile = new File(getImagesThumbFolderPath(context), fileName);
                if(imgFile.exists()){
                    Picasso.with(context).load(imgFile).error(getNoImageAvailableDrawable(context))
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(imageView);
                    Picasso.with(context).invalidate(imgFile);
                }else{
                    Picasso.with(context)
                            .load(user.getServerAddress() + "/IntelligentDataSynchronizer/GetThumbImage?fileName=" + fileName)
                            .placeholder(getLoadingImageDrawable(context))
                            .error(getNoImageAvailableDrawable(context))
                            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .into(imageView, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    Utils.createFileInThumbDir(fileName,
                                            ((BitmapDrawable)imageView.getDrawable()).getBitmap(), context);
                                    Picasso.with(context)
                                            .invalidate(user.getServerAddress() + "/IntelligentDataSynchronizer/GetThumbImage?fileName=" + fileName);
                                }

                                @Override
                                public void onError() { }
                            });
                }
            }else{
                Picasso.with(context).load(R.drawable.no_image_available)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(imageView);
            }
        } catch (Exception e) {
            if(imageView!=null){
                Picasso.with(context).load(R.drawable.no_image_available)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(imageView);
            }
            e.printStackTrace();
        }
    }

    public static Bitmap getImageFromOriginalDirByFileName(Context context, String fileName){
        try {
            File imgFile = new File(getImagesOriginalFolderPath(context), fileName);
            if(imgFile.exists()){
                return decodeSampledBitmap(imgFile.getAbsolutePath(), 250, 250);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getImageFromThumbDirByFileName(Context context, String fileName){
        try {
            File imgFile = new File(getImagesThumbFolderPath(context), fileName);
            if(imgFile.exists()){
                return decodeSampledBitmap(imgFile.getAbsolutePath(), 150, 150);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param image
     * @param context
     * @param user
     */
    public static void saveUserCompanyImage(Bitmap image, Context context, User user){
        //check if external storage is available so that we can dump our PDF file there
        if (!Utils.isExternalStorageAvailable() || Utils.isExternalStorageReadOnly()) {
            Log.e(TAG, context.getString(R.string.external_storage_unavailable));
        } else if (image!=null && context!=null
                && context.getExternalFilesDir(null)!=null){
            //path for the image file in the external storage
            File imageFile = new File(getImagesUserCompanyFolderPath(context, user),
                    "user_company_logo.jpg");
            try {
                try {
                    imageFile.createNewFile();
                } catch (IOException e) {
                    new File(getImagesUserCompanyFolderPath(context, user)).mkdirs();
                    imageFile = new File(getImagesUserCompanyFolderPath(context, user),
                            "user_company_logo.jpg");
                    imageFile.createNewFile();
                }
                FileOutputStream fo = new FileOutputStream(imageFile);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (IOException | NullPointerException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void saveUserCompanyImage(Context context, User user, InputStream inputStream){
        OutputStream outputStream = null;
        try {
            // write the inputStream to a FileOutputStream
            try {
                outputStream = new FileOutputStream(new File(getImagesUserCompanyFolderPath(context, user),
                        "user_company_logo.jpg"));
            } catch (FileNotFoundException e) {
                new File(getImagesUserCompanyFolderPath(context, user)).mkdirs();
                outputStream = new FileOutputStream(new File(getImagesUserCompanyFolderPath(context, user),
                        "user_company_logo.jpg"));
            }
            int read;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Bitmap getUserCompanyImage(Context context, User user){
        try {
            File imgFile = new File(getImagesUserCompanyFolderPath(context, user), "user_company_logo.jpg");
            if(imgFile.exists()){
                return decodeSampledBitmap(imgFile.getAbsolutePath(), 150, 150);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean deleteUserCompanyImage(Context context, User user) {
        try {
            File imgFile = new File(getImagesUserCompanyFolderPath(context, user), "user_company_logo.jpg");
            if (imgFile.exists()) {
                return imgFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap getThumbImage(Context context, User user, String fileName){
        return getThumbImage(context, user, fileName, 150, 150);
    }

    public static Bitmap getThumbImage(Context context, User user, String fileName, int reqWidth, int reqHeight){
        if(!TextUtils.isEmpty(fileName)){
            File imgFile = new File(getImagesThumbFolderPath(context), fileName);
            if(imgFile.exists()){
                return decodeSampledBitmap(imgFile.getAbsolutePath(), reqWidth, reqHeight);
            } else {
                //Si el archivo no existe entonces se descarga
                GetFileFromServlet getFileFromServlet =
                        new GetFileFromServlet(fileName, true, user, context);
                try {
                    return getFileFromServlet.execute().get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static Bitmap decodeSampledBitmap(String pathName, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static String getAppVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "error";
    }

    public static int getAppVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void setCustomToolbarTitle(final Context context, Toolbar toolbar, boolean goHome){
        if (context!=null && toolbar!=null) {
            toolbar.setTitle("");
            if (goHome) {
                for (int i = 0; i < toolbar.getChildCount(); i++) {
                    if (toolbar.getChildAt(i) instanceof ImageView) {
                        (toolbar.getChildAt(i)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (BuildConfig.IS_SALES_FORCE_SYSTEM) {
                                    context.startActivity(new Intent(context, SalesForceSystemMainActivity.class));
                                } else {
                                    context.startActivity(new Intent(context, MainActivity.class));
                                }
                            }
                        });
                        break;
                    }
                }
            }
        }
    }

    public static void setCustomActionbarTitle(final Activity activity, ActionBar actionBar, boolean goToHome){
        if(activity!=null && actionBar!=null){
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            View customView = activity.getLayoutInflater().inflate(R.layout.actionbar_title, null);
            if (customView!=null) {
                if (goToHome && customView.findViewById(R.id.actionbarLogo)!=null) {
                    customView.findViewById(R.id.actionbarLogo).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activity.getApplicationContext().startActivity(new Intent(activity.getApplicationContext(),
                                    MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                    });
                }
                actionBar.setCustomView(customView);
            }
        }
    }

    public static void manageNotificationOnDrawerLayout(Activity activity) {
        try {
            if (activity != null && activity.findViewById(R.id.badge_ham) != null) {
                if (PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getBoolean("show_badge", false)) {
                    activity.findViewById(R.id.badge_ham).setVisibility(View.VISIBLE);
                } else {
                    activity.findViewById(R.id.badge_ham).setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param context
     * @param user
     * @param navigationView
     */
    public static void loadNavigationViewBadge(Context context, User user, NavigationView navigationView){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && context!=null && user!=null
                && navigationView!=null && navigationView.getMenu()!=null) {
            if(navigationView.getMenu().findItem(R.id.nav_wish_list)!=null) {
                try {
                    int count = (new OrderLineDB(context, user)).getActiveWishListLinesNumber();
                    if (count > 0) {
                        ((TextView) navigationView.getMenu().findItem(R.id.nav_wish_list).getActionView())
                                .setText(count < 1000 ? String.valueOf(count) : "+999");
                    }
                } catch (NullPointerException e) {
                    // do nothing
                }
            }
            if (navigationView.getMenu().findItem(R.id.nav_recommended_products_list)!=null) {
                try {
                    int count = (new RecommendedProductDB(context, user)).getRecommendedProductsCount();
                    if (count > 0) {
                        ((TextView) navigationView.getMenu().findItem(R.id.nav_recommended_products_list).getActionView())
                                .setText(count < 1000 ? String.valueOf(count) : "+999");
                    }
                } catch (Exception e) {
                    // do nothing
                }
            }
            if (navigationView.getMenu().findItem(R.id.nav_notifications_history_list)!=null) {
                try {
                    int count = (new NotificationHistoryDB(context, user)).getCountByStatus(NotificationHistory.STATUS_NOT_SEEN);
                    if (count > 0) {
                        ((TextView) navigationView.getMenu().findItem(R.id.nav_notifications_history_list).getActionView())
                                .setText(count < 1000 ? String.valueOf(count) : "+999");
                    }
                } catch (Exception e) {
                    // do nothing
                }
            }
            if (navigationView.getMenu().findItem(R.id.nav_business_partners)!=null) {
                try {
                    int count = 0;
                    if (user.getUserProfileId()== UserProfile.SALES_MAN_PROFILE_ID) {
                        count = (new BusinessPartnerDB(context, user)).getBusinessPartnersCount();
                    } else if (user.getUserProfileId()== UserProfile.BUSINESS_PARTNER_PROFILE_ID) {
                        count = (new UserBusinessPartnerDB(context, user)).getUserBusinessPartnersCount();
                    }
                    if (count > 0) {
                        ((TextView) navigationView.getMenu().findItem(R.id.nav_business_partners).getActionView())
                                .setText(String.valueOf(count));
                    }
                } catch (Exception e) {
                    // do nothing
                }
            }
            if (navigationView.getMenu().findItem(R.id.nav_chat)!=null) {
                try {
                    int count = (new ChatMessageDB(context, user)).getUnreadMessagesCount();
                    if (count > 0) {
                        ((TextView) navigationView.getMenu().findItem(R.id.nav_chat).getActionView())
                                .setText(String.valueOf(count));
                    }
                } catch (Exception e) {
                    // do nothing
                }
            }
        }
    }

    /**
     *
     * @param itemId
     * @param activity
     */
    public static void navigationItemSelectedBehave(int itemId, Activity activity) {
        try {
            if (itemId == R.id.nav_home) {
                if (BuildConfig.IS_SALES_FORCE_SYSTEM) {
                    activity.startActivity(new Intent(activity, SalesForceSystemMainActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP));
                } else {
                    activity.startActivity(new Intent(activity, MainActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP));
                }
            } else if (itemId == R.id.nav_shopping_cart) {
                activity.startActivity(new Intent(activity, ShoppingCartActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));

            } else if (itemId == R.id.nav_shopping_sale) {
                User user = getCurrentUser(activity);
                if (BuildConfig.IS_SALES_FORCE_SYSTEM
                        || (user!=null && user.getUserProfileId()==UserProfile.SALES_MAN_PROFILE_ID)) {
                    activity.startActivity(new Intent(activity, ShoppingSaleActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                } else {
                    activity.startActivity(new Intent(activity, ShoppingSalesListActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                }

            } else if (itemId == R.id.nav_recommended_products_list) {
                activity.startActivity(new Intent(activity, RecommendedProductsListActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));

            } else if (itemId == R.id.nav_wish_list) {
                activity.startActivity(new Intent(activity, WishListActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));

            } else if (itemId == R.id.nav_orders) {
                activity.startActivity(new Intent(activity, OrdersListActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));

            } else if (itemId == R.id.nav_sales_orders) {
                activity.startActivity(new Intent(activity, SalesOrdersListActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));

            } else if (itemId == R.id.nav_data_sync_settings) {
                activity.startActivity(new Intent(activity, SettingsDataSync.class));

            } else if (itemId == R.id.nav_images_settings) {
                activity.startActivity(new Intent(activity, SettingsImagesManagement.class));

            } else if (itemId == R.id.nav_business_partners) {
                activity.startActivity(new Intent(activity, BusinessPartnersListActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));

            } else if (itemId == R.id.nav_share) {
                try {
                    showPromptShareApp(activity);
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            } else if (itemId == R.id.nav_my_company) {
                activity.startActivity(new Intent(activity, CompanyActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));

            } else if (itemId == R.id.nav_contact_us) {
                activity.startActivity(new Intent(activity, ContactUsActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));

            } else if (itemId == R.id.nav_report_error) {
                Intent contactUsEmailIntent = new Intent(Intent.ACTION_SEND);
                contactUsEmailIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                // need this to prompts email client only
                contactUsEmailIntent.setType("message/rfc822");
                contactUsEmailIntent.putExtra(Intent.EXTRA_EMAIL,
                        new String[]{Parameter.getReportErrorEmail(activity, getCurrentUser(activity))});

                activity.startActivity(Intent.createChooser(contactUsEmailIntent, activity.getString(R.string.send_error_report)));

            } else if (itemId == R.id.nav_prices_list) {
                activity.startActivity(new Intent(activity, PricesListActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));

            } else if (itemId == R.id.nav_orders_tracking) {
                activity.startActivity(new Intent(activity, OrdersTrackingListActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));

            } else if (itemId == R.id.nav_welcome_message) {
                activity.startActivity(new Intent(activity, WelcomeScreenSlideActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            } else if (itemId == R.id.nav_notifications_history_list) {
                activity.startActivity(new Intent(activity, NotificationsListActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            } else if (itemId == R.id.nav_chat) {
                User user = getCurrentUser(activity);
                if (user != null) {
                    if (user.getUserProfileId() == UserProfile.BUSINESS_PARTNER_PROFILE_ID) {
                        activity.startActivity(new Intent(activity, ChatMessagesActivity.class)
                                .putExtra(ChatMessagesActivity.KEY_CHAT_CONTACT_ID, new SalesRepDB(activity, user).getSalesRepId())
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    } else if (user.getUserProfileId() == UserProfile.SALES_MAN_PROFILE_ID) {
                        activity.startActivity(new Intent(activity, ChatContactsListActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void inflateNavigationView(NavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener,
                                             final Activity activity, final Toolbar toolbar, final User user) {
        DrawerLayout drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(activity, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view); //must call super
                Utils.loadNavigationViewBadge(activity, user,
                        (NavigationView) activity.findViewById(R.id.nav_view));
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) activity.findViewById(R.id.nav_view);
        if(navigationView!=null && user!=null){
            navigationView.inflateMenu(R.menu.drawer_menu);
            if (user.getUserProfileId() == UserProfile.SALES_MAN_PROFILE_ID) {
                navigationView.getMenu().findItem(R.id.nav_my_company).setVisible(false);
            }
            if (!Parameter.isActiveOrderTracking(activity, user) && navigationView.getMenu()!=null
                    && navigationView.getMenu().findItem(R.id.nav_orders_tracking)!=null) {
                navigationView.getMenu().findItem(R.id.nav_orders_tracking).setVisible(false);
            }
            if (!TextUtils.isEmpty(BuildConfig.SERVER_ADDRESS)) {
                navigationView.getMenu().findItem(R.id.nav_data_sync_settings).setVisible(false);
            }
            navigationView.getMenu().findItem(R.id.nav_chat)
                    .setVisible(Parameter.isRequestPriceAvailable(activity, user));
            navigationView.setNavigationItemSelectedListener(onNavigationItemSelectedListener);
            ((TextView) navigationView.getHeaderView(0).findViewById(R.id.user_name))
                    .setText(activity.getString(R.string.welcome_user, user.getUserName()));
        }
    }


    private static String imagesThumbFolderPath;
    public static String getImagesThumbFolderPath(Context context){
        if(imagesThumbFolderPath==null){
            imagesThumbFolderPath = context.getExternalFilesDir(null) + "/images/thumb/";
        }
        return imagesThumbFolderPath;
    }

    private static String imagesOriginalFolderPath;
    public static String getImagesOriginalFolderPath(Context context){
        if(imagesOriginalFolderPath==null){
            imagesOriginalFolderPath = context.getExternalFilesDir(null) + "/images/original/";
        }
        return imagesOriginalFolderPath;
    }

    private static String imagesBannerPortraitFolderPath;
    public static String getImagesBannerPortraitFolderPath(Context context){
        if(imagesBannerPortraitFolderPath==null){
            imagesBannerPortraitFolderPath = context.getExternalFilesDir(null) + "/images/banner/portrait/";
        }
        return imagesBannerPortraitFolderPath;
    }

    private static String imagesBannerLandscapeFolderPath;
    public static String getImagesBannerLandscapeFolderPath(Context context){
        if(imagesBannerLandscapeFolderPath==null){
            imagesBannerLandscapeFolderPath = context.getExternalFilesDir(null) + "/images/banner/landscape/";
        }
        return imagesBannerLandscapeFolderPath;
    }

    private static String imagesProductBrandPromotionalPortraitFolderPath;
    public static String getImagesProductBrandPromotionalPortraitFolderPath(Context context){
        if(imagesProductBrandPromotionalPortraitFolderPath==null){
            imagesProductBrandPromotionalPortraitFolderPath = context.getExternalFilesDir(null) +
                    "/images/productBrandPromotional/portrait/";
        }
        return imagesProductBrandPromotionalPortraitFolderPath;
    }

    private static String imagesProductBrandPromotionalLandscapeFolderPath;
    public static String getImagesProductBrandPromotionalLandscapeFolderPath(Context context){
        if(imagesProductBrandPromotionalLandscapeFolderPath==null){
            imagesProductBrandPromotionalLandscapeFolderPath = context.getExternalFilesDir(null) +
                    "/images/productBrandPromotional/landscape/";
        }
        return imagesProductBrandPromotionalLandscapeFolderPath;
    }

    public static String getImagesUserCompanyFolderPath(Context context, User user){
        return context.getExternalFilesDir(null) + File.separator + user.getUserGroup()
                + File.separator + user.getUserName() + "/images/userCompany/";
    }

    public static User getCurrentUser(Context context) {
        try {
            AccountManager accountManager = AccountManager.get(context);
            final Account availableAccounts[] = accountManager
                    .getAccountsByType(BuildConfig.AUTHENTICATOR_ACCOUNT_TYPE);
            if (availableAccounts.length > 0) {
                return ApplicationUtilities.getUserByIdFromAccountManager(context,
                        accountManager.getUserData(availableAccounts[0], AccountGeneral.USERDATA_USER_ID));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String getMacAddress (Context context) {
        String macAddress = null;
        try {
            macAddress = (((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
                    .getConnectionInfo()).getMacAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return macAddress!=null ? macAddress : "NOT AVAILABLE";
    }

    public static int getColor(Context context, int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.getColor(context, resId);
        } else {
            return context.getResources().getColor(resId);
        }
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static int convertDpToPixel(int dp, Context context){
        return dp * (context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static int convertPixelsToDp(int px, Context context){
        return px / (context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static void createPdfFileInDownloadFolder(final Context context, String sourceFilePath, String fileName){
        InputStream inStream;
        OutputStream outStream;
        try{
            File sourceFile = new File(sourceFilePath);
            File destinationFile =new File(Environment.getExternalStorageDirectory() +
                    File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + fileName);
            inStream = new FileInputStream(sourceFile);
            outStream = new FileOutputStream(destinationFile);
            byte[] buffer = new byte[1024];
            int length;
            //copy the file content in bytes
            while ((length = inStream.read(buffer)) > 0){
                outStream.write(buffer, 0, length);
            }
            inStream.close();
            outStream.close();
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_file_download_white_24dp)
                            .setContentTitle(context.getString(R.string.app_name))
                            .setContentText("El archivo se encuentra en \"Descargas\".")
                            .setContentInfo(fileName);
            // Creates an explicit intent for an Activity in your app
            final Intent resultIntent = new Intent(Intent.ACTION_VIEW);
            resultIntent.setDataAndType(Uri.fromFile(destinationFile), "application/pdf");
            //resultIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // Adds the back stack for the Intent (but not the Intent itself)
            //stackBuilder.addParentStack(ResultActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            mBuilder.setAutoCancel(true);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(1639, mBuilder.build());
            Toast toast = Toast.makeText(context, "Se creó el archivo "+fileName+" en la carpeta \"Descargas\"", Toast.LENGTH_SHORT);
            toast.getView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(resultIntent);
                }
            });
            toast.show();
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(context, "Hubo un error creando el archivo en la carpeta de \"Descargas\".", Toast.LENGTH_SHORT).show();
        }
    }

    private static Drawable noImageAvailable;
    public static Drawable getNoImageAvailableDrawable(Context context){
        if(noImageAvailable==null){
            try {
                noImageAvailable = context.getResources().getDrawable(R.drawable.no_image_available);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        return noImageAvailable;
    }

    private static Drawable loadingImage;
    public static Drawable getLoadingImageDrawable(Context context){
        if(loadingImage==null){
            try {
                loadingImage = context.getResources().getDrawable(R.drawable.loading_image);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        return loadingImage;
    }

    /**
     *
     * @param context
     */
    public static int getSyncPeriodicityFromPreferences(Context context){
        try {
            return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString("sync_periodicity",
                    context.getString(R.string.sync_periodicity_default_value)));
        } catch (Exception e) {
            return 3600;
        }
    }

    /**
     *
     * @param context
     * @param businessPartnerId
     */
    public static void setAppCurrentBusinessPartnerId(Context context, int businessPartnerId){
        try {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putInt(BusinessPartner.CURRENT_APP_BP_ID_SHARED_PREFS_KEY, businessPartnerId);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Devuelve el actual businessPartnerId que se está usando en la aplicacion
     * @param context
     * @param user
     * @return businessPartnerId
     * @throws Exception
     */
    public static int getAppCurrentBusinessPartnerId(Context context, User user) throws Exception {
        try {
            if (BuildConfig.IS_SALES_FORCE_SYSTEM || user.getUserProfileId() == UserProfile.SALES_MAN_PROFILE_ID) {
                if (!PreferenceManager.getDefaultSharedPreferences(context)
                        .contains(BusinessPartner.CURRENT_APP_BP_ID_SHARED_PREFS_KEY) ||
                        PreferenceManager.getDefaultSharedPreferences(context)
                                .getInt(BusinessPartner.CURRENT_APP_BP_ID_SHARED_PREFS_KEY, 0) == 0) {
                    setAppCurrentBusinessPartnerId(context, (new BusinessPartnerDB(context, user))
                            .getMaxActiveBusinessPartnerId());
                }
                return PreferenceManager.getDefaultSharedPreferences(context)
                        .getInt(BusinessPartner.CURRENT_APP_BP_ID_SHARED_PREFS_KEY, 0);
            } else if (user.getUserProfileId() == UserProfile.BUSINESS_PARTNER_PROFILE_ID) {
                //return user.getServerUserId();
                return (new BusinessPartnerDB(context, user)).getMaxActiveBusinessPartnerId();
            } else {
                throw new Exception("UserProfileId no identificado.");
            }
        } catch (Exception e) {
            throw new Exception("Error consultando el cliente. Message: " + e.getMessage());
        }
    }

    public static String getUrlScreenParameters(boolean mIsLandscape, Context context){
        try {
            String urlScreenParameters = mIsLandscape ? "&orientation=landscape" : "&orientation=portrait";
            switch (context.getResources().getDisplayMetrics().densityDpi) {
                case DisplayMetrics.DENSITY_LOW:    urlScreenParameters += "&screenPlayDensity=LOW";    break;
                case DisplayMetrics.DENSITY_MEDIUM: urlScreenParameters += "&screenPlayDensity=MEDIUM"; break;
                case DisplayMetrics.DENSITY_TV:     urlScreenParameters += "&screenPlayDensity=TV";     break;
                case DisplayMetrics.DENSITY_HIGH:   urlScreenParameters += "&screenPlayDensity=HIGH";   break;
                case DisplayMetrics.DENSITY_280:    urlScreenParameters += "&screenPlayDensity=280";    break;
                case DisplayMetrics.DENSITY_XHIGH:  urlScreenParameters += "&screenPlayDensity=XHIGH";  break;
                case DisplayMetrics.DENSITY_360:    urlScreenParameters += "&screenPlayDensity=360";    break;
                case DisplayMetrics.DENSITY_400:    urlScreenParameters += "&screenPlayDensity=400";    break;
                case DisplayMetrics.DENSITY_420:    urlScreenParameters += "&screenPlayDensity=420";    break;
                case DisplayMetrics.DENSITY_XXHIGH: urlScreenParameters += "&screenPlayDensity=XXHIGH"; break;
                case DisplayMetrics.DENSITY_560:    urlScreenParameters += "&screenPlayDensity=560";    break;
                case DisplayMetrics.DENSITY_XXXHIGH: urlScreenParameters += "&screenPlayDensity=XXXHIGH"; break;
                default: urlScreenParameters += "&screenPlayDensity="+context.getResources().getDisplayMetrics().densityDpi; break;
            }
            switch(context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
                case Configuration.SCREENLAYOUT_SIZE_LARGE: urlScreenParameters += "&screenSize=LARGE"; break;
                case Configuration.SCREENLAYOUT_SIZE_NORMAL: urlScreenParameters += "&screenSize=NORMAL"; break;
                case Configuration.SCREENLAYOUT_SIZE_SMALL: urlScreenParameters += "&screenSize=SMALL"; break;
                default: urlScreenParameters += "&screenSize="+(context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK); break;
            }
            urlScreenParameters += "&smallestWidth="+context.getString(R.string.smallest_width);
            return urlScreenParameters;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     *
     * @param context
     * @return
     */
    public static List<String> getListOfFilesInThumbDir(Context context) {
        return getListOfFilesByFolder(new File (getImagesThumbFolderPath(context)));
    }

    /**
     *
     * @param context
     * @return
     */
    public static List<String> getListOfFilesInOriginalDir(Context context) {
        return getListOfFilesByFolder(new File (getImagesOriginalFolderPath(context)));
    }

    /**
     * Devuelve una lista con los nombres de los archivos que se encuentran en el fichero
     * @param folder
     * @return
     */
    private static List<String> getListOfFilesByFolder(final File folder) {
        if (folder!=null && folder.listFiles()!=null) {
            List<String> filesName = new ArrayList<>();
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.isDirectory()) {
                    getListOfFilesByFolder(fileEntry);
                } else {
                    filesName.add(fileEntry.getName());
                }
            }
            return filesName;
        }
        return null;
    }

    public static void lockScreenOrientation(Activity activity) {
        if (activity!=null && activity.getResources()!=null && activity.getResources().getConfiguration()!=null) {
            if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            } else {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            }
        }

    }

    public static void unlockScreenOrientation(Activity activity) {
        if (activity!=null) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    public static boolean deleteThumbImagesFolder(Context context) {
        try {
            return delete(new File(Utils.getImagesThumbFolderPath(context)));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteOriginalImagesFolder(Context context) {
        try {
            return delete(new File(Utils.getImagesOriginalFolderPath(context)));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean delete(File fileOrFolder) {
        boolean result = true;
        if(fileOrFolder.isDirectory()) {
            for (File file : fileOrFolder.listFiles()) {
                result = result && delete(file);
            }
        }
        result = result && fileOrFolder.delete();
        return result;
    }

    private static long getSize(File file) {
        long size;
        if (file.isDirectory()) {
            size = 0;
            for (File child : file.listFiles()) {
                size += getSize(child);
            }
        } else {
            size = file.length();
        }
        return size;
    }

    public static String getFolderSize(File file) {
        long size = getSize(file);
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups))
                + " " + units[digitGroups];
    }

    private static boolean saveImagesInDevice(Context context){
        try {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean("save_images_in_device", true);
        } catch(Exception e) {
            return false;
        }
    }

    private static boolean saveOriginalImagesInDevice(Context context){
        try {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean("save_original_images_in_device", false);
        } catch(Exception e) {
            return false;
        }
    }

    public static Typeface getTypefaceMedium(Context context) {
        try {
            if (typefaceMedium==null) {
                typefaceMedium = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");
            }
            return typefaceMedium;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Deletes a directory tree recursively.
     */
    public static void deleteDirectoryTree(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteDirectoryTree(child);
            }
        }
        fileOrDirectory.delete();
    }
}
