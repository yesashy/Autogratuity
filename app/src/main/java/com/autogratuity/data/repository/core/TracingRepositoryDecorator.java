package com.autogratuity.data.repository.core;

import android.os.SystemClock;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A decorator for repository interfaces that adds performance tracing capabilities.
 * <p>
 * This class uses the decorator pattern to wrap repository implementations and add
 * method timing and logging functionality without modifying the original repositories.
 * <p>
 * Usage:
 * <pre>
 * DeliveryRepository originalRepo = new DeliveryRepositoryImpl(context);
 * DeliveryRepository tracingRepo = TracingRepositoryDecorator.create(originalRepo, DeliveryRepository.class);
 * </pre>
 * 
 * @param <T> The repository interface type
 */
public class TracingRepositoryDecorator<T> implements InvocationHandler {
    
    private static final String TAG = "RepositoryTracing";
    
    // Default thresholds for warning levels (milliseconds)
    private static final long DEFAULT_INFO_THRESHOLD_MS = 50;
    private static final long DEFAULT_WARN_THRESHOLD_MS = 300;
    private static final long DEFAULT_ERROR_THRESHOLD_MS = 1000;
    
    // Log levels
    private static final int LEVEL_INFO = 0;
    private static final int LEVEL_WARN = 1;
    private static final int LEVEL_ERROR = 2;
    
    // Method timing statistics
    private static final Map<String, MethodStats> methodStats = new HashMap<>();
    
    // The wrapped repository instance
    private final T target;
    
    // Configuration
    private final boolean detailedLogging;
    private final long infoThresholdMs;
    private final long warnThresholdMs;
    private final long errorThresholdMs;
    
    /**
     * Create a new TracingRepositoryDecorator.
     * 
     * @param target The repository instance to wrap
     * @param detailedLogging Whether to log detailed parameter and result information
     * @param infoThresholdMs Threshold for info-level logs (ms)
     * @param warnThresholdMs Threshold for warning-level logs (ms)
     * @param errorThresholdMs Threshold for error-level logs (ms)
     */
    private TracingRepositoryDecorator(T target, boolean detailedLogging, 
                                     long infoThresholdMs, long warnThresholdMs, long errorThresholdMs) {
        this.target = target;
        this.detailedLogging = detailedLogging;
        this.infoThresholdMs = infoThresholdMs;
        this.warnThresholdMs = warnThresholdMs;
        this.errorThresholdMs = errorThresholdMs;
    }
    
