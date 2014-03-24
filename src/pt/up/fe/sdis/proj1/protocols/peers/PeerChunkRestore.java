package pt.up.fe.sdis.proj1.protocols.peers;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import pt.up.fe.sdis.proj1.Chunk;
import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.BackupSystem;
import pt.up.fe.sdis.proj1.utils.CounterObserver;
import pt.up.fe.sdis.proj1.utils.MyFile;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class PeerChunkRestore extends AbstractProtocol {
	
	public PeerChunkRestore(BackupSystem bs) {
		super(bs.Comm.MC.Publisher);
		_bs = bs;
		
		start(new Func1<Message, Boolean>(){
			@Override
			public Boolean call(Message arg0) {
				return arg0.type == Message.Type.GETCHUNK;
			}
		});
	}

	@Override
	protected void ProcessMessage(final Message msg) {
		if (_bs.Files.containsChunk(msg.getFileID(), msg.getChunkNo())) {
			final CounterObserver obs = new CounterObserver();
			
			byte[] chunkArray;
			
			try {
				chunkArray = MyFile.ReadChunk(msg.getFileID(), msg.getChunkNo());
			} catch (IOException e) {
				return;
			}
			
			final Chunk chunk = new Chunk(msg.getChunkNo(), 0, msg.getFileID(), chunkArray);
			
			final Subscription sub = _bs.Comm.MDR.Publisher.getObservable()
					.filter(new Func1<Message, Boolean>() {
						@Override
						public Boolean call(Message arg0) {
							return arg0.type == Message.Type.CHUNK
									&& arg0.getFileID().equals(msg.getFileID())
									&& arg0.getChunkNo().equals(
											msg.getChunkNo());
						}
					}).subscribe(obs);
			
			
	        Schedulers.io().schedule(new Action1<Scheduler.Inner>() {
	            @Override
	            public void call(Scheduler.Inner arg0) {
	                if (!obs.received()) _bs.Comm.MDR.Sender.Send(Message.makeChunk(chunk));
	                sub.unsubscribe();
	            }
	        }, rand.nextInt(401), TimeUnit.MILLISECONDS);
		}
	}
	
	private BackupSystem _bs;
	private static Random rand = new Random();
}
