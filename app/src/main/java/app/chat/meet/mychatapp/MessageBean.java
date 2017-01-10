package app.chat.meet.mychatapp;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Meet on 10/6/16.
 */
public class MessageBean {

    @SerializedName("rosterJID")
    public String rosterId;
    @SerializedName("messageId")
    public String messageId;
    @SerializedName("message")
    public String message;
    @SerializedName("isMe")
    public boolean isMe;

    public MessageBean(String rosterId,String messageId, String message, boolean isMe) {
        this.rosterId = rosterId;
        this.messageId = messageId;
        this.message = message;
        this.isMe = isMe;
    }
}
