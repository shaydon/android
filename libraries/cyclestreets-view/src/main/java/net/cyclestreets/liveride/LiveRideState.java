package net.cyclestreets.liveride;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.LiveRideActivity;
import net.cyclestreets.view.R;
import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Segment;

import org.osmdroid.util.GeoPoint;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.speech.tts.TextToSpeech;

public abstract class LiveRideState
{
  private static final int NOTIFICATION_ID = 1;
  
  static public LiveRideState InitialState(final Context context) 
  { 
    final TextToSpeech tts = new TextToSpeech(context, 
          new TextToSpeech.OnInitListener() { public void onInit(int arg0) { } }
    );
    final ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, ToneGenerator.MAX_VOLUME);
    return new LiveRideStart(context, tts, toneGenerator);
  } // InitialState
  
  static public LiveRideState StoppedState(final Context context) 
  { 
    return new Stopped(context); 
  } // StoppedState
  //////////////////////////////////////////
  
  private Context context_;
  private TextToSpeech tts_;
  private ToneGenerator toneGenerator_;
  
  protected LiveRideState(final Context context, final TextToSpeech tts, final ToneGenerator toneGenerator)
  {
    log("Created " + this.getClass().getName());
    context_ = context;
    tts_ = tts;
    toneGenerator_ = toneGenerator;
  } // LiveRideState
  
  protected LiveRideState(final LiveRideState state) 
  {
    log("Created " + this.getClass().getName());
    context_ = state.context();
    tts_ = state.tts();
    toneGenerator_ = state.toneGenerator();
  } // LiveRideState

  protected void log(String msg)
  {
    android.util.Log.d("CYCLESTREETS", msg);
  }

  public abstract LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy);
  public abstract boolean isStopped();
  public abstract boolean arePedalling();
  
  protected Context context() { return context_; }
  protected TextToSpeech tts() { return tts_; }
  protected ToneGenerator toneGenerator() { return toneGenerator_; }

  protected void appendTurnAndStreet(final StringBuilder text, final Segment seg)
  {
    if(seg.turn().length() != 0)
      text.append(seg.turn()).append(" into ");
    text.append(speakableStreet(seg));
  }

  protected String speakableStreet(Segment seg)
  {
    return seg.street().replace("un-", "un").replace("Un-", "un");
  }

  protected void notify(final Segment seg) 
  {
    notification(seg.street() + " " + seg.distance(), seg.toString());
    
    final StringBuilder instruction = new StringBuilder();
    appendTurnAndStreet(instruction, seg);
    if(CycleStreetsPreferences.verboseVoiceGuidance())
    {
      if(!seg.distance().trim().equals(""))
      {
        instruction.append(". Continue ").append(seg.distance());
      }
      instruction.append(".");
    }
    else {
        instruction.append(". Continue ").append(seg.distance());
    }
    speak(instruction.toString());
  } // notify
  
  protected void notify(final String text)
  {
    notify(text, text);
  } // notify
  
  protected void notify(final String text, final String ticker) 
  {
    notification(text, ticker);
    speak(text);
  } // notify
  
  private void notification(final String text, final String ticker)
  {
    final NotificationManager nm = nm();
    final Notification notification = new Notification(R.drawable.icon, ticker, System.currentTimeMillis());
    notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;
    final Intent notificationIntent = new Intent(context(), LiveRideActivity.class);
    final PendingIntent contentIntent = PendingIntent.getActivity(context(), 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    notification.setLatestEventInfo(context(), "CycleStreets", text, contentIntent);
    nm.notify(NOTIFICATION_ID, notification);
  } // notify

  protected void cancelNotification()
  {
    nm().cancel(NOTIFICATION_ID);
  } // cancelNotification

  private NotificationManager nm()
  {
    return (NotificationManager)context().getSystemService(Context.NOTIFICATION_SERVICE);
  } // nm

  private void speak(final String words)
  {
    log("Speech: " + words);
    tts().speak(words, TextToSpeech.QUEUE_ADD, null);
  } // speak

  protected void playTone(final int toneType, final int durationMs)
  {
    toneGenerator().startTone(toneType, durationMs);
  }
} // interface LiveRideState

