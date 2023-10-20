package de.blau.android.prefs;

import java.util.List;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import de.blau.android.App;
import de.blau.android.Logic;
import de.blau.android.Main;
import de.blau.android.R;
import de.blau.android.dialogs.DataLoss;
import de.blau.android.util.DatabaseUtil;
import de.blau.android.util.FileUtil;
import de.blau.android.util.ReadFile;
import de.blau.android.util.SelectFile;
import de.blau.android.util.Snack;
import de.blau.android.util.ThemeUtils;

/** Provides an activity for editing the API list */
public class APIEditorActivity extends URLListEditActivity {

    private static final String DEBUG_TAG = "APIEditorActivity";

    private static final int MENU_COPY = 1;

    private AdvancedPrefDatabase db;

    /**
     * Construct a new instance
     */
    public APIEditorActivity() {
        super();
        addAdditionalContextMenuItem(MENU_COPY, R.string.menu_copy);
    }

    /**
     * Start the activity showing a dialog if data has been changed
     * 
     * @param activity the calling FragmentActivity
     */
    public static void start(@NonNull FragmentActivity activity) {
        Intent intent = new Intent(activity, APIEditorActivity.class);
        final Logic logic = App.getLogic();
        if (logic != null && logic.hasChanges()) {
            DataLoss.showDialog(activity, intent, -1);
        } else {
            activity.startActivity(intent);
        }
    }

