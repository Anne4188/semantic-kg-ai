package ngordnet.ngrams;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * An object for mapping a year number (e.g. 1996) to numerical data. Provides
 * utility methods useful for data analysis.


/** It is public and extends TreeMap with two generic types.
 * key type parameter is Integer, and the value type parameter is Double
 * Each key will correspond to a year, and each value a numerical data point for that year.*/
public class TimeSeries extends TreeMap<Integer, Double> {

    private static final int MIN_YEAR = 1400; //the minimum year allowed for a TimeSeries
    private static final int MAX_YEAR = 2100;

    //private instance variable that stores the class type of the value of the TimeSeries.
    private Class<Number> numberClass;


    /**No argument constructor that creates a new empty TimeSeries*/
    public TimeSeries() {
        super();
    }

    /**Creates a copy of TimeSeries, but only between STARTYEAR and ENDYEAR,
     * inclusive of both end points.*/
    public TimeSeries(TimeSeries ts, int startYear, int endYear) {
        super();
        for (int year = startYear; year <= endYear; year++) {
            if (ts.containsKey(year)) {
                this.put(year, ts.get(year));
            }
        }
    }


    /**returns all the years for the TimeSeries(in any order) as a list of integers*/
    public List<Integer> years() {
        ArrayList<Integer> listIntegers = new ArrayList<>(keySet());
        return listIntegers;

    }

    /**
     * Returns all data for this TimeSeries (in any order)as a list of Doubles.
     * Must be in the same order as years().
     */
    public List<Double> data() {
        List<Double> listDoubles = new ArrayList<>();
        for (int year : years()) {
            listDoubles.add(get(year));
        }
        return listDoubles;
    }

    /**Returns the year-wise sum of this TimeSeries with the given TS. In other words, for
     * each year, sum the data from this TimeSeries with the data from TS. Should return a
     * new TimeSeries (does not modify this TimeSeries).
     *
     * If both TimeSeries don't contain any years, return an empty TimeSeries.
     * If one TimeSeries contains a year that the other one doesn't, the returned TimeSeries
     * should store the value from the TimeSeries that contains that year.
     */
    public TimeSeries plus(TimeSeries ts) {
        TimeSeries ystAns = new TimeSeries();
        for (int year : years()) {
            ystAns.put(year, get(year));
        }
        for (int year : ts.years()) {
            if (ystAns.containsKey(year)) {
                ystAns.put(year, ystAns.get(year) + ts.get(year));
            } else {
                ystAns.put(year, ts.get(year));
            }
        }
        return ystAns;
    }

    /**
     * Returns the quotient of the value for each year this TimeSeries divided by the
     * value for the same year in TS. Should return a new TimeSeries (does not modify this
     * TimeSeries).
     *
     * If TS is missing a year that exists in this TimeSeries, throw an
     * IllegalArgumentException.
     * If TS has a year that is not in this TimeSeries, ignore it.
     */
    public TimeSeries dividedBy(TimeSeries ts) {
        TimeSeries ansTS = new TimeSeries();
        for (int year : years()) {
            if (!ts.containsKey(year)) {
                throw new IllegalArgumentException("We are not found year in TS: " + year);
            }
            ansTS.put(year, get(year) / ts.get(year));
        }
        return ansTS;
    }

    //helper method
    /**public TimeSeries<Double> get(Number startYear, Number endYear) {
        int start = startYear.intValue();
        int end = endYear.intValue();
        TimeSeries<Double> result = new TimeSeries<>(start, end);
        for (int year = start; year <= end; year++) {
            if (this.containsKey(year)) {
                result.put(year, this.get(year));
            }
        }
        return result;
    } */

    //---helper method---
    //checks if a given year exists in the TimeSeries.
    public boolean contains(int year) {
        return this.containsKey(year); //returns a boolean value
    }


    /** returns a new TimeSeries that contains only the values between the given startKey and endKey,
     * inclusive of both endpoints
     */
    public TimeSeries subset(int startKey, int endKey) {
        TimeSeries subTS = new TimeSeries();
        for (int i = startKey; i <= endKey; i++) {
            if (this.containsKey(i)) {
                subTS.put(i, this.get(i));
            }
        }
        return subTS;
    }

    public double sum() {
        double sum = 0.0;
        for (double value : values()) {
            sum += value;
        }
        return sum;
    }
}

