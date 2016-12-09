package com.books.leemon.buchstabenkiste.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.books.leemon.buchstabenkiste.db.BookTable;
import com.books.leemon.buchstabenkiste.db.DatabaseHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by user on 14.06.2016.
 */
public class BooksProvider extends ContentProvider {

//database
    private DatabaseHelper dbHelper;

    //ContentProvider
    public static String AUTHORITY = "com.books.leeomon.buchstabenkiste.contentprovider";
    private static final String BASE_PATH = "books";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    //MIME Typen
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/books";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/book";

    //Uri-Matcher
    private static final int BOOKS = 10;
    private static final int BOOK_ID = 20;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, BOOKS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", BOOK_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }


    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        checkColumns(projection);

        queryBuilder.setTables(BookTable.TABLE_BOOK);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case BOOKS:
                break;
            case BOOK_ID:
                queryBuilder.appendWhere(BookTable.COLUMN_ROWID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
         return uri.getPath();
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //TODO check for duplicates: ISBN & author+title
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case BOOKS:
                id = sqlDB.insert(BookTable.TABLE_BOOK, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case BOOKS:
                rowsDeleted = sqlDB.delete(BookTable.TABLE_BOOK, selection,
                        selectionArgs);
                break;
            case BOOK_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(BookTable.TABLE_BOOK,
                            BookTable.COLUMN_ROWID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(BookTable.TABLE_BOOK,
                            BookTable.COLUMN_ROWID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case BOOKS:
                rowsUpdated = sqlDB.update(BookTable.TABLE_BOOK,
                        values,
                        selection,
                        selectionArgs);
                break;
            case BOOK_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(BookTable.TABLE_BOOK,
                            values,
                            BookTable.COLUMN_ROWID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(BookTable.TABLE_BOOK,
                            values,
                            BookTable.COLUMN_ROWID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = {BookTable.COLUMN_ROWID, BookTable.COLUMN_AUTHOR, BookTable.COLUMN_COVER, BookTable.COLUMN_DESCRIPTION, BookTable.COLUMN_ISBN, BookTable.COLUMN_PAGES, BookTable.COLUMN_PUBLISHED, BookTable.COLUMN_RATING, BookTable.COLUMN_STATE, BookTable.COLUMN_TITLE
        };
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

}
