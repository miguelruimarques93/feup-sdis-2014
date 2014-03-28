package pt.up.fe.sdis.proj1.protocols.peers;

import pt.up.fe.sdis.proj1.BackupSystem;
import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.MessageFilter;

public class PeerFileDeletion extends AbstractProtocol {

	public PeerFileDeletion(BackupSystem bs) {
		super(bs.Comm.MC.Publisher);
		_bs = bs;

		start(new MessageFilter(Message.Type.DELETE));
	}

	@Override
	protected void ProcessMessage(Message msg) {
	    System.out.println("DelHere");
	    _bs.Files.addDeletedFile(msg.getFileID());
	    System.out.println("DelHere1");
		if(_bs.Files.containsFile(msg.getFileID())){
			_bs.deletePhysicalFile(msg.getFileID());
		}
	}

	private BackupSystem _bs;
}
