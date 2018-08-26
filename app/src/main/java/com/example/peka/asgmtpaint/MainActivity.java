package com.example.peka.asgmtpaint;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.content.pm.ActivityInfo;

//Library for the color picker. Was got from https://github.com/yukuku/ambilwarna
import yuku.ambilwarna.AmbilWarnaDialog;





public class MainActivity extends AppCompatActivity {

    private PaintView paintView;
    int mDrawColor;
    int mBrushSize;
    int mToolId;
    Button mColorBtn;
    ImageButton mSizeBtn;
    ImageButton mToolsBtn;
    ImageButton mBrushBtn;
    ImageButton mRectBtn;
    ImageButton mOvalBtn;
    ImageButton mDotBtn;
    ImageButton mEraserBtn;
    ImageButton mUndoBtn;
    SeekBar mSizeBar;
    TextView mSizeTxt;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Buttons animation when clicking
        final Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
        //Layout for painting
        paintView = findViewById(R.id.PaintView);

        //Color picker button click
        mDrawColor = Color.RED; //Default color
        mColorBtn = findViewById(R.id.btnColor);
        mColorBtn.setBackgroundColor(mDrawColor);
        mColorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animAlpha);
                openColorPicker();
            }
        });

        //Brush size change button click
        mBrushSize = 20; //Default brush size
        mSizeTxt = findViewById(R.id.txtSize);
        mSizeTxt.setText(String.valueOf(mBrushSize));
        mSizeBar = findViewById(R.id.barSize);
        mSizeBar.setProgress(10); //Default brush size minus 10, as the minimal brush size is 10
        mSizeBtn = findViewById(R.id.btnSize);
        mSizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animAlpha);
                openSizeChanger();
            }
        });

        mSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changeBrushSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //Select tool button click
        mToolsBtn = findViewById(R.id.btnTools);
        mBrushBtn  = findViewById(R.id.btnBrush);
        mRectBtn = findViewById(R.id.btnRect);
        mOvalBtn  = findViewById(R.id.btnOval);
        mDotBtn  = findViewById(R.id.btnDot);

        mToolsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animAlpha);
                openToolsMenu();
            }
        });

        mBrushBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mToolId = 1;
                paintView.setTool(mToolId);
                paintView.setColor(mDrawColor);
                mToolsBtn.setImageResource(R.drawable.brush);
                closeAll();
            }
        });
        mRectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mToolId = 2;
                paintView.setTool(mToolId);
                paintView.setColor(mDrawColor);
                mToolsBtn.setImageResource(R.drawable.rect);
                closeAll();
            }
        });
        mOvalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mToolId = 3;
                paintView.setTool(mToolId);
                paintView.setColor(mDrawColor);
                mToolsBtn.setImageResource(R.drawable.oval);
                closeAll();
            }
        });
        mDotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mToolId = 4;
                paintView.setTool(mToolId);
                paintView.setColor(mDrawColor);
                mToolsBtn.setImageResource(R.drawable.dot);
                closeAll();
            }
        });

        //Eraser button click
        mEraserBtn = findViewById(R.id.btnEraser);
        mEraserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.setEraser();
                paintView.normal();
                v.startAnimation(animAlpha);
                closeAll();
            }
        });

        //Undo button click
        mUndoBtn = findViewById(R.id.btnUndo);
        mUndoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.undo();
                v.startAnimation(animAlpha);
            }
        });


        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView.init(metrics);
    }

    //Open color picker dialog and use color picker library
    public void openColorPicker() {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, mDrawColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                mDrawColor = color;
                paintView.setColor(mDrawColor);
                mColorBtn.setBackgroundColor(mDrawColor);
            }
        });
        colorPicker.show();
    }

    //Open size bar
    public void openSizeChanger() {
        if (mSizeBar.getVisibility() == View.VISIBLE)
        {
            mSizeBar.setVisibility(View.INVISIBLE);
            mSizeTxt.setVisibility(View.INVISIBLE);
        }

        else
        {
            closeAll();
            mSizeBar.setVisibility(View.VISIBLE);
            mSizeTxt.setVisibility(View.VISIBLE);
        }
    }

    //Open tools menu
    public void openToolsMenu() {
        if (mBrushBtn.getVisibility() == View.VISIBLE)
        {
            closeAll();
        }

        else
        {
            closeAll();
            mBrushBtn.setVisibility(View.VISIBLE);
            mRectBtn.setVisibility(View.VISIBLE);
            mOvalBtn.setVisibility(View.VISIBLE);
            mDotBtn.setVisibility(View.VISIBLE);
        }
    }

    //Close all active menus
    public void closeAll() {
        mBrushBtn.setVisibility(View.INVISIBLE);
        mRectBtn.setVisibility(View.INVISIBLE);
        mOvalBtn.setVisibility(View.INVISIBLE);
        mDotBtn.setVisibility(View.INVISIBLE);
        mSizeBar.setVisibility(View.INVISIBLE);
        mSizeTxt.setVisibility(View.INVISIBLE);

    }

    //Change brush size
    public void changeBrushSize(int progress) {
        mBrushSize = progress + 10; //As minimal brush size is 10
        mSizeTxt.setText(String.valueOf(mBrushSize));
        paintView.setSize(mBrushSize);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.normal:
                paintView.normal();
                return true;

            case R.id.emboss:
                paintView.emboss();
                return true;

            case R.id.blur:
                paintView.blur();
                return true;

            case R.id.undo:
                paintView.undo();
                return true;

            case R.id.redo:
                paintView.redo();
                return true;

            case R.id.clear:
                paintView.clear();
                return true;

            case R.id.swag:
                if(item.isChecked()){
                    item.setChecked(false);
                    paintView.swagOff();
                    paintView.setColor(mDrawColor);
                }else {
                    item.setChecked(true);
                    paintView.swagOn();
                }
        }

        return super.onOptionsItemSelected(item);
    }
}
