import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class Main {

    public static void main(String[] args) {
        List<InetAddress> ips = new ArrayList<>();
        List<Integer> ports = new ArrayList<>();
        List<Integer> clientPorts = new ArrayList<>();

        try {
            ips.add(InetAddress.getByName("localhost"));
            ports.add(Integer.parseInt(args[2]));
            clientPorts.add(Integer.parseInt(args[3]));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        URBLayer urb = new URBLayer();
        new Communication(ips,  ports, clientPorts, urb);
        new StreamletProtocol(2, 1, Integer.parseInt(args[0]), 1L, urb);
    }

}
