package com.doneu.backgammoncounter;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.doneu.backgammoncounter.beans.Challenge;
import com.doneu.backgammoncounter.utils.Constants;

import io.realm.Realm;

public class NewChallengeActivity extends BaseActivity implements TextWatcher {

    private FloatingActionButton fab;
    private EditText edtPlayerName;
    private EditText edtMyPoints;
    private EditText edtOppPoints;
    private Realm realm;
    private Challenge challenge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(BuildConfig.DEBUG) {
            Log.d(Constants.TAG, "onCreate: NewChallengeActivity //" + super.theme);
        }

        setContentView(R.layout.activity_new_challenge);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setEnabled(false);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Spielernamen auslesen
                String name = edtPlayerName.getText().toString().trim();
                if (!TextUtils.isEmpty(name)) { // checkt null oder "" ohne Gefahr einer NullPointer
                    challenge.setOpponent(name);
                }

                // initialen Score auslesen und setzen, keine Eingabe = 0
                String myPoints_string = edtMyPoints.getText().toString();
                String oppPoints_string = edtOppPoints.getText().toString();
                int myPoints = TextUtils.isEmpty(myPoints_string) ? 0 : Integer.parseInt(myPoints_string);
                int oppPoints = TextUtils.isEmpty(oppPoints_string) ? 0 : Integer.parseInt(oppPoints_string);
                challenge.setMyPoints(myPoints);
                challenge.setOppPoints(oppPoints);

                realm.beginTransaction();
                realm.copyToRealmOrUpdate(challenge);
                realm.commitTransaction();

                Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                intent.putExtra(getResources().getString(R.string.challenge_id), challenge.getId());
                startActivity(intent);

                finish();
            }
        });

        Realm.init(getApplicationContext());
        realm = Realm.getDefaultInstance();

        // Create New Challenge Object
        this.instantiateNewDefaultChallenge();

        edtPlayerName = (EditText) findViewById(R.id.edtPlayerName);
        edtMyPoints = (EditText) findViewById(R.id.myPoints);
        edtOppPoints = (EditText) findViewById(R.id.oppPoints);

        // Listener für Änderungen im Namensfeld
        edtPlayerName.addTextChangedListener(this);

        InputFilter[] scoreFilters = new InputFilter[]{
                // max length filter for score input
                new InputFilter.LengthFilter(Constants.MAX_SCORE_LENGTH)
        };
        edtMyPoints.setFilters(scoreFilters);
        edtOppPoints.setFilters(scoreFilters);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
//        Log.d(Constants.TAG, "afterTextChanged: " + s.toString());
        fab.setEnabled(!TextUtils.isEmpty(s.toString().trim()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();

            String[] projection = {ContactsContract.Contacts.DISPLAY_NAME}; // Columns aus der Datenbank
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(0);
                Log.d(Constants.TAG, "onActivityResult:" + name);
                edtPlayerName.setText(name);
                cursor.close();
            }
        }
    }

    public void onClick_Contact(View view) {
        // Zu Kontakte-App wechseln und Kontakt auswählen lassen
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.instantiateNewDefaultChallenge();
    }

    protected void instantiateNewDefaultChallenge() {
        this.challenge = new Challenge(realm, "John Doe", 0, 0);
    }
}
