package com.example.jardinenfantmobile.user;

public class ReadWriteUserDetails {
    public String userUid, doB, gender, mobile, role;

    public ReadWriteUserDetails(){};
    public ReadWriteUserDetails(String textdoB, String textGender, String textMobile){
        this.doB = textdoB;
        this.gender = textGender;
        this.mobile = textMobile;
        this.role = "client";
    }
}
