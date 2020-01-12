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

public class DisplayGalleryViewModel extends AndroidViewModel {

    private List<Uri> mPicUris;

    public DisplayGalleryViewModel(Application application) {
        super(application);
        mPicUris = new ArrayList<>();
    }

    public void setPicUris(List<Uri> listContacts) {
        if (mPicUris != null) {
            mPicUris.clear();
        }
        mPicUris.addAll(listContacts);
    }


    public List<Uri> getPicUris() { return mPicUris; }

}