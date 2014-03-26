package pt.up.fe.sdis.proj1.protocols.peers;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.BackupSystem;
import pt.up.fe.sdis.proj1.utils.MessageFilter;

public class PeerFileDeletion extends AbstractProtocol {

	public PeerFileDeletion(BackupSystem bs) {
		super(bs.Comm.MC.Publisher);
		_bs = bs;

		start(new MessageFilter(Message.Type.DELETE));
	}

	@Override
	protected void ProcessMessage(Message msg) {
		if(_bs.Files.containsFile(msg.getFileID())){
			_bs.deleteFile(msg.getFileID());
		}
	}

	private BackupSystem _bs;
}
