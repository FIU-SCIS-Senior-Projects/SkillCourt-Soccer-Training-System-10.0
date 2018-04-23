package fiu.com.skillcourt.ui.main;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import fiu.com.skillcourt.R;

/**
 * Created by Joshua Mclendon on 3/10/18.
 */

public class NonBottomNavigationFragments extends BaseFragment {
    protected String mCurrentColor;
    private static final String TAG = NonBottomNavigationFragments.class.getSimpleName();

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity.navigation.setVisibility(View.INVISIBLE);
    }

    protected void changeColor(String color, View view) {
        TextView led1 = view.findViewById(R.id.led_1);
        TextView led2 = view.findViewById(R.id.led_2);
        TextView led3 = view.findViewById(R.id.led_3);
        TextView led4 = view.findViewById(R.id.led_4);
        TextView led5 = view.findViewById(R.id.led_5);
        switch (color.toLowerCase()) {
            case "white":
                mCurrentColor = "white";
                led1.setBackgroundColor(Color.WHITE);
                led2.setBackgroundColor(Color.WHITE);
                led3.setBackgroundColor(Color.WHITE);
                led4.setBackgroundColor(Color.WHITE);
                led5.setBackgroundColor(Color.WHITE);
                Log.i(TAG, "Changing to white");
                break;
            case "blue":
                mCurrentColor = "blue";
                led1.setBackgroundColor(Color.parseColor("#42bcf4"));
                led2.setBackgroundColor(Color.parseColor("#42bcf4"));
                led3.setBackgroundColor(Color.parseColor("#42bcf4"));
                led4.setBackgroundColor(Color.parseColor("#42bcf4"));
                led5.setBackgroundColor(Color.parseColor("#42bcf4"));
                Log.i(TAG, "Changing to blue");
                break;
            case "green":
                mCurrentColor = "green";
                led1.setBackgroundColor(Color.GREEN);
                led2.setBackgroundColor(Color.GREEN);
                led3.setBackgroundColor(Color.GREEN);
                led4.setBackgroundColor(Color.GREEN);
                led5.setBackgroundColor(Color.GREEN);
                Log.i(TAG, "Changing to green");
                break;
            case "red":
                mCurrentColor = "red";
                led1.setBackgroundColor(Color.RED);
                led2.setBackgroundColor(Color.RED);
                led3.setBackgroundColor(Color.RED);
                led4.setBackgroundColor(Color.RED);
                led5.setBackgroundColor(Color.RED);
                Log.i(TAG, "Changing to red");
                break;
        }
    }
}
