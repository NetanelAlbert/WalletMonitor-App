package albert.netanel.walletmonitor.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by nati on 4/9/18.
 * organize all the names and the functions that related to the table 'categories'
 */

public class CategoriesManager {
    public static final String TABLE_NAME = "categories";
    public static final String ID_COLUMN = "id";
    public static final String NAME_COLUMN = "name";
    public static final String PRIORITY_COLUMN = "priority";

    private final SQLiteDatabase sqLiteDatabase;

    public CategoriesManager(SQLiteDatabase sqLiteDatabase){
        this.sqLiteDatabase = sqLiteDatabase;
    }

    /**insert a raw to the table or chang the priority column if already exist
     * @param categoryDaos list of all the categories */
    public void updateCategories(ArrayList<CategoryDao> categoryDaos){
        sqLiteDatabase.beginTransaction();
        for(int i = 0; i < categoryDaos.size(); i++){
            CategoryDao categoryDao = categoryDaos.get(i);
            if(categoryDao.getId() == null) {
                sqLiteDatabase.execSQL("insert into " + CategoriesManager.TABLE_NAME + " values(null, '" + categoryDao.getName() +
                        "', " + i + ")");
            } else {
                sqLiteDatabase.execSQL("update " + CategoriesManager.TABLE_NAME +
                        " set " + CategoriesManager.PRIORITY_COLUMN + " = " + i +
                " where " + CategoriesManager.ID_COLUMN + " = " + categoryDao.getId());
            }
        }
        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();
    }

    /**get all the categories as list ordered by user priority
     * @return list of all the categories as {@link CategoryDao}*/
    public ArrayList<CategoryDao> getAllCategories(){
        String getCategoriesSql = "select * from " + CategoriesManager.TABLE_NAME +
                " order by " + CategoriesManager.PRIORITY_COLUMN;
        Cursor cursor = sqLiteDatabase.rawQuery(getCategoriesSql, null);
        ArrayList<CategoryDao> categoriesList= new ArrayList<>();
        if(cursor.moveToFirst()){
            do{
                categoriesList.add(new CategoryDao(cursor.getInt(0), cursor.getString(1), cursor.getInt(2)));
            } while (cursor.moveToNext());
        }
        return categoriesList;
    }

    /**remove category from table *if* it isn't in use at the expenses table
     * @param categoryDao the categoryDao that need to be remove
     * @return true if removed, false if shouldn't*/
    public boolean removeCategory(CategoryDao categoryDao){
        Integer id = categoryDao.getId();
        //check if coming from DB or it is a new one
        if(id == null){
            return true;
        } else {
            //check if this category used in the expenses table
            Cursor cursor = sqLiteDatabase.rawQuery("select * from " + ExpensesManager.TABLE_NAME +
                    " where " + ExpensesManager.ID_COLUMN + " = " + id, null);
            int useAmount = cursor.getCount();
            cursor.close();
            if(useAmount == 0){
                sqLiteDatabase.execSQL("delete from " + CategoriesManager.TABLE_NAME + " where " + CategoriesManager.ID_COLUMN + " = " + id);
                return true;
            } else {
                return false;
            }

        }
    }
}
