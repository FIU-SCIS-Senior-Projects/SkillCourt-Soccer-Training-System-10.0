package fiu.com.skillcourt.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import fiu.com.skillcourt.R;

public class PadViewAdapter extends BaseAdapter{
    private static final String TAG = PadViewAdapter.class.getSimpleName();
    private final Context mContext;
    private List<?> mItems;

    public PadViewAdapter(Context context, List<?> items){
        mContext = context;
        mItems = items;
    }


    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int i) {
        return mItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            view = layoutInflater.inflate(R.layout.layout_pad, null);
        }
        return view;
    }
}
