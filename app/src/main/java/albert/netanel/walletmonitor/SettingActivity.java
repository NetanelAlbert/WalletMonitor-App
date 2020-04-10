package albert.netanel.walletmonitor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import albert.netanel.walletmonitor.database.CategoriesManager;
import albert.netanel.walletmonitor.database.CategoryDao;
import albert.netanel.walletmonitor.database.CurrenciesManager;
import albert.netanel.walletmonitor.database.CurrencyDao;
import albert.netanel.walletmonitor.database.DatabaseHelper;
import albert.netanel.walletmonitor.database.ExpensesManager;

/**let the user to set some behaviors of the application*/
public class SettingActivity extends AppCompatActivity implements ChangeCurrencyListener{

    private SQLiteDatabase sqLiteDatabase;
    private CategoriesManager categoriesManager;
    private CurrenciesManager currenciesManager;
    private SettingCategoriesAdapter categoryDaoAdapter;
    private ListView listView;
    private ArrayList<CategoryDao> categoryDaosList;
    private LinearLayout categoriesLayout;
    private Button showCategories;
    private Button hideCategories;
    private LinearLayout currenciesLayout;
    private Button showCurrencies;
    private Button hideCurrencies;
    private Spinner currenciesSpinner;
    private ArrayAdapter<CurrencyDao> currencyAdapter;
    private ArrayList<CurrencyDao> currencies;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private boolean shouldAllowBack = true;
    private ProgressBar progressBar;
    private boolean inCategoriesEdit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setTitle("הגדרות");

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        sqLiteDatabase = databaseHelper.getWritableDatabase();

        preferences = getSharedPreferences(getString(R.string.shared_preferences_key), MODE_PRIVATE);
        editor = preferences.edit();

        currenciesManager  = new CurrenciesManager(sqLiteDatabase);
        categoriesManager = new CategoriesManager(sqLiteDatabase);

        listView = findViewById(R.id.settingCategoriesList);
        categoryDaosList = categoriesManager.getAllCategories();
        categoryDaoAdapter = new SettingCategoriesAdapter(this, R.layout.spinner_layout, R.id.categoriesLvTv, categoryDaosList, categoriesManager);
        listView.setAdapter(categoryDaoAdapter);

        categoriesLayout = findViewById(R.id.categories_setting_layout);
        showCategories = findViewById(R.id.show_categories);
        hideCategories = findViewById(R.id.hide_categories);
        currenciesLayout = findViewById(R.id.currencies_setting_layout);
        showCurrencies = findViewById(R.id.show_currencies);
        hideCurrencies = findViewById(R.id.hide_currencies);

