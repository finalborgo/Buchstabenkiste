package com.books.leemon.buchstabenkiste.activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.books.leemon.buchstabenkiste.R;
import com.books.leemon.buchstabenkiste.adapters.BookListAdapter;
import com.books.leemon.buchstabenkiste.apis.GoogleBooksApi;
import com.books.leemon.buchstabenkiste.db.BookTable;
import com.books.leemon.buchstabenkiste.contentprovider.BooksProvider;
import com.books.leemon.buchstabenkiste.db.DatabaseHelper;
import com.books.leemon.buchstabenkiste.models.pojo.GoogleBooksModel;
import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity  {

    //Fields for the bar code scanner
    private static final int ZBAR_SCANNER_REQUEST = 0;
    private static final int ZBAR_QR_SCANNER_REQUEST = 1;

    private GoogleBooksApi googleBooksApi;

    private BookListAdapter bookAdapter;

    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;
    private static final int DELETE_ID = Menu.FIRST + 1;


    private Uri bookUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //bind Views
        ButterKnife.bind(this);

    }

    // create the menu based on the XML defintion
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.listmenu, menu);
        return true;
    }

    // Reaction to the menu selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.insert:
                if (isCameraAvailable()) {
                    Intent intent = new Intent(MainActivity.this, ZBarScannerActivity.class);
                    startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
                } else {
                    Toast.makeText(MainActivity.this, "Rear Facing Camera Unavailable", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.export:
                exportData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public boolean isCameraAvailable() {
        PackageManager pm = getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ZBAR_SCANNER_REQUEST:
            case ZBAR_QR_SCANNER_REQUEST:
                if (resultCode == RESULT_OK) {


                    final String isbn = data.getStringExtra(ZBarConstants.SCAN_RESULT);
                    Toast.makeText(this, "Scan Result = " + isbn, Toast.LENGTH_SHORT).show();


                    googleBooksApi = GoogleBooksApi.Factory.getInstance();
                    googleBooksApi.getBookDataForISBN("isbn:" + isbn).enqueue(new Callback<GoogleBooksModel>() {
                        @Override
                        public void onResponse(Call<GoogleBooksModel> call, Response<GoogleBooksModel> response) {
                            //Check if valid ISBN --> get good result
                            int i = response.body().getTotalItems();
                            if (response.body().getTotalItems() > 0) {
                                //TODO Check for Nullpointer!!

                                //store data to db
                                saveBook(response, isbn);

                            } else { // invalid ISBN, wrong input --> no result
                                showErrorDialog("ungültige ISBN",
                                        "Die ISBN wurde nicht erkannt oder ist nicht in der Datenbank hinterlegt. " +
                                                "Du kannst den Barcode erneut scannen, die ISBN von Hand eingeben oder abbrechen.",
                                        "Scannen", "Abbrechen", "ISBN tippen", isbn);
                            }
                        }

                        @Override
                        public void onFailure(Call<GoogleBooksModel> call, Throwable t) {
                            Log.e("failed", t.getMessage());
                        }
                    });

                }
        }
    }

    private void saveBook(Response<GoogleBooksModel> response, String isbn) {
        ContentValues values = new ContentValues();
        values.put(BookTable.COLUMN_ISBN, isbn);
        values.put(BookTable.COLUMN_TITLE, response.body().getItems().get(0).getVolumeInfo().getTitle());
        values.put(BookTable.COLUMN_AUTHOR, response.body().getItems().get(0).getVolumeInfo().getAuthors().get(0));
        values.put(BookTable.COLUMN_DESCRIPTION, response.body().getItems().get(0).getVolumeInfo().getDescription());
        values.put(BookTable.COLUMN_COVER, response.body().getItems().get(0).getVolumeInfo().getImageLinks().getThumbnail());
        values.put(BookTable.COLUMN_PAGES, response.body().getItems().get(0).getVolumeInfo().getPageCount());
        values.put(BookTable.COLUMN_PUBLISHED, response.body().getItems().get(0).getVolumeInfo().getPublishedDate());

        bookUri = getContentResolver().insert(BooksProvider.CONTENT_URI, values);
    }

    private void showBookDetails(long id) {
        Intent bookDetailActivity = new Intent(MainActivity.this, BookDetailActivity.class);

        Uri todoUri = Uri.parse(BooksProvider.CONTENT_URI + "/" + id);
        bookDetailActivity.putExtra(BooksProvider.CONTENT_ITEM_TYPE, todoUri);

        startActivity(bookDetailActivity);
    }


    /*@Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {BookTable.COLUMN_ROWID, BookTable.COLUMN_COVER, BookTable.COLUMN_TITLE, BookTable.COLUMN_AUTHOR};
        CursorLoader cursorLoader = new CursorLoader(this,
                BooksProvider.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        bookAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookAdapter.swapCursor(null);
    }*/

    private void exportData() {
        SQLiteDatabase sqldb = new DatabaseHelper(this).getReadableDatabase();
        Cursor c = null;
        try {
            c = sqldb.rawQuery("select * from " + BookTable.TABLE_BOOK, null);
            int rowcount = 0;
            int colcount = 0;
            File sdCardDir = Environment.getExternalStorageDirectory();
            // to this path add a new directory path
            File dir = new File(sdCardDir.getAbsolutePath() + "/buchstabenkiste/");
            // create this directory if not already created
            dir.mkdir();
            String filename = "booksbackup.csv";
            // the name of the file to export with
//            File saveFile = new File(sdCardDir, filename);
            File saveFile = new File(dir, filename);


            FileWriter fw = new FileWriter(saveFile);

            BufferedWriter bw = new BufferedWriter(fw);

            rowcount = c.getCount();
            colcount = c.getColumnCount();
//HelloWorld
            Toast.makeText(MainActivity.this, "Bücher gespeichert unter " + saveFile.getAbsolutePath()+ " ITEMS: "+rowcount, Toast.LENGTH_LONG).show();
            Log.d("PFAD", saveFile.getAbsolutePath()+" --- "+rowcount);

            if (rowcount > 0) {
                c.moveToFirst();
                for (int i = 0; i < colcount; i++) {
                    if (i != colcount - 1) {
                        String col = c.getColumnName(i);
                        bw.write(c.getColumnName(i) + ",");

                    } else {
                        String col = c.getColumnName(i);
                        bw.write(c.getColumnName(i));
                    }
                }
                bw.newLine();

                for (int i = 0; i < rowcount; i++) {
                    c.moveToPosition(i);
                    for (int j = 0; j < colcount; j++) {
                        if (j != colcount - 1) {
                            String col = c.getString(j);
                            if (c.getString(j) == null)
                                bw.write("" + ",");
                            else
                                bw.write(c.getString(j) + ",");
                        } else {
                            String col = c.getString(j);
                            if (c.getString(j) == null)
                                bw.write("");
                            else
                                bw.write(c.getString(j));
                        }
                    }
                    bw.newLine();
                }
                bw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private void showErrorDialog(String title, String desc, String btnPos, String btnNeg, String btnNtr, String isbn) {
        AlertDialog.Builder alert = new AlertDialog.Builder(
                MainActivity.this);
        final String _isbn = isbn;

        alert.setTitle(title);
        alert.setMessage(desc);
        if (btnPos != null) {
            alert.setPositiveButton(btnPos, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startBarcodeScan();
                }
            });
        }
        alert.setNegativeButton(btnNeg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        if (btnNtr != null) {
            alert.setNeutralButton(btnNtr, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //TODO new dialog for editing ISBN
                    dialog.dismiss();

                    showSetISBNDialog(_isbn);
                }
            });
        }

        alert.show();
    }

    private void showSetISBNDialog(String isbn) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(
                MainActivity.this);
        final String _isbn = isbn;
        alert.setTitle("ISBN eingeben");
        alert.setMessage("Bitte gib die ISBN ein");
        final EditText inputISBN = new EditText(this);
        inputISBN.setText(_isbn);
        alert.setView(inputISBN);
        alert.setPositiveButton("Suchen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String newISBN = inputISBN.getText().toString();
                googleBooksApi = GoogleBooksApi.Factory.getInstance();
                googleBooksApi.getBookDataForISBN("isbn:" + newISBN).enqueue(new Callback<GoogleBooksModel>() {
                    @Override
                    public void onResponse(Call<GoogleBooksModel> call, Response<GoogleBooksModel> response) {
                        //Check if valid ISBN --> get good result
                        int i = response.body().getTotalItems();
                        if (response.body().getTotalItems() > 0) {
                            //TODO Check for Nullpointer!!

                            //store data to db
                            saveBook(response, newISBN);

                        } else { // invalid ISBN, wrong input --> no result
                            showErrorDialog("ungültige ISBN",
                                    "Die ISBN wurde nicht erkannt oder ist nicht in der Datenbank hinterlegt. " +
                                            "Du kannst den Barcode erneut scannen, die ISBN von Hand eingeben oder abbrechen.",
                                    "Scannen", "Abbrechen", "ISBN tippen", newISBN);
                        }
                    }

                    @Override
                    public void onFailure(Call<GoogleBooksModel> call, Throwable t) {
                        Log.e("failed", t.getMessage());
                    }
                });
            }
        });


        alert.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private void showSetAuthorTitleDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(
                MainActivity.this);

        alert.setTitle("Buchen suchen");
        alert.setMessage("Gib Autor und Titel des Buches an");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText author = new EditText(this);
        author.setHint("Autor");
        final EditText title = new EditText(this);
        title.setHint("Titel");

        layout.addView(author);
        layout.addView(title);

        alert.setView(layout);

        alert.setPositiveButton("Suchen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String strAuthor = author.getText().toString().replace(" ", "+");
                final String strTtitle = title.getText().toString().replace(" ", "+");
                googleBooksApi = GoogleBooksApi.Factory.getInstance();
                googleBooksApi.getBookDataForAuthorAndTitle("inauthor:" + strAuthor + "+intitle:" + strTtitle).enqueue(new Callback<GoogleBooksModel>() {
                    @Override
                    public void onResponse(Call<GoogleBooksModel> call, Response<GoogleBooksModel> response) {
                        //Check if valid ISBN --> get good result
                        int i = response.body().getTotalItems();
                        if (response.body().getTotalItems() > 0) {
                            //TODO Check for Nullpointer!!
                            final String isbn = response.body().getItems().get(0).getVolumeInfo().getIndustryIdentifiers().get(1).getIdentifier();
                            //store data to db
                            saveBook(response, isbn);

                        } else { // invalid ISBN, wrong input --> no result
                            showErrorDialog("kein Buch", "Das Buch wurde leider nicht gefunden", null, "OK", null, null);
                        }
                    }

                    @Override
                    public void onFailure(Call<GoogleBooksModel> call, Throwable t) {
                        Log.e("failed", t.getMessage());
                    }
                });
            }
        });
        alert.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        alert.show();
    }


    private void startBarcodeScan() {
        if (isCameraAvailable()) {
            Intent intent = new Intent(MainActivity.this, ZBarScannerActivity.class);
            startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
        } else {
            Toast.makeText(MainActivity.this, "Rear Facing Camera Unavailable", Toast.LENGTH_SHORT).show();
        }
    }

}