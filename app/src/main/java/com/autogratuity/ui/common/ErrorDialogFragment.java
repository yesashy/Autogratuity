package com.autogratuity.ui.common;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.autogratuity.R;
import com.autogratuity.data.model.ErrorInfo;

import java.util.Objects;

/**
 * A standardized error dialog that provides consistent error presentation across the app.
 * <p>
 * This dialog supports:
 * - Different error types with appropriate icons
 * - Primary and secondary actions
 * - Detailed error information for developers
 * - Retry functionality
 * <p>
 * Usage:
 * <pre>
 * ErrorDialogFragment.builder()
 *     .setTitle("Connection Error")
 *     .setMessage("Unable to connect to server")
 *     .setErrorType(ErrorDialogFragment.ERROR_TYPE_NETWORK)
 *     .setPrimaryButton("Retry", () -> { /* retry action */ })
 *     .setSecondaryButton("Cancel", null)
 *     .setDismissCallback(() -> { /* dismissed callback */ })
 *     .show(getChildFragmentManager(), "error_dialog");
 * </pre>
 */
public class ErrorDialogFragment extends DialogFragment {

    // Error type constants
    public static final int ERROR_TYPE_GENERIC = 0;
    public static final int ERROR_TYPE_NETWORK = 1;
    public static final int ERROR_TYPE_SERVER = 2;
    public static final int ERROR_TYPE_AUTH = 3;
    public static final int ERROR_TYPE_VALIDATION = 4;
    public static final int ERROR_TYPE_PERMISSION = 5;

    // Bundle keys
    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_ERROR_TYPE = "error_type";
    private static final String ARG_ERROR_CODE = "error_code";
    private static final String ARG_ERROR_DETAILS = "error_details";
    private static final String ARG_PRIMARY_BUTTON_TEXT = "primary_button_text";
    private static final String ARG_SECONDARY_BUTTON_TEXT = "secondary_button_text";
    private static final String ARG_IS_CANCELLABLE = "is_cancellable";
    private static final String ARG_SHOW_DETAILS = "show_details";

    // Callback interfaces
    public interface ActionCallback {
        void onAction();
    }

    // Static callback references (not stored in bundle)
    private static ActionCallback sPrimaryCallback;
    private static ActionCallback sSecondaryCallback;
    private static ActionCallback sDismissCallback;

    /**
     * Builder for creating ErrorDialogFragment instances
     */
    public static class Builder {
        private final Bundle args = new Bundle();
        private ActionCallback primaryCallback;
        private ActionCallback secondaryCallback;
        private ActionCallback dismissCallback;

        public Builder() {
            // Set defaults
            args.putInt(ARG_ERROR_TYPE, ERROR_TYPE_GENERIC);
            args.putBoolean(ARG_IS_CANCELLABLE, true);
            args.putBoolean(ARG_SHOW_DETAILS, false);
        }

        /**
         * Set the dialog title
         */
        public Builder setTitle(String title) {
            args.putString(ARG_TITLE, title);
            return this;
        }

        /**
         * Set the dialog message
         */
        public Builder setMessage(String message) {
            args.putString(ARG_MESSAGE, message);
            return this;
        }

        /**
         * Set the error type which determines the icon and styling
         */
        public Builder setErrorType(int errorType) {
            args.putInt(ARG_ERROR_TYPE, errorType);
            return this;
        }

        /**
         * Set error details from an ErrorInfo object
         */
        public Builder setErrorInfo(ErrorInfo errorInfo) {
            if (errorInfo == null) {
                return this;
            }

            // Set title and message if not already set
            if (!args.containsKey(ARG_TITLE) || args.getString(ARG_TITLE) == null) {
                String title = getDefaultTitleForErrorInfo(errorInfo);
                args.putString(ARG_TITLE, title);
            }

            if (!args.containsKey(ARG_MESSAGE) || args.getString(ARG_MESSAGE) == null) {
                args.putString(ARG_MESSAGE, errorInfo.getMessage());
            }

            // Set error type based on error code
            if (!args.containsKey(ARG_ERROR_TYPE) || args.getInt(ARG_ERROR_TYPE) == ERROR_TYPE_GENERIC) {
                args.putInt(ARG_ERROR_TYPE, getErrorTypeFromErrorInfo(errorInfo));
            }

            // Set error code and details
            args.putString(ARG_ERROR_CODE, errorInfo.getCode());
            args.putString(ARG_ERROR_DETAILS, errorInfo.getDetailsForLogging());

            return this;
        }

