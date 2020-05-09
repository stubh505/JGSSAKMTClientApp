package com.joythakur.jgssakmtapp;

import androidx.lifecycle.ViewModelProviders;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joythakur.jgssakmtapp.model.Events;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;

public class EventFragment extends Fragment {

    private EventViewModel mViewModel;
    private ArrayList<Events> eventList;
    private EventRecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView recyclerView;


    public static EventFragment newInstance() {
        return new EventFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(EventViewModel.class);

        eventList = new ArrayList<>();

        GetEvents getEvents = new GetEvents();

        getEvents.execute("http://jgssakmtback.herokuapp.com/jgssakmt_backend/EventsAPI/getAllEvents");

        recyclerViewAdapter = new EventRecyclerViewAdapter(eventList, requireActivity().getApplicationContext());

        recyclerView = requireActivity().findViewById(R.id.eventRecyclerView);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
    }

    private class GetEvents extends AsyncTask<String, Void, String> {

        JSONArray jsonArray;

        @Override
        protected String doInBackground(String... apis) {
            try {
                URL url = new URL(apis[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                InputStreamReader ips = new InputStreamReader(inputStream);
                StringBuilder res = new StringBuilder();
                int data = ips.read();
                while (data != -1) {
                    res.append((char) data);
                    data = ips.read();
                }

                Events b;

                jsonArray = new JSONArray(res.toString());

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    b = new Events();
                    b.setEventId(jsonObject.getInt("eventId"));
                    b.setImgUrl(jsonObject.getString("imgUrl"));
                    b.setName(jsonObject.getString("name"));
                    b.setEndTime(Timestamp.valueOf(jsonObject.getString("endTime").replace('T', ' ')));
                    b.setStartTime(Timestamp.valueOf(jsonObject.getString("startTime").replace('T', ' ')));
                    b.setExcerpt(jsonObject.getString("excerpt"));
                    eventList.add(b);
                }

                return res.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public void onPostExecute(String s) {
            super.onPostExecute(s);
            recyclerView.setAdapter(recyclerViewAdapter);
        }

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(new GridLayoutManager(
                    requireActivity().getApplicationContext(), 2, LinearLayoutManager.VERTICAL, false));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(
                    requireActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        }
    }


}
