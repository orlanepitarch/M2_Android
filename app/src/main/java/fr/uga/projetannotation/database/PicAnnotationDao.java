package fr.uga.projetannotation.database;

import android.net.Uri;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import fr.uga.projetannotation.model.*;

@Dao
public interface PicAnnotationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPictureEvent(EventAnnotation a);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPictureContact(ContactAnnotation a);

    @Query("DELETE FROM event_annotation")
    void deleteAllEvent();

    @Query("DELETE FROM contact_annotation")
    void deleteAllContact();

    @Transaction
    @Query("SELECT picUri FROM event_annotation UNION SELECT picUri FROM contact_annotation")
    LiveData<List<Uri>> loadAnnotations();

    @Delete
    void deleteContact(ContactAnnotation c);

    @Query("DELETE FROM event_annotation WHERE picUri=:uri")
    void deletePicEvent(Uri uri);

    @Query("DELETE FROM contact_annotation WHERE picUri=:uri")
    void deletePicContacts(Uri uri);

    @Transaction
    @Query("SELECT * from event_annotation where picUri=:picUri")
    LiveData<PicAnnotation> getPicAnnotation(Uri picUri);

    @Transaction
    @Query("SELECT picUri from contact_annotation where contactUri IN (:contactUri) GROUP BY picUri HAVING COUNT(picUri) == :sizeContact")
    LiveData<List<Uri>> searchByContacts(List<Uri> contactUri, Integer sizeContact);

    @Transaction
    @Query("SELECT picUri from event_annotation where eventUri=:eventUri")
    LiveData<List<Uri>> searchByEvent(Uri eventUri);

    @Transaction
    @Query("SELECT picUri from event_annotation where eventUri IN (:eventsUri)")
    LiveData<List<Uri>> searchByEvents(List<Uri> eventsUri);

    @Transaction
    @Query("SELECT picUri from event_annotation where eventUri=:eventUri INTERSECT SELECT picUri from contact_annotation where contactUri IN (:contactUri) GROUP BY picUri HAVING COUNT(picUri) == :sizeContact")
    LiveData<List<Uri>> searchByEventAndContacts(Uri eventUri, List<Uri> contactUri, Integer sizeContact);

}
