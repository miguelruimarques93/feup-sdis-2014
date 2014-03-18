package pt.up.fe.sdis.proj1.messages;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import pt.up.fe.sdis.proj1.Chunk;

public class Message {
    public enum Type {
        PUTCHUNK, GETCHUNK, CHUNK, STORED, DELETE, REMOVED
    }

    public final Type type;

    public Message(Type type) {
        this.type = type;
    }

    private byte[] fileID = new byte[32];

    protected void setFileID(byte[] f) {
        fileID = f;
    }

    public byte[] getFileID() {
        return fileID;
    }

    private byte[] version = null;

    protected void setVersion(int i, int j) {
        if (version == null) {
            version = new byte[2];
        }
        version[0] = (byte) i;
        version[1] = (byte) j;
    }

    public int[] getVersion() {
        return new int[] { (int) version[0], (int) version[1] };
    }

    private Integer chunkNo = null;

    private Byte replicationDeg = null;

    private byte[] body = null;

    protected void setChunkNo(int c) {
        chunkNo = c;
    }
    
    public Integer getChunkNo() {
        return chunkNo;
    }

    protected void setReplicationDeg(int i) {
        replicationDeg = (byte) i;
    }

    protected void setBody(byte[] b) {
        body = b;
    }

    public byte[] getBody() { return body; }
    
    public InetAddress Sender = null;

    public byte[] toByteArray() {
        StringBuilder sb = new StringBuilder();

        sb.append(type.toString());

        if (version != null)
            sb.append(' ' + Byte.toString(version[0]) + '.'
                    + Byte.toString(version[1]));

        if (fileID != null) {
            sb.append(' ');
            for (int i = fileID.length - 1; i >= 0; --i)
                sb.append(String.format("%02X", fileID[i]));
        }

        if (chunkNo != null)
            sb.append(' ' + chunkNo.toString());

        if (replicationDeg != null)
            sb.append(' ' + replicationDeg.toString());

        sb.append("\r\n\r\n");

        byte[] result;

        result = sb.toString().getBytes(StandardCharsets.US_ASCII);

        if (body != null) {
            int prevLength = result.length;
            result = Arrays.copyOf(result, prevLength + body.length);
            System.arraycopy(body, 0, result, prevLength, body.length);
        }

        return result;
    }

    public static Message putChunkFromChunk(Chunk chunk) {
        Message result = new Message(Type.PUTCHUNK);

        result.setVersion(1, 0);
        result.setChunkNo(chunk.chunkNo);
        result.setReplicationDeg(chunk.replicationDeg);
        result.setFileID(chunk.fileID);
        result.setBody(chunk.data);

        return result;
    }

    public static Message chunkFromChunk(Chunk chunk) {
        Message result = new Message(Type.CHUNK);

        result.setVersion(1, 0);
        result.setChunkNo(chunk.chunkNo);
        result.setFileID(chunk.fileID);
        result.setBody(chunk.data);

        return result;
    }

    public static Message getChunk(byte[] fileID, int chunkNo) {
        Message result = new Message(Type.GETCHUNK);
        
        result.setVersion(1, 0);
        result.setFileID(fileID);
        result.setChunkNo(chunkNo);
        
        return result;
    }
    
    public static Message delete(byte[] fileID) {
        Message result = new Message(Type.DELETE);
        result.setFileID(fileID);
        return result;
    }
    
    public static Message fromByteArray(byte[] bArray) throws IOException {
        ByteArrayInputStream byteArrayStream = new ByteArrayInputStream(bArray);
        DataInputStream dis = new DataInputStream(byteArrayStream);

        @SuppressWarnings("deprecation")
        String str = dis.readLine();
        String[] msgParams = str.split(" ");

        int paramNum = 0;

        Type t = Type.valueOf(msgParams[paramNum]);
        Message msg = new Message(t);

        if (msg.type != Type.DELETE) {
            paramNum++;
            String version = msgParams[paramNum];
            msg.setVersion(Character.getNumericValue(version.charAt(0)),
                    Character.getNumericValue(version.charAt(2)));
        }

        paramNum++;
        String fileId = msgParams[paramNum];
        ArrayList<String> chars = new ArrayList<String>(Arrays.asList(fileId
                .split("(?<=\\G..)")));
        Collections.reverse(chars);

        for (int i = 0; i < msg.fileID.length; ++i)
            msg.fileID[i] = (byte) Short.parseShort(chars.get(i), 16);

        if (msg.type != Type.DELETE) {
            paramNum++;
            String chunkNoStr = msgParams[paramNum];
            msg.chunkNo = Integer.parseInt(chunkNoStr);
        }

        if (msg.type == Type.PUTCHUNK) {
            paramNum++;
            String replicationDegStr = msgParams[paramNum];
            msg.replicationDeg = Byte.parseByte(replicationDegStr);
        }

        if (msg.type == Type.PUTCHUNK || msg.type == Type.CHUNK) {
            dis.skip(2);
            msg.body = new byte[dis.available()];
            dis.readFully(msg.body);
        }

        return msg;
    }

    public static void main(String[] args) throws IOException {
        Message msg = new Message(Type.PUTCHUNK);
        msg.setVersion(1, 0);
        msg.setBody(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
        msg.setChunkNo(4526);
        msg.setReplicationDeg(3);
        msg.setFileID(new byte[] { 0x4a, 0x2f, 0x3e, 0x4a, 0x4e, 0x43, 0x34,
                0x33, 0x33, 0x34, 0x4e, 0x1c, 0x04, 0x1c, 0x04, 0x1c, 0x55,
                0x4e, 0x38, 0x2d, 0x00, 0x6f, 0x1c, 0x04, 0x1c, 0x3c, 0x55,
                0x56, 0x50, 0x53, 0x70, 0x1a });
        byte[] b = msg.toByteArray();

        System.out.println(Arrays.toString(b));

        Message msg1 = Message.fromByteArray(b);

        System.out.println(Arrays.toString(msg1.toByteArray()));
    }
}
