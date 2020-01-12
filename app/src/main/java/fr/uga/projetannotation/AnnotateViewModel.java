package fr.uga.projetannotation;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import androidx.lifecycle.MutableLiveData;


import java.util.ArrayList;
import java.util.List;

import fr.uga.projetannotation.database.AnnotateRepository;
import fr.uga.projetannotation.model.ContactAnnotation;
import fr.uga.projetannotation.model.EventAnnotation;
import fr.uga.projetannotation.model.PicAnnotation;

public class AnnotateViewModel extends AndroidViewModel {

    private AnnotateRepository mRepository;
    private MutableLiveData<Uri> mPicUri;
    private MutableLiveData<Uri> mEventUri;
    private MutableLiveData<List<Uri>> mContactsUri;
    private List<Uri> mContact;
    private List<Uri> mContactsDelete;

    private LiveData<List<EventAnnotation>> mAllAnnotation;

    public AnnotateViewModel(Application application) {
        super(application);
        mRepository = new AnnotateRepository(application);
        mPicUri = new MutableLiveData<>();
        mContactsUri = new MutableLiveData<>();
        mContact = new ArrayList<>();
        mContactsDelete = new ArrayList<>();
        mEventUri = new MutableLiveData<>();
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
        mContactsUri.setValue(mContact);
    }

    void setEventUri(Uri eventUri) {
        mEventUri.setValue(eventUri);
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
        if(getEventUri().getValue() != null || (getContactsUri().getValue() != null && getContactsUri().getValue().size() > 0)) {
            insertPictureEvent(new EventAnnotation(getPicUri(), getEventUri().getValue()));
            for (Uri contact : mContact) {
                insertPictureContact(new ContactAnnotation(getPicUri(), contact));
            }
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

    public void deleteAnnotation() {
        mRepository.deletePicAnnot(getPicUri());
    }
}