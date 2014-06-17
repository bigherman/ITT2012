package dk.bigherman.android.pisviewer;

import java.io.IOException;
import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.SQLException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class AirfieldListActivity extends Activity {

	boolean showCountries = true;
	ArrayList<Airfield> airfieldList = new ArrayList<Airfield>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_airfield_list);
		
		final DataBaseHelper myDbHelper = new DataBaseHelper(this.getApplicationContext());
    	
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
		
	    final ListView listview = (ListView) findViewById(R.id.listViewAirfields);
	    final ArrayList<String> list = myDbHelper.listCountries();
	   
	    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
	    
	    listview.setAdapter(adapter);
	    
	    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() 
	    {
	        @Override
	        public void onItemClick(AdapterView<?> parent, final View view, int position, long id) 
	        {
	        	if(showCountries)
	        	{
	        		String item = (String) parent.getItemAtPosition(position);
	        		
	        		airfieldList = myDbHelper.listAirfieldsInCountry(item);
	        		
	        		adapter.clear();
	        		for(int i = 0; i < airfieldList.size(); i++)
	        		{
	        			adapter.add(airfieldList.get(i).getName() + " (" + airfieldList.get(i).getIcaoCode() + ")");
	        		}
	        		listview.setSelectionAfterHeaderView();
	        		showCountries = !showCountries;
	        	}
	        	else
	        	{
	        		Airfield airfieldItem = airfieldList.get(position);
	        		
	        		showCountries = !showCountries;
	        		
	        		goToMap(listview, airfieldItem);
	        	}
	        }

	    });
	}

	public void goToMap(View view, Airfield airfield)
	{
		Intent i = new Intent(getApplicationContext(), MainActivity.class);
		i.putExtra("lat", airfield.getLat());
		i.putExtra("long", airfield.getLng());
		i.putExtra("icao", airfield.getIcaoCode());
		i.putExtra("airfield_name", airfield.getName());
		startActivity(i);
		this.finish();
		return;
	}
	
	@Override
	public void onBackPressed()
	{
		this.finish();
		return;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.airfield_list, menu);
		return true;
	}
}
