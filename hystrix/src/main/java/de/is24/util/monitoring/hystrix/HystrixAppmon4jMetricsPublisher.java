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

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherCommand;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherThreadPool;
import de.is24.util.monitoring.CorePlugin;


public class HystrixAppmon4jMetricsPublisher extends HystrixMetricsPublisher {
  private final CorePlugin corePlugin;

  public HystrixAppmon4jMetricsPublisher(CorePlugin corePlugin) {
    this.corePlugin = corePlugin;
  }

  @Override
  public HystrixMetricsPublisherCommand getMetricsPublisherForCommand(HystrixCommandKey commandKey,
                                                                      HystrixCommandGroupKey commandGroupKey,
                                                                      HystrixCommandMetrics metrics,
                                                                      HystrixCircuitBreaker circuitBreaker,
                                                                      HystrixCommandProperties properties) {
    return new HystrixAppmon4jMetricsPublisherCommand(commandKey, commandGroupKey, metrics, circuitBreaker, properties,
      corePlugin);
  }

  @Override
  public HystrixMetricsPublisherThreadPool getMetricsPublisherForThreadPool(HystrixThreadPoolKey threadPoolKey,
                                                                            HystrixThreadPoolMetrics metrics,
                                                                            HystrixThreadPoolProperties properties) {
    return new HystrixAppmon4jMetricsPublisherThreadPool(threadPoolKey, metrics, properties, corePlugin);
  }
}
