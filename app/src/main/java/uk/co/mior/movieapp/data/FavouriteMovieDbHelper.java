package uk.co.mior.movieapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import static uk.co.mior.movieapp.data.FavouriteMovieContract.*;

public class FavouriteMovieDbHelper extends SQLiteOpenHelper {

    // The name of the database
    private static final String DATABASE_NAME = "favouriteMoviesDb.db";

    // If you change the database scheme, you must increment the database version
    private static final int VERSION = 1;

    public FavouriteMovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create favourite movies table
//        final String CREATE_TABLE = "CREATE TABLE " + FavouriteMovieEntry.TABLE_NAME + " (" +
//                FavouriteMovieEntry._ID + " INTEGER PRIMARY KEY, " +
//                FavouriteMovieEntry.COLUMN_POSTERPATH + " TEXT NOT NULL, " +
//                FavouriteMovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
//                FavouriteMovieEntry.COLUMN_VOTEAVERAGE + " REAL NOT NULL, " +
//                FavouriteMovieEntry.COLUMN_RELEASEDATE + " TEXT NOT NULL, " +
//                FavouriteMovieEntry.COLUMN_ID + " INTEGER NOT NULL);";

        final String CREATE_TABLE = "CREATE TABLE " + FavouriteMovieEntry.TABLE_NAME + " (" +
                FavouriteMovieEntry._ID + " INTEGER PRIMARY KEY, " +
                FavouriteMovieEntry.COLUMN_ID + " INTEGER NOT NULL);";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavouriteMovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
