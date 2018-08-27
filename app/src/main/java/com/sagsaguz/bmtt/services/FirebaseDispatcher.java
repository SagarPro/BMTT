package com.sagsaguz.bmtt.services;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.gson.Gson;
import com.sagsaguz.bmtt.notification.MessageReceivingService;
import com.sagsaguz.bmtt.utils.AWSProvider;
import com.sagsaguz.bmtt.utils.AmountDO;
import com.sagsaguz.bmtt.utils.BmttUsersDO;
import com.sagsaguz.bmtt.utils.Config;
import com.sagsaguz.bmtt.utils.PaymentDO;
import com.sagsaguz.bmtt.utils.SendEMail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class FirebaseDispatcher extends JobService {

    RunTask runTask;
    String email="", phone="", arn="";

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onStartJob(final JobParameters job) {
        runTask = new RunTask(){
            @Override
            protected void onPostExecute(String s) {
                /*Toast.makeText(getApplicationContext(), "Working here...", Toast.LENGTH_SHORT).show();
                Log.d("Sagardispatcher", "Working");*/
                try {
                    startService(new Intent(getApplicationContext(), MessageReceivingService.class));
                    jobFinished(job, false);
                } catch (IllegalStateException e){
                    Log.d("OREO", "Illegal state exception");
                }
            }
        };
        runTask.execute();
        //new RunTask().execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    public class RunTask extends AsyncTask<Void,Void,String>
    {

        PaymentDO paymentDO = new PaymentDO();

        @Override
        protected String doInBackground(Void... voids) {
            Log.d("Sagardispatcher", "Working");

            SharedPreferences userPref = getSharedPreferences("USERDETAILS", MODE_PRIVATE);
            email = userPref.getString("EMAIL", "");
            phone = userPref.getString("PHONE", "");
            String userType = userPref.getString("USERTYPE", "");
            String name = userPref.getString("NAME", "");

            AWSProvider awsProvider = new AWSProvider();
            AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
            dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
            DynamoDBMapper dynamoDBMapper = DynamoDBMapper.builder()
                    .dynamoDBClient(dynamoDBClient)
                    .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                    .build();

            if (userType.equals("user")) {

                arn = userPref.getString("ARN", "");

                try {
                    paymentDO = dynamoDBMapper.load(PaymentDO.class, email, phone);

                    Map<String, String> installments = paymentDO.getInstallments();
                    Map<String, String> payments = paymentDO.getPayment();

                    List<String> dates = new ArrayList<>();
                    List<Integer> noOfDays = new ArrayList<>();
                    noOfDays.add(7);
                    noOfDays.add(3);
                    noOfDays.add(0);
                    noOfDays.add(-3);
                    noOfDays.add(-7);
                    noOfDays.add(-10);
                    noOfDays.add(-14);

                    Set insKeys = installments.keySet();
                    for (Object key1 : insKeys) {
                        dates.add((String) key1);
                    }

                    Collections.sort(dates, new DateComparator());

                    dates.remove(0);

                    Date currentDate = null, insDate;

                    Date c = Calendar.getInstance().getTime();
                    SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");
                    String formattedDate = df.format(c.getTime());
                    try {
                        currentDate = df.parse(formattedDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    List<Integer> hrs = new ArrayList<>();
                    hrs.add(12);
                    hrs.add(13);
                    hrs.add(14);
                    hrs.add(15);
                    hrs.add(16);
                    hrs.add(17);

                    SimpleDateFormat sdf = new SimpleDateFormat("HH");
                    String hour = sdf.format(c.getTime());

                    if (hrs.contains(Integer.parseInt(hour))) {

                        for (int i = 0; i < dates.size(); i++) {
                            try {
                                insDate = df.parse(dates.get(i));

                                if (currentDate != null) {
                                    long remaining = insDate.getTime() - currentDate.getTime();
                                    int remainingDays = (int) TimeUnit.DAYS.convert(remaining, TimeUnit.MILLISECONDS);

                                    if (noOfDays.contains(remainingDays)) {

                                        if (payments.get(dates.get(i)).equals("pending")) {

                                            int count = userPref.getInt(dates.get(i), 0);

                                            if (count == 0) {

                                                String message = "Dear " + name + ", Gentle reminder for BMTT course pending Fees. Installment-" + (i + 1) + ", payment due amount is " + installments.get(dates.get(i)) + ". Kindly make the Payment in our mobile app";

                                                sendEmail(message);
                                                //sendSMS(message);

                                                if (!arn.equals("")) {
                                                    AWSCredentials awsCredentials = new BasicAWSCredentials(Config.ACCESSKEY, Config.SECRETKEY);
                                                    AmazonSNSClient snsClient = new AmazonSNSClient(awsCredentials);
                                                    PublishRequest publishRequest = new PublishRequest();
                                                    publishRequest.setMessage(8 + arn + "$" + message);
                                                    publishRequest.setSubject("Pending fees reminder");
                                                    publishRequest.withTargetArn(arn);
                                                    snsClient.publish(publishRequest);
                                                }

                                                SharedPreferences.Editor editor = userPref.edit();
                                                editor.putInt(dates.get(i), (count + 1));
                                                editor.apply();

                                            }

                                        }
                                    } else {
                                        SharedPreferences.Editor editor = userPref.edit();
                                        editor.putInt(dates.get(i), 0);
                                        editor.apply();
                                    }
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                } catch (AmazonClientException e){
                    e.printStackTrace();
                }
            }

            if (userType.equals("SuperAdmin")){
                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("HH");
                SimpleDateFormat yearf = new SimpleDateFormat("yyyy");
                SimpleDateFormat monthf = new SimpleDateFormat("MM");
                SimpleDateFormat monthff = new SimpleDateFormat("MMM");
                SimpleDateFormat todayf = new SimpleDateFormat("dd");
                SimpleDateFormat yearff = new SimpleDateFormat("yy");
                SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");
                String hour = sdf.format(c.getTime());
                String month = monthf.format(c.getTime());
                String monthc = monthff.format(c.getTime());
                String today = todayf.format(c.getTime());
                String year = yearff.format(c.getTime());
                String todayAmt = "0", monthAmt = "0", yearAmt = "0";
                int count = userPref.getInt(df.format(c.getTime())+"SA", 0);
                if (hour.equals("17") || hour.equals("18")){
                    if (count == 0) {
                        AmountDO amountDO = dynamoDBMapper.load(AmountDO.class, Config.SUPERADMIN, yearf.format(c.getTime()));
                        if (amountDO != null){
                            String todayVal = amountDO.getToday();
                            if (today.equals(todayVal.substring(0,2).trim())){
                                todayAmt = String.valueOf(todayVal.substring(3));
                            } else {
                                todayAmt = "0";
                            }
                            String monthVal = amountDO.getMonth();
                            if (month.equals(monthVal.substring(0,2).trim())){
                                monthAmt = String.valueOf(monthVal.substring(3));
                            } else {
                                monthAmt = "0";
                            }
                            String annualVal = amountDO.getAnnual();
                            if (year.equals(annualVal.substring(0,2).trim())){
                                yearAmt = String.valueOf(annualVal.substring(3));
                            } else {
                                yearAmt = "0";
                            }
                        }

                        int todayAdm = 0, monthAdm = 0;
                        ScanRequest request = new ScanRequest().withTableName(Config.USERSTABLENAME);
                        ScanResult response = dynamoDBClient.scan(request);
                        List<Map<String, AttributeValue>> rows = response.getItems();
                        for(Map<String, AttributeValue> map : rows){
                            String admissionDate = map.get("admissionDone").getS();
                            if (admissionDate.contains("paid")) {
                                try {
                                    String date = admissionDate.substring(5);
                                    if (monthc.equals(date.substring(0, 3).trim())) {
                                        monthAdm++;
                                        if (today.equals(date.substring(4, 6).trim()))
                                            todayAdm++;
                                    }
                                } catch (NumberFormatException e) {
                                    Log.d("number_format_exception", e.getMessage());
                                    return "true";
                                }
                            }
                        }

                        String message;
                        if (amountDO != null) {
                            message = "Paid Amount Details\n\nToday : " + todayAmt + " Rs\nMonth : " + monthAmt + " Rs\nAnnual : " + yearAmt + " Rs\nToday's Admission : " + todayAdm + "\nThis Month's Admission : " + monthAdm;
                        } else {
                            message = "Amount is not yet paid in this year.";
                        }
                        //sendAmountEmail("bmtt@brightkidmont.com", message);
                        //sendAmountEmail("susmita_sanyal@brightkidmont.com", message);
                        //sendAmountEmail("psanyal@brightkidmont.com", message);
                        //sendAmountEmail("headoffice@brightkidmont.com", message);


                        //Sending email with cc
                        Properties props = new Properties();

                        props.put("mail.smtp.host", "smtp.gmail.com");
                        props.put("mail.smtp.socketFactory.port", "465");
                        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                        props.put("mail.smtp.auth", "true");
                        props.put("mail.smtp.port", "465");

                        Session session = Session.getDefaultInstance(props,
                                new javax.mail.Authenticator() {
                                    //Authenticating the password
                                    protected PasswordAuthentication getPasswordAuthentication() {
                                        return new PasswordAuthentication(Config.FROMEMAIL, Config.FROMPASSWORD);
                                    }
                                });
                        try {
                            MimeMessage mm = new MimeMessage(session);
                            mm.setFrom(new InternetAddress(Config.FROMEMAIL));
                            mm.addRecipient(Message.RecipientType.TO, new InternetAddress("susmita_sanyal@brightkidmont.com"));
                            mm.addRecipient(Message.RecipientType.CC, new InternetAddress("psanyal@brightkidmont.com"));
                            mm.addRecipient(Message.RecipientType.CC, new InternetAddress("headoffice@brightkidmont.com"));
                            mm.setSubject("Paid Amount Details");
                            mm.setText(message);
                            Transport.send(mm);

                        } catch (MessagingException e) {
                            e.printStackTrace();
                        }

                        SharedPreferences.Editor editor = userPref.edit();
                        editor.putInt(df.format(c.getTime())+"SA", 1);
                        editor.apply();
                    }
                }

            }
            return "true";
        }
    }

    private void sendAmountEmail(String to, String message) {
        String subject = "Paid Amount Details";
        SendEMail sm = new SendEMail(this, to, subject, message);
        sm.execute();
    }

    private void sendEmail(String message) {
        String subject = "Pending fees reminder.";
        SendEMail sm = new SendEMail(this, email, subject, message);
        sm.execute();
    }

    private void sendSMS(String message){
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone, null, message, null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private class DateComparator implements Comparator<String> {

        @Override
        public int compare(String obj1, String obj2) {
            SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");
            Date d1 = null, d2 = null;
            try {
                d1 = df.parse(obj1);
                d2 = df.parse(obj2);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return d1.compareTo(d2);
        }

    }

}
