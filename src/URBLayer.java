import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class URBLayer {
    private List<ObjectOutputStream> outputs ;
    private Communication communication;
    private StreamletProtocol streamlet;
    private List<Message> messages;

    public URBLayer(){
        messages = new ArrayList<Message>();
    }

    public void setCommunication(Communication communication){
        this.communication = communication;
    }

    public void setStreamlet(StreamletProtocol streamlet){
        this.streamlet = streamlet;
    }

    public void broadcast(Message m){
        communication.broadcast(m);
    }

    public void deliver(Message m){
        if (m.getType() == Utils.MessageType.ECHO){
            Message mes = (Message) m.getContent();
            if(isFirst(mes)){
                communication.broadcast(new Message(Utils.MessageType.ECHO, mes, streamlet.getNode_id()));
                streamlet.URB_deliver(mes);
            }
            return;
        }
        if(isFirst(m)){
            communication.broadcast(new Message(Utils.MessageType.ECHO, m, streamlet.getNode_id()));
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
