package albert.netanel.walletmonitor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import albert.netanel.walletmonitor.database.CategoryDao;
import albert.netanel.walletmonitor.database.ExpensesManager;

/**organise and handle the custom listView*/
public class TrackingAdapter extends ArrayAdapter<CategoryDao> {
    final ExpensesManager expensesManager ;
    private Calendar monthToShow;
    /**constructor that keeping the expense manager
     * @param expensesManager using when we want to sand commend to database
     * */
    public TrackingAdapter(@NonNull Context context, int resource, @NonNull List objects, ExpensesManager expensesManager, Calendar calendar) {
        super(context, resource, objects);
        this.expensesManager = expensesManager;
        this.monthToShow = calendar;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        CategoryDao categoryDao = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.tracking_list_view, parent, false);
        }
        // Lookup view for data population
        TextView sumTextView = convertView.findViewById(R.id.sumTextView);
        TextView categoryTextView = convertView.findViewById(R.id.categoryTextView);
        // Populate the data into the template view using the data object
        sumTextView.setText(String.valueOf(expensesManager.getSumForMonth(categoryDao.getId(), monthToShow)));
        categoryTextView.setText(categoryDao.getName());
        // Return the completed view to render on screen
        return convertView;
    }}
