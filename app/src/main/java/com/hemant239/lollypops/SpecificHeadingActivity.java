package com.hemant239.lollypops;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hemant239.lollypops.adapters.ItemAdapter;
import com.hemant239.lollypops.objects.Item;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

public class SpecificHeadingActivity extends AppCompatActivity {


    FloatingActionButton addNewItem;

    RecyclerView itemList;
    public static RecyclerView.Adapter<ItemAdapter.ViewHolder> itemListAdapter;
    RecyclerView.LayoutManager itemListLayoutManager;

    ArrayList<Item> items;

    boolean isWishList;

    String headingKey;

    String listName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_heading);

        isWishList=getIntent().getBooleanExtra("isWishList",false);
        headingKey=getIntent().getStringExtra("headingKey");
        String headingName=getIntent().getStringExtra("headingName");
        String headingImage=getIntent().getStringExtra("headingImage");

        initializeViews();
        listName="Completed";
        addNewItem.setVisibility(View.GONE);

        if(isWishList){
            addNewItem.setVisibility(View.VISIBLE);
            listName="WishList";
        }


        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(R.layout.specific_heading_toolbar);
        View view=getSupportActionBar().getCustomView();



        ImageView mHeadingImage=view.findViewById(R.id.toolbarHeadingImage);
        TextView mHeadingName=view.findViewById(R.id.toolbarHeadingName);
        TextView mHeadingList=view.findViewById(R.id.toolbarHeadingListName);


        mHeadingName.setText(headingName);
        mHeadingList.setText(listName);
        if(headingImage!=null && !headingImage.equals("")) {
            mHeadingImage.setClipToOutline(true);
            Glide.with(getApplicationContext()).load(Uri.parse(headingImage)).into(mHeadingImage);
        }
        mHeadingImage.setOnClickListener(v -> {
            String path="Headings/"+headingKey+"/imageUri";
            Intent intent =new Intent(getApplicationContext(), ImageViewActivity.class);
            intent.putExtra("URI",headingImage);
            intent.putExtra("id",headingKey);
            intent.putExtra("path",path);
            startActivity(intent);

        });








        initializeRecyclerViews();
        

        final String finalListName = listName;
        addNewItem.setOnClickListener(v -> createDialogueBox(finalListName));

        getItems(listName);


    }

    private void createDialogueBox(final String listName) {
        final EditText editTextName=new EditText(this);
        editTextName.setHint("enter name here");
        final EditText editTextDes=new EditText(this);
        editTextDes.setHint("enter desc here");

        LinearLayout linearLayout=new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(editTextName,0);
        linearLayout.addView(editTextDes,1);

        final AlertDialog alertDialog=new AlertDialog.Builder(this).setTitle("NEW ELEMENT").create();
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

                DatabaseReference itemDB=FirebaseDatabase.getInstance().getReference().child("Headings").child(headingKey).child(listName);
                String itemId=itemDB.push().getKey();

                Item item=new Item(itemId,name,MainActivity.curUser.getName(),dateTemp,"",des,"",String.valueOf(date.getTime()),"");
                assert itemId != null;
                itemDB.child(itemId).setValue(item);


            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", (dialog, which) -> alertDialog.dismiss());

        alertDialog.show();
    }

    private void getItems(final String listName) {
        FirebaseDatabase.getInstance().getReference().child("Headings").child(headingKey).child(listName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists() && snapshot.getValue()!=null){
                    final Item item=snapshot.getValue(Item.class);
                    items.add(item);
                    itemListAdapter.notifyItemInserted(items.size()-1);
                    FirebaseDatabase.getInstance().getReference().child("Headings").child(headingKey)
                            .child(listName).child(Objects.requireNonNull(snapshot.getKey())).child("imageUri").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot imageSnapshot) {
                            if(imageSnapshot.exists()){
                                assert item != null;
                                Item tempItem=new Item(item.getId());
                                int indexOfItem=items.indexOf(tempItem);
                                if(indexOfItem>-1){
                                    items.get(indexOfItem).setImageUri(Objects.requireNonNull(imageSnapshot.getValue()).toString());
                                    itemListAdapter.notifyItemChanged(indexOfItem);
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
        items=new ArrayList<>();
        itemList=findViewById(R.id.itemsRecyclerView);
        itemList.setHasFixedSize(false);
        itemList.setNestedScrollingEnabled(false);

        itemList.addItemDecoration(new DividerItemDecoration(itemList.getContext(),DividerItemDecoration.VERTICAL));


        itemListAdapter = new ItemAdapter(this,items,isWishList,headingKey);
        itemListAdapter.setHasStableIds(true);
        itemList.setAdapter(itemListAdapter);

        itemListLayoutManager=new LinearLayoutManager(getApplicationContext(),RecyclerView.VERTICAL,false);
        itemList.setLayoutManager(itemListLayoutManager);
    }
    private void initializeViews() {
        addNewItem=findViewById(R.id.fabSpecificActivity);
    }



    private void sortAscendingDescending() {
        Collections.reverse(items);
        itemListAdapter.notifyDataSetChanged();
    }
    private void sortItemsViaCompletionDate() {
        items.sort((Item item1, Item item2) -> item1.getTimeCompleted().compareToIgnoreCase(item2.getTimeCompleted()));
        itemListAdapter.notifyDataSetChanged();
    }
    private void sortItemsViaCreatedDate() {
        items.sort((Item item1, Item item2) -> item1.getTimeAdded().compareToIgnoreCase(item2.getTimeAdded()));
        itemListAdapter.notifyDataSetChanged();
    }
    private void sortItemsViaNames() {
        items.sort((Item item1, Item item2) -> item1.getName().compareToIgnoreCase(item2.getName()));
        itemListAdapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.specific_heading_menu,menu);

        if(listName.equals("WishList")){
            menu.findItem(R.id.itemSortCompleteDate).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.itemSortName:
                sortItemsViaNames();
                break;
            case R.id.itemSortCreateDate:
                sortItemsViaCreatedDate();
                break;
            case R.id.itemSortCompleteDate:
                sortItemsViaCompletionDate();
                break;

            case R.id.itemAscendingDescending:
                sortAscendingDescending();
                break;




            default:
                break;
        }
        return true;
    }



}