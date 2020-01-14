package fr.uga.projetannotation;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;

import java.util.ArrayList;
import java.util.List;

//stocke la liste des Uri de photo à afficher :
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