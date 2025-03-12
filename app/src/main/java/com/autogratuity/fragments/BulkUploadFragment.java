package com.autogratuity.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.autogratuity.R;
import com.autogratuity.utils.KmlImportUtil;

public class BulkUploadFragment extends Fragment {
    private static final String TAG = "BulkUploadFragment";
    private static final int REQUEST_KML_KMZ_FILE = 123;

    private Button uploadButton;

    public static BulkUploadFragment newInstance() {
        return new BulkUploadFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bulk_upload, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        uploadButton = view.findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(v -> importFromGoogleMaps());
    }

    private void importFromGoogleMaps() {
        // Create intent to select KML/KMZ file
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"application/vnd.google-earth.kml+xml", "application/vnd.google-earth.kmz", "application/xml", "text/xml"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, REQUEST_KML_KMZ_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_KML_KMZ_FILE && resultCode == getActivity().RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri fileUri = data.getData();

                // Get persistent permission to access this file
                try {
                    getActivity().getContentResolver().takePersistableUriPermission(
                            fileUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                } catch (SecurityException e) {
                    Log.e(TAG, "Could not take persistable URI permission", e);
                    // Continue anyway as we might not need persistent access
                }

                // Show confirmation dialog before importing
                new AlertDialog.Builder(getActivity())
                        .setTitle("Import from Google Maps")
                        .setMessage("Import delivery data from this KML/KMZ file? This will add locations to your Autogratuity database.")
                        .setPositiveButton("Import", (dialog, which) -> {
                            // Parse the KML/KMZ file
                            KmlImportUtil importUtil = new KmlImportUtil(getActivity());
                            boolean success = importUtil.importFromKmlKmz(fileUri);

                            if (success) {
                                Toast.makeText(getActivity(), "Started importing data from Google Maps", Toast.LENGTH_SHORT).show();
                                // Refresh UI after a delay to allow import to start
                                new Handler().postDelayed(() -> {
                                    // Could add refresh logic here if needed
                                }, 2000);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }
}