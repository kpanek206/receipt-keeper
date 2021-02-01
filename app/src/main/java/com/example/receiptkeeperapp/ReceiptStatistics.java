package com.example.receiptkeeperapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReceiptStatistics extends AppCompatActivity {
    private Button showStatisticsBtn;
    private EditText firstDateView, secondDateView;
    private AnyChartView statisticChart;
    private LinearLayout statisticsLayout;
    private TextView sumView;

    private List<Upload> mUploads;
    private DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;

    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

    DatePickerDialog datePickerDialog;
    int year;
    int month;
    int dayOfMonth;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_statistics);
        showStatisticsBtn = findViewById(R.id.show_statistics_btn);
        firstDateView = findViewById(R.id.text_view_first_date);
        secondDateView = findViewById(R.id.text_view_second_date);
        statisticChart = findViewById(R.id.statistics_chart);
        statisticsLayout = findViewById(R.id.staticLayout);
        sumView = findViewById(R.id.text_view_sum);
        mUploads = new ArrayList<>();
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
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReceiptStatistics.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        firstDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDate(firstDateView);
            }
        });

        secondDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDate(secondDateView);
            }
        });

        showStatisticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<DataEntry> dataEntries = new ArrayList<>();
                dateCheck(mUploads, dataEntries);
            }
        });

    }

    public void selectDate(final TextView dateView){
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(ReceiptStatistics.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        dateView.setText(day + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, dayOfMonth);
        datePickerDialog.show();
    }

    public void dateCheck(List<Upload> mUploads, List<DataEntry> dataEntries){

        double sum = 0;
        Double[] prices = {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
        String[] categories = {"Elektronika", "Odzież", "Obuwie", "Jedzenie", "Zdrowie", "Kosmetyki", "Motoryzacja", "Dom", "Zabawki", "Inne"};

        Pie pie = AnyChart.pie();
        List<Upload> checkUploadList = new ArrayList<>();

        int mSize = mUploads.size();

        String firstDate = firstDateView.getText().toString().trim();
        String secondDate = secondDateView.getText().toString().trim();

        try {
            Date date1 = formatter.parse(firstDate);
            Date date2 = formatter.parse(secondDate);
            //Date date3 = formatter.parse(dbDate);

            for(int i = 0; i<mSize;i++) {

                Upload upload = mUploads.get(i);
                String dbDate = upload.getDate();
                Date date3 = formatter.parse(dbDate);
                if ((date3.after(date1) || date3.equals(date1)) && (date3.before(date2) || date3.equals(date2))) {
                    System.out.println(dbDate);
                    checkUploadList.add(upload);

                } else {
                    System.out.println("nie jest pomiędzy");
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        int checkUpSize = checkUploadList.size();

        for (int j = 0; j<checkUpSize; j++){
            Upload checkUpload = checkUploadList.get(j);
            double price = Double.parseDouble(checkUpload.getPrice());
            System.out.println(price);
            if(checkUpload.getCategory().equals("Elektronika")){
                prices[0]+=price;
            }
            else if(checkUpload.getCategory().equals("Odzież")){
                prices[1]+=price;
            }
            else if(checkUpload.getCategory().equals("Obuwie")){
                prices[2]+=price;
            }
            else if(checkUpload.getCategory().equals("Jedzenie")){
                prices[3]+=price;
            }
            else if(checkUpload.getCategory().equals("Zdrowie")){
                prices[4]+=price;
            }
            else if(checkUpload.getCategory().equals("Kosmetyki")){
                prices[5]+=price;
            }
            else if(checkUpload.getCategory().equals("Motoryzacja")){
                prices[6]+=price;
            }
            else if(checkUpload.getCategory().equals("Dom")){
                prices[7]+=price;
            }
            else if(checkUpload.getCategory().equals("Zabawki")){
                prices[8]+=price;
            }
            else if(checkUpload.getCategory().equals("Inne")){
                prices[9]+=price;
            }
        }


        for (int i = 0; i<categories.length; i++){
            dataEntries.add(new ValueDataEntry(categories[i], prices[i]));
        }

        pie.data(dataEntries);
        statisticChart.setChart(pie);

        System.out.println(dataEntries.get(1));

        for(int x = 0;x<10;x++){
            sum += prices[x];
        }

        sumView.setText("Suma: "+ sum +" PLN");

    }

    public void setupStatisticChart(Double[] prices){



    }
}



