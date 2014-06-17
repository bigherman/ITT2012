package dk.bigherman.android.pisviewer;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.SQLException;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;

import android.support.v4.app.FragmentActivity;
 
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import dk.bigherman.android.pisviewer.Airfield;
import dk.bigherman.android.pisviewer.DataBaseHelper;

public class MainActivity extends FragmentActivity 
{
	GoogleMap gMap;
	String serverIP = "";
	//private enum Colour{BLU, WHT, GRN, YLO, AMB, RED, BLK, NIL};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		Log.i("mine", "Creating Main...");
		
		if(android.os.Build.VERSION.SDK_INT > 9)
		{
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		
		serverIP = getResources().getString(R.string.server_ip);
		
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		final EditText icaoText = (EditText) findViewById(R.id.edit_icao);
		icaoText.setBackgroundColor(0x22FFFFFF);
	    /*icaoText.addTextChangedListener(new TextWatcher()
	    {
	        public void afterTextChanged(Editable s)
	        {
	            if (s.length()<4)
	            {
	            	icaoText.setBackgroundColor(0x22FFFFFF);
	            }
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    }); */
		
        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
 
        // Showing status
        if(status!=ConnectionResult.SUCCESS)
        { 	// Google Play Services are not available
 
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
 
        }
        else
        {
        	Intent i = getIntent();
        	double lat = i.getDoubleExtra("lat", 56.0);
        	double lng = i.getDoubleExtra("long", 10.3);
        	String icao = i.getStringExtra("icao");
        	String airfieldName = i.getStringExtra("airfield_name");
        	
        	// Google Play Services are available
 
            // Getting reference to the SupportMapFragment of activity_main.xml
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            gMap = fm.getMap();
            
            gMap.setInfoWindowAdapter(new InfoWindowAdapter(){
            
            	@Override
            	public View getInfoWindow(Marker arg0)
            	{
            		return null;
            	}
            	
            	@Override
            	public View getInfoContents(Marker arg0)
            	{
            		View v = getLayoutInflater().inflate(R.layout.windowlayout, null);
            		
            		TextView tvName = (TextView) v.findViewById(R.id.tv_name);
            		
            		TextView tvMetar = (TextView) v.findViewById(R.id.tv_metar);
            		       
            		tvName.setText(Html.fromHtml("<b>" + arg0.getTitle() + "</b"));
            		
            		tvMetar.setText(arg0.getSnippet());
            		
            		return v;
            	}
            		
            });
        	// Creating a LatLng object for the current location (somewhere near Aarhus! :-))
            LatLng latLng = new LatLng(lat, lng);
     
            // Showing the current location in Google Map
            gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
     
            // Zoom in the Google Map at a level where all (most) of Denmark will be visible
            gMap.animateCamera(CameraUpdateFactory.zoomTo(6));
            
            showMetar(icaoText, icao, airfieldName);
        }
	}
	
