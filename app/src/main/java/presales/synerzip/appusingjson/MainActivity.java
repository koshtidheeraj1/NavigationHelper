package presales.synerzip.appusingjson;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
		private static final int REQUEST_ENABLE_BT = 10;
		String json = "";
		double dLatitude;
		double dLongitude;
		private static String TAG = MainActivity.class.getSimpleName();
		GoogleMap map = null;
		String key = "AIzaSyBPGJPKwU14vZGQUFFmkEXakheVXEnUwfg";
		EditText destination;
		Button ok;

		class GmapResponseListner implements Response.Listener<JSONObject> {
				GoogleMap map;
				public GmapResponseListner(GoogleMap map) {
						this.map = map;
				}
				@Override
				public void onResponse(JSONObject response) {
						try {
								List<NavLeg> pathName = new ArrayList<>();
								JSONArray gmap1 = response.getJSONArray("routes");
								for (int j = 0; j < gmap1.length(); j++) {
										JSONObject legs_object = (JSONObject) gmap1.get(j);
										JSONArray legs = legs_object.getJSONArray("legs");
										for (int k = 0; k < legs.length(); k++) {
												JSONObject dist_value = (JSONObject) legs.get(k);
												JSONArray steps = dist_value.getJSONArray("steps");
												for (int l = 0; l < steps.length(); l++) {
														JSONObject latlang = (JSONObject) steps.get(l);
														JSONObject end_location = (JSONObject) latlang.get("end_location");
														JSONObject distance = (JSONObject) latlang.get("distance");

														float lat = Float.parseFloat(end_location.getString("lat"));
														float lng = Float.parseFloat(end_location.getString("lng"));
														String dis = distance.getString("text");
														NavLeg nav_leg = new NavLeg(lat,lng,dis);
														pathName.add(nav_leg);
												}
										}
								}
								NavLeg first = pathName.get(0);
								Location myLocation = map.getMyLocation();
								BluetoothTransmit transmitter = new BluetoothTransmit();
								transmitter.init();
								if (first.lng > myLocation.getLongitude()) {
										transmitter.transmit("1");
								} else {
										transmitter.transmit("2");
								}
						} catch (JSONException e) {
								e.printStackTrace();
						}
				}
		}

		class StartNavOnClickListner implements View.OnClickListener {
				GoogleMap map;
				public StartNavOnClickListner(GoogleMap map) {
						this.map = map;
				}
				@Override
				public void onClick(View v) {
						Location loc = map.getMyLocation();
						double lat = loc.getLatitude();
						double lng = loc.getLongitude();
						//String url = "https://maps.googleapis.com/maps/api/directions/json?origin=Pune&destination=Mumbai&mode=walking&AIzaSyBPGJPKwU14vZGQUFFmkEXakheVXEnUwfg";
						String url = "https://maps.googleapis.com/maps/api/directions/json?";
						url = url + "origin=" + lat + "," + lng + "&destination=" + destination.getText().toString();
						url = url + "&mode=" + "driving" + "&" + key;
								/* List<NameValuePair> params = new LinkedList<NameValuePair>();
								params.add(new BasicNameValuePair("origin=", dLatitude + "," + dLongitude));
								params.add(new BasicNameValuePair("&destination=", destination.getText().toString()));
								params.add(new BasicNameValuePair("&mode=", "driving"));
								params.add(new BasicNameValuePair("&", key));
								String paramString = URLEncodedUtils.format(params, "utf-8");
								url += paramString;*/


						// Request a string response from the provided URL.
						JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
										url, null, new GmapResponseListner(map), new Response.ErrorListener() {
								@Override
								public void onErrorResponse(VolleyError error) {
										VolleyLog.d(TAG, "Error: " + error.getMessage());
										Toast.makeText(getApplicationContext(),
														error.getMessage(), Toast.LENGTH_SHORT).show();
								}
						});

						// Add the request to the RequestQueue.
						AppController.getInstance(getApplicationContext()).addToRequestQueue(jsonObjReq);
				}
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_main);
				// Obtain the SupportMapFragment and get notified when the map is ready to be used.
				SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
								.findFragmentById(R.id.map);
				mapFragment.getMapAsync(this);
		}

		@Override
		public void onMapReady(GoogleMap googleMap) {
				map = googleMap;
				map.setMyLocationEnabled(true);
				makeJsonObjectRequest();
				BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				if (mBluetoothAdapter == null) {
						System.out.print("device does not support bluetooth");
				} else if (!mBluetoothAdapter.isEnabled()) {
						Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
						startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				}
		}

		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
				super.onActivityResult(requestCode, resultCode, data);
				// check if the request code is same as what is passed  here it is 2
				if (requestCode == REQUEST_ENABLE_BT) {
						System.out.println("dheeraj" + "Returned from bluetooth connnection request");
						if (resultCode != RESULT_OK) {
								System.out.println("dheeraj" + "Bluetooth connnection request denied");
						}
				}
		}

		private void makeJsonObjectRequest() {
				//RequestQueue queue = Volley.newRequestQueue(this);
				destination = (EditText) findViewById(R.id.destination);
				ok = (Button) findViewById(R.id.getdirection);
				ok.setOnClickListener(new StartNavOnClickListner(map));
		}
}
