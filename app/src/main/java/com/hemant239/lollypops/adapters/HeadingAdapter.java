package com.hemant239.lollypops.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hemant239.lollypops.ImageViewActivity;
import com.hemant239.lollypops.MainActivity;
import com.hemant239.lollypops.R;
import com.hemant239.lollypops.SpecificHeadingActivity;
import com.hemant239.lollypops.objects.Heading;
import com.hemant239.lollypops.objects.Item;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class HeadingAdapter extends RecyclerView.Adapter<HeadingAdapter.ViewHolder>{

    ArrayList<Heading> headings;
    Context context;

    AlertDialog alertDialog;

    public HeadingAdapter(Context context, ArrayList<Heading> headings){
        this.context=context;
        this.headings=headings;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_heading,null,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Heading heading=headings.get(position);


        holder.name.setText(heading.getName());
        holder.creator.setText(heading.getCreator());
        holder.date.setText(heading.getDate());
        holder.description.setText("Description: "+heading.getDescription());

        if(!heading.getImageUri().equals("")){
            holder.image.setClipToOutline(true);
            Glide.with(context).load(Uri.parse(heading.getImageUri())).into(holder.image);
        }
        else{
            holder.image.setImageResource(R.drawable.ic_baseline_person_24);
//            holder.image.setVisibility(View.GONE);
//            holder.headingImageLayout.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.linearLayout.getVisibility()==View.GONE){
                    holder.linearLayout.setVisibility(View.VISIBLE);
                    holder.description.setVisibility(View.VISIBLE);
                }
                else{
                    holder.linearLayout.setVisibility(View.GONE);
                    holder.description.setVisibility(View.GONE);
                }
            }
        });


        holder.wishList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, SpecificHeadingActivity.class);
                intent.putExtra("isWishList",true);
                intent.putExtra("headingName",heading.getName());
                intent.putExtra("headingImage",heading.getImageUri());
                intent.putExtra("headingKey",heading.getId());
                context.startActivity(intent);
            }
        });
        holder.completed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, SpecificHeadingActivity.class);
                intent.putExtra("isWishList",false);
                intent.putExtra("headingName",heading.getName());
                intent.putExtra("headingImage",heading.getImageUri());
                intent.putExtra("headingKey",heading.getId());
                context.startActivity(intent);
            }
        });


        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path="Headings/"+heading.getId()+"/imageUri";
                Intent intent =new Intent(context, ImageViewActivity.class);
                intent.putExtra("URI",heading.getImageUri());
                intent.putExtra("id",heading.getId());
                intent.putExtra("path",path);
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
                                createDialogForDelete(heading,position);
                                break;

                            case R.id.changeDetailsMenu:
                                createDialogForChangeDetails(heading,position);
                                break;

                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });

    }

    private void createDialogForChangeDetails(final Heading heading, final int position) {
        final EditText editTextName=new EditText(context);
        editTextName.setText(heading.getName());
        final EditText editTextDes=new EditText(context);
        editTextDes.setText(heading.getDescription());
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
                    changeNameInDatabase(heading.getId(),position,name,des);
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

    private void createDialogForDelete(final Heading heading, final int position) {
        alertDialog=new AlertDialog.Builder(context)
                .setTitle("DELETE")
                .setMessage("Are you sure you want to delete it\n you will loose all the data")
                .create();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteHeadingFromDatabase(heading.getId(),position);
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

    private void changeNameInDatabase(String headingId, int position, String name, String des) {
        FirebaseDatabase.getInstance().getReference().child("Headings").child(headingId).child("name").setValue(name);
        FirebaseDatabase.getInstance().getReference().child("Headings").child(headingId).child("description").setValue(des);
        headings.get(position).setName(name);
        headings.get(position).setDescription(des);
        MainActivity.headingListAdapter.notifyItemChanged(position);

    }

    private void deleteHeadingFromDatabase(String headingId,int position) {
        FirebaseDatabase.getInstance().getReference().child("Headings").child(headingId).removeValue();
        headings.remove(position);

        if(position==0){
            MainActivity.headingListAdapter.notifyDataSetChanged();
        }
        else{
            MainActivity.headingListAdapter.notifyItemRemoved(position);
        }


    }

    @Override
    public int getItemCount() {
        return headings.size();
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
        TextView    name,
                    creator,
                    date,
                    description;

        ImageView image;

        Button  wishList,
                completed,
                menuButton;

        LinearLayout linearLayout,
                        headingImageLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name=itemView.findViewById(R.id.headingName);
            creator=itemView.findViewById(R.id.headingCreatorName);
            date=itemView.findViewById(R.id.headingCreatedDate);
            description=itemView.findViewById(R.id.headingDesc);
            image=itemView.findViewById(R.id.headingImage);

            wishList=itemView.findViewById(R.id.wishListButton);
            completed=itemView.findViewById(R.id.completedButton);
            menuButton=itemView.findViewById(R.id.headingMenuButton);

            linearLayout=itemView.findViewById(R.id.linearLayout);
            headingImageLayout=itemView.findViewById(R.id.headingImageLayout);




        }
    }
}
