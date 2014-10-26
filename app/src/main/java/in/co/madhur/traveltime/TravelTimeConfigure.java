package in.co.madhur.traveltime;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by madhur on 10/26/14.
 */
public class TravelTimeConfigure extends ActionBarActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.configuration_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

        AutoCompleteTextView sourceCompView = (AutoCompleteTextView) findViewById(R.id.source);
        AutoCompleteTextView destCompView = (AutoCompleteTextView) findViewById(R.id.destination);
        sourceCompView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.location_list_item));
        destCompView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.location_list_item));


    }

    private ArrayList<String> autocomplete(String input)
    {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try
        {
            StringBuilder sb = new StringBuilder(Consts.PLACES_API_BASE + Consts.TYPE_AUTOCOMPLETE + Consts.OUT_JSON);
            sb.append("?key=" + Consts.API_KEY);
            sb.append("&components=country:uk");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1)
            {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e)
        {
            Log.e(Consts.LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e)
        {
            Log.e(Consts.LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally
        {
            if (conn != null)
            {
                conn.disconnect();
            }
        }

        try
        {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++)
            {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e)
        {
            Log.e(Consts.LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable
    {
        private ArrayList<String> resultList;

        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId)
        {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount()
        {
            return resultList.size();
        }

        @Override
        public String getItem(int index)
        {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter()
        {
            Filter filter = new Filter()
            {
                @Override
                protected FilterResults performFiltering(CharSequence constraint)
                {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null)
                    {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results)
                {
                    if (results != null && results.count > 0)
                    {
                        notifyDataSetChanged();
                    } else
                    {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }

}
