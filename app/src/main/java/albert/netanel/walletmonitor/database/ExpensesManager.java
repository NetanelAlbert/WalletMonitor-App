package albert.netanel.walletmonitor.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by nati on 4/9/18.
 * organize all the names and the functions that related to the table 'expenses'
 */

public class ExpensesManager {
    public static final String TABLE_NAME = "expenses";
    public static final String ID_COLUMN = "id";
    public static final String AMOUNT_COLUMN = "amount";
    public static final String CURRENCY_COLUMN = "currency";
    public static final String CATEGORY_COLUMN = "category";
    public static final String DATE_COLUMN = "date";
    public static final String TIME_COLUMN = "time";
    public static final String DESCRIPTION_COLUMN = "description";
    public static final String NOTICE_COLUMN = "notice";
    public static final String ORIGINAL_AMOUNT_COLUMN = "originalAmount";

    private final SQLiteDatabase sqLiteDatabase;

    public ExpensesManager(SQLiteDatabase sqLiteDatabase){
        this.sqLiteDatabase = sqLiteDatabase;
    }

    /**insert a new expense into the database
     * @param expenseDao contain all data about the new expense*/
    public void putExpense(ExpenseDao expenseDao) throws Exception {
        String sql = "insert into " + ExpensesManager.TABLE_NAME + " values(null, " +
                expenseDao.getAmount() + ", " +
                expenseDao.getCurrency() + ", " +
                expenseDao.getCategory() + ", '" +
                expenseDao.getDate() + "', '" +
                expenseDao.getTime() + "', '" +
                expenseDao.getDescription() + "', '" +
                expenseDao.getNotice() + "'," +
                expenseDao.getOriginalAmount() + ")";
        sqLiteDatabase.execSQL(sql);
    }

    /**get the name of currency according to given id
     * @param id the id of the currency
     * @return the currency name*/
    public String getCurrencyName(int id){
        String sql = "select " + CurrenciesManager.NAME_COLUMN +
                " from " + CurrenciesManager.TABLE_NAME +
                " where " + CurrenciesManager.ID_COLUMN + " = " + id;
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        cursor.moveToFirst();
        return cursor.getString(0);
    }

    /**get the name of category according to given id
     * @param id the id of the category
     * @return the category name*/
    public String getCategoryName(int id){
        String sql = "select " + CategoriesManager.NAME_COLUMN +
                " from " + CategoriesManager.TABLE_NAME +
                " where " + CategoriesManager.ID_COLUMN + " = " + id;
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        cursor.moveToFirst();
        return cursor.getString(0);
    }

    /**find the sum of all expenses in this month
     * @return sum for this month*/
    public float getSumForMonth(Calendar monthToShow){
        int month = monthToShow.get(Calendar.MONTH) + 1;
        String monthString = String.valueOf(month);
        if(month < 10){
            monthString = "0" + monthString;
        }
        String sql = "select sum(" + ExpensesManager.AMOUNT_COLUMN + ") from " + ExpensesManager.TABLE_NAME + " " +
                "where strftime('%m', " + ExpensesManager.DATE_COLUMN + ") like '" + monthString + "' " +
                "and strftime('%Y', " + ExpensesManager.DATE_COLUMN + ") like '" + monthToShow.get(Calendar.YEAR) + "'";
        Cursor cursor = sqLiteDatabase.rawQuery(sql , null);
        cursor.moveToFirst();
        float sum = cursor.getFloat(0);
        if(sum > 0){
            return sum;
        } else {
            return 0;
        }
    }

    /**find the sum of all expenses in this month by category
     * @return sum for this month by category*/
    public float getSumForMonth(int category, Calendar monthToShow){
        int month = monthToShow.get(Calendar.MONTH) + 1;
        String monthString = String.valueOf(month);
        if(month < 10){
            monthString = "0" + monthString;
        }
        String sql = "select sum(" + ExpensesManager.AMOUNT_COLUMN + ") from " + ExpensesManager.TABLE_NAME + " " +
                "where strftime('%m', " + ExpensesManager.DATE_COLUMN + ") like '" + monthString + "' " +
                "and strftime('%Y', " + ExpensesManager.DATE_COLUMN + ") like '" + monthToShow.get(Calendar.YEAR) + "' " +
                "and " + ExpensesManager.CATEGORY_COLUMN + " = " + category;
        Cursor cursor = sqLiteDatabase.rawQuery(sql , null);
        cursor.moveToFirst();
        float sum = cursor.getFloat(0);
        if(sum > 0){
            return sum;
        } else {
            return 0;
        }
    }

