package com.doneu.backgammoncounter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.doneu.backgammoncounter.beans.Challenge;
import com.doneu.backgammoncounter.beans.Game;
import com.doneu.backgammoncounter.utils.Constants;

import io.realm.Realm;

//import com.doneu.backgammoncounter.utils.Constants.Increase;
//import com.doneu.backgammoncounter.beans.Game.Increase;


public class GameActivity extends BaseActivity {

    private Realm realm;
    private Context context;
    private Game game;
    private TextView pointsTextView;
    private Button pointsDownBtn;
    private Button pointsUpBtn;
    private Button myIncreaseBtn;
    private Button oppIncreaseBtn;
    private Button myLostBtn;
    private Button oppLostbtn;
    private TextView myNameAndPointsTextView;
    private TextView oppNameAndPointsTextView;
    private TextView oppExtraInfoTextView;
    private TextView myExtraInfoTextView;
    private RelativeLayout lightDarkLayout;
    private FrameLayout gameLayout;

//    private boolean myTurnToIncrease;
//    private boolean noPlayerIncreasementYet;
//    private boolean increasedMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(BuildConfig.DEBUG) {
            Log.d(Constants.TAG, "onCreate: GameActivity //" + super.theme);
        }

        setContentView(R.layout.activity_game);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

          /* Hiding the Status Bar worked, but only with a visible delay in it */
