import java.io.Serializable;

public class Transaction implements Serializable {
    private int sender;
    private int receiver;
    private int id;
    private double amount;

    public Transaction(int sender, int receiver, int id, double amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.id = id;
        this.amount = amount;
    }

    public int getSender() {
        return sender;
    }

    public int getReceiver() {
        return receiver;
    }

    public int getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Sender: " + sender + " Id: " + id;
    }
}
