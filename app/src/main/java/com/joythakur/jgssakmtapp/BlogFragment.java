package com.joythakur.jgssakmtapp;

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
import android.widget.TextView;

import com.joythakur.jgssakmtapp.model.Blogs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class BlogFragment extends Fragment {

    private ArrayList<Blogs> blogList;
    private TextView noData;
    private BlogRecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView recyclerView;

    public static BlogFragment newInstance() {
        return new BlogFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.blog_fragment, container, false);
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

    private class GetBlogs extends AsyncTask<String, Void, String> {

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

                Blogs b;

                jsonArray = new JSONArray(res.toString());

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    b = new Blogs();
                    b.setBlogId(jsonObject.getInt("blogId"));
                    b.setImgUrl(jsonObject.getString("imgUrl"));
                    b.setTitle(jsonObject.getString("title"));
                    b.setExcerpt(jsonObject.getString("excerpt"));
                    blogList.add(b);
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
            noData.setAlpha(0);
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        blogList = new ArrayList<>();

        GetBlogs getBlogs = new GetBlogs();

        getBlogs.execute("http://jgssakmtback.herokuapp.com/jgssakmt_backend/BlogsAPI/getAllBlogs");

        recyclerViewAdapter = new BlogRecyclerViewAdapter(blogList, requireActivity().getApplicationContext());

        recyclerView = getActivity().findViewById(R.id.blogRecyclerView);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        noData = getActivity().findViewById(R.id.noBlogs);
    }

}
