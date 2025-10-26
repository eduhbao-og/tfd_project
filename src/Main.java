import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class Main {

    public static void main(String[] args) {
        int nodes = Integer.parseInt(args[0]);
        List<InetAddress> ips = new ArrayList<InetAddress>(nodes);
        List<Integer> clientPorts = new ArrayList<Integer>(nodes);

        int nodeId = Integer.parseInt(args[1]);

        int serverPort = Integer.parseInt(args[2]);

        int i = 3;
        for(int j = 0;j != nodes - 1; j++){
            try {
                ips.add(InetAddress.getByName(args[i]));
                clientPorts.add(Integer.parseInt(args[i+nodes-1]));
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
            i++;
        }

        URBLayer urb = new URBLayer();
        new StreamletProtocol(nodes, 3, nodeId, 1L, urb);
        new Communication(nodeId, ips, serverPort, clientPorts, urb);
    }

}
