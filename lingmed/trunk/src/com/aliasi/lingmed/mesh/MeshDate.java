package com.aliasi.lingmed.mesh;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import org.xml.sax.SAXException;

public class MeshDate {

    private final int mYear;
    private final int mMonth;
    private final int mDay;

    public MeshDate(String year,
                    String month,
                    String day) {
        mYear = Integer.valueOf(year);
        mMonth = Integer.valueOf(month);
        mDay = Integer.valueOf(day);
    }

    public int year() {
        return mYear;
    }

    public int month() {
        return mMonth;
    }

    public int day() {
        return mDay;
    }

    @Override
    public String toString() {
        return String.format("%04d/%02d/%02d",year(),month(),day());
    }

    static class Handler extends DelegateHandler {
        private boolean mFound;
        private final TextAccumulatorHandler mYearHandler;
        private final TextAccumulatorHandler mMonthHandler;
        private final TextAccumulatorHandler mDayHandler;
        public Handler(DelegatingHandler parent) {
            super(parent);
            
            mYearHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.YEAR_ELEMENT,mYearHandler);

            mMonthHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.MONTH_ELEMENT,mMonthHandler);

            mDayHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.DAY_ELEMENT,mDayHandler);
            
        }
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
            mFound = true;
        }
        public void reset() {
            mYearHandler.reset();
            mMonthHandler.reset();
            mDayHandler.reset();
            mFound = false;
        }
        public MeshDate getDate() {
            return mFound 
                ? new MeshDate(mYearHandler.getText(),
                               mMonthHandler.getText(),
                               mDayHandler.getText())
                : null;
        }
    }

}