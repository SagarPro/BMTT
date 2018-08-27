package com.sagsaguz.bmtt.utils;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "bmtt-mobilehub-1157786907-Payment")

public class PaymentDO {
    private String _emailId;
    private String _phone;
    private Map<String, String> _installments;
    private Map<String, String> _payment;

    @DynamoDBHashKey(attributeName = "emailId")
    @DynamoDBAttribute(attributeName = "emailId")
    public String getEmailId() {
        return _emailId;
    }

    public void setEmailId(final String _emailId) {
        this._emailId = _emailId;
    }
    @DynamoDBRangeKey(attributeName = "phone")
    @DynamoDBAttribute(attributeName = "phone")
    public String getPhone() {
        return _phone;
    }

    public void setPhone(final String _phone) {
        this._phone = _phone;
    }
    @DynamoDBAttribute(attributeName = "installments")
    public Map<String, String> getInstallments() {
        return _installments;
    }

    public void setInstallments(final Map<String, String> _installments) {
        this._installments = _installments;
    }
    @DynamoDBAttribute(attributeName = "payment")
    public Map<String, String> getPayment() {
        return _payment;
    }

    public void setPayment(final Map<String, String> _payment) {
        this._payment = _payment;
    }

}
