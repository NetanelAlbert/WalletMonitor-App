package albert.netanel.walletmonitor.database;

/**
 * Created by nati on 4/9/18.
 * represent one raw at expenses table
 */

public class ExpenseDao {
    private int id;
    private double amount;
    private int currency;
    private int category;
    private String date;
    private String time;
    private String description;
    private String notice;
    private double originalAmount;

    public ExpenseDao (int id, double amount, int currency, int category, String date, String time, String description, String notice, double originalAmount){
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.category = category;
        this.date = date;
        this.time = time;
        this.description = description;
        this.notice = notice;
        this.originalAmount = originalAmount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getCurrency() {
        return currency;
    }

    public void setCurrency(int currency) {
        this.currency = currency;
    }

    public int getCategory() {
        return category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public double getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(double originalAmount) {
        this.originalAmount = originalAmount;
    }
}
