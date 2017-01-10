package app.chat.meet.mychatapp;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChatActivity extends BaseActivity {

    String rosterJID = "", rosterName;
    EditText etMessage;
    RecyclerView rvConversation;
    List<MessageBean> listConversation = new ArrayList<>();
    AdapterListConversation mAdapterListConversation;
    MyDatabase myDb;
    LinearLayoutManager mLinearLayoutManager;
    ActionBar actionBar;
    RosterListener rosterListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (!getIntent().hasExtra(Config.roster_jid))
            finish();

        myDb = new MyDatabase(getApplicationContext());
        rosterJID = getIntent().getExtras().get(Config.roster_jid).toString();

        if(getIntent().hasExtra(Config.roster_name) && getIntent().getExtras().get(Config.roster_name)!=null) {
            rosterName = getIntent().getExtras().get(Config.roster_name).toString();
        }

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle((rosterName != null) ?rosterName:rosterJID);
            Roster roster = Roster.getInstanceFor(connection);
            Presence presence = roster.getPresence(rosterJID);
            actionBar.setSubtitle(presence.getStatus());
        }
        etMessage = (EditText) findViewById(R.id.etMessage);
        rvConversation = (RecyclerView) findViewById(R.id.rvConversation);
        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvConversation.setLayoutManager(mLinearLayoutManager);

        //To scroll RecyclerView to bottom while keyboard opens
        mLinearLayoutManager.setStackFromEnd(true);

        listConversation = myDb.getConversationList(rosterJID);

        mAdapterListConversation = new AdapterListConversation();
        rvConversation.setAdapter(mAdapterListConversation);
        rvConversation.scrollToPosition(mAdapterListConversation.getItemCount()-1);

        rosterListener = new RosterListener() {
            @Override
            public void entriesAdded(Collection<String> addresses) {
                Log.d("myTAG", "[RosterListener] entriesAdded(): " + addresses.toString());
            }

            @Override
            public void entriesUpdated(Collection<String> addresses) {
                Log.d("myTAG", "[RosterListener] entriesUpdated(): " + addresses.toString());
            }

            @Override
            public void entriesDeleted(Collection<String> addresses) {
                Log.d("myTAG", "[RosterListener] entriesDeleted(): " + addresses.toString());
            }

            @Override
            public void presenceChanged(final Presence presence) {
                Log.d("myTAG", "[RosterListener] presenceChanged(): " + presence.getStatus());

                ChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!(presence.getFrom().substring(0, presence.getFrom().lastIndexOf("/"))).equals(rosterJID))
                            return;
                        actionBar.setSubtitle(presence.getStatus());
                    }
                });
            }
        };

        super.connectToServer();
    }

    /**
     * onClick method for SEND button
     */
    public void sendMessage(View view) {
        String text = etMessage.getText().toString();
        if (TextUtils.isEmpty(text))
            return;
        try {
            Log.i("myTAG", "Sending text [" + text + "] to [" + rosterJID + "]");
            Message msg = new Message(rosterJID, Message.Type.chat);
            msg.setBody(text);
            connection.sendStanza(msg);
            MessageBean messageBean = new MessageBean(rosterJID, "", text, true);
            listConversation.add(messageBean);
            mAdapterListConversation.notifyItemInserted(listConversation.size());
            rvConversation.scrollToPosition(mAdapterListConversation.getItemCount()-1);
            myDb.insertConversationMessage(messageBean);
            etMessage.setText("");
        } catch (SmackException.NotConnectedException e) {
            Toast.makeText(ChatActivity.this, "Unable to connect at moment, please try again later!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void gotMessage(MessageBean messageBean) {
        if (!messageBean.rosterId.equals(rosterJID))
            return;
        listConversation.add(messageBean);
        mAdapterListConversation.notifyItemInserted(listConversation.size());
        rvConversation.scrollToPosition(mAdapterListConversation.getItemCount()-1);
    }

    @Override
    public void connected(AbstractXMPPConnection connection) {
        Roster.getInstanceFor(connection).addRosterListener(rosterListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Roster.getInstanceFor(connection).removeRosterListener(rosterListener);
    }

    private class AdapterListConversation extends RecyclerView.Adapter<AdapterListConversation.ViewHolderRoster> {

        Drawable bubble_right, bubble_left;

        public AdapterListConversation() {
            bubble_right = getApplicationContext().getResources().getDrawable(R.drawable.bubble_right);
            bubble_left = getApplicationContext().getResources().getDrawable(R.drawable.bubble_left);
        }

        @Override
        public ViewHolderRoster onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            return new ViewHolderRoster(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_message, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolderRoster holder, final int position) {
            if (listConversation.get(position).isMe) {
                holder.tvMessageLeft.setVisibility(View.GONE);
                holder.tvMessageRight.setVisibility(View.VISIBLE);
                holder.tvMessageRight.setText(listConversation.get(position).message);
            } else {
                holder.tvMessageRight.setVisibility(View.GONE);
                holder.tvMessageLeft.setVisibility(View.VISIBLE);
                holder.tvMessageLeft.setText(listConversation.get(position).message);
            }

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return listConversation.size();
        }

        public class ViewHolderRoster extends RecyclerView.ViewHolder {
            TextView tvMessageLeft, tvMessageRight;

            public ViewHolderRoster(View itemView) {
                super(itemView);
                tvMessageLeft = (TextView) itemView.findViewById(R.id.tvMessageLeft);
                tvMessageRight = (TextView) itemView.findViewById(R.id.tvMessageRight);
            }
        }
    }
}
