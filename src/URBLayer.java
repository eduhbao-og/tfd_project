import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class URBLayer {
    private List<ObjectOutputStream> outputs ;
    private Communication communication;
    private List<Message> messages;

    public URBLayer(List<InetAddress> ips, List<Integer> ports){
        messages = new ArrayList<Message>();
        communication = new Communication(ips, ports, this);
    }

    public void receive(Message m){
        if(isFirst(m)){
            communication.broadcast(m);
            //streamlet deliver
        }
    }

    private boolean isFirst(Message m){
        if (messages.contains(m))
            return true;
        messages.add(m);
        return false;
    }

}
