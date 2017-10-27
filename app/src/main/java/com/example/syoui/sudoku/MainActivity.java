package com.example.syoui.sudoku;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myscript.atk.scw.SingleCharWidget;
import com.myscript.atk.scw.SingleCharWidgetApi;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        SingleCharWidgetApi.OnConfiguredListener,
        SingleCharWidgetApi.OnTextChangedListener,
        SingleCharWidgetApi.OnBackspaceGestureListener,
        SingleCharWidgetApi.OnSingleTapGestureListener,
        SingleCharWidgetApi.OnLongPressGestureListener,
        SingleCharWidgetApi.OnReturnGestureListener{


    private static final String TAG = "SingleCharDemo";

    private SingleCharWidgetApi widget;

    private ImageAdapter _im;

    private StepList stepList;

    View containerView;
    View childView;
    Animation inAnimation;
    Animation outAnimation;

    GridView gridview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        containerView = findViewById(R.id.container);
        childView = findViewById(R.id.childview);
        inAnimation = (Animation) AnimationUtils.loadAnimation(this, R.anim.in_animation);
        outAnimation= (Animation) AnimationUtils.loadAnimation(this, R.anim.out_animation);



//        containerView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // ビューが表示されてるか判定
//                if(childView.getVisibility() == View.GONE){
//                    // アニメーションしながらViewを表示
//                    childView.startAnimation(inAnimation);
//                    childView.setVisibility(View.VISIBLE);
//                }
//                else{
//                    // アニメーションしながらViewを隠す
//                    childView.startAnimation(outAnimation);
//                    childView.setVisibility(View.GONE);
//                }
//            }
//        });



        gridview = (GridView) findViewById(R.id.gridview);
        _im = new ImageAdapter(this,gridview);
        stepList = new StepList(gridview,_im,getApplicationContext());
        _im.sp = stepList;
        gridview.setAdapter(_im);
        init();

    }


    private void init(){
        widget = (SingleCharWidget) findViewById(R.id.singleChar_widget);
        if (!widget.registerCertificate(MyCertificate.getBytes()))
        {
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
            dlgAlert.setMessage("Please use a valid certificate.");
            dlgAlert.setTitle("Invalid certificate");
            dlgAlert.setCancelable(false);
            dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    //dismiss the dialog
                }
            });
            dlgAlert.create().show();
            return;
        }

        widget.setOnConfiguredListener(this);
        widget.setOnTextChangedListener(this);
        widget.setOnBackspaceGestureListener(this);
        widget.setOnReturnGestureListener(this);
        widget.setOnSingleTapGestureListener(this);
        widget.setOnLongPressGestureListener(this);

        // references assets directly from the APK to avoid extraction in application
        // file system
        widget.addSearchDir("zip://" + getPackageCodePath() + "!/assets/conf");

        // The configuration is an asynchronous operation. Callbacks are provided to
        // monitor the beginning and end of the configuration process and update the UI
        // of the input method accordingly.
        //
        // "en_US" references the en_US bundle name in conf/en_US.conf file in your assets.
        // "si_text" references the configuration name in en_US.conf
        widget.configure("en_US", "si_text");

        widget.setRecognitionMode(2);

    }

    protected void onDestroy()
    {
        widget.setOnTextChangedListener(null);
        widget.setOnConfiguredListener(null);
        super.onDestroy();
    }

    @Override
    public void onConfigured(SingleCharWidgetApi widget, boolean success)
    {
        if(!success)
        {
            Toast.makeText(getApplicationContext(), widget.getErrorString(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Unable to configure the Single Char Widget: " + widget.getErrorString());
            return;
        }
        Toast.makeText(getApplicationContext(), "Single Char Widget Configured", Toast.LENGTH_SHORT).show();
        if(BuildConfig.DEBUG)
            Log.d(TAG, "Single Char Widget configured!");
    }

    @Override
    public void onTextChanged(SingleCharWidgetApi widget, String s, boolean intermediate) {
        //Toast.makeText(getApplicationContext(), "Recognition update", Toast.LENGTH_SHORT).show();

        if(BuildConfig.DEBUG)
        {
            Log.d(TAG, "" + s.charAt(s.length()-1));

            if(!intermediate) return;


            if(s.charAt(s.length()-1) == 'V'){
                stepList.showStepList();
            }

            if(s.charAt(s.length()-1) == ' '){
                Log.i("insert Space","");
                stepList.gotoNextStep();
            }

            if(s.charAt(s.length()-1)-'0' > 0 && s.charAt(s.length()-1)-'0' < 10){
                Step sp = new Step(_im.selectRow,_im.selectCol,s.charAt(s.length()-1)-'0');
                stepList.add(sp);
                _im.changeRowColVal(s.charAt(s.length()-1)-'0');

                gridview.setAdapter(_im);
            }


            if(_im.checkSuccess()){
                showSuccess();
            }else{
                showFailure();
            }

        }
    }

    @Override
    public void onBackspaceGesture(SingleCharWidgetApi singleCharWidgetApi, int i, int i1) {
            Log.i("onBackspace",i+":"+i1);
            stepList.gotoLastStep();

            gridview.setAdapter(_im);


    }

    @Override
    public void onReturnGesture(SingleCharWidgetApi singleCharWidgetApi, int i) {
            Log.i("onReturnGesture",i+"");
            stepList.clearStepList();
            _im.resetArray();
            _im.selectCol = -1;
            _im.selectRow = -1;

            gridview.setAdapter(_im);
    }


    @Override
    public boolean onSingleTapGesture(SingleCharWidgetApi w, float x, float y) {
        Log.d(TAG, "Single tap gesture detected at x=" + x + " y=" + y);
        // we don't handle the gesture


        return false;
    }

    @Override
    public boolean onLongPressGesture(SingleCharWidgetApi w, float x, float y) {
        Log.d(TAG, "Long press gesture detected at x=" + x + " y=" + y);
        // we don't handle the gesture
        //showSuccess();

        //remove

        return false;
    }


    private void showSuccess(){
        


        childView.startAnimation(inAnimation);
        childView.setVisibility(View.VISIBLE);

        Snackbar.make(findViewById(R.id.myCoordinatorLayout),"congratulation",
                Snackbar.LENGTH_LONG)
                .show();
    }


    private void showFailure(){


        childView.startAnimation(inAnimation);
        childView.setVisibility(View.VISIBLE);

        if(childView.getVisibility() == View.VISIBLE){
            childView.startAnimation(outAnimation);
            childView.setVisibility(View.GONE);
            Snackbar.make(findViewById(R.id.myCoordinatorLayout),"Some thing is wrong",
                    Snackbar.LENGTH_LONG)
                    .show();
        }


    }

}



class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private GridView gv;
    public int rowNum = 9;
    public int colNum = 9;

    public int selectRow = -1;
    public int selectCol = -1;
    public StepList sp;

    int defaultArr[][] =  {
        {2,0,0,0,0,8,1,0,0},
        {0,0,0,0,7,1,0,0,0},
        {0,0,9,0,0,5,4,6,0},
        {0,0,0,4,0,0,0,0,3},
        {0,3,0,0,0,0,0,0,2},
        {8,7,1,0,0,0,0,9,0},
        {1,0,2,0,0,0,0,0,0},
        {0,0,3,0,0,9,0,0,6},
        {0,0,0,2,6,0,0,5,1}
    };

    int arr[][] =  {
            {2,0,0,0,0,8,1,0,0},
            {0,0,0,0,7,1,0,0,0},
            {0,0,9,0,0,5,4,6,0},
            {0,0,0,4,0,0,0,0,3},
            {0,3,0,0,0,0,0,0,2},
            {8,7,1,0,0,0,0,9,0},
            {1,0,2,0,0,0,0,0,0},
            {0,0,3,0,0,9,0,0,6},
            {0,0,0,2,6,0,0,5,1}
    };

    int constArr[][]= {
            {1,0,0,0,0,1,1,0,0},
            {0,0,0,0,1,1,0,0,0},
            {0,0,1,0,0,1,1,1,0},
            {0,0,0,1,0,0,0,0,1},
            {0,1,0,0,0,0,0,0,1},
            {1,1,1,0,0,0,0,1,0},
            {1,0,1,0,0,0,0,0,0},
            {0,0,1,0,0,1,0,0,1},
            {0,0,0,1,1,0,0,1,1}
    };

    int successArr[][] = {
            {2,5,6,9,4,8,1,3,7},
            {3,4,8,6,7,1,9,2,5},
            {7,1,9,3,2,5,4,6,8},
            {9,2,5,4,8,6,7,1,3},
            {6,3,4,1,9,7,5,8,2},
            {8,7,1,5,3,2,6,9,4},
            {1,6,2,8,5,4,3,7,9},
            {5,8,3,7,1,9,2,4,6},
            {4,9,7,2,6,3,8,5,1}
    };

    public ImageAdapter(Context c,GridView _gv) {
        mContext = c;
        gv = _gv;
    }


    public void resetArray(){

        for(int i=0;i<arr.length;i++){
            for(int j=0;j<arr[i].length;j++){
                arr[i][j] = defaultArr[i][j];
            }
        }

    }

    public void changeRowColVal(int val){
        if(selectRow >= 0 && selectCol >= 0){
            arr[selectRow][selectCol] = val;
        }
    }

    public int getCount() {
        return rowNum*colNum;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public LinearLayout getView(int position, View convertView, ViewGroup parent) {

        int currentRow = (int)Math.floor(position/colNum);
        int currentCol = position%colNum;

        int valOfCell = arr[currentRow][currentCol];

        TextView et;

        LinearLayout ly = new LinearLayout(mContext);

        ly.setBackgroundColor(Color.BLACK);

        ly = setBorder(currentRow,currentCol,ly);

        et = new TextView(mContext);

        if(constArr[currentRow][currentCol] == 0){
            et.setOnClickListener(new CellOnClickListener(gv,et,this,currentRow,currentCol));

            et.setOnLongClickListener(new CellOnLongClickListener(gv,et,this,currentRow,currentCol,sp));

            et.setTextColor(Color.BLACK);
        }else{
            et.setTextColor(Color.GRAY);
        }

        et.setGravity(Gravity.CENTER);


        //here check the value of the cell is illegal or not

        if(valOfCell>0){
            et.setText(valOfCell+"");
        }else{
            et.setText("");
        }



        if(currentRow == selectRow && currentCol == selectCol){
            et.setBackgroundColor(Color.DKGRAY);
        }else{
            et.setBackgroundColor(Color.WHITE);
        }

        if(arr[currentRow][currentCol] > 0){

            if(!checkTheValueOfCell(currentRow,currentCol)){
                if(currentRow == selectRow && currentCol == selectCol){
                    et.setTextColor(Color.RED);
                }else{
                    et.setBackgroundColor(Color.YELLOW);
                }
            }

        }

        et.setHeight(120);
        et.setWidth(130);
        et.setTextSize(33);

        ly.addView(et);

        return ly;
    }

    private boolean checkTheValueOfCell(int currentRow,int currentCol){

        //check the value in row is duplicated or not

        for(int i=0;i<arr[currentRow].length;i++){
            if(arr[currentRow][i] == arr[currentRow][currentCol]){
                if(i == currentCol) continue;
                Log.i("row check",arr[currentRow][i]+":"+arr[currentRow][currentCol]);
                return false;
            }
        }



        //check the value in col is duplicated or not

        for(int j=0;j<9;j++){
            if(arr[j][currentCol] == arr[currentRow][currentCol]){
                if( j == currentRow) continue;
                Log.i("col check",arr[currentRow][j]+":"+arr[currentRow][currentCol]);
                return false;
            }
        }


        //check the value in the 9X9xcells is duplicated or not

        int boundaryRowB = currentRow + (2-(currentRow%3));
        int boundaryRowT = currentRow - (currentRow%3);
        int boundaryColL = currentCol - (currentCol%3);
        int boundaryColR = currentCol + (2-currentCol%3);



        for(int i=boundaryRowT;i<=boundaryRowB;i++){
            for(int j=boundaryColL;j<=boundaryColR;j++){
                if(arr[i][j] == arr[currentRow][currentCol]){
                    if(i == currentRow && j == currentCol) continue;
                    Log.i("cross check",arr[i][j]+":"+arr[currentRow][currentCol]);
                    return false;
                }
            }
        }

        return true;
    }


    protected boolean checkSuccess(){
        boolean res = true;
        for(int i=0;i<arr.length;i++){
            for(int j=0;j<arr[i].length;j++){
                Log.i("stoped at",i+":"+j);
                if(arr[i][j] != successArr[i][j]) return false;
            }
        }

        return  res;
    }



    private LinearLayout setBorder(int rowNum,int colNum,LinearLayout _ly){
        //left top right bottom
        int defaultL = 1;
        int defaultT = 1;
        int defaultR = 1;
        int defaultB = 1;

        if(rowNum%3 == 2){
            defaultB = 5;
        }

        if(colNum%3 == 2){
            defaultR = 5;
        }

        _ly.setPadding(defaultL,defaultT,defaultR,defaultB);
        return _ly;
    }

}


class CellOnClickListener implements TextView.OnClickListener{

    TextView textview;
    ImageAdapter ia;
    GridView gv;
    int positionRow,positionCol;
    CellOnClickListener(GridView ma,TextView _view,ImageAdapter _ia,int selectedRow,int selectedCol){
        textview = _view;
        ia = _ia;
        positionRow = selectedRow;
        positionCol = selectedCol;
        gv = ma;
    }
    @Override
    public void onClick(View view) {
        textview.setBackgroundColor(Color.DKGRAY);

        ia.selectRow = positionRow;
        ia.selectCol = positionCol;

        gv.setAdapter(ia);


    }

}


class CellOnLongClickListener implements View.OnLongClickListener {

    GridView gv;
    TextView et;
    int currentRow;
    int currentCol;
    ImageAdapter im;
    StepList  sp;

    CellOnLongClickListener(GridView _gv,TextView _et,ImageAdapter _im,int _currentRow,int _currentCol,StepList _sp){
        super();
        gv = _gv;
        et = _et;
        im = _im;
        currentRow = _currentRow;
        currentCol = _currentCol;
        sp = _sp;
    }
    @Override
    public boolean onLongClick(View view) {
        im.selectRow = currentRow;
        im.selectCol = currentCol;
        im.changeRowColVal(0);
        Step _sp = new Step(im.selectRow,im.selectCol,0);
        sp.add(_sp);
        return false;
    }
}


class StepList extends ArrayList<Step>{

    int currentStep;
    GridView gv;
    ImageAdapter im;
    Context c;


    public StepList(GridView _v,ImageAdapter _im,Context _c) {
        super();
        gv = _v;
        im = _im;
        c = _c;
    }

    void showStepList(){
        for(int i=0;i<this.size();i++){
            Log.i("rowNum",this.get(i).rowNum+"");
            Log.i("colNum",this.get(i).colNum+"");
            Log.i("value",this.get(i).value+"");
        }
    }

    @Override
    public boolean add(Step step) {
        currentStep = this.size();
        Log.i("current step",currentStep+"");
        return super.add(step);
    }

    void clearStepList(){
        currentStep = 0;
        this.clear();
    }


     void gotoLastStep(){
        if(currentStep > 0){
            currentStep = currentStep - 1;
            int lastStepRowNum = this.get(currentStep).rowNum;
            int lastStepColNum = this.get(currentStep).colNum;
            int lastStepVal = this.get(currentStep).value;
            im.selectRow = lastStepRowNum;
            im.selectCol = lastStepColNum;
            im.changeRowColVal(lastStepVal);
            gv.setAdapter(im);
        }else{
            Toast.makeText(c, "It is already the first step", Toast.LENGTH_LONG).show();
        }

    }


    void gotoNextStep(){

        if(currentStep < this.size()-1){
            currentStep = currentStep + 1;
            int lastStepRowNum = this.get(currentStep).rowNum;
            int lastStepColNum = this.get(currentStep).colNum;
            int nextStepVal = this.get(currentStep).value;
            im.selectRow = lastStepRowNum;
            im.selectCol = lastStepColNum;
            im.changeRowColVal(nextStepVal);

            gv.setAdapter(im);
        }else{
            Toast.makeText(c, "It is already the last step now", Toast.LENGTH_LONG).show();
        }

    }


}

class Step{
    int rowNum;
    int colNum;
    int value;

    public Step(int rowNum, int colNum, int value) {
        this.rowNum = rowNum;
        this.colNum = colNum;
        this.value = value;
    }
}