    /**
     * Create a repository proxy with tracing capabilities.
     * 
     * @param <T> The repository interface type
     * @param repository The repository implementation to wrap
     * @param repositoryInterface The repository interface class
     * @return A proxy that implements the repository interface with tracing
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(T repository, Class<T> repositoryInterface) {
        return create(repository, repositoryInterface, false);
    }
    
    /**
     * Create a repository proxy with tracing capabilities.
     * 
     * @param <T> The repository interface type
     * @param repository The repository implementation to wrap
     * @param repositoryInterface The repository interface class
     * @param detailedLogging Whether to log detailed parameter and result information
     * @return A proxy that implements the repository interface with tracing
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(T repository, Class<T> repositoryInterface, boolean detailedLogging) {
        return create(repository, repositoryInterface, detailedLogging,
                DEFAULT_INFO_THRESHOLD_MS, DEFAULT_WARN_THRESHOLD_MS, DEFAULT_ERROR_THRESHOLD_MS);
    }
    
    /**
     * Create a repository proxy with tracing capabilities and custom thresholds.
     * 
     * @param <T> The repository interface type
     * @param repository The repository implementation to wrap
     * @param repositoryInterface The repository interface class
     * @param detailedLogging Whether to log detailed parameter and result information
     * @param infoThresholdMs Threshold for info-level logs (ms)
     * @param warnThresholdMs Threshold for warning-level logs (ms)
     * @param errorThresholdMs Threshold for error-level logs (ms)
     * @return A proxy that implements the repository interface with tracing
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(T repository, Class<T> repositoryInterface, boolean detailedLogging,
                              long infoThresholdMs, long warnThresholdMs, long errorThresholdMs) {
        return (T) Proxy.newProxyInstance(
                repositoryInterface.getClassLoader(),
                new Class<?>[] { repositoryInterface },
                new TracingRepositoryDecorator<>(repository, detailedLogging, 
                        infoThresholdMs, warnThresholdMs, errorThresholdMs)
        );
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Skip Object methods like equals, hashCode, toString
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(target, args);
        }
        
        // Get repository and method names for logging
        String repoName = target.getClass().getSimpleName();
        String methodName = method.getName();
        String fullMethodName = repoName + "." + methodName;
        
        // Start timing
        long startTime = SystemClock.elapsedRealtimeNanos();
        
        // Log method invocation if detailed logging is enabled
        if (detailedLogging) {
            logMethodInvocation(fullMethodName, args);
        }
        
        try {
            // Execute the original method
            Object result = method.invoke(target, args);
            
            // Calculate execution time
            long endTime = SystemClock.elapsedRealtimeNanos();
            long durationNanos = endTime - startTime;
            long durationMs = TimeUnit.NANOSECONDS.toMillis(durationNanos);
            
            // Update statistics
            updateMethodStats(fullMethodName, durationMs);
            
            // Log execution time based on thresholds
            logExecutionTime(fullMethodName, durationMs);
            
            // Log result if detailed logging is enabled
            if (detailedLogging) {
                logMethodResult(fullMethodName, result);
            }
            
            return result;
        } catch (Throwable t) {
            // Calculate execution time even for errors
            long endTime = SystemClock.elapsedRealtimeNanos();
            long durationNanos = endTime - startTime;
            long durationMs = TimeUnit.NANOSECONDS.toMillis(durationNanos);
            
            // Update statistics for errors
            updateMethodStats(fullMethodName, durationMs, true);
            
            // Log error with execution time
            Log.e(TAG, String.format("ERROR in %s after %dms: %s",
                    fullMethodName, durationMs, t.getMessage()));
            
            throw t.getCause(); // Unwrap the InvocationTargetException
        }
    }
    
    /**
     * Log method invocation details.
     * 
     * @param methodName Method name for logging
     * @param args Method arguments
     */
    private void logMethodInvocation(String methodName, Object[] args) {
        StringBuilder logMessage = new StringBuilder("Invoking ").append(methodName).append("(");
        
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    logMessage.append(", ");
                }
                
