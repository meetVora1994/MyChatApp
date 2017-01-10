package app.chat.meet.mychatapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MyIntentService extends IntentService {

    static AbstractXMPPConnection mConnection= null;
    private static final String ACTION_FOO = "app.chat.meet.mychatapp.action.FOO";
    public static final String BROADCAST_ACTION_GOT_MESSAGE = "app.chat.meet.GOT_MESSAGE";
    public static final String BROADCAST_DATA_GOT_MESSAGE = "messageBean";


    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_BAZ = "app.chat.meet.mychatapp.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "app.chat.meet.mychatapp.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "app.chat.meet.mychatapp.extra.PARAM2";

    public MyIntentService() {
        super("MyIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startMessageReceiverService(Context context, AbstractXMPPConnection connection) {
        mConnection = connection;
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_FOO);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                mConnection.addAsyncStanzaListener(new StanzaListener() {
                    @Override
                    public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                        Message message = (Message) packet;
                        Log.i("myTAG", "RECEIVED FROM: " + message.getFrom());
                        Log.i("myTAG", "RECEIVED BODY: " + message.getBody());
                        Log.i("myTAG", "RECEIVED TYPE: " + message.getType());

                        MessageBean messageBean = new MessageBean(
                                message.getFrom().substring(0, message.getFrom().lastIndexOf("/")),
                                message.getStanzaId(),
                                message.getBody(),
                                false
                        );
                        handleActionFoo(messageBean);

                    }
                }, MessageTypeFilter.CHAT);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(MessageBean messageBean) {
        new MyDatabase(getApplicationContext()).insertConversationMessage(messageBean);
        Intent localIntent = new Intent(BROADCAST_ACTION_GOT_MESSAGE)
                .putExtra(BROADCAST_DATA_GOT_MESSAGE, new Gson().toJson(messageBean));
        LocalBroadcastManager.getInstance(MyIntentService.this).sendBroadcast(localIntent);
    }
}
