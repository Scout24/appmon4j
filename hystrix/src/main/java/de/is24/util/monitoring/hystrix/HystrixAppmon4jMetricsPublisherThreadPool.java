/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.is24.util.monitoring.hystrix;

import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherThreadPool;
import de.is24.util.monitoring.AbstractStateValueProvider;
import de.is24.util.monitoring.CorePlugin;
import static de.is24.util.monitoring.tools.KeyHelper.name;


public class HystrixAppmon4jMetricsPublisherThreadPool implements HystrixMetricsPublisherThreadPool {
  private final HystrixThreadPoolKey key;
  private final HystrixThreadPoolMetrics metrics;
  private final HystrixThreadPoolProperties properties;
  private final String metricGroup;
  private final String metricType;
  private final CorePlugin corePlugin;

  public HystrixAppmon4jMetricsPublisherThreadPool(HystrixThreadPoolKey threadPoolKey, HystrixThreadPoolMetrics metrics,
                                                   HystrixThreadPoolProperties properties,
                                                   CorePlugin corePlugin) {
    this.key = threadPoolKey;
    this.metrics = metrics;
    this.properties = properties;
    this.metricGroup = "HystrixThreadPool";
    this.metricType = key.name();
    this.corePlugin = corePlugin;
  }

  @Override
  public void initialize() {
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("threadActiveCount")) {
        @Override
        public long getValue() {
          return metrics.getCurrentActiveCount().longValue();
        }
      });

    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("completedTaskCount")) {
        @Override
        public long getValue() {
          return metrics.getCurrentCompletedTaskCount().longValue();
        }
      });

    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("largestPoolSize")) {
        @Override
        public long getValue() {
          return metrics.getCurrentLargestPoolSize().longValue();
        }
      });

    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("totalTaskCount")) {
        @Override
        public long getValue() {
          return metrics.getCurrentTaskCount().longValue();
        }
      });

    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("queueSize")) {
        @Override
        public long getValue() {
          return metrics.getCurrentQueueSize().longValue();
        }
      });

    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("rollingMaxActiveThreads")) {
        @Override
        public long getValue() {
          return metrics.getRollingMaxActiveThreads();
        }
      });

    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("countThreadsExecuted")) {
        @Override
        public long getValue() {
          return metrics.getCumulativeCountThreadsExecuted();
        }
      });

    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("rollingCountThreadsExecuted")) {
        @Override
        public long getValue() {
          return metrics.getRollingCountThreadsExecuted();
        }
      });

    // properties
    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("propertyValue_corePoolSize")) {
        @Override
        public long getValue() {
          return properties.coreSize().get();
        }
      });

    corePlugin.registerStateValue(new AbstractStateValueProvider(
        createMetricName("propertyValue_keepAliveTimeInMinutes")) {
        @Override
        public long getValue() {
          return properties.keepAliveTimeMinutes().get();
        }
      });

    corePlugin.registerStateValue(new AbstractStateValueProvider(
        createMetricName("propertyValue_queueSizeRejectionThreshold")) {
        @Override
        public long getValue() {
          return properties.queueSizeRejectionThreshold().get();
        }
      });

    corePlugin.registerStateValue(new AbstractStateValueProvider(createMetricName("propertyValue_maxQueueSize")) {
        @Override
        public long getValue() {
          return properties.maxQueueSize().get();
        }
      });
  }

  protected String createMetricName(String name) {
    return name(metricGroup, metricType, name);
  }
}
