package albert.netanel.walletmonitor;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import albert.netanel.walletmonitor.database.CategoriesManager;
import albert.netanel.walletmonitor.database.CategoryDao;
import albert.netanel.walletmonitor.database.CurrenciesManager;
import albert.netanel.walletmonitor.database.CurrencyDao;
import albert.netanel.walletmonitor.database.DBListener;
import albert.netanel.walletmonitor.database.DatabaseHelper;
import albert.netanel.walletmonitor.database.ExpenseDao;
import albert.netanel.walletmonitor.database.ExpensesManager;
import albert.netanel.walletmonitor.fixerServer.FixerGetLatest;

public class MainActivity extends AppCompatActivity implements TimeListener, DateListener, View.OnTouchListener, DBListener {

    private SQLiteDatabase sqLiteDatabase;
    private Button timeButton;
    private Button dateButton;
    private Button addButton;
    private Spinner currenciesSpinner;
    private ArrayList<CurrencyDao> currencies;
    private CurrencyDao iLSCurrency;
    private ArrayAdapter<CurrencyDao> currencyAdapter;
    private boolean isDateButtonFocusOnThisDay = true;
    private SharedPreferences preferences;
    private SharedPreferences.Editor prefEditor;
    private ExpensesManager expensesManager;
    private CurrenciesManager currenciesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("הוצאה חדשה");

        preferences = getSharedPreferences(getString(R.string.shared_preferences_key), MODE_PRIVATE);
        prefEditor = preferences.edit();

        if(preferences.getBoolean("isFirstTime", true)){
            if(isNetworkAvailable()){
                prefEditor.putBoolean("isFirstTime" , false);
                prefEditor.apply();
            } else {
                final Context context = this;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("שגיאה!");
                builder.setMessage("המכשיר צריך להיות מחובר לאינטרנט בהפעלה ראשונה");
                builder.setNeutralButton("יציאה" , new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((MainActivity)context).finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return;
            }
        }

        DatabaseHelper databaseHelper = new DatabaseHelper(this, this);
        sqLiteDatabase = databaseHelper.getWritableDatabase();

        currenciesSpinner = findViewById(R.id.currenciesSpinner);
        currenciesManager = new CurrenciesManager(sqLiteDatabase);
        currencies = currenciesManager.getAllCurrencies();
        boolean found = false;
        for(int i = 0; i < currencies.size() && !found; i++){
            if(currencies.get(i).getName().equals("ILS")){
                iLSCurrency = currencies.get(i);
                currencies.remove(i);
                found = true;
            }
        }
        currencyAdapter = new ArrayAdapter<>(this, R.layout.spinner_layout, R.id.spinnertextview, currencies);
        currenciesSpinner.setAdapter(currencyAdapter);
        int lastCurrency = findLastCurrency(currencies);
        currenciesSpinner.setSelection(lastCurrency);


        timeButton = findViewById(R.id.timebutton);
        dateButton = findViewById(R.id.datebutton);

        Calendar calendar = Calendar.getInstance();
        int minutes = calendar.get(Calendar.MINUTE);
        String minutesString = String.valueOf(minutes);
        if(minutes < 10){
            minutesString = "0" + minutesString;
        }
        timeButton.setText(calendar.get(Calendar.HOUR_OF_DAY ) + ":" + minutesString);
        dateButton.setText(calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR));
        addButton = findViewById(R.id.putExpense_button);

        expensesManager = new ExpensesManager(sqLiteDatabase);

