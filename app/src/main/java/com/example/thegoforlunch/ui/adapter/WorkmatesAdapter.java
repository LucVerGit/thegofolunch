package com.example.thegoforlunch.ui.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.example.thegoforlunch.R;
import com.example.thegoforlunch.databinding.CellWorkmatesBinding;
import com.example.thegoforlunch.model.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class WorkmatesAdapter extends FirestoreRecyclerAdapter<User, WorkmatesAdapter.WorkmateViewHolder> {


    // interface
    public interface OnWorkmateClickListener {
        void OnWorkmateClick(String placeId, String userName);
    }


    // private static
    private final OnWorkmateClickListener mListener;


    // variables
    private final RequestManager glide;


    // constructor
    public WorkmatesAdapter(@NonNull FirestoreRecyclerOptions<User> options, RequestManager glide, OnWorkmateClickListener listener) {
        super(options);
        this.glide = glide;
        this.mListener = listener;
    }

    // inherited methods
    @NonNull
    @Override
    public WorkmateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WorkmateViewHolder(CellWorkmatesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), mListener);
    }

    @Override
    protected void onBindViewHolder(@NonNull WorkmateViewHolder holder, int position, @NonNull User model) {
        holder.bindWithUserDetails(model, glide);
    }

    // view holder
    public static class WorkmateViewHolder extends RecyclerView.ViewHolder implements ViewGroup.OnClickListener {

        private final CellWorkmatesBinding mBinding;
        private final OnWorkmateClickListener mListener;
        private User mUser;

        public WorkmateViewHolder(CellWorkmatesBinding binding, OnWorkmateClickListener listener) {
            super(binding.getRoot());
            this.mBinding = binding;
            this.mListener = listener;
            this.mUser = null;
            binding.getRoot().setOnClickListener(this);
        }

        public void bindWithUserDetails(User user, RequestManager glide) {
            mUser = user;
            glide.load(user.getUrlPicture())
                    .circleCrop()
                    .into(mBinding.cellWorkmatesProfilePicture);

            String firstName = user.getName().split(" ")[0];
            String stringHasDecided = mBinding
                    .getRoot()
                    .getContext()
                    .getResources()
                    .getString(R.string.has_decided, firstName, user.getSelectedRestaurantName());
            String stringHasNotDecided = mBinding
                    .getRoot()
                    .getContext()
                    .getResources()
                    .getString(R.string.has_not_decided, firstName);

            if (user.getSelectedRestaurantId() != null) {
                mBinding.cellWorkmatesTextView.setText(stringHasDecided);
                mBinding.cellWorkmatesTextView.setTextAppearance(
                        mBinding.getRoot().getContext(),
                        R.style.TextHasDecided);
            } else {
                mBinding.cellWorkmatesTextView.setText(stringHasNotDecided);
                mBinding.cellWorkmatesTextView.setTextAppearance(
                        mBinding.getRoot().getContext(),
                        R.style.TextHasNotDecided);
            }
        }

        @Override
        public void onClick(View v) {
            mListener.OnWorkmateClick(mUser.getSelectedRestaurantId(), mUser.getName());
        }
    }
}
