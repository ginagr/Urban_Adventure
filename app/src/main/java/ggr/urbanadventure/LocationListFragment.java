package ggr.urbanadventure;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class LocationListFragment extends Fragment {

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private RecyclerView locationRecyclerView;
    private LocationAdapter adapter;
    private boolean subtitleVisible;
    //created to get all areaLocations for our singleton class
    //LocationLab locationLab;
    //List<AreaLocation> allLocations;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        allLocations.addAll(locationLab.getLocations());
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_list, container, false);

        locationRecyclerView = (RecyclerView) view
                .findViewById(R.id.location_recycler_view);
        locationRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (savedInstanceState != null) {
            subtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, subtitleVisible);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_location_list, menu);

        String vi = getString(R.string.visited);

        MenuItem vis = menu.findItem(R.id.menu_item_show_subtitle);

        SpannableString spanString = new SpannableString(vi);
        spanString.setSpan(new ForegroundColorSpan(Color.GRAY), 0, spanString.length(), 0); // fix the color to gray

        vis.setTitle(spanString);


        updateLocationNum();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.map:
                goToMapActivity();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateLocationNum() {
        LocationLab locationLab = LocationLab.get(getActivity());
        int count = 0;
        List<AreaLocation> loc = locationLab.getLocations();
        for(int i = 0; i < loc.size(); i++){
            if(!loc.get(i).isHasVisited()) {
                count++;
            }
        }

        String locationCount = "" + count;
        String subtitle = getString(R.string.num_locations, locationCount);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    private void updateUI() {
        LocationLab locationLab = LocationLab.get(getActivity());
        List<AreaLocation> areaLocations = locationLab.getLocations();

        if (adapter == null) {
            adapter = new LocationAdapter(areaLocations);
            locationRecyclerView.setAdapter(adapter);
        } else {
            adapter.setAreaLocations(areaLocations);
            adapter.notifyDataSetChanged();
        }



        updateLocationNum();
    }

    private class LocationHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView titleTextView;
        private TextView locationTypeTextView;
        private AppCompatCheckBox visitedCheckBox;
        private TextView distanceTextView;
        Brain brain = Brain.getInstance();

        private AreaLocation areaLocation;
        public LocationHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            titleTextView = (TextView) itemView.findViewById(R.id.list_item_location_title_text_view);
            locationTypeTextView = (TextView) itemView.findViewById(R.id.list_item_location_type_text_view);
            visitedCheckBox = (AppCompatCheckBox) itemView.findViewById(R.id.list_item_location_visited_check_box);
            distanceTextView = (TextView) itemView.findViewById(R.id.list_item_location_type_distance_view);



        }

        public void bindLocation(AreaLocation bAreaLocation) {
            areaLocation = bAreaLocation;
            titleTextView.setText(areaLocation.getTitle());
            locationTypeTextView.setText(areaLocation.getLocationType().toString());
            visitedCheckBox.setChecked(areaLocation.isHasVisited());

            displayDistanceFromUserToLocation(distanceTextView);

//            double distance = brain.getDistanceBetweenTwo(areaLocation.getLatitude(),areaLocation.getLongitude(),
//                    brain.getCurrentLocation().getLatitude(),brain.getCurrentLocation().getLongitude());
//
//            String dist = Double.toString(distance);
//            distanceTextView.setText(dist);

            if(areaLocation.isHasVisited()){
                itemView.setBackgroundColor(Color.parseColor("#ecf0f1"));
                titleTextView.setTextColor(Color.parseColor("#bdc3c7"));
                locationTypeTextView.setTextColor(Color.parseColor("#bdc3c7"));
            }
        }

        public void displayDistanceFromUserToLocation(TextView distanceText) {
            if (brain.getCurrentLocation() != null) {
                double distance = brain.getDistanceBetweenTwo(areaLocation.getLatitude(), areaLocation.getLongitude(),
                        brain.getCurrentLocation().getLatitude(),brain.getCurrentLocation().getLongitude());


                // round distance
                distance = Math.round(distance * 10) / 10.0;

                String dist = Double.toString(distance);
                distanceText.setText(" -   " + dist + " mi");
            } else {
                distanceText.setText("");
            }
        }


        @Override
        public void onClick(View v) {
            Intent intent = LocationPagerActivity.newIntent(getActivity(), areaLocation.getId());
            startActivity(intent);
        }
    }

    private class LocationAdapter extends RecyclerView.Adapter<LocationHolder> {

        private List<AreaLocation> areaLocations;

        public LocationAdapter(List<AreaLocation> lAreaLocations) {
            areaLocations = lAreaLocations;
        }

        @Override
        public LocationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_location, parent, false);
            return new LocationHolder(view);
        }

        @Override
        public void onBindViewHolder(LocationHolder holder, int position) {
            AreaLocation areaLocation = areaLocations.get(position);
            holder.bindLocation(areaLocation);
        }

        @Override
        public int getItemCount() {
            return areaLocations.size();
        }

        public void setAreaLocations(List<AreaLocation> areaLocations) {
            this.areaLocations = areaLocations;
        }
    }

    public boolean goToMapActivity() {
        Intent intent = new Intent(getActivity(), MapsActivity.class);
        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT); // Reopen activity if already exists.
        startActivity(intent);
        return true;
    }

}
