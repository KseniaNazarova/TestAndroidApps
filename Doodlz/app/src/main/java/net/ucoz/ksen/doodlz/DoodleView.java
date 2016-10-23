package net.ucoz.ksen.doodlz;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.provider.MediaStore;
import android.support.v4.print.PrintHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kseniya on 21.10.2016.
 */

public class DoodleView extends View {

    private static final float TOUCH_TOLERANCE = 10;

    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private final Paint paintScreen; // вывод bitmap на экран
    private final Paint paintLine; // рисование линий на bitmap

    // данные нарисованных контуров path и содержащихся в нихточек
    private final Map<Integer, Path> pathMap = new HashMap<>();
    private final Map<Integer, Point> previousPointMap = new HashMap<>();

    public DoodleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paintScreen = new Paint();

        paintLine = new Paint();
        // параметры по умолчанию
        paintLine.setAntiAlias(true); // сглаживание
        paintLine.setColor(Color.BLACK);
        paintLine.setStyle(Paint.Style.STROKE); // сплошная
        paintLine.setStrokeWidth(5); // толщина линии
        paintLine.setStrokeCap(Paint.Cap.ROUND); // закругленные концы
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmap.eraseColor(Color.WHITE); //  фон
    }

    public void clear(){
        pathMap.clear(); // удалить все контуры
        previousPointMap.clear(); // удалить предыдущие точки
        bitmap.eraseColor(Color.WHITE);
        invalidate(); // перерисовать
    }

    public void setDrawingColor(int color){
        paintLine.setColor(color);
    }

    public int getDrawingColor(){
        return paintLine.getColor();
    }

    public void setLineWidth(int width){
        paintLine.setStrokeWidth(width);
    }

    public int getLineWidth(){
        return (int) paintLine.getStrokeWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, paintScreen);
        for (Integer key : pathMap.keySet()){
            canvas.drawPath(pathMap.get(key), paintLine);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked(); // тип события
        int actionIndex = event.getActionIndex(); // указатель (пальец)

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN)
            touchStarted(event.getX(actionIndex), event.getY(actionIndex), event.getPointerId(actionIndex));
        else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP)
            touchEnded(event.getPointerId(actionIndex));
        else
            touchMoved(event);

        invalidate();
        return true;
    }

    private void touchStarted(float x, float y, int lineId){
        Path path; // хранит контур для заданного id
        Point point; // хранит последнюю точку в контуре

        if (pathMap.containsKey(lineId)){
            path = pathMap.get(lineId);
            path.reset();
            point = previousPointMap.get(lineId);
        }
        else {
            path = new Path();
            pathMap.put(lineId, path);
            point = new Point();
            previousPointMap.put(lineId, point);
        }

        path.moveTo(x, y);
        point.x = (int) x;
        point.y = (int) y;
    }

    private void touchMoved(MotionEvent event){
        for (int i = 0; i < event.getPointerCount(); i++) {
            int pointerId = event.getPointerId(i);
            int pointerIndex = event.findPointerIndex(pointerId);

            if (pathMap.containsKey(pointerId)){
                float newX = event.getX(pointerIndex);
                float newY = event.getY(pointerIndex);

                Path path = pathMap.get(pointerId);
                Point point = previousPointMap.get(pointerId);

                float deltaX = Math.abs(newX - point.x);
                float deltaY = Math.abs(newY - point.y);

                if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE){
                    path.quadTo(point.x, point.y, (newX + point.x)/2, (newY + point.y)/2); // Bezier

                    point.x = (int) newX;
                    point.y = (int) newY;
                }
            }
        }
    }

    private void touchEnded(int lineId){
        Path path = pathMap.get(lineId);
        bitmapCanvas.drawPath(path, paintLine);
        path.reset();
    }

    public void saveImage(){
        final String name = "Doodlz" + System.currentTimeMillis() + ".jpg";
        String location = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, name, "Doodlz image");
        int result = location != null ? R.string.message_saved : R.string.message_error_saving;
        showToastMessage(result);
    }

    public void printImage(){
        if (PrintHelper.systemSupportsPrint()){
            PrintHelper printHelper = new PrintHelper(getContext());
            printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            printHelper.printBitmap("Doodlz Image", bitmap);
        }
        else {
            showToastMessage(R.string.message_error_printing);
        }
    }

    private void showToastMessage(int stringId){
        Toast message = Toast.makeText(getContext(), stringId, Toast.LENGTH_SHORT);
        message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
        message.show();
    }

}
