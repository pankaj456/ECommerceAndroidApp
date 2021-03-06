package com.smartbuilders.smartsales.ecommerce.data;

import android.content.Context;
import android.database.Cursor;

import com.smartbuilders.synchronizer.ids.model.User;
import com.smartbuilders.synchronizer.ids.providers.DataBaseContentProvider;
import com.smartbuilders.smartsales.ecommerce.model.Company;
import com.smartbuilders.smartsales.ecommerce.utils.DateFormat;
import com.smartbuilders.smartsales.ecommerce.utils.Utils;

/**
 * Created by stein on 5/6/2016.
 */
public class UserCompanyDB {

    private Context mContext;
    private User mUser;

    public UserCompanyDB(Context context, User user){
        this.mContext = context;
        this.mUser = user;
    }

    public String insertUserCompany(Company company) {
        try {
            int rowsAffected = mContext.getContentResolver()
                    .update(DataBaseContentProvider.INTERNAL_DB_URI.buildUpon()
                            .appendQueryParameter(DataBaseContentProvider.KEY_USER_ID, mUser.getUserId())
                            .appendQueryParameter(DataBaseContentProvider.KEY_SEND_DATA_TO_SERVER, String.valueOf(Boolean.TRUE)).build(),
                    null,
                    "insert into user_company (USER_ID, NAME, COMMERCIAL_NAME, TAX_ID, " +
                        " ADDRESS, CONTACT_PERSON, EMAIL_ADDRESS, PHONE_NUMBER, CREATE_TIME, APP_VERSION, APP_USER_NAME, DEVICE_MAC_ADDRESS) " +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ",
                    new String[]{String.valueOf(mUser.getServerUserId()) ,company.getName(),
                            company.getCommercialName(), company.getTaxId(), company.getAddress(),
                            company.getContactPerson(), company.getEmailAddress(), company.getPhoneNumber(),
                            DateFormat.getCurrentDateTimeSQLFormat(),
                            Utils.getAppVersionName(mContext), mUser.getUserName(), Utils.getMacAddress(mContext)});
            if (rowsAffected <= 0){
                return "No se insertó o actualizó el registro en la base de datos.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return null;
    }

    public String updateUserCompany(Company company) {
        try {
            int rowsAffected = mContext.getContentResolver()
                    .update(DataBaseContentProvider.INTERNAL_DB_URI.buildUpon()
                            .appendQueryParameter(DataBaseContentProvider.KEY_USER_ID, mUser.getUserId())
                            .appendQueryParameter(DataBaseContentProvider.KEY_SEND_DATA_TO_SERVER, String.valueOf(Boolean.TRUE)).build(),
                            null,
                            "UPDATE user_company SET NAME=?, COMMERCIAL_NAME=?, TAX_ID=?, ADDRESS=?, " +
                                    " CONTACT_PERSON=?, EMAIL_ADDRESS=?, PHONE_NUMBER=?, APP_VERSION=?, " +
                                    " APP_USER_NAME=?, DEVICE_MAC_ADDRESS=?, SEQUENCE_ID = 0 " +
                            " WHERE USER_ID = ? ",
                            new String[]{company.getName(), company.getCommercialName(), company.getTaxId(), company.getAddress(),
                                    company.getContactPerson(), company.getEmailAddress(), company.getPhoneNumber(),
                                    Utils.getAppVersionName(mContext), mUser.getUserName(), Utils.getMacAddress(mContext),
                                    String.valueOf(mUser.getServerUserId())});
            if (rowsAffected <= 0){
                return "No se insertó o actualizó el registro en la base de datos.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return null;
    }

    public Company getUserCompany(){
        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(DataBaseContentProvider.INTERNAL_DB_URI.buildUpon()
                    .appendQueryParameter(DataBaseContentProvider.KEY_USER_ID, mUser.getUserId())
                    .build(), null,
                    "select NAME, COMMERCIAL_NAME, TAX_ID, ADDRESS, CONTACT_PERSON, " +
                        " EMAIL_ADDRESS, PHONE_NUMBER " +
                    " from USER_COMPANY " +
                    " where USER_ID = ? AND IS_ACTIVE = ?",
                    new String[]{String.valueOf(mUser.getServerUserId()), "Y"}, null);
            if(c!=null && c.moveToNext()){
                Company company = new Company();
                company.setName(c.getString(0));
                company.setCommercialName(c.getString(1));
                company.setTaxId(c.getString(2));
                company.setAddress(c.getString(3));
                company.setContactPerson(c.getString(4));
                company.setEmailAddress(c.getString(5));
                company.setPhoneNumber(c.getString(6));
                return company;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(c!=null){
                try {
                    c.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
