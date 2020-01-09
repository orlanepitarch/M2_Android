package fr.uga.projetannotation.database;

import android.net.Uri;

import androidx.room.TypeConverter;

public class Converters {

        @TypeConverter
        public static Uri fromString(String value) {
            return value == null ? null : Uri.parse(value);
        }

        @TypeConverter
        public static String UriToString(Uri value) {
            return value == null ? null : value.toString();
        }

}
