package albert.netanel.walletmonitor.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import albert.netanel.walletmonitor.R;
import albert.netanel.walletmonitor.fixerServer.FixerGetLatest;
import albert.netanel.walletmonitor.fixerServer.FixerListener;

/**
 * Created by nati on 4/9/18.
 * create the database on first time
 */
public class DatabaseHelper extends SQLiteOpenHelper implements FixerListener {

    private static final String DATABASE_NAME = "db4";
    private static final int DATABASE_VER = 1;

    private final Context context;
    private DBListener listener;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VER);
        this.context = context;
    }

    public DatabaseHelper(Context context, DBListener listener) {
        super(context, DATABASE_NAME, null, DATABASE_VER);
        this.context = context;
        this.listener = listener;
    }
    private SQLiteDatabase db;

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;

        final String currenciesTableCreate = "create table " + CurrenciesManager.TABLE_NAME + "(" +
                CurrenciesManager.ID_COLUMN + " integer primary key autoincrement not null, " +
                CurrenciesManager.NAME_COLUMN + " text(3) unique not null ," +
                CurrenciesManager.PRIORITY_COLUMN +" integer)";
        db.execSQL(currenciesTableCreate);

        final String categoriesTableCreate = "create table " + CategoriesManager.TABLE_NAME + "(" +
                CategoriesManager.ID_COLUMN + " integer primary key autoincrement not null, " +
                CategoriesManager.NAME_COLUMN +" text(30) not null, " +
                CategoriesManager.PRIORITY_COLUMN +" integer)";
        db.execSQL(categoriesTableCreate);

        final String expensesTableCreate = "create table " + ExpensesManager.TABLE_NAME + "(" +
                ExpensesManager.ID_COLUMN +" integer primary key autoincrement not null, " +
                ExpensesManager.AMOUNT_COLUMN + " real not null , " +
                ExpensesManager.CURRENCY_COLUMN + " integer not null, " +
                ExpensesManager.CATEGORY_COLUMN + " integer not null, " +
                ExpensesManager.DATE_COLUMN + " text(10) not null, " +
                ExpensesManager.TIME_COLUMN + " text(10) not null, " +
                ExpensesManager.DESCRIPTION_COLUMN + " text(20), " +
                ExpensesManager.NOTICE_COLUMN + " text(60) , " +
                ExpensesManager.ORIGINAL_AMOUNT_COLUMN + " real , " +
                "foreign key (" + ExpensesManager.CURRENCY_COLUMN + ") references " +
                CurrenciesManager.TABLE_NAME + "(" + CurrenciesManager.ID_COLUMN + "), " +
                "foreign key (" + ExpensesManager.CATEGORY_COLUMN + ") references " +
                CategoriesManager.TABLE_NAME + "(" + CategoriesManager.ID_COLUMN + "))";
        db.execSQL(expensesTableCreate);
        FixerGetLatest fixerGetLatest = new FixerGetLatest(this);
        fixerGetLatest.execute();

        List<String> firstCategories = Arrays.asList("מזון", "ביגוד", "רכב", "פנאי", "זוגיות");
        for(int i = 0; i < firstCategories.size(); i++) {
            String addCategorySql = "insert into " + CategoriesManager.TABLE_NAME + " values(null, '" + firstCategories.get(i) + "', " + i + ")";
            db.execSQL(addCategorySql);
        }

        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.shared_preferences_key),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(context.getString(R.string.preference_last_forward_currency_priority), 995);
        editor.apply();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void handleFixerResult(String fixerResult) {
        if (fixerResult.contains("true")) {

            Iterator<String> symbols = null;
            try {
                JSONObject body = new JSONObject(fixerResult);
                JSONObject rates = body.getJSONObject("rates");
                symbols = rates.keys();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            while (symbols.hasNext()){
                String symbol = symbols.next();
                int priority = 999;
                switch(symbol){
                    case "ILS" : priority = 996;
                        break;
                    case  "USD" : priority = 997;
                        break;
                    case "EUR" : priority = 998;
                        break;
                }
                String sql = ("insert into " +
                        CurrenciesManager.TABLE_NAME +
                        " values(null, '" + symbol + "', " + priority + ")");
                db.execSQL(sql);
            }

            Cursor cursor = db.rawQuery("select " + CurrenciesManager.ID_COLUMN +
                    " from " + CurrenciesManager.TABLE_NAME + " where " +
                    CurrenciesManager.NAME_COLUMN + " like 'ILS'", null);
            if(cursor.moveToFirst()){
                int idOfILS = cursor.getInt(0);
                SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE).edit();
                editor.putInt(context.getString(R.string.preference_primary_currency), idOfILS);
                editor.putInt(context.getString(R.string.preference_last_currency), idOfILS);
                editor.apply();
            }
            if (listener != null) {
                listener.updateData();
            }

        } else {
            Toast.makeText(context, "שגיאה בהורדת רשימת מטבעות", Toast.LENGTH_LONG).show();
        }

    }
}
