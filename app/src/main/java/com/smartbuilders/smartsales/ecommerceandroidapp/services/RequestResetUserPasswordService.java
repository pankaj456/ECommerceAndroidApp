package com.smartbuilders.smartsales.ecommerceandroidapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.jasgcorp.ids.utils.ConsumeWebService;
import com.smartbuilders.smartsales.ecommerceandroidapp.RequestResetUserPasswordFragment;
import com.smartbuilders.smartsales.ecommerceandroidapp.RequestUserPasswordFragment;

import org.ksoap2.serialization.SoapPrimitive;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;

/**
 * Created by stein on 7/6/2016.
 */
public class RequestResetUserPasswordService extends IntentService {

    private static final String TAG = RequestResetUserPasswordService.class.getSimpleName();

    public static final String SERVER_ADDRESS = "SERVER_ADDRESS";
    public static final String USER_EMAIL = "USER_EMAIL";

    public RequestResetUserPasswordService() {
        super(RequestResetUserPasswordService.class.getSimpleName());
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public RequestResetUserPasswordService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        String serverAddress = workIntent.getStringExtra(SERVER_ADDRESS);
        String userEmail = workIntent.getStringExtra(USER_EMAIL);

        String resultMsg = null;
        try {
            LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
            parameters.put("userGroup", "catalogo-febeca");
            parameters.put("userEmail", userEmail);
            ConsumeWebService a = new ConsumeWebService(getApplicationContext(),
                    serverAddress,
                    "/IntelligentDataSynchronizer/services/ManageUser?wsdl",
                    "resetUserPassword",
                    "urn:resetUserPassword",
                    parameters);
            Object response =  a.getWSResponse();
            if(response instanceof SoapPrimitive){
                Log.d(TAG, "response: "+response.toString());
                //TODO: enviar broadcast a la aplicacion
            }else if (response != null){
                throw new ClassCastException("response classCastException.");
            }else if (response == null) {

            } else {
                throw new NullPointerException("response error.");
            }
        } catch(ConnectException e){
            e.printStackTrace();
        } catch(SocketTimeoutException e){
            e.printStackTrace();
        } catch(SocketException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(RequestResetUserPasswordFragment.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(RequestResetUserPasswordFragment.MESSAGE, resultMsg);
        sendBroadcast(broadcastIntent);
    }
}