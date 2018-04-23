package fiu.com.skillcourt.ui.game;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import fiu.com.skillcourt.R;
import fiu.com.skillcourt.adapters.PlayerViewAdapter;
import fiu.com.skillcourt.services.GameService;
import fiu.com.skillcourt.structures.Player;
import fiu.com.skillcourt.ui.main.NonBottomNavigationFragments;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by Joshua Mclendon on 2/5/18.
 */
public class StartGameFragment extends NonBottomNavigationFragments {

    //START PLAYER STRUCT

    private int mPlayerCount;
    private ArrayList<Integer> sequenceOne;
    private ArrayList<Integer> sequenceTwo;
    private ArrayList<Integer> sequenceThree;
    private ArrayList<Integer> sequenceFour;
    private List<Player> players = new CopyOnWriteArrayList<>();

    //END NEW PLAYER STRUCT

    private static final String TAG = StartGameFragment.class.getSimpleName();
    private static final String PAD_UI_COLOR_CHANGE = "PAD_UI_COLOR_CHANGE";
    private static final String PAD_HIT_UI_COUNT_CHANGE = "PAD_HIT_UI_COUNT_CHANGE";
    private static final String PAD_MISS_UI_COUNT_CHANGE = "PAD_MISS_UI_COUNT_CHANGE";
    IntentFilter gameIntentFilter;
    HitReceiver hitReceiver;
    private CountDownTimer gameTimer, countDownTimer;
    private TextView mCountDown;
    private TextView mTimer;
    private TextView mGameModeTv;
    private TextView mHitCount;
    private TextView mMissCount;
    private TextView mScoreCount;
    private TextView mPlayerCountTv;
    private String mGameMode, mGameType, playerOneColor, playerTwoColor, playerThreeColor, playerFourColor;
    private long mGameTime;
    private GridView mGridView;
    private PlayerViewAdapter mPlayerViewAdapter;
    private boolean mGameBounded = false;
    private GameService mGameService;
    private Bundle bundle;
    private Player mPlayer;

