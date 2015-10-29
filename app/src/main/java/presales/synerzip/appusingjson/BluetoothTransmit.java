package presales.synerzip.appusingjson;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.*;
import android.content.Intent;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by synerzip on 28/10/15.
 */
public class BluetoothTransmit {

		BluetoothAdapter mBluetoothAdapter = null;
		BluetoothDevice comm_device = null;
		BluetoothSocket comm_socket = null;
		private UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

		class bluetooth_runnable implements Runnable {
				BluetoothSocket comm_socket;
				String dir;
				public bluetooth_runnable(BluetoothSocket comm_socket, String dir) {
						this.dir = dir;
						this.comm_socket = comm_socket;
				}
				@Override
				public void run() {
						OutputStream outputStream = null;
						try {
								comm_socket.connect();
								outputStream = comm_socket.getOutputStream();
								outputStream.write(dir.getBytes());
								outputStream.flush();
						} catch (IOException e) {
								try {
										outputStream.close();
										comm_socket.close();
								} catch (IOException e1) {
										e1.printStackTrace();
								}
								System.out.println("bluetooth data send exception" + e.getMessage());
						}
				}
		}

		public void init() {
				// Bluetooth
				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
				// If there are paired devices
				if (pairedDevices.size() > 0) {
						// Loop through paired devices
						for (BluetoothDevice device : pairedDevices) {
								System.out.print(device.getName());
								if (device.getName().equals("HC-05")) {
										comm_device = device;
								}
						}
				}

				if (comm_device == null) {
						// Need to code for scanning available bluetooth devices.
						// mBluetoothAdapter.startDiscovery();
				}

				if (comm_device != null) {
						// Get a BluetoothSocket to connect with the given BluetoothDevice
						try {
								// MY_UUID is the app's UUID string, also used by the server code
								comm_socket = comm_device.createRfcommSocketToServiceRecord(DEFAULT_UUID);
						} catch (IOException e) {
								System.out.print("bluetooth socket creation problem");
						}
				}
		}

		public void transmit(String dir) {
				if (comm_socket != null) {
						new Thread(new bluetooth_runnable(comm_socket,dir)).start();
				}

				// Below code is for getting map locations.
				/*RequestQueue queue = Volley.newRequestQueue(this);
				String url = "https://maps.googleapis.com/maps/api/directions/json?origin=Brooklyn&destination=Queens&mode=transit&AIzaSyBLrqOwYloZ6V4lCsmURLKMmd0VrIo-4x4";

				// Request a string response from the provided URL.
				StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
								new Response.Listener<String>() {
										@Override
										public void onResponse(String response) {
												System.out.print("dheeraj" + response);
										}
								}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
								System.out.print("Google directions API response error");
						}
				});
				// Add the request to the RequestQueue.
				queue.add(stringRequest);*/
		}
}
