package de.blau.android.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import de.blau.android.HelpViewer;
import de.blau.android.Main;
import de.blau.android.R;
import de.blau.android.util.ImmersiveDialogFragment;
import de.blau.android.util.Util;

/**
 * Display a dialog giving new users minimal instructions
 *
 */
public class Newbie extends ImmersiveDialogFragment {

    private static final String DEBUG_TAG = Newbie.class.getSimpleName();

    private static final String TAG = "fragment_newbie";

    private Main main;

    /**
     * Display a dialog giving new users minimal instructions
     * 
     * @param activity the calling Activity
     */
    public static void showDialog(@NonNull FragmentActivity activity) {
        dismissDialog(activity);
        try {
            FragmentManager fm = activity.getSupportFragmentManager();
            Newbie newbieFragment = newInstance();
            newbieFragment.show(fm, TAG);
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

    /**
     * Get a new instance of Newbie
     * 
     * @return a new Newbie instance
     */
    @NonNull
    private static Newbie newInstance() {
        Newbie f = new Newbie();
        f.setShowsDialog(true);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        Log.d(DEBUG_TAG, "onAttach");
        super.onAttach(context);
        if (!(context instanceof Main)) {
            throw new ClassCastException(context.toString() + " can only be called from Main");
        }
        main = (Main) context;
    }

    @NonNull
    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(null);
        builder.setTitle(R.string.welcome_title);
        String message = getString(R.string.welcome_message);
        if (main.isFullScreen()) {
            message = message + getString(R.string.welcome_message_fullscreen);
        }
        builder.setMessage(Util.fromHtml(message));
        builder.setPositiveButton(R.string.okay, (dialog, which) -> {
            FragmentActivity activity = getActivity();
            if (activity instanceof Main) {
                main.gotoCurrentLocation();
                main.setFollowGPS(true);
            } else {
                Log.e(DEBUG_TAG, "getActivity returned null in onClick");
            }
        });
        builder.setNeutralButton(R.string.read_introduction, (dialog, which) -> {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                HelpViewer.start(activity, R.string.help_introduction);
            } else {
                Log.e(DEBUG_TAG, "getActivity returned null in onClick");
            }
        });
        return builder.create();
    }
}
