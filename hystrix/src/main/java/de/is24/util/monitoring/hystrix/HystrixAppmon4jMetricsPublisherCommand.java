package de.is24.util.monitoring.hystrix;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherCommand;
import com.netflix.hystrix.util.HystrixRollingNumberEvent;
import de.is24.util.monitoring.AbstractStateValueProvider;
import de.is24.util.monitoring.CorePlugin;
import static de.is24.util.monitoring.tools.KeyHelper.name;


public class HystrixAppmon4jMetricsPublisherCommand implements HystrixMetricsPublisherCommand {
  private final HystrixCommandKey key;
  private final HystrixCommandGroupKey commandGroupKey;
  private final HystrixCommandMetrics metrics;
  private final HystrixCircuitBreaker circuitBreaker;
  private final HystrixCommandProperties properties;
  private final String metricGroup;
  private final String metricType;
  private final CorePlugin corePlugin;

  public HystrixAppmon4jMetricsPublisherCommand(HystrixCommandKey commandKey, HystrixCommandGroupKey commandGroupKey,
                                                HystrixCommandMetrics metrics, HystrixCircuitBreaker circuitBreaker,
                                                HystrixCommandProperties properties, CorePlugin corePlugin) {
    this.key = commandKey;
    this.commandGroupKey = commandGroupKey;
    this.metrics = metrics;
    this.circuitBreaker = circuitBreaker;
    this.properties = properties;
    this.metricGroup = "HystrixCommand";
    this.metricType = key.name();
    this.corePlugin = corePlugin;
  }

