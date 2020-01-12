package fr.uga.projetannotation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

public class Search extends AppCompatActivity {
    private SearchViewModel mSearchViewModel;
    private RecyclerView contactView;
    private TextView eventView;
    private TextView displayDateDepart;
    private TextView displayDateFin;
    private ContactListAdapter adapter;
    private static final int PICK_CONTACT = 2;
    private static final int PICK_EVENT = 3;
    private final static int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private boolean readContactAuthorized = false;
    private DatePickerDialog.OnDateSetListener mDateSetListenerDepart;
    private DatePickerDialog.OnDateSetListener mDateSetListenerFin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        mSearchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        contactView = this.findViewById(R.id.contactView);
        adapter = new ContactListAdapter(this);
        contactView.setAdapter(adapter);
        contactView.setLayoutManager(new LinearLayoutManager(this));
        eventView = this.findViewById(R.id.eventPick);
        displayDateDepart = this.findViewById(R.id.displayDateDepart);
        displayDateFin = this.findViewById(R.id.displayDateFin);
        mDateSetListenerDepart = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mSearchViewModel.setDateDepart(year, month, dayOfMonth);
                month = month+1;
                displayDateDepart.setText(dayOfMonth +"/"+month+"/"+year);
                ImageView btnDeleteDateDepart = findViewById(R.id.btnDeleteDateDepart);
                btnDeleteDateDepart.setVisibility(View.VISIBLE);
            }
        };

        mDateSetListenerFin = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mSearchViewModel.setDateFin(year,month,dayOfMonth);
                month = month+1;
                displayDateFin.setText(dayOfMonth +"/"+month+"/"+year);
                ImageView btnDeleteDateFin = findViewById(R.id.btnDeleteDateFin);
                btnDeleteDateFin.setVisibility(View.VISIBLE);
            }
        };



        adapter.getAllContactsUri().observe(this, new  Observer<List<Uri>>() {
            @Override
            public void onChanged(List<Uri> listContact) {
                mSearchViewModel.setListContact(listContact);
            }
        });

        //si des données sont stockées dans le VM :
        if(mSearchViewModel.getContactsUri().getValue() != null) {
            adapter.setListContact(mSearchViewModel.getContactsUri().getValue());
        }
        if (mSearchViewModel.getEventUri().getValue() != null) {
            if (mSearchViewModel.getEventUri().getValue() != null && findViewById(R.id.btnDeleteEvent) != null) {
                eventView.setText(getEventName(mSearchViewModel.getEventUri().getValue().getLastPathSegment()));
                ImageView btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
                btnDeleteEvent.setVisibility(View.VISIBLE);
            }
        }
        if(mSearchViewModel.getDisplayDateDepart() != "") {
            displayDateDepart.setText(mSearchViewModel.getDisplayDateDepart());
            ImageView btnDeleteDateDepart = findViewById(R.id.btnDeleteDateDepart);
            btnDeleteDateDepart.setVisibility(View.VISIBLE);
        }
        if(mSearchViewModel.getDisplayDateFin() != "") {
            displayDateDepart.setText(mSearchViewModel.getDisplayDateFin());
            ImageView btnDeleteDateFin = findViewById(R.id.btnDeleteDateFin);
            btnDeleteDateFin.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //le if est utile si on fait retour avant d'avoir choisi une image -> sinon erreur
        // + permet d'étudier que ça concerne bien le choix d'image
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            mSearchViewModel.setContactUri(uri);
            adapter.setContact(uri);
        }

        if (requestCode == PICK_EVENT && resultCode == RESULT_OK) {
            eventView.setText(getEventName(data.getData().getLastPathSegment()));
            mSearchViewModel.setEventUri(data.getData());
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
        Intent event = new Intent(Search.this, ChooseEvent.class);
        Search.this.startActivityForResult(event, PICK_EVENT);
    }

    public void onDepartDateClick(View v) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = new DatePickerDialog(this, R.style.Theme_AppCompat_Dialog_Alert, mDateSetListenerDepart, year, month, day);
        dialog.getWindow().setBackgroundDrawableResource(R.color.design_default_color_primary);
        dialog.show();
    }

    public void onDateFinClick(View v) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = new DatePickerDialog(this, R.style.Theme_AppCompat_Dialog_Alert, mDateSetListenerFin, year, month, day);
        dialog.getWindow().setBackgroundDrawableResource(R.color.design_default_color_primary);
        dialog.show();
    }

    public void onDeleteDateFinClick(View v) {
        mSearchViewModel.setDateFin(0,20,0);
        displayDateFin.setText("");
        ImageView btnDeleteDateFin = findViewById(R.id.btnDeleteDateFin);
        btnDeleteDateFin.setVisibility(View.INVISIBLE);
    }
    public void onDeleteDateDepartClick(View v) {
        mSearchViewModel.setDateDepart(0,20,0);
        displayDateDepart.setText("");
        ImageView btnDeleteDateDepart = findViewById(R.id.btnDeleteDateDepart);
        btnDeleteDateDepart.setVisibility(View.INVISIBLE);
    }


    public void onDeleteEventClick(View v) {
        ImageView btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
        btnDeleteEvent.setVisibility(View.INVISIBLE);
        mSearchViewModel.deleteEvent();
        eventView.setText("");
    }

    public void onSearchClick(View v) {
        // test si la recherche est possible (au moins une condition de recherche valide) :
        if((mSearchViewModel.getContactsUri().getValue() != null && mSearchViewModel.getContactsUri().getValue().size() == 0 && mSearchViewModel.getEventUri().getValue() != null)
        || (mSearchViewModel.getEventUri().getValue() == null && mSearchViewModel.getContactsUri().getValue() != null && mSearchViewModel.getContactsUri().getValue().size() != 0)
        || (mSearchViewModel.getTpsFin() != -1 && mSearchViewModel.getTpsDepart() !=-1 && mSearchViewModel.getTpsDepart() < mSearchViewModel.getTpsFin() && ContextCompat.checkSelfPermission(this.getApplication(), Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED)
        || (mSearchViewModel.getEventUri().getValue() != null && mSearchViewModel.getContactsUri().getValue() != null && mSearchViewModel.getContactsUri().getValue().size() != 0)
        ) {
            Toast.makeText(this, "Recherche en cours",Toast.LENGTH_LONG).show();
            mSearchViewModel.search().observe(this, new Observer<List<Uri>>() {
                @Override
                public void onChanged(List<Uri> picUri) {
                    if(picUri.size() == 0) {
                        Toast.makeText(Search.this, "Pas de résultat !",Toast.LENGTH_LONG).show();
                    } else {
                        Intent intent = new Intent(Search.this, DisplayGallery.class);
                        intent.putExtra("IMGURIS", picUri.toString());
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        Search.this.startActivity(intent);
                    }
                }
            });
        } else {
            Toast.makeText(this, "Veuillez remplir une condition de recherche",Toast.LENGTH_LONG).show();
        }
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
}
