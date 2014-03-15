package pt.up.fe.sdis.proj1.protocols;

import pt.up.fe.sdis.proj1.messages.Message;
import pt.up.fe.sdis.proj1.utils.MulticastChannelMesssagePublisher;
import rx.Observer;
import rx.Subscription;
import rx.functions.Func1;

public abstract class AbstractProtocol implements Observer<Message> {

    public AbstractProtocol(MulticastChannelMesssagePublisher mcmp) {
        _subscription = mcmp.getObservable().subscribe(this);
    }
    
    public AbstractProtocol(MulticastChannelMesssagePublisher mcmp, Func1<Message, Boolean> filter) {
        _subscription = mcmp.getObservable().filter(filter).subscribe(this);
    }
    
    @Override
    public void onCompleted() { }

    @Override
    public void onError(Throwable arg0) { }

    @Override
    public void onNext(Message arg0) { ProcessMessage(arg0); }
    
    public abstract void ProcessMessage(Message msg);
    
    protected Subscription _subscription;

}
