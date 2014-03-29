package pt.up.fe.sdis.proj1.protocols.peers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import pt.up.fe.sdis.proj1.BackupSystem;
import pt.up.fe.sdis.proj1.Chunk;
import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.protocols.AbstractProtocol;
import pt.up.fe.sdis.proj1.utils.CounterObserver;
import pt.up.fe.sdis.proj1.utils.MessageFilter;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class PeerChunkRestore extends AbstractProtocol {
	
    public PeerChunkRestore(BackupSystem bs) {
		super(bs.Comm.MC.Publisher);
		_bs = bs;
		
		start(new MessageFilter(Message.Type.GETCHUNK));
	}

	@Override
	protected void ProcessMessage(final Message msg) {
		if (_bs.Files.containsChunk(msg.getFileID(), msg.getChunkNo())) {
		    if (msg.getVersion()[0] == 2) {
		        final Integer uniqueID = _bs.getAddress().toString().hashCode();
		        Message haveChunkMsg = Message.makeHaveChunk(msg.getFileID(), uniqueID, msg.getChunkNo());
		        
		        _bs.Comm.MC.Sender.Send(haveChunkMsg);
		        _bs.Comm.MC.Publisher.getObservable().filter(new MessageFilter(Message.Type.LISTENINGFOR, msg.getFileID(), msg.getChunkNo())).subscribe(new Subscriber<Message>() {

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Message t) {
                        unsubscribe();
                        
                        if (!t.getUniqueID().equals(uniqueID))
                            return;
                        
                        byte[] chunkArray = null;
                        try {
                            chunkArray = _bs.readChunk(t.getFileID(), t.getChunkNo());
                        } catch (FileNotFoundException e) {
                            _bs.Files.removeChunk(t.getFileID(), t.getChunkNo());
                            Message msg1 = Message.makeRemoved(t.getFileID(), t.getChunkNo());
                            _bs.Comm.MC.Sender.Send(msg1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (chunkArray == null)
                            return;
                        
                        System.out.println("Here");
                        Chunk chunk = new Chunk(t.getChunkNo(), t.getFileID(), chunkArray);
                        Message chunkMessage = Message.makeChunk(chunk);
                        byte[] msgArray = chunkMessage.toByteArray();
                        DatagramSocket ds = null;
                        try {
                            ds = new DatagramSocket();
                            System.out.println(t.getPort());
                            DatagramPacket dp = new DatagramPacket(msgArray, msgArray.length, t.Sender, t.getPort());
                            System.out.println("Sending to... " + dp.getAddress());
                            ds.send(dp);
                        } catch (IOException e) {
                            System.out.println("Heree");
                            e.printStackTrace();
                        } finally {
                            if (ds != null)
                                ds.close();
                        }
                    }
                });
		        
		    } else {
                final CounterObserver replyCounter = new CounterObserver();
                byte[] chunkArray = null;
                try {
                    chunkArray = _bs.readChunk(msg.getFileID(), msg.getChunkNo());
                } catch (FileNotFoundException e) {
                    _bs.Files.removeChunk(msg.getFileID(), msg.getChunkNo());
                    Message msg1 = Message.makeRemoved(msg.getFileID(), msg.getChunkNo());
                    _bs.Comm.MC.Sender.Send(msg1);
                } catch (IOException e) {
                }
                if (chunkArray == null)
                    return;

                final Chunk chunk = new Chunk(msg.getChunkNo(), msg.getFileID(), chunkArray);
                final Subscription sub = _bs.Comm.MDR.Publisher.getObservable()
                        .filter(new MessageFilter(Message.Type.CHUNK, msg.getFileID(), msg.getChunkNo())).subscribe(replyCounter);

                Schedulers.io().schedule(new Action1<Scheduler.Inner>() {
                    @Override
                    public void call(Scheduler.Inner arg0) {
                        if (!replyCounter.received())
                            _bs.Comm.MDR.Sender.Send(Message.makeChunk(chunk));
                        sub.unsubscribe();
                    }
                }, rand.nextInt(401), TimeUnit.MILLISECONDS);
		    }
		}
	}
	
	private BackupSystem _bs;
	private static Random rand = new Random();
}
