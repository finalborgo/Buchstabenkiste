package com.books.leemon.buchstabenkiste.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by user on 15.06.2016.
 */
public class BookTable {

    public static final String TABLE_BOOK = "book";
    //DB Columns
    public static final String COLUMN_ROWID = "_id";
    public static final String COLUMN_ISBN = "isbn";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_COVER = "cover";
    public static final String COLUMN_PAGES = "pages";
    public static final String COLUMN_PUBLISHED = "published";
    public static final String COLUMN_STATE = "state";
    public static final String COLUMN_RATING = "rating";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_BOOK + " (" + COLUMN_ROWID + " integer primary key autoincrement, "
            + COLUMN_ISBN + " text not null, "
            + COLUMN_TITLE + " text not null, "
            + COLUMN_AUTHOR + " text not null, "
            + COLUMN_DESCRIPTION + " text, "
            + COLUMN_COVER + " text, "
            + COLUMN_PAGES + " integer not null, "
            + COLUMN_PUBLISHED + " text, "
            + COLUMN_STATE + " integer default 0, "
            + COLUMN_RATING + " integer default -1);";


    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(BookTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOK);
        onCreate(database);
    }


}
