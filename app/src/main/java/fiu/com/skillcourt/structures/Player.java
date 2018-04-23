package fiu.com.skillcourt.structures;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Joshua Mclendon on 2/18/2018.
 */

public class Player {
    private int mPlayerId;
    private int mTotalCount;
    private int mHitPercentage;
    private int mMissPercentage;
    private int mHitCount;
    private int mMissCount;
    private int mTotalPoints;
    private ConcurrentHashMap<String, Integer> padHits = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> padMisses = new ConcurrentHashMap<>();
    private String mHitColor;

    public Player(int playerId, String hitColor) {
        mPlayerId = playerId;
        mHitColor = hitColor;
        resetStats();
    }

    public void resetStats() {
        mTotalCount = 0;
        mHitPercentage = 0;
        mMissPercentage = 0;
        mHitCount = 0;
        mMissCount = 0;
        mTotalPoints = 0;

    }

    public int getPlayerId() {
        return mPlayerId;
    }

    public int getTotalCount() {
        return mTotalCount;
    }

    public void setTotalCount(int mTotalCount) {
        this.mTotalCount = mTotalCount;
    }

    public int getHitPercentage() {
        return mHitPercentage;
    }

    public void setHitPercentage(int mHitPercentage) {
        this.mHitPercentage = mHitPercentage;
    }

    public int getMissPercentage() {
        return mMissPercentage;
    }

    public void setMissPercentage(int mMissPercentage) {
        this.mMissPercentage = mMissPercentage;
    }

    public int getHitCount() {
        return mHitCount;
    }

    public int getMissCount() {
        return mMissCount;
    }

    public void addHit() {
        mHitCount++;
    }

    public void addMiss() {
        mMissCount++;
    }

    public int getTotalPoints() {
        return mTotalPoints;
    }

    public void addPoints() {
        mTotalPoints += 5;
    }

    public void removePoints() {
        mTotalPoints -= 2;
    }

    public ConcurrentHashMap<String, Integer> getPadHits() {
        return padHits;
    }

    public void setPadHits(ConcurrentHashMap<String, Integer> padHits) {
        this.padHits = padHits;
    }

    public ConcurrentHashMap<String, Integer> getPadMisses() {
        return padMisses;
    }

    public void setPadMisses(ConcurrentHashMap<String, Integer> padMisses) {
        this.padMisses = padMisses;
    }

    public String getHitColor() {
        return mHitColor;
    }

    public void setHitColor(String hitColor) {
        mHitColor = hitColor;
    }

    public String getMemoryHitColor() {
        return mHitColor;
    }

    public void setMemoryHitColor(String hitColor) {
        mHitColor = hitColor;
    }
}