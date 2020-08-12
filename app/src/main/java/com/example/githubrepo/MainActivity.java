package com.example.githubrepo;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import com.example.githubrepo.datafrominternet.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    EditText mEditText;
    TextView mDisplay_url;
    TextView mJson_result;
    TextView mError_message;
    ProgressBar mProgressBar;
    private static final int LOADER_UNIQUE_ID = 22;
    private static final String QUERY_URL_KEY_FOR_SAVE_INSTANCE_STATE = "queryURL";
    private static final String BUNDLE_KEY_FOR_URL_BUILT_FROM_USER_INPUT="urlBuiltFromUserInput";


    //------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText=findViewById(R.id.search_box);
        mDisplay_url=findViewById(R.id.display_url);
        mJson_result=findViewById(R.id.json_result);
        mError_message=findViewById(R.id.error_message_display);
        mProgressBar=findViewById(R.id.loading_indicator);


        if (savedInstanceState != null) {
            String queryURL = savedInstanceState.getString(QUERY_URL_KEY_FOR_SAVE_INSTANCE_STATE);

            mDisplay_url.setText(queryURL);
        }
        LoaderManager.getInstance(this).initLoader(LOADER_UNIQUE_ID,null,this);


    }

    //------------------------------------------------------------------------------------------



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemThatWasClickedId = item.getItemId();
        if(itemThatWasClickedId == R.id.action_search){

                makeGithubSearchQuery();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //------------------------------------------------------------------------------------------


    private void makeGithubSearchQuery(){
        String githubQueryEnteredByUser = mEditText.getText().toString();

        if(TextUtils.isEmpty(githubQueryEnteredByUser)){
            mDisplay_url.setText("No query entered, nothing to search for.");
            mJson_result.setText("Enter something to search for");
            return;
        }
        URL githubSearchUrlCreatedFromUserInput = NetworkUtils.buildUrl(githubQueryEnteredByUser);
        mDisplay_url.setText(githubSearchUrlCreatedFromUserInput.toString());

        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_FOR_URL_BUILT_FROM_USER_INPUT, githubSearchUrlCreatedFromUserInput.toString());



        LoaderManager loaderManager=getSupportLoaderManager();
        Loader<String> githubSearchLoader=loaderManager.getLoader(LOADER_UNIQUE_ID);
        if(githubSearchLoader==null){
            loaderManager.initLoader(LOADER_UNIQUE_ID,bundle,this);
        }
        else {
            loaderManager.restartLoader(LOADER_UNIQUE_ID,bundle,this);
        }


    }



    //------------------------------------------------------------------------------------------


    private void showJsonDataView() {
        mError_message.setVisibility(View.INVISIBLE);
        mJson_result.setVisibility(View.VISIBLE);
    }

    //------------------------------------------------------------------------------------------


    private void showErrorMessage() {
        mJson_result.setVisibility(View.INVISIBLE);
        mError_message.setVisibility(View.VISIBLE);
    }

    //------------------------------------------------------------------------------------------


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        String queryURL = mDisplay_url.getText().toString();
        outState.putString(QUERY_URL_KEY_FOR_SAVE_INSTANCE_STATE, queryURL);


    }


    //------------------------------------------------------------------------------------------


    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable  final Bundle args) {

        return new AsyncTaskLoader<String>(this) {

            String githubJsonResult;

            @Nullable

            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                if (args == null) {
                    return;
                }

                if(githubJsonResult!=null)
                    deliverResult(githubJsonResult);
                else {
                    Log.d(MainActivity.class.getSimpleName(), "Entering in loadInBackground");

                    mProgressBar.setVisibility(View.VISIBLE);
                    forceLoad();
                }



            }

            @Override
            public String loadInBackground() {

                if(TextUtils.isEmpty(args.getString(BUNDLE_KEY_FOR_URL_BUILT_FROM_USER_INPUT)))
                    return null;

                try {
                    String githubResultInJson=NetworkUtils
                            .getResponseFromHttpUrl(new URL(args.getString(BUNDLE_KEY_FOR_URL_BUILT_FROM_USER_INPUT)));

                    Log.d(MainActivity.class.getSimpleName(), "loadInBackground got called");

                    return githubResultInJson;
                }catch (IOException e){
                    e.printStackTrace();
                    return null;
                }

            }

            @Override
            public void deliverResult(@Nullable String githubJsonResult) {
                this.githubJsonResult=githubJsonResult;
                Log.d(MainActivity.class.getSimpleName(), "DeliverResult got called");
                super.deliverResult(githubJsonResult);

            }
        };

    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String githubResultInJson) {

        mProgressBar.setVisibility(View.INVISIBLE);
        if(githubResultInJson==null)
            showErrorMessage();
        else {
            mJson_result.setText(githubResultInJson);
            showJsonDataView();
        }
        Log.d(MainActivity.class.getSimpleName(), "OnLoadFinished got called");


    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }


}
