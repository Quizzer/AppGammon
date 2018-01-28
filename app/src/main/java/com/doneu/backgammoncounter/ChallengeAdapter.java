package com.doneu.backgammoncounter;

import android.content.Context;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.doneu.backgammoncounter.beans.Challenge;

import java.util.HashSet;
import java.util.Set;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

/**
 * Created by doneu on 23.05.17.
 */

class ChallengeAdapter extends RealmBaseAdapter<Challenge> {

    private boolean inDeletionMode = false;
    private Set<Integer> challengesToDelete = new HashSet<>();
    private Context context;

    ChallengeAdapter(Context context, OrderedRealmCollection<Challenge> realmResults) {
        super(realmResults);
        this.context = context;
    }

    private static class ViewHolder {
        CheckBox deletionCheckBox;
        TextView text;
        TextView secundaryText;
        TextView score;
    }

    void enableDeletionMode(boolean enabled) {
        inDeletionMode = enabled;
        if (!enabled) {
            challengesToDelete.clear();
        }
        notifyDataSetChanged();
    }

    boolean isInDeletionMode() {
        return inDeletionMode;
    }

    Set<Integer> getChallengesToDeleteByID() {
        return challengesToDelete;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.text = (TextView) convertView.findViewById(R.id.textview);
            viewHolder.secundaryText = (TextView) convertView.findViewById(R.id.textview2);
            viewHolder.score = (TextView) convertView.findViewById(R.id.textViewScore);
            viewHolder.deletionCheckBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (adapterData != null) {
            final Challenge item = adapterData.get(position);

            // get relative time of the latest Game played
            String timeString;
            if (item.getGames().size() > 0) {
                long timestamp = item.getTimestamp_lastPlayed();
                long now = System.currentTimeMillis();
                timeString = DateUtils.getRelativeTimeSpanString(timestamp, now, 0).toString();
                timeString = timeString.substring(0, 1).toLowerCase() + timeString.substring(1);

                viewHolder.secundaryText.setText(context.getResources().getString(R.string.lastPlayed_string, timeString));
            } else {
                viewHolder.secundaryText.setText(context.getString(R.string.notGamesyet));
            }

            viewHolder.text.setText(Html.fromHtml(context.getResources().getString(R.string.youAgainst_string, "<b>" + item.getOpponent() +"</b>")));
            String scoreDivider = context.getResources().getString(R.string.scoreDivider);
            viewHolder.score.setText(context.getResources().getString(R.string.score_string, item.getMyPoints(), scoreDivider, item.getOppPoints()));

            if (inDeletionMode) {
                viewHolder.deletionCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            challengesToDelete.add(item.getId());
                        } else {
                            challengesToDelete.remove(item.getId());
                        }
                    }
                });
            } else {
                viewHolder.deletionCheckBox.setOnCheckedChangeListener(null);
            }
            viewHolder.deletionCheckBox.setChecked(challengesToDelete.contains(item.getId()));
            viewHolder.deletionCheckBox.setVisibility(inDeletionMode ? View.VISIBLE : View.GONE);
        }
        return convertView;
    }
}
