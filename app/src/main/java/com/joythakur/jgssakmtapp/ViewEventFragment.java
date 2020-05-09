package com.joythakur.jgssakmtapp;

import androidx.lifecycle.ViewModelProviders;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.joythakur.jgssakmtapp.model.Events;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ViewEventFragment extends Fragment {

    private ViewEventViewModel mViewModel;
    private TextView eventName;
    private TextView eventDescription;
    private TextView startTime;
    private TextView endTime;
    private ImageView eventImage;

    public static ViewEventFragment newInstance() {
        return new ViewEventFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_event_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ViewEventViewModel.class);

        assert getArguments() != null;
        int eventId = ViewEventFragmentArgs.fromBundle(getArguments()).getEventId();

        GetEvent event = new GetEvent();
        event.execute("http://jgssakmtback.herokuapp.com/jgssakmt_backend/EventsAPI/getEvent/"+ eventId);

        eventName = requireActivity().findViewById(R.id.viewEventName);
        eventDescription = requireActivity().findViewById(R.id.viewEventContent);
        eventImage = requireActivity().findViewById(R.id.viewEventImage);
        startTime = requireActivity().findViewById(R.id.viewEventStartTime);
        endTime = requireActivity().findViewById(R.id.viewEventEndTime);
    }

    private class GetEvent extends AsyncTask<String, Void, String> {

        JSONObject jsonObject;
        Events b;

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

                jsonObject = new JSONObject(res.toString());

                b = new Events();
                b.setExcerpt(jsonObject.getString("excerpt"));
                b.setName(jsonObject.getString("name"));
                b.setEndTime(Timestamp.valueOf(jsonObject.getString("endTime").replace('T', ' ')));
                b.setStartTime(Timestamp.valueOf(jsonObject.getString("startTime").replace('T', ' ')));
                b.setImgUrl(jsonObject.getString("imgUrl"));
                b.setDescription(jsonObject.getString("description"));

                return res.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public void onPostExecute(String s) {
            super.onPostExecute(s);
            setData(b);
        }

        private void setData(Events b) {
            Spanned htmlAsSpanned = Html.fromHtml(b.getDescription());
            eventDescription.setText(htmlAsSpanned);
            eventName.setText(b.getName());
            SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yy hh:mm a", Locale.ENGLISH);
            startTime.setText(format.format(b.getStartTime()));
            endTime.setText(format.format(b.getEndTime()));

            new DownloadImageTask(eventImage).execute(b.getImgUrl());
        }
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView bmImage;

        DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urlDisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mIcon11;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
