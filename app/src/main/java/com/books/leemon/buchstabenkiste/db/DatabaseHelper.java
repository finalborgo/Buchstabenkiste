package com.books.leemon.buchstabenkiste.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

/**
 * Created by user on 14.06.2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    //DB Constants
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "letterbox.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This method is called when the app is first installed and no
     * database has yet been created.
     */
    @Override
    public void onCreate(SQLiteDatabase database) {
        BookTable.onCreate(database);
    }


    /**
     * This method will be called in the future when we release
     * version 2.0 of our Tasks app, at which point we'll need to
     * upgrade our database from version 1.0 to version 2.0.
     * For now, there's nothing that needs to be done here.
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
       BookTable.onUpgrade(database, oldVersion, newVersion);
        ;
    }



}
