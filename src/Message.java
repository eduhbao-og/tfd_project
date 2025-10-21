public class Message {
    private Utils.MessageType type;
    private Object content;
    private int sender;

    public Message(Utils.MessageType type, Object content, int sender) {
        this.type = type;
        switch (type) {
            case PROPOSE -> {
                if (!(content instanceof Block)) {
                    throw new IllegalArgumentException("For a PROPOSE-type message, the content must be a Block.");
                }
            }
            case VOTE -> {
                if (!(content instanceof Block)) {
                    throw new IllegalArgumentException("For a VOTE-type message, the content must be a Block.");
                }
                if (((Block) content).getTransactions() != null) {
                    throw new IllegalArgumentException("For a VOTE-type message, the Block's transactions field must be null.");
                }
            }
            case ECHO -> {
                if (!(content instanceof Message)) {
                    throw new IllegalArgumentException("For a VOTE-type message, the content must be a Message.");
                }
            }
        }
        this.content = content;
        this.sender = sender;
    }

    public Utils.MessageType getType() {
        return type;
    }

    public Object getContent() {
        return content;
    }

    public int getSender() {
        return sender;
    }
}
