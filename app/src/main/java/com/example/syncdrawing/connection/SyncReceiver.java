package com.example.syncdrawing.connection;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.syncdrawing.packet.Endian;
import com.example.syncdrawing.packet.Packet;
import com.example.syncdrawing.packet.PacketID;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SyncReceiver {
	Handler mHandler = null;
	public SyncReceiver(Handler pHandler) {
		mHandler = pHandler;
	}

	public synchronized void readEvent(byte[] pData) {

		boolean isRunning = true;
		Packet packet = null;
		Log.e("test1", "readEvent 1");
		if (pData.length > 0) {

			// final byte stx = pData[0];
			Log.e("test1", "readEvent 2");
			// if (stx == PacketID.STX) {
			Log.e("test1", "readEvent 3");
			ByteBuffer recvByteBuff = ByteBuffer.allocate(pData.length);
			recvByteBuff.put(pData);
			recvByteBuff.flip();

			while (isRunning) {
				Log.e("test1", "readEvent 4");
				// packetID
				byte[] idBuff = new byte[2];
				recvByteBuff.get(idBuff, 0, 2);
				int packetId = Endian.byteToShortBigEndian(idBuff);
				Log.e("test1", "readEvent 5");
				if (packetId != 0) {
					// 패킷 아이디에 따른 분류
					switch (packetId) {
						case PacketID.BC_BROWSER_NAVIGATE :
						case PacketID.BC_BROWSER_INPUT_KEYBOARD :
						case PacketID.BC_BROWSER_INPUT_MOUSE :
						case PacketID.BC_BROWSER_SCROLL :
							Log.e("test1", "readEvent 6");
							packet = readBrowserData(recvByteBuff, packetId);
							break;
						case PacketID.BC_DRAW_CLEAR :
							packet = readClearEvent(recvByteBuff, packetId);
							break;
						case PacketID.BC_DRAW_GESTURE_DOWN :
							packet = readDrawEvent(recvByteBuff, packetId);
							break;
						case PacketID.BC_DRAW_GESTURE_MOVE :
							packet = readDrawEvent(recvByteBuff, packetId);
							break;
						case PacketID.BC_DRAW_GESTURE_UP :
							packet = readDrawEvent(recvByteBuff, packetId);
							break;
						default :
							break;
					}

					Log.e("test1", "readEvent 7");
					final byte last = recvByteBuff.get();
					if (last == PacketID.ETX) {
						final byte stx = recvByteBuff.get();
						if (stx == PacketID.STX) {
							isRunning = true;
						} else {
							isRunning = false;
						}
						sendMessage(SyncSocket.MessageTypeClass.SIMSOCK_DATA, packet);
					} else {
						isRunning = false;
						packet = null;
					}

				} else {
					isRunning = false;
					packet = null;
				}
			}
		}
	}
	private void sendMessage(int what, Object obj) {
		
		synchronized (mHandler) {
			if (mHandler != null && obj != null) {
				Message msg = Message.obtain();
				msg.what = what;
				msg.obj = obj;
				mHandler.sendMessage(msg);				
				
				Log.e("makeMessage", " msg.what  " + msg.what + "msg.obj:" + msg.obj);
			}
		}
	}

	private Packet readClearEvent(ByteBuffer recvByteBuff, int pPacketId) {
		int packetSize = 0;
		Packet pack = null;
		byte[] dataBuffer = null;
		if (recvByteBuff != null) {
			byte[] sizeBuff = new byte[2];
			recvByteBuff.get(sizeBuff, 0, 2);
			packetSize = Endian.byteToShortBigEndian(sizeBuff);

			if (packetSize != 0) {
				dataBuffer = new byte[packetSize];
				recvByteBuff.get(dataBuffer, 0, packetSize);
				ByteBuffer mDataBuffer = ByteBuffer.wrap(dataBuffer);

				byte B = mDataBuffer.get();
				byte G = mDataBuffer.get();
				byte R = mDataBuffer.get();
				byte A = mDataBuffer.get();

				int intB = Endian.Byte_To_UInt(B);
				int intG = Endian.Byte_To_UInt(G);
				int intR = Endian.Byte_To_UInt(R);
				int intA = Endian.Byte_To_UInt(A);

				pack = new Packet();
				pack.setBackgoundColor(Color.argb(intA, intR, intG, intB));
				pack.mPacketID = (short) pPacketId; // PacketId

				return pack;

			}
		}

		return null;
	}

	private Packet readDrawEvent(ByteBuffer recvByteBuff, int pPacketId) {
		int packetSize = 0;
		Packet pack = null;
		byte[] dataBuffer = null;
		if (recvByteBuff != null) {
			byte[] sizeBuff = new byte[2];
			recvByteBuff.get(sizeBuff, 0, 2);
			packetSize = Endian.byteToShortBigEndian(sizeBuff);

			if (packetSize != 0) {
				dataBuffer = new byte[packetSize];
				recvByteBuff.get(dataBuffer, 0, packetSize);
				ByteBuffer mDataBuffer = ByteBuffer.wrap(dataBuffer);

				byte B = mDataBuffer.get();
				byte G = mDataBuffer.get();
				byte R = mDataBuffer.get();
				byte A = mDataBuffer.get();

				int intB = Endian.Byte_To_UInt(B);
				int intG = Endian.Byte_To_UInt(G);
				int intR = Endian.Byte_To_UInt(R);
				int intA = Endian.Byte_To_UInt(A);

				byte penType = mDataBuffer.get();
				int intPenType = Endian.Byte_To_UInt(penType);

				byte penThickness = mDataBuffer.get();
				int intPenThickness = Endian.Byte_To_UInt(penThickness);

				byte[] sX = new byte[4];
				byte[] sY = new byte[4];

				mDataBuffer.get(sX, 0, 4);
				float fsX = ByteBuffer.wrap(sX).order(ByteOrder.LITTLE_ENDIAN).getFloat();

				mDataBuffer.get(sY, 0, 4);
				float fsY = ByteBuffer.wrap(sY).order(ByteOrder.LITTLE_ENDIAN).getFloat();

				if (fsX >= 0 && fsY <= 1 && fsX >= 0 && fsY >= 0) {

					pack = new Packet();
					pack.setColor(Color.argb(intA, intR, intG, intB));
					pack.setXY(fsX, fsY);
					pack.setPenThickness(intPenThickness);
					pack.setPenType(intPenType);
					pack.mPacketID = (short) pPacketId; // PacketId
					pack.eventTime = System.currentTimeMillis();					
					
					return pack;
				}
			}
		}

		return null;
	}

	private Packet readBrowserData(ByteBuffer recvByteBuff, int pPacketId) {

		int packetSize = 0;
		int pType = 0;
		int packetUrlSize = 0;
		int packetHeaderSize = 0;
		int packetPostSize = 0;

		byte[] dataBuffer = null;
		byte[] urlData = null;
		byte[] headerData = null;
		byte[] postData = null;
		Log.e("test1", "readBrowserData 1");
		if (recvByteBuff != null) {
			// PacketSize
			try {
				byte[] sizeBuff = new byte[2];
				recvByteBuff.get(sizeBuff, 0, 2);
				packetSize = Endian.byteToShortBigEndian(sizeBuff);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.e("test1", "readBrowserData 2");

			if (packetSize != 0) {
				Log.e("test1", "readBrowserData 3");

				dataBuffer = new byte[packetSize];
				recvByteBuff.get(dataBuffer, 0, packetSize);
				ByteBuffer mDataBuffer = ByteBuffer.wrap(dataBuffer);

				if (mDataBuffer.capacity() > 0) {
					Log.e("test1", "readBrowserData 4");

					// get or post
					pType = mDataBuffer.get();
					// url size
					byte[] urlSizeBuff = new byte[2];
					mDataBuffer.get(urlSizeBuff, 0, 2);
					packetUrlSize = Endian.byteToShortBigEndian(urlSizeBuff);
					Log.e("test1", "readBrowserData 5");

					if (packetUrlSize > 0) {
						// url data
						urlData = new byte[packetUrlSize];
						mDataBuffer.get(urlData, 0, packetUrlSize);
						Log.e("test1", "readBrowserData 6");

						// header size
						byte[] headerSizeBuff = new byte[2];
						mDataBuffer.get(headerSizeBuff, 0, 2);
						packetHeaderSize = Endian.byteToShortBigEndian(headerSizeBuff);

						if (packetHeaderSize != 0) {
							// header data
							headerData = new byte[packetHeaderSize];
							mDataBuffer.get(headerData, 0, packetHeaderSize);
							Log.e("test1", "readBrowserData 7");

							// Post size
							byte[] postSizeBuff = new byte[2];
							mDataBuffer.get(postSizeBuff, 0, 2);
							packetPostSize = Endian.byteToShortBigEndian(postSizeBuff);
							Log.e("test1", "readBrowserData 8");

							if (packetPostSize != 0) {
								// Post data
								postData = new byte[packetPostSize];
								mDataBuffer.get(postData, 0, packetPostSize);
								Log.e("test1", "readBrowserData 9");

							}
						}
						Log.e("test1", "readBrowserData 10");

						Packet packet = new Packet();
						packet.mPacketID = (short) pPacketId; // PacketId
						packet.mType = pType; // PacketType
						packet.setUrl(urlData);
						packet.setHeader(headerData);
						packet.setPost(postData);
//						packet.setUserAgent(mUserAgent);
						return packet;

					}
				}
			}
		}
		return null;
	}
}
