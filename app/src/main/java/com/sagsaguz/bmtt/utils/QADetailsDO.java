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

@DynamoDBTable(tableName = "bmtt-mobilehub-1157786907-QADetails")

public class QADetailsDO {
    private String _question;
    private String _user;
    private String _answer;
    private String _name;
    private String _when;

    @DynamoDBHashKey(attributeName = "question")
    @DynamoDBAttribute(attributeName = "question")
    public String getQuestion() {
        return _question;
    }

    public void setQuestion(final String _question) {
        this._question = _question;
    }
    @DynamoDBRangeKey(attributeName = "user")
    @DynamoDBAttribute(attributeName = "user")
    public String getUser() {
        return _user;
    }

    public void setUser(final String _user) {
        this._user = _user;
    }
    @DynamoDBAttribute(attributeName = "answer")
    public String  getAnswer() {
        return _answer;
    }

    public void setAnswer(final String _answer) {
        this._answer = _answer;
    }
    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return _name;
    }

    public void setName(final String _name) {
        this._name = _name;
    }
    @DynamoDBAttribute(attributeName = "when")
    public String getWhen() {
        return _when;
    }

    public void setWhen(final String _when) {
        this._when = _when;
    }

}
