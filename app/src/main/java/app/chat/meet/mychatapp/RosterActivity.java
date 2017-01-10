package app.chat.meet.mychatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RosterActivity extends BaseActivity {

    RecyclerView rvRosterList;
    LinearLayoutManager mLinearLayoutManager;
    List<RosterBean> rosterList = new ArrayList<>();
    AdapterRosterList mAdapterRosterList;
    MyDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roster);

        db = new MyDatabase(getApplicationContext());

        rvRosterList = (RecyclerView) findViewById(R.id.rvRosterList);
        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvRosterList.setHasFixedSize(true);
        rvRosterList.setLayoutManager(mLinearLayoutManager);
        mAdapterRosterList = new AdapterRosterList();

        // Fetching cached Roster list from Database
        rosterList = db.getRosterList();
        mAdapterRosterList.notifyDataSetChanged();

        rvRosterList.setAdapter(mAdapterRosterList);

        super.connectToServer();
    }

    @Override
    public void connected(AbstractXMPPConnection connection) {
        Roster roster = Roster.getInstanceFor(connection);
        Log.d("myTAG", "Roster Entries: " + roster.getEntries().toString());
        if(db.insertRoster(new ArrayList<>(roster.getEntries()))){
            rosterList = new ArrayList<>();
            rosterList = db.getRosterList();
            mAdapterRosterList.notifyDataSetChanged();
        }
    }

    @Override
    public void gotMessage(MessageBean messageBean) {
//        if (!messageBean.rosterJID.equals(rosterJID))
//            return;
        Log.d("myTAG", "gotMessage() returned: " + messageBean.rosterId);

    }

    private class AdapterRosterList extends RecyclerView.Adapter<AdapterRosterList.ViewHolderRoster> {

        @Override
        public ViewHolderRoster onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            return new ViewHolderRoster(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_roster, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolderRoster holder, int position) {
            final RosterBean item = rosterList.get(position);
            holder.tvName.setText(
                    (item.rosterName != null) ? (item.rosterName) : (rosterList.get(position).rosterJID)
            );
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getApplicationContext(), ChatActivity.class);
                    i.putExtra(Config.roster_jid, item.rosterJID);
                    i.putExtra(Config.roster_name, item.rosterName);
                    startActivity(i);
                }
            });
        }

        @Override
        public int getItemCount() {
            return rosterList.size();
        }

        public class ViewHolderRoster extends RecyclerView.ViewHolder {
            TextView tvName;

            public ViewHolderRoster(View itemView) {
                super(itemView);
                tvName = (TextView) itemView.findViewById(R.id.tvName);
            }
        }
    }
}
