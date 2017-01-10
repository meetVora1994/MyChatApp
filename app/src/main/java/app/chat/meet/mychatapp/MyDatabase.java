package app.chat.meet.mychatapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jivesoftware.smack.roster.RosterEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Meet on 10/6/16.
 */
public class MyDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyChatAppDB.db";

    public static final String TABLE_CONVERSATION = "conversation";
    public static final String COLUMN_CONVERSATION_DB_ID = "id";
    public static final String COLUMN_CONVERSATION_ROSTER_ID = "rosterJID";
    public static final String COLUMN_CONVERSATION_MESSAGE_ID = "messageId";
    public static final String COLUMN_CONVERSATION_MESSAGE = "message";
    public static final String COLUMN_CONVERSATION_IS_ME = "isMe";

    public static final String TABLE_ROSTER = "rosterEntry";
    public static final String COLUMN_ROSTER_DB_ID = "id";
    public static final String COLUMN_ROSTER_ROSTER_JID = "rosterJid";
    public static final String COLUMN_ROSTER_NAME = "rosterName";

    public MyDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL("create table " + TABLE_CONVERSATION + "("
                + COLUMN_CONVERSATION_DB_ID + " integer primary key autoincrement, "
                + COLUMN_CONVERSATION_ROSTER_ID + " text, "
                + COLUMN_CONVERSATION_MESSAGE_ID + " text, "
                + COLUMN_CONVERSATION_MESSAGE + " text, "
                + COLUMN_CONVERSATION_IS_ME + " integer" + ");"
        );
        db.execSQL("create table " + TABLE_ROSTER + "("
                + COLUMN_ROSTER_DB_ID + " integer primary key autoincrement, "
                + COLUMN_ROSTER_ROSTER_JID + " text, "
                + COLUMN_ROSTER_NAME + " text" + ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONVERSATION + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROSTER + ";");
        onCreate(db);
    }

    /**
     * For TABLE_CONVERSATION
     *
     * @param messageBean This method is used to insert a Message in Database
     */
    public void insertConversationMessage(MessageBean messageBean) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_CONVERSATION_ROSTER_ID, messageBean.rosterId);
        contentValues.put(COLUMN_CONVERSATION_MESSAGE_ID, messageBean.message);
        contentValues.put(COLUMN_CONVERSATION_MESSAGE, messageBean.message);
        contentValues.put(COLUMN_CONVERSATION_IS_ME, messageBean.isMe ? 1 : 0);
        db.insert(TABLE_CONVERSATION, null, contentValues);
        db.close();
    }

    /**
     * For TABLE_CONVERSATION
     *
     * @param roster_id
     * @return Gives the entire conversation of a particular roster_jid
     */
    public List<MessageBean> getConversationList(String roster_id) {
        List<MessageBean> conversationList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CONVERSATION + " WHERE " + COLUMN_CONVERSATION_ROSTER_ID + " = ?", new String[]{roster_id});
        if (cursor.moveToFirst()) {
            do {
                conversationList.add(new MessageBean(
                        roster_id,
                        cursor.getString(cursor.getColumnIndex(COLUMN_CONVERSATION_MESSAGE_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_CONVERSATION_MESSAGE)),
                        (cursor.getInt(cursor.getColumnIndex(COLUMN_CONVERSATION_IS_ME)) == 1)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return conversationList;
    }

    /**
     * For TABLE_ROSTER
     *
     * @param rosterList This method is used to insert a Roster in Database
     */
    public boolean insertRoster(List<RosterEntry> rosterList) {
        long isDataInserted = 0;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues;
        for (RosterEntry rosterEntry : rosterList) {
            if ((db.rawQuery("SELECT * FROM " + TABLE_ROSTER + " WHERE " + COLUMN_ROSTER_ROSTER_JID + " = ?", new String[]{rosterEntry.getUser()}).getCount()) != 0)
                continue;
            contentValues = new ContentValues();
            contentValues.put(COLUMN_ROSTER_ROSTER_JID, rosterEntry.getUser());
            contentValues.put(COLUMN_ROSTER_NAME, rosterEntry.getName());
            isDataInserted = db.insert(TABLE_ROSTER, null, contentValues);
        }
        db.close();
        return (isDataInserted != 0);
    }

    /**
     * For TABLE_ROSTER
     *
     * @return Gives the Roster List
     */
    public List<RosterBean> getRosterList() {
        List<RosterBean> rosterList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ROSTER, null);
        if (cursor.moveToFirst()) {
            do {
                rosterList.add(new RosterBean(
                        cursor.getString(cursor.getColumnIndex(COLUMN_ROSTER_ROSTER_JID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_ROSTER_NAME))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return rosterList;
    }
}