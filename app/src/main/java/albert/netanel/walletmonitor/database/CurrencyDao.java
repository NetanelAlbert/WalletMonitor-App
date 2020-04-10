package albert.netanel.walletmonitor.database;

/**
 * Created by nati on 4/9/18.
 * represent one raw at currencies table
 */

public class CurrencyDao {
    private int id;
    private String name;
    private int priority;

    public CurrencyDao(int id, String name, int priority){
        this.id = id;
        this.name = name;
        this.priority = priority;
    }

    @Override
    public String toString(){
        return this.name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
