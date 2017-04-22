package com.marat.apps.android.pro3.Dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.marat.apps.android.pro3.Adapters.TimetableListViewAdapter;
import com.marat.apps.android.pro3.Interfaces.RegistrationTimeChosenListener;
import com.marat.apps.android.pro3.Models.Box;
import com.marat.apps.android.pro3.R;

public class DialogFragmentTimetableContent extends Fragment implements AdapterView.OnItemClickListener {

    private static final String TAG = "logtag";

    private static final String ARG_BOX = "box";

    private RegistrationTimeChosenListener regTimeChosenDelegate;

    private Box box;

    private ListView timetableListView;
    private TimetableListViewAdapter adapter;

    public static DialogFragmentTimetableContent newInstance(Box box) {
        DialogFragmentTimetableContent fragment = new DialogFragmentTimetableContent();
        Bundle args = new Bundle();
        args.putParcelable(ARG_BOX, box);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        box = getArguments().getParcelable(ARG_BOX);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_timetable_content, container, false);
        timetableListView = (ListView) v.findViewById(R.id.timetableListView);
        adapter = new TimetableListViewAdapter(getContext(), box.getTimetableRows());
        timetableListView.setAdapter(adapter);
        timetableListView.setOnItemClickListener(this);
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        regTimeChosenDelegate = (RegistrationTimeChosenListener) context;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        boolean isOk = adapter.setStartTimePosition(position);
        regTimeChosenDelegate.registrationTimeIsChosen(isOk, box.getBoxId(), adapter.getStartTime());
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "Content: " + "visibility = " + isVisibleToUser);
        if (!isVisibleToUser) {
            if (adapter != null) {
                adapter.clearSlots();
                regTimeChosenDelegate.registrationTimeIsChosen(false, box.getBoxId(), "");
            }
        }
    }
}
