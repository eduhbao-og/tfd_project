import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class URBLayer {
    private List<ObjectOutputStream> outputs;
    private Communication communication;
    private StreamletProtocol streamlet;
    private List<Block> blocks;

    public URBLayer() {
        blocks = new ArrayList<Block>();
    }

    public void setCommunication(Communication communication) {
        this.communication = communication;
    }

    public void setStreamlet(StreamletProtocol streamlet) {
        this.streamlet = streamlet;
    }

    public synchronized void broadcast(Message m) {
        System.out.println("SENT: " + m);
        communication.broadcast(m);
        if(m.getType() != Utils.MessageType.ECHO){
            Block block = (Block) m.getContent();
            for (Block b : blocks) {
                if (b.isEqual(block)) {
                    return ;
                }
            }
            blocks.add(block);
        }
    }

    public void deliver(Message m) {
        if (m.getType() == Utils.MessageType.ECHO) {
            Message mes = (Message) m.getContent();
            if (isFirst(mes)) {
                System.out.println("RECEIVED ECHO: " + mes);
                communication.broadcast(new Message(Utils.MessageType.ECHO, mes, streamlet.getNode_id()));
                streamlet.URB_deliver(mes);
            }
            return;
        }
        if (isFirst(m)) {
            System.out.println("RECEIVED: " + m);
            communication.broadcast(new Message(Utils.MessageType.ECHO, m, streamlet.getNode_id()));
            streamlet.URB_deliver(m);
        }
    }

    private synchronized boolean isFirst(Message m) {
        Block block = (Block) m.getContent();
        for (Block b : blocks) {
            if (b.isEqual(block)) {
                return false;
            }
        }
        blocks.add((Block) m.getContent());
        return true;
    }

}
