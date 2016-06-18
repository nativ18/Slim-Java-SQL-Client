package model;

import engine.Deserializer;
import engine.SqlBinder;

public class User extends BaseEntity {

    @SqlBinder(val = "first_name")
    public String firstName;
    @SqlBinder(val = "last_name")
    public String lastName;
    @SqlBinder(val = "location")
    public String location;
    @SqlBinder(val = "password")
    public String password;
    @SqlBinder(val = "birthday")
    public long birthdate;
    @SqlBinder(val = "gender")
    public int gender;
    @SqlBinder(val = "thumb_url")
    public String thumbUrl;

    // not part of persistent entity
    private int unreadMsgs;
    private boolean isFollowed;
    private String mLeaderIdentifier;

    @Deserializer
    public User(long id, String firstName, String lastName, String location, String password, long birthdate,
                Integer gender, String thumbUrl) {
        super();
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.location = location;
        this.password = password;
        this.birthdate = birthdate;
        this.gender = gender;
        this.thumbUrl = thumbUrl;
    }

    public User() {
    }

    public int getUnreadMsgs() {
        return unreadMsgs;
    }

    public void setUnreadMsgs(int unreadMsgs) {
        this.unreadMsgs = unreadMsgs;
    }

    public boolean isFollowed() {
        return isFollowed;
    }

    public void setIsFollowed(boolean isFollowed) {
        this.isFollowed = isFollowed;
    }

    public void setGender(int gender) {
        this.gender = gender;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setFollowed(boolean isFollowed) {
        this.isFollowed = isFollowed;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public void setUnreadMessages(int unreadMsgs) {
        this.unreadMsgs = unreadMsgs;
    }

    public int getUnreadMessages() {
        return unreadMsgs;
    }

    public String getFullname() {
        return String.format("%s %s", firstName, lastName);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(long birthdate) {
        this.birthdate = birthdate;
    }

    public void setLeaderIdentifier(String leaderIdentifier) {
        this.mLeaderIdentifier = leaderIdentifier;
    }

    public String getLeaderIdentifier() {
        return mLeaderIdentifier;
    }
}
