package com.cansis.saad.payiee;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by Saad on 03/06/2017.
 */

public class Adapter extends BaseAdapter {

    private Context mContext;

    // Constructor
    public Adapter(Context c) {
        mContext = c;
    }


    @Override
    public int getCount() {

        return 12;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }
}
