package com.example.hieroglyphicstranslator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class symbolAdapter extends ArrayAdapter<symbol> {

    //constructor for symbolAdapter
    public symbolAdapter(@NonNull Context context, ArrayList<symbol>symbols) {
        super(context, 0);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }
        symbol currentSymbol=getItem(position);

        TextView firstTextView = (TextView) listItemView.findViewById(R.id.first_text);
        firstTextView.setText(currentSymbol.getFirstText());

        TextView secondTextView = (TextView) listItemView.findViewById(R.id.second_text);
        secondTextView.setText(currentSymbol.getSecondText());

        TextView thirdTextView = (TextView) listItemView.findViewById(R.id.third_text);
        thirdTextView.setText(currentSymbol.getThirdText());

        ImageView image=(ImageView) listItemView.findViewById(R.id.symbolImage);
        /**Glide.with(getContext())
                .load("")
                .into("");**/
        image.setImageBitmap(currentSymbol.getSymboolImage());

        return listItemView;
    }
}