        /**
         * Set error details from an Exception
         */
        public Builder setError(Throwable error) {
            if (error == null) {
                return this;
            }

            // Set title and message if not already set
            if (!args.containsKey(ARG_TITLE) || args.getString(ARG_TITLE) == null) {
                args.putString(ARG_TITLE, "Error");
            }

            if (!args.containsKey(ARG_MESSAGE) || args.getString(ARG_MESSAGE) == null) {
                args.putString(ARG_MESSAGE, error.getMessage());
            }

            // Set error type based on exception type
            if (!args.containsKey(ARG_ERROR_TYPE) || args.getInt(ARG_ERROR_TYPE) == ERROR_TYPE_GENERIC) {
                args.putInt(ARG_ERROR_TYPE, getErrorTypeFromException(error));
            }

            // Set error code and details
            args.putString(ARG_ERROR_CODE, error.getClass().getSimpleName());
            args.putString(ARG_ERROR_DETAILS, getStackTraceAsString(error));

            return this;
        }

        /**
         * Set the primary button text and callback
         */
        public Builder setPrimaryButton(String text, ActionCallback callback) {
            args.putString(ARG_PRIMARY_BUTTON_TEXT, text);
            this.primaryCallback = callback;
            return this;
        }

        /**
         * Set the secondary button text and callback
         */
        public Builder setSecondaryButton(String text, ActionCallback callback) {
            args.putString(ARG_SECONDARY_BUTTON_TEXT, text);
            this.secondaryCallback = callback;
            return this;
        }

        /**
         * Set whether the dialog can be cancelled by pressing back or touching outside
         */
        public Builder setCancellable(boolean cancellable) {
            args.putBoolean(ARG_IS_CANCELLABLE, cancellable);
            return this;
        }

        /**
         * Set a callback for when the dialog is dismissed
         */
        public Builder setDismissCallback(ActionCallback callback) {
            this.dismissCallback = callback;
            return this;
        }

        /**
         * Show or hide the error details section
         */
        public Builder showDetails(boolean showDetails) {
            args.putBoolean(ARG_SHOW_DETAILS, showDetails);
            return this;
        }

        /**
         * Create the dialog fragment
         */
        public ErrorDialogFragment create() {
            ErrorDialogFragment fragment = new ErrorDialogFragment();
            fragment.setArguments(args);
            sPrimaryCallback = primaryCallback;
            sSecondaryCallback = secondaryCallback;
            sDismissCallback = dismissCallback;
            return fragment;
        }

        /**
         * Create and show the dialog
         */
        public void show(FragmentManager manager, String tag) {
            create().show(manager, tag);
        }
    }

    /**
     * Create a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);
        
        // Inflate custom view
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_error, null);
        builder.setView(dialogView);
        
        // Get arguments
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        
        // Configure dialog
        setCancelable(args.getBoolean(ARG_IS_CANCELLABLE, true));
        
        // Set up UI components
        TextView titleTextView = dialogView.findViewById(R.id.error_title);
        TextView messageTextView = dialogView.findViewById(R.id.error_message);
        ImageView iconImageView = dialogView.findViewById(R.id.error_icon);
        TextView errorDetailsTextView = dialogView.findViewById(R.id.error_details);
        View detailsContainer = dialogView.findViewById(R.id.error_details_container);
        
        // Set title and message
        String title = args.getString(ARG_TITLE, "Error");
        String message = args.getString(ARG_MESSAGE, "An unknown error occurred");
        titleTextView.setText(title);
        messageTextView.setText(message);
        
        // Set icon based on error type
        int errorType = args.getInt(ARG_ERROR_TYPE, ERROR_TYPE_GENERIC);
        iconImageView.setImageResource(getIconForErrorType(errorType));
        
        // Set error details if available
        String errorDetails = args.getString(ARG_ERROR_DETAILS);
        if (errorDetails != null && args.getBoolean(ARG_SHOW_DETAILS, false)) {
            errorDetailsTextView.setText(errorDetails);
            detailsContainer.setVisibility(View.VISIBLE);
        } else {
            detailsContainer.setVisibility(View.GONE);
        }
        
        // Build dialog with custom button handling
        AlertDialog dialog = builder.create();
        
        // Show dialog before customizing buttons
        dialog.show();
        
        // Customize primary button
        String primaryButtonText = args.getString(ARG_PRIMARY_BUTTON_TEXT);
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (primaryButtonText != null) {
            positiveButton.setText(primaryButtonText);
            positiveButton.setOnClickListener(v -> {
                if (sPrimaryCallback != null) {
                    sPrimaryCallback.onAction();
                }
                dialog.dismiss();
            });
        } else {
            positiveButton.setVisibility(View.GONE);
        }
        
        // Customize secondary button
        String secondaryButtonText = args.getString(ARG_SECONDARY_BUTTON_TEXT);
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (secondaryButtonText != null) {
            negativeButton.setText(secondaryButtonText);
            negativeButton.setOnClickListener(v -> {
                if (sSecondaryCallback != null) {
                    sSecondaryCallback.onAction();
                }
                dialog.dismiss();
            });
        } else {
            negativeButton.setVisibility(View.GONE);
        }
        
        return dialog;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (sDismissCallback != null) {
            sDismissCallback.onAction();
        }
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        // Clear static callbacks to prevent memory leaks
        sPrimaryCallback = null;
        sSecondaryCallback = null;
        sDismissCallback = null;
    }

    /**
     * Get the default icon resource for an error type
     */
    @DrawableRes
    private static int getIconForErrorType(int errorType) {
        switch (errorType) {
            case ERROR_TYPE_NETWORK:
                return R.drawable.ic_error_network;
            case ERROR_TYPE_SERVER:
                return R.drawable.ic_error_server;
            case ERROR_TYPE_AUTH:
                return R.drawable.ic_error_auth;
            case ERROR_TYPE_VALIDATION:
                return R.drawable.ic_error_validation;
            case ERROR_TYPE_PERMISSION:
                return R.drawable.ic_error_permission;
            case ERROR_TYPE_GENERIC:
            default:
                return R.drawable.ic_error;
        }
    }

