package com.sagsaguz.bmtt;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.sagsaguz.bmtt.adapter.AttachmentAdapter;
import com.sagsaguz.bmtt.utils.AWSProvider;
import com.sagsaguz.bmtt.utils.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttachmentsActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE = 3;
    private static final int REQUEST_CODE = 4;

    private ProgressBar pbAttachment;
    private ListView lvAttachment;
    private FloatingActionButton fb_attachment;

    private AttachmentAdapter attachmentAdapter;

    private List<String> attachmentList = new ArrayList<>();

    String userType = "user";

    private FilePickerDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attachments_layout);

        pbAttachment = findViewById(R.id.pbAttachment);
        lvAttachment = findViewById(R.id.lvAttachment);
        fb_attachment = findViewById(R.id.fb_attachment);
        pbAttachment.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
        pbAttachment.setVisibility(View.GONE);

        Intent intent = getIntent();
        userType = intent.getStringExtra("USERTYPE");

        fb_attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storagePermissionCheck();
            }
        });

        attachmentAdapter = new AttachmentAdapter(AttachmentsActivity.this, attachmentList);
        lvAttachment.setAdapter(attachmentAdapter);

        lvAttachment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                final Dialog dialog = new Dialog(AttachmentsActivity.this);
                dialog.setContentView(R.layout.custom_dialog);

                TextView tvTitle = dialog.findViewById(R.id.tvTitle);
                tvTitle.setText("Pdf file viewer");

                TextView tvMessage = dialog.findViewById(R.id.tvMessage);
                tvMessage.setText("Choose how you want to view the pdf file.");

                Button btnStandard = dialog.findViewById(R.id.btnRemove);
                btnStandard.setText("Standard");
                btnStandard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        Uri uri =  Uri.parse("https://s3.amazonaws.com/brightkidmont/pdf/"+attachmentList.get(i));
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "application/pdf");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e){
                            Toast.makeText(AttachmentsActivity.this, "Please try other type of viewer", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                Button btnCustom = dialog.findViewById(R.id.btnCancel);
                btnCustom.setText("Custom");
                btnCustom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        WebView mWebView=new WebView(AttachmentsActivity.this);
                        mWebView.getSettings().setJavaScriptEnabled(true);
                        mWebView.loadUrl("https://docs.google.com/gview?embedded=true&url="+"https://s3.amazonaws.com/brightkidmont/pdf/"+attachmentList.get(i));
                        setContentView(mWebView);
                    }
                });

                dialog.show();

            }
        });

        lvAttachment.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                deleteAttachment(i);
                return true;
            }
        });

        if(userType.equals("user")){
            fb_attachment.setVisibility(View.GONE);
            lvAttachment.setOnItemLongClickListener(null);
        }

        new LoadPdf().execute();

        DialogProperties properties = new DialogProperties();

        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;

        dialog = new FilePickerDialog(AttachmentsActivity.this,properties);
        dialog.setTitle("Select a File");
        dialog.getWindow().setBackgroundDrawableResource(R.color.shadow);

        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                //files is the array of the paths of files selected by the Application User.
                uploadFile(files);
            }
        });

    }

    private void deleteAttachment(final int position){
        final Dialog dialog = new Dialog(AttachmentsActivity.this);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        tvTitle.setText("Delete pdf file");
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        tvMessage.setText("Are you sure, you want to delete\n"+attachmentList.get(position));
        Button btnDelete = dialog.findViewById(R.id.btnRemove);
        btnDelete.setText("Delete");
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                new DeleteFile().execute(attachmentList.get(position));
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void storagePermissionCheck(){
        int permissionCheck = ContextCompat.checkSelfPermission(AttachmentsActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck == 0) {
            dialog.show();
        } else {
            final Dialog dialog2 = new Dialog(AttachmentsActivity.this);
            dialog2.setContentView(R.layout.permission_dialog);

            TextView dialog_message = dialog2.findViewById(R.id.dialog_message);
            dialog_message.setText("This app needs storage permission for uploading files.Click ok or you can allow permissions manually by clicking on settings below.");
            TextView pCancel = dialog2.findViewById(R.id.pCancel);
            TextView pSettings = dialog2.findViewById(R.id.pSettings);
            TextView pOk = dialog2.findViewById(R.id.pOk);
            pCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog2.dismiss();
                }
            });
            pSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog2.dismiss();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
            pOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog2.dismiss();
                    ActivityCompat.requestPermissions(AttachmentsActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE);
                }
            });
            dialog2.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dialog.show();
                }
                break;
            }
        }
    }

    private void uploadFile(String[] filePath){

        final ProgressDialog progressDialog = new ProgressDialog(AttachmentsActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setMessage("Uploading, please wait.");
        progressDialog.setCancelable(true);
        progressDialog.show();

        try {
            BasicAWSCredentials credentials = new BasicAWSCredentials(Config.ACCESSKEY, Config.SECRETKEY);
            AmazonS3Client s3 = new AmazonS3Client(credentials);
            TransferUtility transferUtility = new TransferUtility(s3, AttachmentsActivity.this);

            for (int i=0; i<filePath.length; i++){

                File file = new File(filePath[i]);
                final String fileName = filePath[i].substring(filePath[i].lastIndexOf("/")+1);

                if (!attachmentList.contains(fileName)){
                    final TransferObserver observer = transferUtility.upload(
                            Config.BUCKETNAME + "/pdf",
                            fileName,
                            file,
                            CannedAccessControlList.PublicRead
                    );

                    observer.setTransferListener(new TransferListener() {
                        @Override
                        public void onStateChanged(int id, TransferState state) {
                            if (TransferState.COMPLETED.equals(observer.getState())) {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                attachmentList.add(fileName);
                                Collections.sort(attachmentList);
                                attachmentAdapter.notifyDataSetChanged();
                                Toast.makeText(AttachmentsActivity.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        }

                        @Override
                        public void onError(int id, Exception ex) {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Log.d("profilePicUpload Failed", ex.getMessage());
                            Toast.makeText(AttachmentsActivity.this, "Failed to upload, Try again", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(AttachmentsActivity.this, "File with this name already exists", Toast.LENGTH_LONG).show();
                }

            }

        } catch (AmazonClientException e){
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            Toast.makeText(AttachmentsActivity.this, "Failed to upload, Try again", Toast.LENGTH_LONG).show();
        }
    }


    private class LoadPdf extends AsyncTask<Void, Void, Boolean>{

        @Override
        protected void onPreExecute() {
            if(pbAttachment != null){
                pbAttachment.setVisibility(View.VISIBLE);
            }
            attachmentList.clear();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            AWSCredentials myCredentials = new BasicAWSCredentials(Config.ACCESSKEY, Config.SECRETKEY);
            AmazonS3 s3client = new AmazonS3Client(myCredentials);

            try {
                ObjectListing listing = s3client.listObjects( Config.BUCKETNAME, "pdf/");
                final List<S3ObjectSummary> summaries = listing.getObjectSummaries();

                for(S3ObjectSummary objectSummary : summaries){
                    String key = objectSummary.getKey();
                    if (!key.equals("pdf/")) {
                        String s = key.substring(key.indexOf("/")+1);
                        attachmentList.add(s);
                    }
                }
                return true;
            } catch (AmazonClientException e){
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(pbAttachment != null){
                pbAttachment.setVisibility(View.GONE);
            }
            if (aBoolean){
                Collections.sort(attachmentList);
                attachmentAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(AttachmentsActivity.this, "Please check your internet and try again.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class DeleteFile extends AsyncTask<String, Void, Boolean>{

        ProgressDialog progressDialog;
        String filename = "";

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(AttachmentsActivity.this, R.style.MyAlertDialogStyle);
            progressDialog.setMessage("Deleting, please wait.");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            try {
                AWSProvider awsProvider = new AWSProvider();
                AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
                dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
                AmazonS3 s3client = new AmazonS3Client(awsProvider.getCredentialsProvider(getBaseContext()));

                s3client.deleteObject(Config.BUCKETNAME, "pdf/"+strings[0]);
                filename = strings[0];
                return true;
            } catch (AmazonClientException e){
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDialog.dismiss();
            if (aBoolean){
                attachmentList.remove(filename);
                Collections.sort(attachmentList);
                attachmentAdapter.notifyDataSetChanged();
                Toast.makeText(AttachmentsActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AttachmentsActivity.this, "Failed to delete, try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
