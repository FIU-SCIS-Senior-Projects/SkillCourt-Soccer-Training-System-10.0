package fiu.com.skillcourt.ui.game;


import android.graphics.Color;
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
import android.widget.TextView;

import at.grabner.circleprogress.CircleProgressView;
import fiu.com.skillcourt.R;
import fiu.com.skillcourt.ui.main.HomeFragment;
import fiu.com.skillcourt.ui.main.NonBottomNavigationFragments;

/**
 * Created by Joshua Mclendon on 2/7/18.
 */
public class GameOverFragment extends NonBottomNavigationFragments {

    private TextView mScore, mHits, mMode, mTime;
    private Button mPlayAgainButton, mNewGameButton, mHomeButton;
    private int testHit;
    private int testMiss;
    private String mGameMode;
    private long mGameTime;
    private int hit, miss, totalPoints;
    private CircleProgressView mCircleView;

    public GameOverFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity.setTitle("Game Over");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_over, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity.getSupportActionBar().show();
        final Bundle bundle = getArguments();
        mGameMode = bundle.getString("GAME_MODE");
        mGameTime = bundle.getLong("GAME_TIME", 30);
        testHit = bundle.getInt("HIT_COUNT", 0);
        testMiss = bundle.getInt("MISS_COUNT", 0);
        totalPoints = bundle.getInt("SCORE", 0);

        hit = (int) ((double) testHit / (testHit + testMiss) * 100);
        miss = (int) ((double) testMiss / (testHit + testMiss) * 100);

        Log.i("GameOverActivity", "Hit: " + hit + "%" + " Miss: " + miss + "%");

        mCircleView = view.findViewById(R.id.game_over_progress);
        mScore = view.findViewById(R.id.game_over_score);
        mHits = view.findViewById(R.id.game_over_hits);
        mMode = view.findViewById(R.id.game_over_mode);
        mTime = view.findViewById(R.id.game_over_time);
        mPlayAgainButton = view.findViewById(R.id.game_over_play_again_btn);
        mNewGameButton = view.findViewById(R.id.game_over_new_game_btn);
        mHomeButton = view.findViewById(R.id.game_over_home_btn);

        if(mGameMode == null){
            mGameMode = "Random";
        }
        mMode.setText(mGameMode);
        mMode.setAllCaps(true);
        mHits.setText("Hits: " + testHit + " / " + (testHit + testMiss));
        if(totalPoints < 0){
            totalPoints = 0;
        }
        mScore.setText("Score: " + totalPoints);
        setGameTimeText(mGameTime);
        if(hit == 0){
            setProgressBarColor(Color.parseColor("#B3B3B3"));
        }
        else if (hit > 0 && hit <= 20) {
            setProgressBarColor(Color.RED);
        }else if(hit > 20 && hit <= 40){
            setProgressBarColor(Color.parseColor("#f48342"));
        }else if(hit > 40 && hit <= 60){
            setProgressBarColor(Color.parseColor("#f4ce42"));
        }else if(hit > 60 && hit < 80){
            setProgressBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        mCircleView.setValueAnimated(hit);

        mPlayAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Fragment fragment = new StartGameFragment();
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                mainActivity.startBottomNavFragment(fragment, fragmentTransaction);
            }
        });

        mNewGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new GameModeFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                mainActivity.startFragmentWithBackButton(fragment, fragmentTransaction, false);
            }
        });

        mHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new HomeFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                mainActivity.startBottomNavFragment(fragment, fragmentTransaction);
            }
        });
    }

    private void setProgressBarColor(int color){
        mCircleView.setTextColor(color);
        mCircleView.setBarColor(color);
        mCircleView.setUnitColor(color);
    }

    private void setGameTimeText(long seconds) {
        if (seconds >= 60) {
            int minute = (int) seconds / 60;
            seconds = seconds % 60;
            if (seconds < 10) {
                mTime.setText(minute + ":0" + seconds);
            } else {
                mTime.setText(minute + ":" + seconds);
            }
        } else {
            if (seconds < 10) {
                mTime.setText("00:0" + seconds);
            } else {
                mTime.setText("00:" + seconds);
            }
        }
    }
}