import java.io.*;
import java.util.List;

public class Utils {

    public enum MessageType {PROPOSE, VOTE, ECHO, SYNC, RECONNECT}
    public enum BlockStatus {PROPOSED, NOTARIZED, FINALIZED}

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deepCopy(List<Block> object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.flush();

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);

            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Deep copy failed", e);
        }
    }

}
