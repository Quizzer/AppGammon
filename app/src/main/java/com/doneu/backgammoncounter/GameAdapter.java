package com.doneu.backgammoncounter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.doneu.backgammoncounter.beans.Game;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

/**
 * Created by doneu on 23.05.17.
 */

class GameAdapter extends RealmBaseAdapter<Game> {

    private boolean inDeletionMode = false;
    private Set<Integer> gamesToDelete = new HashSet<>();
    private Context context;

    GameAdapter(Context context, OrderedRealmCollection<Game> realmResults) {
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
            gamesToDelete.clear();
        }
        notifyDataSetChanged();
    }

    boolean isInDeletionMode() {
        return inDeletionMode;
    }

    Set<Integer> getGamesToDeleteByID() {
        return gamesToDelete;
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
            final Game item = adapterData.get(position);

            long start = item.getTimestamp_start();
            long end = item.getTimestamp_end();
            long now = System.currentTimeMillis();
            long duration = end - start;
            if (item.isClosed()) {
                boolean forMe = item.isForMe();
                viewHolder.text.setText(forMe? context.getString(R.string.victory) : context.getString(R.string.defeat));
                viewHolder.text.setTypeface(null, Typeface.BOLD);
                viewHolder.secundaryText.setText(DateUtils.getRelativeTimeSpanString(end, now, 0).toString() + " - " + TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS) + " min");
                viewHolder.score.setText((forMe? context.getString(R.string.plus, item.getPoints()) : context.getString(R.string.minus, item.getPoints())));
                viewHolder.score.setTextColor(forMe? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));
            } else {
                //duration = now - start;
                viewHolder.text.setText(context.getString(R.string.open));
                viewHolder.text.setTypeface(null, Typeface.BOLD_ITALIC);
                viewHolder.secundaryText.setText(DateUtils.getRelativeTimeSpanString(start, now, 0).toString() + " - " + context.getString(R.string.open));
                viewHolder.score.setText(context.getString(R.string.noScoreYet, item.getPoints()));
                viewHolder.score.setTextColor(Color.GRAY);
            }



            if (inDeletionMode) {
                viewHolder.deletionCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            gamesToDelete.add(item.getId());
                        } else {
                            gamesToDelete.remove(item.getId());
                        }
                    }
                });
            } else {
                viewHolder.deletionCheckBox.setOnCheckedChangeListener(null);
            }
            viewHolder.deletionCheckBox.setChecked(gamesToDelete.contains(item.getId()));
            viewHolder.deletionCheckBox.setVisibility(inDeletionMode ? View.VISIBLE : View.GONE);
        }
        return convertView;
    }
}
