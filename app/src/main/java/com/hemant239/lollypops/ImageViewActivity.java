package com.hemant239.lollypops;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hemant239.lollypops.objects.Item;

import java.util.Objects;

public class ImageViewActivity extends AppCompatActivity {

    ImageView imageView;
    Button changePhoto;

    String path,id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        initializeViews();

        String uri = getIntent().getStringExtra("URI");
        path=getIntent().getStringExtra("path");
        id=getIntent().getStringExtra("id");
        assert uri != null;
        if (!uri.equals("")) {
            Glide.with(this).load(Uri.parse(uri)).into(imageView);
        }


        if(path!=null &&!path.equals("")){
            changePhoto.setVisibility(View.VISIBLE);
            changePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updatePhotoInDatabase();
                }
            });
        }
    }


    int ADD_MEDIA_CODE=1;
    private void updatePhotoInDatabase() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select an image"),ADD_MEDIA_CODE);
    }


    private void initializeViews() {
        imageView = findViewById(R.id.imageView);
        changePhoto=findViewById(R.id.changePhoto);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                Toast.makeText(getApplicationContext(), "choose a valid button", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK && requestCode==ADD_MEDIA_CODE){

            final StorageReference profileStorage=FirebaseStorage.getInstance().getReference().child("Images").child(id);

            assert data != null;
            final UploadTask uploadTask=profileStorage.putFile(Objects.requireNonNull(data.getData()));
            Intent intent =new Intent(getApplicationContext(),LoadingActivity.class);
            intent.putExtra("message", "Your Image is being Uploaded \n please wait");
            intent.putExtra("isNewUser", true);
            startActivity(intent);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    profileStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            FirebaseDatabase.getInstance().getReference().child(path).setValue(uri.toString());
                            Glide.with(getApplicationContext()).load(uri).into(imageView);
                            ((LoadingActivity) LoadingActivity.context).finish();
                        }
                    });

                }
            });

        }
    }
}