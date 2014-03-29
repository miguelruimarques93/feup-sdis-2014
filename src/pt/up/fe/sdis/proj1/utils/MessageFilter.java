package pt.up.fe.sdis.proj1.utils;

import pt.up.fe.sdis.proj1.messages.Message;
import rx.functions.Func1;

public class MessageFilter implements Func1<Message, Boolean> {

	public MessageFilter(Message.Type type, FileID fileId, Integer chunkNo) {
		_type = type;
		_fileId = fileId;
		_chunkNo = chunkNo;
	}
	
	public MessageFilter(Message.Type type, FileID fileId) {
		_type = type;
		_fileId = fileId;
		_chunkNo = null;
	}
	
	public MessageFilter(Message.Type type) {
		_type = type;
		_fileId = null;
		_chunkNo = null;
	}
	
	@Override
	public Boolean call(Message arg0) {
		return (arg0.type == _type) &&
			   (_fileId == null ? true : arg0.getFileID().equals(_fileId)) &&
			   (_chunkNo == null ? true : arg0.getChunkNo().equals(_chunkNo));
	}
	
	private final Message.Type _type;
	private final FileID _fileId;
	private final Integer _chunkNo;
}
