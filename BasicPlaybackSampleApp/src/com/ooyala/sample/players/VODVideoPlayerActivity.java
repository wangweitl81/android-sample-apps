package com.ooyala.sample.players;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.PlayerDomain;
import com.ooyala.android.ui.OoyalaPlayerLayoutController;
import com.ooyala.sample.R;

/**
 * This activity illustrates how you can play VOD with CCs
 * through the SDK
 *
 */
public class VODVideoPlayerActivity extends Activity implements Observer {
  public final static String getName() {
    return "VOD with CCs";
  }
  final String TAG = this.getClass().toString();

  String EMBED = null;
  String EMBED_TWO = "92cWp0ZDpDm4Q8rzHfVK6q9m6OtFP-ww";
  final String PCODE  = "R2d3I6s06RyB712DN0_2GsQS-R-Y";
  final String DOMAIN = "http://ooyala.com";

  protected OoyalaPlayerLayoutController playerLayoutController;
  protected OoyalaPlayer player;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTitle(getName());
    setContentView(R.layout.player_simple_layout);

    EMBED = getIntent().getExtras().getString("embed_code");

    //Initialize the player
    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    playerLayoutController = new OoyalaPlayerLayoutController(playerLayout, PCODE, new PlayerDomain(DOMAIN));
    player = playerLayoutController.getPlayer();
    player.addObserver(this);

    if (player.setEmbedCode(EMBED)) {
      player.play();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    Log.d(TAG, "Player Activity Stopped");
    if (player != null) {
      player.suspend();
    }
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    Log.d(TAG, "Player Activity Restarted");
    if (player != null) {
      player.resume();
    }
  }

  /**
   * Listen to all notifications from the OoyalaPlayer
   */
  @Override
  public void update(Observable arg0, Object arg1) {
    if (arg1 == OoyalaPlayer.TIME_CHANGED_NOTIFICATION) {
      return;
    }
    Log.d(TAG, "Notification Recieved: " + arg1 + " - state: " + player.getState());
  }

}