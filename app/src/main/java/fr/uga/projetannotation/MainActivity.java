package fr.uga.projetannotation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import java.util.List;

import fr.uga.projetannotation.database.AnnotateRepository;
import fr.uga.projetannotation.model.ContactAnnotation;

public class MainActivity extends AppCompatActivity {
    public AnnotateRepository mRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        mRepository = new AnnotateRepository(this.getApplication());


        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                Intent intentAct = new Intent(MainActivity.this, Annotate.class);
                intentAct.putExtra("IMGURI", intent.getParcelableExtra(Intent.EXTRA_STREAM).toString());
                intentAct.setAction(Intent.ACTION_SEND);
                intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                MainActivity.this.startActivity(intentAct);
            }
        }
        setContentView(R.layout.activity_main);
    }

    public void AnnotateClick(View v){
        Intent intent = new Intent(MainActivity.this, Annotate.class);
        MainActivity.this.startActivity(intent);
    }

    public void ViewClick(View v){
        Intent intent = new Intent(MainActivity.this, DisplayGallery.class);

        mRepository.getAllAnnotation().observe(this, new Observer<List<Uri>>() {
            public void onChanged(@Nullable List<Uri> annotation) {
                if (annotation != null) {
                    intent.putExtra("IMGURIS", annotation.toString());
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    MainActivity.this.startActivity(intent);
                }
            }
        });

    }

    public void DelAllClick(View v) {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("Êtes vous sûr de vouloir supprimer toutes les annotations ?")

                //si on ne veut pas sauvegarder les changements mais que l'utilisateur avait supprimé des contact ou un event, on doit les réinsérer dans la BDD :
                .setPositiveButton("Oui", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRepository.deleteAll();
                    }

                })
                .setNegativeButton("Annuler", null)
                .show();

    }

}
