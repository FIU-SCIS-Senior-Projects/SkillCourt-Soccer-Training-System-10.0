package fiu.com.skillcourt.structures;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;


/**
 * Created by Sandra Lucia Hurtado Elias on 2/21/18.
 */

public class Sequence {
    private static final String TAG = Sequence.class.getName();
    private Deque<Integer> sequence;
    private List<Pad> pads;
    private List<Player> players;
    private Pad currentLitPad;
    private Pad nextPad = null;
    private String gameMode, gameType;
    private String mCurrentActiveUUID;
    private String lastUsedColor;
    private int mCurrentPadLit = -1;
    private int mLastPadLit = -1;
    private int mNextPadLit = -1;


    public Sequence(List<Integer> sequence, String gameMode, String gameType, List<Pad> pads, List<Player> players) {
        this.sequence = new ArrayDeque<>(sequence);
        this.pads = pads;
        this.players = players;
        this.gameMode = gameMode;
        this.gameType = gameType;

        for (Player player : players) {
            player.resetStats();
        }
    }

    //Specifically for random game mode since we don't need to pass in a sequence list
    public Sequence(String gameMode, String gameType, List<Pad> pads, List<Player> players) {
        this.pads = pads;
        this.players = players;
        this.gameMode = gameMode;
        this.gameType = gameType;

        for (Player player : players) {
            player.resetStats();
        }
    }

    public void next() {
        switch (gameMode) {
            case "Random":

                if (pads.size() > 2) {
                    //check to make sure the same pad doesn't light up twice in a row
                    while (true) {
                        mCurrentPadLit = (int) (Math.random() * pads.size());
                        if (mCurrentPadLit == mLastPadLit) continue;
                        Log.i(TAG, "Last Light up #" + mLastPadLit);
                        Log.i(TAG, "Light up #" + mCurrentPadLit);
                        mLastPadLit = mCurrentPadLit;
                        break;

                    }

                } else {
                    mCurrentPadLit = (int) (Math.random() * pads.size());
                    Log.i(TAG, "Light up #" + mCurrentPadLit);
                }

                //mNextPadLit = (int) (Math.random() * pads.size());
                Log.i(TAG, "Next light up #" + mNextPadLit);
                break;
            case "Sequence":
                //remove from queue then add it back to the end for reuse
                mCurrentPadLit = sequence.poll();
                addToSequence(mCurrentPadLit);
                mCurrentPadLit--; //so no index out of bounds exceptions when getting from the list
                Log.i(TAG, "Light up #" + mCurrentPadLit);
                break;
            case "Memory":
                //remove the first from the queue then the second
                mCurrentPadLit = sequence.poll();
                mNextPadLit = sequence.poll();
                //add the first removed to the end but add the second removed
                //to the front of the queue because it's suppose to be the next pad to be lit
                addToSequence(mCurrentPadLit);
                addToFront(mNextPadLit);
                mCurrentPadLit--;//so no index out of bounds exceptions when getting from the list
                mNextPadLit--;//so no index out of bounds exceptions when getting from the list

                Log.i(TAG, "Light up #" + mCurrentPadLit);
                Log.i(TAG, "Next light up #" + mNextPadLit);
                break;
        }

        //if the first removed and second removed are not the same number then we need to
        //light the nextPad otherwise it's the same pad to be lit next so no need to try and light it twice
        if (mNextPadLit != -1 && mCurrentPadLit != mNextPadLit) {
            nextPad = pads.get(mNextPadLit);
        } else {
            nextPad = null;
        }
        currentLitPad = pads.get(mCurrentPadLit);

        //if we are in duo game type then alternate the lighting of the player's colors
        //for example player 1 can have red while player 2 can have green
        if (gameType.equalsIgnoreCase("Duo")) {
            if (lastUsedColor.equalsIgnoreCase(players.get(0).getHitColor())) {
                lastUsedColor = players.get(1).getHitColor();
                currentLitPad.turnOn();
                //this is for is its duo game type but memory game mode because we will also have a
                //specific color for "next pad to be lit" that's different from the normal
                //player lighting colors
                if (nextPad != null) {
                    long startTime = System.currentTimeMillis();
                    nextPad.turnOn();
                }
            } else {
                lastUsedColor = players.get(0).getHitColor();
                currentLitPad.turnOn();
                //this is for is its duo game type but memory game mode because we will also have a
                //specific color for "next pad to be lit" that's different from the normal
                //player lighting colors
                if (nextPad != null) {
                    nextPad.turnOn(players.get(1).getMemoryHitColor());
                }
            }
        } else {
            //if not duo game type then just turn the pad on normally with no alternating colors
            //since it'll either be a single player mode (one color) or multiplayer where everyone
            //has their own color therefore they'll have their own game thread as well
            currentLitPad.turnOn();
            //for memory game mode
            if (nextPad != null) {
                long startTime = System.currentTimeMillis();
                nextPad.turnOn();
            }
        }
        mCurrentActiveUUID = currentLitPad.getUuid();

    }

    public void reset() {
        mLastPadLit = -1;
        mCurrentPadLit = -1;
        mNextPadLit = -1;
    }

    public List<Pad> getPads() {
        return pads;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Pad getCurrentLitPad() {
        return currentLitPad;
    }


    public Pad getNextLitPad() {
        return nextPad;
    }

    public String getGameMode() {
        return gameMode;
    }

    public List<String> getUUIDs() {

        List<String> UUIDs = new ArrayList<>();
        for (Pad pad : getPads()) {
            UUIDs.add(pad.getUuid());
        }
        return UUIDs;
    }

    public String getCurrentActiveUUID() {
        return mCurrentActiveUUID;
    }

    public void addToSequence(Integer num) {
        sequence.add(num);
    }

    public void addToFront(Integer num) {
        sequence.addFirst(num);
    }
}
