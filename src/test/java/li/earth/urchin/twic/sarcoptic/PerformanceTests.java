package li.earth.urchin.twic.sarcoptic;

import li.earth.urchin.twic.jmhunit.JMHRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.Result;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static li.earth.urchin.twic.jmhunit.ScaledStatistics.scale;
import static li.earth.urchin.twic.jmhunit.StatisticsMatchers.betterThan;
import static li.earth.urchin.twic.jmhunit.StatisticsMatchers.noWorseThan;
import static org.hamcrest.MatcherAssert.assertThat;

@Fork(1)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 20, time = 50, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class PerformanceTests {

    @ClassRule
    public static JMHRule jmh = new JMHRule();

    @Test
    public void gettingAFieldValueIsFast() throws Exception {
        Result beanResult = jmh.getAggregatedResult("getValueFromBean");
        Result structResult = jmh.getAggregatedResult("getValueFromStruct");
        Result mapResult = jmh.getAggregatedResult("getValueFromMap");

        assertThat(structResult.getStatistics(), noWorseThan(scale(1.5, beanResult.getStatistics())));
        assertThat(structResult.getStatistics(), betterThan(mapResult.getStatistics()));
    }

    @State(Scope.Benchmark)
    public static class Fixtures {

        public static interface TestStruct extends Struct<TestStruct> {

            String value();

        }

        public static class TestBean {

            private final String value = null;

            public String getValue() {
                return value;
            }

        }

        public final TestStruct struct = Struct.of(TestStruct.class);

        public final TestBean bean = new TestBean();

        public final Map<String, String> map = new HashMap<>(Collections.singletonMap("value", null));

    }

    @Benchmark
    public void getValueFromBean(Fixtures fixtures, Blackhole blackhole) {
        blackhole.consume(fixtures.bean.getValue());
    }

    @Benchmark
    public void getValueFromStruct(Fixtures fixtures, Blackhole blackhole) {
        blackhole.consume(fixtures.struct.value());
    }

    @Benchmark
    public void getValueFromMap(Fixtures fixtures, Blackhole blackhole) {
        blackhole.consume(fixtures.map.get("value"));
    }

}