        currencies = currenciesManager.getAllCurrencies();
        currencyAdapter = new ArrayAdapter<>(this, R.layout.spinner_layout, R.id.spinnertextview, currencies);
        currenciesSpinner = findViewById(R.id.currencies_setting_spinner);
        currenciesSpinner.setAdapter(currencyAdapter);
        currenciesSpinner.setSelection(findPrimaryCurrency());


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_setting_back:
                onBackPressed();
        }
        return true;
    }

    /**looking for the CurrencyDao that contain the primary app currency
     * @return the index of the primary CurrencyDao in the list*/
    private int findPrimaryCurrency() {
        int primaryCurrency = preferences.getInt(getString(R.string.preference_primary_currency), 0);
        for(int i = 0; i < currencies.size(); i++){
            if(currencies.get(i).getId() == primaryCurrency){
                return i;
            }
        }
        return 0;
    }

    /**add category to the temporary list
     * @param view get the view that call her*/
    public void onAddCategory(View view) {
        EditText newCategoryEditText = findViewById(R.id.addCategoryEditText);
        String content = newCategoryEditText.getText().toString();
        if(content.length() > 1) {
            categoryDaoAdapter.add(new CategoryDao(content));
            newCategoryEditText.setText(null);
            inCategoriesEdit = true;
        } else {
            Toast.makeText(this,"אורך מינימלי להזנה הוא 2 תווים", Toast.LENGTH_SHORT).show();
        }
    }

    /**save the new categories and the new order
     * @param view get the view that call her*/
    public void onSaveCategoriesButton(@Nullable View view) {
        try {
            categoriesManager.updateCategories(categoryDaosList);
            Toast.makeText(this, "סעיפי הוצאה עודכנו בהצלחה", Toast.LENGTH_SHORT).show();
            inCategoriesEdit = false;
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("שגיאת תחביר");
            builder.setMessage("אסור להזין תווים מיוחדים");
            builder.create().show();
        }
    }

    /**hiding the categories setting layout*/
    public void hideCategoriesLayout(View view) {
        hideCategories.setVisibility(View.GONE);
        showCategories.setVisibility(View.VISIBLE);
        categoriesLayout.setVisibility(View.GONE);
    }

    /**showing the categories setting layout*/
    public void showCategoriesLayout(View view) {
        hideCategories.setVisibility(View.VISIBLE);
        showCategories.setVisibility(View.GONE);
        categoriesLayout.setVisibility(View.VISIBLE);
    }

    /**make all the chang that need to be done to change the primary currency
     * @param view get the view that call her*/
    public void onSaveCurrencyButton(View view) {

        CurrencyDao currencyDao = (CurrencyDao)currenciesSpinner.getSelectedItem();
        String symbol = currencyDao.getName();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("אזהרה");
        builder.setMessage("האם ברצונך לשנות את המטבע העיקרי ל " + symbol + "?" + "\nזה עלול לקחת זמן");
        final SettingActivity listener = this;
        builder.setPositiveButton("כן", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                shouldAllowBack = false;
                progressBar = findViewById(R.id.saveCurrencyPB);
                progressBar.setVisibility(View.VISIBLE);

                ChangeCurrencyThread thread = new ChangeCurrencyThread(sqLiteDatabase, listener);
                thread.execute(((CurrencyDao)currenciesSpinner.getSelectedItem()));

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("לא", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing but closing the dialog
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }

    /**showing the currencies setting layout*/
    public void showCurrenciesLayout(View view) {
        hideCurrencies.setVisibility(View.VISIBLE);
        showCurrencies.setVisibility(View.GONE);
        currenciesLayout.setVisibility(View.VISIBLE);
    }

    /**hiding the currencies setting layout*/
    public void hideCurrenciesLayout(View view) {
        hideCurrencies.setVisibility(View.GONE);
        showCurrencies.setVisibility(View.VISIBLE);
        currenciesLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        //checking if data changing finished
        if (shouldAllowBack && !inCategoriesEdit)
            super.onBackPressed();
        else if (!shouldAllowBack)
            Toast.makeText(this, "אנא המתן לסיום שינוי המטבע", Toast.LENGTH_LONG).show();
        else {
            if(inCategoriesEdit){
                final SettingActivity settingActivity = this;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("אזהרה");
                builder.setMessage("לא שמרת את השינויים שביצעת בקטגוריות");
                builder.setPositiveButton("שמור וצא", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onSaveCategoriesButton(null);
                        dialog.dismiss();
                        settingActivity.myOnBackPressed();
                    }
                });

                builder.setNegativeButton("צא מבלי לשמור", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        settingActivity.myOnBackPressed();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    private void myOnBackPressed(){
        super.onBackPressed();
    }

    @Override
    public void changeCurrencyFinished() {
        int newPrimaryCurrency = ((CurrencyDao)currenciesSpinner.getSelectedItem()).getId();

        editor.putInt(getString(R.string.preference_primary_currency), newPrimaryCurrency);
        editor.putInt(getString(R.string.preference_last_currency), newPrimaryCurrency);
        editor.apply();

        final String symbol = new ExpensesManager(sqLiteDatabase).getCurrencyName(newPrimaryCurrency);
        final Context context = this;

        shouldAllowBack = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context, "המטבע הראשי שונה בהצלחה ל " + symbol, Toast.LENGTH_SHORT).show();

            }
        });


    }

    @Override
    public void changeCurrencyFailed(final Exception e) {
        final Context context = this;
        shouldAllowBack = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("שגיאה");
                builder.setMessage(e.getMessage());
                builder.create().show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        sqLiteDatabase.close();
        super.onDestroy();
    }

    public void setInCategoriesEditOn(){
        inCategoriesEdit = true;
    }
}
