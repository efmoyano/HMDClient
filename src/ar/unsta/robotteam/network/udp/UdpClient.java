package ar.unsta.robotteam.network.udp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import ar.unsta.robotteam.network.utils.ServerFinder;


public class UdpClient {

	private String serverIp;
	private int serverPort;
	private Socket tcpSocket;
	private boolean m_running = false;
	private boolean m_connected = false;
	public static final String UDP_SERVICE_ID = "37dac17f9d93d3e91da0845159bd5ef5";
	private InetAddress peerIpAddress;
	private DatagramSocket m_Socket;
	private int sequenceNumber = 0;
	private int timestamp;
	private int m_seqNumber;

	public boolean startClient() {

		try {
			m_Socket = new DatagramSocket();
			m_Socket.setSoTimeout(1000);

			tcpSocket = new Socket();
			tcpSocket
					.connect(new InetSocketAddress(serverIp, serverPort), 1000);

			peerIpAddress = tcpSocket.getInetAddress();

			m_connected = true;
			m_running = true;

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
						serverFinderTask();
						System.err
								.println("Error TCP 0x001: " + e.getMessage());

					}// end try
				}// en run
			}.start();
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

	public void sendMessage(String p_message) {

		try {
			byte[] imageInByte = p_message.getBytes();
			sequenceNumber++;
			timestamp = (int) (System.currentTimeMillis() / 1000);

			OrientationPacket packet = new OrientationPacket(sequenceNumber,
					timestamp, imageInByte);

			int packet_length = packet.getLength();

			byte[] packet_bits = new byte[packet_length];
			packet.getPacket(packet_bits);

			DatagramPacket sendPacket = new DatagramPacket(packet_bits,
					packet_length, peerIpAddress, 25000);

			if (sendPacket.getLength() < 65536) {
				m_Socket.send(sendPacket);
			}
		} catch (NullPointerException ex) {
			System.err.println("Error 1");
		} catch (java.net.SocketException ex) {
			System.err.println("Error 2");
		} catch (IOException ex) {
			System.err.println("Error 3");
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

				serverFinder.find(UDP_SERVICE_ID);

				m_find = serverFinder.foundServer();
				
				System.out.println("Looking UDP Server");

				if (m_find) {
					System.out.println("Server Found");
					serverIp = serverFinder.getServerIp();
					serverPort = serverFinder.getServerPort();
					startClient();
					findServerTimer.cancel();
				}
			}
		}, 0, 3000);
	}
}