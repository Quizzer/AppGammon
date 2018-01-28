package com.doneu.backgammoncounter.beans;

import android.util.Log;

import com.doneu.backgammoncounter.utils.Constants;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by doneu on 18.05.17.
 */

public class Challenge extends RealmObject {

    @PrimaryKey
    private int id;
    private String opponent;
    private int myPoints;
    private int oppPoints;
    private RealmList<Game> games; // one-to-many relation for games
    public long timestamp_lastPlayed; // is accessed by challenges.sort("timestamp_lastPlayed" ...) in MainActivity

    public Challenge() {}

    public Challenge(Realm realm, String opponent, int myPoints, int oppPoints) {
        this.id = this.getNextId(realm);
        this.opponent = opponent;
        this.myPoints = myPoints;
        this.oppPoints = oppPoints;
        this.games = new RealmList<>();
    }

    private int getNextId(Realm realm) {
        try {
            if (realm.where(Challenge.class).findAll().size() > 0) {
                return realm.where(Challenge.class).max("id").intValue() + 1;
            } else
                return 0;
        } catch(ArrayIndexOutOfBoundsException ex) {
            return 0;
        }
    }


    public static Challenge getByPrimaryKey(Realm realm, int id) {
        return realm.where(Challenge.class).equalTo("id", id).findFirst();
    }

    public int getId() {
        return id;
    }

    public RealmList<Game> getGames() {
        return games;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOppPoints() {
        return oppPoints;
    }

    public String getOpponent() {
        return opponent;
    }

    public int getMyPoints() {
        return myPoints;
    }

    public void setOpponent(String opponent) {
        this.opponent = opponent;
    }

    public void setMyPoints(int myPoints) {
        this.myPoints = myPoints;
    }

    public void setOppPoints(int oppPoints) {
        this.oppPoints = oppPoints;
    }

    public void addGame(Game game) {
        this.getGames().add(game);
    }

    public boolean hasOpenGames() {
        boolean hasMinOneOpenGame = false;
        for (Game game: this.getGames()) {
            if (!game.isClosed()) {
                hasMinOneOpenGame = true;
                break;
            }
        }
        return hasMinOneOpenGame;
    }

    public long getTimestamp_lastPlayed() {
        Game latestGame = new Game();
        for (Game game: this.getGames()) {
            if (game.getTimestamp_end() > latestGame.getTimestamp_end()) {
                latestGame = game;
            }
        }
        return latestGame.getTimestamp_end();
    }

    public void setLatestGame(Realm realm) {
        realm.beginTransaction();
        this.timestamp_lastPlayed = this.getTimestamp_lastPlayed();
        realm.commitTransaction();
    }
}
