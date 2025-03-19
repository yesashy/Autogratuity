package com.autogratuity.data.repository.core;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Query.Direction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fluent interface for building standardized Firestore queries.
 * <p>
 * This class provides a standardized approach to creating Firestore queries with
 * validation, error handling, and a consistent interface. It supports all common
 * Firestore query operations through a fluent builder pattern.
 * <p>
 * Example usage:
 * <pre>
 * Query query = QueryBuilder.collection("deliveries")
 *     .whereEqualTo("userId", userId)
 *     .whereGreaterThan("amount", 10.0)
 *     .orderBy("timestamp", Direction.DESCENDING)
 *     .limit(20)
 *     .build();
 * </pre>
 */
public class QueryBuilder {
    private static final String TAG = "QueryBuilder";

    // Maximum values for Firestore limitations
    private static final int MAX_LIMIT = 1000;
    private static final int MAX_WHERE_CLAUSES = 10;
    private static final int MAX_ORDER_BY_CLAUSES = 10;
    
    // Firestore instance
    private final FirebaseFirestore db;
    
    // Base collection reference
    private CollectionReference collectionRef;
    
    // Query components
    private final List<WhereClause> whereClauses = new ArrayList<>();
    private final List<OrderByClause> orderByClauses = new ArrayList<>();
    private Integer limitValue;
    private DocumentReference startAfterDocument;
    private DocumentReference endBeforeDocument;
    private Object[] startAtValues;
    private Object[] startAfterValues;
    private Object[] endAtValues;
    private Object[] endBeforeValues;
    
    // Tracking for clauses added for validation
    private final Map<String, Boolean> hasOrderByForField = new HashMap<>();
    
    /**
     * Private constructor to enforce factory method usage
     *
     * @param db FirebaseFirestore instance
     * @param collectionPath Path to the collection
     */
    private QueryBuilder(FirebaseFirestore db, String collectionPath) {
        this.db = db;
        this.collectionRef = db.collection(collectionPath);
    }
    
    /**
     * Create a new QueryBuilder for the specified collection
     *
     * @param collectionPath Path to the collection
     * @return New QueryBuilder instance
     */
    public static QueryBuilder collection(String collectionPath) {
        return new QueryBuilder(FirebaseFirestore.getInstance(), collectionPath);
    }
    
    /**
     * Create a new QueryBuilder for the specified collection with a provided FirebaseFirestore instance
     *
     * @param db FirebaseFirestore instance
     * @param collectionPath Path to the collection
     * @return New QueryBuilder instance
     */
    public static QueryBuilder collection(FirebaseFirestore db, String collectionPath) {
        return new QueryBuilder(db, collectionPath);
    }
    
    /**
     * Create a new QueryBuilder from an existing CollectionReference
     *
     * @param collectionRef Existing CollectionReference
     * @return New QueryBuilder instance
     */
    public static QueryBuilder fromCollection(CollectionReference collectionRef) {
        QueryBuilder builder = new QueryBuilder(FirebaseFirestore.getInstance(), collectionRef.getPath());
        builder.collectionRef = collectionRef;
        return builder;
    }
    
    /**
     * Class representing a where clause in the query
     */
    private static class WhereClause {
        final String field;
        final String operator;
        final Object value;
        final boolean isFieldPath;
        
        WhereClause(String field, String operator, Object value) {
            this.field = field;
            this.operator = operator;
            this.value = value;
            this.isFieldPath = false;
        }
        
        WhereClause(FieldPath fieldPath, String operator, Object value) {
            this.field = fieldPath.toString();
            this.operator = operator;
            this.value = value;
            this.isFieldPath = true;
        }
    }
    
    /**
     * Class representing an order by clause in the query
     */
    private static class OrderByClause {
        final String field;
        final Direction direction;
        final boolean isFieldPath;
        
        OrderByClause(String field, Direction direction) {
            this.field = field;
            this.direction = direction;
            this.isFieldPath = false;
        }
        
