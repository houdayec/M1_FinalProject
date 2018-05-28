package dapm.g1.final_project.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dapm.g1.final_project.MainActivity;
import dapm.g1.final_project.R;

public class TypeActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * VIEW BINDING
     */
    @BindView(R.id.generateAnamorphosis)
    Button mGenerateAnamorphosisButton;

    @BindView((R.id.spinner))
    Spinner mSpinnerDirection;

    @BindView(R.id.validVideoFP)
    ImageButton validVideo;

    /**
     * INTERN STATE
     */

    private Paint mPaint;
    private boolean didUserAlreadyDraw = false;

    float tempDx, tempDy = 0;

    private List<Point> listPoints = new ArrayList<>();

    public int width;
    public  int height;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint   mBitmapPaint;
    Context context;
    private Paint circlePaint;
    private Path circlePath;
    private Uri uriData;
    private DrawingView dv;
    private LinearLayout layoutDrawingView;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);

        // Binding view
        ButterKnife.bind(this);

        uriData = Uri.parse(getIntent().getStringExtra("uri_video"));

        dv = new DrawingView(this);
        dv.setBackgroundColor(getColor(R.color.white));

        layoutDrawingView = findViewById(R.id.layoutDrawingView);
        layoutDrawingView.addView(dv);

        // Setup drawing view

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(10);

        validVideo.setOnClickListener(this);
        //cancelVideo.setOnClickListener(this);
    }

    /**
     * Method called when user clicks the generate button
     */
    @OnClick(R.id.generateAnamorphosis)
    void generateAnamorphosis(){
        Intent intentFinalRender = new Intent(this, Test_recup_frame.class);
        Bundle bundleArgs = new Bundle();
        bundleArgs.putString("uri_video", uriData.toString());
        bundleArgs.putString("direction",mSpinnerDirection.getSelectedItem().toString());
        intentFinalRender.putExtras(bundleArgs);
        startActivity(intentFinalRender);
    }

    /**
     * Method to handle view clicks
     * @param view
     */

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.validVideoFP:
                Intent intentFinalRender = new Intent(this, Test_Multi_Thread.class);
                Bundle bundleArgs = new Bundle();
                bundleArgs.putString("uri_video", uriData.toString());
                intentFinalRender.putExtras(bundleArgs);
                startActivity(intentFinalRender);
                break;
            case R.id.cancelVideoFP:
                tempDx = 0;
                mCanvas = new Canvas();
                listPoints = new ArrayList<>();
                break;
        }
    }

    /**
     * Custom drawing view
     */

    public class DrawingView extends View {

        public DrawingView(Context c) {
            super(c);
            context=c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            circlePaint = new Paint();
            circlePath = new Path();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.BLUE);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(10);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);

        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath( mPath,  mPaint);
            canvas.drawPath( circlePath,  circlePaint);

        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        /**
         * Sub custom events - user interaction with canvas
         * @param x
         * @param y
         */

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
            listPoints.add(new Point((int)x, (int)y));
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            System.out.println("coords : " + dx + " : " + dy);
            System.out.println("coords : " + x + " : " + y);
            if(tempDx < x){
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                    mX = x;
                    mY = y;
                    circlePath.reset();
                    circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
                    tempDx = x;
                    listPoints.add(new Point((int)x, (int)y));
                }
            }

        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            circlePath.reset();
            // commit the path to our offscreen
            mCanvas.drawPath(mPath,  mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }

        /**
         * Method to get user interaction events with the drawing canvas
         * @param event
         * @return
         */

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }

    /**
     * Managing when user click the back button
     */

    @Override
    public void onBackPressed() {
        Intent intentMainActivity = new Intent(this, MainActivity.class);
        startActivity(intentMainActivity);
    }
}
