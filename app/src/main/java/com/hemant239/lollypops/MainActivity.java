package com.hemant239.lollypops;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hemant239.lollypops.adapters.HeadingAdapter;
import com.hemant239.lollypops.objects.Heading;
import com.hemant239.lollypops.objects.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    static User curUser,
                boss;

    FloatingActionButton addNewHeading;

    RecyclerView headingList;
    public static RecyclerView.Adapter<HeadingAdapter.ViewHolder> headingListAdapter;
    RecyclerView.LayoutManager headingListLayoutManager;


    ArrayList<Heading> headings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);




        getUsers();

        initializeViews();
        initializeRecyclerViews();

        getHeadings();

        addNewHeading.setOnClickListener(v -> createDialogueBox());




    }

    private void createDialogueBox() {

        final EditText editTextName=new EditText(this);
        editTextName.setHint("enter name here");
        final EditText editTextDes=new EditText(this);
        editTextDes.setHint("enter desc here");

        LinearLayout linearLayout=new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(editTextName,0);
        linearLayout.addView(editTextDes,1);

        final AlertDialog alertDialog=new AlertDialog.Builder(this).setTitle("NEW HEADINGS").create();
        alertDialog.setView(linearLayout,5,5,5,5);


        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "CREATE", (dialog, which) -> {
            String name=editTextName.getText().toString();
            String des=editTextDes.getText().toString();
            if(name.equals("")){
                Toast.makeText(getApplicationContext(),"Name cannot be empty",Toast.LENGTH_SHORT).show();
            }
            else{
                final Date date = Calendar.getInstance().getTime();
                SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("EEE, MMM dd, yyyy");
                String dateTemp = simpleDateFormatDate.format(date);

                DatabaseReference headingDB=FirebaseDatabase.getInstance().getReference().child("Headings");
                String headingID=headingDB.push().getKey();

                Heading heading=new Heading(headingID,name,curUser.getName(),dateTemp,String.valueOf(date.getTime()),des,"");

                assert headingID != null;
                headingDB.child(headingID).setValue(heading);
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", (dialog, which) -> alertDialog.dismiss());

        alertDialog.show();
    }

    private void getHeadings() {
        FirebaseDatabase.getInstance().getReference().child("Headings").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists() && snapshot.getValue()!=null){
                    final Heading heading=snapshot.getValue(Heading.class);
                    headings.add(heading);
                    headingListAdapter.notifyItemInserted(headings.size()-1);


                    FirebaseDatabase.getInstance().getReference().child("Headings").child(Objects.requireNonNull(snapshot.getKey())).child("imageUri").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot imageSnapshot) {
                            if(imageSnapshot.exists()){
                                assert heading != null;
                                Heading tempHeading=new Heading(heading.getId());
                                int indexOfItem=headings.indexOf(tempHeading);
                                if(indexOfItem>-1){
                                    headings.get(indexOfItem).setImageUri(Objects.requireNonNull(imageSnapshot.getValue()).toString());
                                    headingListAdapter.notifyItemChanged(indexOfItem);
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeRecyclerViews() {
        headings=new ArrayList<>();
        headingList=findViewById(R.id.headingsRecyclerView);
        headingList.setHasFixedSize(false);
        headingList.setNestedScrollingEnabled(false);

        headingList.addItemDecoration(new DividerItemDecoration(headingList.getContext(),DividerItemDecoration.VERTICAL));


        headingListAdapter = new HeadingAdapter(this,headings);
        headingListAdapter.setHasStableIds(true);
        headingList.setAdapter(headingListAdapter);

        headingListLayoutManager=new LinearLayoutManager(getApplicationContext(),RecyclerView.VERTICAL,false);
        headingList.setLayoutManager(headingListLayoutManager);

    }

    private void initializeViews() {
        addNewHeading=findViewById(R.id.fabMainActivity);
    }


    private void getUsers() {
        DatabaseReference mUserDb=FirebaseDatabase.getInstance().getReference().child("Users");
        final String curUserKey= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        mUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot childSnapshot:snapshot.getChildren()){

                        if(curUserKey.equals(childSnapshot.getKey())){

                            curUser= childSnapshot.getValue(User.class);
                        }
                        else{
                            boss= childSnapshot.getValue(User.class);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void sortHeadingsViaDate() {
        headings.sort((Heading heading1, Heading heading2) -> heading1.getTimestamp().compareToIgnoreCase(heading2.getTimestamp()));
        headingListAdapter.notifyDataSetChanged();
    }
    private void sortHeadingsViaNames() {
        headings.sort((Heading heading1, Heading heading2) -> heading1.getName().compareToIgnoreCase(heading2.getName()));
        headingListAdapter.notifyDataSetChanged();
    }
    private void sortAscendingDescending() {
        Collections.reverse(headings);
        headingListAdapter.notifyDataSetChanged();
    }

    private void viewBossProfile() {
        Intent intent = new Intent(this, UserDetailsActivity.class);
        intent.putExtra("userObject", boss);
        startActivity(intent);
    }
    private void viewYourProfile() {
        Intent intent = new Intent(this, UserDetailsActivity.class);
        intent.putExtra("userObject", curUser);
        startActivity(intent);
    }
    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent=new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutMenu:
                logOut();
                break;

            case R.id.yourProfile:
                viewYourProfile();
                break;

            case R.id.bossProfile:
                viewBossProfile();
                break;

            case R.id.headingSortName:
                sortHeadingsViaNames();
                break;
            case R.id.headingSortDate:
                sortHeadingsViaDate();
                break;
            case R.id.headingAscendingDescending:
                sortAscendingDescending();
                break;

            default:
                break;

        }
        return true;
    }




}