package pt.up.fe.sdis.proj1.messages;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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
	
	
	public byte[] toByteArray() throws UnsupportedEncodingException {
	    StringBuilder sb = new StringBuilder();
	    
	    sb.append(type.toString());
	    
	    if (version != null)
	        sb.append(' ' + Byte.toString(version[0]) + '.' + Byte.toString(version[1]));
	    
	    if (fileID != null) {
	        sb.append(' ');
	        for (int i = fileID.length - 1; i >= 0; --i) sb.append(String.format("%02X", fileID[i]));
	    }
	    
	    if (chunkNo != null)
	        sb.append(' ' + chunkNo.toString());
	    
	    if (replicationDeg != null) sb.append(' ' + replicationDeg.toString());
	    
		sb.append("\r\n\r\n");
		
		byte[] result = sb.toString().getBytes("US-ASCII");
		
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
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		Message msg = new Message(Type.CHUNK);
		byte[] b = msg.toByteArray();
		
		Message msg1 = Message.fromByteArray(b);
		System.out.println(msg1.type);
	}
}
