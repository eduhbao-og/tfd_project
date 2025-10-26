import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class URBLayer {
    private Communication communication;
    private StreamletProtocol streamlet;
    private List<Message> messages;

    public URBLayer() {
        messages = new ArrayList<Message>();
    }

    public void setCommunication(Communication communication) {
        this.communication = communication;
    }

    public void setStreamlet(StreamletProtocol streamlet) {
        this.streamlet = streamlet;
    }

    public void start(){
        streamlet.start();
    }

    public synchronized void broadcast(Message m) {
        communication.broadcast(m);
        if(m.getType() != Utils.MessageType.ECHO) {
            if (m.getType() == Utils.MessageType.PROPOSE) {
                Block block = (Block) m.getContent();
                for (Message b : messages) {
                    if (((Block) b.getContent()).isEqual(block)) {
                        return;
                    }
                }
                messages.add(m);
            } else {
                Block block = (Block) m.getContent();
                for (Message b : messages) {
                    if (((Block) b.getContent()).isEqual(block) && b.getSender() == m.getSender()) {
                        return;
                    }
                }
                messages.add(m);
            }
        }
    }

    public void deliver(Message m) {
        if (m.getType() == Utils.MessageType.ECHO) {
            Message mes = (Message) m.getContent();
            if (isFirst(mes)) {
                communication.broadcast(new Message(Utils.MessageType.ECHO, mes, streamlet.getNode_id()));
                streamlet.URB_deliver(mes);
            }
            return;
        }
        if (isFirst(m)) {
            communication.broadcast(new Message(Utils.MessageType.ECHO, m, streamlet.getNode_id()));
            streamlet.URB_deliver(m);
        }
    }

    private synchronized boolean isFirst(Message m) {
        Block block = (Block) m.getContent();
        if (m.getType() == Utils.MessageType.VOTE) {
            for (Message mes : messages) {
                if (((Block) mes.getContent()).isEqual(block) && m.getSender() == mes.getSender()) {
                    return false;
                }
            }
            messages.add(m);
            return true;
        } else {
            for (Message b : messages) {
                if (((Block)b.getContent()).isEqual(block)) {
                    return false;
                }
            }
            messages.add(m);
            return true;
        }
    }
}
