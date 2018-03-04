package li.earth.urchin.twic.jmhunit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class JMHRule implements TestRule {

    private Map<String, RunResult> results;

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Options options = new OptionsBuilder().include(description.getClassName() + ".*")
                                                      .shouldFailOnError(true)
                                                      .jvmArgsAppend(workaroundGradleSecurityManagerInheritance())
                                                      .build();

                results = new Runner(options).run().stream().collect(toMapByBenchmarkName());

                base.evaluate();
            }
        };
    }

    private String[] workaroundGradleSecurityManagerInheritance() {
        return new String[]{"-Djava.security.manager=",
                            "-Djava.security.policy=" + getClass().getResource("permissive.policy")};
    }

    private Collector<RunResult, ?, Map<String, RunResult>> toMapByBenchmarkName() {
        return Collectors.toMap(r -> r.getParams().getBenchmark().replaceFirst(".*\\.", ""), Function.identity());
    }

    public RunResult getRunResult(String benchmarkName) {
        if (results == null) {
            throw new IllegalStateException("benchmarks have not yet run");
        }

        RunResult runResult = results.get(benchmarkName);

        if (runResult == null) {
            throw new NoSuchElementException("no such benchmark: " + benchmarkName + "; benchmarks are " + results.keySet());
        }

        return runResult;
    }

    public Result getAggregatedResult(String benchmarkName) {
        return getRunResult(benchmarkName).getAggregatedResult().getPrimaryResult();
    }

}
