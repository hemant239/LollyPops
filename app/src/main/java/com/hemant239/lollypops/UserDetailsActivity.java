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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hemant239.lollypops.objects.User;

import java.util.Objects;

public class UserDetailsActivity extends AppCompatActivity {


    TextView    mUserName,
                mUserPhone,
                mUserStatus;


    ImageView mUserImage;

    EditText    mUserEditStatus,
                mUserEditName;
    Button  mChangePhoto,
            mUpdateDetails;
    String  userId,
            userName,
            userPhone,
            userStatus,
            userImage;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        user = (User) getIntent().getSerializableExtra("userObject");
        assert user != null;
        userId = user.getUserId();
        userName = user.getName();
        userPhone = user.getPhoneNumber();
        userStatus = user.getStatus();
        userImage = user.getProfileImageUri();

        initializeViews();

        mUserName.setText(userName);
        mUserPhone.setText(userPhone);
        mUserStatus.setText(userStatus);

        if(!userImage.equals("")) {
            mUserImage.setClipToOutline(true);
            Glide.with(this).load(Uri.parse(userImage)).into(mUserImage);
            mUserImage.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), ImageViewActivity.class);
                intent.putExtra("URI", userImage);
                startActivity(intent);
            });
        }

        String curUserKey = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        if (curUserKey.equals(userId)) {
            mUserEditName.setText(userName);
            mUserEditStatus.setText(userStatus);
            mUserName.setVisibility(View.GONE);
            mUserStatus.setVisibility(View.GONE);
            mUserEditStatus.setVisibility(View.VISIBLE);
            mUserEditName.setVisibility(View.VISIBLE);
            mUpdateDetails.setVisibility(View.VISIBLE);
            mChangePhoto.setVisibility(View.VISIBLE);

            mChangePhoto.setOnClickListener(v -> openGallery());

            mUpdateDetails.setOnClickListener(v -> updateDetails());

        }
    }

    private void updateDetails() {
        String status = mUserEditStatus.getText().toString();
        String name  =mUserEditName.getText().toString();

        if (name.equals("") || status.equals("")) {
            Toast.makeText(getApplicationContext(), " Name and Status cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if (status.equals(userStatus) && name.equals(userName)) {
            Toast.makeText(getApplicationContext(), "Details updated", Toast.LENGTH_SHORT).show();
        }
        else {
            MainActivity.curUser.setStatus(status);
            MainActivity.curUser.setName(name);
            FirebaseDatabase.getInstance().getReference().child("Users/" + userId).setValue(MainActivity.curUser);
            Toast.makeText(getApplicationContext(), "Details updated", Toast.LENGTH_SHORT).show();
        }
    }

    final int CHANGE_PROFILE_PHOTO_CODE = 1;
    final int CANCEL_UPLOAD_TASK = 3;
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select an image"), CHANGE_PROFILE_PHOTO_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CHANGE_PROFILE_PHOTO_CODE:
                    final StorageReference profileStorage = FirebaseStorage.getInstance().getReference().child("ProfilePhotos").child(userId);
                    final DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

                    assert data != null;
                    final UploadTask uploadTask = profileStorage.putFile(Objects.requireNonNull(data.getData()));
                    Intent intent = new Intent(getApplicationContext(), LoadingActivity.class);
                    intent.putExtra("message", "Your Image is being uploaded \\n please wait");
                    intent.putExtra("isNewUser", false);
                    startActivityForResult(intent, CANCEL_UPLOAD_TASK);
                    uploadTask.addOnSuccessListener(taskSnapshot -> profileStorage.getDownloadUrl().addOnSuccessListener(uri -> {
                        mUserDb.child("profileImageUri").setValue(uri.toString());
                        MainActivity.curUser.setProfileImageUri(uri.toString());
                        Glide.with(getApplicationContext()).load(uri).into(mUserImage);
                        ((LoadingActivity) LoadingActivity.context).finish();
                    }));

                    break;

                case 100:
                    break;

                default:
                    Toast.makeText(getApplicationContext(), "something went wrong, please try again later", Toast.LENGTH_SHORT).show();

            }

        }
    }

    private void initializeViews() {
        mUserName = findViewById(R.id.userDetailName);
        mUserPhone = findViewById(R.id.userDetailPhone);
        mUserStatus = findViewById(R.id.userDetailStatus);
        mUserImage = findViewById(R.id.userDetailProfileImage);

        mUserEditStatus = findViewById(R.id.userDetailEditStatus);
        mUserEditName = findViewById(R.id.userDetailEditName);

        mChangePhoto = findViewById(R.id.changeProfilePicture);
        mUpdateDetails = findViewById(R.id.updateUserDetails);
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
}