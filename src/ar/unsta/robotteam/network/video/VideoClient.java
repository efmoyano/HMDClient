package ar.unsta.robotteam.network.video;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import ar.unsta.robotteam.network.VideoPacket;
import ar.unsta.robotteam.network.utils.ServerFinder;

public class VideoClient {

	private DatagramSocket l_socket;
	private static final int m_receivedPort = 25000;
	private byte[] streamBuffer;
	private VideoEventListener videoEventListener;
	private int m_seqNumber;
	private String serverIP;
	private int serverPortNumber;
	private Socket l_connectSocket;
	public static final String VIDEO_SERVICE_ID = "b00b5fee17e57eeb00b5fee17e57eeee";

	public int getServerPortNumber() {
		return serverPortNumber;
	}

	public void setServerPortNumber(int serverPortNumber) {
		this.serverPortNumber = serverPortNumber;
	}

	public String getServerIP() {
		return serverIP;
	}

	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}

	public VideoClient(VideoEventListener p_rtspEventListener) {
		videoEventListener = p_rtspEventListener;
		streamBuffer = new byte[65536];
	}

	public boolean connect() {
		try {
			l_connectSocket = new Socket();
			l_connectSocket.connect(new InetSocketAddress(serverIP,
					serverPortNumber), 1000);

			new Thread() {

				public void run() {
					InputStream l_is;
					try {
						l_is = l_connectSocket.getInputStream();
						BufferedReader l_reader = new BufferedReader(
								new InputStreamReader(l_is));

						String l_line = l_reader.readLine();
						while (l_line != null) {
							m_seqNumber = Integer.parseInt(l_line);
							l_line = l_reader.readLine();
						} // end while
					} catch (IOException e) {
						System.err
								.println("Error TCP 0x001: " + e.getMessage());
						serverFinderTask();

					}// end try
				}// en run
			}.start();

			detectDisconectionTask();

			return true;

		} catch (UnknownHostException ex) {
			System.err.println(VideoClient.class.getName() + " Error 5a001 :"
					+ ex.getMessage());
			ex.printStackTrace();
			return false;

		} catch (IOException ex) {
			System.err.println(VideoClient.class.getName() + " Error 5a002 :"
					+ ex.getMessage());
			ex.printStackTrace();
			return false;

		}
	}

	public void play() {
		try {

			l_socket = new DatagramSocket(m_receivedPort);
			l_socket.setSoTimeout(5);

			new Thread() {

				public void run() {

					while (true) {
						DatagramPacket l_receivedSocket = new DatagramPacket(
								streamBuffer, streamBuffer.length);
						try {

							l_socket.receive(l_receivedSocket);

							VideoPacket videoPacket = new VideoPacket(
									l_receivedSocket.getData(),
									l_receivedSocket.getLength());

							int payload_length = videoPacket
									.getpayload_length();
							byte[] payload = new byte[payload_length];
							videoPacket.getpayload(payload);

							Bitmap imageBitmap = BitmapFactory.decodeByteArray(
									payload, 0, payload.length);

							videoEventListener.onImageReceived(imageBitmap);

						} catch (InterruptedIOException iioe) {
							// iioe.printStackTrace();
						} catch (IOException ex) {
							System.err.println(VideoClient.class.getName()
									+ " Error 5a003 :" + ex.getMessage());
						}
					}
				}
			}.start();
		} catch (SocketException ex) {
			System.err.println(VideoClient.class.getName() + " Error 5a004 :"
					+ ex.getMessage());
		}
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

				serverFinder.find(VideoClient.VIDEO_SERVICE_ID);

				m_find = serverFinder.foundServer();
				System.out.println("Looking for Video servers");

				if (m_find) {
					System.out.println("Server Found: "
							+ serverFinder.getServerName());
					serverIP = serverFinder.getServerIp();
					serverPortNumber = serverFinder.getServerPort();
					connect();
					play();
					findServerTimer.cancel();
				}
			}
		}, 0, 3000);
	}

}
