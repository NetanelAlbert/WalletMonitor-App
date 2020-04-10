package albert.netanel.walletmonitor;
/**listener for {@link ChangeCurrencyThread}*/
public interface ChangeCurrencyListener {
    /**notify listener object that ChangeCurrencyThread finished*/
    void changeCurrencyFinished();

    void changeCurrencyFailed(final Exception e);
}
