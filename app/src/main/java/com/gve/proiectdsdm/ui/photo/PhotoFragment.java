package com.gve.proiectdsdm.ui.photo;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.gve.proiectdsdm.R;
import com.gve.proiectdsdm.databinding.FragmentPhotoBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class PhotoFragment extends Fragment {

    private FragmentPhotoBinding binding;
    private String currentPhotoPath, currentPhotoName;
    ActivityResultLauncher<Intent> mTakePhoto;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        this.binding = FragmentPhotoBinding.inflate(inflater, container, false);
        this.mTakePhoto = null;
        View root = binding.getRoot();
        initPhotoUI();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    private void changeUIVisibility (Button button, ImageView imageView, VideoView videoView, boolean isVisible) {
        if (isVisible) {
            imageView.setVisibility(View.INVISIBLE);
            button.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.VISIBLE);
            requireActivity().findViewById(R.id.nav_view).setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.VISIBLE);
            button.setVisibility(View.INVISIBLE);
            videoView.setVisibility(View.INVISIBLE);
            videoView.stopPlayback();
            requireActivity().findViewById(R.id.nav_view).setVisibility(View.INVISIBLE);
        }
        button.setEnabled(isVisible);
        requireActivity().findViewById(R.id.nav_view).setEnabled(isVisible);
    }

    private File createPhotoFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        String photoFileName = "DSDM_" + timeStamp;
        File storageDir = super.requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File photo = File.createTempFile(photoFileName, ".jpg", storageDir);
        currentPhotoName = photoFileName;
        currentPhotoPath = photo.getAbsolutePath();
        return photo;
    }

    private void photoFunction(Button photoBtn, ImageView photoImage, VideoView photoVideoView, ActivityResultLauncher<Intent> mTakePhoto){
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createPhotoFile();
        } catch (IOException ioException) {
            Toast.makeText(super.requireActivity().getApplicationContext(), "Could not create photo!", Toast.LENGTH_SHORT).show();
            changeUIVisibility(photoBtn, photoImage, photoVideoView, true);
        }
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(super.requireContext(), "com.gve.proiectdsdm.provider", photoFile);
            photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            mTakePhoto.launch(photoIntent);
        }
    }

    private void initPhotoUI(){

        //region Video
        final VideoView photoVideoView = binding.photoVideo;
        String videoPath = "android.resource://" + requireActivity().getPackageName() + "/" + R.raw.video;
        Uri videoUri = Uri.parse(videoPath);
        photoVideoView.setVideoURI(videoUri);
        photoVideoView.setOnClickListener(v -> {
            if (photoVideoView.isPlaying()) {
                photoVideoView.pause();
            } else {
                photoVideoView.start();
            }
        });
        //endregion

        //region Photo Button
        final ImageView photoImage = binding.photoImage;
        photoImage.setVisibility(View.INVISIBLE);
        final Handler handler = new Handler();
        final Button photoBtn = binding.photoBtn;
        final ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(photoImage, "scaleX", 10f);
        scaleUpX.setDuration(500);
        final ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(photoImage, "scaleY", 10f);
        scaleUpY.setDuration(500);
        final AnimatorSet scaleUp = new AnimatorSet();
        final ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(photoImage, "scaleX", 1f);
        scaleDownX.setDuration(500);
        final ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(photoImage, "scaleY", 1f);
        scaleDownY.setDuration(500);
        final AnimatorSet scaleDown = new AnimatorSet();
        if (mTakePhoto == null) {
            mTakePhoto = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                try {
                    MediaStore.Images.Media.insertImage(super.requireActivity().getContentResolver(), currentPhotoPath, currentPhotoName, null);
                    if (new File(currentPhotoPath).delete()){
                        Toast.makeText(super.requireActivity().getApplicationContext(), "Photo saved to gallery!", Toast.LENGTH_SHORT).show();
                        changeUIVisibility(photoBtn, photoImage, photoVideoView, true);
                    }
                } catch (FileNotFoundException e) {
                    if (new File(currentPhotoPath).delete()){
                        Toast.makeText(super.requireActivity().getApplicationContext(), "Photo cancelled!", Toast.LENGTH_SHORT).show();
                        changeUIVisibility(photoBtn, photoImage, photoVideoView, true);
                    }
                }
            });
        }
        photoBtn.setOnClickListener(v -> {
            changeUIVisibility(photoBtn, photoImage, photoVideoView, false);
            scaleUp.play(scaleUpX).with(scaleUpY);
            scaleUp.start();
            handler.postDelayed(() -> {
                scaleDown.play(scaleDownX).with(scaleDownY);
                scaleDown.start();
            }, 500);
            handler.postDelayed(() -> photoFunction(photoBtn, photoImage, photoVideoView, mTakePhoto), 750);
        });
        //endregion
    }

}