package ggr.urbanadventure.database;

public class LocationDbSchema {
    public static final class LocationTable {
        public static final String NAME = "locations";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String LOCATIONTYPE = "locationType";
            public static final String PICTUREPATH = "picturePath";
            public static final String DESCRIPTION = "description";
            public static final String PHONENUMBER = "phoneNumber";
            public static final String WEBSITEURL = "websiteURL";
            public static final String LONGITUDE = "longitude";
            public static final String LATITUDE = "latitude";
            public static final String HASVISITED = "hasVisited";

        }
    }
}

