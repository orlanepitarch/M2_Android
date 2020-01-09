package fr.uga.projetannotation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();


        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                Intent intentAct = new Intent(MainActivity.this, Annotate.class);
                intentAct.putExtra("IMGURI", intent.getParcelableExtra(Intent.EXTRA_STREAM).toString());
                intentAct.setAction(Intent.ACTION_SEND);
                MainActivity.this.startActivity(intentAct);
            }
        }
        setContentView(R.layout.activity_main);
    }

    public void AnnotateClick(View v){
        Intent intent = new Intent(MainActivity.this, Annotate.class);
        MainActivity.this.startActivity(intent);
    }


}
