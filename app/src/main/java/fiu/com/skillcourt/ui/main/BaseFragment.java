package fiu.com.skillcourt.ui.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by Joshua Mclendon on 2/26/18.
 */

public class BaseFragment extends Fragment {
    protected MainActivity mainActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mainActivity.navigation.getVisibility() == View.INVISIBLE) {
            mainActivity.navigation.setVisibility(View.VISIBLE);
        }
    }
}
