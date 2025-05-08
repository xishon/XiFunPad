package com.app.zee.xifunpad;

import static com.app.zee.xifunpad.Constants.Constants.*;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.zee.xifunpad.Enum.Brush;
import com.app.zee.xifunpad.Storage.SharedPreferencesHelper;
import com.app.zee.xifunpad.Utils.BitmapUtils;
import com.github.dhaval2404.colorpicker.ColorPickerDialog;
import com.github.dhaval2404.colorpicker.listener.ColorListener;
import com.github.dhaval2404.colorpicker.model.ColorShape;
import com.raed.rasmview.RasmContext;
import com.raed.rasmview.RasmView;
import com.raed.rasmview.brushtool.data.BrushesRepository;
import com.raed.rasmview.brushtool.model.BrushConfig;
import com.raed.rasmview.state.RasmState;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SketchPad extends AppCompatActivity {

    RasmView rasmView;
    RasmContext rasmContext;
    RasmState rasmState;
    Button clear, save, reset, brushColor, bgColor;
    ImageView undo, redo, imgOptions;

    LinearLayout optionsSection;

    SeekBar size, flow, opacity;

    Spinner penType;

    boolean optionsopen = false;

    SharedPreferencesHelper preferencesHelper;

    // pen name
    String brushName = "pen";

    // size, flow and opacity
    float sizeF, flowF, opacityF;

    // Float range and precision
    private final float minValue = 0.0f;  // Minimum float value
    private final float maxValue = 5.0f;  // Maximum float value
    private final int seekBarMax = 100;   // 100 steps for precision to 0.01

    int brush_color = 0, bg_color = 0;

    Resources resources;
    BrushesRepository brushesRepository;
    com.raed.rasmview.brushtool.data.Brush realBrush;

    // Assuming 'bitmap' is your Bitmap object
    Bitmap bitmap = null;
    String fileName = "my_image";  // Name of the file without extension

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sketch_pad);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Enable Dark Mode based on system settings
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);


