import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {
    private Utils.MessageType type;
    private Object[] content;
    private int sender;

    public Message(Utils.MessageType type, Object[] content, int sender) {
        this.type = type;
        this.content = content;
        this.sender = sender;
    }

    public Utils.MessageType getType() {
        return type;
    }

    public Object[] getContent() {
        return content;
    }

    public int getSender() {
        return sender;
    }

    @Override
    public String toString(){
        return "MESSAGE: " + "Type: " + type + " Sender: " + sender + "; Content: " + content;
    }
}
