import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class Main {

    public static void main(String[] args) {
        int nodes = Integer.parseInt(args[0]);
        List<InetAddress> ips = new ArrayList<InetAddress>(nodes);
        List<Integer> serverPorts = new ArrayList<Integer>(nodes);
        List<Integer> clientPorts = new ArrayList<Integer>(nodes);

        int nodeId = Integer.parseInt(args[1]);

        int i = 2;
        for(int j = 0;j != nodes - 1; j++){
            try {
                serverPorts.add(Integer.parseInt(args[i]));
                ips.add(InetAddress.getByName(args[i+nodes-1]));
                clientPorts.add(Integer.parseInt(args[i+(2*(nodes-1))]));
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
            i++;
        }

        URBLayer urb = new URBLayer();
        new Communication(nodeId, ips, serverPorts, clientPorts, urb);
        new StreamletProtocol(nodes, 3, nodeId, 1L, urb);
    }

}
