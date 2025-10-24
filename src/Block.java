import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Block implements Serializable {
    private Utils.BlockStatus status;
    private String prev_hash;
    private String hash;
    private int epoch;
    private int length;
    private List<Transaction> transactions = new ArrayList<>();

    public Block(Utils.BlockStatus status, String prev_hash, String hash, int epoch, int length, List<Transaction> transactions) {
        this.status = status;
        this.prev_hash = prev_hash;
        this.hash = hash;
        this.epoch = epoch;
        this.length = length;
        this.transactions = transactions;
    }

    private static String generateHash() {
        byte[] array = new byte[5];
        new Random().nextBytes(array);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = digest.digest(array);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    public List<Transaction> getTransactions() {
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

    public static Block createGenesisBlock() {
        return new Block(Utils.BlockStatus.NOTARIZED, "0", "0", 0, 0, new ArrayList<>());
    }

    public static Block createBlock(String prev_hash, int epoch, int length, List<Transaction> transactions) {
        return new Block(Utils.BlockStatus.PROPOSED, prev_hash, generateHash(), epoch, length, transactions);
    }
}
