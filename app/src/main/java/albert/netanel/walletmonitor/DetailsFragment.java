package albert.netanel.walletmonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;

import albert.netanel.walletmonitor.database.ExpenseDao;
import albert.netanel.walletmonitor.database.ExpensesManager;
/**show the details of one expense.
 * using at the tracking activity when user tap a raw in the listView*/
public class DetailsFragment extends DialogFragment {

    private ExpenseDao expenseDao;
    private ExpensesManager expensesManager;
    private Context context;


    public DetailsFragment() {
        // Empty constructor is required for DialogFragment
    }

    public void setExpenseDao(ExpenseDao expenseDao){
        this.expenseDao = expenseDao;
    }

    public void setExpensesManager(ExpensesManager expensesManager){
        this.expensesManager = expensesManager;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_details, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        int primaryCurrency = preferences.getInt(getString(R.string.preference_primary_currency), 0);
        if(primaryCurrency != expenseDao.getCurrency()) {
            TextView originalAmountTv = view.findViewById(R.id.original_sum_frag);
            originalAmountTv.setText(expenseDao.getOriginalAmount() + " " + expensesManager.getCurrencyName(expenseDao.getCurrency()));
            view.findViewById(R.id.original_sum_frag).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textView24).setVisibility(View.VISIBLE);
        }

        // Get field from view
        TextView titleTv = view.findViewById(R.id.title_frag);
        TextView sumTv = view.findViewById(R.id.sum_frag);
        TextView dateTv = view.findViewById(R.id.date_frag);
        TextView timeTv = view.findViewById(R.id.time_frag);
        TextView categoryTv = view.findViewById(R.id.category_frag);
        TextView noticeTv = view.findViewById(R.id.notice_frag);

        //delete the .00 if it is a round number
        double sum = expenseDao.getAmount();
        String sumString = String.valueOf(sum);
        if(sum % 1 == 0){
            sumString = String.valueOf((int)sum);
        }

        //Set the fields by the expenseDao data
        titleTv.setText(expenseDao.getDescription());
        sumTv.setText(sumString);
        String date;
        try {
            date = expensesManager.formatDateFromDatabase(expenseDao.getDate());
        } catch (ParseException e) {
            dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("שגיאה");
            builder.setMessage("שגיאה בשליפת תאריך מבסיס הנתונים");
            builder.create().show();
            date = "שגיאת המרה";
        }
        dateTv.setText(date);
        timeTv.setText(expenseDao.getTime());
        categoryTv.setText(expensesManager.getCategoryName(expenseDao.getCategory()));
        noticeTv.setText(expenseDao.getNotice());


    }

    @Override
    public void onResume() {
        super.onResume();
        //Set the fragment layout
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }
}
