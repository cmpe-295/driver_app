package spartansaferide.sjsu.edu.driver;

import android.app.Service;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

/**
 * Created by priyanka.tummala on 10/18/16.
 */
public class AndroidFirebaseMessageService extends FirebaseMessagingService {

    private static final String TAG = "SafeRideMsgService";
    DriverMapsActivity driverMaps;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Log data to Log Cat
//        Log.d(TAG, "From: " + remoteMessage.getFrom());
//        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        //create notification
        createNotification(remoteMessage.getNotification().getBody());
        String notification_body = remoteMessage.getNotification().getBody();

        Notification.getInstance().message=notification_body;
    }



    private void createNotification( String messageBody) {
        Intent intent = new Intent( this , DriverMapsActivity.class );
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent resultIntent = PendingIntent.getActivity( this , 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder( this)
                .setSmallIcon(R.mipmap.spartan_logo)
                .setContentTitle("SafeRide")
                .setContentText("You have a new ride!")
                .setAutoCancel( true )
                .setSound(notificationSoundURI)
                .setContentIntent(resultIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, mNotificationBuilder.build());
    }


    public interface SetRoute{
        public void parseNotifcation(String N);
    }

}
