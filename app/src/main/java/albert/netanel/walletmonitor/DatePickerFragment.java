package albert.netanel.walletmonitor;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

import albert.netanel.walletmonitor.MainActivity;

/**show to the user a calendar and return to the main activity the date he chose*/
public class DatePickerFragment extends android.support.v4.app.DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private DateListener dateListener;

    public void setDateListener(DateListener dateListener){
        this.dateListener = dateListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    /**calling by the dialog and call the func setDateButton on mainActivity*/
    public void onDateSet(DatePicker view, int year, int month, int day) {
        dateListener.setDateButton(year, month, day);
    }
}
