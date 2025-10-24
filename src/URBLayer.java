import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class URBLayer {
    private List<ObjectOutputStream> outputs ;
    private Communication communication;
    private StreamletProtocol streamlet;
    private List<Message> messages;

    public URBLayer(List<InetAddress> ips, List<Integer> ports){
        messages = new ArrayList<Message>();
    }

    public void setCommunication(Communication communication){
        this.communication = communication;
    }

    public void setCommunication(StreamletProtocol streamlet){
        this.streamlet = streamlet;
    }

    public void broadcast(Message m){
        communication.broadcast(m);
    }

    public void deliver(Message m){
        if(isFirst(m)){
            communication.broadcast(m);
            streamlet.URB_deliver(m);
        }
    }

    private boolean isFirst(Message m){
        if (messages.contains(m)) {
            return true;
        }
        messages.add(m);
        return false;
    }

}
