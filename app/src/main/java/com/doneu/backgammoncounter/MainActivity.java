package com.doneu.backgammoncounter;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.doneu.backgammoncounter.beans.Challenge;
import com.doneu.backgammoncounter.beans.Game;
import com.doneu.backgammoncounter.utils.Constants;

import java.util.Set;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ChallengeAdapter challengeAdapter;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(BuildConfig.DEBUG) {
            Log.d(Constants.TAG, "onCreate: MainActivity //" + super.theme);
        }

        setContentView(R.layout.activity_main);

        // Tipp: from Resources Strings...
        // getResources().getStringArray(R.array.locations);

        ListView lstChallenges = (ListView) findViewById(R.id.lstChallenges);

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        RealmResults<Challenge> challenges = realm.where(Challenge.class).findAllAsync();
        for (Challenge chlg: challenges) {
            chlg.setLatestGame(realm);
        }
        challenges = challenges.sort("timestamp_lastPlayed", Sort.DESCENDING);

        challengeAdapter = new ChallengeAdapter(this, challenges);
        lstChallenges.setAdapter(challengeAdapter);
        lstChallenges.setOnItemClickListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), NewChallengeActivity.class);
                startActivity(intent);

                // SNACKBAR EXAMPLE
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_INDEFINITE)
                //        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        RealmResults<Challenge> challenges = realm.where(Challenge.class).findAllAsync();
        for (Challenge chlg: challenges) {
            chlg.setLatestGame(realm);
        }
        challengeAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(Constants.TAG, "onItemClick: " + parent.getItemAtPosition(position));
        Challenge clickedChallenge = (Challenge) parent.getItemAtPosition(position);
        Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
        intent.putExtra(getResources().getString(R.string.challenge_id), clickedChallenge.getId());
        startActivity(intent);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public void onDeletionItem() {

        if (challengeAdapter.isInDeletionMode()) {

            Set<Integer> challengesToDelete_IDs = challengeAdapter.getChallengesToDeleteByID();

            if (challengesToDelete_IDs.size() > 0) {

                final Integer[] simpleIntegerIDs = new Integer[challengesToDelete_IDs.size()];
                challengesToDelete_IDs.toArray(simpleIntegerIDs);
                
                realm.executeTransactionAsync(new Realm.Transaction(){
                    @Override
                    public void execute(Realm realm) {
                        final RealmResults<Challenge> challengesToDelete = realm.where(Challenge.class).in("id", simpleIntegerIDs).findAll();

                        for (Challenge challenge: challengesToDelete) {
                            if (!challenge.getGames().isEmpty()) {
                                RealmResults<Game> gamesToDelete = realm.where(Game.class).equalTo("challenge.id", challenge.getId()).findAll();
                                Log.d(Constants.TAG, "execute: deleted " + challenge.getGames().size() + " Game(s) from Challenge vs. " + challenge.getOpponent() + String.format(" (id%d)", challenge.getId()));
                                gamesToDelete.deleteAllFromRealm();
                            }
                            Log.d(Constants.TAG, "execute: deleted Challenge vs. " + challenge.getOpponent() + String.format(" (id%d)", challenge.getId()));
                        }
                        challengesToDelete.deleteAllFromRealm();
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
            challengeAdapter.enableDeletionMode(false);
        } else {
            challengeAdapter.enableDeletionMode(true);
        }

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

}
