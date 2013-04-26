package de.is24.util.monitoring.tools;

import de.is24.util.monitoring.CorePlugin;
import de.is24.util.monitoring.MultiValueProvider;
import de.is24.util.monitoring.ReportVisitor;
import de.is24.util.monitoring.StateValueProvider;
import org.apache.log4j.Logger;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.Thread.State;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;


/**
 * A collection of Java Virtual Machine metrics.
 * This code was originally taken from de.is24.util.monitoring.tools.VirtualMachineMetrics
 */
public class VirtualMachineMetrics {
  private static final Logger LOGGER = Logger.getLogger(VirtualMachineMetrics.class);
  private static final int MAX_STACK_TRACE_DEPTH = 100;


  /**
   * Per-GC statistics.
   */
  public static class GarbageCollectorStats {
    private final long runs, timeMS;

    private GarbageCollectorStats(long runs, long timeMS) {
      this.runs = runs;
      this.timeMS = timeMS;
    }

    /**
     * Returns the number of times the garbage collector has run.
     *
     * @return the number of times the garbage collector has run
     */
    public long getRuns() {
      return runs;
    }

    /**
     * Returns the amount of time in the given unit the garbage collector has taken in total.
     *
     * @param unit    the time unit for the return value
     * @return the amount of time in the given unit the garbage collector
     */
    public long getTime(TimeUnit unit) {
      return unit.convert(timeMS, TimeUnit.MILLISECONDS);
    }
  }

  /**
   * The management interface for a buffer pool, for example a pool of {@link
   * java.nio.ByteBuffer#allocateDirect direct} or {@link java.nio.MappedByteBuffer mapped}
   * buffers.
   */
  public static class BufferPoolStats {
    private final long count, memoryUsed, totalCapacity;

    private BufferPoolStats(long count, long memoryUsed, long totalCapacity) {
      this.count = count;
      this.memoryUsed = memoryUsed;
      this.totalCapacity = totalCapacity;
    }

    /**
     * Returns an estimate of the number of buffers in the pool.
     *
     * @return An estimate of the number of buffers in this pool
     */
    public long getCount() {
      return count;
    }

    /**
     * Returns an estimate of the memory that the Java virtual machine is using for this buffer
     * pool. The value returned by this method may differ from the estimate of the total {@link
     * #getTotalCapacity capacity} of the buffers in this pool. This difference is explained by
     * alignment, memory allocator, and other implementation specific reasons.
     *
     * @return An estimate of the memory that the Java virtual machine is using for this buffer
     *         pool in bytes, or {@code -1L} if an estimate of the memory usage is not
     *         available
     */
    public long getMemoryUsed() {
      return memoryUsed;
    }

    /**
     * Returns an estimate of the total capacity of the buffers in this pool. A buffer's
     * capacity is the number of elements it contains and the value returned by this method is
     * an estimate of the total capacity of buffers in the pool in bytes.
     *
     * @return An estimate of the total capacity of the buffers in this pool in bytes
     */
    public long getTotalCapacity() {
      return totalCapacity;
    }
  }

