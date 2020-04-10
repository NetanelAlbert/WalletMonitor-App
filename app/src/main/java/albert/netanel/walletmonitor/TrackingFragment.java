package albert.netanel.walletmonitor;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import albert.netanel.walletmonitor.database.CategoryDao;
import albert.netanel.walletmonitor.database.ExpenseDao;
import albert.netanel.walletmonitor.database.ExpensesManager;

public class TrackingFragment extends DialogFragment implements AdapterView.OnItemClickListener {
    private Context context;
    private ExpensesManager expensesManager;
    private Calendar monthToShow;
    private CategoryDao categoryDao;
    private ArrayList<ExpenseDao> expenses;
    private DetailsAdapter detailsAdapter;


    public void setFields(Context context, ExpensesManager expensesManager, Calendar monthToShow, CategoryDao categoryDao){
        this.context = context;
        this.expensesManager = expensesManager;
        this.monthToShow = monthToShow;
        this.categoryDao =categoryDao;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_tracing, container);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView fragmentTracingTV = view.findViewById(R.id.fragment_tracing__textView);
        fragmentTracingTV.setText(categoryDao.getName() + " - "
                + ((TrackingActivity)context).getMonthName() + " " +monthToShow.get(Calendar.YEAR));
        ListView listView = view.findViewById(R.id.fragment_tracing_lisView);
        expenses = expensesManager.getAllExpenses(categoryDao.getId(), monthToShow);
        detailsAdapter = new DetailsAdapter(context, R.layout.details_list_view, expenses, expensesManager);
        listView.setAdapter(detailsAdapter);

        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DetailsFragment fragment = new DetailsFragment();
        fragment.setExpenseDao(expenses.get(position));
        fragment.setExpensesManager(expensesManager);
        fragment.setContext(context);
        fragment.show(((TrackingActivity)context).getSupportFragmentManager(), "DitailsInsideTrackingFragment");
    }
}
