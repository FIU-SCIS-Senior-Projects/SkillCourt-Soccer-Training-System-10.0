package fiu.com.skillcourt.structures;

import android.util.Log;

/**
 * Created by Joshua Mclendon on 11/29/17.
 */
public class Game extends Thread {
    private static final String TAG = Game.class.getName();
    private boolean isHit = false;
    private long gameTime;
    private int padLightUpTime, padLightUpTimeDelay;
    private Sequence sequence;

    public Game(Sequence sequence, long gameTime, int padLightUpTime, int padLightUpTimeDelay) {
        this.gameTime = gameTime;
        this.padLightUpTime = padLightUpTime;
        this.padLightUpTimeDelay = padLightUpTimeDelay;
        this.sequence = sequence;
    }


    public void setIsHit(boolean isHit) {
        this.isHit = isHit;
    }

    public Sequence getSequence() {

        return sequence;
    }

    @Override
    public void interrupt() {
        super.interrupt();
        for (Pad pad : sequence.getPads()) {
            pad.turnOff();
            Log.i(TAG, "Sending game over");
        }
    }

    @Override
    public void run() {
        for (Pad pad : sequence.getPads()) {
            pad.startGame(); //send a game is starting so start sensor reading
        }
        long startTime = System.currentTimeMillis();

        while (gameTime > System.currentTimeMillis() - startTime && !Thread.interrupted()) {
            isHit = false;
            sequence.next();
            //if the player specified a time that the pad stays lit
            if (padLightUpTime != 0) {
                long startLightUpTime = System.currentTimeMillis();
                //basically loop for the amount of time in seconds (ex. 5 secs) and if the pad isn't hit
                while (padLightUpTime > System.currentTimeMillis() - startLightUpTime && !isHit) {
                    //if the time of the game runs out then the game is over
                    //so interrupt if it hasn't already been interrupted
                    if (!(gameTime > System.currentTimeMillis() - startTime) && !Thread.currentThread().isInterrupted()) {
                        Thread.currentThread().interrupt();
                    }
                }
                //if there was a light up time and it expired before the pad was hit then turn it off
                //since the pad only turns off if hit or when we tell it too
                if (!isHit) {
                    sequence.getCurrentLitPad().turnOff();
                }
            } else {
                //loop until a hit is registered or the game is over
                while (!isHit) {
                    if (!(gameTime > System.currentTimeMillis() - startTime) && !Thread.currentThread().isInterrupted()) {
                        Thread.currentThread().interrupt();
                    }
                }

            }
            //if we are in memory game mode then also turn off the pad that is next to be lit
            if (sequence.getNextLitPad() != null) {
                sequence.getNextLitPad().turnOff();
            }
            //if there is a delay time then loop for the amount of time in seconds (ex. 2 secs)
            if (padLightUpTimeDelay != 0) {
                long startLightUpDelayTime = System.currentTimeMillis();
                while (padLightUpTimeDelay > System.currentTimeMillis() - startLightUpDelayTime) {
                    //if the time of the game runs out then the game is over
                    //so interrupt if it hasn't already been interrupted
                    if (!(gameTime > System.currentTimeMillis() - startTime) && !Thread.currentThread().isInterrupted()) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

    }


}