package fiu.com.skillcourt.ui.main;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import fiu.com.skillcourt.R;
import fiu.com.skillcourt.ui.game.CreateGameFragment;
import fiu.com.skillcourt.ui.game.GameModeFragment;

/**
 * Created by Sergio Rosales on 2/6/18.
 */
public class HomeFragment extends BaseFragment {

    private FloatingActionButton mPlayButton;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity.setTitle("SkillCourt");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        mPlayButton = getActivity().findViewById(R.id.playButton);

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mainActivity.isConnected()) {
                    if (mainActivity.mConnectionService.getPadsConnected() > 0) {
                        Fragment fragment;
                        if(mainActivity.mConnectionService.getPadsConnected() > 1) {
                            fragment = new GameModeFragment();
                        }else{
                            Bundle bundle = new Bundle();
                            bundle.putString("GAME_MODE", "Random");
                            fragment = new CreateGameFragment();
                            fragment.setArguments(bundle);
                        }
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        mainActivity.startFragmentWithBackButton(fragment, fragmentTransaction, true);
                    } else {
                        Toast.makeText(getActivity(), "You must be connected to at least 1 pad to play", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "You be must connected to a network.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
