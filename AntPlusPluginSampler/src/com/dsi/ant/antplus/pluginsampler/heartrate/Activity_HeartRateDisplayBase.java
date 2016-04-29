/*
This software is subject to the license described in the License.txt file
included with this software distribution. You may not use this file except in compliance
with this license.

Copyright (c) Dynastream Innovations Inc. 2013
All rights reserved.
 */

package com.dsi.ant.antplus.pluginsampler.heartrate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dsi.ant.antplus.pluginsampler.R;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.DataState;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.ICalculatedRrIntervalReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.IHeartRateDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.IPage4AddtDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.RrFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc.IRssiReceiver;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IDeviceStateChangeReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IPluginAccessResultReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusLegacyCommonPcc.ICumulativeOperatingTimeReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusLegacyCommonPcc.IManufacturerAndSerialReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusLegacyCommonPcc.IVersionAndModelReceiver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Base class to connects to Heart Rate Plugin and display all the event data.
 */
public abstract class Activity_HeartRateDisplayBase extends Activity
{
    protected abstract void requestAccessToPcc();

    AntPlusHeartRatePcc hrPcc = null;
    protected PccReleaseHandle<AntPlusHeartRatePcc> releaseHandle = null;

    TextView tv_status;

    TextView tv_estTimestamp;

    TextView tv_rssi;

    TextView tv_computedHeartRate;
    TextView tv_heartBeatCounter;
    TextView tv_heartBeatEventTime;

    TextView tv_manufacturerSpecificByte;
    TextView tv_previousHeartBeatEventTime;

    TextView tv_calculatedRrInterval;

    TextView tv_cumulativeOperatingTime;

    TextView tv_manufacturerID;
    TextView tv_serialNumber;

    TextView tv_hardwareVersion;
    TextView tv_softwareVersion;
    TextView tv_modelNumber;

