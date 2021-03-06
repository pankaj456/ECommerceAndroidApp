package com.smartbuilders.synchronizer.ids.utils;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.content.Context;
import android.content.Intent;

import com.smartbuilders.smartsales.ecommerce.BuildConfig;

/**
 * before 27.02.2016
 * @author jsarco
 *
 */
public class ConsumeWebService {
	
	public static final String SHOW_TOAST_MESSAGE 	= BuildConfig.APPLICATION_ID + "." + ConsumeWebService.class.getSimpleName() + ".SHOW_TOAST_MESSAGE";
	public static final String MESSAGE 				= BuildConfig.APPLICATION_ID + "." + ConsumeWebService.class.getSimpleName() + ".MESSAGE";
	private static final String NAMESPACE 			= "http://webservices.ids.jasgcorp.com";
	private static final int MAX_RETRY_NUMBER 				= 3;
	private static final int HTTP_TRANSPORT_TIMEOUT 		= 30*1000;//in milliseconds
	
	//Constantes para la invocacion del web service
	private Context context;
	private String serverAddress;
	private String url;
	private String methodName;
	private String soapAction;
	private LinkedHashMap<String, Object> parameters;
	private int retryNumber;
	private int connectionTimeOut;
	private int maxRetryNumber = MAX_RETRY_NUMBER;

	public ConsumeWebService(Context context, String serverAddress, String url,
							 String methodName, String soapAction, LinkedHashMap<String, Object> parameters,
                             int connectionTimeOut, int maxRetryNumber){
		this(context, serverAddress, url, methodName, soapAction, parameters, connectionTimeOut);
		this.maxRetryNumber = maxRetryNumber;
	}

	public ConsumeWebService(Context context, String serverAddress, String url,
							 String methodName, String soapAction, LinkedHashMap<String, Object> parameters, int connectionTimeOut){
		this(context, serverAddress, url, methodName, soapAction, parameters);
		this.connectionTimeOut = connectionTimeOut;
		this.maxRetryNumber = MAX_RETRY_NUMBER;
	}

	public ConsumeWebService(Context context, String serverAddress, String url, 
			String methodName, String soapAction, LinkedHashMap<String, Object> parameters){
		this.context 		= context;
		this.serverAddress 	= serverAddress;
		this.url 			= url;
		this.methodName 	= methodName;
		this.soapAction 	= soapAction;
		this.parameters 	= parameters;
		this.maxRetryNumber = MAX_RETRY_NUMBER;
	}
	
	public Object getWSResponse() throws Exception {
		SoapObject request;
		SoapSerializationEnvelope envelope;
		//Objeto que representa el modelo de transporte
		//Recibe la URL del ws
		HttpTransportSE transporte = new HttpTransportSE(serverAddress+url,
				connectionTimeOut>0 ? connectionTimeOut : HTTP_TRANSPORT_TIMEOUT);
		Object response;
		try {
			//Hace la llamada al ws

			//Se crea un objeto SoapObject para poder realizar la peticion
			//para consumir el ws SOAP. El constructor recibe
			//el namespace. Por lo regular el namespace es el dominio
			//donde se encuentra el web service
			request = new SoapObject(NAMESPACE, methodName);
			for(String parameterName : this.parameters.keySet()){
				request.addProperty(parameterName, this.parameters.get(parameterName)); // Paso parametros al WS
			}

			//Se crea un objeto SoapSerializationEnvelope para serealizar la
			//peticion SOAP y permitir viajar el mensaje por la nube
			//el constructor recibe la version de SOAP
			envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true; //se asigna true para el caso de que el WS sea de dotNet
			
			//Se envuelve la peticion soap
			envelope.setOutputSoapObject(request);
			transporte.call(soapAction, envelope);
			response = envelope.getResponse();
        } catch(ConnectException e){
			return retry(e);
        } catch(SocketTimeoutException e){
			return retry(e);
        } catch(SocketException e){
			return retry(e);
        } catch(Exception e){
			throw e;
		}
		return response;
	}

	private Object retry(Exception exception) throws Exception{
    	if(++retryNumber <= maxRetryNumber){
    		try {
    			context.sendBroadcast((new Intent(SHOW_TOAST_MESSAGE))
    					.putExtra(MESSAGE, "Failed to communicate with server, retry in "+((2*retryNumber)*1000)+" milliseconds."));
                //Se incrementa el tiempo de timeOut
				connectionTimeOut += connectionTimeOut/3;
    		    Thread.sleep((2*retryNumber)*1000);
    		} catch(InterruptedException ex) {
    		    Thread.currentThread().interrupt();
    		}
    		return getWSResponse();
    	}
    	throw exception;
	}
}
