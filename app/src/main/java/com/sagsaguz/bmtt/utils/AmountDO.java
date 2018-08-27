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

@DynamoDBTable(tableName = "bmtt-mobilehub-1157786907-Amount")

public class AmountDO {
    private String _email;
    private String _year;
    private String _annual;
    private String _month;
    private String _today;

    @DynamoDBHashKey(attributeName = "email")
    @DynamoDBAttribute(attributeName = "email")
    public String getEmail() {
        return _email;
    }

    public void setEmail(final String _email) {
        this._email = _email;
    }
    @DynamoDBRangeKey(attributeName = "year")
    @DynamoDBAttribute(attributeName = "year")
    public String getYear() {
        return _year;
    }

    public void setYear(final String _year) {
        this._year = _year;
    }
    @DynamoDBAttribute(attributeName = "annual")
    public String getAnnual() {
        return _annual;
    }

    public void setAnnual(final String _annual) {
        this._annual = _annual;
    }
    @DynamoDBAttribute(attributeName = "month")
    public String getMonth() {
        return _month;
    }

    public void setMonth(final String _month) {
        this._month = _month;
    }
    @DynamoDBAttribute(attributeName = "today")
    public String getToday() {
        return _today;
    }

    public void setToday(final String _today) {
        this._today = _today;
    }

}
