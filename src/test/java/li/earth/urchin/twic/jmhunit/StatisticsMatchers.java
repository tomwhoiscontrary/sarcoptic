package li.earth.urchin.twic.jmhunit;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.openjdk.jmh.util.Statistics;

public class StatisticsMatchers {

    public static Matcher<? super Statistics> indistinguishableFrom(Statistics statistics) {
        return Matchers.comparesEqualTo(statistics);
    }

    public static Matcher<? super Statistics> worseThan(Statistics statistics) {
        return Matchers.lessThan(statistics);
    }

    public static Matcher<? super Statistics> noWorseThan(Statistics statistics) {
        return Matchers.greaterThanOrEqualTo(statistics);
    }

    public static Matcher<? super Statistics> betterThan(Statistics statistics) {
        return Matchers.greaterThan(statistics);
    }

    public static Matcher<? super Statistics> noBetterThan(Statistics statistics) {
        return Matchers.lessThanOrEqualTo(statistics);
    }

}
