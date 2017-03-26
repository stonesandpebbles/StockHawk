package com.udacity.stockhawk.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.StockDetail;

public class DetailActivity extends AppCompatActivity implements GraphFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            StockDetail stockDetail = getIntent().getParcelableExtra(GraphFragment.STOCK_DETAIL);
            arguments.putParcelable(GraphFragment.STOCK_DETAIL, stockDetail);
            GraphFragment fragment = GraphFragment.newInstance(stockDetail);
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_graph, fragment)
                    .commit();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
