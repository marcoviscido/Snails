package it.unipg.studenti.ai.snails;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private static int RESULT_LOAD_IMG = 1;
    String imgDecodableString;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Button button1= (Button)findViewById(R.id.button);

        Button button2= (Button)findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mioIntent2 = new Intent(MainActivity.this,Photo.class);
                startActivity(mioIntent2);
            }
        });



    }


}
