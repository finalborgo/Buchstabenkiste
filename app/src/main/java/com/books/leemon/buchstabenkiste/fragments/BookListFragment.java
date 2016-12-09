package com.books.leemon.buchstabenkiste.fragments;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.books.leemon.buchstabenkiste.R;
import com.books.leemon.buchstabenkiste.activities.BookDetailActivity;
import com.books.leemon.buchstabenkiste.activities.MainActivity;
import com.books.leemon.buchstabenkiste.adapters.BookListAdapter;
import com.books.leemon.buchstabenkiste.contentprovider.BooksProvider;
import com.books.leemon.buchstabenkiste.db.BookTable;


public class BookListFragment extends ListFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private BookListAdapter bookAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
       fillData();
        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        showBookDetails(id);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {BookTable.COLUMN_ROWID, BookTable.COLUMN_COVER, BookTable.COLUMN_TITLE, BookTable.COLUMN_AUTHOR};
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
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
    }

    private void showBookDetails(long id) {
        Intent bookDetailActivity = new Intent(getActivity(), BookDetailActivity.class);
        Uri todoUri = Uri.parse(BooksProvider.CONTENT_URI + "/" + id);
        bookDetailActivity.putExtra(BooksProvider.CONTENT_ITEM_TYPE, todoUri);
        startActivity(bookDetailActivity);
    }

    private void fillData() {
        // Fields from the database (projection)
        // Must include the _id column for the adapter to work
        String[] from = new String[]{BookTable.COLUMN_COVER, BookTable.COLUMN_TITLE, BookTable.COLUMN_AUTHOR};
        // Fields on the UI to which we map
        int[] to = new int[]{R.id.listBookCover, R.id.listBookTitle, R.id.listBookAuthor};

        getLoaderManager().initLoader(0, null, this);
        bookAdapter = new BookListAdapter(getActivity(), R.layout.list_view_item_book, null, from,
                to, 0);

        setListAdapter(bookAdapter);


    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), "Item löschen", Toast.LENGTH_LONG).show();
        boolean deleted = false;
        Uri uri = Uri.parse(BooksProvider.CONTENT_URI + "/" + id);
        int rowsDeleted = getActivity().getContentResolver().delete(uri, null, null);
        if (rowsDeleted == 1) {
            deleted = true;
        }
        return deleted;
    }
}
