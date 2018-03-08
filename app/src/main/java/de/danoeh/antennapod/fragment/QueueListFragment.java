package de.danoeh.antennapod.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;


public class QueueListFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "QueueListFragment";

    //List of queue fragments
    public List<QueueFragment> queueList = new List<QueueFragment>() {

        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @NonNull
        @Override
        public Iterator<QueueFragment> iterator() {
            return null;
        }

        @NonNull
        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @NonNull
        @Override
        public <T> T[] toArray(@NonNull T[] ts) {
            return null;
        }

        @Override
        public boolean add(QueueFragment queueFragment) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(@NonNull Collection<?> collection) {
            return false;
        }

        @Override
        public boolean addAll(@NonNull Collection<? extends QueueFragment> collection) {
            return false;
        }

        @Override
        public boolean addAll(int i, @NonNull Collection<? extends QueueFragment> collection) {
            return false;
        }

        @Override
        public boolean removeAll(@NonNull Collection<?> collection) {
            return false;
        }

        @Override
        public boolean retainAll(@NonNull Collection<?> collection) {
            return false;
        }

        @Override
        public void clear() {

        }

        @Override
        public QueueFragment get(int i) {
            return null;
        }

        @Override
        public QueueFragment set(int i, QueueFragment queueFragment) {
            return null;
        }

        @Override
        public void add(int i, QueueFragment queueFragment) {

        }

        @Override
        public QueueFragment remove(int i) {
            return null;
        }

        @Override
        public int indexOf(Object o) {
            return 0;
        }

        @Override
        public int lastIndexOf(Object o) {
            return 0;
        }

        @NonNull
        @Override
        public ListIterator<QueueFragment> listIterator() {
            return null;
        }

        @NonNull
        @Override
        public ListIterator<QueueFragment> listIterator(int i) {
            return null;
        }

        @NonNull
        @Override
        public List<QueueFragment> subList(int i, int i1) {
            return null;
        }
    };

    //button to add queues to list
    public Button addButton;


    // Called to do initial creation of fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // Retains fragment instance across Activity re-creation
        setHasOptionsMenu(true); // Fragment would like to participate in populating the options menu
    }

    // Called to have fragment instantiate its user interface view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_queue_list, container, false);

        addButton = (Button) root.findViewById(R.id.addQueue);
        addButton.setOnClickListener(this);

        return root;
    }


    //adds a queue to the list of queues
    @Override
    public void onClick(View v){
        createNewQueue();
    }

    // Called when fragment is visible to the user
    @Override
    public void onStart(){
        super.onStart();
    }

    // Called when fragment is visible to the user and actively running
    @Override
    public void onResume(){
        super.onResume();
    }

    // Called when the fragment is no longer resumed
    @Override
    public void onPause(){
        super.onPause();
    }

    /*
    * Called when the view previously created by onCreateView has been detached from the fragment
    * Allows the fragment to clean up resources associated with its View
    */
    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }

    public List<QueueFragment> getQueuesList() {
        return this.queueList;
    }

    public void createNewQueue(){
        QueueFragment toAdd = new QueueFragment();
        this.queueList.add(toAdd);
    }
}
