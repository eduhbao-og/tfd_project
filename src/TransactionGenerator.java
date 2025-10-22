import java.util.ArrayList;
import java.util.List;

public class TransactionGenerator {

    private int transaction_id = 0;
    private final int node_id;
    private final int num_nodes;

    public TransactionGenerator(int node_id, int num_nodes) {
        this.node_id = node_id;
        this.num_nodes = num_nodes;
    }

    public List<Transaction> getTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            int receiver = -1;
            while(receiver < 0 || receiver == node_id) {
                receiver = (int) (Math.random() * num_nodes);
            }
            transactions.add(new Transaction(node_id, receiver, transaction_id++, Math.random()*100));
        }
        return transactions;
    }

}
