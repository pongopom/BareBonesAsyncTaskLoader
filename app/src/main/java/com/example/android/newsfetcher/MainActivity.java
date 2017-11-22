    package com.example.android.newsfetcher;

    import android.app.LoaderManager;
    import android.content.AsyncTaskLoader;
    import android.content.Loader;
    import android.net.Uri;
    import android.os.Bundle;
    import android.support.v7.app.AppCompatActivity;
    import android.widget.TextView;

    import java.io.IOException;
    import java.io.InputStream;
    import java.net.HttpURLConnection;
    import java.net.MalformedURLException;
    import java.net.URL;
    import java.util.Scanner;

    //Make MainActivity impliment LoaderCallbacks
    public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>{

        public static final int BOOK_LOADER = 22;
        //does a fetch from google books change the isbn number to your favorite book and see if it can find it
        public static final String BOOK_URL =  "https://www.googleapis.com/books/v1/volumes?q=isbn:1430264543";
        TextView mResultTextView ;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            //have a textView in your layout xml with this id
            mResultTextView = (TextView)findViewById(R.id.tv_result);
            goAndFetchMyJson(BOOK_URL);
        }

        private void goAndFetchMyJson(String url) {
            //create a new bundle
            Bundle bundle = new Bundle();
            //Add a key value pair of the past in url string to the bundle
            bundle.putString("UrlKey",url);
            LoaderManager loaderManager = getLoaderManager();
            Loader<String> loader = loaderManager.getLoader(BOOK_LOADER);
            if(loader==null){
                // If we dont have a loader lets create one
                System.out.println("new loader created");
                loaderManager.initLoader(BOOK_LOADER, bundle, this);
            }else{
                // over wise lets restart the loader we have
                System.out.println("restarting the loader");
                loaderManager.restartLoader(BOOK_LOADER, bundle, this);
            }
        }

    /*----------------------------------------START OF LOADERCALLBACKS OVERRIDE METHODS----------------------------*/
        @Override
        public Loader<String> onCreateLoader(int i, final Bundle bundle) {
            // Create a new AsyncLoader Instance
            return new  AsyncTaskLoader<String>(this) {
                /*----START OF ASYNCTASKLOADER OVERRIDE METHODES----*/
                @Override
                public String loadInBackground() {
                 // do the backGround work on another thread
                    String uri = bundle.getString("UrlKey");
                    URL url = buildUrl(uri);
                    String operationResultString="";
                    try {
                        operationResultString = MainActivity.getResponseFromHttpUrl(url);//This just create a HTTPUrlConnection and return result in strings
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return operationResultString;
                }

                @Override
                protected void onStartLoading() {
                    //If we had a spinner we could start it here
                    forceLoad();
                }
               /*----START OF ASYNCTASKLOADER OVERRIDE METHODES----*/
            };
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String s) {
             //Update ui on main thread
            //We could stop the spinner here if we had one
            //Just dump the Json fetch into a text view
            mResultTextView.setText(s);
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {
            //Not used yet
        }
      /*----------------------------------------END OF LOADERCALLBACKS OVERRIDE METHODS----------------------------*/


        /*----------------------------------------START OF HELPER METHODS----------------------------*/

        //Used to create a url from a string
        public static URL buildUrl(String queryString) {
            Uri builtUri = Uri.parse(queryString);
            URL url = null;
            try {
                url = new URL(builtUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return url;
        }

      //Used to parse in our url and hopefully get some Json back
        public static String getResponseFromHttpUrl(URL url) throws IOException {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = urlConnection.getInputStream();
                Scanner scanner = new Scanner(in);
                scanner.useDelimiter("\\A");
                boolean hasInput = scanner.hasNext();
                if (hasInput) {
                    return scanner.next();
                } else {
                    return null;
                }
            } finally {
                urlConnection.disconnect();
            }
        }
         /*----------------------------------------END OF HELPER METHODS----------------------------*/
    }
