package fr.uga.projetannotation.database;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.ArrayList;
import java.util.List;

import fr.uga.projetannotation.model.ContactAnnotation;
import fr.uga.projetannotation.model.EventAnnotation;
import fr.uga.projetannotation.model.PicAnnotation;

public class AnnotateRepository {
    private PicAnnotationDao mAnnotationDao;
    //private LiveData<List<EventAnnotation>> mAllAnnotation;

    //ajouter get et set pour les events et contact avec pour paraméter l'uri de la photo

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public AnnotateRepository(Application application) {
        AnnotationDatabase db = AnnotationDatabase.getDatabase(application);
        mAnnotationDao = db.getPicAnnotationDao();
       // mAllAnnotation = mAnnotationDao.loadAnnotations();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    /*public LiveData<List<EventAnnotation>> getAllAnnotation() {
        return mAllAnnotation;
    }*/

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insertPictureEvent(EventAnnotation event) {
        AnnotationDatabase.databaseWriteExecutor.execute(() -> {
            mAnnotationDao.insertPictureEvent(event);
        });
        Log.v("repo insert", event.toString());
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

    public LiveData<Uri> getEventAnnotation(Uri picUri) {
        if (mAnnotationDao.getEventAnnotation(picUri).getValue() != null) {
            Log.v("repo", mAnnotationDao.getEventAnnotation(picUri).getValue().toString());
        }
        return mAnnotationDao.getEventAnnotation(picUri);
    }

    public LiveData<Uri> getEvent(Uri picUri) {
        return mAnnotationDao.getEvent(picUri);
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

    public LiveData<List<Uri>> searchByOneContact(Uri contactUri) {
        return mAnnotationDao.searchByOneContact(contactUri);
    }

    public LiveData<List<Uri>> searchByContacts(List<Uri> contactsUri) {
        return mAnnotationDao.searchByContacts(contactsUri, contactsUri.size());
    }

    public LiveData<List<Uri>> searchByEvent(Uri eventUri) {
        return mAnnotationDao.searchByEvent(eventUri);
    }

    public LiveData<List<Uri>> searchByEventAndContacts(Uri eventUri, List<Uri> contactsUri) {
        return mAnnotationDao.searchByEventAndContacts(eventUri, contactsUri, contactsUri.size());
    }


}