    /**
     * Start the activity and return a result
     * 
     * @param activity the calling Activity
     * @param apiName the name of the api
     * @param apiUrl the url
     * @param requestCode the code to identify the result
     */
    public static void startForResult(@NonNull Activity activity, @NonNull String apiName, @NonNull String apiUrl, int requestCode) {
        Intent intent = new Intent(activity, APIEditorActivity.class);
        intent.setAction(ACTION_NEW);
        intent.putExtra(EXTRA_NAME, apiName);
        intent.putExtra(EXTRA_VALUE, apiUrl);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Preferences prefs = new Preferences(this);
        if (prefs.lightThemeEnabled()) {
            setTheme(R.style.Theme_customLight);
        }
        db = new AdvancedPrefDatabase(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getAddTextResId() {
        return R.string.urldialog_add_api;
    }

    @Override
    protected void onLoadList(List<ListEditItem> items) {
        API[] apis = db.getAPIs();
        API current = db.getCurrentAPI();
        for (API api : apis) {
            items.add(new ListEditItem(api.id, api.name, api.url, api.readonlyurl, api.notesurl, api.oauth, current.id.equals(api.id)));
        }
    }

    @Override
    protected void onItemClicked(ListEditItem item) {
        API current = db.getCurrentAPI();
        if (!item.id.equals(current.id)) {
            Main.prepareRedownload();
        }
        db.selectAPI(item.id);
        // this is a bit hackish, but only one can be selected
        for (ListEditItem lei : items) {
            lei.active = false;
        }
        item.active = true;
        updateAdapter();
    }

    @Override
    protected void onItemCreated(ListEditItem item) {
        db.addAPI(item.id, item.name, item.value, item.value2, item.value3, "", "", item.boolean0);
    }

    @Override
    protected void onItemEdited(ListEditItem item) {
        db.setAPIDescriptors(item.id, item.name, item.value, item.value2, item.value3, item.boolean0);
    }

    @Override
    protected void onItemDeleted(ListEditItem item) {
        db.deleteAPI(item.id);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        selectedItem = (ListEditItem) getListView().getItemAtPosition(info.position);
        if (selectedItem != null) {
            menu.add(Menu.NONE, MENUITEM_EDIT, Menu.NONE, r.getString(R.string.edit)).setOnMenuItemClickListener(this);
            if (!selectedItem.id.equals(LISTITEM_ID_DEFAULT)) {
                menu.add(Menu.NONE, MENUITEM_DELETE, Menu.NONE, r.getString(R.string.delete)).setOnMenuItemClickListener(this);
            }
            for (Entry<Integer, Integer> entry : additionalMenuItems.entrySet()) {
                menu.add(Menu.NONE, entry.getKey() + MENUITEM_ADDITIONAL_OFFSET, Menu.NONE, r.getString(entry.getValue())).setOnMenuItemClickListener(this);
            }
        }
    }

    @Override
    public void onAdditionalMenuItemClick(int menuItemId, ListEditItem clickedItem) {
        switch (menuItemId) {
        case MENU_COPY:
            ListEditItem item = new ListEditItem(getString(R.string.copy_of, clickedItem.name), clickedItem.value, clickedItem.value2, clickedItem.value3,
                    clickedItem.boolean0);
            db.addAPI(item.id, item.name, item.value, item.value2, item.value3, "", "", item.boolean0);
            items.clear();
            onLoadList(items);
            updateAdapter();
            break;
        default:
            Log.e(DEBUG_TAG, "Unknown menu item " + menuItemId);
            break;
        }
    }

    /**
     * Opens the dialog to edit an item
     * 
     * @param item the selected item
     */
    @Override
    protected void itemEditDialog(final ListEditItem item) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        final LayoutInflater inflater = ThemeUtils.getLayoutInflater(ctx);
        final View mainView = inflater.inflate(R.layout.listedit_apiedit, null);
        final TextView editName = (TextView) mainView.findViewById(R.id.listedit_editName);
        final TextView editValue = (TextView) mainView.findViewById(R.id.listedit_editValue);
        final TextView editValue2 = (TextView) mainView.findViewById(R.id.listedit_editValue_2);
        final TextView editValue3 = (TextView) mainView.findViewById(R.id.listedit_editValue_3);
        final CheckBox oauth = (CheckBox) mainView.findViewById(R.id.listedit_oauth);
        final ImageButton fileButton = (ImageButton) mainView.findViewById(R.id.listedit_file_button);

        if (item != null) {
            editName.setText(item.name);
            editValue.setText(item.value);
            editValue2.setText(item.value2);
            editValue3.setText(item.value3);
            oauth.setChecked(item.boolean0);
        } else if (isAddingViaIntent()) {
            String tmpName = getIntent().getExtras().getString(EXTRA_NAME);
            String tmpValue = getIntent().getExtras().getString(EXTRA_VALUE);
            editName.setText(tmpName == null ? "" : tmpName);
            editValue.setText(tmpValue == null ? "" : tmpValue);
            oauth.setChecked(false);
        }
        if (item != null && item.id.equals(LISTITEM_ID_DEFAULT)) {
            // name and value are not editable
            editName.setInputType(InputType.TYPE_NULL);
            editName.setBackground(null);
            editValue.setBackground(null);
            editValue.setInputType(InputType.TYPE_NULL);
            editValue2.setEnabled(true);
            editValue3.setEnabled(false);
        }

        setViewAndButtons(builder, mainView);

        fileButton.setOnClickListener(view -> SelectFile.read(APIEditorActivity.this, R.string.config_msfPreferredDir_key, new ReadFile() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean read(Uri uri) {
                Uri fileUri = FileUtil.contentUriToFileUri(APIEditorActivity.this, uri);
                if (fileUri == null) {
                    Snack.toastTopError(APIEditorActivity.this, R.string.not_found_title);
                    return false;
                }
                try {
                    if (!DatabaseUtil.isValidSQLite(fileUri.getPath())) {
                        throw new SQLiteException("Not a SQLite database file");
                    }
                    editValue2.setText(fileUri.toString());
                    SelectFile.savePref(new Preferences(APIEditorActivity.this), R.string.config_msfPreferredDir_key, fileUri);
                    return true;
                } catch (SQLiteException sqex) {
                    Snack.toastTopError(APIEditorActivity.this, R.string.toast_not_mbtiles);
                    return false;
                }
            }
        }));

        final AlertDialog dialog = builder.create();
        dialog.show();

        // overriding the handlers
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            boolean validAPIURL = true;
            boolean validReadOnlyAPIURL = true;
            boolean validNotesAPIURL = true;
            String name = editName.getText().toString().trim();
            String apiURL = editValue.getText().toString().trim();
            String readOnlyAPIURL = editValue2.getText().toString().trim();
            String notesAPIURL = editValue3.getText().toString().trim();
            boolean enabled = oauth.isChecked();

            // (re-)set to black
            changeBackgroundColor(editValue, VALID_COLOR);
            changeBackgroundColor(editValue2, VALID_COLOR);
            changeBackgroundColor(editValue3, VALID_COLOR);

            // validate entries
            validAPIURL = Patterns.WEB_URL.matcher(apiURL).matches();
            if (!"".equals(readOnlyAPIURL)) {
                validReadOnlyAPIURL = Patterns.WEB_URL.matcher(readOnlyAPIURL).matches() || readOnlyAPIURL.startsWith(FileUtil.FILE_SCHEME_PREFIX);
            } else {
                readOnlyAPIURL = null;
            }
            if (!"".equals(notesAPIURL)) {
                validNotesAPIURL = Patterns.WEB_URL.matcher(notesAPIURL).matches();
            } else {
                notesAPIURL = null;
            }

            // save or display toast
            if (validAPIURL && validNotesAPIURL && validReadOnlyAPIURL) { // check if fields valid, optional ones
                                                                          // checked if values entered
                if (!"".equals(apiURL)) {
                    if (item == null) {
                        // new item
                        finishCreateItem(new ListEditItem(name, apiURL, readOnlyAPIURL, notesAPIURL, enabled));
                    } else {
                        item.name = name;
                        item.value = apiURL;
                        item.value2 = readOnlyAPIURL;
                        item.value3 = notesAPIURL;
                        item.boolean0 = enabled;
                        finishEditItem(item);
                    }
                }
                dialog.dismiss();
            } else if (!validAPIURL) { // if garbage value entered show toasts
                Snack.barError(APIEditorActivity.this, R.string.toast_invalid_apiurl);
                changeBackgroundColor(editValue, ERROR_COLOR);
            } else if (!validReadOnlyAPIURL) {
                Snack.barError(APIEditorActivity.this, R.string.toast_invalid_readonlyurl);
                changeBackgroundColor(editValue2, ERROR_COLOR);
            } else if (!validNotesAPIURL) {
                Snack.barError(APIEditorActivity.this, R.string.toast_invalid_notesurl);
                changeBackgroundColor(editValue3, ERROR_COLOR);
            }
        });

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(v -> dialog.dismiss());
    }
}