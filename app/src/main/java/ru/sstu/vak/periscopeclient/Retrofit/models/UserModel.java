package ru.sstu.vak.periscopeclient.Retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by Anton on 01.05.2018.
 */

public class UserModel {

    public UserModel() {
    }

    public UserModel(int id, String login, String profileImagePath, int userAlphaColor, String firstName, String lastName, String aboutMe) {
        this.id = id;
        this.login = login;
        this.profileImagePath = profileImagePath;
        this.userAlphaColor = userAlphaColor;
        this.firstName = firstName;
        this.lastName = lastName;
        this.aboutMe = aboutMe;
    }

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("login")
    @Expose
    private String login;

    @SerializedName("profileImagePath")
    @Expose
    private String profileImagePath;

    @SerializedName("userAlphaColor")
    @Expose
    private int userAlphaColor;

    @SerializedName("firstName")
    @Expose
    private String firstName;

    @SerializedName("lastName")
    @Expose
    private String lastName;

    @SerializedName("aboutMe")
    @Expose
    private String aboutMe;

    @SerializedName("updateDate")
    @Expose
    private Date updateDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public int getUserAlphaColor() {
        return userAlphaColor;
    }

    public void setUserAlphaColor(int userAlphaColor) {
        this.userAlphaColor = userAlphaColor;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}
