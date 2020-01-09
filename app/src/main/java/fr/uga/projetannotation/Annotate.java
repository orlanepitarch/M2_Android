package fr.uga.projetannotation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.*;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import fr.uga.projetannotation.model.ContactAnnotation;
import fr.uga.projetannotation.model.PicAnnotation;

//TODO Sauvegarder et quitter ou juste quitter quand quit(annotate)
public class Annotate extends AppCompatActivity {

    private static final int PICK_IMG = 1;
    private static final int PICK_CONTACT = 2;
    private static final int PICK_EVENT = 3;
    private TextView tv;
    private ImageView imgView;
    private RecyclerView contactView;
    private TextView eventView;
    private String mContactName;
    private String mEventName;
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
        //tv = this.findViewById(R.id.imgUri);
        imgView = this.findViewById(R.id.imageView);

        contactView = this.findViewById(R.id.contactView);
        adapter = new ContactListAdapter(this);
        contactView.setAdapter(adapter);
        contactView.setLayoutManager(new LinearLayoutManager(this));

        mAnnotateViewModel = new ViewModelProvider(this).get(AnnotateViewModel.class);
        eventView = this.findViewById(R.id.eventPick);

        adapter.getAllContactsUri().observe(this, new  Observer<List<Uri>>() {
            @Override
            public void onChanged(List<Uri> listContact) {
                Log.v("je suis GIGA chiante", listContact.toString());
                mAnnotateViewModel.setListContact(listContact);
            }
        });

        if(mAnnotateViewModel.getPicUri() != null ) {
            imgView.setImageURI(mAnnotateViewModel.getPicUri());
            if(mAnnotateViewModel.getContactsUri() != null) {
                Log.v("je suis chiante", mAnnotateViewModel.getContactsUri().getValue().toString());
                adapter.setListContact(mAnnotateViewModel.getContactsUri().getValue());
            }
            if (mAnnotateViewModel.getEventUri() != null) {
                if (mAnnotateViewModel.getEventUri().getValue() != null && findViewById(R.id.btnDeleteEvent) != null) {
                    eventView.setText(getEventName(mAnnotateViewModel.getEventUri().getValue().getLastPathSegment()));
                    ImageView btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
                    btnDeleteEvent.setVisibility(View.VISIBLE);
                }

            }
        }
        else {
            if (Intent.ACTION_SEND.equals(intent.getAction())) {
                Uri imgUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                //tv.setText(imgUri.toString());
                //Chngement si la valeur de l'event change, on doit actualiser la valeur sur l'UI
                mAnnotateViewModel.getEventUri().observe(this, new  Observer<Uri>() {
                    @Override
                    public void onChanged(Uri uri) {
                        String id = uri.getLastPathSegment();
                        eventView.setText(getEventName(id));
                    }
                });

                mAnnotateViewModel.getContactsUri().observe(this, new  Observer<List<Uri>>() {
                    @Override
                    public void onChanged(List<Uri> contactsUri) {
                        //appeler adapter.setContact(list contacts Uri) -> changer la méthode dans l'adapater et gérer l'insertion dans la liste des contacts
                        // par la suite : gérer la suppression
                    }
                });
                //afficher l'image :
                //pb pour la résolution des images (si trop grosse cette méthode sera problématique : => utiliser glide android
                imgView.setImageURI(imgUri);

            } else {
                Intent pick = new Intent(Intent.ACTION_PICK);
                pick.setType("image/*");
                this.startActivityForResult(pick, PICK_IMG);
            }
        }



    }

    // affiche les données, sans on avait l'interface de base meme avec le pick
    // test sur les request code pour séparé le code en fonction de la provenance du startActivityForResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //le if est utile si on fait retour avant d'avoir choisi une image -> sinon erreur
        // + permet d'étudier que ça concerne bien le choix d'image
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMG && resultCode == RESULT_OK) {
            mAnnotateViewModel.setPicUri(data.getData());
            imgView.setImageURI(mAnnotateViewModel.getPicUri());

            // Permet de définir les données si la photo est déjà annotée dans la BDD :
            mAnnotateViewModel.getPicAnnotation(data.getData()).observe(this, new Observer<PicAnnotation>() {
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

    public void onContactClick(View v) {
        checkContactReadPermission();
        if (readContactAuthorized) {
            Intent pick = new Intent(Intent.ACTION_PICK);
            pick.setData(ContactsContract.Contacts.CONTENT_URI);
            this.startActivityForResult(pick, PICK_CONTACT);
        }
    }

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

    // confirmation pour quitter l'activité sans sauvegarder
    // TODO: à remplacer par un "sauvegarder et quitter" ou juste "quitter"
    public void onCancelClick(View v){
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("Êtes vous sûr de vouloir quitter sans sauvegarder ?")
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

    // Sauvegarde si l'utilisateur appuie sur le bouton de sauvegarde
    public void onSaveClick(View v){
        mAnnotateViewModel.save();
        Toast.makeText(this,"Sauvegardé",Toast.LENGTH_LONG).show();
    }

    // Sauvegarder et quitter
    public void onFinishClick(View v){
        mAnnotateViewModel.save();
        finish();
    }


}