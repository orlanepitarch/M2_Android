package fr.uga.projetannotation.database;

import android.net.Uri;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.sqlite.db.SupportSQLiteQuery;

import fr.uga.projetannotation.model.*;

@Dao
public interface PicAnnotationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPictureEvent(EventAnnotation a);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPictureContact(ContactAnnotation a);

    @Query("DELETE FROM event_annotation")
    void deleteAll();

    @Transaction
    @Query("SELECT * FROM event_annotation")
    List<EventAnnotation> loadAnnotations();

    @Query("SELECT eventUri from event_annotation where picUri=:picUri")
    LiveData<Uri> getEvent(Uri picUri);

    @Delete
    void deleteContact(ContactAnnotation c);

    @Transaction
    @Query("SELECT * from event_annotation where picUri=:picUri")
    LiveData<PicAnnotation> getPicAnnotation(Uri picUri);

    @Query("SELECT eventUri from event_annotation where picUri=:picUri")
    LiveData<Uri> getEventAnnotation(Uri picUri);

    @RawQuery
    LiveData<List<PicAnnotation>> getAnnotation(SupportSQLiteQuery query);
}
