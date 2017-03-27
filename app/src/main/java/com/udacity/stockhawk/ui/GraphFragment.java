package com.udacity.stockhawk.ui;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.StockDetail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GraphFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GraphFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GraphFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String STOCK_DETAIL = "stockDetail";

    private StockDetail mStockDetail;

    private OnFragmentInteractionListener mListener;

    private LineChart mChart;

    public GraphFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param stockDetail stock object containing fields.
     * @return A new instance of fragment GraphFragment.
     */
    public static GraphFragment newInstance(StockDetail stockDetail) {
        GraphFragment fragment = new GraphFragment();
        Bundle args = new Bundle();
        args.putParcelable(STOCK_DETAIL, stockDetail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStockDetail = getArguments().getParcelable(STOCK_DETAIL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
        TextView stockName = (TextView) rootView.findViewById(R.id.stock_name);
        stockName.setText(mStockDetail.getName() + StockAdapter.OPEN_BRACE + mStockDetail.getSymbol() + StockAdapter.CLOSE_BRACE);
        mChart = (LineChart) rootView.findViewById(R.id.historical_chart);
        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setHighlightPerDragEnabled(true);
        // set an alternative background color

        //mChart.setBackgroundColor(Color.WHITE);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        l.setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        //xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
        //xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setCenterAxisLabels(true);
        //xAxis.setGranularity(1f); // one hour
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            //private SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM yy");
            private SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM yy");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                //long millis = TimeUnit.HOURS.toMillis((long) value);
                return mFormat.format(new Date((long)value));
            }
        });

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);


        LineDataSet dataSet = new LineDataSet(mStockDetail.getDataEntries(), "Label"); // add entries to dataset
        dataSet.setColor(Color.WHITE);
        //dataSet.setCircleColor(Color.BLUE);
        dataSet.setDrawCircles(false);
        dataSet.setDrawFilled(true);
        //dataSet.setFillColor(R.color.colorPrimary);
        LineData lineData = new LineData(dataSet);
        //lineData.setDrawValues(false);
        //lineData.setValueTextSize(10f);
        lineData.setValueTextColor(Color.WHITE);
        mChart.setData(lineData);
        mChart.notifyDataSetChanged();
        mChart.invalidate(); // refresh
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

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
}
