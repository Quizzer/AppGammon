package com.doneu.backgammoncounter.beans;

import android.util.Log;

import com.doneu.backgammoncounter.utils.Constants;
//import com.doneu.backgammoncounter.utils.Constants.Increase;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by doneu on 18.05.17.
 */

public class Game extends RealmObject {

    @PrimaryKey
    private int id;
    private Challenge challenge;
    private long timestamp_start;
    private long timestamp_end;
    private int points;
    private boolean doubling; // Todo Refactor Enum: Doubling, Trebling, Simple [Multiplication]
    private boolean trebling;
    private boolean forMe; // Todo Refactor Enum: Open, ForMe, ForOpp [Status]
    private boolean closed;
    private int increase;

    public Game() {
    }

    public Game(Realm realm, Challenge challenge) {
        this.id = this.getNextId(realm);
        this.challenge = challenge;
        this.timestamp_start = System.currentTimeMillis();
        this.timestamp_end = this.timestamp_start;
        this.points = Constants.POINTS_AT_START;
        this.closed = false;
    }

    private int getNextId(Realm realm) {
        try {
            if (realm.where(Game.class).findAll().size() > 0) {
                return realm.where(Game.class).max("id").intValue() + 1;
            } else
                return 0;
        } catch(ArrayIndexOutOfBoundsException ex) {
            return 0;
        }
    }

    public static Game getByPrimaryKey(Realm realm, int id) {
        return realm.where(Game.class).equalTo("id", id).findFirst();
    }

    public void setIncrease(int increase) {
        this.increase = increase;
    }

    public void setIncrease(Realm realm, int increase) {
        realm.beginTransaction();
        this.increase = increase;
        realm.commitTransaction();
    }

    public int getId() {
        return id;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    private void setTimestamp_end(long timestamp_end) {
        this.timestamp_end = timestamp_end;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setDoubling(boolean doubling) {
        this.doubling = doubling;
    }

    public void setTrebling(boolean trebling) {
        this.trebling = trebling;
    }

    private void setForMe(boolean forMe) {
        this.forMe = forMe;
    }

    private void setClosed(boolean closed) {
        this.closed = closed;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public long getTimestamp_start() {
        return timestamp_start;
    }

    public long getTimestamp_end() {
        return timestamp_end;
    }

    public int getPoints() {
        if (this.isClosed()) {
            int pointsToAdd = this.points;
            pointsToAdd = this.isDoubling()? pointsToAdd*2 : pointsToAdd;
            pointsToAdd = this.isTrebling()? pointsToAdd*3 : pointsToAdd;
            return pointsToAdd;
        } else {
            return points;
        }
    }

    public boolean isDoubling() {
        return doubling;
    }

    public boolean isTrebling() {
        return trebling;
    }

    public boolean isForMe() {
        return forMe;
    }

    public boolean isClosed() {
        return closed;
    }

    public void close(boolean countForMe) {
        this.setForMe(countForMe);
        this.setClosed(true);
        this.setTimestamp_end(System.currentTimeMillis());
    }

    public boolean hasNoIncreaseYet() {
//        Log.d(Constants.TAG, "noIncreaseYet: CHECK ****************** this:    equals - " + this.increase.equals(Increase.NONE));
        Log.d(Constants.TAG, "noIncreaseYet: ************** or this:      == - " + (this.increase == Constants.NONE));

//        return this.increase.equals(Increase.NONE);
        return this.increase == Constants.NONE;
    }
    public boolean isMyTurnToIncrease() {
//        return this.increase.equals(Increase.MY_TURN_TO_INCREASE);
        return this.increase == Constants.MY_TURN_TO_INCREASE;
    }
    public boolean isOppTurnToIncrease() {
//        return this.increase.equals(Increase.OPP_TURN_TO_INCREASE);
        return this.increase == Constants.OPP_TURN_TO_INCREASE;
    }
    public boolean isMyAnswerPending() {
//        return this.increase.equals(Increase.MY_INCREASE_PENDING);
        return this.increase == Constants.MY_INCREASE_PENDING;
    }
    public boolean isOppAnswerPending() {
//        return this.increase.equals(Increase.OPP_INCREASE_PENDING);
        return this.increase == Constants.OPP_INCREASE_PENDING;
    }

}
