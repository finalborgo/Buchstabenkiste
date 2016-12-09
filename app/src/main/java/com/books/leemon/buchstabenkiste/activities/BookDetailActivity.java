package com.books.leemon.buchstabenkiste.activities;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.books.leemon.buchstabenkiste.R;
import com.books.leemon.buchstabenkiste.db.BookTable;
import com.books.leemon.buchstabenkiste.contentprovider.BooksProvider;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;


public class BookDetailActivity extends AppCompatActivity {

    @Bind(R.id.detailBookAuthor)
    TextView detailBookAuthor;
    @Bind(R.id.detailBookDescription)
    TextView detailBookDescription;
    @Bind(R.id.detailBookISBN)
    TextView detailBookISBN;
    @Bind(R.id.detailBookPages)
    TextView detailBookPages;
    @Bind(R.id.detailBookThumbnail)
    ImageView detailBookThumbnail;
    @Bind(R.id.detailBookTitle)
    TextView detailBookTitle;

    @Bind(R.id.state)
    Spinner state;


    private Uri bookUri;


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_book_detail);

        //bind Views
        ButterKnife.bind(this);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        Bundle extras = getIntent().getExtras();

        // check from the saved Instance
        bookUri = (bundle == null) ? null : (Uri) bundle
                .getParcelable(BooksProvider.CONTENT_ITEM_TYPE);

        // Or passed from the other activity
        if (extras != null) {
            bookUri = extras
                    .getParcelable(BooksProvider.CONTENT_ITEM_TYPE);

            fillData(bookUri);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        state.setSelection(state.getSelectedItemPosition(), false);
        state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ContentValues values = new ContentValues();

                Toast.makeText(BookDetailActivity.this, "selected: " + parent.getItemAtPosition(position).toString() + ", " + position, Toast.LENGTH_SHORT).show();
                values.put(BookTable.COLUMN_STATE, position);
                // Update book state
                getContentResolver().update(bookUri, values, null, null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void fillData(Uri uri) {
        String[] projection = {BookTable.COLUMN_COVER, BookTable.COLUMN_TITLE,
                BookTable.COLUMN_AUTHOR, BookTable.COLUMN_PAGES, BookTable.COLUMN_ISBN, BookTable.COLUMN_DESCRIPTION, BookTable.COLUMN_STATE};
        Cursor cursor = getContentResolver().query(uri, projection, null, null,
                null);
        if (cursor != null) {
            cursor.moveToFirst();
            String title = cursor.getString(cursor
                    .getColumnIndexOrThrow(BookTable.COLUMN_TITLE));


            detailBookTitle.setText(cursor.getString(cursor
                    .getColumnIndexOrThrow(BookTable.COLUMN_TITLE)));
            detailBookAuthor.setText(cursor.getString(cursor
                    .getColumnIndexOrThrow(BookTable.COLUMN_AUTHOR)));
            detailBookPages.setText(cursor.getString(cursor
                    .getColumnIndexOrThrow(BookTable.COLUMN_PAGES)));
            detailBookISBN.setText(cursor.getString(cursor
                    .getColumnIndexOrThrow(BookTable.COLUMN_ISBN)));
            detailBookDescription.setText(cursor.getString(cursor
                    .getColumnIndexOrThrow(BookTable.COLUMN_DESCRIPTION)));
            String cover = cursor.getString(cursor
                    .getColumnIndexOrThrow(BookTable.COLUMN_COVER));
            Picasso.with(this)
                    .load(cursor.getString(cursor
                            .getColumnIndexOrThrow(BookTable.COLUMN_COVER)))
                    /*.placeholder(R.drawable.ic_book_stack)
                    .error(R.drawable.ic_book_stack)*/
                    .into(detailBookThumbnail);
            int stateB=cursor.getInt(cursor.getColumnIndexOrThrow(BookTable.COLUMN_STATE));
            state.setSelection(cursor.getInt(cursor.getColumnIndexOrThrow(BookTable.COLUMN_STATE)));
            // always close the cursor
            cursor.close();
        }
    }

}
