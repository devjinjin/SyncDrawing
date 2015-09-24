package com.example.syncdrawing.connection;

import java.io.*;
import java.net.*;
import java.nio.*;

import android.content.*;
import android.os.*;
import android.util.*;

import com.example.syncdrawing.packet.Packet;


public class SyncSocket extends Thread {

	private Socket mSocket;

	// private BufferedReader buffRecv;
	// private BufferedWriter buffSend;
	DataInputStream in;
	DataOutputStream out;

	private String mAddr = "175.198.74.199";
	private int mPort = 2315;
	public static int BUFFER_SIZE = 1024 * 10;
	public static final byte STX = (byte) 0xD3;
	public static final byte ETX = (byte) 0x7A;
	public static final int intSTX = 211;
	public static final int intETX = 122;
	private boolean mConnected = false;
	private Handler mHandler = null;
	private SyncReceiver mReceiver = null;
	private SyncSender mSender = null;
	private Context mContext = null;
	public static class MessageTypeClass {
		public static final int SIMSOCK_CONNECTED = 0;
		public static final int SIMSOCK_DATA = 2;
		public static final int SIMSOCK_DISCONNECTED = 1;
		public static final int SIMSOCK_DISCONNECTED_2 = 3;
	};

	public SyncSocket(Context pContext, String addr, int port, Handler handler) {
		mAddr = addr;
		mPort = port;
		mHandler = handler;
		mContext = pContext;
		mReceiver = new SyncReceiver(mHandler);
		mSender = new SyncSender();
	}

	private void makeMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
		Log.e("makeMessage", " msg.what  " + msg.what + "msg.obj:" + msg.obj);
	}

	private boolean connect(String addr, int port) {
		try {
			InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(addr), port);
			mSocket = new Socket();
			mSocket.connect(socketAddress, 5000);

		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
			return false;
		}
		return true;
	}
	@Override
	public void start() {
		mConnected = true;
		super.start();
	}

	@Override
	public void run() {
		if (!connect(mAddr, mPort)) {
			mContext.sendBroadcast(new Intent("syncdrawing.example.com.connectfail"));
			return; // connect failed
		}
		if (mSocket == null) {
			mContext.sendBroadcast(new Intent("syncdrawing.example.com.connectfail"));
			return;
		}

		try {
			// buffRecv = new BufferedReader(new
			// InputStreamReader(mSocket.getInputStream(), "utf-8"));
			// buffSend = new BufferedWriter(new
			// OutputStreamWriter(mSocket.getOutputStream(), "utf-8"));
			in = new DataInputStream(mSocket.getInputStream());
			out = new DataOutputStream(mSocket.getOutputStream());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// mConnected = true;

		makeMessage(SyncSocket.MessageTypeClass.SIMSOCK_CONNECTED, "");
		Log.d("SimpleSocket", "socket_thread loop started");

		while (mConnected) {

			// String aLine1 = buffRecv.;
			// int value = buffRecv.read();

			try {

				Log.e("test", "receive data 1 : " + new String());
				if (in.readByte() == STX) {
					byte[] recv = new byte[BUFFER_SIZE];
					in.read(recv);
					Log.e("test", "receive data 2");

					mReceiver.readEvent(recv);
				}

			} catch (SocketException e) {
				e.printStackTrace();
				try {
					in.close();
					out.close();
					disconnect();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			} catch (Exception e) {

				e.printStackTrace();
			}
		}

		try {
			in.close();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mConnected = false;
	}

	synchronized public boolean isConnected() {
		return mConnected;
	}

	public void disconnect() {
		try {
			mConnected = false;
			if (!mSocket.isClosed()) {
				mSocket.close();
			}

			if (!isInterrupted()) {
				interrupt();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public boolean sendClearEvent(Packet pPack) {
		if (out != null) {
			byte[] sendData = mSender.sendClearEvent(pPack);

			if (sendData != null && sendData.length > 0) {
				try {
					out.write(sendData);
					return true;

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return false;
	}
	public boolean sendDrawingEvent(Packet pPack) {
		if (out != null) {
			byte[] sendData = mSender.sendDrawingEvent(pPack);

			if (sendData != null && sendData.length > 0) {
				try {
			
						out.write(sendData);
						return true;
			
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	public boolean sendNavigationEvent(String url, String userAgent) {
		if (out != null) {
			byte[] sendData = mSender.sendNavigationEvent(url, userAgent);

			try {
				out.write(sendData);
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

}