package com.doneu.backgammoncounter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.doneu.backgammoncounter.beans.Challenge;
import com.doneu.backgammoncounter.beans.Game;
import com.doneu.backgammoncounter.utils.Constants;

import java.util.Set;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class HistoryActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    GameAdapter gameAdapter;
    Challenge challenge;
    Context context;
    Realm realm;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(Constants.TAG, "onActivityResult: returned from Game to History");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(BuildConfig.DEBUG) {
            Log.d(Constants.TAG, "onCreate: HistoryActivity //" + super.theme);
        }

        setContentView(R.layout.activity_history_fab);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (challenge.hasOpenGames()) {
                    Toast.makeText(context, R.string.openGamesFirst, Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                    intent.putExtra(getResources().getString(R.string.challenge_id), challenge.getId());
                    startActivityForResult(intent, 0);
                }
            }
        });

        ListView lstGames = (ListView) findViewById(R.id.lstGames);

        context = getApplicationContext();
        Realm.init(context);
        realm = Realm.getDefaultInstance();

        // retrieve challenge object
        int challenge_id = getIntent().getIntExtra(getResources().getString(R.string.challenge_id), -1);
        if (challenge_id != -1) {
            challenge = Challenge.getByPrimaryKey(realm, challenge_id);
        } else {
            Toast.makeText(this, R.string.challengeNotFound_error, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);
            finish();
        }

        Log.d(Constants.TAG, "onCreate: " + String.format(Constants.log_challenge_string, challenge.getId(), challenge.getOpponent(), challenge.getMyPoints(), challenge.getOppPoints()));

        RealmResults<Game> games = realm.where(Game.class).equalTo("challenge.id", challenge.getId()).findAllAsync();
        games = games.sort("timestamp_end", Sort.DESCENDING);

        gameAdapter = new GameAdapter(this, games);
        lstGames.setAdapter(gameAdapter);
        lstGames.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(Constants.TAG, "onItemClick: " + parent.getItemAtPosition(position));
        Game clickedGame = (Game) parent.getItemAtPosition(position);
        if (!clickedGame.isClosed()) {
            Intent intent = new Intent(getApplicationContext(), GameActivity.class);
            intent.putExtra(getResources().getString(R.string.game_id), clickedGame.getId());
            startActivityForResult(intent, 0);
        } else {
            Toast.makeText(context, R.string.gameClosed, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deletionItem:
                Log.d(Constants.TAG, "onClickDeletionItem: ");
                this.onDeletionItem();
                return true;
            case R.id.settingsItem:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onDeletionItem() {
        Log.d(Constants.TAG, "onClickDeletionItem: ");
        if (gameAdapter.isInDeletionMode()) {
            Set<Integer> gamesToDelete_IDs = gameAdapter.getGamesToDeleteByID();

            if (gamesToDelete_IDs.size() > 0) {

                final Integer[] simpleIntegerIDs = new Integer[gamesToDelete_IDs.size()];
                gamesToDelete_IDs.toArray(simpleIntegerIDs);

                realm.executeTransactionAsync(new Realm.Transaction(){
                    @Override
                    public void execute(Realm realm) {
                        final RealmResults<Game> gamesToDelete = realm.where(Game.class).in("id", simpleIntegerIDs).findAll();

                        for (Game game: gamesToDelete) {
                            if (game.isClosed()) {
                                Challenge challenge = game.getChallenge();
                                if (game.isForMe()) {
                                    Log.d(Constants.TAG, "execute: remove " + game.getPoints() + "(" + game.isForMe() + ") points from Challenge[id-" + challenge.getId() + "] vs. " + challenge.getId());
                                    game.getChallenge().setMyPoints((challenge.getMyPoints() - game.getPoints()));
                                } else {
                                    game.getChallenge().setOppPoints((challenge.getOppPoints() - game.getPoints()));
                                }
                                Log.d(Constants.TAG, "execute: remove " + game.getPoints() + "(" + game.isForMe() + ") points from Challenge[id-" + challenge.getId() + "] vs. " + challenge.getId());
                            }
                            Log.d(Constants.TAG, "execute: deleted " + String.format(Constants.log_game_string, game.getId(), game.getPoints(), game.isClosed(), game.getTimestamp_end()));
                        }
                        gamesToDelete.deleteAllFromRealm();
                    }
                }, new Realm.Transaction.OnSuccess() {

                    @Override
                    public void onSuccess() {
                        Log.d(Constants.TAG, "onSuccess: DELETE");
                    }
                }, new Realm.Transaction.OnError() {

                    @Override
                    public void onError(Throwable error) {
                        Log.d(Constants.TAG, "onError: ERROR");
                    }
                });
            }
            gameAdapter.enableDeletionMode(false);
        } else {
            gameAdapter.enableDeletionMode(true);
        }
    }

}
