package ggr.urbanadventure.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import ggr.urbanadventure.database.LocationDbSchema.LocationTable;

import java.util.UUID;

import ggr.urbanadventure.AreaLocation;

public class LocationCursorWrapper extends CursorWrapper {
    public LocationCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public AreaLocation getLocation() {
        String uuidString = getString(getColumnIndex(LocationTable.Cols.UUID));
        String title = getString(getColumnIndex(LocationTable.Cols.TITLE));
        int locationType = getInt(getColumnIndex(LocationTable.Cols.LOCATIONTYPE));
        String picturePath = getString(getColumnIndex(LocationTable.Cols.PICTUREPATH));
        String description = getString(getColumnIndex(LocationTable.Cols.DESCRIPTION));
        double longitude = getDouble(getColumnIndex(LocationTable.Cols.LONGITUDE));
        double latitude = getDouble(getColumnIndex(LocationTable.Cols.LATITUDE));
        int hasVisited = getInt(getColumnIndex(LocationTable.Cols.HASVISITED));
        String phoneNumber = getString(getColumnIndex(LocationTable.Cols.PHONENUMBER));
        String websiteURL = getString(getColumnIndex(LocationTable.Cols.WEBSITEURL));

        AreaLocation areaLocation = new AreaLocation(title, locationType, description, latitude, longitude);
        areaLocation.setId(UUID.fromString(uuidString));
        areaLocation.setPicturePath(picturePath);
        areaLocation.setPhoneNumber(phoneNumber);
        areaLocation.setWebsiteURL(websiteURL);
        areaLocation.setHasVisited(hasVisited != 0);

        return areaLocation;
    }
}
