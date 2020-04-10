package albert.netanel.walletmonitor.fixerServer;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by nati on 4/6/18.
 */

public class FixerGetLatest extends AsyncTask<String, Long, String> {

    private FixerListener fixerListener;

    public FixerGetLatest(FixerListener fixerListener){
        this.fixerListener = fixerListener;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            URL url = new URL("http://data.fixer.io/api/latest?access_key=e991db36f6c4a38390ae65d48b87d223");
            URLConnection con = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));

            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(bufferedReader.readLine());

            while (bufferedReader.ready()) {
                String inputLine = bufferedReader.readLine();
                stringBuffer.append(inputLine);

            }

            bufferedReader.close();
            if (fixerListener != null){
                fixerListener.handleFixerResult(stringBuffer.toString());
            }
            return stringBuffer.toString();

        } catch (Exception e) {
            return null;
        }
    }
}
