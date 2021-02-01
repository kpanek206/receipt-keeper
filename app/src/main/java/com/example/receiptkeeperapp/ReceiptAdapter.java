package com.example.receiptkeeperapp;


import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder> {
    private Context mContext;
    private List<Upload> mUploads;
    private OnItemClickListener mListener;

    public ReceiptAdapter(Context context, List<Upload> uploads){
        mContext = context;
        mUploads = uploads;
    }

    @NonNull
    @Override
    public ReceiptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.receipt_items, parent, false);
        return new ReceiptViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiptViewHolder holder, int position) {
        Upload uploadCurrent = mUploads.get(position);
        holder.textViewName.setText("Nazwa: " + uploadCurrent.getName());
        holder.textViewPrice.setText("Cena w PLN: " + uploadCurrent.getPrice());
        holder.textViewDate.setText("Data: " + uploadCurrent.getDate());
        holder.textViewNip.setText("NIP: " + uploadCurrent.getNip());
        holder.textViewCategory.setText("Kategoria: " + uploadCurrent.getCategory());
        Picasso.with(mContext)
                .load(uploadCurrent.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }


    public class ReceiptViewHolder extends  RecyclerView.ViewHolder implements  View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        public TextView textViewNip, textViewName, textViewPrice, textViewDate, textViewCategory;
        public ImageView imageView;
        public ReceiptViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewNip = itemView.findViewById(R.id.text_view_nip);
            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewPrice = itemView.findViewById(R.id.text_view_price);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            textViewCategory = itemView.findViewById(R.id.text_view_category);
            imageView = itemView.findViewById(R.id.image_view_upload);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }


        @Override
        public void onClick(View view) {
            if (mListener != null){
                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION){
                    mListener.onItemClick(position);
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle("Wybierz opcję:");
            MenuItem doWhatever = contextMenu.add(Menu.NONE, 1, 1, "Zmień dane");
            MenuItem delete = contextMenu.add(Menu.NONE, 2, 2, "Usuń");

            doWhatever.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (mListener != null){
                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION){

                    switch (menuItem.getItemId()){
                        case 1:
                            mListener.onWhateverClick(position);
                            return true;
                        case 2:
                            mListener.onDeleteClick(position);
                            return true;
                    }
                }
            }
            return false;
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position);

        void onWhateverClick(int position);

        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }
}
