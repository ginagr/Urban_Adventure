package ggr.urbanadventure;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ggr.urbanadventure.database.LocationDbSchema.LocationTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ggr.urbanadventure.database.LocationBaseHelper;
import ggr.urbanadventure.database.LocationCursorWrapper;

public class LocationLab {
    private static LocationLab locationLab;

    private Context context;
    private SQLiteDatabase database;

    public static LocationLab get(Context context) {
        if (locationLab == null) {
            locationLab = new LocationLab(context);
        }
        return locationLab;
    }

    private LocationLab(Context context) {
        this.context = context.getApplicationContext();
        database = new LocationBaseHelper(this.context)
                .getWritableDatabase();
    }


    public void addLocation(AreaLocation c) {
        ContentValues values = getContentValues(c);

        database.insert(LocationTable.NAME, null, values);
    }

    public List<AreaLocation> getLocations() {
        List<AreaLocation> areaLocation = new ArrayList<>();

        LocationCursorWrapper cursor = queryLocation(null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            areaLocation.add(cursor.getLocation());
            cursor.moveToNext();
        }
        cursor.close();

        return areaLocation;
    }

    public AreaLocation getLocation(UUID id) {
        LocationCursorWrapper cursor = queryLocation(
                LocationTable.Cols.UUID + " = ?",
                new String[] { id.toString() }
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getLocation();
        } finally {
            cursor.close();
        }
    }

    public void updateLocation(AreaLocation areaLocation) {
        String uuidString = areaLocation.getId().toString();
        ContentValues values = getContentValues(areaLocation);

        database.update(LocationTable.NAME, values,
                LocationTable.Cols.UUID + " = ?",
                new String[] { uuidString });
    }

    private static ContentValues getContentValues(AreaLocation areaLocation) {
        ContentValues values = new ContentValues();
        values.put(LocationTable.Cols.UUID, areaLocation.getId().toString());
        values.put(LocationTable.Cols.TITLE, areaLocation.getTitle());
        values.put(LocationTable.Cols.LOCATIONTYPE, areaLocation.getLocationTypeInt());
        values.put(LocationTable.Cols.PICTUREPATH, areaLocation.getPicturePath());
        values.put(LocationTable.Cols.DESCRIPTION, areaLocation.getDescription());
        values.put(LocationTable.Cols.PHONENUMBER, areaLocation.getPhoneNumber());
        values.put(LocationTable.Cols.WEBSITEURL, areaLocation.getWebsiteURL());
        values.put(LocationTable.Cols.LONGITUDE, areaLocation.getLongitude());
        values.put(LocationTable.Cols.LATITUDE, areaLocation.getLatitude());
        values.put(LocationTable.Cols.HASVISITED, areaLocation.isHasVisited() ? 1 : 0);


        return values;
    }

    private LocationCursorWrapper queryLocation(String whereClause, String[] whereArgs) {
        Cursor cursor = database.query(
                LocationTable.NAME,
                null, // Columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null  // orderBy\

        );

        return new LocationCursorWrapper(cursor);
    }

    void deleteAll()
    {
        database.delete(LocationTable.NAME, null, null);

    }
}
