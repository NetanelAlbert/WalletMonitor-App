package albert.netanel.walletmonitor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import albert.netanel.walletmonitor.database.CategoryDao;
import albert.netanel.walletmonitor.database.ExpenseDao;
import albert.netanel.walletmonitor.database.ExpensesManager;

/**
 * organise and handle the custom listView
 */
public class DetailsAdapter extends ArrayAdapter<ExpenseDao> {
    private final ExpensesManager expensesManager;
    private final Context context;

    public DetailsAdapter(@NonNull Context context, int resource, @NonNull List objects, ExpensesManager expensesManager) {
        super(context, resource, objects);
        this.expensesManager = expensesManager;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ExpenseDao expenseDao = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.details_list_view, parent, false);
        }
        // Lookup view for data population
        TextView dateTextView = convertView.findViewById(R.id.dateTextView);
        TextView descriptionTextView = convertView.findViewById(R.id.descriptionTextView);
        TextView sumTextView = convertView.findViewById(R.id.sumTextView);
        //change the format of the date from sql format to more familiar one
        String date;
        try {
            date = expensesManager.formatDateFromDatabase(expenseDao.getDate());
        } catch (ParseException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("שגיאה");
            builder.setMessage("שגיאה בשליפת תאריך מבסיס הנתונים");
            builder.create().show();
            date = "שגיאת המרה";
        }
        //delete the .00 if it is a round number
        double sum = expenseDao.getAmount();
        String sumString = String.valueOf(sum);
        if (sum % 1 == 0) {
            sumString = String.valueOf((int) sum);
        }
        // Populate the data into the template view using the data object
        dateTextView.setText(date);
        descriptionTextView.setText(expenseDao.getDescription());
        sumTextView.setText(sumString);
        // Return the completed view to render on screen
        return convertView;
    }
}

