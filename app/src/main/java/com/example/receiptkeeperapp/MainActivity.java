package com.example.receiptkeeperapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amitshekhar.DebugDB;
import com.anychart.AnyChartView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.spec.ECField;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Button captureImageBtn, saveReceiptBtn, receiptListBtn, selectDateBtn, receiptStatisticsBtn;
    private ImageView imageView;
    private EditText textViewPrice, textViewDate, textViewNip, textViewName;
    private TextView textPrice, textDate, textNip, textCategory;
    private String currentPhotoPath;
    private ProgressBar progressBar;
    private Spinner categoriesSpinner;
    private ScrollView scroll;

    private Uri mImageUri;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask mUploadTask;

    private String priceString, nipStringClean, spinnerText;

    DatePickerDialog datePickerDialog;
    int year;
    int month;
    int dayOfMonth;
    Calendar calendar;

    //Pattern expressionPrice = Pattern.compile("(TOTAL|RAZEM|SUMA|PLN|USD|EUR|€|\\$)\\s?(\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{2}))|(\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{2})?)\\s?(PLN|USD|EUR|€|\\$)\n");
    Pattern expressionPrice = Pattern.compile("(RAZEM:|SUMA|RAZEM|SUMA|PLN)\\s?(\\d{1,12}(?:\\s?\\d{3})*(?:[.,]\\d{2}))|(\\d{1,12}(?:\\s?\\d{3})*(?:[.,]\\d{2})?)\\s?(PLN)\n");
    Pattern expressionDate = Pattern.compile("(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)|(0?[1-9]|[12][0-9]|3[01])-(0?[1-9]|1[012])-((19|20)\\d\\d)|(0?[1-9]|[12][0-9]|3[01])\\.(0?[1-9]|1[012])\\.((19|20)\\d\\d)|((19|20)\\d\\d)/(0?[1-9]|1[012])/([0-3][0-9])|((19|20)\\d\\d)-(0?[1-9]|1[012])-([0-3][0-9])|((19|20)\\d\\d)\\.(0?[1-9]|1[012])\\.([0-3][0-9])");
    Pattern expressionNip = Pattern.compile("(NIP|NIP:)\\s[0-9]{10}\\s|[0-9]{3}-[0-9]{3}-[0-9]{2}-[0-9]{2}|[0-9]{3}-[0-9]{2}-[0-9]{2}-[0-9]{3}");


    SimpleDateFormat formatter1=new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat formatter2=new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat formatter3=new SimpleDateFormat("dd.MM.yyyy");
    SimpleDateFormat formatter4=new SimpleDateFormat("yyyy/MM/dd");
    SimpleDateFormat formatter5=new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat formatter6=new SimpleDateFormat("yyyy.MM.dd");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            //grant the permission
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
        }

        setInvisibility();

        captureImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });


        saveReceiptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(MainActivity.this, "Paragon jest dodawany...", Toast.LENGTH_SHORT).show();
                }else{
                    uploadFile();
                }

            }
        });

        receiptListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openReceiptActivity();
            }
        });

        selectDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                datePickerDialog = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                textViewDate.setText(day + "/" + (month + 1) + "/" + year);
                            }
                        }, year, month, dayOfMonth);
                datePickerDialog.show();
            }
        });

        receiptStatisticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openReceiptStatistics();
            }
        });

    }

    private void setVisibility(){
        imageView.setVisibility(View.VISIBLE);
        textViewName.setVisibility(View.VISIBLE);
        textViewPrice.setVisibility(View.VISIBLE);
        textViewDate.setVisibility(View.VISIBLE);
        textViewNip.setVisibility(View.VISIBLE);
        saveReceiptBtn.setVisibility(View.VISIBLE);
        categoriesSpinner.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        textPrice.setVisibility(View.VISIBLE);
        textDate.setVisibility(View.VISIBLE);
        textNip.setVisibility(View.VISIBLE);
        textCategory.setVisibility(View.VISIBLE);
        scroll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
    }
    private void setInvisibility(){
        textViewName.setText("");
        textViewPrice.setText("");
        textViewDate.setText("");
        textViewNip.setText("");
        imageView.setImageBitmap(null);
        imageView.setVisibility(View.INVISIBLE);
        textViewName.setVisibility(View.INVISIBLE);
        textViewPrice.setVisibility(View.INVISIBLE);
        textViewDate.setVisibility(View.INVISIBLE);
        textViewNip.setVisibility(View.INVISIBLE);
        saveReceiptBtn.setVisibility(View.INVISIBLE);
        categoriesSpinner.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        textPrice.setVisibility(View.INVISIBLE);
        textDate.setVisibility(View.INVISIBLE);
        textNip.setVisibility(View.INVISIBLE);
        textCategory.setVisibility(View.INVISIBLE);
        scroll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
    }

    private void init(){
        captureImageBtn = findViewById(R.id.capture_image_btn);
        imageView = findViewById(R.id.image_view);
        textViewPrice = findViewById(R.id.text_display_price);
        textViewDate = findViewById(R.id.text_display_date);
        textViewNip = findViewById(R.id.text_display_nip);
        textViewName = findViewById(R.id.text_display_name);
        textPrice = findViewById(R.id.text_price);
        textDate = findViewById(R.id.text_date);
        textNip = findViewById(R.id.text_nip);
        textCategory = findViewById(R.id.text_category);
        saveReceiptBtn = findViewById(R.id.save_receipt_btn);
        receiptListBtn = findViewById(R.id.receipt_list_btn);
        selectDateBtn = findViewById(R.id.select_date_btn);
        receiptStatisticsBtn = findViewById(R.id.statistics_btn);
        progressBar = findViewById(R.id.progress_bar);
        scroll = findViewById(R.id.scroll_view);
        categoriesSpinner = findViewById(R.id.text_display_category);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoriesSpinner.setAdapter(adapter);
        categoriesSpinner.setOnItemSelectedListener(this);

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
    }

    private String getFileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void uploadFile(){
        if(mImageUri != null){
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
            +"."+getFileExtension(mImageUri));

            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(0);
                                }
                            }, 500);

                            Toast.makeText(MainActivity.this, "Dodano do listy!", Toast.LENGTH_LONG).show();


                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful());
                            Uri downloadUrl = urlTask.getResult();

                            Upload upload = new Upload(
                                    textViewName.getText().toString().trim(),
                                    textViewPrice.getText().toString().trim(),
                                    textViewDate.getText().toString().trim(),
                                    textViewNip.getText().toString().trim(),
                                    spinnerText,
                                    downloadUrl.toString());

                            scroll.scrollTo(0,0);
                            setInvisibility();

                            String uploadId = mDatabaseRef.push().getKey();
                            mDatabaseRef.child(uploadId).setValue(upload);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            scroll.scrollTo(0,0);
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressBar.setProgress((int) progress);
                        }
                    });
        }else{
            Toast.makeText(this, "Zrób zdjęcie paragonu!", Toast.LENGTH_SHORT).show();
            scroll.scrollTo(0,0);
        }
    }

    private void dispatchTakePictureIntent() {

        String fileName = "photo";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);

            currentPhotoPath = imageFile.getAbsolutePath();

            mImageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.receiptkeeperapp.fileprovider", imageFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            startActivityForResult(intent, 1);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void isolateData(String receiptText){
        // Dopasowuje wyrażenie regularne do tekstu i je wyświetla
        Matcher matcherDate = expressionDate.matcher(receiptText);
        Matcher matcherPrice = expressionPrice.matcher(receiptText);
        Matcher matcherNip = expressionNip.matcher(receiptText);


        while (matcherDate.find()) {
            String matcherDateString = matcherDate.group();
            Pattern expressionFormatter1 = Pattern.compile("(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)");
            Pattern expressionFormatter2 = Pattern.compile("(0?[1-9]|[12][0-9]|3[01])-(0?[1-9]|1[012])-((19|20)\\d\\d)");
            Pattern expressionFormatter3 = Pattern.compile("(0?[1-9]|[12][0-9]|3[01])\\.(0?[1-9]|1[012])\\.((19|20)\\d\\d)");
            Pattern expressionFormatter4 = Pattern.compile("((19|20)\\d\\d)/(0?[1-9]|1[012])/([0-3][0-9])");
            Pattern expressionFormatter5 = Pattern.compile("((19|20)\\d\\d)-(0?[1-9]|1[012])-([0-3][0-9])");
            Pattern expressionFormatter6 = Pattern.compile("((19|20)\\d\\d)\\.(0?[1-9]|1[012])\\.([0-3][0-9])");
            Matcher matcherFormatter1 = expressionFormatter1.matcher(matcherDateString);
            Matcher matcherFormatter2 = expressionFormatter2.matcher(matcherDateString);
            Matcher matcherFormatter3 = expressionFormatter3.matcher(matcherDateString);
            Matcher matcherFormatter4 = expressionFormatter4.matcher(matcherDateString);
            Matcher matcherFormatter5 = expressionFormatter5.matcher(matcherDateString);
            Matcher matcherFormatter6 = expressionFormatter6.matcher(matcherDateString);

            while (matcherFormatter1.find()){
                String sDate1 = matcherFormatter1.group();
                try {
                    Date date1 = formatter1.parse(sDate1);
                    String mDate1 = formatter1.format(date1);
                    textViewDate.setText(mDate1);


                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            while (matcherFormatter2.find()){
                String sDate2 = matcherFormatter2.group();
                try {
                    Date date2 = formatter2.parse(sDate2);
                    String mDate2 = formatter1.format(date2);
                    textViewDate.setText(mDate2);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            while (matcherFormatter3.find()){
                String sDate3 = matcherFormatter3.group();
                try {
                    Date date3 = formatter3.parse(sDate3);
                    String mDate3 = formatter1.format(date3);
                    textViewDate.setText(mDate3);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            while (matcherFormatter4.find()){
                String sDate4 = matcherFormatter4.group();
                try {
                    Date date4 = formatter4.parse(sDate4);
                    String mDate4 = formatter1.format(date4);
                    textViewDate.setText(mDate4);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            while (matcherFormatter5.find()){
                String sDate5 = matcherFormatter5.group();
                try {
                    Date date5 = formatter5.parse(sDate5);
                    String mDate5 = formatter1.format(date5);
                    textViewDate.setText(mDate5);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            while (matcherFormatter6.find()){
                String sDate6 = matcherFormatter6.group();
                try {
                    Date date6 = formatter6.parse(sDate6);
                    String mDate6 = formatter1.format(date6);
                    textViewDate.setText(mDate6);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

        }
        while (matcherPrice.find()) {
            String matcherPriceString = matcherPrice.group();
            Pattern expressionFloatPrice = Pattern.compile("(\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{2}))|(\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{2})?)");
            Matcher matcherFloatPrice = expressionFloatPrice.matcher(matcherPriceString);
            while(matcherFloatPrice.find()){
                priceString = matcherFloatPrice.group().replace(",",".");
                textViewPrice.setText(priceString);
                double price = Double.parseDouble(priceString);
                //System.out.println("Cena: " + price + "PLN");
            }

        }
        while (matcherNip.find()) {
            String nipString = matcherNip.group();
            Pattern expressionNipOnly = Pattern.compile("[0-9]{10}|[0-9]{3}-[0-9]{3}-[0-9]{2}-[0-9]{2}|[0-9]{3}-[0-9]{2}-[0-9]{2}-[0-9]{3}");
            Matcher matcherNipOnly = expressionNipOnly.matcher(nipString);

            while(matcherNipOnly.find()){
                nipStringClean = matcherNipOnly.group().replace("-","");
                textViewNip.setText(nipStringClean);

            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);

            ImageView imageView = findViewById(R.id.image_view);

            imageView.setImageBitmap(bitmap);

            FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
            //2. Get an instance of FirebaseVision
            FirebaseVision firebaseVision = FirebaseVision.getInstance();
            //3. Create an instance of FirebaseVisionTextRecognizer
            FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = firebaseVision.getOnDeviceTextRecognizer();
            //4. Create a task to process the image
            Task<FirebaseVisionText> task = firebaseVisionTextRecognizer.processImage(firebaseVisionImage);
            //5. if task is success
            task.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                    String  receiptText = firebaseVisionText.getText();
                    System.out.println(receiptText);
                    setVisibility();
                    isolateData(receiptText);
                }
            });
            //6. if task is failure
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void openReceiptActivity() {
        Intent intent = new Intent(this, ReceiptActivity.class);
        startActivity(intent);
    }

    private void openReceiptStatistics(){
        Intent intent = new Intent(this, ReceiptStatistics.class);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        spinnerText = adapterView.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
