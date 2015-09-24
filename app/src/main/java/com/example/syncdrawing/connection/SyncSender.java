package com.example.syncdrawing.connection;


import com.example.syncdrawing.packet.Endian;
import com.example.syncdrawing.packet.Packet;
import com.example.syncdrawing.packet.PacketID;

import java.nio.ByteBuffer;

public class SyncSender {

	public SyncSender() {

	}

	public byte[] sendClearEvent(Packet pPack) {
		ByteBuffer bufferData = ByteBuffer.allocate(SyncSocket.BUFFER_SIZE);

		bufferData.put(SyncSocket.STX);

		byte[] id = Endian.ShortTobyteLittleEndian(pPack.mPacketID);
		bufferData.put(id);

		byte[] size = Endian.ShortTobyteLittleEndian((byte) 4);
		bufferData.put(size);

		int color = pPack.getPenColor();
		// color
		bufferData.put((byte) ((color) & 0xFF)); // b
		bufferData.put((byte) ((color >> 8) & 0xFF)); // g
		bufferData.put((byte) ((color >> 16) & 0xFF)); // r
		bufferData.put((byte) ((color >> 24) & 0xFF)); // a

		bufferData.put(SyncSocket.ETX);

		byte[] sendData = new byte[bufferData.position()];
		bufferData.flip();
		bufferData.get(sendData);

		return sendData;
	}

	public byte[] sendDrawingEvent(Packet pPack) {
		ByteBuffer bufferData = ByteBuffer.allocate(SyncSocket.BUFFER_SIZE);

		bufferData.put(SyncSocket.STX);

		byte[] id = Endian.ShortTobyteLittleEndian(pPack.mPacketID);
		bufferData.put(id);

		byte[] size = Endian.ShortTobyteLittleEndian((byte) 14);
		bufferData.put(size);

		int color = pPack.getPenColor();

		// color
		bufferData.put((byte) ((color) & 0xFF)); // b
		bufferData.put((byte) ((color >> 8) & 0xFF)); // g
		bufferData.put((byte) ((color >> 16) & 0xFF)); // r
		bufferData.put((byte) ((color >> 24) & 0xFF)); // a

		// pen type
		bufferData.put((byte) pPack.getPenType());

		// pen thickness
		bufferData.put((byte) pPack.getPenThickness());

		// start x
		byte[] sX = ByteBuffer.allocate(4).putFloat(pPack.getX()).array();

		bufferData.put(Endian.convertEndign(sX));

		// start y
		byte[] sY = ByteBuffer.allocate(4).putFloat(pPack.getY()).array();
		bufferData.put(Endian.convertEndign(sY));

		bufferData.put(SyncSocket.ETX);

		byte[] sendData = new byte[bufferData.position()];
		bufferData.flip();
		bufferData.get(sendData);

		return sendData;
	}

	public byte[] sendNavigationEvent(String url, String userAgent) {

	
			ByteBuffer bufferData = ByteBuffer.allocate(SyncSocket.BUFFER_SIZE);
			// // get or post - 1
			bufferData.put((byte) 0);

			// url data
			String urlData = url;
			short urlLen = (short) urlData.getBytes().length;

			// url size - 72
			// url data
			byte[] urlSize = Endian.ShortTobyteLittleEndian(urlLen);
			bufferData.put(urlSize);
			bufferData.put(urlData.getBytes());

			// header size
			// header data 
//			String headerData = "Header : Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8 \nReferer: http://www.naver.com/\nUser-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2378.0 Safari/537.36";
			String headerData = "Header : Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8 \nReferer: "+urlData+"\nUser-Agent: "+userAgent;
			short headerLen = (short) headerData.getBytes().length;
			byte[] headerSize = Endian.ShortTobyteLittleEndian(headerLen);

			bufferData.put(headerSize);
			bufferData.put(headerData.getBytes());

			// post size - 0
			// post data
			bufferData.put(new byte[]{0, 0});

			// data/////////////////////////////////////////

			ByteBuffer buffer = ByteBuffer.allocate(SyncSocket.BUFFER_SIZE);
			// stx
			byte stx = (byte) SyncSocket.intSTX;
			buffer.put(stx);

			// id - 32001
			byte[] id = Endian.ShortTobyteLittleEndian(PacketID.BC_BROWSER_NAVIGATE);
			buffer.put(id);

			byte[] objectData = new byte[bufferData.position()];
			bufferData.flip();
			bufferData.get(objectData);

			short objectSize = (short) objectData.length;
			// size 280
			byte[] dataSize = Endian.ShortTobyteLittleEndian(objectSize);
			buffer.put(dataSize);

			// data
			buffer.put(objectData);
			// etx
			buffer.put((byte) SyncSocket.intETX);

			byte[] sendData = new byte[buffer.position()];
			buffer.flip();
			buffer.get(sendData);

			return sendData;

	}

}
