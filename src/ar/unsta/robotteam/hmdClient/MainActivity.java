package ar.unsta.robotteam.hmdClient;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import ar.unsta.robotteam.hmdClient.R;
import ar.unsta.robotteam.network.udp.UdpClient;
import ar.unsta.robotteam.network.video.VideoClient;
import ar.unsta.robotteam.network.video.VideoEventListener;

public class MainActivity extends Activity implements SensorEventListener {

	// Sensors
	private SensorManager mSensorManager;
	private Sensor mOrientation;

	// Data
	private int pan;
	private int tilt;

	// TCP para sensores
	private UdpClient udpClient;

	// Video
	private VideoClient videoClient;
	private ImageView imageView;

	private int delay = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initComponents();

		initSensor();

		udpClient = new UdpClient();

		videoClient = new VideoClient(new VideoEventListener() {

			@Override
			public void onImageReceived(final Bitmap p_image) {

				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						imageView.setImageBitmap(p_image);

					}
				});
			}
		});
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {

				while (true) {
					if (udpClient != null && udpClient.isConnected()) {
						udpClient.sendMessage(pan + " " + tilt);
					}
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
		});

		t.start();

		udpClient.serverFinderTask();

		videoClient.serverFinderTask();

	}

	@SuppressWarnings("deprecation")
	public void initSensor() {

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		mSensorManager.registerListener(this, mOrientation,
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	public void onBackPressed() {
	}

	private void initComponents() {

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
		ActionBar actionBar = getActionBar();
		actionBar.hide();

		setContentView(R.layout.activity_main);

		imageView = (ImageView) findViewById(R.id.imageView1);

	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		pan = (int) event.values[0];
		tilt = (int) event.values[1];

		if (tilt <= 0) {
			tilt = tilt + 360;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {

			if (delay > 0) {
				delay = delay - 1;
			}
		}
		if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {

			delay = delay + 1;
		}
		return true;
	}
}
