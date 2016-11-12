package com.chubbycat.xthewiz.delthemall;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chubbycat.xthewiz.delthemall.picker.FilePickerActivity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SecondActivity extends AppCompatActivity {

    private static final int FILE_CODE = 1;
    private static final int READ_REQUEST_CODE = 42;
    private static final int REQUEST_WRITE_STORAGE = 112;

    TextView tvDirPath;
    Button btnDeleteNow;
    Button btnChooseFolder;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        initInstances();
    }

    private void initInstances() {
        tvDirPath = (TextView) findViewById(R.id.tvDirPath);
        btnDeleteNow = (Button) findViewById(R.id.btnDeleteNow);
        btnChooseFolder = (Button) findViewById(R.id.btnChooseFolder);

        tvDirPath.setText(getPathFromFile());
    }
//
//    public void setSchedule(View view) {
//        // Get Current Time
//        final Calendar c = Calendar.getInstance();
//        int mHour = c.get(Calendar.HOUR_OF_DAY);
//        int mMinute = c.get(Calendar.MINUTE);
//
//        // Launch Time Picker Dialog
//        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
//                new TimePickerDialog.OnTimeSetListener() {
//
//                    @Override
//                    public void onTimeSet(TimePicker view, int hourOfDay,
//                                          int minute) {
//
//                        tvSetTime.setText(String.format("%02d:%02d", hourOfDay, minute));
//                    }
//                }, mHour, mMinute, true);
//        timePickerDialog.show();
//    }

    public void popupChooseFolderDialog(View view) {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // For JellyBean and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = resultData.getClipData();

                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            // Do something with the URI
                        }
                    }
                    // For Ice Cream Sandwich
                } else {
                    ArrayList<String> paths = resultData.getStringArrayListExtra
                            (FilePickerActivity.EXTRA_PATHS);

                    if (paths != null) {
                        int counter = 0;
                        for (String path: paths) {
                            counter++;
                            Uri uri = Uri.parse(path);
                            // Do something with the URI
                            tvDirPath.setText(counter + ". " + uri.toString() + "\n");
                        }
                    }
                }

            } else {
                uri = resultData.getData();
                // Do something with the URI
                tvDirPath.setText(resultData.getDataString());
                requestDirPermission();
            }
        }

        if(requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//             uri = null;
            if(resultData != null) {
                uri = resultData.getData();
                Log.i("PERM_X", "Uri: " + uri.toString());
                Log.i("PERM_X", "Str: " + resultData.toUri(0));
                tvDirPath.setText(uri.toString());

            }
        }
    }

    public void requestDirPermission() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("*/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
//        ActivityCompat.requestPermissions(this
//                , new String[]
//                        { Manifest.permission.WRITE_EXTERNAL_STORAGE }
//                , REQUEST_WRITE_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i("TEST_X", "result = " + grantResults[0] + ", " + PackageManager.PERMISSION_GRANTED);
        switch (requestCode)
        {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //reload my activity with permission granted or use the features what required the permission
                    Toast.makeText(this, "result = " + grantResults, Toast.LENGTH_SHORT);
                } else
                {
                    Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
                Toast.makeText(this, "result = " + grantResults[0], Toast.LENGTH_SHORT);
            }
        }

    }

    public void deleteThem(View view) {
        String folderPath = tvDirPath.getText().toString();
        int counter = 0;
        int total = 0;

        if(folderPath.isEmpty()) {
            toast("กรุณาระบุ Folder ที่ต้องการลบข้อมูล");
        } else {
            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, uri);
            total = pickedDir.listFiles().length;
            Log.i("TEST_X", "Can Delete ? " + pickedDir.canWrite());
            Log.i("TEST_X", "Total = " + total);
            for(DocumentFile docFile : pickedDir.listFiles()) {
                Log.i("DEL_X", "Delete " + docFile.getName() + " : " + docFile.delete());
                counter++;
                Log.i("DEL_X", "Counter = " + counter);

                if(counter == total) {
                    toast("ลบไฟล์เสร็จสิ้นแล้ว");
                    setPathToFile(folderPath);
                }
            }
        }
    }

    private void setPathToFile(String filePath){

        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput("deletePath.txt", Context.MODE_PRIVATE);
            outputStream.write(filePath.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getPathFromFile(){
        FileInputStream inputStream;
        int c;
        String temp = "";

        try{
            inputStream = openFileInput("deletePath.txt");

            while ( (c = inputStream.read()) != -1) {
                temp = temp + Character.toString((char)c);
            }

            inputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        return temp;
    }

    private void toast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    class DeleteFileAsyncTask extends AsyncTask<Void, Integer, Void> {
        boolean running;
        ProgressDialog progressDialog;


        @Override
        protected Void doInBackground(Void... voids) {
            int i = 10;
            while(running) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if( i-- == 0 ) {
                    running = false;
                }

                publishProgress(i);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values){
            super.onProgressUpdate(values);
            progressDialog.setMessage(String.valueOf(values[0]));
        }

    }
}