	public boolean onOptionsItemSelected (MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_settings:
				final AlertDialog.Builder alert = new AlertDialog.Builder(this);
				
				alert.setTitle("Set Server IP Address");
				alert.setIcon(R.drawable.setserver_inverse);
				
			    final EditText input = new EditText(this);
			    alert.setView(input);
			    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() 
			    {
			        public void onClick(DialogInterface dialog, int whichButton) 
			        {
			            String value = input.getText().toString().trim();
			            //Toast.makeText(getApplicationContext(), value, Toast.LENGTH_SHORT).show();
			            serverIP = value;
			        }
			    });

			    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
			    {
			        public void onClick(DialogInterface dialog, int whichButton)
			        {
			            dialog.cancel();
			        }
			    });
			    alert.show();
				break;
		}
		return true;
	}
	
	public void showMetar(View view, String icao, String airfieldName)
	{
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		
		/*Button btnGetMetar = (Button)findViewById(R.id.btnShowMap); 
		btnGetMetar.setEnabled(false);*/
		
		//TextView textMetar = (TextView) findViewById(R.id.text_metar);
		//textMetar.setBackgroundColor(0x55FFFFFF);
    	//textMetar.setText("Waiting for report");
		
		gMap.clear();
		try 
	    {
	    	DataBaseHelper myDbHelper = new DataBaseHelper(this.getApplicationContext());
	    
	    	EditText icaoText = (EditText) findViewById(R.id.edit_icao);
	    	//String icaoCode = icaoText.getText().toString().toUpperCase();
	    	icaoText.setText(airfieldName + " (" + icao + ")");
	    	
	    	Log.i("airfields", "Start db load");
            try 
            {
            	myDbHelper.createDataBase();
            	myDbHelper.openDataBase();
    	 	}
            catch (IOException ioe)
            {
    	 		throw new Error("Unable to create database");
    	 	}
            catch(SQLException sqle)
            {
    	 		throw sqle;
    	 	}
            
            LatLng mapCentre = myDbHelper.icaoToLatLng(icao);
            LatLngBounds mapBounds = new LatLngBounds(new LatLng(mapCentre.latitude-5.0, mapCentre.longitude-(3.0/Math.cos(mapCentre.latitude*Math.PI/180))), new LatLng(mapCentre.latitude+5.0, mapCentre.longitude+(3.0/Math.cos(mapCentre.latitude*Math.PI/180))));
            //gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mapCentre, 6.0f));
            gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 600, 1200, 0));
            
            ArrayList<Airfield> airfields = myDbHelper.airfieldsInArea(mapBounds);
    	 	myDbHelper.close();
	    	
    	 	new DownloadReportTask().execute(icao);
    	 	new DownloadAirfieldsTask().execute(airfields);
	    } 
	    catch (Exception e)
	    {
	    	//btnGetMetar = (Button)findViewById(R.id.btnShowMap);
			//btnGetMetar.setEnabled(true);
			
			EditText icaoText = (EditText) findViewById(R.id.edit_icao);
			icaoText.setBackgroundColor(0x22FFFFFF);
			Toast.makeText(getApplicationContext(), "ICAO not found", Toast.LENGTH_LONG).show();
			//textMetar.setText("ICAO not found");
	    }
	}
	
	private String readMetarFeed(String icaoCode) 
	{
	    String metarURL = "http://" + serverIP + "/test_json.php?icao=" + icaoCode;
	
		StringBuilder builder = new StringBuilder();
	    	    
	    HttpGet httpGet = new HttpGet(metarURL);
	    HttpParams httpParameters = new BasicHttpParams();
	    
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used. 
		int timeoutConnection = 3000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 5000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	
		DefaultHttpClient client = new DefaultHttpClient(httpParameters);
			
		client.setParams(httpParameters);
	    
	    try
	    {
	    	HttpResponse response = client.execute(httpGet);
	    	StatusLine statusLine = response.getStatusLine();
	    	int statusCode = statusLine.getStatusCode();
	    	if (statusCode == 200)
	    	{
	    		HttpEntity entity = response.getEntity();
	    		InputStream content = entity.getContent();
	    		BufferedReader reader = new BufferedReader(new InputStreamReader(content));
	    		String line;
	    		while ((line = reader.readLine()) != null)
	    		{
	    			builder.append(line);
	    		}
	    	}
	    	else
	    	{
	    		Log.e(MainActivity.class.toString(), "Failed to download file");
	    	}
	    }
	    catch (ClientProtocolException e)
	    {
	    	e.printStackTrace();
	    	throw new RuntimeException("Error connecting to server during download process");
	    }
	    catch (IOException e)
	    {
	    	e.printStackTrace();
	    	throw new RuntimeException("Error during download process - all airfields may not be displayed");
	    }
	    
	    return builder.toString();
	}
	
	private class DownloadReportTask extends AsyncTask<String, Void, String> 
	{
	     protected String doInBackground(String... icaoCode) 
	     {
	    	String readMetarFeed;
	    	 
	    	try
	    	{
	    		readMetarFeed = readMetarFeed(icaoCode[0]);
	    	}
	    	catch (Exception ex)
	    	{
	    		readMetarFeed = ex.getMessage();
	    	}
	        return readMetarFeed;
	     }

	     protected void onPostExecute(String metarFeed) 
	     {
	    	//TextView textMetar = (TextView) findViewById(R.id.text_metar);
	    	EditText icaoText = (EditText) findViewById(R.id.edit_icao);
	    	 
	    	JSONObject jsonObject = null;
	    	//String metar = "";
	    	String colour = "";
	    	
			try 
			{
				jsonObject = new JSONObject(metarFeed);
				//metar = jsonObject.getString("report");
				colour = jsonObject.getString("colour");
				
		    	if (colour.contentEquals("BLU"))
		    	{
		    		//textMetar.setBackgroundColor(0x550000FF);
		    		icaoText.setBackgroundColor(0x770000FF);
		    	}
		    	else if (colour.contentEquals("WHT"))
		    	{
		    		//textMetar.setBackgroundColor(0x55FFFFFF);
		    		icaoText.setBackgroundColor(0x77FFFFFF);
		    	}
		    	else if (colour.contentEquals("GRN"))
		    	{
		    		//textMetar.setBackgroundColor(0x5500FF00);
		    		icaoText.setBackgroundColor(0x7700FF00);
		    	}
		    	else if (colour.contentEquals("YLO"))
		    	{
		    		//textMetar.setBackgroundColor(0x55FFFF00);
		    		icaoText.setBackgroundColor(0x77FFFF00);
		    	}
		    	else if (colour.contentEquals("AMB"))
		    	{
		    		//textMetar.setBackgroundColor(0x55FF9933);
		    		icaoText.setBackgroundColor(0x77FF9933);
		    	}
		    	else if (colour.contentEquals("RED"))
		    	{
		    		//textMetar.setBackgroundColor(0x55FF0000);
		    		icaoText.setBackgroundColor(0x77FF0000);
		    	}
		    	else if (colour.contentEquals("NIL"))
		    	{
		    		//textMetar.setBackgroundColor(0x55FFFFFF);
		    		icaoText.setBackgroundColor(0x22FFFFFF);
		    	}
			}
			catch (JSONException e) 
			{
				//metar = "No report found";
				if (metarFeed.contentEquals(""))
				{
					Toast.makeText(getApplicationContext(), "No report found", Toast.LENGTH_LONG).show();
				}
				else
				{
					Toast.makeText(getApplicationContext(), metarFeed, Toast.LENGTH_LONG).show();
				}
				icaoText.setBackgroundColor(0x00000000);
				
			}
	    	finally
	    	{
	    		//textMetar.setText(metar);
	    	}
			super.onPostExecute(metarFeed);
	     }
	 }
	
	private class DownloadAirfieldsTask extends AsyncTask<ArrayList<Airfield>, MarkerOptions, String> 
	{
	     protected String doInBackground(ArrayList<Airfield>... airfield) 
	     {
    	 	String exception = null;
	    	JSONObject jsonObject = null;

    	 	int icon_state=R.drawable.icn_empty;
    	 	String readMetarFeed;
                      
            for (int i=0; i<airfield[0].size();i++)
            {
            	String colour = "NIL";
            	
            	try
            	{
            		readMetarFeed = readMetarFeed(airfield[0].get(i).getIcaoCode());
	            	
	            	if(readMetarFeed != null)
	            	{
	            		Log.i("airfields", readMetarFeed);
	            		
	            		try 
	            		{
							jsonObject = new JSONObject(readMetarFeed);
							colour = jsonObject.getString("colour");
						
		        	    	if (colour.contentEquals("BLU"))
		        	    	{
		        	    		icon_state=R.drawable.icn_blue;
		        	    	}
		        	    	else if (colour.contentEquals("WHT"))
		        	    	{
		        	    		icon_state=R.drawable.icn_white;
		        	    	}
		        	    	else if (colour.contentEquals("GRN"))
		        	    	{
		        	    		icon_state=R.drawable.icn_green;
		        	    	}
		        	    	else if (colour.contentEquals("YLO"))
		        	    	{
		        	    		icon_state=R.drawable.icn_yellow;
		        	    	}
		        	    	else if (colour.contentEquals("AMB"))
		        	    	{
		        	    		icon_state=R.drawable.icn_amber;
		        	    	}
		        	    	else if (colour.contentEquals("RED"))
		        	    	{
		        	    		icon_state=R.drawable.icn_red;
		        	    	}
		        	    	else if (colour.contentEquals("NIL"))
		        	    	{
		        	    		icon_state=R.drawable.icn_empty;
		        	    	}
		        	    	
		        	    	MarkerOptions marker = null;
		        	    	
		        	    	marker = new MarkerOptions()
							.position(new LatLng(airfield[0].get(i).getLat(), airfield[0].get(i).getLng()))
							.title(airfield[0].get(i).getName())
							.snippet(jsonObject.getString("report"))
							.icon(BitmapDescriptorFactory.fromResource(icon_state));
							if (marker!=null)
							{
								publishProgress(marker);
							}
	            		}
	            		catch (JSONException e)
	        	    	{
							Log.i("ex", e.getMessage());
						}
	            	}
	            	else
	            	{
	            		MarkerOptions marker = null;
		            	
						marker = new MarkerOptions()
						.position(new LatLng(airfield[0].get(i).getLat(), airfield[0].get(i).getLng()))
						.title(airfield[0].get(i).getName())
						.snippet("No report available")
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.icn_empty));
						if (marker!=null)
						{
							publishProgress(marker);
						}
	            	}
            	}
            	catch (Exception ex)
            	{
            		Log.i("ex", ex.getMessage());
            		exception = ex.getMessage();
            	}
    		}
            return exception;
	     }

	     @Override
	     protected void onProgressUpdate(MarkerOptions... marker)
	     {
	    	 gMap.addMarker(marker[0]);
	    	
	    	 super.onProgressUpdate();
	     }

		@Override
		protected void onPostExecute(String result) 
		{
			/*Button btnGetMetar = (Button)findViewById(R.id.btnShowMap);
			btnGetMetar.setEnabled(true);*/
			
			if(result!=null)
			{
				Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
			}
			
			super.onPostExecute(result);
		}
	 }

	@Override
	public void onBackPressed()
	{
		Intent i = new Intent(getApplicationContext(), AirfieldListActivity.class);
		startActivity(i);
		this.finish();
		return;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