  public static void registerVMStates(CorePlugin corePlugin) {
    final MemoryMXBean memory = VirtualMachineMBeans.getInstance().getMemory();
    corePlugin.registerStateValue(new StateValueProvider() {
        @Override
        public long getValue() {
          return memory.getHeapMemoryUsage().getInit();
        }

        @Override
        public String getName() {
          return "jvm.memory.heap.init";
        }
      });
    corePlugin.registerStateValue(new StateValueProvider() {
        @Override
        public long getValue() {
          return memory.getNonHeapMemoryUsage().getInit();
        }

        @Override
        public String getName() {
          return "jvm.memory.nonHeap.init";
        }
      });
    corePlugin.registerStateValue(new StateValueProvider() {
        @Override
        public long getValue() {
          return memory.getHeapMemoryUsage().getUsed();
        }

        @Override
        public String getName() {
          return "jvm.memory.heap.used";
        }
      });
    corePlugin.registerStateValue(new StateValueProvider() {
        @Override
        public long getValue() {
          return memory.getNonHeapMemoryUsage().getUsed();
        }

        @Override
        public String getName() {
          return "jvm.memory.nonHeap.used";
        }
      });
    corePlugin.registerStateValue(new StateValueProvider() {
        @Override
        public long getValue() {
          return memory.getHeapMemoryUsage().getCommitted();
        }

        @Override
        public String getName() {
          return "jvm.memory.heap.committed";
        }
      });
    corePlugin.registerStateValue(new StateValueProvider() {
        @Override
        public long getValue() {
          return memory.getNonHeapMemoryUsage().getCommitted();
        }

        @Override
        public String getName() {
          return "jvm.memory.nonHeap.committed";
        }
      });

    for (final MemoryPoolMXBean pool : VirtualMachineMBeans.getInstance().getMemoryPools()) {
      corePlugin.registerStateValue(new StateValueProvider() {
          @Override
          public long getValue() {
            return pool.getUsage().getUsed();
          }

          @Override
          public String getName() {
            return "jvm.memory." + pool.getName().replace(" ", "_") + ".used";
          }
        });
      corePlugin.registerStateValue(new StateValueProvider() {
          @Override
          public long getValue() {
            return pool.getUsage().getCommitted();
          }

          @Override
          public String getName() {
            return "jvm.memory." + pool.getName().replace(" ", "_") + ".committed";
          }
        });
    }

    final ThreadMXBean threadMXBean = VirtualMachineMBeans.getInstance().getThreads();
    corePlugin.registerStateValue(new StateValueProvider() {
        @Override
        public long getValue() {
          return threadMXBean.getThreadCount();
        }

        @Override
        public String getName() {
          return "jvm.threads.count";
        }
      });
    corePlugin.registerStateValue(new StateValueProvider() {
        @Override
        public long getValue() {
          return threadMXBean.getDaemonThreadCount();
        }

        @Override
        public String getName() {
          return "jvm.threads.daemon";
        }
      });

    final OperatingSystemMXBean operatingSystemMXBean = VirtualMachineMBeans.getInstance().getOs();
    corePlugin.registerStateValue(new StateValueProvider() {
        @Override
        public long getValue() {
          try {
            final Method getOpenFileDescriptorCount = operatingSystemMXBean.getClass()
              .getDeclaredMethod("getOpenFileDescriptorCount");
            getOpenFileDescriptorCount.setAccessible(true);

            return ((Long) getOpenFileDescriptorCount.invoke(operatingSystemMXBean)).longValue();
          } catch (NoSuchMethodException e) {
            return -1;
          } catch (IllegalAccessException e) {
            return -1;
          } catch (InvocationTargetException e) {
            return -1;
          }
        }

        @Override
        public String getName() {
          return "jvm.filedescriptors.open";
        }
      });
    corePlugin.registerStateValue(new StateValueProvider() {
        @Override
        public long getValue() {
          try {
            final Method getMaxFileDescriptorCount = operatingSystemMXBean.getClass()
              .getDeclaredMethod("getMaxFileDescriptorCount");
            getMaxFileDescriptorCount.setAccessible(true);

            return ((Long) getMaxFileDescriptorCount.invoke(operatingSystemMXBean)).longValue();

          } catch (NoSuchMethodException e) {
            return -1;
          } catch (IllegalAccessException e) {
            return -1;
          } catch (InvocationTargetException e) {
            return -1;
          }
        }

        @Override
        public String getName() {
          return "jvm.filedescriptors.max";
        }
      });

    List<GarbageCollectorMXBean> garbageCollectors = VirtualMachineMBeans.getInstance().getGarbageCollectors();
    LOGGER.info("found " + garbageCollectors.size() + " garbage collectors");
    for (GarbageCollectorMXBean gc : garbageCollectors) {
      final GarbageCollectorMXBean finalGC = gc;
      LOGGER.info("adding garbage collector " + gc.getName());

      corePlugin.registerStateValue(new StateValueProvider() {
          @Override
          public long getValue() {
            return finalGC.getCollectionCount();
          }

          @Override
          public String getName() {
            return "jvm.gc." + finalGC.getName() + ".count";
          }
        });
      corePlugin.registerStateValue(new StateValueProvider() {
          @Override
          public long getValue() {
            return finalGC.getCollectionTime();
          }

          @Override
          public String getName() {
            return "jvm.gc." + finalGC.getName() + ".time";
          }
        });
    }

    corePlugin.registerMultiValueProvider(new ThreadStateProvider());
  }


  public static class ThreadStateProvider implements MultiValueProvider {
    @Override
    public Collection<de.is24.util.monitoring.State> getValues() {
      final List<de.is24.util.monitoring.State> threadStates = new ArrayList<de.is24.util.monitoring.State>();
      final Map<State, Integer> conditions = new HashMap<State, Integer>();

      for (State state : State.values()) {
        conditions.put(state, 0);
      }

      ThreadMXBean threadMXBean = VirtualMachineMBeans.getInstance().getThreads();
      final long[] allThreadIds = threadMXBean.getAllThreadIds();
      final ThreadInfo[] allThreads = threadMXBean.getThreadInfo(allThreadIds);
      int liveCount = 0;
      for (ThreadInfo info : allThreads) {
        if (info != null) {
          final State state = info.getThreadState();
          conditions.put(state, conditions.get(state) + 1);
          liveCount++;
        }
      }

      long total = 0;
      for (State state : new ArrayList<State>(conditions.keySet())) {
        Integer value = conditions.get(state);
        total = total + value.longValue();
        threadStates.add(new de.is24.util.monitoring.State("jvm.threads", state.name(), value));
      }
      threadStates.add(new de.is24.util.monitoring.State("jvm.threads", "total", total));
      return threadStates;
    }

    @Override
    public String getName() {
      return "VMThreadStates";
    }

    @Override
    public void accept(ReportVisitor visitor) {
      visitor.reportMultiValue(this);
    }

  }


  public Map<String, BufferPoolStats> getBufferPoolStats() {
    try {
      final String[] attributes = { "Count", "MemoryUsed", "TotalCapacity" };

      final ObjectName direct = new ObjectName("java.nio:type=BufferPool,name=direct");
      final ObjectName mapped = new ObjectName("java.nio:type=BufferPool,name=mapped");

      MBeanServer mBeanServer = VirtualMachineMBeans.getInstance().getMBeanServer();
      final AttributeList directAttributes = mBeanServer.getAttributes(direct, attributes);
      final AttributeList mappedAttributes = mBeanServer.getAttributes(mapped, attributes);

      final Map<String, BufferPoolStats> stats = new TreeMap<String, BufferPoolStats>();

      final BufferPoolStats directStats = new BufferPoolStats((Long) ((Attribute) directAttributes.get(0)).getValue(),
        (Long) ((Attribute) directAttributes.get(1)).getValue(),
        (Long) ((Attribute) directAttributes.get(2)).getValue());

      stats.put("direct", directStats);

      final BufferPoolStats mappedStats = new BufferPoolStats((Long) ((Attribute) mappedAttributes.get(0)).getValue(),
        (Long) ((Attribute) mappedAttributes.get(1)).getValue(),
        (Long) ((Attribute) mappedAttributes.get(2)).getValue());

      stats.put("mapped", mappedStats);

      return Collections.unmodifiableMap(stats);
    } catch (JMException e) {
      return Collections.emptyMap();
    }
  }
}
