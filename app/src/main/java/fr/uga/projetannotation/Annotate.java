package fr.uga.projetannotation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import fr.uga.projetannotation.model.ContactAnnotation;
import fr.uga.projetannotation.model.PicAnnotation;

public class Annotate extends AppCompatActivity {

    private static final int PICK_IMG = 1;
    private static final int PICK_CONTACT = 2;
    private static final int PICK_EVENT = 3;
    private ImageView imgView;
    private RecyclerView contactView;
    private TextView eventView;
    private final static int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private boolean readContactAuthorized = false;
    private ContactListAdapter adapter;
    private AnnotateViewModel mAnnotateViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotate);

        // on récupère l'intent créant l'activité :
        Intent intent = this.getIntent();

        //on instancie les variables, le ViewModel ainsi que l'adapter utile au recyclerView affichant les contacts :
        imgView = this.findViewById(R.id.imageView);

        contactView = this.findViewById(R.id.contactView);
        adapter = new ContactListAdapter(this);
        contactView.setAdapter(adapter);
        contactView.setLayoutManager(new LinearLayoutManager(this));

        mAnnotateViewModel = new ViewModelProvider(this).get(AnnotateViewModel.class);

        eventView = this.findViewById(R.id.eventPick);

        //Si les données changent sur l'UI, on doit également modifier dans le VM (notamment lors d'une suppresssion)
        adapter.getAllContactsUri().observe(this, new  Observer<List<Uri>>() {
            @Override
            public void onChanged(List<Uri> listContact) {
                mAnnotateViewModel.setListContact(listContact);
            }
        });

        //si des données sont stockées dans le VM -> l'activité est récrée suite à une rotation de l'écran, on doit réafficher les données:
        if(mAnnotateViewModel.getPicUri() != null ) {
            imgView.setImageURI(mAnnotateViewModel.getPicUri());
            if(mAnnotateViewModel.getContactsUri().getValue() != null) {
                adapter.setListContact(mAnnotateViewModel.getContactsUri().getValue());
            }
            if (mAnnotateViewModel.getEventUri().getValue() != null) {
                if (findViewById(R.id.btnDeleteEvent) != null) {
                    eventView.setText(getEventName(mAnnotateViewModel.getEventUri().getValue().getLastPathSegment()));
                    ImageView btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
                    btnDeleteEvent.setVisibility(View.VISIBLE);
                }

            }

        }
        else {
            // Si la photo pour l'annotation est choisie depuis le téléphone puis partagée à notre application (données dans l'extra de l'intent, mises par l'activité MAINACTIVITY) :
            if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getStringExtra("IMGURI") != null) {
                Uri imgUri = (Uri) Uri.parse(intent.getStringExtra("IMGURI"));
                //si elle ne vient pas des documents du téléphone, on ne pourra pas l'afficher dans la galerie d'annotations, on l'indique donc à l'utilisateur :
                if(!Arrays.asList(imgUri.toString().split("/")).get(3).equals("document")) {
                    Toast.makeText(this,"Attention, cette source d'image n'est pas compatible avec la galerie d'annotation",Toast.LENGTH_LONG).show();
                }

                mAnnotateViewModel.setPicUri(imgUri);

                // Permet de définir les données contact et event si la photo est déjà annotée dans la BDD :
                mAnnotateViewModel.getPicAnnotation(imgUri).observe(this, new Observer<PicAnnotation>() {
                    public void onChanged(@Nullable PicAnnotation annotation) {
                        if (annotation != null) {
                            if (annotation.getEventUri() != null) {
                                eventView.setText(getEventName(annotation.getEventUri().getLastPathSegment()));
                                mAnnotateViewModel.setEventUri(annotation.getEventUri());
                                findViewById(R.id.btnDeleteEvent).setVisibility(View.VISIBLE);
                            }
                            if (annotation.getContactUris() != null) {
                                for (Uri contact : annotation.getContactUris()) {
                                    mAnnotateViewModel.setContactUri(contact);
                                }
                                adapter.setListContact(annotation.getContactUris());
                            }
                        }
                    }
                });

                //Changement si la valeur de l'event change, on doit actualiser la valeur sur l'UI
                mAnnotateViewModel.getEventUri().observe(this, new  Observer<Uri>() {
                    @Override
                    public void onChanged(Uri uri) {
                        if(uri != null) {
                            String id = uri.getLastPathSegment();
                            eventView.setText(getEventName(id));
                        }
                    }
                });

                //afficher l'image :
                imgView.setImageURI(imgUri);

                // permet de supprimer un contact dans la BDD si un contact est supprimé sur l'UI (si la suppression est annulée, on réinsérera ce contact dans la BDD):
                adapter.getContactDelete().observe(this, new Observer<Uri>() {
                    @Override
                    public void onChanged(Uri uri) {
                        mAnnotateViewModel.deleteContact(uri);
                    }
                });

            // l'utilisateur lance l'annotation sans photo préalable -> pick photo à faire
            } else {
                if(intent.getStringExtra("IMGURI") == null) {
                    Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    pick.setType("image/*");
                    pick.addFlags((Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
                    this.startActivityForResult(pick, PICK_IMG);
                }
            }
        }



    }

    //Affiche un message pour choisir entre "Sauvegarder et Quitter" et "quitter" quand l'utilisateur appuie sur le bouton retour de son téléphone :
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Handle the back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage("Êtes vous sûr de vouloir quitter sans sauvegarder ?")

                    //si on ne veut pas sauvegarder les changements mais que l'utilisateur avait supprimé des contacts, on doit les réinsérer dans la BDD :
                    .setPositiveButton("Quitter", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mAnnotateViewModel.getDeletedContact().size() > 0) {
                                for (Uri contact : mAnnotateViewModel.getDeletedContact()) {
                                    mAnnotateViewModel.insertPictureContact(new ContactAnnotation(mAnnotateViewModel.getPicUri(), contact));
                                }
                            }
                            finish();
                        }

                    })
                    .setNegativeButton("Sauvegarder & quitter", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAnnotateViewModel.save();
                            finish();
                        }
                    })
                    .show();

                return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    // affiche les données, test sur les request code pour séparer le code en fonction de la provenance du startActivityForResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // on stop l'annotation si aucune image n'a été choisi :
        if(requestCode == PICK_IMG && resultCode != RESULT_OK) {
            finish();
        }
        if (requestCode == PICK_IMG && resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();

            final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // Check for the freshest data.
            this.getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
            mAnnotateViewModel.setPicUri(imageUri);

            imgView.setImageURI(mAnnotateViewModel.getPicUri());

            // Permet de définir les données si la photo est déjà annotée dans la BDD :
            mAnnotateViewModel.getPicAnnotation(imageUri).observe(this, new Observer<PicAnnotation>() {
                public void onChanged(@Nullable PicAnnotation annotation) {
                    if(annotation != null) {
                        if(annotation.getEventUri() != null) {
                            eventView.setText(getEventName(annotation.getEventUri().getLastPathSegment()));
                            mAnnotateViewModel.setEventUri(annotation.getEventUri());
                            findViewById(R.id.btnDeleteEvent).setVisibility(View.VISIBLE);
                        }
                        if(annotation.getContactUris() != null) {
                            for (Uri contact : annotation.getContactUris()) {
                                mAnnotateViewModel.setContactUri(contact);
                            }
                            adapter.setListContact(annotation.getContactUris());
                        }
                    }
                }
            });

            // permet de supprimer un contact dans la BDD si un contact est supprimé sur l'UI :
            adapter.getContactDelete().observe(this, new Observer<Uri>() {
                @Override
                public void onChanged(Uri uri) {
                    mAnnotateViewModel.deleteContact(uri);
                }
            });

        }
        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            mAnnotateViewModel.setContactUri(uri);
            adapter.setContact(uri);
        }

        if (requestCode == PICK_EVENT && resultCode == RESULT_OK) {
            eventView.setText(getEventName(data.getData().getLastPathSegment()));
            mAnnotateViewModel.setEventUri(data.getData());
            ImageView btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
            btnDeleteEvent.setVisibility(View.VISIBLE);
        }

    }

    //Lance la sélection du contact si on clique sur "Choisir_Contact"
    public void onContactClick(View v) {
        checkContactReadPermission();
        if (readContactAuthorized) {
            Intent pick = new Intent(Intent.ACTION_PICK);
            pick.setData(ContactsContract.Contacts.CONTENT_URI);
            this.startActivityForResult(pick, PICK_CONTACT);
        }
    }

    //Lance l'activité ChosseEvent si on clique sur "Choisir_Event"
    public void onEventClick(View v) {
        Intent event = new Intent(Annotate.this, ChooseEvent.class);
        Annotate.this.startActivityForResult(event, PICK_EVENT);
    }

    public void onDeleteEventClick(View v) {
        ImageView btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
        btnDeleteEvent.setVisibility(View.INVISIBLE);
        mAnnotateViewModel.deleteEvent();
        eventView.setText("");
    }

    //retourne le nom d'un event d'après son id -> permet d'afficher sur l'UI le nom plutot que l'URI
    public String getEventName(String id) {
        Cursor cursor = null;
        String result = "";
        try {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED) {
                cursor = getContentResolver().query(CalendarContract
                                .Events.CONTENT_URI,
                        null,
                        CalendarContract
                                .Events._ID + "=?",
                        new String[]{id},
                        null);
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex
                            (CalendarContract.Events.TITLE));
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.v("event name", "erreur "+e);
        }
        return result;
    }

    public void checkContactReadPermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        } else {
            // Permission has already been granted
            readContactAuthorized = true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    readContactAuthorized = true;
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    // confirmation pour quitter l'activité sans sauvegarder (clic sur sur "X" dans la barre de menu)
    public void onCancelClick(View v){
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("Êtes vous sûr de vouloir quitter sans sauvegarder ?")

                //si on ne veut pas sauvegarder les changements mais que l'utilisateur avait supprimé des contact ou un event, on doit les réinsérer dans la BDD :
                .setPositiveButton("Quitter", new DialogInterface.OnClickListener()
                {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mAnnotateViewModel.getDeletedContact().size() > 0 ) {
                            for (Uri contact : mAnnotateViewModel.getDeletedContact()) {
                                mAnnotateViewModel.insertPictureContact(new ContactAnnotation(mAnnotateViewModel.getPicUri(), contact));
                            }
                        }
                        finish();
                    }

                })
                .setNegativeButton("Annuler", null)
                .show();
    }


    // Sauvegarder et quitter
    public void onFinishClick(View v){
        mAnnotateViewModel.save();
        finish();
    }

    // Supprimer l'annotation :
    public void onDeleteClick(View v){
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("Êtes vous sûr de vouloir supprimer cette annotation ?")
                .setPositiveButton("Oui", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAnnotateViewModel.deleteAnnotation();
                        finish();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

}