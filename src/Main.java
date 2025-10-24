import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<InetAddress> ips = new ArrayList<>();
        List<Integer> ports = new ArrayList<>();

        URBLayer urb = new URBLayer(ips, ports);
        StreamletProtocol streamlet = new StreamletProtocol(5, 3, 0, 1L, urb);
        Communication communication = new Communication(ips, ports, urb);
    }

}
