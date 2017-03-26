package com.udacity.stockhawk.ui;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.data.StockDetail;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import yahoofinance.Stock;

class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private final Context context;
    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat dollarFormat;
    private final DecimalFormat percentageFormat;
    private Cursor cursor;
    private final StockAdapterOnClickHandler clickHandler;
    private Map<String, StockDetail> mStockDetailMap;

    private static final String OPEN_BRACE = "(";
    private static final String CLOSE_BRACE = ")";

    public StockDetail getStockDetail(String symbol) {
        return mStockDetailMap.get(symbol);
    }

    StockAdapter(Context context, StockAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix(context.getString(R.string.plus_symbol)+dollarFormatWithPlus.getCurrency().getSymbol(Locale.ENGLISH));
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
            percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix(context.getString(R.string.plus_symbol));
         mStockDetailMap = new HashMap<String, StockDetail>();
    }

    void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    String getSymbolAtPosition(int position) {

        cursor.moveToPosition(position);
        return cursor.getString(Contract.Quote.POSITION_SYMBOL);
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View item = LayoutInflater.from(context).inflate(R.layout.list_item_quote, parent, false);

        return new StockViewHolder(item);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {

        cursor.moveToPosition(position);
        StockDetail stockDetail = new StockDetail();
        String name = cursor.getString(Contract.Quote.POSITION_COMPANY);
        stockDetail.setName(name);
        String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
        stockDetail.setSymbol(symbol);
        holder.symbol.setText(name + " "+ OPEN_BRACE + symbol + CLOSE_BRACE);
        holder.symbol.setContentDescription(context.getResources().getString(R.string.a11y_name_symbol, name + " "+ OPEN_BRACE + symbol + CLOSE_BRACE));
        float price = cursor.getFloat(Contract.Quote.POSITION_PRICE);
        if(price != 0.0) {
            holder.price.setText(dollarFormat.format(price));
            holder.price.setContentDescription(context.getResources().getString(R.string.a11y_price, dollarFormat.format(price)));
            stockDetail.setPrice(price);

            float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

            if (rawAbsoluteChange > 0) {
                holder.change.setBackgroundResource(R.drawable.percent_change_pill_green);
            } else {
                holder.change.setBackgroundResource(R.drawable.percent_change_pill_red);
            }

            String change = dollarFormatWithPlus.format(rawAbsoluteChange);
            stockDetail.setAbsoluteChange(rawAbsoluteChange);
            String percentage = percentageFormat.format(percentageChange / 100);
            stockDetail.setPercentageChange(percentageChange);
            if (PrefUtils.getDisplayMode(context)
                    .equals(context.getString(R.string.pref_display_mode_absolute_key))) {
                holder.change.setText(change);
                holder.change.setContentDescription(context.getResources().getString(R.string.a11y_price_change, change));
            } else {
                holder.change.setText(percentage);
                holder.change.setContentDescription(context.getResources().getString(R.string.a11y_price_change, percentage));
            }

            stockDetail.setHistoryStr(cursor.getString(Contract.Quote.POSITION_HISTORY));
            holder.price.setVisibility(View.VISIBLE);
            holder.change.setVisibility(View.VISIBLE);
        }
        else{
            holder.price.setVisibility(View.GONE);
            holder.change.setVisibility(View.GONE);
        }
        mStockDetailMap.put(symbol, stockDetail);

    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }
        return count;
    }


    interface StockAdapterOnClickHandler {
        void onClick(String symbol);
    }

    class StockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.symbol)
        TextView symbol;

        @BindView(R.id.price)
        TextView price;

        @BindView(R.id.change)
        TextView change;

        StockViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            cursor.moveToPosition(adapterPosition);
            int symbolColumn = cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL);
            clickHandler.onClick(cursor.getString(symbolColumn));

        }


    }
}
