package com.hemant239.lollypops.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.hemant239.lollypops.ImageViewActivity;
import com.hemant239.lollypops.MainActivity;
import com.hemant239.lollypops.R;
import com.hemant239.lollypops.SpecificHeadingActivity;
import com.hemant239.lollypops.objects.Item;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {


    ArrayList<Item> items;
    Context context;
    Boolean isWishList;

    String parentKey;
    String parentList;

    AlertDialog alertDialog;

    public ItemAdapter(Context context,ArrayList<Item> items,Boolean isWishList,String parentKey){
        this.context=context;
        this.items=items;
        this.isWishList=isWishList;
        this.parentKey=parentKey;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_specific_item,null,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Item item=items.get(position);

        holder.name.setText(item.getName());
        holder.creator.setText(item.getCreatedBy());
        holder.createdOn.setText(item.getCreatedOn());
        holder.description.setText("Description: "+item.getDescription());


        if(!item.getImageUri().equals("")){
            holder.imageUri.setClipToOutline(true);
            Glide.with(context).load(Uri.parse(item.getImageUri())).into(holder.imageUri);
        }
        else{
            holder.imageUri.setImageResource(R.drawable.ic_baseline_person_24);
//            holder.imageUri.setVisibility(View.GONE);
//            holder.itemImageLayout.setVisibility(View.GONE);

        }

        if(isWishList){
            holder.wishList.setVisibility(View.GONE);
            holder.finishedLinearLayout.setVisibility(View.GONE);
            parentList="WishList";
        }
        else{
            holder.completed.setVisibility(View.GONE);
            holder.completedOn.setText(item.getCompletedOn());
            parentList="Completed";
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.linearLayout.getVisibility()==View.GONE){
                    holder.linearLayout.setVisibility(View.VISIBLE);
                    holder.description.setVisibility(View.VISIBLE);
                } else{
                    holder.linearLayout.setVisibility(View.GONE);
                    holder.description.setVisibility(View.GONE);
                }
            }
        });



        holder.wishList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { fromCompletedToWishList(item,position);
            }
        });

        holder.completed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { fromWishListToCompleted(item,position);
            }
        });


        holder.imageUri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path="Headings/"+parentKey+"/"+parentList+"/"+item.getId()+"/imageUri";
                Intent intent =new Intent(context, ImageViewActivity.class);
                intent.putExtra("URI",item.getImageUri());
                intent.putExtra("path",path);
                intent.putExtra("id",item.getId());
                context.startActivity(intent);
            }
        });



        holder.menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu=new PopupMenu(context,holder.menuButton);
                popupMenu.inflate(R.menu.item_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch(menuItem.getItemId()){

                            case R.id.deleteMenu:
                                createDialogForDelete(item,position);
                                break;

                            case R.id.changeDetailsMenu:
                                createDialogForChangeDetails(item,position);
                                break;

                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });





    }

    private void createDialogForChangeDetails(final Item item, final int position) {
        final EditText editTextName=new EditText(context);
        editTextName.setText(item.getName());
        final EditText editTextDes=new EditText(context);
        editTextDes.setText(item.getDescription());
        editTextDes.setHint("enter desc here");

        LinearLayout linearLayout=new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(editTextName,0);
        linearLayout.addView(editTextDes,1);

        final AlertDialog alertDialog=new AlertDialog.Builder(context).setTitle("NEW DETAILS").create();
        alertDialog.setView(linearLayout,5,5,5,5);


        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name=editTextName.getText().toString();
                String des=editTextDes.getText().toString();
                if(name.equals("")){
                    Toast.makeText(context,"Name cannot be empty",Toast.LENGTH_SHORT).show();
                }
                else{
                    changeNameInDatabase(item.getId(),position,name,des);
                }
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }

    private void createDialogForDelete(final Item item, final int position) {
        alertDialog=new AlertDialog.Builder(context)
                .setTitle("DELETE")
                .setMessage("Are you sure you want to delete it\n you will loose all the data")
                .create();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItemFromDatabase(item.getId(),position);
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();

    }


    private void fromWishListToCompleted(Item item, int position) {
        String itemId=item.getId();
        DatabaseReference itemDB=FirebaseDatabase.getInstance().getReference().child("Headings").child(parentKey);
        final Date date = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("EEE, MMM dd, yyyy");
        String dateTemp = simpleDateFormatDate.format(date);

        item.setCompletedOn(dateTemp);
        item.setTimeCompleted(String.valueOf(date.getTime()));

        itemDB.child("Completed").child(itemId).setValue(item);
        itemDB.child("WishList").child(itemId).removeValue();
        items.remove(position);

        if(position==0){
            SpecificHeadingActivity.itemListAdapter.notifyDataSetChanged();
        }
        else{
            SpecificHeadingActivity.itemListAdapter.notifyItemRemoved(position);
        }
    }

    private void fromCompletedToWishList(Item item, int position) {
        String itemId=item.getId();
        DatabaseReference itemDB=FirebaseDatabase.getInstance().getReference().child("Headings").child(parentKey);
        itemDB.child("WishList").child(itemId).setValue(item);
        itemDB.child("Completed").child(itemId).removeValue();
        items.remove(position);

        if(position==0){
            SpecificHeadingActivity.itemListAdapter.notifyDataSetChanged();
        }
        else{
            SpecificHeadingActivity.itemListAdapter.notifyItemRemoved(position);
        }
    }

    private void changeNameInDatabase(String itemId, int position, String name, String des) {
        FirebaseDatabase.getInstance().getReference().child("Headings").child(parentKey).child(parentList).child(itemId).child("name").setValue(name);
        FirebaseDatabase.getInstance().getReference().child("Headings").child(parentKey).child(parentList).child(itemId).child("description").setValue(des);
        items.get(position).setName(name);
        items.get(position).setDescription(des);
        SpecificHeadingActivity.itemListAdapter.notifyItemChanged(position);
    }

    private void deleteItemFromDatabase(String itemId, int position) {
        FirebaseDatabase.getInstance().getReference().child("Headings").child(parentKey).child(parentList).child(itemId).removeValue();
        items.remove(position);

        if(position==0){
            SpecificHeadingActivity.itemListAdapter.notifyDataSetChanged();
        }
        else{
            SpecificHeadingActivity.itemListAdapter.notifyItemRemoved(position);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name,
                creator,
                createdOn,
                completedOn,
                description;

        ImageView imageUri;

        Button  wishList,
                completed,
                menuButton;

        LinearLayout linearLayout,
                        finishedLinearLayout,
                        itemImageLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name=itemView.findViewById(R.id.itemName);
            creator=itemView.findViewById(R.id.itemCreator);
            createdOn=itemView.findViewById(R.id.itemCreatedDate);
            completedOn=itemView.findViewById(R.id.itemCompletedDate);
            description=itemView.findViewById(R.id.itemDesc);
            imageUri=itemView.findViewById(R.id.itemImage);

            wishList=itemView.findViewById(R.id.itemToWishList);
            completed=itemView.findViewById(R.id.itemToCompleted);
            menuButton=itemView.findViewById(R.id.itemMenuButton);

            linearLayout=itemView.findViewById(R.id.itemLinearLayout);
            finishedLinearLayout=itemView.findViewById(R.id.finishedOnLayout);
            itemImageLayout=itemView.findViewById(R.id.itemImageLayout);

        }
    }
}
