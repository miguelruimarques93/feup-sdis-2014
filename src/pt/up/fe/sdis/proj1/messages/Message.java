package pt.up.fe.sdis.proj1.messages;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class Message  {
	public enum Type {
		PUTCHUNK,
		GETCHUNK,
		CHUNK,
		STORED,
		DELETE,
		REMOVED
	}
	
	private static final byte[] CRLF = { 0xD, 0xA };
	private static final byte SPACE = 32;
	
	public final Type type;
	
	public Message(Type type){
		this.type = type;
	}
	
	private byte[] fileID = new byte[32];
	
	private byte[] version = null;
	
	
	public void setVersion(byte major, byte minor){
		if (version == null) {
			version = new byte[2];
		}
		version[0] = major;
		version[1] = minor;
	}
	
	public byte[] getVersion() {
		return version;
	}
	
	private Integer chunkNo = null;
	
	private Byte replicationDeg = null;
	
	private byte[] body = null;
	
	
	public byte[] toByteArray() {
		ByteBuffer bb = ByteBuffer.allocate(90);
		bb.put(type.toString().getBytes());
		
		if (version != null) {
			bb.put(SPACE);
			bb.put((Byte.toString(version[0]) + '.' + Byte.toString(version[1])).getBytes());
		}
		
		if (fileID != null) {
			bb.put(SPACE);
			for (int i = fileID.length - 1; i >= 0; --i) {
				bb.put(String.format("%02x", fileID[i]).getBytes());
			}
		}
		
		if (chunkNo != null) {
			bb.put(SPACE);
			String chunkNoStr = chunkNo.toString();
			if (chunkNoStr.length() <= 6) bb.put(chunkNoStr.getBytes());
		}

		if (replicationDeg != null) {
			bb.put(SPACE);
			bb.put(replicationDeg.toString().getBytes());
		}
		
		bb.put(CRLF);
		bb.put(CRLF);
		
		byte[] result = new byte[bb.position()];
		bb.position(0);
		bb.get(result);
		
		if (body != null) {
			int prevLength = result.length;
			result = Arrays.copyOf(result, prevLength + body.length);
			System.arraycopy(body, 0, result, prevLength, body.length);
		}
		
		return result;
	}
	
	public static Message fromByteArray(byte[] bArray) {
		ByteBuffer bb = ByteBuffer.wrap(bArray);
		StringBuilder sb = new StringBuilder();
		
		for (char c = (char)bb.get(); c != ' '; c = (char)bb.get()) sb.append(c);
		Type t = Type.valueOf(sb.toString());
		Message msg = new Message(t);
		
		return msg;
	}
	
	public static void main(String[] args) {
		Message msg = new Message(Type.REMOVED);
		byte[] b = msg.toByteArray();
		
		Message msg1 = Message.fromByteArray(b);
		System.out.println(msg1.type);
	}
}
