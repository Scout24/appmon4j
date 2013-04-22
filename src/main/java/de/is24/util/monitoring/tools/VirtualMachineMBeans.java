package de.is24.util.monitoring.tools;

import javax.management.MBeanServer;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.Thread.State;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MonitorInfo;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * A collection of Java Virtual Machine metrics.
 * This code was originally taken from de.is24.util.monitoring.tools.VirtualMachineMetrics
 */
public class VirtualMachineMBeans {
  private static final int MAX_STACK_TRACE_DEPTH = 100;

  private static final VirtualMachineMBeans INSTANCE = new VirtualMachineMBeans(
    ManagementFactory.getMemoryMXBean(),
    ManagementFactory.getMemoryPoolMXBeans(),
    ManagementFactory.getOperatingSystemMXBean(),
    ManagementFactory.getThreadMXBean(),
    ManagementFactory.getGarbageCollectorMXBeans(),
    ManagementFactory.getRuntimeMXBean(),
    ManagementFactory.getPlatformMBeanServer());

  /**
   * The default instance of {@link VirtualMachineMBeans}.
   *
   * @return the default {@link VirtualMachineMBeans instance}
   */
  public static VirtualMachineMBeans getInstance() {
    return INSTANCE;
  }


  private final MemoryMXBean memory;
  private final List<MemoryPoolMXBean> memoryPools;
  private final OperatingSystemMXBean os;
  private final ThreadMXBean threads;
  private final List<GarbageCollectorMXBean> garbageCollectors;
  private final RuntimeMXBean runtime;
  private final MBeanServer mBeanServer;

  VirtualMachineMBeans(MemoryMXBean memory, List<MemoryPoolMXBean> memoryPools,
                       OperatingSystemMXBean os,
                       ThreadMXBean threads, List<GarbageCollectorMXBean> garbageCollectors,
                       RuntimeMXBean runtime, MBeanServer mBeanServer) {
    this.memory = memory;
    this.memoryPools = memoryPools;
    this.os = os;
    this.threads = threads;
    this.garbageCollectors = garbageCollectors;
    this.runtime = runtime;
    this.mBeanServer = mBeanServer;
  }

  public MemoryMXBean getMemory() {
    return memory;
  }

  public List<MemoryPoolMXBean> getMemoryPools() {
    return memoryPools;
  }

  public OperatingSystemMXBean getOs() {
    return os;
  }

  public ThreadMXBean getThreads() {
    return threads;
  }

  public List<GarbageCollectorMXBean> getGarbageCollectors() {
    return garbageCollectors;
  }

  public RuntimeMXBean getRuntime() {
    return runtime;
  }

  public MBeanServer getMBeanServer() {
    return mBeanServer;
  }

  /**
  * Returns the version of the currently-running jvm.
  *
  * @return the version of the currently-running jvm, eg "1.6.0_24"
  * @see <a href="http://java.sun.com/j2se/versioning_naming.html">J2SE SDK/JRE Version String
  *      Naming Convention</a>
  */
  public String getVersion() {
    return System.getProperty("java.runtime.version");
  }

  /**
   * Returns the name of the currently-running jvm.
   *
   * @return the name of the currently-running jvm, eg  "Java HotSpot(TM) Client VM"
   * @see <a href="http://download.oracle.com/javase/6/docs/api/java/lang/System.html#getProperties()">System.getProperties()</a>
   */
  public String getName() {
    return System.getProperty("java.vm.name");
  }


  /**
   * Returns a set of strings describing deadlocked threads, if any are deadlocked.
   *
   * @return a set of any deadlocked threads
   */
  public Set<String> getDeadlockedThreads() {
    final long[] threadIds = threads.findDeadlockedThreads();
    if (threadIds != null) {
      final Set<String> threads = new HashSet<String>();
      for (ThreadInfo info : this.threads.getThreadInfo(threadIds, MAX_STACK_TRACE_DEPTH)) {
        final StringBuilder stackTrace = new StringBuilder();
        for (StackTraceElement element : info.getStackTrace()) {
          stackTrace.append("\t at ").append(element.toString()).append('\n');
        }

        threads.add(
          String.format(
            "%s locked on %s (owned by %s):\n%s",
            info.getThreadName(), info.getLockName(),
            info.getLockOwnerName(),
            stackTrace.toString()));
      }
      return Collections.unmodifiableSet(threads);
    }
    return Collections.emptySet();
  }


  /**
   * Dumps all of the threads' current information to an output stream.
   *
   * @param out an output stream
   */
  public void getThreadDump(OutputStream out) {
    final ThreadInfo[] threads = this.threads.dumpAllThreads(true, true);
    final PrintWriter writer = new PrintWriter(out, true);

    for (int ti = threads.length - 1; ti >= 0; ti--) {
      final ThreadInfo t = threads[ti];
      writer.printf("%s id=%d state=%s",
        t.getThreadName(),
        t.getThreadId(),
        t.getThreadState());

      final LockInfo lock = t.getLockInfo();
      if ((lock != null) && (t.getThreadState() != State.BLOCKED)) {
        writer.printf("\n    - waiting on <0x%08x> (a %s)",
          lock.getIdentityHashCode(),
          lock.getClassName());
        writer.printf("\n    - locked <0x%08x> (a %s)",
          lock.getIdentityHashCode(),
          lock.getClassName());
      } else if ((lock != null) && (t.getThreadState() == State.BLOCKED)) {
        writer.printf("\n    - waiting to lock <0x%08x> (a %s)",
          lock.getIdentityHashCode(),
          lock.getClassName());
      }

      if (t.isSuspended()) {
        writer.print(" (suspended)");
      }

      if (t.isInNative()) {
        writer.print(" (running in native)");
      }

      writer.println();
      if (t.getLockOwnerName() != null) {
        writer.printf("     owned by %s id=%d\n", t.getLockOwnerName(), t.getLockOwnerId());
      }

      final StackTraceElement[] elements = t.getStackTrace();
      final MonitorInfo[] monitors = t.getLockedMonitors();

      for (int i = 0; i < elements.length; i++) {
        final StackTraceElement element = elements[i];
        writer.printf("    at %s\n", element);
        for (int j = 1; j < monitors.length; j++) {
          final MonitorInfo monitor = monitors[j];
          if (monitor.getLockedStackDepth() == i) {
            writer.printf("      - locked %s\n", monitor);
          }
        }
      }
      writer.println();

      final LockInfo[] locks = t.getLockedSynchronizers();
      if (locks.length > 0) {
        writer.printf("    Locked synchronizers: count = %d\n", locks.length);
        for (LockInfo l : locks) {
          writer.printf("      - %s\n", l);
        }
        writer.println();
      }
    }

    writer.println();
    writer.flush();
  }

}
