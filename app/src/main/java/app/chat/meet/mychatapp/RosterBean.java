package app.chat.meet.mychatapp;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Meet on 17/6/16.
 */
public class RosterBean {

    @SerializedName("rosterJID")
    public String rosterJID;
    @SerializedName("rosterName")
    public String rosterName;

    public RosterBean(String rosterJID, String rosterName) {
        this.rosterJID = rosterJID;
        this.rosterName = rosterName;
    }

}