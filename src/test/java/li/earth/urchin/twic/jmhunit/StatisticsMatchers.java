package li.earth.urchin.twic.jmhunit;

import org.hamcrest.Matcher;
import org.openjdk.jmh.util.Statistics;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

public class StatisticsMatchers {

    public static Matcher<? super Statistics> worseThan(Statistics statistics) {
        return lessThan(statistics);
    }

    public static Matcher<? super Statistics> noWorseThan(Statistics statistics) {
        return greaterThanOrEqualTo(statistics);
    }

    public static Matcher<? super Statistics> betterThan(Statistics statistics) {
        return greaterThan(statistics);
    }

    public static Matcher<? super Statistics> noBetterThan(Statistics statistics) {
        return lessThanOrEqualTo(statistics);
    }

}
