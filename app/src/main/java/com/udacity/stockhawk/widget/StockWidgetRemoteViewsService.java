package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.StockDetail;
import com.udacity.stockhawk.ui.GraphFragment;
import com.udacity.stockhawk.ui.StockAdapter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Abhijeet on 29-03-2017.
 */

public class StockWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;
            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                        null,
                        null,
                        Contract.Quote.COLUMN_COMPANY + " ASC");

                Binder.restoreCallingIdentity(identityToken);

            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);
                StockDetail stockDetail = new StockDetail();

                String name = data.getString(Contract.Quote.POSITION_COMPANY);
                stockDetail.setName(name);
                String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
                stockDetail.setSymbol(symbol);
                views.setTextViewText(R.id.symbol, name + " " + StockAdapter.OPEN_BRACE + symbol + StockAdapter.CLOSE_BRACE);
                float price = data.getFloat(Contract.Quote.POSITION_PRICE);
                if(price != 0.0) {
                    stockDetail.setHistoryStr(data.getString(Contract.Quote.POSITION_HISTORY));

                    DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);

                    views.setTextViewText(R.id.price, dollarFormat.format(price));
                    setRemoteContentDescription(views, getString(R.string.a11y_price, dollarFormat.format(price)));


                }
                else{
                    views.setTextViewText(R.id.price, "");
                }
                final Intent fillInIntent = new Intent();

                fillInIntent.putExtra(GraphFragment.STOCK_DETAIL, stockDetail);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.price, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }
        };
    }
}
