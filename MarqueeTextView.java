package tw.pma.parkinfo;


import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class MarqueeTextView extends androidx.appcompat.widget.AppCompatTextView {

    private final Context context;
    private Scroller scroller;
    private TextView textView;
    private boolean paused = true; //跑馬燈暫停狀態
    private boolean startForRight = true; //跑馬燈是否由畫面最右邊近來
    private boolean isPaused = false; //是否暫停過跑馬燈

    private JSONArray messageData = new JSONArray();
    private int startX; //起始位置
    private int distance; //跑馬燈捲動距離
    private int velocity; //速度參數
    private int number = 0; //目前資料位置
    private String Message = "";

    public MarqueeTextView(Context context) {
        super(context);

        this.context = context;
        setSingleLine();
        setEllipsize(null);
        setHorizontallyScrolling(true);
        setVisibility(INVISIBLE);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        setSingleLine();
        setEllipsize(null);
        setHorizontallyScrolling(true);
        setVisibility(INVISIBLE);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.context = context;
        setSingleLine();
        setEllipsize(null);
        setHorizontallyScrolling(true);
        setVisibility(INVISIBLE);
    }

    //初始化跑馬燈
    public void initMarqueeTextView(TextView textView, JSONArray messageData, boolean startForRight, int velocity) {

        this.textView = textView;
        this.messageData = messageData;
        this.startForRight = startForRight;
        this.velocity = velocity;
        number = 0;

        //資料內容可自定義
        Message = messageData.optJSONObject(number).optString("NewsTitle");

        this.textView.setText(Message);
        startScroll();
    }

    public void startScroll() { //啟動跑馬燈

        paused = true;
        startX = !startForRight ? 0 : - getWidth(); //依據狀態決定跑馬燈起始位置
        resumeScroll();
    }

    public void resumeScroll() { //重新啟動跑馬燈

        if (paused) {
            scroller = new Scroller(context, new LinearInterpolator());
            setScroller(scroller);
            paused = false;
            setVisibility(VISIBLE);
            invalidate();
            calculateMoveDistance();
            int duration = velocity * distance; //跑馬燈滾動的時間；velocity數字越小越快，最小為1
            scroller.startScroll(startX, 0, distance, 0, duration);
        }
    }

    private void calculateMoveDistance() { //計算位置

        Rect rect = new Rect();
        String textString =  String.valueOf(getText());
        getPaint().getTextBounds(textString, 0, textString.length(), rect);
        int moveDistance = rect.width();

        if (isPaused) { //判斷是否有暫停過跑馬燈抓取距離
            isPaused = false;
            distance = !startForRight ? moveDistance : moveDistance - startX;
        } else {
            distance = !startForRight ? moveDistance : moveDistance + getWidth();
        }
    }

    public void pauseScroll() { //跑馬燈暫停

        if (scroller == null) {
            return;
        }

        if (paused) {
            return;
        }
        paused = true;
        isPaused = true;
        startX = scroller.getCurrX(); //儲存當前X的位置
        scroller.abortAnimation();
    }

    public void stopScroll(boolean isStop) { //停止跑馬燈
        paused = isStop;
    }

    public JSONObject getMessageData() { //取得跑馬燈相關資料(可自定義)
        return messageData.optJSONObject(number);
    }

    @Override
    public void computeScroll() { //跑馬燈結束
        super.computeScroll();

        if (scroller == null) {
            return;
        }

        if (scroller.isFinished() && (!paused)) {

            setVisibility(INVISIBLE);

            if (messageData.length() != 0) { //抓取下一筆資料(可自定義)
                if (number == messageData.length() - 1) {
                    number = 0;
                } else {
                    number++;
                }
                Message = messageData.optJSONObject(number).optString("NewsTitle");
            }

            //為確保資料替換時，長度大於上一筆時不會顯示在畫面上，所以遲0.5秒
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    textView.setText(Message);
                    startScroll();
                }
            }, 500);
        }
    }
}