                Object arg = args[i];
                if (arg == null) {
                    logMessage.append("null");
                } else if (arg instanceof String) {
                    // Truncate long strings
                    String strArg = (String) arg;
                    if (strArg.length() > 50) {
                        logMessage.append("\"").append(strArg.substring(0, 47)).append("...\"");
                    } else {
                        logMessage.append("\"").append(strArg).append("\"");
                    }
                } else if (arg.getClass().isArray()) {
                    logMessage.append(Arrays.toString((Object[]) arg));
                } else {
                    logMessage.append(arg.toString());
                }
            }
        }
        
        logMessage.append(")");
        Log.d(TAG, logMessage.toString());
    }
    
    /**
     * Log method result.
     * 
     * @param methodName Method name for logging
     * @param result Method result
     */
    private void logMethodResult(String methodName, Object result) {
        StringBuilder logMessage = new StringBuilder(methodName).append(" returned ");
        
        if (result == null) {
            logMessage.append("null");
        } else if (result instanceof String) {
            // Truncate long strings
            String strResult = (String) result;
            if (strResult.length() > 50) {
                logMessage.append("\"").append(strResult.substring(0, 47)).append("...\"");
            } else {
                logMessage.append("\"").append(strResult).append("\"");
            }
        } else if (result.getClass().isArray()) {
            logMessage.append(Arrays.toString((Object[]) result));
        } else {
            // For complex objects, just show the class name and toString length
            logMessage.append("[")
                    .append(result.getClass().getSimpleName())
                    .append(" with ")
                    .append(result.toString().length())
                    .append(" chars]");
        }
        
        Log.d(TAG, logMessage.toString());
    }
    
    /**
     * Log execution time based on configured thresholds.
     * 
     * @param methodName Method name for logging
     * @param durationMs Execution time in milliseconds
     */
    private void logExecutionTime(String methodName, long durationMs) {
        int logLevel;
        if (durationMs >= errorThresholdMs) {
            logLevel = LEVEL_ERROR;
        } else if (durationMs >= warnThresholdMs) {
            logLevel = LEVEL_WARN;
        } else if (durationMs >= infoThresholdMs) {
            logLevel = LEVEL_INFO;
        } else {
            // Below threshold, don't log
            return;
        }
        
        String logMessage = String.format("%s completed in %dms", methodName, durationMs);
        
        switch (logLevel) {
            case LEVEL_ERROR:
                Log.e(TAG, logMessage);
                break;
            case LEVEL_WARN:
                Log.w(TAG, logMessage);
                break;
            case LEVEL_INFO:
                Log.i(TAG, logMessage);
                break;
        }
    }
    
    /**
     * Update method timing statistics.
     * 
     * @param methodName Method name for tracking
     * @param durationMs Execution time in milliseconds
     */
    private void updateMethodStats(String methodName, long durationMs) {
        updateMethodStats(methodName, durationMs, false);
    }
    
    /**
     * Update method timing statistics.
     * 
     * @param methodName Method name for tracking
     * @param durationMs Execution time in milliseconds
     * @param isError Whether the invocation resulted in an error
     */
    private synchronized void updateMethodStats(String methodName, long durationMs, boolean isError) {
        MethodStats stats = methodStats.get(methodName);
        if (stats == null) {
            stats = new MethodStats();
            methodStats.put(methodName, stats);
        }
        
        // Update statistics
        stats.count++;
        stats.totalTimeMs += durationMs;
        stats.minTimeMs = Math.min(stats.minTimeMs, durationMs);
        stats.maxTimeMs = Math.max(stats.maxTimeMs, durationMs);
        
        if (isError) {
            stats.errorCount++;
        }
        
        // Update average
        stats.avgTimeMs = stats.totalTimeMs / stats.count;
    }
    
    /**
     * Get statistics for all repository methods.
     * 
     * @return Map of method names to their statistics
     */
    public static Map<String, MethodStats> getStatistics() {
        return new HashMap<>(methodStats);
    }
    
    /**
     * Reset all statistics.
     */
    public static void resetStatistics() {
        methodStats.clear();
    }
    
    /**
     * Class to track method execution statistics.
     */
    public static class MethodStats {
        private long count = 0;
        private long errorCount = 0;
        private long totalTimeMs = 0;
        private long minTimeMs = Long.MAX_VALUE;
        private long maxTimeMs = 0;
        private long avgTimeMs = 0;
        
        /**
         * Get the number of times the method has been called.
         * 
         * @return Call count
         */
        public long getCount() {
            return count;
        }
        
        /**
         * Get the number of times the method has resulted in an error.
         * 
         * @return Error count
         */
        public long getErrorCount() {
            return errorCount;
        }
        
        /**
         * Get the total time spent executing the method (in milliseconds).
         * 
         * @return Total execution time (ms)
         */
        public long getTotalTimeMs() {
            return totalTimeMs;
        }
        
        /**
         * Get the minimum execution time (in milliseconds).
         * 
         * @return Minimum execution time (ms)
         */
        public long getMinTimeMs() {
            return minTimeMs == Long.MAX_VALUE ? 0 : minTimeMs;
        }
        
        /**
         * Get the maximum execution time (in milliseconds).
         * 
         * @return Maximum execution time (ms)
         */
        public long getMaxTimeMs() {
            return maxTimeMs;
        }
        
        /**
         * Get the average execution time (in milliseconds).
         * 
         * @return Average execution time (ms)
         */
        public long getAvgTimeMs() {
            return avgTimeMs;
        }
    }
}