package com.sagsaguz.bmtt.notification;

/*
 * Copyright 2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.sagsaguz.bmtt.sns_tools.AmazonSNSClientWrapper;
import com.sagsaguz.bmtt.sns_tools.SampleMessageGenerator.Platform;

public class SNSMobilePush {

	private AmazonSNSClientWrapper snsClientWrapper;

	private SNSMobilePush(AmazonSNS snsClient) {
		this.snsClientWrapper = new AmazonSNSClientWrapper(snsClient);
	}

	private static final Map<Platform, Map<String, MessageAttributeValue>> attributesMap = new HashMap<Platform, Map<String, MessageAttributeValue>>();
	static {
		attributesMap.put(Platform.GCM, null);
	}

	public static void main(String[] args) throws IOException {

		AmazonSNS sns = new AmazonSNSClient(new PropertiesCredentials(SNSMobilePush.class.getResourceAsStream("aws.properties")));

		try {
			SNSMobilePush sample = new SNSMobilePush(sns);
			 sample.demoAndroidAppNotification();
		} catch (AmazonServiceException ase) {
			System.out
					.println("Caught an AmazonServiceException, which means your request made it "
							+ "to Amazon SNS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out
					.println("Caught an AmazonClientException, which means the client encountered "
							+ "a serious internal problem while trying to communicate with SNS, such as not "
							+ "being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}

	private void demoAndroidAppNotification() {
		// TODO: Please fill in following values for your application. You can
		// also change the notification payload as per your preferences using
		// the method
		// com.amazonaws.sns.samples.tools.SampleMessageGenerator.getSampleAndroidMessage()
		String serverAPIKey = "AAAAHcI37w8:APA91bHaPfB3pMHcyLP8g-yT9jPuJVYjX8a7Xj7Y7liHlMZ6oIPL5meb_rUqF895eLGATK29rPEhZYNN2y6amM9lWytU0XdbztYuXleKVvYDO50t8eVNH-y7Vi9Zm3EQdClB1q6-MdEU";
		String applicationName = "BMTT";
		String registrationId = "APA91bHQL0jorZUNeCQY0XrmdiMj3pysMB2ISorNbMcysSRqcUQX1FsW5JFHrz-EG-x9CFnclZ_kVqcLd1Af1ABtG5K6wOPX8RK3qhUToATOqqtPszG_yf0GCiRqjPL4Kizy7ZD_8XtA";
		snsClientWrapper.demoNotification(Platform.GCM, "", serverAPIKey,
				registrationId, applicationName, attributesMap);
	}

}
