package fiu.com.skillcourt.ui.game;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import fiu.com.skillcourt.R;
import fiu.com.skillcourt.ui.configuration.CreateSequenceFragment;
import fiu.com.skillcourt.ui.main.NonBottomNavigationFragments;

public class GameModeFragment extends NonBottomNavigationFragments {

    private static final String TAG = GameModeFragment.class.getSimpleName();

    private Button mRandomModeButton;
    private Button mManualModeButton;

    public GameModeFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity.setTitle("Select Game Mode");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_mode_select, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRandomModeButton = view.findViewById(R.id.randomGameMode);
        mManualModeButton = view.findViewById(R.id.manualGameMode);

        mRandomModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("GAME_MODE", "Random");

                Fragment fragment = new CreateGameFragment();
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                mainActivity.startFragmentWithBackButton(fragment, fragmentTransaction, true);
            }
        });

        mManualModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new CreateSequenceFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                mainActivity.startFragmentWithBackButton(fragment, fragmentTransaction, true);
            }
        });

    }
}
