package albert.netanel.walletmonitor.fixerServer;

/**
 * Created by nati on 4/6/18.
 */

public interface FixerListener {

    void handleFixerResult(String fixerResult);
}