    /**convert date string from 1970-12-30 format to 30/12/1970
     * @param databaseDate the string of date format
     * @return string of converted date*/
    public String formatDateFromDatabase(String databaseDate) throws ParseException {
        SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat toFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date tempDate = fromFormat.parse(databaseDate);
        String date = toFormat.format(tempDate);
        return date;
    }

    public ArrayList<ExpenseDao> getAllExpenses() {

        Cursor cursor = sqLiteDatabase.rawQuery("select * from " + ExpensesManager.TABLE_NAME +
                " order by " + ExpensesManager.DATE_COLUMN + " desc, " + ExpensesManager.TIME_COLUMN + " desc", null);
        ArrayList<ExpenseDao> expenseDaos = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                expenseDaos.add(new ExpenseDao(
                        cursor.getInt(0),
                        cursor.getDouble(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getDouble(8)
                ));
            } while (cursor.moveToNext());

        }
        return expenseDaos;
    }

    /**convert an amount according to given json
     * @param chosenCurrency the currency to convert from
     * @param primaryCurrency the currency to convert to
     * @param originalAmount the amount to convert
     * @param json the json string from http://data.fixer.io/api/...
     * @return the converted amount*/
    public double getConvertAmount(String chosenCurrency, String primaryCurrency, double originalAmount, String json) throws JSONException {
        JSONObject body = new JSONObject(json);
        JSONObject rates = body.getJSONObject("rates");
        double chosenRate = rates.getDouble(chosenCurrency);
        double primaryRate = rates.getDouble(primaryCurrency);
        double numberToConvert = originalAmount * primaryRate / chosenRate;
        DecimalFormat decimalFormat = new DecimalFormat(".##");
        double convertAmount = Double.parseDouble(decimalFormat.format(numberToConvert));

        return convertAmount;
    }

    /**change the field of the amount convert to the primary currency
     * @param id the id of the raw that need to update
     * @param amount the amount to insert*/
    public void changePrimaryCurrencyAmount(int id, double amount){
        sqLiteDatabase.execSQL("update " + ExpensesManager.TABLE_NAME +
                " set " + ExpensesManager.AMOUNT_COLUMN + " = " + amount +
                    " where " + ExpensesManager.ID_COLUMN + " = " + id);
    }

    /**remove an expense raw from database
     * @param id represent the id of the expense that need to be deleted*/
    public void removeExpense(int id){
        sqLiteDatabase.execSQL("delete from " + ExpensesManager.TABLE_NAME + " where " + ExpensesManager.ID_COLUMN + " = " + id);
    }

    public ArrayList<ExpenseDao> getAllExpenses(Integer category, Calendar monthToShow) {
        int month = monthToShow.get(Calendar.MONTH) + 1;
        String monthString = String.valueOf(month);
        if(month < 10){
            monthString = "0" + monthString;
        }
        Cursor cursor = sqLiteDatabase.rawQuery("select * from " + ExpensesManager.TABLE_NAME + " " +
                        "where strftime('%m', " + ExpensesManager.DATE_COLUMN + ") like '"
                        + monthString + "' " +
                        "and strftime('%Y', " + ExpensesManager.DATE_COLUMN + ") like '"
                        + monthToShow.get(Calendar.YEAR) + "' " +
                        "and " + ExpensesManager.CATEGORY_COLUMN + " = " + category +
                " order by " + ExpensesManager.DATE_COLUMN + " desc, " + ExpensesManager.TIME_COLUMN + " desc", null);
        ArrayList<ExpenseDao> expenseDaos = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                expenseDaos.add(new ExpenseDao(
                        cursor.getInt(0),
                        cursor.getDouble(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getDouble(8)
                ));
            } while (cursor.moveToNext());

        }
        return expenseDaos;
    }

    public ArrayList<ExpenseDao> getAllExpenses(String orderBy, String order) {

        Cursor cursor = sqLiteDatabase.rawQuery("select * from " + ExpensesManager.TABLE_NAME +
                " order by " + orderBy + " " + order + ", " + ExpensesManager.TIME_COLUMN + " desc", null);
        ArrayList<ExpenseDao> expenseDaos = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                expenseDaos.add(new ExpenseDao(
                        cursor.getInt(0),
                        cursor.getDouble(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getDouble(8)
                ));
            } while (cursor.moveToNext());

        }
        return expenseDaos;
    }
}