        if(iLSCurrency != null) // todo check if steal relevant after fix the Fixer problem
            if(preferences.getInt(getString(R.string.preference_last_currency), iLSCurrency.getId()) != iLSCurrency.getId())
                findViewById(R.id.foreignCarrencySwitch).performClick();

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onResume() {
        EditText amountEditText = findViewById(R.id.amount_editText);
        EditText descriptionEditText = findViewById(R.id.description_editText);
        EditText noticeEditText = findViewById(R.id.notice_editText);
        amountEditText.setText(null);
        descriptionEditText.setText(null);
        noticeEditText.setText(null);

        (findViewById(R.id.amount_editText)).requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        Spinner categoriesSpinner = findViewById(R.id.categoriesSpinner);
        ArrayList<CategoryDao> categories = new CategoriesManager(sqLiteDatabase).getAllCategories();
        ArrayAdapter<CategoryDao> categoriesAdapter = new ArrayAdapter<>(this, R.layout.spinner_layout, R.id.spinnertextview, categories);
        categoriesSpinner.setAdapter(categoriesAdapter);

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()){
            case R.id.item_main_tracking :
                intent = new Intent(this, TrackingActivity.class);
                break;
            case R.id.item_main_details :
                intent = new Intent(this, DetailsActivity.class);
                break;
            case R.id.item_main_setting :
                intent = new Intent(this, SettingActivity.class);
                break;

        }
        startActivity(intent);
        return true;
    }

    /**show {@link TimePickerFragment} to the user*/
    public void onTimeButtonClick(View view){

        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setTimeListener(this);
        timePickerFragment.show(getSupportFragmentManager(),"time");

    }

    /**show {@link DatePickerFragment} to the user*/
    public void onDateButtonClick(View view){
        DatePickerFragment datePicker = new DatePickerFragment();
        datePicker.setDateListener(this);
        datePicker.show(getSupportFragmentManager(), "date");
    }

    @Override
    public void setTimeButton(int hour, int minutes) {

        String minutesString = String.valueOf(minutes);
        if(minutes < 10){
            minutesString = "0" + minutesString;
        }
        Calendar calendar = Calendar.getInstance();
        if(hour <= calendar.get(Calendar.HOUR_OF_DAY) || !isDateButtonFocusOnThisDay) {
            Button timeButton = findViewById(R.id.timebutton);
            timeButton.setText(hour + ":" + minutesString);
        } else {
            Toast.makeText(this, "לא ניתן להכניס שעה עתידית", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void setDateButton(final int year, final int month, final int day){
        Calendar userDate = Calendar.getInstance();
        userDate.set(year, (month), day);
        Calendar currentDate = Calendar.getInstance();
        int currentYear = currentDate.get(Calendar.YEAR);
        int currentMonth = currentDate.get(Calendar.MONTH);
        int currentDay = currentDate.get(Calendar.DAY_OF_MONTH);

        final Button dateButton = findViewById(R.id.datebutton);
        if(currentYear == year && currentMonth == month && currentDay == day) {

            dateButton.setText(day + "/" + (month + 1) + "/" + year);
            isDateButtonFocusOnThisDay = true;

        } else if(userDate.getTimeInMillis() > currentDate.getTimeInMillis()){

            Toast.makeText(this, "לא ניתן להכניס תאריך עתידי", Toast.LENGTH_LONG).show();
        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("אזהרה");
            builder.setMessage("תאריך זה עבר. האם אתה בטוח?");

            builder.setPositiveButton("כן", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dateButton.setText(day + "/" + (month + 1) + "/" + year);
                    isDateButtonFocusOnThisDay = false;
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton("לא", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing but close the dialog
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();




        }
    }

    /**call by the add expense button.
     * collect all the information from the activity fields and insert to database
     * @param view get the view that call her*/
    public void addExpense(View view) {
        EditText amountEditText = findViewById(R.id.amount_editText);
        EditText descriptionEditText = findViewById(R.id.description_editText);
        EditText noticeEditText = findViewById(R.id.notice_editText);
        noticeEditText.setOnTouchListener(this);
        amountEditText.setOnTouchListener(this);
        String amountString = amountEditText.getText().toString();
        double amount;
        if(amountString.length() > 0) {
            amount = Double.parseDouble(amountString);
        } else {
            amount = 0;
        }
        Switch foriegnCurrencySwitch = findViewById(R.id.foreignCarrencySwitch);
        CurrencyDao selctedCurrency;
        if(foriegnCurrencySwitch.isChecked())
            selctedCurrency = (CurrencyDao)currenciesSpinner.getSelectedItem();
        else
            selctedCurrency = iLSCurrency;
        int currency = selctedCurrency.getId();
        Spinner categoriesSpinner = findViewById(R.id.categoriesSpinner);
        int category = ((CategoryDao)categoriesSpinner.getSelectedItem()).getId();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat fromFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat toFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date tempDate;
        try {
            tempDate = fromFormat.parse(dateButton.getText().toString());
        } catch (ParseException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("שגיאה");
            builder.setMessage("לא הוזן תאריך בפורמט מתאים");
            builder.create().show();
            return;        }
        String date = toFormat.format(tempDate);
        String time = timeButton.getText().toString();
        String description = descriptionEditText.getText().toString();
        String notice = noticeEditText.getText().toString();
        double originalAmount = amount;
        //convert if not primary currency
        if(currency != preferences.getInt(getString(R.string.preference_primary_currency), 0)){
            if(!isNetworkAvailable()){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("שגיאה!");
                builder.setMessage("המכשיר צריך להיות מחובר לאינטרנט כדי להמיר מטבע");
                builder.setNeutralButton("אישור" , new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return;
            }
            CurrencyDao currencyDao = (CurrencyDao)currenciesSpinner.getSelectedItem();
            String chosenSymbol = currencyDao.getName();
            String primaryCurrencySymbol = expensesManager.getCurrencyName(preferences.getInt(getString(R.string.preference_primary_currency), 0));
            FixerGetLatest fixerGetLatest = new FixerGetLatest(null);
            try {
                String result = fixerGetLatest.execute().get(2, TimeUnit.SECONDS);
                amount = expensesManager.getConvertAmount(chosenSymbol,primaryCurrencySymbol ,amount, result);

            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("בדוק את החיבור לאינטרנט");
                builder.setMessage("לא ניתן להמיר ממטבע חוץ ללא גישה לאינטרנט");
                builder.create().show();
                return;

            }

        }
        if(amount > 10000){
            Toast.makeText(this, "הסכום המקסימלי להוצאה הוא 10,000", Toast.LENGTH_SHORT).show();
            return;
        }

        if(amount > 0 && description.length() > 0) {
            try {
                expensesManager.putExpense(new ExpenseDao(
                        0, amount, currency, category, date, time, description, notice,originalAmount));
                Intent intent = new Intent(this, TrackingActivity.class);
                prefEditor.putInt(getString(R.string.preference_last_currency), currency);
                prefEditor.apply();
                int currencyPriority = ((CurrencyDao)currenciesSpinner.getSelectedItem()).getPriority();
                currenciesManager.moveCurrencyforward(currency, currencyPriority);
                startActivity(intent);
            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("שגיאת תחביר");
                builder.setMessage("אסור להזין תווים מיוחדים");
                builder.create().show();
            }

        } else {
            Animation addingErrorAnimation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
            addingErrorAnimation.setDuration(500); // duration - half a second
            addingErrorAnimation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
            addingErrorAnimation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
            addingErrorAnimation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
            addButton.startAnimation(addingErrorAnimation);

            if(description.length() == 0) {
                String descriptionHint = descriptionEditText.getHint().toString();
                if (!descriptionHint.contains("שדה חובה")) {
                    descriptionEditText.setHint(descriptionHint + " - שדה חובה");
                    descriptionEditText.setHintTextColor(Color.RED);

                }
            }
            if(amount == 0) {
                String amountHint = amountEditText.getHint().toString();
                if (!amountHint.contains("שדה חובה")) {
                    amountEditText.setHint(amountHint + " - שדה חובה");
                    amountEditText.setHintTextColor(Color.RED);

                }
            }

        }
    }

    /**looking for the CurrencyDao that contain the last used currency
     * @return the index of the last used CurrencyDao in the list*/
    private int findLastCurrency(ArrayList<CurrencyDao> currencies) {
        int lastCurrency = preferences.getInt(getString(R.string.preference_last_currency), 0);
        for(int i = 0; i < currencies.size(); i++){
            if(currencies.get(i).getId() == lastCurrency){
                return i;
            }
        }
        return 0;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if(view.getId() == R.id.description_editText || view.getId() == R.id.amount_editText) {
            addButton.clearAnimation();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        if(sqLiteDatabase != null)
            sqLiteDatabase.close();
        super.onDestroy();
    }

    @Override
    public void updateData() {
        finish();
        startActivity(getIntent());
    }

    public void onForiegnCurrencySwitchClick(View view) {

        if(((Switch)view).isChecked())
            currenciesSpinner.setVisibility(View.VISIBLE);
        else
            currenciesSpinner.setVisibility(View.INVISIBLE);

    }

    public void onCategroiesTextViewClick(View view) {
        findViewById(R.id.categoriesSpinner).performClick();
    }

}

