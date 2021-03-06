package ca.ualberta.boost;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import ca.ualberta.boost.models.Ride;

/**
 * RideRequestSummaryFragment defines a fragment that displays
 * a preliminary summary for a Ride request, showing the Ride's start location,
 * end location, and cost. The Rider requests a Ride using this fragment.
 */
public class RideRequestSummaryFragment extends DialogFragment{
    private OnFragmentInteractionListener listener;
    private Ride ride;
    private TextView fareText;

    /**
     * RideRequestSummaryFragment constructor
     * @param ride
     *      ride request to get and display a summary of
     */
    RideRequestSummaryFragment(Ride ride){
        this.ride = ride;
    }

    /**
     * Interface that enforces the implementing class to handle
     * what happens when the fragment's
     * positive button (accept) is pressed
     */
    public interface OnFragmentInteractionListener {
        void onAcceptPressed();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener){
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + "must implement OnFragmentInteractionListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_ride_request_summary, null);
        fareText = view.findViewById(R.id.fareText);

        String amount = "$" + ride.getFare();
        fareText.setText(amount);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // building the dialog
        return builder
                .setView(view)
                .setNegativeButton("Cancel", null) // null -> does nothing
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // send ride request in parent activity
                        listener.onAcceptPressed();
                    }
                }).create();
    }
}