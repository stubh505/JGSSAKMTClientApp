package com.joythakur.jgssakmtapp;

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

import com.joythakur.jgssakmtapp.model.Blogs;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ViewBlogFragment extends Fragment {

    private TextView blogTitle;
    private TextView blogContent;
    private TextView posted;
    private TextView edited;
    private ImageView blogImage;

    public static ViewBlogFragment newInstance() {
        return new ViewBlogFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_blog_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        assert getArguments() != null;
        Integer blogId = ViewBlogFragmentArgs.fromBundle(getArguments()).getBlogId();

        GetBlog blog = new GetBlog();
        blog.execute("http://jgssakmtback.herokuapp.com/jgssakmt_backend/BlogsAPI/getBlog/"+ blogId);

        blogTitle = requireActivity().findViewById(R.id.viewBlogTitle);
        blogContent = requireActivity().findViewById(R.id.viewBlogContent);
        blogImage = requireActivity().findViewById(R.id.viewBlogImage);
        posted = requireActivity().findViewById(R.id.viewBlogPosted);
        edited = requireActivity().findViewById(R.id.viewBlogEdited);
    }

    private class GetBlog extends AsyncTask<String, Void, String> {

        JSONObject jsonObject;
        Blogs b;

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

                b = new Blogs();
                b.setExcerpt(jsonObject.getString("excerpt"));
                b.setTitle(jsonObject.getString("title"));
                b.setEdited(Timestamp.valueOf(jsonObject.getString("edited").replace('T', ' ')));
                b.setPosted(Timestamp.valueOf(jsonObject.getString("posted").replace('T', ' ')));
                b.setImgUrl(jsonObject.getString("imgUrl"));
                b.setContent(jsonObject.getString("content"));

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

        private void setData(Blogs b) {
            Spanned htmlAsSpanned = Html.fromHtml(b.getContent());
            blogContent.setText(htmlAsSpanned);
            blogTitle.setText(b.getTitle());
            SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yy hh:mm a", Locale.ENGLISH);
            posted.setText(format.format(b.getPosted()));
            edited.setText(format.format(b.getEdited()));

            new DownloadImageTask(blogImage).execute(b.getImgUrl());
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
