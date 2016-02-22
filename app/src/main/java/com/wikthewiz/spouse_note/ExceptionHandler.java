package com.wikthewiz.spouse_note;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

/**
 * Created by fimiljen on 2016-02-15.
 */
public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Context mFullscreenNoteActivity;

    public ExceptionHandler(FullscreenNoteActivity fullscreenNoteActivity) {
        mFullscreenNoteActivity = fullscreenNoteActivity;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.d("EXCEPTION: thread:" + thread.getName(), ex.getMessage() + "\n" + Log.getStackTraceString(ex));

        AlertDialog.Builder messageBox = new AlertDialog.Builder(mFullscreenNoteActivity);
        messageBox.setTitle("Error");
        messageBox.setMessage(ex.getMessage());
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        messageBox.show();
    }
}
