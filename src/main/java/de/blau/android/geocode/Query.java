package de.blau.android.geocode;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import de.blau.android.App;
import de.blau.android.R;
import de.blau.android.dialogs.Progress;
import de.blau.android.dialogs.ProgressDialog;
import de.blau.android.geocode.Search.SearchResult;
import de.blau.android.osm.ViewBox;
import de.blau.android.util.ExecutorTask;
import de.blau.android.util.Snack;

class Query extends ExecutorTask<String, Void, List<SearchResult>> {
    private static final String DEBUG_TAG = Query.class.getSimpleName();

    protected AlertDialog progress = null;

    protected final ViewBox          bbox;
    protected final String           url;
    protected final FragmentActivity activity;

    /**
     * Query a geocoder
     * 
     * @param activity the calling FragmentActivity, if null no progress spinner will be shown
     * @param url URL for the specific instance of the geocoder
     * @param bbox a ViewBox to restrict the query to, if null the whole world will be considered
     */
    public Query(@Nullable FragmentActivity activity, @NonNull String url, @Nullable ViewBox bbox) {
        super(App.getLogic().getExecutorService(), App.getLogic().getHandler());
        this.url = url;
        this.bbox = bbox;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        if (activity != null) {
            progress = ProgressDialog.get(activity, Progress.PROGRESS_SEARCHING);
            progress.show();
        }
    }

    @Override
    protected List<SearchResult> doInBackground(String query) {
        return new ArrayList<>();
    }

    @Override
    protected void onPostExecute(List<SearchResult> res) {
        try {
            if (progress != null) {
                progress.dismiss();
            }
        } catch (Exception ex) {
            Log.e(DEBUG_TAG, "dismiss dialog failed with " + ex);
        }
    }

    /**
     * Show a toast if connection to server has failed
     * 
     * @param message message to display
     */
    void connectionError(@NonNull final String message) {
        activity.runOnUiThread(() -> Snack.toastTopError(activity, activity.getString(R.string.toast_server_connection_failed, message)));
    }
}
