package fr.uga.projetannotation;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;


import java.util.ArrayList;
import java.util.List;

import fr.uga.projetannotation.database.AnnotateRepository;
import fr.uga.projetannotation.model.ContactAnnotation;
import fr.uga.projetannotation.model.EventAnnotation;
import fr.uga.projetannotation.model.PicAnnotation;

public class SearchViewModel extends AndroidViewModel {

    private AnnotateRepository mRepository;
    private MutableLiveData<Uri> mEventUri;
    private MutableLiveData<List<Uri>> mContactsUri;
    private List<Uri> mContact;

    private LiveData<List<EventAnnotation>> mAllAnnotation;

    public SearchViewModel(Application application) {
        super(application);
        mRepository = new AnnotateRepository(application);
        mContactsUri = new MutableLiveData<>();
        mContact = new ArrayList<>();
        mEventUri = new MutableLiveData<Uri>();
    }

    LiveData<PicAnnotation> getPicAnnotation(Uri picUri) {
        return mRepository.getPicAnnotation(picUri);
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

    void deleteEvent(){
        mEventUri.setValue(null);
    }

    public LiveData<List<Uri>> search() {
        //recherche par event :
        if(getContactsUri().getValue().size() == 0 && getEventUri().getValue() != null) {
            Log.v("lll", "by envent only" + getEventUri().getValue().toString());
            return mRepository.searchByEvent(getEventUri().getValue());
        } else if (getEventUri().getValue() == null && getContactsUri().getValue().size() != 0 ) {
            if (getContactsUri().getValue().size() == 1) {
                Log.v("lll", "by one contact only " + getContactsUri().getValue().get(0).toString());
                return mRepository.searchByOneContact(getContactsUri().getValue().get(0));
            } else {
                Log.v("lll", "by more contact " + getContactsUri().getValue().toString());
                return mRepository.searchByContacts(getContactsUri().getValue());
            }
        } else {
                Log.v("lll", "by contact and event " + getContactsUri().getValue().toString() + "  " + getEventUri().getValue().toString());
                return  mRepository.searchByEventAndContacts(getEventUri().getValue(), getContactsUri().getValue());
        }
    }
}