package com.mycompany.android.imageclassifier;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.glomadrian.dashedcircularprogress.DashedCircularProgress;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mycompany.android.imageclassifier.ViewModel.AddClassifierViewModel;
import com.mycompany.android.imageclassifier.ViewModel.AddClassifierViewModelFactory;
import com.mycompany.android.imageclassifier.database.AppDatabase;
import com.mycompany.android.imageclassifier.database.ImageEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by delaroy on 9/8/18.
 */

public class DetailActivity extends AppCompatActivity  {

    public static final String EXTRA_IMAGE_ID = "extraImageId";
    public static final String INSTANCE_IMAGE_ID = "instanceImageId";
    private static final int DEFAULT_IMAGE_ID = -1;
    private int mImageId = DEFAULT_IMAGE_ID;
    private AppDatabase mDb;
    private String pred_class, melanoma, nevus, seborrheic;

    @BindView(R.id.classifier_image)
    ImageView classifier_image;

    @BindView(R.id.melanoma)
    TextView m_melanoma;

    @BindView(R.id.nevus)
    TextView m_nevus;

    @BindView(R.id.seborr)
    TextView m_seborr;

    @BindView(R.id.image_status)
    DashedCircularProgress image_status;

    @BindView(R.id.status)
    TextView status;

    @BindView(R.id.paymentImage)
    ImageView paymentImage;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        mDb = AppDatabase.getInstance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_IMAGE_ID)) {
            mImageId = savedInstanceState.getInt(INSTANCE_IMAGE_ID, DEFAULT_IMAGE_ID);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_IMAGE_ID)) {
            // populate the UI
            mImageId = intent.getIntExtra(EXTRA_IMAGE_ID, DEFAULT_IMAGE_ID);

            AddClassifierViewModelFactory factory = new AddClassifierViewModelFactory(mDb, mImageId);
            final AddClassifierViewModel viewModel
                    = ViewModelProviders.of(this, factory).get(AddClassifierViewModel.class);

            viewModel.getImageClassifier().observe(this, new Observer<ImageEntry>() {
                @Override
                public void onChanged(@Nullable ImageEntry taskEntry) {
                    viewModel.getImageClassifier().removeObserver(this);
                    populateUI(taskEntry);
                }
            });
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(INSTANCE_IMAGE_ID, mImageId);
        super.onSaveInstanceState(outState);
    }

    private void populateUI(ImageEntry imageentry) {
        if (imageentry == null) {
            return;
        }

        String image = imageentry.getImage();
        melanoma = imageentry.getMelanoma();
        nevus = imageentry.getNevus();
        pred_class = imageentry.getPredclass();
        seborrheic = imageentry.getSeborrheic();

        m_melanoma.setText(String.format("Melanoma: %s", melanoma));
        m_nevus.setText(String.format("Nevus: %s", nevus));
        m_seborr.setText(String.format("Seborrheic Keratosis: %s", seborrheic));

        if (pred_class.equals("melanoma")) {
            status.setText("Cancerous");
            status.setTextColor(getResources().getColor(R.color.bg_row_background));
            image_status.setValue(999);
            image_status.setProgressColor(getResources().getColor(R.color.bg_row_background));
            image_status.setExternalColor(getResources().getColor(R.color.white));
            paymentImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_cancel_black_24dp));

        } else {
            status.setText("Good");
            status.setTextColor(getResources().getColor(R.color.green));
            image_status.setValue(999);
            image_status.setProgressColor(getResources().getColor(R.color.green));
            image_status.setExternalColor(getResources().getColor(R.color.white));
            paymentImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_black_24dp));

        }
        Glide.with(this)
                .load(image)
                .into(classifier_image);
    }
}
