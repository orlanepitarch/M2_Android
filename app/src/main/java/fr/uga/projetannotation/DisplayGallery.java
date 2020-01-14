package fr.uga.projetannotation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


//affiche sous forme de grid view toutes les images annotées ou toutes les images liées au résultat de la recherche :
public class DisplayGallery extends AppCompatActivity {
    static final int REQUEST_PERMISSION_KEY = 1;
    private boolean readGalleryAuthorized = false;
    private DisplayGalleryViewModel mDisplayGalleryViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // lien avec le VM :
        mDisplayGalleryViewModel = new ViewModelProvider(this).get(DisplayGalleryViewModel.class);

        setContentView(R.layout.display_all_pictures);
        final GridView gridView = findViewById(R.id.gridView);
        final MutableLiveData<List<Uri>> listAnnot = new MutableLiveData<>();
        checkReadGalleryPermission();
        final GalleryAdapter galleryAdapter = new GalleryAdapter(this, listAnnot.getValue());
        // Si la liste d'uri de photos est stockée dans le VM, on doit juste envoyer les données à l'adapter qui se chargera d'afficher les images :
        if(mDisplayGalleryViewModel.getPicUris().size() != 0 ) {
            if(readGalleryAuthorized) {
                gridView.setAdapter(galleryAdapter);
                galleryAdapter.setData(mDisplayGalleryViewModel.getPicUris());
            }
        } else {
            //On reçoit les données via une activité, on lit la liste dans l'extra :
            Intent intent = getIntent();
            String test1 = intent.getStringExtra("IMGURIS");
            // Puisque la liste arrive ici via .toString(), on doit enlever les [] identifiant une liste puis séparer selon les " , " afin de reconstituer une liste.
            List<String> imgUriString = (List<String>) Arrays.asList(test1.substring(1, test1.length()-1).split("\\s*(,\\s*)+"));
            List<Uri> imgUri = new ArrayList<>();
            //On doit transformer la liste<String> en list<Uri> afin d'afficher les images. On doit faire attention à la provenance des images via leur URI afin de ne pas avoir
            // d'exception liées à la permission de faire une imageView d'une Uri présente dans la BDD issue de la galerie du téléphone :
            if(imgUriString.size() != 0 && !imgUriString.toString().equals("[]")) {
                for (String uri : imgUriString) {
                    if(Arrays.asList(uri.split("/")).get(3).equals("document")) {
                        imgUri.add(Uri.parse(uri));
                    } else {
                        Toast.makeText(this,"Attention, vos données contiennent des données non compatibles avec la galerie",Toast.LENGTH_LONG).show();
                    }

                }
            }
            if(readGalleryAuthorized) {
                gridView.setAdapter(galleryAdapter);
                mDisplayGalleryViewModel.setPicUris(imgUri);
                galleryAdapter.setData(imgUri);
            }
        }



        // Quand l'utilisateur clique sur un GirdItem, on lance l'activité Annotate avec la donnée sur l'image choisie afin d'fficher son annotation :
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Object o = gridView.getItemAtPosition(position);
                Intent intentAct = new Intent(DisplayGallery.this, Annotate.class);
                intentAct.putExtra("IMGURI", o.toString());
                intentAct.setAction(Intent.ACTION_SEND);
                DisplayGallery.this.startActivity(intentAct);
            }
        });

    }

// Définition de l'adapter affichant dans le gridView les données, sous forme de GridItem (ou ViewHolder) composé d'ImageView :
    final class GalleryAdapter extends BaseAdapter {
        private List<Uri> data;
        private LayoutInflater layoutInflater;
        private Context context;

        public GalleryAdapter(Context aContext, List<Uri> listData) {
            this.context = aContext;
            if (listData == null){
                this.data = new ArrayList<>();
            } else {
                this.data = listData;
            }
            layoutInflater = LayoutInflater.from(aContext);
        }

        void setData(List<Uri> data) {
            if (this.data.size() > 0) {
                data.clear();
            }
            this.data.addAll(data);
            this.notifyDataSetChanged();
        }


        @Override
        public int getCount() {
            if(data == null){
                return 0;
            }else {
                return data.size();
            }
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

                ViewHolder holder;
                if (convertView == null) {
                    convertView = layoutInflater.inflate(R.layout.img, null);
                    holder = new ViewHolder();
                    holder.picUri = (ImageView) convertView.findViewById(R.id.GalleryImgView);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                Uri picUri = this.data.get(position);
                holder.picUri.setImageURI(picUri);

            return convertView;
        }


    }

    static class ViewHolder {
        ImageView picUri;
    }

    public void checkReadGalleryPermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_KEY);
            }
        } else {
            // Permission has already been granted
            readGalleryAuthorized = true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_KEY: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    readGalleryAuthorized = true;
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }



}