        OrderByClause(FieldPath fieldPath, Direction direction) {
            this.field = fieldPath.toString();
            this.direction = direction;
            this.isFieldPath = true;
        }
    }
    
    //-----------------------------------------------------------------------------------
    // Where clause methods - add filtering conditions to the query
    //-----------------------------------------------------------------------------------
    
    /**
     * Add a whereEqualTo clause to the query
     *
     * @param field Field name to filter on
     * @param value Value to compare with
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder whereEqualTo(String field, Object value) {
        validateField(field);
        validateValueForField(field, value);
        whereClauses.add(new WhereClause(field, "==", value));
        return this;
    }
    
    /**
     * Add a whereEqualTo clause with FieldPath to the query
     *
     * @param fieldPath FieldPath to filter on
     * @param value Value to compare with
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder whereEqualTo(FieldPath fieldPath, Object value) {
        validateFieldPath(fieldPath);
        validateValueForFieldPath(fieldPath, value);
        whereClauses.add(new WhereClause(fieldPath, "==", value));
        return this;
    }
    
    /**
     * Add a whereNotEqualTo clause to the query
     *
     * @param field Field name to filter on
     * @param value Value to compare with
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder whereNotEqualTo(String field, Object value) {
        validateField(field);
        validateValueForField(field, value);
        whereClauses.add(new WhereClause(field, "!=", value));
        return this;
    }
    
    /**
     * Add a whereNotEqualTo clause with FieldPath to the query
     *
     * @param fieldPath FieldPath to filter on
     * @param value Value to compare with
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder whereNotEqualTo(FieldPath fieldPath, Object value) {
        validateFieldPath(fieldPath);
        validateValueForFieldPath(fieldPath, value);
        whereClauses.add(new WhereClause(fieldPath, "!=", value));
        return this;
    }
    
    /**
     * Add a whereGreaterThan clause to the query
     *
     * @param field Field name to filter on
     * @param value Value to compare with
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder whereGreaterThan(String field, Object value) {
        validateField(field);
        validateValueForField(field, value);
        whereClauses.add(new WhereClause(field, ">", value));
        return this;
    }
    
    /**
     * Add a whereGreaterThanOrEqualTo clause to the query
     *
     * @param field Field name to filter on
     * @param value Value to compare with
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder whereGreaterThanOrEqualTo(String field, Object value) {
        validateField(field);
        validateValueForField(field, value);
        whereClauses.add(new WhereClause(field, ">=", value));
        return this;
    }
    
    /**
     * Add a whereLessThan clause to the query
     *
     * @param field Field name to filter on
     * @param value Value to compare with
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder whereLessThan(String field, Object value) {
        validateField(field);
        validateValueForField(field, value);
        whereClauses.add(new WhereClause(field, "<", value));
        return this;
    }
    
    /**
     * Add a whereLessThanOrEqualTo clause to the query
     *
     * @param field Field name to filter on
     * @param value Value to compare with
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder whereLessThanOrEqualTo(String field, Object value) {
        validateField(field);
        validateValueForField(field, value);
        whereClauses.add(new WhereClause(field, "<=", value));
        return this;
    }
    
    /**
     * Add a whereArrayContains clause to the query
     *
     * @param field Field name to filter on
     * @param value Value to check for in the array
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder whereArrayContains(String field, Object value) {
        validateField(field);
        validateValueForField(field, value);
        whereClauses.add(new WhereClause(field, "array-contains", value));
        return this;
    }
    
    /**
     * Add a whereArrayContainsAny clause to the query
     *
     * @param field Field name to filter on
     * @param values Values to check for in the array
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder whereArrayContainsAny(String field, List<?> values) {
        validateField(field);
        validateArray(values, "whereArrayContainsAny");
        whereClauses.add(new WhereClause(field, "array-contains-any", values));
        return this;
    }
    
    /**
     * Add a whereIn clause to the query
     *
     * @param field Field name to filter on
     * @param values Values to check for field equality
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder whereIn(String field, List<?> values) {
        validateField(field);
        validateArray(values, "whereIn");
        whereClauses.add(new WhereClause(field, "in", values));
        return this;
    }
    
    /**
     * Add a whereNotIn clause to the query
     *
     * @param field Field name to filter on
     * @param values Values to check for field inequality
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder whereNotIn(String field, List<?> values) {
        validateField(field);
        validateArray(values, "whereNotIn");
        whereClauses.add(new WhereClause(field, "not-in", values));
        return this;
    }
    
    /**
     * Add a whereIsNull clause to the query
     * This is implemented as an additional filter with a null value
     *
     * @param field Field name to filter on
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder whereIsNull(String field) {
        validateField(field);
        whereClauses.add(new WhereClause(field, "==", null));
        return this;
    }
    
    /**
     * Add a whereIsNotNull clause to the query
     * This is implemented as an additional filter with a not equals null operation
     *
     * @param field Field name to filter on
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder whereIsNotNull(String field) {
        validateField(field);
        whereClauses.add(new WhereClause(field, "!=", null));
        return this;
    }
    
    //-----------------------------------------------------------------------------------
    // Order by methods - add sorting to the query
    //-----------------------------------------------------------------------------------
    
    /**
     * Add an orderBy clause to the query with ascending direction (default)
     *
     * @param field Field name to order by
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder orderBy(String field) {
        return orderBy(field, Direction.ASCENDING);
    }
    
    /**
     * Add an orderBy clause to the query with specified direction
     *
     * @param field Field name to order by
     * @param direction Direction to order (ASCENDING or DESCENDING)
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder orderBy(String field, Direction direction) {
        validateField(field);
        validateOrderBy(field);
        orderByClauses.add(new OrderByClause(field, direction));
        hasOrderByForField.put(field, true);
        return this;
    }
    
    /**
     * Add an orderBy clause with FieldPath to the query with ascending direction (default)
     *
     * @param fieldPath FieldPath to order by
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder orderBy(FieldPath fieldPath) {
        return orderBy(fieldPath, Direction.ASCENDING);
    }
    
    /**
     * Add an orderBy clause with FieldPath to the query with specified direction
     *
     * @param fieldPath FieldPath to order by
     * @param direction Direction to order (ASCENDING or DESCENDING)
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder orderBy(FieldPath fieldPath, Direction direction) {
        validateFieldPath(fieldPath);
        validateOrderBy(fieldPath.toString());
        orderByClauses.add(new OrderByClause(fieldPath, direction));
        hasOrderByForField.put(fieldPath.toString(), true);
        return this;
    }
    
    //-----------------------------------------------------------------------------------
    // Limit methods - add result limitations to the query
    //-----------------------------------------------------------------------------------
    
    /**
     * Add a limit clause to the query
     *
     * @param limit Maximum number of documents to return
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder limit(int limit) {
        validateLimit(limit);
        limitValue = limit;
        return this;
    }
    
    //-----------------------------------------------------------------------------------
    // Pagination methods - add pagination to the query
    //-----------------------------------------------------------------------------------
    
    /**
     * Add a startAt clause to the query using field values
     *
     * @param fieldValues Values to start at (must match orderBy fields)
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder startAt(Object... fieldValues) {
        validateCursorValues(fieldValues, "startAt");
        startAtValues = fieldValues;
        return this;
    }
    
    /**
     * Add a startAfter clause to the query using field values
     *
     * @param fieldValues Values to start after (must match orderBy fields)
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder startAfter(Object... fieldValues) {
        validateCursorValues(fieldValues, "startAfter");
        startAfterValues = fieldValues;
        return this;
    }
    
    /**
     * Add a startAfter clause to the query using a document snapshot
     *
     * @param documentRef Document reference to start after
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder startAfter(DocumentReference documentRef) {
        if (documentRef == null) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.VALIDATION_ERROR,
                    "Document reference cannot be null for startAfter");
        }
        startAfterDocument = documentRef;
        return this;
    }
    
    /**
     * Add an endAt clause to the query using field values
     *
     * @param fieldValues Values to end at (must match orderBy fields)
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder endAt(Object... fieldValues) {
        validateCursorValues(fieldValues, "endAt");
        endAtValues = fieldValues;
        return this;
    }
    
    /**
     * Add an endBefore clause to the query using field values
     *
     * @param fieldValues Values to end before (must match orderBy fields)
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder endBefore(Object... fieldValues) {
        validateCursorValues(fieldValues, "endBefore");
        endBeforeValues = fieldValues;
        return this;
    }
    
    /**
     * Add an endBefore clause to the query using a document snapshot
     *
     * @param documentRef Document reference to end before
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder endBefore(DocumentReference documentRef) {
        if (documentRef == null) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.VALIDATION_ERROR,
                    "Document reference cannot be null for endBefore");
        }
        endBeforeDocument = documentRef;
        return this;
    }
    
    //-----------------------------------------------------------------------------------
    // Utility methods for common query patterns
    //-----------------------------------------------------------------------------------
    
    /**
     * Add a date range filter to the query
     *
     * @param field Field name to filter on
     * @param startDate Start date (inclusive)
     * @param endDate End date (exclusive)
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder whereDateBetween(String field, Date startDate, Date endDate) {
        validateField(field);
        validateDateRange(startDate, endDate);
        
        return whereGreaterThanOrEqualTo(field, startDate)
                .whereLessThan(field, endDate)
                .orderBy(field, Direction.ASCENDING); // Include standard ordering
    }
    
    /**
     * Add a numeric range filter to the query
     *
     * @param field Field name to filter on
     * @param min Minimum value (inclusive)
     * @param max Maximum value (exclusive)
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder whereNumberBetween(String field, Number min, Number max) {
        validateField(field);
        validateNumberRange(min, max);
        
        return whereGreaterThanOrEqualTo(field, min)
                .whereLessThan(field, max)
                .orderBy(field, Direction.ASCENDING); // Include standard ordering
    }
    
    /**
     * Add a filter to return only documents with fields matching a user ID
     *
     * @param userIdField Field name containing user ID
     * @param userId User ID to filter by
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder whereUserId(String userIdField, String userId) {
        validateField(userIdField);
        validateString(userId, "userId");
        
        return whereEqualTo(userIdField, userId);
    }
    
    /**
     * Add a text search filter using array contains
     * Note: This requires the field to be an array of words or tokens
     *
     * @param field Field name to search in
     * @param searchTerm Term to search for
     * @return This QueryBuilder instance for chaining
     */
    public QueryBuilder whereTextSearch(String field, String searchTerm) {
        validateField(field);
        validateString(searchTerm, "searchTerm");
        
        return whereArrayContains(field, searchTerm.toLowerCase().trim());
    }
    
