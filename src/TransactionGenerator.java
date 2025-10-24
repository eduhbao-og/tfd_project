import java.util.ArrayList;
import java.util.List;

public class TransactionGenerator {

    private int transaction_id = 0;
    private final int num_nodes;

    public TransactionGenerator(int num_nodes) {
        this.num_nodes = num_nodes;
    }

    public List<Transaction> getTransactions(int num_transactions) {
        List<Transaction> transactions = new ArrayList<>();
        for(int i = 0; i < num_transactions; i++) {
            transactions.add(new Transaction((int) (Math.random()*num_nodes),
                    (int) (Math.random()*num_nodes), transaction_id++, Math.random()*100));
        }
        return transactions;
    }

}
