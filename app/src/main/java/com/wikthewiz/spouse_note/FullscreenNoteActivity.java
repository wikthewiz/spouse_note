package com.wikthewiz.spouse_note;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenNoteActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private EditText mTextEditor;
    private Button publishButton;
    private Button cancelButton;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mTextEditor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mButtonsContainer;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
 //           ActionBar actionBar = getSupportActionBar();
   //         if (actionBar != null) {
     //           actionBar.show();
       //     }

            mButtonsContainer.setVisibility(View.VISIBLE);
        }
    };

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private String mCurrentText = "";
    private boolean mEditMode = false;
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }

            return false;
        }
    };

    private void onPublishClick(View view) {
        saveLocalNotes(mCurrentText, this);
        mCurrentText = this.mTextEditor.getText().toString();
    }
    private void onCancelClick(View view) {
        if(mCurrentText.equals(mTextEditor.getText().toString())){
           return;
        }

        mTextEditor.setText(mCurrentText);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_fullscreen_note);
        mButtonsContainer = findViewById(R.id.buttons_container);
        mTextEditor = (EditText)findViewById(R.id.fullscreen_content);
        publishButton = (Button)findViewById(R.id.publish_button);
        cancelButton = (Button)findViewById(R.id.cancel_button);
        setBigText();
        // Set up the user interaction to manually show or hide the system UI.
        mTextEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        mCurrentText = loadLocalNotes(this);
        mTextEditor.setText(mCurrentText);

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.publish_button).setOnTouchListener(mDelayHideTouchListener);
    }

    private String loadTextFromServer() {
        return "";
    }

    private static void saveLocalNotes(String notes, Activity a) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(a.openFileOutput(getStorageFile(a), MODE_PRIVATE));
            writer.write(notes);
            writer.flush();
        } catch (IOException e){
            throw new RuntimeException(a.getString(R.string.error_reading_local_storage), e);
        }
        finally {
            if(writer != null) {
                writer.close();
            }
        }
    }

    private static String loadLocalNotes(Activity a) {
        BufferedReader reader = null;
        try {

            File editTextFile = new File(getStorageFile(a));
            if(!editTextFile.exists()) {
                editTextFile.createNewFile();
                return "";
            }

            reader = new BufferedReader(new InputStreamReader(a.openFileInput("editText")));
            StringBuilder lines = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.append(line).append("\n");
            }

            return lines.toString();
        } catch (IOException e){
            throw new RuntimeException(a.getString(R.string.error_reading_local_storage) + "file:" + getStorageFile(a), e);
        }
        finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(a.getString(R.string.error_closing_local_storage), e);
                }
            }
        }
    }

    private static String getStorageFile(Activity a){
        return a.getFilesDir().getPath().toString() + "/" + getFileName();
    }

    private static String getFileName(){
        return "editText";
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        mEditMode = !mEditMode;
        if(mEditMode) {
            setSmallText();
            showKeyboard();
            mButtonsContainer.setVisibility(View.GONE);
        } else {
            mButtonsContainer.setVisibility(View.VISIBLE);
            setBigText();
            hideKeyboard();
            hide();
        }
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        mTextEditor.requestFocus();
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mTextEditor.getWindowToken(), 0);
        }
    }

    private void setBigText() {
        if ("".equals(mTextEditor.getText().toString())) {
            mTextEditor.setGravity(Gravity.CENTER);
            mTextEditor.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
        }
    }

    private void showKeyboard() {
       mTextEditor.requestFocus();
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInputFromInputMethod(mTextEditor.getWindowToken(), 0);
        }
    }

    private void setSmallText() {
        mTextEditor.setGravity(Gravity.START);
        mTextEditor.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
    }

    private void hide() {
        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mTextEditor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
