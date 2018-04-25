package fiu.com.skillcourt.ui.configuration;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import fiu.com.skillcourt.R;
import fiu.com.skillcourt.adapters.PadViewAdapter;
import fiu.com.skillcourt.structures.Pad;
import fiu.com.skillcourt.ui.game.CreateGameFragment;
import fiu.com.skillcourt.ui.main.NonBottomNavigationFragments;

public class CreateSequenceFragment extends NonBottomNavigationFragments{
    private static final String TAG = CreateSequenceFragment.class.getSimpleName();
    private Button mUndoButton;
    private Button mSaveButton;
    private TextView mNewSeq;
    private GridView mGridView;
    private PadViewAdapter mPadViewAdapter;
    private ArrayList<Integer> sequence = new ArrayList<>();

    public CreateSequenceFragment(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity.setTitle("Create Sequence");
        mCurrentColor = "white";
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_sequence, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSaveButton = view.findViewById(R.id.createSeqBtn);
        mUndoButton = view.findViewById(R.id.undoSeqAddBtn);
        mNewSeq = view.findViewById(R.id.newSequence);
        mGridView = view.findViewById(R.id.padSeqGridView);
        mPadViewAdapter = new PadViewAdapter(getContext(), mainActivity.mConnectionService.getActivePads());
        mGridView.setNumColumns(2);
        mGridView.setAdapter(mPadViewAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, int position, long id) {
                if(sequence.size() == 8){
                    Toast.makeText(getActivity(), "Your sequence is full", Toast.LENGTH_SHORT).show();
                }else {
                    sequence.add(position + 1);
                    updateNewSeqText();
                }
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!sequence.isEmpty()){
                    Bundle bundle = new Bundle();
                    bundle.putIntegerArrayList("SEQUENCE_ONE", sequence);
                    bundle.putString("GAME_MODE", "Sequence");
                    Fragment fragment = new CreateGameFragment();
                    fragment.setArguments(bundle);
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    mainActivity.startFragmentWithBackButton(fragment, fragmentTransaction, false);
                }else{
                    Toast.makeText(getActivity(), "Your sequence is empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mUndoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!sequence.isEmpty()){
                    sequence.remove(sequence.size()-1);
                    updateNewSeqText();
                }else{
                    Toast.makeText(getActivity(), "You don't have anything to undo", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void updateNewSeqText(){
        if(sequence.isEmpty()){
            mNewSeq.setText("");
        }else{
            String text = "";
            for(int i = 0; i < sequence.size(); i++){
                if(i == sequence.size()-1){
                    text += String.valueOf(sequence.get(i));
                }else {
                    text += String.valueOf(sequence.get(i)) + " ";
                }
            }

            mNewSeq.setText(text);
        }
    }

}
