public class Block {
    private String hash;
    private int epoch;
    private int length;
    private Transaction[] transactions;

    public Block(String hash, int epoch) {
        this.hash = hash;
        this.epoch = epoch;
    }

    public Block(String hash, int epoch, Transaction[] transactions) {
        this.hash = hash;
        this.epoch = epoch;
        this.transactions = transactions;
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

    public String getHash() {
        return hash;
    }
}
