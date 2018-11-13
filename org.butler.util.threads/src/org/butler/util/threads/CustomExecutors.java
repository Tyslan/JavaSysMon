package org.butler.util.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomExecutors {
	/**
	 * Creates a thread pool that creates new threads as needed, but will reuse
	 * previously constructed threads when they are available. The name of the
	 * created Threads will contain the prefix.
	 *
	 * @param threadPrefix
	 * @return
	 */
	public static ExecutorService newCachedThreadPool(String threadPrefix) {
		return Executors.newCachedThreadPool(new DefaultNamedThreadFactory(threadPrefix));
	}

	/**
	 * Creates a thread pool that reuses a fixed number of threads operating off a
	 * shared unbounded queue, using the provided ThreadFactory to create new
	 * threads when needed. The name of the created Threads will contain the prefix.
	 *
	 * @param corePoolSize
	 * @param threadPrefix
	 * @return
	 */
	public static ExecutorService newFixedThreadPool(int corePoolSize, String threadPrefix) {
		return Executors.newFixedThreadPool(corePoolSize, new DefaultNamedThreadFactory(threadPrefix));
	}

	/**
	 * Creates an Executor that uses a single worker thread operating off an
	 * unbounded queue. The name of the created Threads will contain the prefix.
	 *
	 * @param threadPrefix
	 * @return
	 */
	public static ExecutorService newSingleThreadExecutor(String threadPrefix) {
		return Executors.newSingleThreadExecutor(new DefaultNamedThreadFactory(threadPrefix));
	}

	/**
	 * Creates a single-threaded executor that can schedule commands to run after a
	 * given delay, or to execute periodically. The name of the created Threads will
	 * contain the prefix.
	 *
	 * @param threadPrefix
	 * @return
	 */
	public static ScheduledExecutorService newSingleThreadScheduledExecutor(String threadPrefix) {
		return Executors.newSingleThreadScheduledExecutor(new DefaultNamedThreadFactory(threadPrefix));
	}

	/**
	 * Creates a thread pool that can schedule commands to run after a given delay,
	 * or to execute periodically.The name of the created Threads will contain the
	 * prefix.
	 *
	 * @param corePoolSize
	 * @param threadPrefix
	 * @return
	 */
	public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize, String threadPrefix) {
		return Executors.newScheduledThreadPool(corePoolSize, new DefaultNamedThreadFactory(threadPrefix));
	}

	/**
	 * The following method shuts down an ExecutorService in two phases, first by
	 * calling shutdown to reject incoming tasks, and then calling shutdownNow, if
	 * necessary, to cancel any lingering tasks.
	 * <p>
	 * Shutdown of the Executor could take up to double the timeout provided.
	 * <p>
	 * Based on the code found in the Java 8 API pages.
	 *
	 * @param executor
	 * @param timeout
	 * @param unit
	 * @return True if the Executor shut down correctly, False otherwise.
	 */
	public static boolean shutdownAndAwaitTermination(ExecutorService executor, long timeout, TimeUnit unit) {
		// Disable new tasks from being submitted
		executor.shutdown();

		try {
			// Wait a while for existing tasks to terminate
			if (!executor.awaitTermination(timeout, unit)) {

				// Cancel currently executing tasks
				executor.shutdownNow();

				// Wait a while for tasks to respond to being cancelled
				if (!executor.awaitTermination(timeout, unit)) {
					return false;
				}
			}
		} catch (InterruptedException e) {
			// (Re-)Cancel if current thread also interrupted
			executor.shutdownNow();

			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}

		return true;
	}

	/**
	 * ThreadFactory with the ability to set the thread name prefix. This class is
	 * exactly similar to
	 * {@link java.util.concurrent.Executors#defaultThreadFactory()} from JDK8,
	 * except for the thread naming feature.
	 * <p>
	 * The factory creates threads that have names on the form
	 * <i>prefix-N-thread-M</i>, where <i>prefix</i> is a string provided in the
	 * constructor, <i>N</i> is the sequence number of this factory, and <i>M</i> is
	 * the sequence number of the thread created by this factory.
	 */
	private static class DefaultNamedThreadFactory implements ThreadFactory {

		private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		public DefaultNamedThreadFactory(String prefix) {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = prefix + "-" + POOL_NUMBER.getAndIncrement() + "-thread-";
		}

		@Override
		public Thread newThread(Runnable r) {
			String threadName = namePrefix + threadNumber.getAndIncrement();

			Thread t = new Thread(group, r, threadName, 0);

			if (t.isDaemon()) {
				t.setDaemon(false);
			}

			if (t.getPriority() != Thread.NORM_PRIORITY) {
				t.setPriority(Thread.NORM_PRIORITY);
			}

			return t;
		}
	}
}
