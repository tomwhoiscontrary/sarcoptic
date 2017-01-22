package li.earth.urchin.twic.jmhunit;

import org.openjdk.jmh.util.AbstractStatistics;
import org.openjdk.jmh.util.Statistics;

import java.util.Iterator;
import java.util.Map;

public class ScaledStatistics extends AbstractStatistics {

    public static Statistics scale(double scaleFactor, Statistics underlyingStatistics) {
        return new ScaledStatistics(scaleFactor, underlyingStatistics);
    }

    private final double scaleFactor;
    private final Statistics underlyingStatistics;

    private ScaledStatistics(double scaleFactor, Statistics underlyingStatistics) {
        this.scaleFactor = scaleFactor;
        this.underlyingStatistics = underlyingStatistics;
    }

    @Override
    public double getMax() {
        return underlyingStatistics.getMax() * scaleFactor;
    }

    @Override
    public double getMin() {
        return underlyingStatistics.getMin() * scaleFactor;
    }

    @Override
    public long getN() {
        return underlyingStatistics.getN();
    }

    @Override
    public double getSum() {
        return underlyingStatistics.getSum() * scaleFactor;
    }

    @Override
    public double getVariance() {
        return underlyingStatistics.getVariance() * scaleFactor;
    }

    @Override
    public double getPercentile(double rank) {
        return underlyingStatistics.getPercentile(rank) * scaleFactor;
    }

    @Override
    public int[] getHistogram(double[] levels) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Map.Entry<Double, Long>> getRawData() {
        throw new UnsupportedOperationException();
    }

}
