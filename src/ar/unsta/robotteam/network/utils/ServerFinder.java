package ar.unsta.robotteam.network.utils;

import com.cjsavage.java.net.discovery.ServiceFinder;
import com.cjsavage.java.net.discovery.ServiceInfo;

public class ServerFinder {

	ServiceFinder serviceFinder;
	int mRequestId = 1;
	private String serverName;
	private String serverIp;
	private int serverPort;
	private boolean serverFound = false;

	public boolean foundServer() {
		return serverFound;
	}

	public void setServerFoundState(boolean p_state) {
		serverFound = p_state;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String p_serverName) {
		serverName = p_serverName;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String p_serverIp) {
		serverIp = p_serverIp;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int p_serverPort) {
		serverPort = p_serverPort;
	}

	public ServerFinder() {
		initFinder();
	}

	public void initFinder() {
		ServiceFinder finder = new ServiceFinder();
		finder.addListener(mListener);
		finder.startListening();
		serviceFinder = finder;
	}

	public void stopFinder() {
		if (serviceFinder.isListening()) {
			serviceFinder.stopListening();
		}
	}

	public void find(String p_serviceID) {

		serviceFinder.findServers(p_serviceID, mRequestId++);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private ServiceFinder.Listener mListener = new ServiceFinder.Listener() {
		@Override
		public void serverFound(ServiceInfo si, int requestId,
				ServiceFinder finder) {

			setServerIp(si.getServiceHost());
			setServerName(si.getServerName());
			setServerPort(si.getServicePort());
			setServerFoundState(true);
		}

		@Override
		public void listenStateChanged(ServiceFinder finder, boolean listening) {
		}
	};
}
