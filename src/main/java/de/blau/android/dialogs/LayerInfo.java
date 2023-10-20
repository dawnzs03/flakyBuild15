package de.blau.android.dialogs;

import android.os.Bundle;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import de.blau.android.R;
import de.blau.android.listener.DoNothingListener;
import de.blau.android.util.InfoDialogFragment;
import de.blau.android.util.ThemeUtils;
import de.blau.android.util.Util;

/**
 * A generic dialog fragment to display some info on layers
 * 
 * @author simon
 *
 */
public abstract class LayerInfo extends InfoDialogFragment {

    private static final String DEBUG_TAG = LayerInfo.class.getName();

    private static final String TAG = "fragment_layer_info";

    /**
     * Show an info dialog
     * 
     * @param activity the calling Activity
     * @param layerInfoFragment an instance of fragment we are going to show
     * @param <T> a class that extends LayerInfo
     */
    public static <T extends LayerInfo> void showDialog(@NonNull FragmentActivity activity, T layerInfoFragment) {
        dismissDialog(activity);
        try {
            FragmentManager fm = activity.getSupportFragmentManager();
            layerInfoFragment.show(fm, TAG);
        } catch (IllegalStateException isex) {
            Log.e(DEBUG_TAG, "showDialog", isex);
        }
    }

    /**
     * Dismiss the dialog
     * 
     * @param activity the calling Activity
     */
    private static void dismissDialog(@NonNull FragmentActivity activity) {
        de.blau.android.dialogs.Util.dismissDialog(activity, TAG);
    }

    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new AlertDialog.Builder(getActivity());
        DoNothingListener doNothingListener = new DoNothingListener();
        builder.setPositiveButton(R.string.done, doNothingListener);
        builder.setView(createView(null));
        return builder.create();
    }

    /**
     * Create the view we want to display
     * 
     * Classes extending LayerInfo need to override this but call through to the super method to get the view
     * 
     * @param container parent view or null
     * @return the View
     */
    protected ScrollView createEmptyView(@Nullable ViewGroup container) {
        LayoutInflater inflater = ThemeUtils.getLayoutInflater(getActivity());
        return (ScrollView) inflater.inflate(R.layout.element_info_view, container, false);
    }

    /**
     * Get the string resource formated as an italic string
     * 
     * @param resId String resource id
     * @return a Spanned containing the string
     */
    protected Spanned toItalic(int resId) {
        return Util.fromHtml("<i>" + getString(resId) + "</i>");
    }
}
