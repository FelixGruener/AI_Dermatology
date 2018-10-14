package com.mycompany.android.imageclassifier;

import android.Manifest;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.mycompany.android.imageclassifier.ViewModel.AppExecutors;
import com.mycompany.android.imageclassifier.ViewModel.MainViewModel;
import com.mycompany.android.imageclassifier.adapter.ClassifierAdapter;
import com.mycompany.android.imageclassifier.database.AppDatabase;
import com.mycompany.android.imageclassifier.database.ImageEntry;
import com.mycompany.android.imageclassifier.model.PredictionResponse;
import com.mycompany.android.imageclassifier.networking.Client;
import com.mycompany.android.imageclassifier.networking.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

import static android.widget.LinearLayout.VERTICAL;
import static com.mycompany.android.imageclassifier.utils.Constants.BASE_URL;

public class MainActivity extends AppCompatActivity implements ClassifierAdapter.ItemClickListener{
    @BindView(R.id.selectImage)
    ImageView selectImage;

    @BindView(R.id.recycler_view)
    RecyclerView recycler_view;

    @BindView(R.id.image_header)
    ImageView image_header;

    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_PICK_PHOTO = 2;
    private static final int CAMERA_PIC_REQUEST = 1111;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private Uri fileUri;
    private String mediaPath;
    private String mImageFileLocation = "";
    public static final String IMAGE_DIRECTORY_NAME = "Android File Upload";
    private String postPath = "";
    private static final String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog progressBar;
    private int progressBarStatus = 0;
    private Handler progressBarbHandler = new Handler();
    private AppDatabase mDb;
    private ClassifierAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        initpDialog();
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
                } else {
                    launchImagePicker();
                }
            }
        });
        mDb = AppDatabase.getInstance(getApplicationContext());

        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ClassifierAdapter(this, this);
        recycler_view.setAdapter(mAdapter);
        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        recycler_view.addItemDecoration(decoration);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                if (viewHolder instanceof ClassifierAdapter.ClassifierViewHolder) {
                    // Here is where you'll implement swipe to delete
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            int position = viewHolder.getAdapterPosition();
                            List<ImageEntry> imageEntries = mAdapter.getClassifier();
                            mDb.imageClassifierDao().deleteClassifier(imageEntries.get(position));
                        }
                    });
                    Toast.makeText(MainActivity.this, "item successfully deleted", Toast.LENGTH_SHORT).show();
                }
            }
        }).attachToRecyclerView(recycler_view);

        setupViewModel();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchImagePicker();
            }else{
                Toast.makeText(MainActivity.this, "Permission denied, the permissions are very important for the apps usage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.topbar, menu);;
        if (postPath.isEmpty()) {
            menu.findItem(R.id.upload).setVisible(Boolean.FALSE);
        }
       return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upload:
                if (postPath.isEmpty() || postPath == null){
                    Toast.makeText(this, "select a valid image for classifying", Toast.LENGTH_SHORT).show();
                }else {
                   // submitFile(postPath);
                    submitData();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getImageClassifier().observe(this, new Observer<List<ImageEntry>>() {
            @Override
            public void onChanged(@Nullable List<ImageEntry> imageEntries) {
                Log.d(TAG, "Updating list of tasks from LiveData in ViewModel");
                mAdapter.setTasks(imageEntries);
            }
        });
    }

    private void launchImagePicker(){
        new MaterialDialog.Builder(this)
            .title(R.string.uploadImages)
            .items(R.array.uploadImages)
            .itemsIds(R.array.itemIds)
            .itemsCallback(new MaterialDialog.ListCallback() {
                @Override
                public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                    switch (which) {
                        case 0:
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(galleryIntent, REQUEST_PICK_PHOTO);
                            break;
                        case 1:
                            captureImage();
                            break;
                        case 2:
                            image_header.setImageResource(R.color.colorPrimary);
                            postPath.equals("");
                            refreshActivity();
                            break;
                    }
                }
            })
                .show();
    }

    /**
     * Launching camera app to capture image
     */
    private void captureImage() {
        if (Build.VERSION.SDK_INT > 21) { //use this if Lollipop_Mr1 (API 22) or above
            Intent callCameraApplicationIntent = new Intent();
            callCameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

            // We give some instruction to the intent to save the image
            File photoFile = null;

            try {
                // If the createImageFile will be successful, the photo file will have the address of the file
                photoFile = createImageFile();
                // Here we call the function that will try to catch the exception made by the throw function
            } catch (IOException e) {
                Logger.getAnonymousLogger().info("Exception error in generating the file");
                e.printStackTrace();
            }
            // Here we add an extra file to the intent to put the address on to. For this purpose we use the FileProvider, declared in the AndroidManifest.
            Uri outputUri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    photoFile);
            callCameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

            // The following is a new line with a trying attempt
            callCameraApplicationIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Logger.getAnonymousLogger().info("Calling the camera App by intent");

            // The following strings calls the camera app and wait for his file in return.
            startActivityForResult(callCameraApplicationIntent, CAMERA_PIC_REQUEST);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            // start the image capture Intent
            startActivityForResult(intent, CAMERA_PIC_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO || requestCode == REQUEST_PICK_PHOTO) {
                if (data != null) {
                    setProgressBar();
                    // Get the Image from data
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    assert cursor != null;
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    mediaPath = cursor.getString(columnIndex);
                    // Set the Image in ImageView for Previewing the Media
                    image_header.setImageBitmap(BitmapFactory.decodeFile(mediaPath));
                    cursor.close();
                    postPath = mediaPath;
                    invalidateOptionsMenu();
                   // String image = uploadImage(postPath);
                    //Toast.makeText(this, "this is base64, " + image, Toast.LENGTH_SHORT).show();
                }
            }else if (requestCode == CAMERA_PIC_REQUEST){
                if (Build.VERSION.SDK_INT > 21) {
                    setProgressBar();
                    Glide.with(this).load(mImageFileLocation).into(image_header);
                    postPath = mImageFileLocation;
                    invalidateOptionsMenu();
                }else{
                    setProgressBar();
                    Glide.with(this).load(fileUri).into(image_header);
                    postPath = fileUri.getPath();
                    invalidateOptionsMenu();
                }
            }
        }
        else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "Sorry, there was an error!", Toast.LENGTH_LONG).show();
        }
    }

    File createImageFile() throws IOException {
        Logger.getAnonymousLogger().info("Generating the image - method started");

        // Here we create a "non-collision file name", alternatively said, "an unique filename" using the "timeStamp" functionality
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmSS").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp;
        // Here we specify the environment location and the exact path where we want to save the so-created file
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/photo_saving_app");
        Logger.getAnonymousLogger().info("Storage directory set");

        // Then we create the storage directory if does not exists
        if (!storageDirectory.exists()) storageDirectory.mkdir();

        // Here we create the file using a prefix, a suffix and a directory
        File image = new File(storageDirectory, imageFileName + ".jpg");
        // File image = File.createTempFile(imageFileName, ".jpg", storageDirectory);

        // Here the location is saved into the string mImageFileLocation
        Logger.getAnonymousLogger().info("File name and path set");

        mImageFileLocation = image.getAbsolutePath();
        fileUri = Uri.parse(mImageFileLocation);
        // The file is returned to the previous intent across the camera application
        return image;
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + ".jpg");
        }  else {
            return null;
        }

        return mediaFile;
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName) {

        File file = new File(postPath);

        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("*/*"),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    private void submitData() {
        showpDialog();
        Service service = Client.createService(Service.class, BASE_URL);
        Call<PredictionResponse> call = service.uploadImage(prepareFilePart("file"));

        call.enqueue(new Callback<PredictionResponse>() {
            @Override
            public void onResponse(Call<PredictionResponse> call, retrofit2.Response<PredictionResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        hidepDialog();
                        DecimalFormat df2 = new DecimalFormat("#.######");
                        PredictionResponse predictionResponse = response.body();
                        String predicted = predictionResponse.getPredictedClass();
                        Double melanoma = predictionResponse.getPredictionStats().getMelanoma();
                        Double nevus = predictionResponse.getPredictionStats().getNevus();
                        Double seborr = predictionResponse.getPredictionStats().getSeborrheicKeratosis();
                        final ImageEntry imageEntry = new ImageEntry(predicted, df2.format(melanoma), df2.format(nevus), df2.format(seborr), postPath);
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                mDb.imageClassifierDao().insertClassifier(imageEntry);
                            }
                        });
                        hidepDialog();
                        refreshLayout();
                        Toast.makeText(MainActivity.this, "image successfully classified and added to database", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "error uploading", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PredictionResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "error uploading ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshLayout(){
        image_header.setImageResource(R.color.colorPrimary);
        postPath.equals("");
        refreshActivity();
    }

    private void refreshActivity(){
        if (Build.VERSION.SDK_INT >= 11) {
            recreate();
        } else {
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            overridePendingTransition(0, 0);

            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    public void setProgressBar(){
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("Please wait...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();
        progressBarStatus = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (progressBarStatus < 100){
                    progressBarStatus += 30;

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    progressBarbHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(progressBarStatus);
                        }
                    });
                }
                if (progressBarStatus >= 100) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progressBar.dismiss();
                }

            }
        }).start();
    }

    protected void initpDialog() {

        progressBar = new ProgressDialog(this);
        progressBar.setMessage(getString(R.string.msg_loading));
        progressBar.setCancelable(true);
    }

    protected void showpDialog() {

        if (!progressBar.isShowing()) progressBar.show();
    }

    protected void hidepDialog() {

        if (progressBar.isShowing()) progressBar.dismiss();
    }

    @Override
    public void onItemClickListener(int itemId) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_IMAGE_ID, itemId);
        startActivity(intent);
    }
}