    //-----------------------------------------------------------------------------------
    // Build and validation methods
    //-----------------------------------------------------------------------------------
    
    /**
     * Build the final Firestore Query object
     *
     * @return Firestore Query instance configured with all clauses
     */
    public Query build() {
        validateQuery();
        Query query = collectionRef;
        
        // Apply where clauses
        for (WhereClause whereClause : whereClauses) {
            if (whereClause.isFieldPath) {
                FieldPath fieldPath = FieldPath.fromDotSeparatedPath(whereClause.field);
                switch (whereClause.operator) {
                    case "==":
                        query = query.whereEqualTo(fieldPath, whereClause.value);
                        break;
                    case "!=":
                        query = query.whereNotEqualTo(fieldPath, whereClause.value);
                        break;
                    case ">":
                        query = query.whereGreaterThan(fieldPath, whereClause.value);
                        break;
                    case ">=":
                        query = query.whereGreaterThanOrEqualTo(fieldPath, whereClause.value);
                        break;
                    case "<":
                        query = query.whereLessThan(fieldPath, whereClause.value);
                        break;
                    case "<=":
                        query = query.whereLessThanOrEqualTo(fieldPath, whereClause.value);
                        break;
                    case "array-contains":
                        query = query.whereArrayContains(fieldPath, whereClause.value);
                        break;
                    case "array-contains-any":
                        query = query.whereArrayContainsAny(fieldPath, (List<?>) whereClause.value);
                        break;
                    case "in":
                        query = query.whereIn(fieldPath, (List<?>) whereClause.value);
                        break;
                    case "not-in":
                        query = query.whereNotIn(fieldPath, (List<?>) whereClause.value);
                        break;
                }
            } else {
                switch (whereClause.operator) {
                    case "==":
                        query = query.whereEqualTo(whereClause.field, whereClause.value);
                        break;
                    case "!=":
                        query = query.whereNotEqualTo(whereClause.field, whereClause.value);
                        break;
                    case ">":
                        query = query.whereGreaterThan(whereClause.field, whereClause.value);
                        break;
                    case ">=":
                        query = query.whereGreaterThanOrEqualTo(whereClause.field, whereClause.value);
                        break;
                    case "<":
                        query = query.whereLessThan(whereClause.field, whereClause.value);
                        break;
                    case "<=":
                        query = query.whereLessThanOrEqualTo(whereClause.field, whereClause.value);
                        break;
                    case "array-contains":
                        query = query.whereArrayContains(whereClause.field, whereClause.value);
                        break;
                    case "array-contains-any":
                        query = query.whereArrayContainsAny(whereClause.field, (List<?>) whereClause.value);
                        break;
                    case "in":
                        query = query.whereIn(whereClause.field, (List<?>) whereClause.value);
                        break;
                    case "not-in":
                        query = query.whereNotIn(whereClause.field, (List<?>) whereClause.value);
                        break;
                }
            }
        }
        
        // Apply order by clauses
        for (OrderByClause orderByClause : orderByClauses) {
            if (orderByClause.isFieldPath) {
                FieldPath fieldPath = FieldPath.fromDotSeparatedPath(orderByClause.field);
                query = query.orderBy(fieldPath, orderByClause.direction);
            } else {
                query = query.orderBy(orderByClause.field, orderByClause.direction);
            }
        }
        
        // Apply limit if specified
        if (limitValue != null) {
            query = query.limit(limitValue);
        }
        
        // Apply pagination if specified
        if (startAtValues != null) {
            query = query.startAt(startAtValues);
        }
        
        if (startAfterValues != null) {
            query = query.startAfter(startAfterValues);
        }
        
        if (startAfterDocument != null) {
            query = query.startAfter(startAfterDocument);
        }
        
        if (endAtValues != null) {
            query = query.endAt(endAtValues);
        }
        
        if (endBeforeValues != null) {
            query = query.endBefore(endBeforeValues);
        }
        
        if (endBeforeDocument != null) {
            query = query.endBefore(endBeforeDocument);
        }
        
        return query;
    }
    
