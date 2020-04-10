package albert.netanel.walletmonitor;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioGroup;

import java.util.ArrayList;

import albert.netanel.walletmonitor.database.DatabaseHelper;
import albert.netanel.walletmonitor.database.ExpenseDao;
import albert.netanel.walletmonitor.database.ExpensesManager;

/**show a list of the user expense order by the date an time*/
public class DetailsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, RadioGroup.OnCheckedChangeListener {
    private ArrayList<ExpenseDao> expenses;
    private ExpensesManager expensesManager;
    private SQLiteDatabase sqLiteDatabase;
    private DetailsAdapter detailsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        setTitle("פירוט הוצאות");

        sqLiteDatabase = new DatabaseHelper(this).getReadableDatabase();
        expensesManager = new ExpensesManager(sqLiteDatabase);

        RadioGroup orderRadioGroup = findViewById(R.id.order_radioGroup);
        orderRadioGroup.setOnCheckedChangeListener(this);
        RadioGroup orderbyRadioGroup = findViewById(R.id.order_by_radioGroup);
        orderbyRadioGroup.setOnCheckedChangeListener(this);

    }

    @Override
    protected void onResume() {
        ListView listView = findViewById(R.id.detailsList);
        expenses = expensesManager.getAllExpenses();
        detailsAdapter = new DetailsAdapter(this, R.layout.details_list_view, expenses, expensesManager);
        listView.setAdapter(detailsAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DetailsFragment fragment = new DetailsFragment();
        fragment.setExpenseDao(expenses.get(position));
        fragment.setExpensesManager(expensesManager);
        fragment.setContext(this);
        fragment.show(getSupportFragmentManager(), "fragment");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()){
            case R.id.item_details_tracking:
                intent = new Intent(this, TrackingActivity.class);
                startActivity(intent);
                break;
            case R.id.item_details_addExpense :
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.item_details_setting :
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

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("אזהרה");
        builder.setMessage("האם ברצונך למחוק הוצאה זאת?" + "\n פעולה זאת לא ניתנת לביטול");

        builder.setPositiveButton("כן", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                int id = expenses.get(position).getId();
                expensesManager.removeExpense(id);
                expenses.remove(position);
                detailsAdapter.notifyDataSetChanged();

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
        return true;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        RadioGroup orderRadioGroup = findViewById(R.id.order_radioGroup);
        RadioGroup orderByRadioGroup = findViewById(R.id.order_by_radioGroup);
        int orderRadioButtonId = orderRadioGroup.getCheckedRadioButtonId();
        int orderByRadioButtonId = orderByRadioGroup.getCheckedRadioButtonId();
        String order = null;
        switch (orderRadioButtonId){
            case R.id.down_order_radioButton:
                order = "desc";
                break;
            case R.id.up_order_radioButton:
                order = "asc";
        }

        String orderBy = null;
        switch (orderByRadioButtonId){
            case R.id.date_radioButton:
                orderBy = ExpensesManager.DATE_COLUMN;
                break;
            case R.id.sum_radioButton:
                orderBy = ExpensesManager.AMOUNT_COLUMN;
        }
        if(order == null || orderBy == null)
            return;

        expenses = expensesManager.getAllExpenses(orderBy, order);
        detailsAdapter = new DetailsAdapter(this, R.layout.details_list_view, expenses, expensesManager);
        ListView listView = findViewById(R.id.detailsList);
        listView.setAdapter(detailsAdapter);

    }
}
