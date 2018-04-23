package fiu.com.skillcourt.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import fiu.com.skillcourt.structures.Game;
import fiu.com.skillcourt.structures.Player;
import fiu.com.skillcourt.structures.Sequence;

/**
 * Created by Joshua Mclendon on 11/29/17.
 */

public class GameService extends Service {


    private static final String TAG = GameService.class.getName();
    private static final String PAD_HIT = "PAD_HIT";
    private static final String PAD_HIT_UUID = "PAD_HIT_UUID";
    private static final String END_GAME_SERVICE = "END_GAME_SERVICE";
    private ConnectionService mConnectionService;
    private List<Player> players;
    private List<Game> games = new CopyOnWriteArrayList<>();
    private ArrayList<Integer> sequence = new ArrayList<>();
    private String mGameMode, mGameType;
    private long mGameTime;
    private int mPadLightUpTime;
    private int mPadLightUpTimeDelay;
    private GameReceiver gameReceiver;
    private IBinder mBinder = new LocalBinder();


    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    @Override
    public void onCreate() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PAD_HIT);
        intentFilter.addAction(END_GAME_SERVICE);
        gameReceiver = new GameReceiver();
        registerReceiver(gameReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!games.isEmpty()) {
            for (Game game : games) {
                if (!game.isInterrupted()) {
                    game.interrupt();
                }
            }
        }
        games.clear();
        unregisterReceiver(gameReceiver);
        Log.i(TAG, "onDestroy");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    public void startGame() {
        mConnectionService.nsdConnection.tearDown(); //stop nsd broadcast
        if (mGameType.equalsIgnoreCase("Multiplayer")) {

        } else {
            Sequence seq;
            if (mGameMode.equalsIgnoreCase("Random")) {
                seq = new Sequence(mGameMode, mGameType, mConnectionService.getActivePads(), players);
            } else {
                seq = new Sequence(sequence, mGameMode, mGameType, mConnectionService.getActivePads(), players);
            }

            Game game = new Game(seq, mGameTime, mPadLightUpTime, mPadLightUpTimeDelay);
            games.add(game);
            game.start();
        }
    }

    private void endGame() {
        Log.i(TAG, "Game Over");

        for (Game game : games) {
            game.interrupt();
        }
        for (Player player : players) {
            Log.i(TAG, "Player " + player.getPlayerId() + " Hit count: " + player.getHitCount());
            Log.i(TAG, "Player " + player.getPlayerId() + " Miss count: " + player.getMissCount());

            player.setTotalCount(player.getHitCount() + player.getMissCount());
            if (player.getTotalCount() != 0) {
                player.setHitPercentage((player.getHitCount() / player.getTotalCount()) * 100);
                player.setMissPercentage((player.getMissCount() / player.getTotalCount()) * 100);
            }

            Log.i(TAG, "Hit Percentage: " + player.getHitPercentage() + "%");
            Log.i(TAG, "Miss Percentage: " + player.getMissPercentage() + "%");
            Log.i(TAG, "Total: " + player.getTotalCount());
            Log.i(TAG, "Total Points: " + player.getTotalPoints());
        }
        mConnectionService.nsdConnection.registerService(mConnectionService.getLocalPort());
    }


    public void setConnectionService(ConnectionService connectionService) {
        mConnectionService = connectionService;
    }

    public void setGameMode(String gameMode) {
        mGameMode = gameMode;
    }

    public void setGameType(String gameType) {
        mGameType = gameType;
    }

    public void setSequence(ArrayList<Integer> sequence) {
        this.sequence = sequence;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public long getGameTime() {
        return mGameTime;
    }

    public void setGameTime(long gameTime) {
        mGameTime = gameTime * 1000;
    }

    public void setPadLightUpTime(double padLightUpTime) {
        mPadLightUpTime = (int) padLightUpTime * 1000;
    }

    public void setPadLightUpTimeDelay(double padLightUpTimeDelay) {
        mPadLightUpTimeDelay = (int) padLightUpTimeDelay * 1000;
    }


    //So activities can access the methods inside this service
    public class LocalBinder extends Binder {
        public GameService getService() {
            return GameService.this;
        }
    }


    private class GameReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //if a pad was hit then see if it was the correct pad by checking the uuid that sent
            //the hit broadcast vs the uuid that was set after sending the light up command in game
            if (intent.getAction().equals(PAD_HIT)) {
                Log.i(TAG, "Hit received");
                String key = "Pad " + intent.getStringExtra(PAD_HIT_UUID);

                for (Game game : games) {
                    if (game.getSequence().getUUIDs().contains(intent.getStringExtra(PAD_HIT_UUID))) {
                        if (game.getSequence().getCurrentActiveUUID().equals(intent.getStringExtra(PAD_HIT_UUID))) {
                            for (Player player : game.getSequence().getPlayers()) {
                                player.addHit();
                                player.addPoints();
                            }
                            Intent intentPadHit = new Intent();
                            intentPadHit.setAction("PAD_HIT_UI_COUNT_CHANGE"); //WHEN DOES IT CHANGE HARDWARE PAD COLOR
                            sendBroadcast(intentPadHit);
                            game.setIsHit(true);

                        } else {
                            for (Player player : game.getSequence().getPlayers()) {
                                player.addMiss();
                                player.removePoints();
                            }
                            Intent intentPadMiss = new Intent();
                            intentPadMiss.setAction("PAD_MISS_UI_COUNT_CHANGE");
                            sendBroadcast(intentPadMiss);
                        }
                        break;
                    }


                }


            } else if (intent.getAction().equals(END_GAME_SERVICE)) {
                endGame();
                stopSelf();
            }

        }
    }
}