    /**
     * Get the default title for an ErrorInfo
     */
    private static String getDefaultTitleForErrorInfo(ErrorInfo errorInfo) {
        String code = errorInfo.getCode();
        if (code == null) {
            return "Error";
        }
        
        if (code.contains("NETWORK") || code.equals("OFFLINE")) {
            return "Network Error";
        } else if (code.contains("SERVER") || code.contains("INTERNAL")) {
            return "Server Error";
        } else if (code.contains("AUTH") || code.contains("UNAUTHENTICATED")) {
            return "Authentication Error";
        } else if (code.contains("PERMISSION") || code.contains("DENIED")) {
            return "Permission Error";
        } else if (code.contains("VALIDATION") || code.contains("INVALID")) {
            return "Validation Error";
        } else {
            return "Error";
        }
    }

    /**
     * Determine error type from ErrorInfo
     */
    private static int getErrorTypeFromErrorInfo(ErrorInfo errorInfo) {
        String code = errorInfo.getCode();
        if (code == null) {
            return ERROR_TYPE_GENERIC;
        }
        
        if (code.contains("NETWORK") || code.equals("OFFLINE")) {
            return ERROR_TYPE_NETWORK;
        } else if (code.contains("SERVER") || code.contains("INTERNAL")) {
            return ERROR_TYPE_SERVER;
        } else if (code.contains("AUTH") || code.contains("UNAUTHENTICATED")) {
            return ERROR_TYPE_AUTH;
        } else if (code.contains("PERMISSION") || code.contains("DENIED")) {
            return ERROR_TYPE_PERMISSION;
        } else if (code.contains("VALIDATION") || code.contains("INVALID")) {
            return ERROR_TYPE_VALIDATION;
        } else {
            return ERROR_TYPE_GENERIC;
        }
    }

    /**
     * Determine error type from Exception
     */
    private static int getErrorTypeFromException(Throwable error) {
        if (error == null) {
            return ERROR_TYPE_GENERIC;
        }
        
        String className = error.getClass().getSimpleName();
        String message = error.getMessage();
        
        // Network related errors
        if (className.contains("SocketTimeout") || 
            className.contains("ConnectException") ||
            className.contains("UnknownHost") ||
            className.contains("Network") ||
            (message != null && message.contains("network"))) {
            return ERROR_TYPE_NETWORK;
        }
        
        // Server errors
        else if (className.contains("Server") ||
                (message != null && (message.contains("server") || message.contains("500")))) {
            return ERROR_TYPE_SERVER;
        }
        
        // Authentication errors
        else if (className.contains("Auth") ||
                className.contains("Login") ||
                className.contains("Credentials") ||
                (message != null && (message.contains("auth") || message.contains("login")))) {
            return ERROR_TYPE_AUTH;
        }
        
        // Permission errors
        else if (className.contains("Permission") ||
                (message != null && message.contains("permission"))) {
            return ERROR_TYPE_PERMISSION;
        }
        
        // Validation errors
        else if (className.contains("Validation") ||
                className.contains("Invalid") ||
                (message != null && (message.contains("validation") || message.contains("invalid")))) {
            return ERROR_TYPE_VALIDATION;
        }
        
        // Default
        else {
            return ERROR_TYPE_GENERIC;
        }
    }

    /**
     * Convert stack trace to string for detailed error display
     */
    private static String getStackTraceAsString(Throwable error) {
        if (error == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(error.getClass().getName()).append(": ").append(error.getMessage()).append("\n");
        
        for (StackTraceElement element : error.getStackTrace()) {
            sb.append("    at ").append(element.toString()).append("\n");
            // Limit stack trace length to prevent excessive text
            if (sb.length() > 1000) {
                sb.append("    ... (truncated)");
                break;
            }
        }
        
        return sb.toString();
    }
}