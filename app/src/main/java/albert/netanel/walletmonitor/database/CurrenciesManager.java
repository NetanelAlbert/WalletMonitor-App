package albert.netanel.walletmonitor.database;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import albert.netanel.walletmonitor.MainActivity;
import albert.netanel.walletmonitor.R;
import albert.netanel.walletmonitor.fixerServer.*;


/**
 * Created by nati on 4/9/18.
 * organize all the names and the functions that related to the table 'currencies'
 */

public class CurrenciesManager {

    public static final String TABLE_NAME = "currency";
    public static final String ID_COLUMN = "id";
    public static final String NAME_COLUMN = "name";
    public static final String PRIORITY_COLUMN = "priority";

    private final SQLiteDatabase sqLiteDatabase;

    public CurrenciesManager(SQLiteDatabase sqLiteDatabase){
        this.sqLiteDatabase = sqLiteDatabase;
    }

    public void moveCurrencyforward(int id, int priority){
        if(priority > 995){
            priority = 995;
        }
        if(priority == 0){
            priority = 1;
        }

        sqLiteDatabase.execSQL("update " + CurrenciesManager.TABLE_NAME + " set " + CurrenciesManager.PRIORITY_COLUMN +
                " = " + (priority - 1) + " where " + CurrenciesManager.ID_COLUMN + " = " + id);
    }

    /**get all the currencies as list ordered by user priority
     * @return list of all the currencies as {@link CurrencyDao}*/
    public ArrayList<CurrencyDao> getAllCurrencies(){
        String query = "select * from " +
                CurrenciesManager.TABLE_NAME + " order by " + CurrenciesManager.PRIORITY_COLUMN;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        ArrayList<CurrencyDao> currencies = new ArrayList<>();
        if(cursor.moveToFirst()) {
            do {
                currencies.add(new CurrencyDao(cursor.getInt(0), cursor.getString(1),
                        cursor.getInt(2)));
            } while (cursor.moveToNext());
        }
        return currencies;
    }

}
