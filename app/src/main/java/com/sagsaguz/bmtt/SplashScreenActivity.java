package com.sagsaguz.bmtt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SplashScreenActivity extends AppCompatActivity {

    private boolean first = true;
    private String login, userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SharedPreferences loginPref = getSharedPreferences("USERDETAILS", MODE_PRIVATE);
        login = loginPref.getString("LOGIN", "logout");
        userType = loginPref.getString("USERTYPE", null);

        loginType();

        /*if(!isOnline()){
            showSnackBar();
        } else {
            if(first){
                first = false;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loginType();
                    }
                }, 3000);
            } else {
                loginType();
            }
        }*/

    }

    private void loginType(){
        assert login != null;
        if (login.equals("login")){
            assert userType != null;
            if (userType.equals("user")){
                startActivity(new Intent(getBaseContext(), HomePageActivity.class));
            } else {
                startActivity(new Intent(getBaseContext(), MainBranchActivity.class));
            }
        } else {
            startActivity(new Intent(getBaseContext(), LoginActivity.class));
        }
        finish();
    }

    private void showSnackBar(){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), "Network connection error!!!", Snackbar.LENGTH_SHORT)
                .setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SplashScreenActivity.this.recreate();
                    }
                });
        snackbar.setActionTextColor(ContextCompat.getColor(getBaseContext(), R.color.colorAccent));

        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary));
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.colorAccent));
        snackbar.setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);
        snackbar.show();
    }

    //checking connectivity with internet
    public Boolean isOnline() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            return (returnVal==0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
