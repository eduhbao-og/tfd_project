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
                Block block = (Block) m.getContent()[0];
                for (Message b : messages) {
                    if (((Block) b.getContent()[0]).isEqual(block)) {
                        return;
                    }
                }
                messages.add(m);
            } else {
                Block block = (Block) m.getContent()[0];
                for (Message b : messages) {
                    if (((Block) b.getContent()[0]).isEqual(block) && b.getSender() == m.getSender()) {
                        return;
                    }
                }
                messages.add(m);
            }
        }
    }

    public void deliver(Message m) {
        if (m.getType() == Utils.MessageType.ECHO) {
            Message mes = (Message) m.getContent()[0];
            if (isFirst(mes)) {
                Object[] content = new Object[1];
                content[0] = mes;
                communication.broadcast(new Message(Utils.MessageType.ECHO, content, streamlet.getNode_id()));
                streamlet.URB_deliver(mes);
            }
            return;
        }
        if (isFirst(m)) {
            Object[] content = new Object[1];
            content[0] = m;
            communication.broadcast(new Message(Utils.MessageType.ECHO, content, streamlet.getNode_id()));
            streamlet.URB_deliver(m);
        }
    }

    private synchronized boolean isFirst(Message m) {
        Block block = (Block) m.getContent()[0];
        if (m.getType() == Utils.MessageType.VOTE) {
            for (Message mes : messages) {
                if (((Block) mes.getContent()[0]).isEqual(block) && m.getSender() == mes.getSender()) {
                    return false;
                }
            }
            messages.add(m);
            return true;
        } else {
            for (Message b : messages) {
                if (((Block)b.getContent()[0]).isEqual(block)) {
                    return false;
                }
            }
            messages.add(m);
            return true;
        }
    }
}
