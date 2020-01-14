package fr.uga.projetannotation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import fr.uga.projetannotation.database.AnnotateRepository;


//Activité principale de notre application, sert de page d'accueil pour que l'utilisateur réalise ce qu'il souhaite faire sur notre application :
public class MainActivity extends AppCompatActivity {
    //lien au répository utile pour la galerie (getAllAnnotation()) + appel aux fonctions de suppression d'annotations :
    public AnnotateRepository mRepository;
    // Entier utile pour éviter de relancer l'activité galerie si elle est lancée lors d'une modification d'annotation :
    private int countCallGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        mRepository = new AnnotateRepository(this.getApplication());

        // Si l'action ouvrant l'application est de type ACTION_SEND, ça veut dire que l'utilisateur a envoyé une image afin de l'annoter,
        // il faut donc la transmettre à l'activité Annotation (via les extras d'Intent) et la démarrer :
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

    //Lance l'activité annotate si l'utilisateur clic sur "Annotate"
    public void AnnotateClick(View v){
        Intent intent = new Intent(MainActivity.this, Annotate.class);
        MainActivity.this.startActivity(intent);
    }

    //Lance l'activité recherche si l'utilisateur clic sur "Search"
    public void SearchClick(View v) {
        Intent intent = new Intent(MainActivity.this, Search.class);
        MainActivity.this.startActivity(intent);
    }

    //Lance l'activité galerie si l'utilisateur clic sur "View" -> nécessite de récupérer toutes les picUri Avant puis d'ajouter la liste dans les extras de l'intent :
    public void ViewClick(View v){
        countCallGallery = 0;
        Toast.makeText(this, "Chargement en cours",Toast.LENGTH_LONG).show();
        mRepository.getAllAnnotation().observe(this, new Observer<List<Uri>>() {
            public void onChanged(@Nullable List<Uri> annotation) {
                Log.v("cc", Integer.toString(countCallGallery));
                //évite le lancement de la galerie à l'infini si on modifie l'annotation en cliquant dessus depuis la galerie : (
                //on lance une seule fois l'activité même si les données change pdt qu'elle est lancées)
                if (annotation != null && countCallGallery == 0) {
                    countCallGallery = countCallGallery+1;
                    Intent intent = new Intent(MainActivity.this, DisplayGallery.class);
                    intent.putExtra("IMGURIS", annotation.toString());
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    MainActivity.this.startActivity(intent);
                }
            }
        });

    }

    //Lance la suppression de toutes les annotations si l'utilisateur confirme sa volonté de tout supprimer :
    public void DelAllClick(View v) {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("Êtes vous sûr de vouloir supprimer toutes les annotations ?")
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

    //Lance la suppression de toutes les annotations non compatibles avec l'affichage des annotations dans la galerie si l'utilisateur confirme sa volonté de supprimer ces annotations:
    public void DelNonConformClick(View v) {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("Êtes vous sûr de vouloir supprimer les annotations non conformes ?")
                .setPositiveButton("Oui", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //on récupére toutes les annotations dans la BDD :
                        mRepository.getAllAnnotation().observe(MainActivity.this, new Observer<List<Uri>>() {
                            public void onChanged(@Nullable List<Uri> annotation) {
                                if (annotation != null) {
                                    //on filtre pour savoir si elles viennent des documents ou de la galerie :
                                    for (Uri uri : annotation) {
                                        if(!Arrays.asList(uri.toString().split("/")).get(3).equals("document")) {
                                            //si l'image annotée ne vient pas des documents du téléphone, on l'a supprime :
                                            mRepository.deletePicAnnot(uri);
                                        }
                                    }
                                }
                            }
                        });
                    }

                })
                .setNegativeButton("Annuler", null)
                .show();
    }

}