    ServiceConnection mGameConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mGameBounded = false;
            mGameService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "binding game service");
            mGameBounded = true;
            final GameService.LocalBinder mLocalBinder = (GameService.LocalBinder) service;
            mGameService = mLocalBinder.getService();

            gameTimer = new CountDownTimer(mGameTime * 1000, 1000) {

                public void onTick(long millisUntilFinished) {
                    Log.i(TAG, "Count down game time" + millisUntilFinished);
                    updateTimerText(millisUntilFinished / 1000);
                }

                public void onFinish() {
                    Log.i(TAG, "Game over");
                    mTimer.setText("00:00");
                    bundle.putInt("HIT_COUNT", mPlayer.getHitCount());
                    bundle.putInt("MISS_COUNT", mPlayer.getMissCount());
                    bundle.putInt("SCORE", mPlayer.getTotalPoints());

                    Fragment fragment = new GameOverFragment();
                    fragment.setArguments(bundle);
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    mainActivity.startBottomNavFragment(fragment, fragmentTransaction);

                    Intent intentGameOver = new Intent();
                    intentGameOver.setAction("END_GAME_SERVICE");
                    getActivity().sendBroadcast(intentGameOver);
                }
            };


            mGameService.setGameMode(mGameMode);
            mGameService.setGameTime(mGameTime);
            mGameService.setGameType(mGameType);
            mGameService.setPlayers(players);
            mGameService.setSequence(sequenceOne);
            mGameService.setConnectionService(mainActivity.mConnectionService);
            mGameService.setPadLightUpTime(bundle.getDouble("PAD_LIGHT_UP_TIME", 0.0));
            mGameService.setPadLightUpTimeDelay(bundle.getDouble("PAD_LIGHT_UP_TIME_DELAY", 0.0));

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    countDownTimer = new CountDownTimer(5000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            Log.i(TAG, "Count down " + (millisUntilFinished + 10));
                            //here you can have your logic to set text to edittext
                            mCountDown.setText(String.valueOf((millisUntilFinished / 1000)));
                        }

                        public void onFinish() {
                            Log.i(TAG, "Game time in seconds: " + mGameTime);
                            mGameService.startGame();
                            setGameView();
                            gameTimer.start();
                        }

                    }.start();
                }
            });
        }
    };

    public StartGameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intentGame = new Intent(getActivity(), GameService.class);

        if (!mGameBounded) {
            getActivity().startService(intentGame);
            getActivity().bindService(intentGame, mGameConnection, BIND_AUTO_CREATE);
        }

        gameIntentFilter = new IntentFilter();
        gameIntentFilter.addAction(PAD_HIT_UI_COUNT_CHANGE);
        gameIntentFilter.addAction(PAD_MISS_UI_COUNT_CHANGE);
        gameIntentFilter.addAction(PAD_UI_COLOR_CHANGE);
        hitReceiver = new HitReceiver();
        getActivity().registerReceiver(hitReceiver, gameIntentFilter);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGameBounded) {
            getActivity().unbindService(mGameConnection);
            mGameBounded = false;
        }
        getActivity().unregisterReceiver(hitReceiver);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity.getSupportActionBar().hide();
        bundle = getArguments();

        mPlayerCount = bundle.getInt("PLAYER_COUNT", 1);

        mGameTime = bundle.getLong("GAME_TIME", 30);
        mGameType = bundle.getString("GAME_TYPE");
        mGameMode = bundle.getString("GAME_MODE");

        if (mGameMode == null || mGameMode.isEmpty()) {
            mGameMode = "Random";
        }

        if(mGameMode.equalsIgnoreCase("Sequence")) {
            sequenceOne = bundle.getIntegerArrayList("SEQUENCE_ONE");
        }
        /*
            playerOneColor = bundle.getString("PLAYER_ONE_COLOR");
            playerTwoColor = bundle.getString("PLAYER_TWO_COLOR");
            playerThreeColor = bundle.getString("PLAYER_THREE_COLOR");
            playerFourColor = bundle.getString("PLAYER_FOUR_COLOR");
        */
        Player player;
        if (mGameType.equalsIgnoreCase("Solo")) {
            playerOneColor = "blue";
            player = new Player(1, playerOneColor);
            players.add(player);
        } else if (mGameType.equalsIgnoreCase("Duo")) {
            player = new Player(1, playerOneColor);
            players.add(player);
            player = new Player(2, playerTwoColor);
            players.add(player);
        } else {

        }


        mCountDown = view.findViewById(R.id.countDown);
        mTimer = view.findViewById(R.id.tvTimer);
        mScoreCount = view.findViewById(R.id.scoreCount);
        mPlayerCountTv = view.findViewById(R.id.playerCount);
        mHitCount = view.findViewById(R.id.hitCount);
        mMissCount = view.findViewById(R.id.missCount);
        mGameModeTv = view.findViewById(R.id.gameMode);
       /*
        mPlayerViewAdapter = new PlayerViewAdapter(getContext(), mPlayerCount);
        mGridView = view.findViewById(R.id.padGameGridView);
        mGridView.setAdapter(mPlayerViewAdapter);
        mGridView.setNumColumns(mPlayerCount);
        mGridView.setVisibility(View.INVISIBLE);
        */
        mTimer.setVisibility(View.INVISIBLE);
        mHitCount.setVisibility(View.INVISIBLE);
        mMissCount.setVisibility(View.INVISIBLE);
        mScoreCount.setVisibility(View.INVISIBLE);
        mPlayerCountTv.setVisibility(View.INVISIBLE);
        mGameModeTv.setVisibility(View.INVISIBLE);
        updateTimerText(mGameTime);
        mPlayer = players.get(0);

        mGameModeTv.setText(mGameMode);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_game, container, false);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
                                  @Override
                                  public boolean onKey(View v, int keyCode, KeyEvent event) {
                                      if (keyCode == KeyEvent.KEYCODE_BACK) {
                                          return true;
                                      }
                                      return false;
                                  }
                              }
        );
        return view;
    }


    private void updateTimerText(long seconds) {
        if (seconds >= 60) {
            int minute = (int) seconds / 60;
            seconds = seconds % 60;
            if (seconds < 10) {
                mTimer.setText(minute + ":0" + seconds);
            } else {
                mTimer.setText(minute + ":" + seconds);
            }
        } else {
            if (seconds < 10) {
                mTimer.setText("00:0" + seconds);
            } else {
                mTimer.setText("00:" + seconds);
            }
        }
    }

    private void setGameView() {
        if (isVisible()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCountDown.setVisibility(View.INVISIBLE);
                    mGameModeTv.setVisibility(View.VISIBLE);
                    mTimer.setVisibility(View.VISIBLE);
                    mHitCount.setVisibility(View.VISIBLE);
                    mMissCount.setVisibility(View.VISIBLE);
                    mScoreCount.setVisibility(View.VISIBLE);
                    mPlayerCountTv.setVisibility(View.VISIBLE);
                    //mGridView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private class HitReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //if a pad was hit then see if it was the correct pad by checking the uuid that sent
            //the hit broadcast vs the uuid that was set after sending the light up command in game

            if (intent.getAction().equals(PAD_HIT_UI_COUNT_CHANGE)) {
                if (isVisible()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mHitCount.setText(String.valueOf(mPlayer.getHitCount()));
                            mScoreCount.setText(String.valueOf(mPlayer.getTotalPoints()));
                        }
                    });
                }
            } else if (intent.getAction().equals(PAD_MISS_UI_COUNT_CHANGE)) {
                if (isVisible()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMissCount.setText(String.valueOf(mPlayer.getMissCount()));
                            mScoreCount.setText(String.valueOf(mPlayer.getTotalPoints()));
                        }
                    });
                }
            }
        }

    }
}