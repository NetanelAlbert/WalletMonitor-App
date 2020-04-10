package albert.netanel.walletmonitor.database;

import java.io.Serializable;

/**
 * Created by nati on 4/9/18.
 * represent one raw at categories table
 */

public class CategoryDao {
    //Integer to check if came from DB
    private Integer id = null;
    private String name;
    private int priority;

    public CategoryDao(int id, String name, int priority){
        this.id = id;
        this.name = name;
        this.priority = priority;

    }

    public CategoryDao(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return this.name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