//        Toast.makeText(this, "Draw!", Toast.LENGTH_SHORT).show();

        initialize();

        clicklisteners();
    }

    private void initialize() {
        rasmView = findViewById(R.id.rasmView);
        rasmContext = rasmView.getRasmContext();

        clear = findViewById(R.id.button_clear);
        undo = findViewById(R.id.undo);
        redo = findViewById(R.id.redo);

        optionsSection = findViewById(R.id.ll_collapse);
        size = findViewById(R.id.sb_size);
        flow = findViewById(R.id.sb_flow);
        opacity = findViewById(R.id.sb_opacity);
        penType = findViewById(R.id.sp_pen);
        imgOptions = findViewById(R.id.img_options);
        save = findViewById(R.id.button_save);
        reset = findViewById(R.id.button_reset);
        bgColor = findViewById(R.id.button_bg);
        brushColor = findViewById(R.id.button_brush_color);

        ArrayAdapter<Brush> adapter = (new ArrayAdapter<Brush>(this, R.layout.spinner_item, Brush.values()));
        penType.setAdapter(adapter);

        // Set the maximum value of the SeekBar (0 to 1 ... with decimal points)
        size.setMax(seekBarMax);
        flow.setMax(seekBarMax);
        opacity.setMax(seekBarMax);

        // Initialize the SharedPreferencesHelper
        preferencesHelper = new SharedPreferencesHelper(this);


        // getting saved values
        // Retrieve the values
        brushName = preferencesHelper.getString(BRUSH_NAME, "Pen");

        // If there's a saved value, set the spinner to that value
        String name = brushName;
        if (name.contains(" ")) {
            name = name.replace(" ", "");
        }

        // set brush type
        resources = getResources(); // Assuming you're in an Activity or Context
        brushesRepository = new BrushesRepository(resources);
        realBrush = com.raed.rasmview.brushtool.data.Brush.valueOf(name);
        rasmContext.setBrushConfig(brushesRepository.get(realBrush));


        sizeF = preferencesHelper.getFloat(SIZE, 0.3f);
        flowF = preferencesHelper.getFloat(FLOW, 1f);
        opacityF = preferencesHelper.getFloat(OPACITY, 1f);

        // setting saved values
        int savedSize = (int) (sizeF * seekBarMax); // Map float to seekbar progress
        size.setProgress(savedSize);

        int savedFlow = (int) (flowF * seekBarMax); // Map float to seekbar progress
        flow.setProgress(savedFlow);

        int savedOpacity = (int) (opacityF * seekBarMax); // Map float to seekbar progress
        opacity.setProgress(savedOpacity);

        rasmContext.getBrushConfig().setSize(sizeF);
        rasmContext.getBrushConfig().setFlow(flowF);
        rasmContext.getBrushConfig().setOpacity(opacityF);

        brush_color = preferencesHelper.getInt(BRUSH_COLOR, -16777216); // default black
        bg_color = preferencesHelper.getInt(BG_COLOR, -1); // default white

        // set brush color
        rasmContext.setBrushColor(brush_color);

        // set bg color
        rasmContext.setBackgroundColor$rasmview_release(bg_color);


        int spinnerPosition = adapter.getPosition(Brush.valueOf(name));
        penType.setSelection(spinnerPosition);

        rasmContext.setRotationEnabled(false); // disable rotation
    }

    private void clicklisteners() {
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rasmContext.clear();
            }
        });


        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rasmState = rasmContext.getState();
                if (rasmState.canCallUndo())
                    rasmState.undo();
            }
        });

        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rasmState = rasmContext.getState();
                if (rasmState.canCallRedo())
                    rasmState.redo();
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rasmView.resetTransformation();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmap = rasmContext.exportRasm();

                // Show the dialog to save the bitmap
                showSaveDrawingDialog(SketchPad.this, bitmap);

            }
        });

        imgOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!optionsopen) {
                    imgOptions.setImageResource(R.drawable.down_icon);
                    optionsSection.animate().alpha(1.0f);
                    optionsSection.animate().translationY(0);
                    optionsSection.setVisibility(View.VISIBLE);
                } else {
                    imgOptions.setImageResource(R.drawable.up_icon);
                    optionsSection.animate().alpha(0.0f);
                    optionsSection.animate().translationY(optionsSection.getHeight());

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Do something after 5s = 5000ms
                            optionsSection.setVisibility(View.GONE);
                        }
                    }, 400);
                }

                optionsopen = !optionsopen;
            }
        });


        brushColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int mDefaultColor = rasmContext.getBrushColor();

                new ColorPickerDialog
                        .Builder(SketchPad.this)
                        .setTitle("Pick Brush Color")
                        .setColorShape(ColorShape.SQAURE)
                        .setDefaultColor(mDefaultColor)
                        .setColorListener(new ColorListener() {
                            @Override
                            public void onColorSelected(int color, @NotNull String colorHex) {
                                // Handle Color Selection
//                                String hexColorWithAlpha = "#" + "AA" + colorHex.substring(1);
                                brush_color = Color.parseColor(colorHex);
                                rasmContext.setBrushColor(brush_color);
                                preferencesHelper.saveInt(BRUSH_COLOR, brush_color);
                            }
                        })
                        .show();
            }
        });

        bgColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int mDefaultColor = rasmContext.getBackgroundColor$rasmview_release();

                new ColorPickerDialog
                        .Builder(SketchPad.this)
                        .setTitle("Pick Brush Color")
                        .setColorShape(ColorShape.SQAURE)
                        .setDefaultColor(mDefaultColor)
                        .setColorListener(new ColorListener() {
                            @Override
                            public void onColorSelected(int color, @NotNull String colorHex) {
                                // Handle Color Selection
//                                String hexColorWithAlpha = "#" + "AA" + colorHex.substring(1);
                                bg_color = Color.parseColor(colorHex);
                                rasmContext.setBackgroundColor$rasmview_release(bg_color);
                                preferencesHelper.saveInt(BG_COLOR, bg_color);
                                rasmView.refreshDrawableState();
                            }
                        })
                        .show();

            }
        });


        penType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Brush brush = (Brush) parent.getItemAtPosition(position);

                preferencesHelper.saveString(BRUSH_NAME, brush.toString());

                brushName = brush.toString();
                if (brushName.contains(" ")) {
                    brushName = brushName.replace(" ", "");
                }

                realBrush = com.raed.rasmview.brushtool.data.Brush.valueOf(brushName);
                rasmContext.setBrushConfig(brushesRepository.get(realBrush));
                rasmContext.getBrushConfig().setSize(sizeF);
                rasmContext.getBrushConfig().setFlow(flowF);
                rasmContext.getBrushConfig().setOpacity(opacityF);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Set an OnSeekBarChangeListener to get the value when the SeekBar changes
        size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Calculate the decimal value based on the progress
                sizeF = progress / 100f;

                rasmContext.getBrushConfig().setSize(sizeF);
                rasmContext.getBrushConfig().setFlow(flowF);
                rasmContext.getBrushConfig().setOpacity(opacityF);

                preferencesHelper.saveFloat(SIZE, sizeF);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Optional: Do something when the user starts dragging the SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Optional: Do something when the user stops dragging the SeekBar
            }
        });

        flow.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Calculate the decimal value based on the progress
                flowF = progress / 100f;

                rasmContext.getBrushConfig().setSize(sizeF);
                rasmContext.getBrushConfig().setFlow(flowF);
                rasmContext.getBrushConfig().setOpacity(opacityF);

                preferencesHelper.saveFloat(FLOW, flowF);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Optional: Do something when the user starts dragging the SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Optional: Do something when the user stops dragging the SeekBar
            }
        });

        opacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Calculate the decimal value based on the progress
                opacityF = progress / 100f;

                rasmContext.getBrushConfig().setSize(sizeF);
                rasmContext.getBrushConfig().setFlow(flowF);
                rasmContext.getBrushConfig().setOpacity(opacityF);

                preferencesHelper.saveFloat(OPACITY, opacityF);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Optional: Do something when the user starts dragging the SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Optional: Do something when the user stops dragging the SeekBar
            }
        });
    }

    private void showSaveDialog(Context context, Bitmap bitmap) {
        // Create an EditText for user to enter the file name
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter drawing name");

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Save Drawing")
                .setMessage("Please enter a name for this drawing")
                .setView(input)  // Add the EditText to the dialog
                .setPositiveButton("Save", (dialog, which) -> {
                    String fileName = input.getText().toString();

                    if (!fileName.isEmpty()) {

                        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, fileName, "Description");

                        Toast.makeText(context, "Drawing saved!", Toast.LENGTH_LONG).show();

                    } else {
                        input.setError("File name cannot be empty");
                        Toast.makeText(context, "File name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }

    private void showSaveDrawingDialog(Context context, Bitmap bitmap) {
        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.save_drawing_dialog, null);

        // Create the dialog with the custom style
        Dialog dialog = new Dialog(context, R.style.CustomDialogTheme);
        dialog.setContentView(dialogView);

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Find the views in the dialog layout
        final EditText editTextFileName = dialogView.findViewById(R.id.et_field);
        Button buttonSave = dialogView.findViewById(R.id.btn_save);
        Button buttonCancel = dialogView.findViewById(R.id.btn_cancel);

        // Create the dialog
        dialog.setContentView(dialogView);

        // Set button click listeners
        buttonSave.setOnClickListener(v -> {
            String fileName = editTextFileName.getText().toString();

            if (!fileName.isEmpty()) {

                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, fileName, "Description");

                Toast.makeText(context, "Drawing saved!", Toast.LENGTH_LONG).show();

            } else {
                editTextFileName.setError("File name cannot be empty");
                Toast.makeText(context, "File name cannot be empty", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());  // Dismiss the dialog on cancel

        dialog.show();  // Show the dialog
    }
}

