package ar.unsta.robotteam.network.utils;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetworkUtils {

	public static boolean canConnect(Context p_context) {
		
		ConnectivityManager cm = (ConnectivityManager) p_context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.isConnectedOrConnecting();
	}

}
