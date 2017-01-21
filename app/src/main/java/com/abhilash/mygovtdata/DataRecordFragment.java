package com.abhilash.mygovtdata;

import android.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DataRecordFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DataRecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DataRecordFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ArrayAdapter<String> mTrainAdapter;

    private OnFragmentInteractionListener mListener;

    public DataRecordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DataRecordFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DataRecordFragment newInstance(String param1, String param2) {
        DataRecordFragment fragment = new DataRecordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String[] data = {};
        List<String> dataRecords = new ArrayList<>(Arrays.asList(data));
        mTrainAdapter = new ArrayAdapter<String>(getActivity(), R.layout.data_record_list_item,
                R.id.data_record_list_item_textview, dataRecords);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_data_record, container, false);

        ListView listViewDataRecords = (ListView) rootView.findViewById(R.id.data_record_listview);
        listViewDataRecords.setAdapter(mTrainAdapter);
        listViewDataRecords.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                loadNextPage(page);
                return true;
            }
        });

        FetchDataRecordTask task = new FetchDataRecordTask();
        task.execute("1");

        return rootView;
    }

    public void loadNextPage(int page) {
        FetchDataRecordTask task = new FetchDataRecordTask();
        task.execute(String.valueOf(page));
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    /*
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    */

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public class FetchDataRecordTask extends AsyncTask<String, Void, List<Train>> {

        private final String LOG_TAG = DataRecordFragment.FetchDataRecordTask.class.getSimpleName();

        private List<Train> getTrainDataFromJson(String trainJsonString) throws JSONException {
            List<Train> trains = new ArrayList<>();
            JSONObject trainJsonObject = new JSONObject(trainJsonString);
            JSONArray trainJsonArray = trainJsonObject.getJSONArray("records");
            for (int i = 0; i < trainJsonArray.length(); i++) {
                String trainNumber = trainJsonArray.getJSONObject(i)
                        .getString("Train No.").replace("'", "").trim();
                String trainName = trainJsonArray.getJSONObject(i)
                        .getString("train Name").trim();

                boolean trainExists = false;
                for (Train train : trains) {
                    if (train.getTrainNumber().equals(trainNumber)) {
                        trainExists = true;
                    }
                }
                if (!trainExists) {
                    trains.add(new Train(trainNumber, trainName));
                }
            }
            Log.i(LOG_TAG, String.valueOf(trains.size()));
            return trains;
        }

        @Override
        protected List<Train> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String trainJsonString = null;
            List<Train> trains;

            try {
                final String BASE_URL = "https://data.gov.in/api/datastore/resource.json?resource_id=b46200c1-ca9a-4bbe-92f8-b5039cc25a12";
                final String API_KEY = "api-key";
                final String OFFSET = "offset";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY, BuildConfig.OPEN_GOV_DATA_API_KEY)
                        .appendQueryParameter(OFFSET, params[0])
                        .build();

                URL url = new URL(builtUri.toString());

                Log.i(LOG_TAG, builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    throw new Exception("No data received. Stream is null.");
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    throw new Exception("No data received. Buffer length was 0.");
                }

                trainJsonString = buffer.toString();

                Log.i(LOG_TAG, trainJsonString);

                trains = getTrainDataFromJson(trainJsonString);

                return trains;
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "MalformedURLException ", exception);
                return null;
            } catch (IOException exception) {
                Log.e(LOG_TAG, "IOException ", exception);
                return null;
            } catch (Exception exception) {
                Log.e(LOG_TAG, "Exception ", exception);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error ", e);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(List<Train> trains) {
            if (trains != null) {
                for (Train train : trains) {
                    mTrainAdapter.add(train.getTrainNumber() + " - " + train.getTrainName());
                }
            }
        }
    }
}
