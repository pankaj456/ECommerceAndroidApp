package com.smartbuilders.smartsales.ecommerceandroidapp.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.jasgcorp.ids.model.User;
import com.smartbuilders.smartsales.ecommerceandroidapp.R;
import com.smartbuilders.smartsales.ecommerceandroidapp.model.Product;
import com.smartbuilders.smartsales.ecommerceandroidapp.model.ProductCategory;
import com.smartbuilders.smartsales.ecommerceandroidapp.model.ProductSubCategory;
import com.smartbuilders.smartsales.ecommerceandroidapp.providers.CachedFileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by Alberto on 26/3/2016.
 */
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    public static ArrayList<Product> getGenericProductsList(int iterations){
        ArrayList<Product> products = new ArrayList<>();
        Product p;
        ProductCategory productCategory = new ProductCategory();
        productCategory.setName("Nombre del grupo");
        ProductSubCategory productSubCategory = new ProductSubCategory();
        productSubCategory.setName("Nombre de la partida");
        for(int i = 0; i<iterations; i++) {
            p = new Product();
            p.setName("Bomba 1/2 hp periferica pedrollo");
            p.setImageId(R.drawable.product1);
            p.setProductCategory(productCategory);
            p.setProductSubCategory(productSubCategory);
            products.add(p);

            p = new Product();
            p.setName("Capacitador con terminal p/bomba 1/2hp");
            p.setImageId(R.drawable.product2);
            p.setProductCategory(productCategory);
            p.setProductSubCategory(productSubCategory);
            products.add(p);

            p = new Product();
            p.setName("Capacitor 25uf semilic");
            p.setImageId(R.drawable.product3);
            p.setProductCategory(productCategory);
            p.setProductSubCategory(productSubCategory);
            products.add(p);

            p = new Product();
            p.setName("Cargador de aire 100gl tm");
            p.setImageId(R.drawable.product4);
            p.setProductCategory(productCategory);
            p.setProductSubCategory(productSubCategory);
            products.add(p);

            p = new Product();
            p.setName("Manometro 0-90psi semilic");
            p.setImageId(R.drawable.product5);
            p.setProductCategory(productCategory);
            p.setProductSubCategory(productSubCategory);
            products.add(p);

            p = new Product();
            p.setName("Mini presostato 20-40 semilic");
            p.setImageId(R.drawable.product6);
            p.setProductCategory(productCategory);
            p.setProductSubCategory(productSubCategory);
            products.add(p);

            p = new Product();
            p.setName("Presostato 20-40 semilic");
            p.setImageId(R.drawable.product7);
            p.setProductCategory(productCategory);
            p.setProductSubCategory(productSubCategory);
            products.add(p);

            p = new Product();
            p.setName("Rolinera para bomba 1/2hp");
            p.setImageId(R.drawable.product8);
            p.setProductCategory(productCategory);
            p.setProductSubCategory(productSubCategory);
            products.add(p);

            p = new Product();
            p.setName("Aspersor pico blanco 3/16\" agroinplast");
            p.setImageId(R.drawable.product9);
            p.setProductCategory(productCategory);
            p.setProductSubCategory(productSubCategory);
            products.add(p);

            p = new Product();
            p.setName("Aspersor oscilante bv");
            p.setImageId(R.drawable.product10);
            p.setProductCategory(productCategory);
            p.setProductSubCategory(productSubCategory);
            products.add(p);

            p = new Product();
            p.setName("Aspersor plastic triple bv");
            p.setImageId(R.drawable.product11);
            p.setProductCategory(productCategory);
            p.setProductSubCategory(productSubCategory);
            products.add(p);

            p = new Product();
            p.setName("Aspersor plastico triple chesterwood");
            p.setImageId(R.drawable.product12);
            p.setProductCategory(productCategory);
            p.setProductSubCategory(productSubCategory);
            products.add(p);
        }
        if(iterations==1){
            Collections.shuffle(products, new Random(System.nanoTime()));
        }
        return products;
    }

    /**
     *
     * @return
     */
    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
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
     * @param ctx
     * @param product
     * @param fileName
     * @return
     */
    public static Intent createShareProductIntent(Context ctx, Product product, String fileName){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_TEXT, product.getName() + " - "
                + "http://www.febeca.com:8080/products/compra/");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://"
                + CachedFileProvider.AUTHORITY + "/" + fileName));
        return shareIntent;
    }

    /**
     *
     * @param fileName
     * @param resId
     * @param ctx
     */
    public static void createFileInCacheDir(String fileName, int resId, Context ctx){
        //check if external storage is available so that we can dump our PDF file there
        if (!Utils.isExternalStorageAvailable() || Utils.isExternalStorageReadOnly()) {
            Toast.makeText(ctx, ctx.getString(R.string.external_storage_unavailable), Toast.LENGTH_LONG).show();
        } else {
            //path for the image file in the external storage
            File imageFile = new File(ctx.getCacheDir() + File.separator + fileName);
            try {
                imageFile.createNewFile();
                FileOutputStream fo = new FileOutputStream(imageFile);
                Bitmap icon = BitmapFactory.decodeResource(ctx.getResources(), resId);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                fo.write(bytes.toByteArray());
            } catch (IOException e1) {
                e1.printStackTrace();
                Toast.makeText(ctx, e1.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     *
     * @param fileName
     * @param resId
     * @param ctx
     */
    public static void createFileInCacheDir(String fileName, Bitmap image, Context ctx){
        //check if external storage is available so that we can dump our PDF file there
        if (!Utils.isExternalStorageAvailable() || Utils.isExternalStorageReadOnly()) {
            Toast.makeText(ctx, ctx.getString(R.string.external_storage_unavailable), Toast.LENGTH_LONG).show();
        } else {
            //path for the image file in the external storage
            File imageFile = new File(ctx.getCacheDir() + File.separator + fileName);
            try {
                imageFile.createNewFile();
                FileOutputStream fo = new FileOutputStream(imageFile);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                fo.write(bytes.toByteArray());
            } catch (IOException e1) {
                e1.printStackTrace();
                Toast.makeText(ctx, e1.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public static Bitmap getImageByFileName(Context context, User user, String fileName){
        File folder = new File(context.getExternalFilesDir(null)+"/"+user.getUserGroup()+"/"+user.getUserName()+"/Data_In/");//-->Android/data/package.name/files/...
        // if the directory does not exist, create it
        if (!folder.exists()) {
            try {
                if (!folder.mkdirs()) {
                    Log.w(TAG, "Failed to create folder: " + folder.getPath() + ".");
                }
            } catch (SecurityException se) {
                se.printStackTrace();
            }
        }
        File imgFile = new  File(context.getExternalFilesDir(null)+"/"+user.getUserGroup()+"/"+user.getUserName()+"/Data_In/"+fileName);
        if(imgFile.exists()){
            return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        }
        return null;
    }

}
