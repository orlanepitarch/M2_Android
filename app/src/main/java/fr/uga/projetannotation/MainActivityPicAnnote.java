package fr.uga.projetannotation;

import androidx.appcompat.app.AppCompatActivity;
import fr.uga.projetannotation.database.AnnotationDatabase;
import fr.uga.projetannotation.database.PicAnnotationDao;
import fr.uga.projetannotation.model.ContactAnnotation;
import fr.uga.projetannotation.model.EventAnnotation;
import fr.uga.projetannotation.model.PicAnnotation;
import fr.uga.projetannotation.R;

import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.lang.annotation.Annotation;
import java.util.List;

public class MainActivityPicAnnote extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AnnotationDatabase.databaseWriteExecutor.execute(() -> {

            AnnotationDatabase db = AnnotationDatabase.getDatabase(this.getApplication());

            PicAnnotationDao dao = db.getPicAnnotationDao();

            EventAnnotation annot = new EventAnnotation(
                    Uri.withAppendedPath(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "1"),
                    Uri.withAppendedPath(CalendarContract.Events.CONTENT_URI, "1")
            );
            dao.insertPictureEvent(annot);

            ContactAnnotation ca = new ContactAnnotation(
                    Uri.withAppendedPath(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "1"),
                    Uri.withAppendedPath(ContactsContract.RawContacts.CONTENT_URI, "1")
            );
            dao.insertPictureContact(ca);
            ca = new ContactAnnotation(
                    Uri.withAppendedPath(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "1"),
                    Uri.withAppendedPath(ContactsContract.RawContacts.CONTENT_URI, "2")
            );
            dao.insertPictureContact(ca);

        });

    }
}