    /**
     * Validate the entire query for consistency and Firestore limitations
     */
    private void validateQuery() {
        // Check maximum clause limits
        if (whereClauses.size() > MAX_WHERE_CLAUSES) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.TOO_MANY_CLAUSES,
                    "Query exceeds maximum of " + MAX_WHERE_CLAUSES + " where clauses");
        }
        
        if (orderByClauses.size() > MAX_ORDER_BY_CLAUSES) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.TOO_MANY_CLAUSES,
                    "Query exceeds maximum of " + MAX_ORDER_BY_CLAUSES + " orderBy clauses");
        }
        
        // Check for cursor/orderBy consistency
        int cursorValueCount = 0;
        if (startAtValues != null) cursorValueCount = Math.max(cursorValueCount, startAtValues.length);
        if (startAfterValues != null) cursorValueCount = Math.max(cursorValueCount, startAfterValues.length);
        if (endAtValues != null) cursorValueCount = Math.max(cursorValueCount, endAtValues.length);
        if (endBeforeValues != null) cursorValueCount = Math.max(cursorValueCount, endBeforeValues.length);
        
        if (cursorValueCount > 0 && cursorValueCount > orderByClauses.size()) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.VALIDATION_ERROR,
                    "Number of cursor values (" + cursorValueCount +
                     ") exceeds number of orderBy clauses (" + orderByClauses.size() + ")");
        }
        
        // Check for inequality+orderBy consistency
        Map<String, String> inequalityFields = new HashMap<>();
        for (WhereClause whereClause : whereClauses) {
            if (whereClause.operator.equals(">") || 
                whereClause.operator.equals(">=") || 
                whereClause.operator.equals("<") || 
                whereClause.operator.equals("<=")) {
                
                inequalityFields.put(whereClause.field, whereClause.operator);
            }
        }
        
        if (!inequalityFields.isEmpty() && !orderByClauses.isEmpty()) {
            String firstOrderByField = orderByClauses.get(0).field;
            if (inequalityFields.containsKey(firstOrderByField)) {
                // This is good - first orderBy matches inequality field
            } else if (!inequalityFields.isEmpty()) {
                // Find the first inequality field
                String firstInequalityField = new ArrayList<>(inequalityFields.keySet()).get(0);
                String operator = inequalityFields.get(firstInequalityField);
                
                throw new FirestoreQueryException(
                        FirestoreQueryException.QueryErrorType.VALIDATION_ERROR,
                        "The first orderBy field must match the inequality field '" + 
                        firstInequalityField + "' with operator '" + operator + "'",
                        firstInequalityField);
            }
        }
    }
    
    /**
     * Validate a field name
     *
     * @param field Field name to validate
     */
    private void validateField(String field) {
        if (field == null || field.trim().isEmpty()) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.INVALID_FIELD,
                    "Field name cannot be null or empty");
        }
    }
    
    /**
     * Validate a FieldPath
     *
     * @param fieldPath FieldPath to validate
     */
    private void validateFieldPath(FieldPath fieldPath) {
        if (fieldPath == null) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.INVALID_FIELD,
                    "FieldPath cannot be null");
        }
    }
    
    /**
     * Validate that a value is appropriate for a field
     *
     * @param field Field name
     * @param value Value to validate
     */
    private void validateValueForField(String field, Object value) {
        // Note: We can't fully validate types since we don't have schema information,
        // but we can perform basic validation on common field names
        
        // Timestamp validation for date fields
        if ((field.toLowerCase().contains("date") || 
             field.toLowerCase().contains("time") || 
             field.toLowerCase().contains("timestamp")) && 
            value != null && 
            !(value instanceof Date) && 
            !(value instanceof com.google.firebase.Timestamp)) {
            
            Log.w(TAG, "Warning: Field '" + field + "' appears to be a date/time field but value is not a Date or Timestamp");
        }
        
        // Boolean validation
        if ((field.toLowerCase().startsWith("is") || 
             field.toLowerCase().startsWith("has") || 
             field.toLowerCase().endsWith("flag")) && 
            value != null && 
            !(value instanceof Boolean)) {
            
            Log.w(TAG, "Warning: Field '" + field + "' appears to be a boolean field but value is not a Boolean");
        }
    }
    
    /**
     * Validate that a value is appropriate for a FieldPath
     *
     * @param fieldPath FieldPath to validate against
     * @param value Value to validate
     */
    private void validateValueForFieldPath(FieldPath fieldPath, Object value) {
        // We can't perform specific validation for FieldPath since we don't know the field name
        // But we can check for null
        if (value == null) {
            // Null is a valid value for equality/inequality checks
            return;
        }
    }
    
    /**
     * Validate an array for whereIn, whereArrayContainsAny, and whereNotIn
     *
     * @param values Array values to validate
     * @param methodName Method name for error reporting
     */
    private void validateArray(List<?> values, String methodName) {
        if (values == null) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.INVALID_VALUE_TYPE,
                    "Value for " + methodName + " cannot be null");
        }
        
        if (values.isEmpty()) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.INVALID_VALUE_TYPE,
                    "Value for " + methodName + " cannot be an empty array");
        }
        
        if (values.size() > 10) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.INVALID_VALUE_TYPE,
                    methodName + " supports a maximum of 10 items, but got " + values.size() + " items");
        }
        
        // Check for null elements
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) == null) {
                throw new FirestoreQueryException(
                        FirestoreQueryException.QueryErrorType.INVALID_VALUE_TYPE,
                        methodName + " does not support null values in the array (at index " + i + ")");
            }
        }
    }
    
    /**
     * Validate an orderBy clause to prevent duplicates
     *
     * @param field Field name to validate
     */
    private void validateOrderBy(String field) {
        if (hasOrderByForField.containsKey(field) && hasOrderByForField.get(field)) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.DUPLICATE_ORDER_BY,
                    "The field '" + field + "' has already been specified in an orderBy clause",
                    field);
        }
    }
    
    /**
     * Validate a limit value
     *
     * @param limit Limit value to validate
     */
    private void validateLimit(int limit) {
        if (limit <= 0) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.VALIDATION_ERROR,
                    "Limit must be greater than 0, but got " + limit);
        }
        
        if (limit > MAX_LIMIT) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.LIMIT_EXCEEDED,
                    "Limit exceeds maximum of " + MAX_LIMIT + ", but got " + limit);
        }
    }
    
    /**
     * Validate cursor values for pagination methods
     *
     * @param values Cursor values to validate
     * @param methodName Method name for error reporting
     */
    private void validateCursorValues(Object[] values, String methodName) {
        if (values == null || values.length == 0) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.VALIDATION_ERROR,
                    methodName + " requires at least one value");
        }
        
        if (orderByClauses.isEmpty()) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.VALIDATION_ERROR,
                    methodName + " requires at least one orderBy clause");
        }
        
        if (values.length > orderByClauses.size()) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.VALIDATION_ERROR,
                    methodName + " has more values (" + values.length +
                     ") than orderBy clauses (" + orderByClauses.size() + ")");
        }
    }
    
    /**
     * Validate a date range
     *
     * @param startDate Start date to validate
     * @param endDate End date to validate
     */
    private void validateDateRange(Date startDate, Date endDate) {
        if (startDate == null) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.VALIDATION_ERROR,
                    "Start date cannot be null");
        }
        
        if (endDate == null) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.VALIDATION_ERROR,
                    "End date cannot be null");
        }
        
        if (startDate.after(endDate)) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.VALIDATION_ERROR,
                    "Start date cannot be after end date");
        }
    }
    
    /**
     * Validate a number range
     *
     * @param min Minimum value to validate
     * @param max Maximum value to validate
     */
    private void validateNumberRange(Number min, Number max) {
        if (min == null) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.VALIDATION_ERROR,
                    "Minimum value cannot be null");
        }
        
        if (max == null) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.VALIDATION_ERROR,
                    "Maximum value cannot be null");
        }
        
        if (min.doubleValue() >= max.doubleValue()) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.VALIDATION_ERROR,
                    "Minimum value must be less than maximum value");
        }
    }
    
    /**
     * Validate a string value
     *
     * @param value String to validate
     * @param paramName Parameter name for error reporting
     */
    private void validateString(String value, String paramName) {
        if (value == null || value.trim().isEmpty()) {
            throw new FirestoreQueryException(
                    FirestoreQueryException.QueryErrorType.VALIDATION_ERROR,
                    paramName + " cannot be null or empty");
        }
    }
}
