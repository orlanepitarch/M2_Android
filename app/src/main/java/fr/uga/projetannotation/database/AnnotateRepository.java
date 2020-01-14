package fr.uga.projetannotation.database;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.LiveData;

import java.util.List;

import fr.uga.projetannotation.model.ContactAnnotation;
import fr.uga.projetannotation.model.EventAnnotation;
import fr.uga.projetannotation.model.PicAnnotation;

public class AnnotateRepository {
    private PicAnnotationDao mAnnotationDao;

    public AnnotateRepository(Application application) {
        AnnotationDatabase db = AnnotationDatabase.getDatabase(application);
        mAnnotationDao = db.getPicAnnotationDao();
    }

    public void insertPictureEvent(EventAnnotation event) {
        AnnotationDatabase.databaseWriteExecutor.execute(() -> {
            mAnnotationDao.insertPictureEvent(event);
        });
    }
    public void insertPictureContact(ContactAnnotation contact) {
        AnnotationDatabase.databaseWriteExecutor.execute(() -> {
            mAnnotationDao.insertPictureContact(contact);
        });

    }

    public LiveData<PicAnnotation> getPicAnnotation(Uri picUri) {
        return mAnnotationDao.getPicAnnotation(picUri);
    }

    public LiveData<List<Uri>> getAllAnnotation() {
        return mAnnotationDao.loadAnnotations();
    }

    public void deletePictureContact(ContactAnnotation contactUri) {
        AnnotationDatabase.databaseWriteExecutor.execute(() -> {
            mAnnotationDao.deleteContact(contactUri);
        });
    }

    public void deleteAll(){
        AnnotationDatabase.databaseWriteExecutor.execute(() -> {
            mAnnotationDao.deleteAllEvent();
            mAnnotationDao.deleteAllContact();
        });
    }

    public void deletePicAnnot(Uri picUri) {
        AnnotationDatabase.databaseWriteExecutor.execute(() -> {
            mAnnotationDao.deletePicEvent(picUri);
            mAnnotationDao.deletePicContacts(picUri);
        });
    }

    public LiveData<List<Uri>> searchByContacts(List<Uri> contactsUri) {
        return mAnnotationDao.searchByContacts(contactsUri, contactsUri.size());
    }

    public LiveData<List<Uri>> searchByEvent(Uri eventUri) {
        return mAnnotationDao.searchByEvent(eventUri);
    }

    public LiveData<List<Uri>> searchByEvents(List<Uri> eventsUri) {
        return mAnnotationDao.searchByEvents(eventsUri);
    }

    public LiveData<List<Uri>> searchByEventAndContacts(Uri eventUri, List<Uri> contactsUri) {
        return mAnnotationDao.searchByEventAndContacts(eventUri, contactsUri, contactsUri.size());
    }


}
