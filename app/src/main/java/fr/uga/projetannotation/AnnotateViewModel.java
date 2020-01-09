package fr.uga.projetannotation;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;

import fr.uga.projetannotation.database.AnnotateRepository;
import fr.uga.projetannotation.model.ContactAnnotation;
import fr.uga.projetannotation.model.EventAnnotation;
import fr.uga.projetannotation.model.PicAnnotation;

public class AnnotateViewModel extends AndroidViewModel {

    private AnnotateRepository mRepository;
    private MutableLiveData<Uri> mPicUri;
    private MutableLiveData<Uri> mEventUri = new MutableLiveData<Uri>();
    private MutableLiveData<List<Uri>> mContactsUri;
    private List<Uri> mContact;
    private List<Uri> mContactsDelete;

    private LiveData<List<EventAnnotation>> mAllAnnotation;

    public AnnotateViewModel (Application application) {
        super(application);
        mRepository = new AnnotateRepository(application);
        mPicUri = new MutableLiveData<>();
        mContactsUri = new MutableLiveData<>();
        mContact = new ArrayList<>();
        mContactsDelete = new ArrayList<>();
    }

    LiveData<PicAnnotation> getPicAnnotation(Uri picUri) {
        return mRepository.getPicAnnotation(picUri);
    }

    void setPicUri(Uri picUri) {
        mPicUri.setValue(picUri);
    }

    public Uri getPicUri() {
        return mPicUri.getValue();
    }

    public void setListContact(List<Uri> listContacts) {
        if (mContact != null) {
            mContact.clear();
        }
        mContact.addAll(listContacts);
        Log.v("add", mContact.toString());
        mContactsUri.setValue(mContact);
    }

    void setEventUri(Uri eventUri) {
        mEventUri.setValue(eventUri);
        //appeler insert
    }

    void setContactUri(Uri uri) {
        if(!mContact.contains(uri)) {
            mContact.add(uri);
        }
        mContactsUri.setValue(mContact);
    }

    public MutableLiveData<Uri> getEventUri() {
        return mEventUri;
    }

    public MutableLiveData<List<Uri>> getContactsUri() { return mContactsUri; }

    LiveData<List<EventAnnotation>> getAllAnnotation() { return mAllAnnotation; }

    void insertPictureEvent(EventAnnotation event) {
        mRepository.insertPictureEvent(event);
    }
    void insertPictureContact(ContactAnnotation contact) {
        mRepository.insertPictureContact(contact);
    }

    void deleteEvent(){
        mEventUri.setValue(null);
    }

    void save(){
        if (getEventUri().getValue() != null) {
            Log.v("bla", "event save");
            insertPictureEvent(new EventAnnotation(getPicUri(), getEventUri().getValue()));
        }
        for (Uri contact : mContact) {
            Log.v("SLT", contact.toString());
            insertPictureContact(new ContactAnnotation(getPicUri(), contact));
        }
        mContactsDelete = new ArrayList<>();
    }

    public void deleteContact(Uri uri) {
        mContactsDelete.add(uri);
        mRepository.deletePictureContact(new ContactAnnotation(getPicUri(), uri));
    }

    public List<Uri> getDeletedContact() {
        return mContactsDelete;
    }
}