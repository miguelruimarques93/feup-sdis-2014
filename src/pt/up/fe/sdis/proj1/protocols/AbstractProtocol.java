package pt.up.fe.sdis.proj1.protocols;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.utils.MulticastChannelMesssagePublisher;
import rx.Subscriber;
import rx.functions.Func1;

public abstract class AbstractProtocol extends Subscriber<Message> {

    public AbstractProtocol(MulticastChannelMesssagePublisher mcmp) {
        _mcmp = mcmp;
    }

    protected final void start() {
        _mcmp.getObservable().subscribe(this);
    }
    
    protected final void start(Func1<Message, Boolean> filter) {
        _mcmp.getObservable().filter(filter).subscribe(this);
    }
    
    public final void finish() {
        unsubscribe();
    }
    
    public final boolean isFinished() {
        return isUnsubscribed();
    }
    
    @Override
    public void onCompleted() { }

    @Override
    public void onError(Throwable arg0) { }

    @Override
    public void onNext(Message arg0) { ProcessMessage(arg0); }
    
    protected abstract void ProcessMessage(Message msg);
    
    private MulticastChannelMesssagePublisher _mcmp;

}
