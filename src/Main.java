import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class Main {

    public static void main(String[] args) {
        try {
            boolean byzantine = false;
            Scanner scanner = new Scanner(new File(args[1]));

            int nodes = Integer.parseInt(scanner.nextLine());
            List<InetAddress> ips = new ArrayList<InetAddress>(nodes);
            List<Integer> clientPorts = new ArrayList<Integer>(nodes);

            String[] line ;

            int nodeId = Integer.parseInt(args[0]);

            int serverPort = 0;

            for (int i = 1; scanner.hasNextLine(); i++) {
                line = scanner.nextLine().split(":");
                if(i == nodeId){
                    serverPort = Integer.parseInt(line[1]);
                }else {
                    try {
                        ips.add(InetAddress.getByName(line[0]));
                        clientPorts.add(Integer.parseInt(line[1]));
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            scanner.close();

            if(args.length >= 3)
                byzantine = args[2].equals("true");

            URBLayer urb = new URBLayer();
            new StreamletProtocol(nodes, 3, nodeId, 1L, urb);
            new Communication(nodeId, ips, serverPort, clientPorts, urb);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

}
