package com.books.leemon.buchstabenkiste.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.books.leemon.buchstabenkiste.R;
import com.books.leemon.buchstabenkiste.activities.MainActivity;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by user on 14.06.2016.
 */
public class BookListAdapter extends SimpleCursorAdapter {

    @Bind(R.id.listBookCover)
    ImageView listBookCover;
    @Bind(R.id.listBookTitle)
    TextView listBookTitle;
    @Bind(R.id.listBookAuthor)
    TextView listBookAuthor;

    public BookListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }


    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        Cursor c=getCursor();
        return LayoutInflater.from(context).inflate(R.layout.list_view_item_book, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ButterKnife.bind(this, view);

        if (cursor != null&&cursor.getCount()>0) {
            // Extract properties from cursor
            String cover = cursor.getString(cursor.getColumnIndexOrThrow("cover"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String author = cursor.getString(cursor.getColumnIndexOrThrow("author"));

            // Populate fields with extracted properties
            listBookAuthor.setText(author);
            listBookTitle.setText(title);

            Picasso.with(context)
                    .load(cover)
                    .placeholder(R.drawable.ic_book_stack)
                    .error(R.drawable.ic_book_stack)
                    .into(listBookCover);
        }
    }


}
