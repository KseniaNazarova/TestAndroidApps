package net.ucoz.ksen.cannongame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

public class Cannon {
    private int baseRadius;
    private int barrelLength; // Длина ствола
    private Point barrelEnd = new Point(); // Конечная точка
    private double barrelAngle; // Угол наклона ствола
    private CannonBall cannonBall; // Ядро
    private CannonView view;
    private Paint paint = new Paint();

    public Cannon(CannonView view, int baseRadius, int barrelLength, int barrelWidth){
        this.view = view;
        this.baseRadius = baseRadius;
        this.barrelLength = barrelLength;
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(barrelWidth);
        align(Math.PI / 2);
    }

    // Направление пушки
    public void align(double barrelAngle){
        this.barrelAngle = barrelAngle;
        barrelEnd.x = (int) (barrelLength * Math.sin(barrelAngle));
        barrelEnd.y = (int) (-barrelLength * Math.cos(barrelAngle)) + view.getScreenHeight() / 2;
    }

    // Создает ядро и стреляет
    public void fireCannonBall(){
        int velocityX = (int)(CannonView.CANNONBALL_SPEED_PERCENT * view.getScreenWidth() * Math.sin(barrelAngle));
        int velocityY = (int)(CannonView.CANNONBALL_SPEED_PERCENT * view.getScreenWidth() * -Math.cos(barrelAngle));

        int radius = (int) (view.getScreenHeight() * CannonView.CANNONBALL_RADIUS_PERSENT);

        cannonBall = new CannonBall(view, Color.BLACK, CannonView.CANNON_SOUND_ID, -radius, view.getScreenHeight() / 2 - radius, radius, velocityX, velocityY);
        cannonBall.playSound();
    }

    public void draw(Canvas canvas){
        canvas.drawLine(0, view.getScreenHeight() / 2, barrelEnd.x, barrelEnd.y, paint);
        canvas.drawCircle(0, view.getScreenHeight() / 2, baseRadius, paint);
    }

    public CannonBall getCannonBall(){
        return cannonBall;
    }

    public void removeCannonBall(){
        cannonBall = null;
    }
}
