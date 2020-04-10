package albert.netanel.walletmonitor;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import albert.netanel.walletmonitor.database.CurrencyDao;
import albert.netanel.walletmonitor.database.ExpenseDao;
import albert.netanel.walletmonitor.database.ExpensesManager;

/**change the convert amount according to the new primary currency*/
public class ChangeCurrencyThread extends AsyncTask<CurrencyDao, Integer, Void> {

    private final SQLiteDatabase sqLiteDatabase;
    private final ChangeCurrencyListener listener;

    public ChangeCurrencyThread(SQLiteDatabase sqLiteDatabase, ChangeCurrencyListener listener) {
        this.sqLiteDatabase = sqLiteDatabase;
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(CurrencyDao... currencyDaos) {
        ExpensesManager expensesManager = new ExpensesManager(sqLiteDatabase);

        CurrencyDao currencyDao = currencyDaos[0];

        ArrayList<ExpenseDao> expenses = expensesManager.getAllExpenses();
        int newPrimaryCurrency = currencyDao.getId();
        String newPrimaryCurrencySymbol = expensesManager.getCurrencyName(newPrimaryCurrency);
        sqLiteDatabase.beginTransaction();
        for (ExpenseDao dao : expenses){
            if(dao.getCurrency() == newPrimaryCurrency){
                expensesManager.changePrimaryCurrencyAmount(dao.getId(), dao.getOriginalAmount());
            } else {
                String daoCurrencySymbol = expensesManager.getCurrencyName(dao.getCurrency());
                try {
                    URL url = new URL("http://data.fixer.io/api/" + dao.getDate() + "?access_key=e991db36f6c4a38390ae65d48b87d223");
                    URLConnection con = url.openConnection();
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));

                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append(bufferedReader.readLine());

                    while (bufferedReader.ready()) {
                        String inputLine = bufferedReader.readLine();
                        stringBuffer.append(inputLine);

                    }
                    String jsonResult =stringBuffer.toString();
                    bufferedReader.close();

                    double newAmount = expensesManager.getConvertAmount(daoCurrencySymbol, newPrimaryCurrencySymbol, dao.getOriginalAmount(), jsonResult);
                    expensesManager.changePrimaryCurrencyAmount(dao.getId(), newAmount);

                } catch (Exception e) {
                    sqLiteDatabase.endTransaction();
                    listener.changeCurrencyFailed(e);
                    return null;
                }

            }

        }
        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();
        listener.changeCurrencyFinished();
        return null;
    }

}
