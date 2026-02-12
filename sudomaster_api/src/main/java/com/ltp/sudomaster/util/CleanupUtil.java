package com.ltp.sudomaster.util;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
import jakarta.persistence.OptimisticLockException;

@Slf4j
public class CleanupUtil {

    private static final int DEFAULT_MAX_RETRIES = 2;
    private static final long BASE_RETRY_DELAY_MS = 100L;

    public static boolean safeDelete(
            String entityType,
            Object entityId,
            Runnable deleteOperation,
            java.util.function.BooleanSupplier verifyNotExists) {
        return safeDelete(entityType, entityId, deleteOperation, verifyNotExists, DEFAULT_MAX_RETRIES);
    }

    public static boolean safeDelete(
            String entityType,
            Object entityId,
            Runnable deleteOperation,
            java.util.function.BooleanSupplier verifyNotExists,
            int maxRetries) {
        
        log.debug("  Attempting {} deletion: {}", entityType, entityId);
        
        try {
            deleteOperation.run();
            
            if (verifyNotExists.getAsBoolean()) {
                log.debug("{} deleted successfully", entityType);
                return true;
            }
            
            log.warn("{} still exists after delete, retrying...", entityType);
            
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    Thread.sleep(BASE_RETRY_DELAY_MS * attempt);
                    deleteOperation.run();
                    
                    if (verifyNotExists.getAsBoolean()) {
                        log.info("{} delete retry succeeded on attempt {}/{}", 
                                 entityType, attempt, maxRetries);
                        return true;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Retry attempt {}/{} interrupted for {}", 
                             attempt, maxRetries, entityType);
                    break;
                } catch (Exception e) {
                    log.warn("Retry attempt {}/{} failed for {}: {}", 
                             attempt, maxRetries, entityType, e.getMessage());
                }
            }
            
            log.error("FAILED: {} still exists after {} retries", entityType, maxRetries);
            return false;
            
        } catch (Exception e) {
            log.error("Error deleting {}: {}", entityType, e.getMessage(), e);
            return false;
        }
    }

    public static boolean safeDeleteCollection(
            String entityType,
            long count,
            Runnable deleteOperation,
            java.util.function.LongSupplier verifyCount) {
        return safeDeleteCollection(entityType, count, deleteOperation, verifyCount, DEFAULT_MAX_RETRIES);
    }

    public static boolean safeDeleteCollection(
            String entityType,
            long count,
            Runnable deleteOperation,
            java.util.function.LongSupplier verifyCount,
            int maxRetries) {
        
        log.debug("  Deleting {} {} entities...", count, entityType);
        
        try {
            deleteOperation.run();
            long remaining = verifyCount.getAsLong();
            
            if (remaining == 0) {
                log.debug("All {} {} deleted successfully", count, entityType);
                return true;
            }
            
            log.warn("{} {} still exist after delete, retrying...", remaining, entityType);
            
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    Thread.sleep(BASE_RETRY_DELAY_MS * attempt);
                    deleteOperation.run();
                    remaining = verifyCount.getAsLong();
                    
                    if (remaining == 0) {
                        log.info("{} deletion retry succeeded on attempt {}/{}", 
                                 entityType, attempt, maxRetries);
                        return true;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Retry attempt {}/{} interrupted for {}", 
                             attempt, maxRetries, entityType);
                    break;
                } catch (Exception e) {
                    log.warn("Retry attempt {}/{} failed for {}: {}", 
                             attempt, maxRetries, entityType, e.getMessage());
                }
            }
            
            log.error("FAILED: {} {} still exist after {} retries", 
                     remaining, entityType, maxRetries);
            return false;
            
        } catch (Exception e) {
            log.error("Error deleting {}: {}", entityType, e.getMessage(), e);
            return false;
        }
    }

    public static <T> T safeSaveWithRetry(
            String operationName,
            java.util.function.Supplier<T> saveOperation) {
        return safeSaveWithRetry(operationName, saveOperation, DEFAULT_MAX_RETRIES);
    }

    public static <T> T safeSaveWithRetry(
            String operationName,
            java.util.function.Supplier<T> saveOperation,
            int maxRetries) {
        
        log.debug("Attempting save: {}", operationName);
        
        try {
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    T result = saveOperation.get();
                    log.debug("Save succeeded: {}", operationName);
                    return result;
                } catch (StaleObjectStateException | OptimisticLockException e) {
                    log.warn("Optimistic lock conflict on attempt {}/{}: {}", 
                            attempt, maxRetries, e.getMessage());
                    if (attempt < maxRetries) {
                        try {
                            Thread.sleep(50L * attempt);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Save operation interrupted", ie);
                        }
                    } else {
                        throw new RuntimeException(
                            "Failed to save " + operationName + " after " + maxRetries + " retries: " + e.getMessage(), e);
                    }
                }
            }
        } catch (RuntimeException e) {
            log.error("Save failed: {} - {}", operationName, e.getMessage());
            throw e;
        }
        
        throw new RuntimeException("Failed to complete save operation: " + operationName);
    }
}
