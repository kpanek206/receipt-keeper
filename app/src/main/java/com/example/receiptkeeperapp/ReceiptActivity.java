package com.example.receiptkeeperapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.UpdateAppearance;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReceiptActivity extends AppCompatActivity implements ReceiptAdapter.OnItemClickListener {
    private RecyclerView mRecyclerView;
    private ReceiptAdapter mAdapter;

    private ProgressBar mProgressCircle;

    private DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;

    private FirebaseStorage mStorage;
    private List<Upload> mUploads;

    DatePickerDialog datePickerDialog;
    int year;
    int month;
    int dayOfMonth;
    Calendar calendar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mProgressCircle = findViewById(R.id.progress_circle);

        mUploads = new ArrayList<>();

        mAdapter = new ReceiptAdapter(ReceiptActivity.this, mUploads);

        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(ReceiptActivity.this);

        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                mUploads.clear();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    upload.setKey(postSnapshot.getKey());
                    mUploads.add(upload);
                }

                mAdapter.notifyDataSetChanged();

                mProgressCircle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReceiptActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void showUpdateDialog(final String key, String name, String price, String date, String nip, String category, final String imageUrl) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.receipt_update, null);
        dialogBuilder.setView(dialogView);
        final EditText editTextName = (EditText) dialogView.findViewById(R.id.text_view_name_update);
        final EditText editTextPrice = (EditText) dialogView.findViewById(R.id.text_view_price_update);
        final EditText editTextDate = (EditText) dialogView.findViewById(R.id.text_view_date_update);
        final EditText editTextNip = (EditText) dialogView.findViewById(R.id.text_view_nip_update);
        final Spinner editTextCategory = (Spinner) dialogView.findViewById(R.id.text_view_category_update);
        final Button buttonUpdate = (Button) dialogView.findViewById(R.id.update_btn);
        final Button selectDateBtn = (Button) dialogView.findViewById(R.id.select_date_btn);

        editTextName.setText(name);
        editTextPrice.setText(price);
        editTextDate.setText(date);
        editTextNip.setText(nip);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                String price = editTextPrice.getText().toString().trim();
                String date = editTextDate.getText().toString().trim();
                String nip = editTextNip.getText().toString().trim();
                String category = editTextCategory.getSelectedItem().toString();

                updateReceipt(name, price, date, nip, category, imageUrl, key);

                alertDialog.dismiss();

            }
        });

        selectDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                datePickerDialog = new DatePickerDialog(ReceiptActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                editTextDate.setText(day + "/" + (month + 1) + "/" + year);
                            }
                        }, year, month, dayOfMonth);
                datePickerDialog.show();
            }
        });


    }

    private boolean updateReceipt(String name, String price, String date, String nip, String category, String imageUrl, String key) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("uploads").child(key);
        Upload upload = new Upload(name, price, date, nip, category, imageUrl);
        databaseReference.setValue(upload);
        Toast.makeText(this, "Zapisano zmiany!", Toast.LENGTH_LONG).show();
        return true;
    }

    @Override
    public void onItemClick(int position) {
    }

    @Override
    public void onWhateverClick(int position) {
        Upload selectedItem = mUploads.get(position);
        showUpdateDialog(selectedItem.getKey(), selectedItem.getName(), selectedItem.getPrice(), selectedItem.getDate(), selectedItem.getNip(),selectedItem.getCategory(), selectedItem.getImageUrl());

    }

    @Override
    public void onDeleteClick(int position) {
        Upload selectedItem = mUploads.get(position);
        final String selectedKey = selectedItem.getKey();

        StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseRef.child(selectedKey).removeValue();
                Toast.makeText(ReceiptActivity.this, "Paragon usunięty!", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mDatabaseRef.removeEventListener(mDBListener);
    }
}