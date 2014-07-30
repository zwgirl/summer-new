/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.summer.jdi.internal.connect;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.sun.jdi.connect.spi.ClosedConnectionException;
import com.sun.jdi.connect.spi.Connection;

public class SocketConnection extends Connection {

	private SocketTransportService fTransport;

	SocketConnection(SocketTransportService transport) {
		fTransport = transport;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.jdi.connect.spi.Connection#close()
	 */
	@Override
	public synchronized void close() throws IOException {
		if (fTransport == null)
			return;

		fTransport.close();
		fTransport = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.jdi.connect.spi.Connection#isOpen()
	 */
	@Override
	public synchronized boolean isOpen() {
		return fTransport != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.jdi.connect.spi.Connection#readPacket()
	 */
	@Override
	public byte[] readPacket() throws IOException {
		DataInputStream stream;
		synchronized (this) {
			if (!isOpen()) {
				throw new ClosedConnectionException();
			}
			stream = new DataInputStream(fTransport.getInputStream());
		}
		synchronized (stream) {
			int packetLength = 0;
			try {
				packetLength = stream.readInt();
			} catch (IOException e) {
				throw new ClosedConnectionException();
			}

			if (packetLength < 11) {
				throw new IOException("JDWP Packet under 11 bytes"); //$NON-NLS-1$
			}

			byte[] packet = new byte[packetLength];
			packet[0] = (byte) ((packetLength >>> 24) & 0xFF);
			packet[1] = (byte) ((packetLength >>> 16) & 0xFF);
			packet[2] = (byte) ((packetLength >>> 8) & 0xFF);
			packet[3] = (byte) ((packetLength >>> 0) & 0xFF);

			stream.readFully(packet, 4, packetLength - 4);
			return packet;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.jdi.connect.spi.Connection#writePacket(byte[])
	 */
	@Override
	public void writePacket(byte[] packet) throws IOException {
		if (!isOpen()) {
			throw new ClosedConnectionException();
		}
		if (packet == null) {
			throw new IllegalArgumentException(
					"Invalid JDWP Packet, packet cannot be null"); //$NON-NLS-1$
		}
		if (packet.length < 11) {
			throw new IllegalArgumentException(
					"Invalid JDWP Packet, must be at least 11 bytes. PacketSize:" + packet.length); //$NON-NLS-1$
		}

		int packetSize = getPacketLength(packet);
		if (packetSize < 11) {
			throw new IllegalArgumentException(
					"Invalid JDWP Packet, must be at least 11 bytes. PacketSize:" + packetSize); //$NON-NLS-1$
		}

		if (packetSize > packet.length) {
			throw new IllegalArgumentException(
					"Invalid JDWP packet: Specified length is greater than actual length"); //$NON-NLS-1$
		}

		OutputStream stream = null;
		synchronized (this) {
			if (!isOpen()) {
				throw new ClosedConnectionException();
			}
			stream = fTransport.getOutputStream();
		}

		synchronized (stream) {
			// packet.length can be > packetSize. Sending too much will cause
			// errors on the other side
			stream.write(packet, 0, packetSize);
		}
	}

	private int getPacketLength(byte[] packet) {
		int len = 0;
		if (packet.length >= 4) {
			len = (((packet[0] & 0xFF) << 24) + ((packet[1] & 0xFF) << 16)
					+ ((packet[2] & 0xFF) << 8) + ((packet[3] & 0xFF) << 0));
		}
		return len;
	}
}
