package fr.uga.projetannotation.database;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import fr.uga.projetannotation.model.*;

@Database(entities={EventAnnotation.class, ContactAnnotation.class}, version=1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AnnotationDatabase extends RoomDatabase {

    public abstract PicAnnotationDao getPicAnnotationDao();

    private static volatile AnnotationDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AnnotationDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AnnotationDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AnnotationDatabase.class, "annotation_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
