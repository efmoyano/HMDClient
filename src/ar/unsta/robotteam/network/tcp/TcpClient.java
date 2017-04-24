package ar.unsta.robotteam.network.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import ar.unsta.robotteam.network.utils.ServerFinder;

public class TcpClient {

	private PrintWriter outPrint;
	private String serverIp;
	private int serverPort;
	private Socket tcpSocket;
	private boolean m_running = false;
	private boolean m_connected = false;
	private int m_seqNumber;
	public static final String TCP_SERVICE_ID = "17d8c17c9d93d3e91da08451d9bdbefe";

	public boolean startClient() {

		try {
			tcpSocket = new Socket();
			tcpSocket
					.connect(new InetSocketAddress(serverIp, serverPort), 1000);

			outPrint = new PrintWriter(tcpSocket.getOutputStream());

			new Thread() {

				public void run() {
					InputStream l_is;
					try {
						l_is = tcpSocket.getInputStream();
						BufferedReader l_reader = new BufferedReader(
								new InputStreamReader(l_is));

						String l_line = l_reader.readLine();
						while (l_line != null) {
							m_seqNumber = Integer.parseInt(l_line);
							l_line = l_reader.readLine();
						} // end while
					} catch (IOException e) {
						m_connected = false;
						// Server Disconnect
						serverFinderTask();
						System.err
								.println("Error TCP 0x001: " + e.getMessage());
					}// end try
				}// en run
			}.start();
			m_connected = true;
			m_running = true;
			detectDisconectionTask();
			return true;

		} catch (SocketException e) {
			System.err.println("Error TCP 0x002: " + e.getMessage());
			m_running = false;
			return false;
		} catch (UnknownHostException e) {
			System.err.println("Error TCP 0x003: " + e.getMessage());
			m_running = false;
			return false;
		} catch (IOException e) {
			System.err.println("Error TCP 0x004: " + e.getMessage());
			m_running = false;
			return false;
		}
	}

	public void stopClient() {
		try {
			if (tcpSocket != null) {
				tcpSocket.close();
			}
		} catch (IOException e) {
			System.err.println("Error TCP 0x005: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void sendMessage(String message) {
		
		if (!tcpSocket.isClosed()) {
			if (outPrint != null && !outPrint.checkError()) {
				outPrint.println(message);
				outPrint.flush();
			}
		}
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public boolean isRunning() {
		return m_running;
	}

	public boolean isConnected() {
		return m_connected;
	}

	public void detectDisconectionTask() {

		final Timer detectorTimer = new Timer();

		detectorTimer.schedule(new TimerTask() {

			int old_seqNumber = -1;

			@Override
			public void run() {

				if (old_seqNumber != m_seqNumber) {
					old_seqNumber = m_seqNumber;
				} else {
					serverFinderTask();
					detectorTimer.cancel();
				}
			}
		}, 0, 2000);
	}

	public void serverFinderTask() {

		final Timer findServerTimer = new Timer();
		findServerTimer.schedule(new TimerTask() {

			boolean m_find;
			ServerFinder serverFinder = new ServerFinder();

			@Override
			public void run() {

				serverFinder.find(TcpClient.TCP_SERVICE_ID);

				m_find = serverFinder.foundServer();

				if (m_find) {
					serverIp = serverFinder.getServerIp();
					serverPort = serverFinder.getServerPort();
					startClient();
					findServerTimer.cancel();
				}
			}
		}, 0, 3000);
	}
}