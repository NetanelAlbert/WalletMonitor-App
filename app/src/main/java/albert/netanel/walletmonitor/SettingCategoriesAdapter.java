package albert.netanel.walletmonitor;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import albert.netanel.walletmonitor.database.CategoriesManager;
import albert.netanel.walletmonitor.database.CategoryDao;

/**A custom adapter for the categories list on the setting activity
 * @see SettingActivity*/
public class SettingCategoriesAdapter extends ArrayAdapter<CategoryDao> {

    private final List<CategoryDao> objectList;
    private final CategoriesManager categoriesManager;
    private final Context context;
    private final SettingActivity settingActivity;

    public SettingCategoriesAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<CategoryDao> objects, CategoriesManager categoriesManager) {
        super(context, resource, textViewResourceId, objects);
        this.objectList = objects;
        this.categoriesManager = categoriesManager;
        this.context = context;
        this.settingActivity = (SettingActivity) context;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        CategoryDao categoryDao = (CategoryDao)getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.currency_setting_list_view, parent, false);
        }
        // Lookup view for data population
        TextView categoriesLvTv = convertView.findViewById(R.id.categoriesLvTv);
        categoriesLvTv.setText(categoryDao.getName());
        categoriesLvTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle("אזהרה");
                builder.setMessage("האם ברצונך למחוק סעיף הוצאה זה?");

                builder.setPositiveButton("כן", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        if(categoriesManager.removeCategory(objectList.get(position))){
                            objectList.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(context, "סעיף הוצאה נמחק", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "לא ניתן למחוק סעיף הוצאה שנמצא בשימוש", Toast.LENGTH_LONG).show();
                        }
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
                return false;
            }
        });
        Button upButton =  convertView.findViewById(R.id.categoryUpButton);
        if(position == 0){
          upButton.setVisibility(View.INVISIBLE);
        } else {
           upButton.setVisibility(View.VISIBLE);
           upButton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Collections.swap(objectList, position, position - 1);
                   notifyDataSetChanged();
                   settingActivity.setInCategoriesEditOn();
               }
           });
        }
        int lastIndex = objectList.size() - 1;
        Button downButton = convertView.findViewById(R.id.categoryDownButton);
        if(position == lastIndex){
           convertView.findViewById(R.id.categoryDownButton).setVisibility(View.INVISIBLE);
        } else {
            downButton.setVisibility(View.VISIBLE);
            downButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Collections.swap(objectList, position , position + 1);
                    notifyDataSetChanged();
                    settingActivity.setInCategoriesEditOn();
                }
            });
        }

        return convertView;
    }
}
