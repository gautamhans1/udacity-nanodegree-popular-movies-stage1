package gautamhans.xyz.popularmoviespt1.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import gautamhans.xyz.popularmoviespt1.R;

public class MovieDetails extends AppCompatActivity {

    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    private static String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w300/";
    private static String END_VOTE = "/10";
    private String movie_id, movie_title;
    private TextView movieTitle, movieTagLine, movieReleasedOn, movieSynopsis, movieRatingText;
    private RatingBar movieRating;
    private ImageView moviePoster;
    private MaterialDialog materialDialog;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moviedetails);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            movie_id = extras.getString("id");
        }

        movieTitle = (TextView) findViewById(R.id.movie_title);
        movieTagLine = (TextView) findViewById(R.id.movie_tag_line);
        movieReleasedOn = (TextView) findViewById(R.id.release_date);
        movieSynopsis = (TextView) findViewById(R.id.movie_synopsis);
        movieRating = (RatingBar) findViewById(R.id.movie_rating);
        moviePoster = (ImageView) findViewById(R.id.movie_poster);
        movieRatingText = (TextView) findViewById(R.id.movie_rating_text);


        materialDialog = new MaterialDialog.Builder(context)
                .backgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarker))
                .titleColor(ContextCompat.getColor(context, R.color.white))
                .contentColor(ContextCompat.getColor(context, R.color.white))
                .title("Loading")
                .progress(true, 0)
                .build();

        new AsyncFetch().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private class AsyncFetch extends AsyncTask<String, String, String> {

        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            materialDialog.setContent("Loading movie details...");
            materialDialog.setCancelable(false);
            materialDialog.show();
        }


        @Override
        protected String doInBackground(String... params) {

            try {
                url = new URL("https://api.themoviedb.org/3/movie/" + movie_id + "?api_key=&language=en-US");
                Log.d("url", String.valueOf(url));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }


            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {

                int response_code = conn.getResponseCode();
                Log.d("response code", String.valueOf(response_code));
                if (response_code == HttpURLConnection.HTTP_OK) {

                    InputStream input = conn.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    return (result.toString());

                } else {

                    return ("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            } finally {
                conn.disconnect();
            }
        }


        @Override
        protected void onPostExecute(String result) {
            materialDialog.dismiss();

            try {
                Log.d("result", result);
                JSONObject jsonObject = new JSONObject(result);
                Log.d("json resp", String.valueOf(jsonObject));
                Picasso.with(MovieDetails.this).load(POSTER_BASE_URL + jsonObject.getString("poster_path"))
                        .placeholder(R.drawable.noposter)
                        .error(R.drawable.noposter)
                        .into(moviePoster);
                movieTitle.setText(jsonObject.getString("original_title"));
                if (jsonObject.getString("tagline") != null) {
                    movieTagLine.setText(jsonObject.getString("tagline"));
                }
                movieRating.setRating((float) jsonObject.getDouble("vote_average"));
                String finalRating = jsonObject.getString("vote_average") + END_VOTE;
                movieRatingText.setText(finalRating);
                movieReleasedOn.setText(jsonObject.getString("release_date"));
                movieSynopsis.setText(jsonObject.getString("overview"));
                try {
                    getSupportActionBar().setTitle(jsonObject.getString("original_title"));
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }
}
