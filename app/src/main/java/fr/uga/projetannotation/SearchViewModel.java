package fr.uga.projetannotation;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import androidx.lifecycle.MutableLiveData;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.uga.projetannotation.database.AnnotateRepository;

public class SearchViewModel extends AndroidViewModel {

    private AnnotateRepository mRepository;
    private MutableLiveData<Uri> mEventUri;
    private MutableLiveData<List<Uri>> mContactsUri;
    private List<Uri> mContact;
    private long tpsDepart;
    private long tpsFin;
    private String displayDateDepart;
    private String displayDateFin;

    public SearchViewModel(Application application) {
        super(application);
        mRepository = new AnnotateRepository(application);
        mContactsUri = new MutableLiveData<>();
        mContact = new ArrayList<>();
        mEventUri = new MutableLiveData<>();
        tpsDepart = -1;
        tpsFin = -1;
        displayDateDepart = "";
        displayDateFin = "";
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

    //Utile pour lister les événements entre deux dates via query sur CalendarContract :
    void setDateDepart(int year, int month, int dayOfMonth) {
        if(year !=0 && month !=20 && dayOfMonth !=0) {
            Calendar c = Calendar.getInstance();
            c.set(year, month, dayOfMonth, 0, 0);
            tpsDepart = c.getTimeInMillis();
            month = month+1;
            displayDateDepart = dayOfMonth+"/"+month+"/"+year;
        } else {
            tpsDepart = -1;
            displayDateDepart = "";
        }
    }
    void setDateFin(int year, int month, int dayOfMonth) {
        if(year !=0 && month !=20 && dayOfMonth !=0) {
            Calendar c = Calendar.getInstance();
            c.set(year, month, dayOfMonth, 0, 0);
            tpsFin = c.getTimeInMillis();
            month = month+1;
            displayDateFin = dayOfMonth+"/"+month+"/"+year;
        } else {
            tpsFin = -1;
            displayDateFin = "";
        }
    }

    public MutableLiveData<Uri> getEventUri() {
        return mEventUri;
    }

    public long getTpsDepart() {
        return tpsDepart;
    }

    public long getTpsFin(){
        return tpsFin;
    }

    public String getDisplayDateDepart() { return displayDateDepart;}

    public String getDisplayDateFin() { return displayDateFin;}

    public MutableLiveData<List<Uri>> getContactsUri() { return mContactsUri; }

    void deleteEvent(){
        mEventUri.setValue(null);
    }

    public LiveData<List<Uri>> search() {

        if((getContactsUri().getValue() != null && getContactsUri().getValue().size() == 0 && getEventUri().getValue() != null) || (getContactsUri().getValue() == null && getEventUri().getValue()!= null)){
            //recherche par event uniquement :
            return mRepository.searchByEvent(getEventUri().getValue());
        } else if (getEventUri().getValue() == null && getContactsUri().getValue() != null && getContactsUri().getValue().size() != 0 ) {
            //recherche par contact uniquement
            return mRepository.searchByContacts(getContactsUri().getValue());
        } else if(tpsFin != -1 && tpsDepart !=-1 && tpsDepart < tpsFin && ContextCompat.checkSelfPermission(this.getApplication(), Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            //recherche par liste d'event compris entre deux dates :

            //on recherche la liste des événements compris entre ces deux dates :
            Cursor mCursor = this.getApplication().getContentResolver().query(
                    CalendarContract.Events.CONTENT_URI,
                    new String [] {CalendarContract.Events._ID,CalendarContract.Events.DTSTART,CalendarContract.Events.DTEND},
                    CalendarContract.Events.DTSTART + "<" + tpsFin + " AND " + tpsDepart + "<" + CalendarContract.Events.DTEND,
                    null,
                    CalendarContract.Events.DTSTART + " ASC");

            // on ajoute les URI des events concerné dans une liste afin de rechercher sur cette liste dans la BDD :
            List<Uri> mEvents = new ArrayList<>();
            for(mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mEvents.add(Uri.withAppendedPath(CalendarContract.Events.CONTENT_URI, String.valueOf(mCursor.getLong(mCursor.getColumnIndex(CalendarContract.Events._ID)))));
            }

            return mRepository.searchByEvents(mEvents);
        } else {
            //recherche par événement et Contact :
                return  mRepository.searchByEventAndContacts(getEventUri().getValue(), getContactsUri().getValue());
        }
    }
}