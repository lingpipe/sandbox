package com.aliasi.lingmed.mesh;

import com.aliasi.lingpipe.xml.DelegateHandler;
import com.aliasi.lingpipe.xml.DelegatingHandler;
import com.aliasi.lingpipe.xml.TextAccumulatorHandler;

import org.xml.sax.SAXException;

/**
 * A {@link MeshDate} consists of a year, month and day.
 * 
 * @author Bob Carpenter
 * @version 1.3
 * @since LingMed1.3
 */
public class MeshDate {

    private final int mYear;
    private final int mMonth;
    private final int mDay;

    MeshDate(String year,
             String month,
             String day) {
        mYear = Integer.valueOf(year);
        mMonth = Integer.valueOf(month);
        mDay = Integer.valueOf(day);
    }

    /**
     * Returns the year for this date.
     *
     * @return Year for this date.
     */
    public int year() {
        return mYear;
    }

    /**
     * Return the month for this date.
     *
     * @return The month for this date.
     */
    public int month() {
        return mMonth;
    }

    /**
     * Return the day for this date.
     *
     * @return The day for this date.
     */
    public int day() {
        return mDay;
    }

    /**
     * Returns a string representation of this date in YYYY/MM/DD
     * format.  
     *
     * @return String representation of this date.
     */
    @Override
    public String toString() {
        return String.format("%04d/%02d/%02d",year(),month(),day());
    }

    static class Handler extends BaseHandler<MeshDate> {
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
        public void reset() {
            mYearHandler.reset();
            mMonthHandler.reset();
            mDayHandler.reset();
        }
        public MeshDate getObject() {
            String yearText = mYearHandler.getText().trim();
            return yearText.length() == 0
                ? null
                : new MeshDate(yearText,
                               mMonthHandler.getText().trim(),
                               mDayHandler.getText().trim());
        }

    }

}
