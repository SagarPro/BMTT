package com.sagsaguz.bmtt;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.sagsaguz.bmtt.utils.Config;

import java.io.File;

public class UploadFilesActivity extends AppCompatActivity {

    ProgressBar pb;
    Button btn_upload;

    AmazonS3Client s3;
    BasicAWSCredentials credentials;
    TransferUtility transferUtility;
    TransferObserver observer;

    String key = Config.ACCESSKEY;
    String secret = Config.SECRETKEY;
    //String path = ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_files_layout);

        pb = (ProgressBar) findViewById(R.id.pb);
        btn_upload = (Button) findViewById(R.id.btn_upload);

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //final Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                //galleryIntent.setType("*/*");
                //startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);

                credentials = new BasicAWSCredentials(key,secret);
                s3 = new AmazonS3Client(credentials);
                transferUtility = new TransferUtility(s3, UploadFilesActivity.this);

                File path = Environment.getExternalStorageDirectory();
                File file = new File(path + "/Appograph/Images/ProfilePics/sss-777.jpg");
                if(!file.exists()) {
                    Toast.makeText(UploadFilesActivity.this, "File Not Found!", Toast.LENGTH_SHORT).show();
                    return;
                }
                observer = transferUtility.upload(
                        Config.BUCKETNAME,
                        "video_test.jpg",
                        file
                );

                observer.setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {

                        if (state.COMPLETED.equals(observer.getState())) {

                            Toast.makeText(UploadFilesActivity.this, "File Upload Complete", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

                        long _bytesCurrent = bytesCurrent;
                        long _bytesTotal = bytesTotal;

                        float percentage =  ((float)_bytesCurrent /(float)_bytesTotal * 100);
                        Log.d("percentage","" +percentage);
                        pb.setProgress((int) percentage);
                        //_status.setText(percentage + "%");
                    }

                    @Override
                    public void onError(int id, Exception ex) {

                        Toast.makeText(UploadFilesActivity.this, "" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

}