    TextView tv_dataStatus;
    TextView tv_rrFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        handleReset();
    }

    /**
     * Resets the PCC connection to request access again and clears any existing display data.
     */
    protected void handleReset()
    {
        //Release the old access if it exists
        if(releaseHandle != null)
        {
            releaseHandle.close();
        }

        requestAccessToPcc();
    }

    protected void showDataDisplay(String status)
    {
        setContentView(R.layout.activity_heart_rate);

        
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        
        
        tv_status = (TextView)findViewById(R.id.textView_Status);

        tv_estTimestamp = (TextView)findViewById(R.id.textView_EstTimestamp);

        tv_rssi = (TextView)findViewById(R.id.textView_Rssi);

        tv_computedHeartRate = (TextView)findViewById(R.id.textView_ComputedHeartRate);
        tv_heartBeatCounter = (TextView)findViewById(R.id.textView_HeartBeatCounter);
        tv_heartBeatEventTime = (TextView)findViewById(R.id.textView_HeartBeatEventTime);

        tv_manufacturerSpecificByte = (TextView)findViewById(R.id.textView_ManufacturerSpecificByte);
        tv_previousHeartBeatEventTime = (TextView)findViewById(R.id.textView_PreviousHeartBeatEventTime);

        tv_calculatedRrInterval = (TextView)findViewById(R.id.textView_CalculatedRrInterval);

        tv_cumulativeOperatingTime = (TextView)findViewById(R.id.textView_CumulativeOperatingTime);

        tv_manufacturerID = (TextView)findViewById(R.id.textView_ManufacturerID);
        tv_serialNumber = (TextView)findViewById(R.id.textView_SerialNumber);

        tv_hardwareVersion = (TextView)findViewById(R.id.textView_HardwareVersion);
        tv_softwareVersion = (TextView)findViewById(R.id.textView_SoftwareVersion);
        tv_modelNumber = (TextView)findViewById(R.id.textView_ModelNumber);

        tv_dataStatus = (TextView)findViewById(R.id.textView_DataStatus);
        tv_rrFlag = (TextView)findViewById(R.id.textView_rRFlag);

        //Reset the text display
        tv_status.setText(status);

        tv_estTimestamp.setText("---");

        tv_rssi.setText("---");

        tv_computedHeartRate.setText("---");
        tv_heartBeatCounter.setText("---");
        tv_heartBeatEventTime.setText("---");

        tv_manufacturerSpecificByte.setText("---");
        tv_previousHeartBeatEventTime.setText("---");

        tv_calculatedRrInterval.setText("---");

        tv_cumulativeOperatingTime.setText("---");

        tv_manufacturerID.setText("---");
        tv_serialNumber.setText("---");

        tv_hardwareVersion.setText("---");
        tv_softwareVersion.setText("---");
        tv_modelNumber.setText("---");
        tv_dataStatus.setText("---");
        tv_rrFlag.setText("---");
        
        
/*        Log.i("CallAPI", "CallAPI- Before API Call");
        new SendHttpRequestTask().execute();	
*/    }

    /**
     * Switches the active view to the data display and subscribes to all the data events
     */
    public void subscribeToHrEvents()
    {
        hrPcc.subscribeHeartRateDataEvent(new IHeartRateDataReceiver()
        {
            @Override
            public void onNewHeartRateData(final long estTimestamp, EnumSet<EventFlag> eventFlags,
                final int computedHeartRate, final long heartBeatCount,
                final BigDecimal heartBeatEventTime, final DataState dataState)
            {
                // Mark heart rate with asterisk if zero detected
                final String textHeartRate = String.valueOf(computedHeartRate)
                    + ((DataState.ZERO_DETECTED.equals(dataState)) ? "*" : "");

                // Mark heart beat count and heart beat event time with asterisk if initial value
                final String textHeartBeatCount = String.valueOf(heartBeatCount)
                    + ((DataState.INITIAL_VALUE.equals(dataState)) ? "*" : "");
                final String textHeartBeatEventTime = String.valueOf(heartBeatEventTime)
                    + ((DataState.INITIAL_VALUE.equals(dataState)) ? "*" : "");

                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        tv_estTimestamp.setText(String.valueOf(estTimestamp));

                        tv_computedHeartRate.setText(textHeartRate);
                        tv_heartBeatCounter.setText(textHeartBeatCount);
                        tv_heartBeatEventTime.setText(textHeartBeatEventTime);

                        tv_dataStatus.setText(dataState.toString());
                        Log.i("CallAPI", "CallAPI- Before API Call");
                        new SendHttpRequestTask().runapi();	
                    }
                });
            }
        });

        hrPcc.subscribePage4AddtDataEvent(new IPage4AddtDataReceiver()
        {
            @Override
            public void onNewPage4AddtData(final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                final int manufacturerSpecificByte,
                final BigDecimal previousHeartBeatEventTime)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        tv_estTimestamp.setText(String.valueOf(estTimestamp));

                        tv_manufacturerSpecificByte.setText(String.format("0x%02X", manufacturerSpecificByte));
                        tv_previousHeartBeatEventTime.setText(String.valueOf(previousHeartBeatEventTime));
                    }
                });
            }
        });

        hrPcc.subscribeCumulativeOperatingTimeEvent(new ICumulativeOperatingTimeReceiver()
        {
            @Override
            public void onNewCumulativeOperatingTime(final long estTimestamp, final EnumSet<EventFlag> eventFlags, final long cumulativeOperatingTime)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        tv_estTimestamp.setText(String.valueOf(estTimestamp));

                        tv_cumulativeOperatingTime.setText(String.valueOf(cumulativeOperatingTime));
                    }
                });
            }
        });

        hrPcc.subscribeManufacturerAndSerialEvent(new IManufacturerAndSerialReceiver()
        {
            @Override
            public void onNewManufacturerAndSerial(final long estTimestamp, final EnumSet<EventFlag> eventFlags, final int manufacturerID,
                final int serialNumber)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        tv_estTimestamp.setText(String.valueOf(estTimestamp));

                        tv_manufacturerID.setText(String.valueOf(manufacturerID));
                        tv_serialNumber.setText(String.valueOf(serialNumber));
                    }
                });
            }
        });

        hrPcc.subscribeVersionAndModelEvent(new IVersionAndModelReceiver()
        {
            @Override
            public void onNewVersionAndModel(final long estTimestamp, final EnumSet<EventFlag> eventFlags, final int hardwareVersion,
                final int softwareVersion, final int modelNumber)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        tv_estTimestamp.setText(String.valueOf(estTimestamp));
                        tv_hardwareVersion.setText(String.valueOf(hardwareVersion));
                        tv_softwareVersion.setText(String.valueOf(softwareVersion));
                        tv_modelNumber.setText(String.valueOf(modelNumber));
                    }
                });
            }
        });

        hrPcc.subscribeCalculatedRrIntervalEvent(new ICalculatedRrIntervalReceiver()
        {
            @Override
            public void onNewCalculatedRrInterval(final long estTimestamp,
                EnumSet<EventFlag> eventFlags, final BigDecimal rrInterval, final RrFlag flag)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        tv_estTimestamp.setText(String.valueOf(estTimestamp));
                        tv_rrFlag.setText(flag.toString());

                        // Mark RR with asterisk if source is not cached or page 4
                        if (flag.equals(RrFlag.DATA_SOURCE_CACHED)
                            || flag.equals(RrFlag.DATA_SOURCE_PAGE_4))
                            tv_calculatedRrInterval.setText(String.valueOf(rrInterval));
                        else
                            tv_calculatedRrInterval.setText(String.valueOf(rrInterval) + "*");
                    }
                });
            }
        });

        hrPcc.subscribeRssiEvent(new IRssiReceiver() {
            @Override
            public void onRssiData(final long estTimestamp, final EnumSet<EventFlag> evtFlags, final int rssi) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_estTimestamp.setText(String.valueOf(estTimestamp));
                        tv_rssi.setText(String.valueOf(rssi) + " dBm");
                    }
                });
            }
        });
    }

    protected IPluginAccessResultReceiver<AntPlusHeartRatePcc> base_IPluginAccessResultReceiver =
        new IPluginAccessResultReceiver<AntPlusHeartRatePcc>()
        {
        //Handle the result, connecting to events on success or reporting failure to user.
        @Override
        public void onResultReceived(AntPlusHeartRatePcc result, RequestAccessResult resultCode,
            DeviceState initialDeviceState)
        {
            showDataDisplay("Connecting...");
            switch(resultCode)
            {
                case SUCCESS:
                    hrPcc = result;
                    tv_status.setText(result.getDeviceName() + ": " + initialDeviceState);
                    subscribeToHrEvents();
                    if(!result.supportsRssi()) tv_rssi.setText("N/A");
                    break;
                case CHANNEL_NOT_AVAILABLE:
                    Toast.makeText(Activity_HeartRateDisplayBase.this, "Channel Not Available", Toast.LENGTH_SHORT).show();
                    tv_status.setText("Error. Do Menu->Reset.");
                    break;
                case ADAPTER_NOT_DETECTED:
                    Toast.makeText(Activity_HeartRateDisplayBase.this, "ANT Adapter Not Available. Built-in ANT hardware or external adapter required.", Toast.LENGTH_SHORT).show();
                    tv_status.setText("Error. Do Menu->Reset.");
                    break;
                case BAD_PARAMS:
                    //Note: Since we compose all the params ourself, we should never see this result
                    Toast.makeText(Activity_HeartRateDisplayBase.this, "Bad request parameters.", Toast.LENGTH_SHORT).show();
                    tv_status.setText("Error. Do Menu->Reset.");
                    break;
                case OTHER_FAILURE:
                    Toast.makeText(Activity_HeartRateDisplayBase.this, "RequestAccess failed. See logcat for details.", Toast.LENGTH_SHORT).show();
                    tv_status.setText("Error. Do Menu->Reset.");
                    break;
                case DEPENDENCY_NOT_INSTALLED:
                    tv_status.setText("Error. Do Menu->Reset.");
                    AlertDialog.Builder adlgBldr = new AlertDialog.Builder(Activity_HeartRateDisplayBase.this);
                    adlgBldr.setTitle("Missing Dependency");
                    adlgBldr.setMessage("The required service\n\"" + AntPlusHeartRatePcc.getMissingDependencyName() + "\"\n was not found. You need to install the ANT+ Plugins service or you may need to update your existing version if you already have it. Do you want to launch the Play Store to get it?");
                    adlgBldr.setCancelable(true);
                    adlgBldr.setPositiveButton("Go to Store", new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Intent startStore = null;
                            startStore = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=" + AntPlusHeartRatePcc.getMissingDependencyPackageName()));
                            startStore.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            Activity_HeartRateDisplayBase.this.startActivity(startStore);
                        }
                    });
                    adlgBldr.setNegativeButton("Cancel", new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    });

                    final AlertDialog waitDialog = adlgBldr.create();
                    waitDialog.show();
                    break;
                case USER_CANCELLED:
                    tv_status.setText("Cancelled. Do Menu->Reset.");
                    break;
                case UNRECOGNIZED:
                    Toast.makeText(Activity_HeartRateDisplayBase.this,
                        "Failed: UNRECOGNIZED. PluginLib Upgrade Required?",
                        Toast.LENGTH_SHORT).show();
                    tv_status.setText("Error. Do Menu->Reset.");
                    break;
                default:
                    Toast.makeText(Activity_HeartRateDisplayBase.this, "Unrecognized result: " + resultCode, Toast.LENGTH_SHORT).show();
                    tv_status.setText("Error. Do Menu->Reset.");
                    break;
            }
        }
        };

        //Receives state changes and shows it on the status display line
        protected  IDeviceStateChangeReceiver base_IDeviceStateChangeReceiver =
            new IDeviceStateChangeReceiver()
        {
            @Override
            public void onDeviceStateChange(final DeviceState newDeviceState)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        tv_status.setText(hrPcc.getDeviceName() + ": " + newDeviceState);
                    }
                });


            }
        };

        @Override
        protected void onDestroy()
        {
            if(releaseHandle != null)
            {
                releaseHandle.close();
            }
            super.onDestroy();
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu)
        {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.activity_heart_rate, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item)
        {
            switch(item.getItemId())
            {
                case R.id.menu_reset:
                    handleReset();
                    tv_status.setText("Resetting...");
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        private class SendHttpRequestTask extends AsyncTask<String, Void, String> {
        	   
        	  protected String doInBackground(String... params) {
        	   String url = "http://192.168.43.10:8080/om2m/gscl/applications/MY_SENSOR/containers/DATA/contentInstances?format=JSON"; // URL to call;
        	   String data="";      	   
        	   JSONObject dato = new JSONObject();
        	   Log.i("CallAPI", "CallAPI -Inside doinbackgroound");
 				try {
				  dato.put("personid",1);
 	              dato.put("timevalue","18:39:50");
 	              dato.put("datatype","HB");
 	              dato.put("datavalue", tv_computedHeartRate.getText().toString());
 	              dato.put("longitude",100.90);
 	              dato.put("latitude",90.60);
 	        	  String name = dato.toString() ;
 	        	  
 	        	   Log.i("CallAPI","CallAPI - " +name);
 	        	   Log.i("CallAPI", "CallAPI -before call");
				  
 	        	   data = sendHttpRequest(url, name);
				 
 	        	   Log.i("CallAPI", "CallAPI - after call");				  
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
 				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	return data;
	          }
        		
        	  public void runapi()
        	  {
        		  doInBackground();  
        	  }
        	  
        	  private String sendHttpRequest(String url, String name) throws MalformedURLException, IOException {
        		  
        		  Log.i("CallAPI", "CallAPI =" + url);
        		  
        		  HttpURLConnection con = (HttpURLConnection) ( new URL(url)).openConnection();
        		  
        		  try{
        			
	        		  con.setRequestProperty("Authorization", "Basic YWRtaW46YWRtaW4=");
	        		  con.setRequestProperty("Content-Type", "application/json");
	        		  //con.setDoOutput(true);
	        		  con.setRequestMethod("POST");
	        		  OutputStream out = new BufferedOutputStream(con.getOutputStream());
	        		  out.write(name.getBytes());
	        		  out.close();
	        		  
	        		  InputStream is = con.getInputStream();
	        		  BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	        		  String line = null;
	        		  StringBuffer sb = new StringBuffer();
	        		  while ((line = reader.readLine()) != null) {
	        		      sb.append(line);
	        		  }
	        		  is.close();
	        		  String response = sb.toString();	        		  
	        		  

        		  }
        		  finally{
        			  con.disconnect();
        		  }

				return name;
        	  }

        	  protected void onPostExecute(String result) {
				Log.i("CallAPI", "DONE!!!!!!");
        	  }
         }

        
}     /*

        private class SendHttpRequestTask extends AsyncTask<String, Void, String>{
        	   
        	  @Override
        	  protected String doInBackground(String... params) {
        	   String url = "http://192.168.43.84:8080/om2m/gscl/applications/MY_SENSOR/containers/DATA/contentInstances?format=JSON"; // URL to call;
        	   String data="";      	   
        	   JSONObject dato = new JSONObject();
        	   Log.i("CallAPI", "CallAPI -Inside doinbackgroound");
 				try {
				  dato.put("personid",1);
 	              dato.put("timevalue","18:39:50");
 	              dato.put("datatype","HB");
 	              dato.put("datavalue", tv_computedHeartRate.getText().toString());
 	              dato.put("longitude",100.90);
 	              dato.put("latitude",90.60);
 	        	  String name = dato.toString() ;
 	        	  
 	        	   Log.i("CallAPI","CallAPI - " +name);
 	        	   Log.i("CallAPI", "CallAPI -before call");
				  
 	        	   data = sendHttpRequest(url, name);
				 
 	        	   Log.i("CallAPI", "CallAPI - after call");				  
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
 				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	return data;
	          }
        		
        	  public void runapi()
        	  {
        		  doInBackground();  
        	  }
        	  
        	  private String sendHttpRequest(String url, String name) throws MalformedURLException, IOException {
        		  Log.i("CallAPI", "CallAPI =" + url);
        		  HttpURLConnection con = (HttpURLConnection) ( new URL(url)).openConnection();
        		  con.setRequestMethod("POST");
        		  con.addRequestProperty("data", name);
        		  con.setDoInput(true);
        		  con.setDoOutput(true);
        		  con.connect();
        		  con.getOutputStream().write( ("name=" + name).getBytes());
				return name;
        	  }

        	  @Override
        	  protected void onPostExecute(String result) {
				Log.i("CallAPI", "DONE!!!!!!");
        	  }
         }

*/
        
    	/*private class CallAPI extends AsyncTask<String, String, String> {
    		
    		@SuppressWarnings("deprecation")
			@Override
    	    protected String doInBackground(String... params) {
    	      
    		String urlString="http:// 192.168.43.84:8080/om2m/gscl/applications/MY_SENSOR/containers/DATA/contentInstances?format=JSON"; // URL to call
    	    String resultToDisplay = "";

    	      Log.i("CallAPI", "Before setting httclient ");

              @SuppressWarnings("deprecation")
			HttpClient httpClient = new DefaultHttpClient();
              
              HttpPost post = new HttpPost(urlString);
              post.setHeader("content-type", "application/json; charset=UTF-8");

              Log.i("CallAPI", "After setting httclient and post header ");
              JSONObject dato = new JSONObject();
              try {
				dato.put("personid",1);
	              dato.put("timevalue","18:39:50");
	              dato.put("datatype","HB");
	              dato.put("datavalue", tv_computedHeartRate.getText().toString());
	              dato.put("longitude",100.90);
	              dato.put("latitude",90.60);
  			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
              
            @SuppressWarnings("deprecation")
			StringEntity entity;
	
             @SuppressWarnings("deprecation")
			HttpResponse resp;
			try {
				entity = new StringEntity(dato.toString());
				Log.i("CallAPI", post.getAllHeaders().toString());
                post.setEntity(entity);
				resp = httpClient.execute(post);

				@SuppressWarnings("deprecation")
				String respStr = EntityUtils.toString(resp.getEntity());
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
              System.out.println("OKAY!");
              return "Done";
    	    }
			
    	}

    	} // end CallAPI
*/