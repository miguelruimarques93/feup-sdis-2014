package pt.up.fe.sdis.proj1.protocols.peers;

import java.io.File;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.BackupSystem;
import rx.functions.Func1;

public class PeerFileDeletion extends AbstractProtocol {

	public PeerFileDeletion(BackupSystem bs) {
		super(bs.Comm.MC.Publisher);
		_bs = bs;

		start(new Func1<Message, Boolean>(){
			@Override
			public Boolean call(Message arg0) {
				return arg0.type == Message.Type.DELETE;
			}
		});
	}

	static public boolean deleteDirectory(File path) {
		if( path.exists() ) {
			File[] files = path.listFiles();
			for(int i=0; i<files.length; i++) {
				if(files[i].isDirectory()) {
					deleteDirectory(files[i]);
				}
				else {
					files[i].delete();
				}
			}
		}
		return( path.delete() );
	}

	@Override
	protected void ProcessMessage(Message msg) {
		if(_bs.Files.containsFile(msg.getFileID())){
			_bs.Files.removeFile(msg.getFileID());
			File dir = new File("backups/"+msg.getHexFileID());
			deleteDirectory(dir);
		}
	}

	private BackupSystem _bs;
}
