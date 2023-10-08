package com.my.newproject16;

import android.Manifest;
import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.*;
import android.net.*;
import android.net.Uri;
import android.os.*;
import android.os.Bundle;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.appbar.AppBarLayout;
import java.io.*;
import java.io.InputStream;
import java.text.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.*;
import org.json.*;
import com.my.newproject16.AccelerometerService;

public class MainActivity extends AppCompatActivity {
	
	private Timer _timer = new Timer();
	
	private Toolbar _toolbar;
	private AppBarLayout _app_bar;
	private CoordinatorLayout _coordinator;
	private double dist = 0;
	private String phoneNumber = "";
	private String message = "";
	private double lat = 0;
	private double lon = 0;
	private double acc = 0;
	private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
	private double maxValue = 0;
	private double minValue = 0;
	
	private LinearLayout linear1;
	private TextView textview1;
	
	private TimerTask t1;
	private AlertDialog.Builder d1;
	private TimerTask t2;
	private SharedPreferences sp1;
	private Intent i1 = new Intent();
	private LocationManager loc;
	private LocationListener _loc_location_listener;
	private TimerTask t3;
	private TimerTask t;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.main);
		initialize(_savedInstanceState);
		
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
		} else {
			initializeLogic();
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1000) {
			initializeLogic();
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
		_app_bar = findViewById(R.id._app_bar);
		_coordinator = findViewById(R.id._coordinator);
		_toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _v) {
				onBackPressed();
			}
		});
		linear1 = findViewById(R.id.linear1);
		textview1 = findViewById(R.id.textview1);
		d1 = new AlertDialog.Builder(this);
		sp1 = getSharedPreferences("number", Activity.MODE_PRIVATE);
		loc = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		_loc_location_listener = new LocationListener() {
			@Override
			public void onLocationChanged(Location _param1) {
				final double _lat = _param1.getLatitude();
				final double _lng = _param1.getLongitude();
				final double _acc = _param1.getAccuracy();
				message = "I am in danger , Help Me\n\nMy Location :\n\nLattitude : ".concat(String.valueOf(_lat).concat("\nLongitude : ".concat(String.valueOf(_lng))).concat("\nClick To Open In Google Map :\nhttps://www.google.com/maps?q=".concat(String.valueOf(_lat).concat(",".concat(String.valueOf(_lng))))));
			}
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}
			
			@Override
			public void onProviderEnabled(String provider) {
			}
			
			@Override
			public void onProviderDisabled(String provider) {
			}
		};
	}
	
	private void initializeLogic() {
		maxValue = 15;
		minValue = 9;
		// 15 and 9 is given for test purpose. increase the value to more for the real life scenario to detect accidents.
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) { 
			ActivityCompat.requestPermissions(this, new String[]{
				Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
		} else { 
			// Permission already granted, start the location service 
		}
		if (sp1.getString("number", "").equals("")) {
			i1.setClass(getApplicationContext(), AddActivity.class);
			startActivity(i1);
		}
		t = new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
							loc.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, _loc_location_listener);
						}
						//Checks In Every 1 Minute
					}
				});
			}
		};
		_timer.scheduleAtFixedRate(t, (int)(0), (int)(60000));
		IntentFilter filter = new IntentFilter(AccelerometerService.VELOCITY_UPDATE_ACTION);
		 registerReceiver(velocityReceiver, filter);
		// Start the AccelerometerService (if not already started)
		Intent accelerometerServiceIntent = new Intent(this, AccelerometerService.class);
		 startService(accelerometerServiceIntent);
	}
	private BroadcastReceiver velocityReceiver = new BroadcastReceiver() {
		@Override public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(AccelerometerService.VELOCITY_UPDATE_ACTION))
			{
				float velocityKmHr = intent.getFloatExtra("VELOCITY_KM_HR", 0.0f); updateVelocityText(velocityKmHr); 
			}
		}
	};
	public void updateVelocityText(float velocityKmHr) {
		dist = (double)velocityKmHr;
		if (maxValue < dist) {
			if (t1==null){
				t1 = new TimerTask() {
					@Override
					public void run() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (dist < minValue) {
									t2 = new TimerTask() {
										@Override
										public void run() {
											runOnUiThread(new Runnable() {
												@Override
												public void run() {
													phoneNumber = sp1.getString("number", "");
													SmsUtils.sendSms(MainActivity.this, 0, phoneNumber, message);
													SketchwareUtil.showMessage(getApplicationContext(), "SMS Deployed");
													d1.setTitle("Emergency On The Way");
													d1.setMessage("A SMS has been sent to your emergency number with your location");
													d1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
														@Override
														public void onClick(DialogInterface _dialog, int _which) {
															t2.cancel();
														}
													});
													d1.create().show();
												}
											});
										}
									};
									_timer.schedule(t2, (int)(5000));
									d1.setTitle("CAUTION ⚠️");
									d1.setMessage("We detected a sudden stop in acceleration...\nIs it an accident ? Please Click Cancel Alert If its not an accident , or else an sms will be sent to your emergency number you added here within 5 second.");
									d1.setPositiveButton("Cancel Alert", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface _dialog, int _which) {
											t2.cancel();
										}
									});
									d1.create().show();
								}
								t1.cancel();
								t1=null;
							}
						});
					}
				};
				_timer.schedule(t1, (int)(1500));
			}
		}
		textview1.setText(String.valueOf((long)(dist)));
	}
	{
	}
	
	
	@Deprecated
	public void showMessage(String _s) {
		Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
	}
	
	@Deprecated
	public int getLocationX(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[0];
	}
	
	@Deprecated
	public int getLocationY(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[1];
	}
	
	@Deprecated
	public int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}
	
	@Deprecated
	public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
		ArrayList<Double> _result = new ArrayList<Double>();
		SparseBooleanArray _arr = _list.getCheckedItemPositions();
		for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
			if (_arr.valueAt(_iIdx))
			_result.add((double)_arr.keyAt(_iIdx));
		}
		return _result;
	}
	
	@Deprecated
	public float getDip(int _input) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
	}
	
	@Deprecated
	public int getDisplayWidthPixels() {
		return getResources().getDisplayMetrics().widthPixels;
	}
	
	@Deprecated
	public int getDisplayHeightPixels() {
		return getResources().getDisplayMetrics().heightPixels;
	}
}