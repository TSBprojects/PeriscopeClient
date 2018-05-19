package ru.sstu.vak.periscopeclient.viewPager.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.sstu.vak.periscopeclient.R;

/**
 * Created by Anton on 24.04.2018.
 */

public class HomeFragment extends Fragment implements View.OnClickListener {

    private static final String ARGUMENT_PAGE_NUMBER = "page_number";


    private int pageNumber;

//    public int getClickedNoteId() {
//        return clickedNoteId;
//    }
//
//    public void setClickedNoteId(int clickedNoteId) {
//        this.clickedNoteId = clickedNoteId;
//    }

    public static HomeFragment newInstance(int pageNum) {
        HomeFragment pageFragment = new HomeFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, pageNum);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View currentView = inflater.inflate(R.layout.home_fragment, null);
//        LinearLayout notes_linearLayout = (LinearLayout) currentView.findViewById(R.id.notes_linearLayout);
//        NotesFactory nf = new NotesFactory(this.getContext(), notes_linearLayout, this);
//
//        try (DatabaseWrapper dbw = new DatabaseWrapper(MainActivity.getInstance(), "myDB")) {
//            Note[] notes = dbw.getAllNotes();
//            for (int i = 0; i < notes.length; i++) {
//                nf.addNoteToScreen(notes[i].getId(), notes[i].getDate(), notes[i].getTitle(), notes[i].getBody(), dbw.getTagsByNoteId(notes[i].getId()));
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return currentView;
    }

    @Override
    public void onClick(View view) {
//        MainActivity.getInstance().initializeFragments();
//        clickedNoteId = view.getId();
//        Intent intent = new Intent(MainActivity.getInstance(), NoteEditActivity.class);
//        startActivity(intent);
    }
}