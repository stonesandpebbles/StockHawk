package com.udacity.stockhawk.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhijeet on 20-03-2017.
 */

public class StockDetail implements Parcelable {

    private String symbol;
    private String name;
    private float price;
    private float absoluteChange;
    private float percentageChange;
    private String historyStr;
    List<Entry> dataEntryList = new ArrayList<Entry>();
    private  static final String NEWLINE="\n";
    private  static final String COMMA=",";

    public String getHistoryStr() {
        return historyStr;
    }

    public void setHistoryStr(String historyStr) {
        this.historyStr = historyStr;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getAbsoluteChange() {
        return absoluteChange;
    }

    public void setAbsoluteChange(float absoluteChange) {
        this.absoluteChange = absoluteChange;
    }

    public float getPercentageChange() {
        return percentageChange;
    }

    public void setPercentageChange(float percentageChange) {
        this.percentageChange = percentageChange;
    }

    public StockDetail(){

    }

    protected StockDetail(Parcel in) {
        symbol = in.readString();
        name = in.readString();
        price = in.readFloat();
        absoluteChange = in.readFloat();
        percentageChange = in.readFloat();
        historyStr = in.readString();
        dataEntryList = in.readArrayList(Entry.class.getClassLoader());
    }


    public static final Creator<StockDetail> CREATOR = new Creator<StockDetail>() {
        @Override
        public StockDetail createFromParcel(Parcel in) {
            return new StockDetail(in);
        }

        @Override
        public StockDetail[] newArray(int size) {
            return new StockDetail[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(symbol);
        dest.writeString(name);
        dest.writeFloat(price);
        dest.writeFloat(absoluteChange);
        dest.writeFloat(percentageChange);
        dest.writeString(historyStr);
        dest.writeList(dataEntryList);
    }

    public List<Entry> getDataEntries(){
        if(dataEntryList.isEmpty()){
            String[] dataEntry = historyStr.split(NEWLINE);
            for(int i =  dataEntry.length - 1; i > 0 ; i--){
                String [] entryXAndY = dataEntry[i].split(COMMA);
                //dataEntryList.add(new Entry(Float.valueOf(entryXAndY[0])/10000000000f, Float.valueOf(entryXAndY[1])));
                //double xVal = Double.valueOf(entryXAndY[0]);
                dataEntryList.add(new Entry(Float.valueOf(entryXAndY[0]), Float.valueOf(entryXAndY[1])));
                //dataEntryList.add(new Entry(3000000200000000f, 260));
            }
        }

        return dataEntryList;

    }
}
