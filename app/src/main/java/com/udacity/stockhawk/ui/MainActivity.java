package com.udacity.stockhawk.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.data.StockDetail;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler {

    private static final int STOCK_LOADER = 0;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.recycler_view)
    RecyclerView stockRecyclerView;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.error)
    TextView error;
    private StockAdapter adapter;
    private boolean mTwoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean firstLoad = false;

    @Override
    public void onClick(String symbol) {

        Timber.d("Symbol clicked: %s", symbol);
        //Create intent
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(GraphFragment.STOCK_DETAIL, adapter.getStockDetail(symbol));
        //if(adapter.getStockDetail(symbol).getPrice() != 0.0) {
            if(mTwoPane){
                // In two-pane mode, show the detail view in this activity by
                // adding or replacing the detail fragment using a
                // fragment transaction.

                GraphFragment fragment = GraphFragment.newInstance(adapter.getStockDetail(symbol));
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.stock_detail_container, fragment, DETAILFRAGMENT_TAG)
                        .commit();
            }
            else {
                //Start detail activity
                startActivity(intent);
            }
       // }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (findViewById(R.id.stock_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                firstLoad = true;
                StockDetail stockDetail = getIntent() != null ? (StockDetail) getIntent().getParcelableExtra(GraphFragment.STOCK_DETAIL) : null;
                if(stockDetail != null)
                    firstLoad = false;
                GraphFragment fragment = GraphFragment.newInstance(stockDetail);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.stock_detail_container, fragment, DETAILFRAGMENT_TAG)
                        .commit();
            }
        }
        else
            mTwoPane = false;

        adapter = new StockAdapter(this, this);
        stockRecyclerView.setAdapter(adapter);
        stockRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();

        QuoteSyncJob.initialize(this);
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = adapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                PrefUtils.removeStock(MainActivity.this, symbol);
                getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
                Intent dataUpdatedIntent = new Intent(QuoteSyncJob.ACTION_DATA_UPDATED);
                getApplicationContext().sendBroadcast(dataUpdatedIntent);
            }
        }).attachToRecyclerView(stockRecyclerView);


    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onRefresh() {

        QuoteSyncJob.syncImmediately(this);

        if (!networkUp() && adapter.getItemCount() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_network));
            error.setVisibility(View.VISIBLE);
        } else if (!networkUp()) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
        } else if (PrefUtils.getStocks(this).size() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_stocks));
            error.setVisibility(View.VISIBLE);
        } else {
            error.setVisibility(View.GONE);
        }
    }

    public void button(@SuppressWarnings("UnusedParameters") View view) {
        DialogFragment df = new AddStockDialog();
        df.show(getFragmentManager(), "StockDialogFragment");
        getFragmentManager().executePendingTransactions();
        Dialog dialog = df.getDialog();
        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);
    }

    void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {
            if (!PrefUtils.getStocks(this).contains(symbol)) {
                if (networkUp()) {
                    swipeRefreshLayout.setRefreshing(true);
                } else {
                    String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }

                PrefUtils.addStock(this, symbol);
                QuoteSyncJob.syncImmediately(this);
            }
            else {
                String message = getString(R.string.toast_stock_already_added, symbol);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_COMPANY);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swipeRefreshLayout.setRefreshing(false);

        if (data.getCount() != 0) {
            error.setVisibility(View.GONE);
        }
        adapter.setCursor(data);
        if(mTwoPane && firstLoad) {
            stockRecyclerView.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    if(stockRecyclerView.findViewHolderForAdapterPosition(0) != null )
                        stockRecyclerView.findViewHolderForAdapterPosition(0).itemView.performClick();
                }
            },50);
            firstLoad = false;
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swipeRefreshLayout.setRefreshing(false);
        adapter.setCursor(null);
    }


    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
            item.setTitle(getString(R.string.a11y_display_option_percentage));
        } else {
            item.setIcon(R.drawable.ic_dollar);
            item.setTitle(getString(R.string.a11y_display_option_currency));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(this);
            setDisplayModeMenuItemIcon(item);
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
