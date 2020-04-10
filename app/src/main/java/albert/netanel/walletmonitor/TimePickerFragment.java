package albert.netanel.walletmonitor;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

/**show to the user a cloak and return to the main activity the time he chose*/
public class TimePickerFragment extends android.support.v4.app.DialogFragment
        implements TimePickerDialog.OnTimeSetListener{

        private TimeListener timeListener;

        public void setTimeListener(TimeListener timeListener){
            this.timeListener = timeListener;
        }

        private int currentHour;
        private int currentMinute;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            currentHour = c.get(Calendar.HOUR_OF_DAY);
            currentMinute = c.get(Calendar.MINUTE);
            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, currentHour, currentMinute,true);
        }

        /**calling by the dialog and call the func setTimeButton on mainActivity*/
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            timeListener.setTimeButton(hourOfDay, minute);
        }

}
