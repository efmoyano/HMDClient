/*
 * Copyright (C) 2013 Ernesto Moyano
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ar.unsta.robotteam.network;

/**
 * @filename VideoPacket
 * @author Ernesto Moyano Universidad del Norte Santo Tomás de Aquino -
 *         Ingeniería Informática
 * @date 4/12/2013 10:58:52
 * @license gpl30
 */
public class VideoPacket {

	private final int HEADER_SIZE = 10;
	private int sequenceNumber;
	private int timeStamp;
	private byte[] header;
	private int payload_size;
	private byte[] payload;

	/**
	 * 
	 * Constructor of an Video object from header fields and payload bitstream
	 * 
	 * @param p_sequenceNumber
	 * @param p_timestamp
	 * @param data
	 * @param data_length
	 */
	public VideoPacket(int p_sequenceNumber, int p_timestamp, byte[] data,
			int data_length) {
		sequenceNumber = p_sequenceNumber;
		timeStamp = p_timestamp;

		header = new byte[HEADER_SIZE];

		header[0] = new Integer(sequenceNumber >> 7).byteValue();
		header[1] = new Integer(sequenceNumber).byteValue();

		for (int i = 0; i < 4; i++) {
			header[5 - i] = new Integer(timeStamp >> (8 * i)).byteValue();
		}

		for (int i = 0; i < 4; i++) {
			header[9 - i] = new Integer(0 >> (8 * i)).byteValue();
		}

		payload_size = data_length;
		payload = new byte[data_length];
		System.arraycopy(data, 0, payload, 0, data_length);
	}

	/**
	 * Constructor of an VideoPacket object from the packet bistream
	 * 
	 * @param packet
	 * @param packet_size
	 */
	public VideoPacket(byte[] packet, int packet_size) {
		if (packet_size >= HEADER_SIZE) {

			header = new byte[HEADER_SIZE];
			System.arraycopy(packet, 0, header, 0, HEADER_SIZE);

			payload_size = packet_size - HEADER_SIZE;
			payload = new byte[payload_size];
			for (int i = HEADER_SIZE; i < packet_size; i++) {
				payload[i - HEADER_SIZE] = packet[i];
			}

			sequenceNumber = unsigned_int(header[1]) + 256
					* unsigned_int(header[0]);
			timeStamp = unsigned_int(header[5]) + 256 * unsigned_int(header[4])
					+ 65536 * unsigned_int(header[3]) + 16777216
					* unsigned_int(header[2]);
		}
	}

	/**
	 * 
	 * @param data
	 * @return the payload bistream of the RTPpacket and its size
	 */
	public int getpayload(byte[] data) {
		System.arraycopy(payload, 0, data, 0, payload_size);

		return payload_size;
	}

	/**
	 * 
	 * @return the length of the payload
	 */
	public int getpayload_length() {
		return payload_size;
	}

	/**
	 * 
	 * @return the total length of the Video packet
	 */
	public int getlength() {
		return payload_size + HEADER_SIZE;
	}

	/**
	 * 
	 * @param packet
	 * @return the packet bitstream and its length
	 */
	public int getpacket(byte[] packet) {
		System.arraycopy(header, 0, packet, 0, HEADER_SIZE);
		System.arraycopy(payload, 0, packet, HEADER_SIZE, payload_size);

		return payload_size + HEADER_SIZE;
	}

	/**
	 * 
	 * @return the timestamp
	 */
	public int gettimestamp() {
		return timeStamp;
	}

	/**
	 * 
	 * @return the sequence number
	 */
	public int getsequencenumber() {
		return sequenceNumber;
	}

	private static int unsigned_int(int nb) {
		if (nb >= 0) {
			return (nb);
		} else {
			return (256 + nb);
		}
	}
}