  @Override
  public void initialize() {
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("isCircuitBreakerOpen")) {
        @Override
        public long getValue() {
          return circuitBreaker.isOpen() ? 1L : 0L;
        }
      });

    // cumulative counts
    createCumulativeCountForEvent("countCollapsedRequests", HystrixRollingNumberEvent.COLLAPSED);
    createCumulativeCountForEvent("countExceptionsThrown", HystrixRollingNumberEvent.EXCEPTION_THROWN);
    createCumulativeCountForEvent("countFailure", HystrixRollingNumberEvent.FAILURE);
    createCumulativeCountForEvent("countFallbackFailure", HystrixRollingNumberEvent.FALLBACK_FAILURE);
    createCumulativeCountForEvent("countFallbackRejection", HystrixRollingNumberEvent.FALLBACK_REJECTION);
    createCumulativeCountForEvent("countFallbackSuccess", HystrixRollingNumberEvent.FALLBACK_SUCCESS);
    createCumulativeCountForEvent("countResponsesFromCache", HystrixRollingNumberEvent.RESPONSE_FROM_CACHE);
    createCumulativeCountForEvent("countSemaphoreRejected", HystrixRollingNumberEvent.SEMAPHORE_REJECTED);
    createCumulativeCountForEvent("countShortCircuited", HystrixRollingNumberEvent.SHORT_CIRCUITED);
    createCumulativeCountForEvent("countSuccess", HystrixRollingNumberEvent.SUCCESS);
    createCumulativeCountForEvent("countThreadPoolRejected", HystrixRollingNumberEvent.THREAD_POOL_REJECTED);
    createCumulativeCountForEvent("countTimeout", HystrixRollingNumberEvent.TIMEOUT);

    // rolling counts
    createRollingCountForEvent("rollingCountCollapsedRequests", HystrixRollingNumberEvent.COLLAPSED);
    createRollingCountForEvent("rollingCountExceptionsThrown", HystrixRollingNumberEvent.EXCEPTION_THROWN);
    createRollingCountForEvent("rollingCountFailure", HystrixRollingNumberEvent.FAILURE);
    createRollingCountForEvent("rollingCountFallbackFailure", HystrixRollingNumberEvent.FALLBACK_FAILURE);
    createRollingCountForEvent("rollingCountFallbackRejection", HystrixRollingNumberEvent.FALLBACK_REJECTION);
    createRollingCountForEvent("rollingCountFallbackSuccess", HystrixRollingNumberEvent.FALLBACK_SUCCESS);
    createRollingCountForEvent("rollingCountResponsesFromCache", HystrixRollingNumberEvent.RESPONSE_FROM_CACHE);
    createRollingCountForEvent("rollingCountSemaphoreRejected", HystrixRollingNumberEvent.SEMAPHORE_REJECTED);
    createRollingCountForEvent("rollingCountShortCircuited", HystrixRollingNumberEvent.SHORT_CIRCUITED);
    createRollingCountForEvent("rollingCountSuccess", HystrixRollingNumberEvent.SUCCESS);
    createRollingCountForEvent("rollingCountThreadPoolRejected", HystrixRollingNumberEvent.THREAD_POOL_REJECTED);
    createRollingCountForEvent("rollingCountTimeout", HystrixRollingNumberEvent.TIMEOUT);

    // the number of executionSemaphorePermits in use right now
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("executionSemaphorePermitsInUse")) {
        @Override
        public long getValue() {
          return metrics.getCurrentConcurrentExecutionCount();
        }
      });

    // error percentage derived from current metrics
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("errorPercentage")) {
        @Override
        public long getValue() {
          return metrics.getHealthCounts().getErrorPercentage();
        }
      });

    // latency metrics
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("latencyExecute_mean")) {
        @Override
        public long getValue() {
          return metrics.getExecutionTimeMean();
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("latencyExecute_percentile_5")) {
        @Override
        public long getValue() {
          return metrics.getExecutionTimePercentile(5);
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("latencyExecute_percentile_25")) {
        @Override
        public long getValue() {
          return metrics.getExecutionTimePercentile(25);
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("latencyExecute_percentile_50")) {
        @Override
        public long getValue() {
          return metrics.getExecutionTimePercentile(50);
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("latencyExecute_percentile_75")) {
        @Override
        public long getValue() {
          return metrics.getExecutionTimePercentile(75);
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("latencyExecute_percentile_90")) {
        @Override
        public long getValue() {
          return metrics.getExecutionTimePercentile(90);
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("latencyExecute_percentile_99")) {
        @Override
        public long getValue() {
          return metrics.getExecutionTimePercentile(99);
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("latencyExecute_percentile_995")) {
        @Override
        public long getValue() {
          return metrics.getExecutionTimePercentile(99.5);
        }
      });

    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("latencyTotal_mean")) {
        @Override
        public long getValue() {
          return metrics.getTotalTimeMean();
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("latencyTotal_percentile_5")) {
        @Override
        public long getValue() {
          return metrics.getTotalTimePercentile(5);
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("latencyTotal_percentile_25")) {
        @Override
        public long getValue() {
          return metrics.getTotalTimePercentile(25);
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("latencyTotal_percentile_50")) {
        @Override
        public long getValue() {
          return metrics.getTotalTimePercentile(50);
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("latencyTotal_percentile_75")) {
        @Override
        public long getValue() {
          return metrics.getTotalTimePercentile(75);
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("latencyTotal_percentile_90")) {
        @Override
        public long getValue() {
          return metrics.getTotalTimePercentile(90);
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("latencyTotal_percentile_99")) {
        @Override
        public long getValue() {
          return metrics.getTotalTimePercentile(99);
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("latencyTotal_percentile_995")) {
        @Override
        public long getValue() {
          return metrics.getTotalTimePercentile(99.5);
        }
      });

    // properties (so the values can be inspected and monitored)
    corePlugin.registerStateValue(new AbstractStateValueProvider(
        createMetricName("propertyValue_rollingStatisticalWindowInMilliseconds")) {
        @Override
        public long getValue() {
          return properties.metricsRollingStatisticalWindowInMilliseconds().get();
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(
        createMetricName("propertyValue_circuitBreakerRequestVolumeThreshold")) {
        @Override
        public long getValue() {
          return properties.circuitBreakerRequestVolumeThreshold().get();
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(
        createMetricName("propertyValue_circuitBreakerSleepWindowInMilliseconds")) {
        @Override
        public long getValue() {
          return properties.circuitBreakerSleepWindowInMilliseconds().get();
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(
        createMetricName("propertyValue_circuitBreakerErrorThresholdPercentage")) {
        @Override
        public long getValue() {
          return properties.circuitBreakerErrorThresholdPercentage().get();
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(
        createMetricName("propertyValue_circuitBreakerForceOpen")) {
        @Override
        public long getValue() {
          return properties.circuitBreakerForceOpen().get() ? 1 : 0;
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(
        createMetricName("propertyValue_circuitBreakerForceClosed")) {
        @Override
        public long getValue() {
          return properties.circuitBreakerForceClosed().get() ? 1 : 0;
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(
        createMetricName("propertyValue_executionIsolationThreadTimeoutInMilliseconds")) {
        @Override
        public long getValue() {
          return properties.executionIsolationThreadTimeoutInMilliseconds().get();
        }
      });

    /*    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("propertyValue_executionIsolationStrategy"), new Gauge<String>() {
            @Override
            public String getValue() {
              return properties.executionIsolationStrategy().get().name();
            }
          });
        corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("propertyValue_metricsRollingPercentileEnabled"), new Gauge<Boolean>() {
            @Override
            public Boolean getValue() {
              return properties.metricsRollingPercentileEnabled().get();
            }
          }); */
    corePlugin.registerStateValue(new AbstractStateValueProvider(
        createMetricName("propertyValue_requestCacheEnabled")) {
        @Override
        public long getValue() {
          return properties.requestCacheEnabled().get() ? 1 : 0;
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("propertyValue_requestLogEnabled")) {
        @Override
        public long getValue() {
          return properties.requestLogEnabled().get() ? 1 : 0;
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(
        createMetricName("propertyValue_executionIsolationSemaphoreMaxConcurrentRequests")) {
        @Override
        public long getValue() {
          return properties.executionIsolationSemaphoreMaxConcurrentRequests().get();
        }
      });
    corePlugin.registerStateValue(new AbstractStateValueProvider(
        createMetricName("propertyValue_fallbackIsolationSemaphoreMaxConcurrentRequests")) {
        @Override
        public long getValue() {
          return properties.fallbackIsolationSemaphoreMaxConcurrentRequests().get();
        }
      });
  }

  protected String createMetricName(String name) {
    return name(metricGroup, metricType, name);
  }

  protected void createCumulativeCountForEvent(String name, final HystrixRollingNumberEvent event) {
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName(name)) {
        @Override
        public long getValue() {
          return metrics.getCumulativeCount(event);
        }
      });
  }

  protected void createRollingCountForEvent(String name, final HystrixRollingNumberEvent event) {
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName(name)) {
        @Override
        public long getValue() {
          return metrics.getRollingCount(event);
        }
      });
  }
}
