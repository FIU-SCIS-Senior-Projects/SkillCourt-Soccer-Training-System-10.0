package fiu.com.skillcourt.ui.configuration;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import org.askerov.dynamicgrid.DynamicGridView;

import fiu.com.skillcourt.R;
import fiu.com.skillcourt.adapters.DragNDropPadViewAdapter;
import fiu.com.skillcourt.structures.Pad;
import fiu.com.skillcourt.ui.main.NonBottomNavigationFragments;

/**
 * Created by Joshua Mclendon on 2/4/18.
 */
public class PadConfigFragment extends NonBottomNavigationFragments {

    private static final String TAG = PadConfigFragment.class.getSimpleName();
    private DynamicGridView mGridView;
    private DragNDropPadViewAdapter mDragNDropPadViewAdapter;

    public PadConfigFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity.setTitle("Configuration");
        return inflater.inflate(R.layout.fragment_pad_config, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDragNDropPadViewAdapter = new DragNDropPadViewAdapter(getActivity(), mainActivity.mConnectionService.getActivePads(), 2);
        mCurrentColor = "white";
        mGridView = view.findViewById(R.id.padGridView);
        mGridView.setAdapter(mDragNDropPadViewAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, final View view, int position, long id) {
                if (mCurrentColor.equalsIgnoreCase("white")) {
                    Log.i(TAG, "Turn blue");
                /*    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            changeColor("blue", view);
                            mDragNDropPadViewAdapter.notifyDataSetChanged();
                        }
                    });*/

                    Pad pad = mainActivity.mConnectionService.getActivePads().get(position);
                    pad.turnOn();
                    mCurrentColor = "blue";

                } else {
                    Log.i(TAG, "Turn white");
                    /*getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            changeColor("white", view);
                            mDragNDropPadViewAdapter.notifyDataSetChanged();
                        }
                    });*/

                    Pad pad = mainActivity.mConnectionService.getActivePads().get(position);
                    pad.turnOff();
                    mCurrentColor = "white";
                }
            }
        });

        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                mGridView.startEditMode(i);
                return true;
            }
        });

        mGridView.setOnDragListener(new DynamicGridView.OnDragListener() {
            @Override
            public void onDragStarted(int position) {

            }

            @Override
            public void onDragPositionsChanged(int oldPosition, int newPosition) {
                Log.i(TAG, mainActivity.mConnectionService.getActivePads().get(oldPosition).getUuid());
                mainActivity.mConnectionService.swap(oldPosition, newPosition);
                Log.i(TAG, mainActivity.mConnectionService.getActivePads().get(newPosition).getUuid());

            }
        });

        mGridView.setOnDropListener(new DynamicGridView.OnDropListener() {
            @Override
            public void onActionDrop() {
                mGridView.stopEditMode();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mainActivity.mConnectionService != null) {
            for (Pad pad : mainActivity.mConnectionService.getActivePads()) {
                pad.turnOff();
            }
        }

    }

}

