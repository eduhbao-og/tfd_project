import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Block {
    private Utils.BlockStatus status;
    private String prev_hash;
    private String hash;
    private int epoch;
    private int length;
    private Transaction[] transactions;

    public Block(String prev_hash, int epoch, int length) {
        generateHash();
        this.status = Utils.BlockStatus.NORMAL;
        this.prev_hash = prev_hash;
        this.epoch = epoch;
        this.length = length;
    }

    public Block(String prev_hash, int epoch, int length, Transaction[] transactions) {
        generateHash();
        this.status = Utils.BlockStatus.NORMAL;
        this.prev_hash = prev_hash;
        this.epoch = epoch;
        this.length = length;
        this.transactions = transactions;
    }

    private void generateHash() {
        byte[] array = new byte[5];
        new Random().nextBytes(array);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = digest.digest(array);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            hash = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    public Transaction[] getTransactions() {
        return transactions;
    }

    public int getLength() {
        return length;
    }

    public int getEpoch() {
        return epoch;
    }

    public String getPrevHash() {
        return prev_hash;
    }

    public String getHash() {
        return hash;
    }

    public Utils.BlockStatus getStatus() {
        return status;
    }

    public void setStatus(Utils.BlockStatus status) {
        this.status = status;
    }
}
