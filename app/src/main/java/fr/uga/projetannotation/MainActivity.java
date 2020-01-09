package fr.uga.projetannotation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void AnnotateClick(View v){
        Intent intent = new Intent(MainActivity.this, Annotate.class);
        MainActivity.this.startActivity(intent);
    }
}
