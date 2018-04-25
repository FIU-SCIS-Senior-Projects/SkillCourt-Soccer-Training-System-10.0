package fiu.com.skillcourt.ui.game;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.shawnlin.numberpicker.NumberPicker;

import java.util.ArrayList;

import fiu.com.skillcourt.R;
import fiu.com.skillcourt.ui.main.NonBottomNavigationFragments;


/**
 * Created by Sergio Rosales on 2/5/18.
 */
public class CreateGameFragment extends NonBottomNavigationFragments {
    private static final String TAG = CreateGameFragment.class.getSimpleName();
    private int mGamePlayers = 1;
    private int mGameMinutes = 0;
    private int mGameSeconds = 0;
    private int check = 0;
    private String mGameType = "Solo";
    private double mPadLightUpTime = 0.0;
    private double mPadLightUpTimeDelay = 0.0;

    public CreateGameFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity.setTitle("Create Game");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_game, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button mPlayGame;
        NumberPicker mMinutePicker;
        NumberPicker mSecondPicker;
        final EditText padLightUp = view.findViewById(R.id.padLightUpTime);
        final EditText padLightUpDelay = view.findViewById(R.id.padLightUpDelayTime);

        mMinutePicker = view.findViewById(R.id.minute_picker);
        mSecondPicker = view.findViewById(R.id.second_picker);
        mPlayGame = view.findViewById(R.id.playGameBtn);

        mMinutePicker.setMinValue(0);
        mMinutePicker.setMaxValue(59);
        mSecondPicker.setMinValue(0);
        mSecondPicker.setMaxValue(59);

        mMinutePicker.setWrapSelectorWheel(true);
        mSecondPicker.setWrapSelectorWheel(true);

        mMinutePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                mGameMinutes = newVal;
            }
        });

        mSecondPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                mGameSeconds = newVal;
            }
        });


        mPlayGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Minutes: " + mGameMinutes);
                Log.i(TAG, "Seconds: " + mGameSeconds);


                String padLightUpTime =  padLightUp.getText().toString();
                String padLightUpTimeDelay =  padLightUpDelay.getText().toString();
                if(!padLightUpTime.isEmpty()){
                    try {
                        mPadLightUpTime = Double.parseDouble(padLightUpTime);
                    }catch(final NumberFormatException e){
                        Toast.makeText(getActivity(), "Your pad light up time is not valid", Toast.LENGTH_SHORT).show();
                    }
                }

                if(!padLightUpTimeDelay.isEmpty()){
                    try {
                        mPadLightUpTimeDelay = Double.parseDouble(padLightUpTimeDelay);
                    }catch(final NumberFormatException e){
                        Toast.makeText(getActivity(), "Your pad light up delay time is not valid", Toast.LENGTH_SHORT).show();
                    }
                }


                check = mainActivity.mConnectionService.getPadsConnected() % mGamePlayers;
                if (mGameMinutes == 0 && mGameSeconds == 0) {
                    Toast.makeText(getActivity(), "Must select an appropriate amount of time for playing", Toast.LENGTH_SHORT).show();
                } else if (check != 0) {
                    Toast.makeText(getActivity(), "Must add " + check + " more pads to play with " + mGamePlayers + " players", Toast.LENGTH_SHORT).show();
                } else {
                    long gameTimeFinal = (60 * mGameMinutes) + mGameSeconds;
                    Log.i(TAG, "Game time in seconds: " + gameTimeFinal);

                    Bundle bundle = getArguments();
                    if(mGamePlayers == 2 ){
                        mGameType = "Duo";
                    }
                    bundle.putString("GAME_TYPE", mGameType);
                    bundle.putLong("GAME_TIME", gameTimeFinal);
                    bundle.putDouble("PAD_LIGHT_UP_TIME", mPadLightUpTime);
                    bundle.putDouble("PAD_LIGHT_UP_TIME_DELAY", mPadLightUpTimeDelay);
                    bundle.putInt("PLAYER_COUNT", mGamePlayers);
                    Fragment fragment = new StartGameFragment();
                    fragment.setArguments(bundle);
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    mainActivity.startBottomNavFragment(fragment, fragmentTransaction);
                }
            }
        });
    }
}