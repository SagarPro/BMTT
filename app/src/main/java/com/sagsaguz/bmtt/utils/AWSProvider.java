package com.sagsaguz.bmtt.utils;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;

public class AWSProvider {

    public CognitoCachingCredentialsProvider getCredentialsProvider(Context context){
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "us-east-1:3a860d85-3680-40a6-b193-5826ba8b654e",
                Regions.US_EAST_1
        );
        return credentialsProvider;
    }

}
