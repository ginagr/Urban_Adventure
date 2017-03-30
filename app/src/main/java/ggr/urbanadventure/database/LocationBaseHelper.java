package ggr.urbanadventure.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ggr.urbanadventure.database.LocationDbSchema.LocationTable;

public class LocationBaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "LocationBaseHelper";
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "locationBase.db";

    public LocationBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + LocationTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                LocationTable.Cols.UUID + ", " +
                LocationTable.Cols.TITLE + ", " +
                LocationTable.Cols.LOCATIONTYPE + ", " +
                LocationTable.Cols.PICTUREPATH + ", " +
                LocationTable.Cols.DESCRIPTION + ", " +
                LocationTable.Cols.PHONENUMBER + ", " +
                LocationTable.Cols.WEBSITEURL + ", " +
                LocationTable.Cols.LONGITUDE + ", " +
                LocationTable.Cols.LATITUDE + ", " +
                LocationTable.Cols.HASVISITED +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}