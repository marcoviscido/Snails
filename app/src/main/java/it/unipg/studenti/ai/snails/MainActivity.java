package it.unipg.studenti.ai.snails;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.UUID;

import it.unipg.studenti.ai.snails.utils.Helpers;

public class MainActivity extends AppCompatActivity {

    private static int RESULT_LOAD_IMG = 1;
    String imageAbsPath;
    SubsamplingScaleImageView imgView;
    Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Helpers.initParams(getApplicationContext());

        imgView = (SubsamplingScaleImageView) findViewById(R.id.imgView);

        btnStart = (Button)findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ElabActivity.class);
                intent.putExtra("imageAbsPath", imageAbsPath);
                //intent.putExtra("filename", UUID.randomUUID().toString() );
                startActivity(intent);
            }
        });
        btnStart.setVisibility(View.INVISIBLE);

        Button btnChoosePicture = (Button)findViewById(R.id.btnChoosePicture);
        btnChoosePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Start the Intent
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                if(cursor!=null) {
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageAbsPath = cursor.getString(columnIndex);
                    cursor.close();
                }
                // Set the Image in ImageView after decoding the String
                imgView.setImage(ImageSource.bitmap(BitmapFactory.decodeFile(imageAbsPath)));
                btnStart.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }

    }


}
