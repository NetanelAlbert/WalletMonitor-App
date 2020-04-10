package albert.netanel.walletmonitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import albert.netanel.walletmonitor.database.CategoriesManager;
import albert.netanel.walletmonitor.database.CategoryDao;
import albert.netanel.walletmonitor.database.DatabaseHelper;
import albert.netanel.walletmonitor.database.ExpensesManager;

/**show the general sum of purchases this month and the sum per category*/
public class TrackingActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private SQLiteDatabase sqLiteDatabase;
    private Calendar monthToShow;
    private ArrayList<CategoryDao> categoriesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        setTitle("מעקב חודשי");

        sqLiteDatabase = new DatabaseHelper(this).getReadableDatabase();

        monthToShow = Calendar.getInstance();



    }

    @Override
    protected void onResume() {
        loadData();
        super.onResume();
    }

    private void loadData(){
        TextView title = findViewById(R.id.trackingTitleTV);
        String month = getMonthName();

        title.setText(month +" "+ monthToShow.get(Calendar.YEAR));
        ExpensesManager expensesManager = new ExpensesManager(sqLiteDatabase);
        TextView sumTextView = findViewById(R.id.sumTextView);

        float sum = expensesManager.getSumForMonth(monthToShow);
        String sumString = String.valueOf(sum);
        if(sum % 1 == 0){
            sumString = String.valueOf((int)sum);
        }
        SharedPreferences preferences = getSharedPreferences(getString(R.string.shared_preferences_key), MODE_PRIVATE);
        String primaryCurrency = expensesManager.getCurrencyName(preferences.getInt(getString(R.string.preference_primary_currency), 0));
        if(primaryCurrency.equals("ILS"))
            primaryCurrency = "ש\"ח";
        sumTextView.setText(sumString + " " + primaryCurrency);

        ListView listView = findViewById(R.id.trackingListView);
        categoriesList = new CategoriesManager(sqLiteDatabase).getAllCategories();

        TrackingAdapter trackingAdapter = new TrackingAdapter(this, R.layout.tracking_list_view, categoriesList, new ExpensesManager(sqLiteDatabase), monthToShow);
        listView.setAdapter(trackingAdapter);

        listView.setOnItemClickListener(this);
    }

    public String getMonthName() {
        switch (monthToShow.get(Calendar.MONTH)){
            case 0: return "ינואר";
            case 1: return "פברואר";
            case 2: return "מרץ";
            case 3: return "אפריל";
            case 4: return "מאי";
            case 5: return "יוני";
            case 6: return "יולי";
            case 7: return "אוגוסט";
            case 8: return "ספטמבר";
            case 9: return "אוקטובר";
            case 10: return "נובמבר";
            case 11: return "דצמבר";
            default: return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tracking_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()){
            case R.id.item_tracking_details :
                intent = new Intent(this, DetailsActivity.class);
                startActivity(intent);
                break;
            case R.id.item_tracking_addExpense :
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.item_tracking_setting :
                intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                break;

        }
        return true;
    }

    @Override
    protected void onDestroy() {
        sqLiteDatabase.close();
        super.onDestroy();
    }

    public void previousMonth(View view) {
        monthToShow.add(Calendar.MONTH, -1);
        loadData();
    }

    public void nextMonth(View view) {
        monthToShow.add(Calendar.MONTH, 1);
        loadData();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TrackingFragment fragment = new TrackingFragment();
        fragment.setFields(this, new ExpensesManager(sqLiteDatabase), monthToShow, categoriesList.get(position));

        fragment.show(getFragmentManager(), "TrackingFragment");
    }
}
