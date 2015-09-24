package com.example.syncdrawing.packet;

import android.util.*;

public class Packet {

	public static final int intSTX = 211;
	public static final int intETX = 122;

	public static final byte sFlatformType = 2;
	private final int BUFFER_SIZE = 1024;

	public byte[] mPacketData = new byte[BUFFER_SIZE];

	public short mPacketID;
	public int mType = 0;
	// public int mPacketUrlSize = 0;
	// public int mPacketHeaderSize = 0;
	// public int mPacketPostSize = 0;

	public int mPackgetDataSize = 0;
	private short mOsVersion = 0;
	public int packetSize = 0;
	public int pType = 0;
	private String mUrl = "";
	private String mHeader = "";
	private String mPost = "";
	private String mUserAgent = "";
	private float X = 0;
	private float Y = 0;

	private int penType = 0;
	private int penThickness = 0;

	public int colorBackgroudnARGB = 0;
	public int colorPenARGB = 0;
	public long eventTime = 0;
	public byte[] sizeBuff = new byte[2];
	public Packet() {

	}
	public short getOsVersion() {
		return mOsVersion;
	}
	public void setOsVersion(short mOsVersion) {
		this.mOsVersion = mOsVersion;
	}
	public String getUrl() {
		return mUrl;
	}

	public void setUrl(byte[] pUrl) {
		if (pUrl != null) {
			this.mUrl = new String(pUrl);
		} else {
			this.mUrl = "";
		}
	}

	public String getHeader() {
		return mHeader;
	}

	public void setHeader(byte[] pHeader) {
		if (pHeader != null) {
			this.mHeader = new String(pHeader);

			Log.e("test", "test");
			if (mHeader.contains("User-Agent:")) {
				int index = mHeader.indexOf("User-Agent:");
				if (index != 0) {
					String userAgent = mHeader.substring(index);
					String[] array = userAgent.split("User-Agent:");
					if (array.length > 1 && array[1].length() > 0) {
						this.mUserAgent = array[1].trim();
					}

				}
			}
		} else {
			this.mHeader = "";
		}
	}
	public String getPost() {
		return mPost;
	}
	public void setPost(byte[] pPost) {
		if (pPost != null) {
			this.mPost = new String(pPost);
		} else {
			this.mPost = "";
		}
	}
	public float getX() {
		return X;
	}

	public float getY() {
		return Y;
	}

	public void setXY(float X, float Y) {
		this.X = X;
		this.Y = Y;
	}

	public void setColor(int colorPenARGB) {
		this.colorPenARGB = colorPenARGB;
	}

	public int getPenColor() {
		return this.colorPenARGB;
	}

	public void setBackgoundColor(int colorBackgroundARGB) {
		this.colorBackgroudnARGB = colorBackgroundARGB;
	}
	public int getBackgounrColor() {
		return this.colorBackgroudnARGB;
	}

	public int getPenThickness() {
		return penThickness;
	}
	public void setPenThickness(int penThickness) {
		this.penThickness = penThickness;
	}
	public int getPenType() {
		return penType;
	}
	public void setPenType(int penType) {
		this.penType = penType;
	}
	public String getUserAgent() {
		return mUserAgent;
	}
	public void setUserAgent(String mUserAgent) {
		this.mUserAgent = mUserAgent;
	}

}