//        //Hide the Status Bar on Android 4.1 and Higher - https://developer.android.com/training/system-ui/status.html#41
//        View decorView = getWindow().getDecorView();
//        // Hide the status bar.
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);
//        // Remember that you should never show the action bar if the
//        // status bar is hidden, so hide that too if necessary.

        // Blendet ActionBar aus, falls vorhanden
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }

        // find all View Elements
        pointsTextView = (TextView) findViewById(R.id.pointsTextView);
        pointsUpBtn = (Button) findViewById(R.id.pointsUpBtn);
        pointsDownBtn = (Button) findViewById(R.id.pointsDownBtn);
        myIncreaseBtn = (Button) findViewById(R.id.myIncreaseBtn);
        oppIncreaseBtn = (Button) findViewById(R.id.oppIncreaseBtn);
        myLostBtn = (Button) findViewById(R.id.myLostBtn);
        oppLostbtn = (Button) findViewById(R.id.oppLostBtn);
        myNameAndPointsTextView = (TextView) findViewById(R.id.myNameAndPointsTextView);
        oppNameAndPointsTextView = (TextView) findViewById(R.id.oppNameAndPointsTextView);
        myExtraInfoTextView = (TextView) findViewById(R.id.myExtraInfoTextView);
        oppExtraInfoTextView = (TextView) findViewById(R.id.oppExtraInfoTextView);
        lightDarkLayout = (RelativeLayout) findViewById(R.id.light_dark_layout);
        gameLayout = (FrameLayout) findViewById(R.id.game_layout);

        context = getApplicationContext();
        Realm.init(context);
        realm = Realm.getDefaultInstance();

        // try to retrieve Game if there is a Game to continue
        int game_id = getIntent().getIntExtra(getString(R.string.game_id), -1);
        if (game_id != -1) {
            game = Game.getByPrimaryKey(realm, game_id);
            pointsTextView.setText(String.valueOf(game.getPoints()));
        } else {
            Challenge challenge = null;
            // if there is no Game the must be a Challenge to retrieve
            int challenge_id = getIntent().getIntExtra(getString(R.string.challenge_id), -1);
            if (challenge_id != -1) {
                challenge = Challenge.getByPrimaryKey(realm, challenge_id);
            } else {
                Toast.makeText(this, R.string.challengeNotFound_error, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                finish();
            }

            final Game unmanagedGame = new Game(realm, challenge);
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    game = realm.copyToRealm(unmanagedGame);
                    game.getChallenge().addGame(game);
                    game.setIncrease(Constants.NONE);
                }
            });

            pointsTextView.setText(String.valueOf(game.getPoints()));

            Log.d(Constants.TAG, "onCreate: " + String.format(Constants.log_challenge_string, challenge.getId(), challenge.getOpponent(), challenge.getMyPoints(), challenge.getOppPoints()));
        }
        Log.d(Constants.TAG, "onCreate: new Game (id" + game.getId() + ") started: " + game.getTimestamp_start() + " initial Points: " + game.getPoints());


        Challenge challenge = game.getChallenge();
        int oppPoints = challenge.getOppPoints();
        int myPoints = challenge.getMyPoints();
        int myDiff = myPoints-oppPoints;
        int oppDiff = oppPoints-myPoints;
        String sMyDiff = (myDiff > 0)? "+"+Integer.toString(myDiff) : Integer.toString(myDiff);
        String sOppDiff = (oppDiff > 0)? "+"+Integer.toString(oppDiff) : Integer.toString(oppDiff);

        // Todo: I18n Strings with replacements!!
        // Todo: Pref Settings Own Name!
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        myNameAndPointsTextView.setText(getString(R.string.nameAndPointsInfo, prefs.getString(getString(R.string.prefsUsername), getString(R.string.prefsUsername_default)), challenge.getMyPoints(), challenge.getOppPoints(), sMyDiff));
        oppNameAndPointsTextView.setText(getString(R.string.nameAndPointsInfo, challenge.getOpponent(), challenge.getOppPoints(), challenge.getMyPoints(), sOppDiff));

        Boolean reverseOrientation = prefs.getBoolean(getString(R.string.prefsOrientationToggle),  Boolean.valueOf(getString(R.string.prefsOrientationToggle_default)));
        if (reverseOrientation) {
            gameLayout.setRotation(180);
            // this turns the whole game layout including game colors
        }

        String myGameColor = prefs.getString(getString(R.string.prefsDarkToggle), getString(R.string.prefsDarkToggle_default));
        if (!myGameColor.equals(getString(R.string.light_key))) {
            lightDarkLayout.setRotation(180);

            // Only if game color is reversed text colors must be adjusted
            int white = getResources().getColor(R.color.white);
            int defaultColor =  myExtraInfoTextView.getCurrentTextColor();
            myExtraInfoTextView.setTextColor(white);
            myNameAndPointsTextView.setTextColor(white);
            oppExtraInfoTextView.setTextColor(defaultColor);
            oppNameAndPointsTextView.setTextColor(defaultColor);
        }

        this.updateButtons();

    }

    public void onClick_pointsUpBtn(View view) {
        this.updatePoints(true);
    }

    public void onClick_pointsDownBtn(View view) {
        this.updatePoints(false);
    }

    private void updatePoints(boolean up) {
        if (up && Constants.MAXIMUM_POINTS > game.getPoints()) {
            realm.beginTransaction();
            game.setPoints(game.getPoints() * Constants.MULTIPLY_POINTS_WITH);
            realm.commitTransaction();
            Log.d(Constants.TAG, "updatePoints_UP: " + game.getPoints());
        } else if (!up && game.getPoints() > 1) {
            realm.beginTransaction();
            game.setPoints(game.getPoints() / Constants.MULTIPLY_POINTS_WITH);
            realm.commitTransaction();
            Log.d(Constants.TAG, "updatePoints_DOWN: " + game.getPoints());
        }
        pointsTextView.setText(String.valueOf(game.getPoints()));
        this.updateButtons();
    }

    private void updateButtons() {
        boolean pointsLessThanMax = game.getPoints() < Constants.MAXIMUM_POINTS;

        pointsDownBtn.setEnabled(game.getPoints() > 1 && game.hasNoIncreaseYet());
        pointsUpBtn.setEnabled(pointsLessThanMax && game.hasNoIncreaseYet());

        if (game.isMyAnswerPending()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            oppExtraInfoTextView.setText(getString(R.string.raisesUpTo_string, prefs.getString(getString(R.string.prefsUsername), getString(R.string.prefsUsername_default)), game.getPoints() * Constants.MULTIPLY_POINTS_WITH));
            oppIncreaseBtn.setText(R.string.accept);
            oppLostbtn.setText(R.string.reject);
            oppIncreaseBtn.setEnabled(true);
            oppLostbtn.setEnabled(true);
            myIncreaseBtn.setEnabled(false);
            myLostBtn.setEnabled(false);
        } else if (game.isOppAnswerPending()) {
            myExtraInfoTextView.setText(getString(R.string.raisesUpTo_string, game.getChallenge().getOpponent(), game.getPoints() * Constants.MULTIPLY_POINTS_WITH));
            myIncreaseBtn.setText(R.string.accept);
            myLostBtn.setText(R.string.reject);
            myIncreaseBtn.setEnabled(true);
            myLostBtn.setEnabled(true);
            oppIncreaseBtn.setEnabled(false);
            oppLostbtn.setEnabled(false);
        } else if (game.isMyTurnToIncrease()) {
            myExtraInfoTextView.setText("");
            myIncreaseBtn.setText(getResources().getString(R.string.increase));
            myLostBtn.setText(getResources().getString(R.string.lost));
            oppLostbtn.setEnabled(true);
            oppIncreaseBtn.setEnabled((pointsLessThanMax && game.isOppTurnToIncrease()) || game.hasNoIncreaseYet());
        } else if (game.isOppTurnToIncrease()) {
            oppExtraInfoTextView.setText("");
            oppIncreaseBtn.setText(getResources().getString(R.string.increase));
            oppLostbtn.setText(getResources().getString(R.string.lost));
            myLostBtn.setEnabled(true);
            myIncreaseBtn.setEnabled((pointsLessThanMax && game.isMyTurnToIncrease()) || game.hasNoIncreaseYet());
        }
    }

    public void onClick_myIncreaseBtn(View view) {
        if (game.isOppAnswerPending()) {
            // opponents increase is accepted
            game.setIncrease(realm, Constants.MY_TURN_TO_INCREASE);
            this.updatePoints(true);
        } else {
            game.setIncrease(realm, Constants.MY_INCREASE_PENDING);
            this.updateButtons();
            Log.d(Constants.TAG, "onClick_myIncreaseBtn: old Points: " + game.getPoints());
            // Todo: Relation btwn TextView and game.getPoints()   ----> possible by Realm?
        }
    }

    public void onClick_oppIncreaseBtn(View view) {
        if (game.isMyAnswerPending()) {
            // my increase is accepted
            game.setIncrease(realm, Constants.OPP_TURN_TO_INCREASE);
            this.updatePoints(true);
        } else {
            game.setIncrease(realm, Constants.OPP_INCREASE_PENDING);
            this.updateButtons();
            Log.d(Constants.TAG, "onClick_oppIncreaseBtn: old Points: " + game.getPoints());
        }
    }

    public void onClick_myLostBtn(View view) {
        if (game.isOppAnswerPending()) {
            this.closeGame(false);
        } else {
            this.showEndGameDialog(false); // counts for Opp
        }
    }

    public void onClick_oppLostBtn(View view) {
        if (game.isMyAnswerPending()) {
            this.closeGame(true);
        } else {
            this.showEndGameDialog(true); // counts for Me
        }
    }

    private void closeGame(boolean countsForMe) {
        if (BuildConfig.DEBUG && game.isDoubling() && game.isTrebling()) {
            throw new AssertionError();
        }
        Challenge challenge = game.getChallenge();
        // close game first, then getPoints returns calculated Points
        realm.beginTransaction();
        game.close(countsForMe);
        realm.commitTransaction();
        int pointsBefore = countsForMe? challenge.getMyPoints() : challenge.getOppPoints();
        int pointsToAdd = game.getPoints();
        int newPoints = pointsBefore + pointsToAdd;
        realm.beginTransaction();
        if (countsForMe) {
            challenge.setMyPoints(newPoints);
        }  else {
            challenge.setOppPoints(newPoints);
        }
        realm.commitTransaction();
        Log.d(Constants.TAG, "onClick_[my/opp]LostBtn: " + String.format(Constants.log_game_string, game.getId(), game.getPoints(), game.isClosed(), game.getTimestamp_end()));
        Log.d(Constants.TAG, "onClick_[my/opp]LostBtn: update " + String.format(Constants.log_challenge_string, challenge.getId(), challenge.getOpponent(), challenge.getMyPoints(), challenge.getOppPoints()));

        // Not needed as long as HistoryActicity starts 'ActivityForResult' and goes for 'onActivityResult',
        // If it is the first Game of a new Challenge, returning to Main is just fine.
//        Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
//        intent.putExtra(getResources().getString(R.string.challenge_id), challenge.getId());
//        startActivity(intent);

        finish();
    }

    private void showEndGameDialog(final boolean countsForMe) {
        final GameActivity gameActivity = this;
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.game_over));
        alertDialog.setMessage(getString(R.string.game_over_text));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "x3", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(Constants.TAG, "EndGameDialog Button x3: counts " + game.getPoints()*3 + " points");
                realm.beginTransaction();
                game.setTrebling(true);
                realm.commitTransaction();
                gameActivity.closeGame(countsForMe);
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "x2", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(Constants.TAG, "EndGameDialog Button x2: counts " + game.getPoints()*2 + " points");
                realm.beginTransaction();
                game.setDoubling(true);
                realm.commitTransaction();
                gameActivity.closeGame(countsForMe);
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.single), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(Constants.TAG, "EndGameDialog Button simple: counts " + game.getPoints() + " points");
                gameActivity.closeGame(countsForMe);
            }
        });
//        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialog) {
//                Log.d(Constants.TAG, "EndGameDialog ABORT : nothing was counted yet");
//            }
//        });

        alertDialog.show();
    }

}
