package net.ucoz.ksen.cannongame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class GameElement {
    protected CannonView view;
    protected Paint paint = new Paint();
    protected Rect shape;

    private float velocityY;
    private int soundId;

    public GameElement(CannonView view, int color, int soundId, int x, int y, int width,int length, float velocityY){
        this.view = view;
        paint.setColor(color);
        this.soundId = soundId;
        shape = new Rect(x, y, x + width, y + length);
        this.velocityY = velocityY;
    }

    // Обновление позиции и проверка столкновений
    public void update(double interval){
        // Смещение по вертикали
        shape.offset(0, (int)(velocityY * interval));

        if (shape.top < 0 && velocityY <0 || shape.bottom > view.getScreenHeigh() && velocityY > 0)
            velocityY *= -1;
    }

    public void draw(Canvas canvas){
        canvas.drawRect(shape, paint);
    }

    public void playSound(){
        view.playSound(soundId);
    }

}
