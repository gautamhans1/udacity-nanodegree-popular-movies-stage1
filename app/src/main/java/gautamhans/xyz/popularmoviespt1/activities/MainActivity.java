package gautamhans.xyz.popularmoviespt1.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import gautamhans.xyz.popularmoviespt1.R;
import gautamhans.xyz.popularmoviespt1.adapters.PopularMovies;
import gautamhans.xyz.popularmoviespt1.models.Results;
import gautamhans.xyz.popularmoviespt1.util.ConnectivityReceiver;
import gautamhans.xyz.popularmoviespt1.util.MyApplication;

public class MainActivity extends AppCompatActivity implements PopularMovies.MovieClickListener,
        ConnectivityReceiver.ConnectivityReceiverListener {

    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    private static final String noMovies = "Sorry, no movies found.";
    private static String type = "popular";
    boolean isLandscape = false;
    MaterialDialog materialDialog;
    private RecyclerView mRecyclerView;
    private PopularMovies mAdapter;
    private FloatingActionMenu floatingActionMenu;
    private FloatingActionButton fab_popular, fab_top_rated;
    private TextView connectionErrorText;
    private Button refreshButton;
    private ImageView noInternetIV;
    private Context context = this;
    private FloatingActionButton floatingActionButton;
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_popular_movies:

                    if (isOnline()) {
                        type = "popular";
                        new AsyncFetchPopular().execute();
                    } else {
                        showError();
                    }
                    break;

                case R.id.fab_top_rated:
                    if (isOnline()) {
                        type = "top_rated";
                        new AsyncFetchPopular().execute();
                    } else {
                        showError();
                    }
                    break;
                case R.id.refresh_button:
                    if (isOnline()) {
                        new AsyncFetchPopular().execute();
                    } else {
                        showError();
                    }
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        floatingActionMenu = (FloatingActionMenu) findViewById(R.id.floating_menu);
        fab_popular = (FloatingActionButton) findViewById(R.id.fab_popular_movies);
        fab_top_rated = (FloatingActionButton) findViewById(R.id.fab_top_rated);
        connectionErrorText = (TextView) findViewById(R.id.connection_error);
        refreshButton = (Button) findViewById(R.id.refresh_button);
        noInternetIV = (ImageView) findViewById(R.id.no_internet_image);
        floatingActionMenu = (FloatingActionMenu) findViewById(R.id.floating_menu);
        floatingActionMenu.setMenuButtonColorNormal(ContextCompat.getColor(context, R.color.colorAccent));
        floatingActionMenu.setMenuButtonColorPressed(ContextCompat.getColor(context, R.color.fab_pressed));


        fab_popular.setOnClickListener(clickListener);
        fab_top_rated.setOnClickListener(clickListener);
        refreshButton.setOnClickListener(clickListener);

        materialDialog = new MaterialDialog.Builder(context)
                .backgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarker))
                .titleColor(ContextCompat.getColor(context, R.color.white))
                .contentColor(ContextCompat.getColor(context, R.color.white))
                .title("Loading")
                .progress(true, 0)
                .build();

        if (isOnline()) {
            new AsyncFetchPopular().execute();
        } else {
            showError();
        }
    }

    private void showError() {
        noInternetIV.setVisibility(View.VISIBLE);
        connectionErrorText.setVisibility(View.VISIBLE);
        refreshButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMovieClick(String id) {
        Intent intent = new Intent(MainActivity.this, MovieDetails.class);
        Bundle extras = new Bundle();
        extras.putString("id", id);
        intent.putExtras(extras);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.d("isConnected", String.valueOf(isConnected));
        if (isConnected) {
            new AsyncFetchPopular().execute();
        } else {
            showError();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.getInstance().setConnectivityListener(this);
    }

    private void showNoMovieError() {
        noInternetIV.setImageResource(R.drawable.no_movies);
        connectionErrorText.setText(noMovies);
        noInternetIV.setVisibility(View.VISIBLE);
        connectionErrorText.setVisibility(View.VISIBLE);
        refreshButton.setVisibility(View.VISIBLE);
    }

    private class AsyncFetchPopular extends AsyncTask<String, String, String> {

//        ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);

        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (type == "popular")
                materialDialog.setContent("Loading popular movies...");
            else
                materialDialog.setContent("Loading top rated movies...");
            materialDialog.setCancelable(false);
            materialDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {

            try {
                url = new URL("http://api.themoviedb.org/3/movie/" + type + "?api_key=");
                Log.d("url", String.valueOf(url));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return e.toString();
            }


            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");

                conn.setDoOutput(true);
            } catch (IOException e) {
                e.printStackTrace();
                showNoMovieError();
            }

            try {

                int response_code = conn.getResponseCode();

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
                showNoMovieError();
                return e.toString();
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
//            super.onPostExecute(s);
            materialDialog.dismiss();
            try {
                refreshButton.setVisibility(View.GONE);
                connectionErrorText.setVisibility(View.GONE);
                noInternetIV.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            List<Results> data = new ArrayList<>();

            materialDialog.dismiss();

            try {
                JSONObject jsonObject = new JSONObject(result);

                JSONArray jArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    Results movieData = new Results();
                    movieData.setTitle(json_data.getString("title"));
                    movieData.setPoster_path(json_data.getString("poster_path"));
                    movieData.setId(json_data.getString("id"));
                    data.add(movieData);
                }

                mRecyclerView = (RecyclerView) findViewById(R.id.rv_popular_movies);
                mAdapter = new PopularMovies(MainActivity.this, data, MainActivity.this);
                RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(MainActivity.this, 2);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdapter);


                try {
                    if (type == "popular")
                        getSupportActionBar().setTitle("Popular Movies");
                    else
                        getSupportActionBar().setTitle("Top Rated Movies");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                showNoMovieError();
            }
        }
    }
}
