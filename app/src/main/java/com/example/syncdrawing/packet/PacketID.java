package com.example.syncdrawing.packet;

public class PacketID {
	public static final byte STX = (byte) 0xD3;

	public static final byte ETX = (byte) 0x7A;

	public final static short CS_CONN_PING = 11001;

	public final static short SC_CONN_PING = 21001;

	public final static short CS_CONN_PONG = 11002;

	public final static short SC_CONN_PONG = 21002;

	public final static short CS_CONN_FLATFORM = 11003;

	public final static short SC_CONN_FLATFORM = 21003;

	public final static short CS_CONN_LOGIN = 11004;

	public final static short SC_CONN_LOGIN = 21004;

	public final static short BC_DRAW_CLEAR = (short) 33001;
	
	public final static short BC_DRAW_GESTURE_DOWN = (short) 33002;
	public final static short BC_DRAW_GESTURE_MOVE = (short) 33003;
	public final static short BC_DRAW_GESTURE_UP = (short) 33004;

	public final static short BC_BROWSER_NAVIGATE = 32001;

	public final static short BC_BROWSER_INPUT_KEYBOARD = 32002;

	public final static short BC_BROWSER_INPUT_MOUSE = 32003;

	public final static short BC_BROWSER_SCROLL = 32004;

}
