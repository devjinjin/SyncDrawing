package com.example.syncdrawing;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.syncdrawing.arcview.RayMenu;
import com.example.syncdrawing.connection.SyncSocket;
import com.example.syncdrawing.packet.Packet;
import com.example.syncdrawing.packet.PacketID;
import com.example.syncdrawing.touch.MouseControl;
import com.example.syncdrawing.view.ColorPickerPanelView;
import com.example.syncdrawing.view.ColorPickerView;
import com.example.syncdrawing.view.DrawingView;
import com.example.syncdrawing.view.NormalDrawingView;


public class MainActivity extends AppCompatActivity {
    private static final int[] ITEM_DRAWABLES = {R.drawable.composer_background, R.drawable.composer_color, R.drawable.composer_color,
            R.drawable.composer_erase, R.mipmap.ic_launcher};

    NormalDrawingView mTouchView;
    SyncSocket mSocketThread;
    Button connectBtn;
    Button disconnectBtn;
    CheckBox browserCb;
    CheckBox drawingCb;
    MouseControl mControl;
    TextView mActionBarTitleTv;
    // MovingMenu mMovingMenu;
    int stX = 0;// Integer.valueOf(mStartX.getText().toString());
    int stY = 0;// Integer.valueOf(mStartY.getText().toString());
    int edX = 50;// Integer.valueOf(mEndX.getText().toString());
    int edY = 50;// Integer.valueOf(mEndY.getText().toString());
    MainFragment mFragment;

    int windowwidth;
    int windowheight;
    int viewWidth = 0;
    int viewHeight = 0;

    private ConnectionBroadCast mBroadcast;

    public void getWindowSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        windowwidth = size.x;
        windowheight = size.y;
    }

    private final void initSubView() {
        RayMenu rayMenu = (RayMenu) findViewById(R.id.ray_menu);
        final int itemCount = ITEM_DRAWABLES.length;
        for (int i = 0; i < itemCount; i++) {
            ImageView item = new ImageView(this);
            item.setImageResource(ITEM_DRAWABLES[i]);

            item.setTag(i);
            rayMenu.addItem(item, new OnClickListener() {
                @Override
                public void onClick(View v) {

                    final int tag = (Integer) v.getTag();
                    if (tag == 0) {
                        if (mTouchView != null) {
                            mTouchView.setDefaultCanvas(Color.TRANSPARENT);

                            if (mSocketThread != null && mSocketThread.isConnected()) {
                                Packet packet = new Packet();
                                packet.setColor(mTouchView.getBackgroundColor());
                                packet.mPacketID = PacketID.BC_DRAW_CLEAR;
                                mSocketThread.sendClearEvent(packet);
                            }
                        }
                        Toast.makeText(MainActivity.this, "Clear All", Toast.LENGTH_SHORT).show();
                    } else if (tag == 1) {
                        showColorPickerDialog(0);
                        Toast.makeText(MainActivity.this, "Pen Color", Toast.LENGTH_SHORT).show();
                        mTouchView.startDrawing();
                    } else if (tag == 2) {
                        showColorPickerDialog(1);
                        Toast.makeText(MainActivity.this, "Background Color", Toast.LENGTH_SHORT).show();
                        mTouchView.startDrawing();
                    } else if (tag == 3) {
                        setTitle("Erase Mode");
                        mTouchView.startEraser();
                        Toast.makeText(MainActivity.this, "Eraser", Toast.LENGTH_SHORT).show();
                    } else if (tag == 4) {
                        setTitle("Drawing Mode");
                        mTouchView.startDrawing();
                        Toast.makeText(MainActivity.this, "Drawing", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    private final void initView() {

        mTouchView = (NormalDrawingView) findViewById(R.id.touchAbleView);

        mTouchView.setOnDrawingFragmentListener(new DrawingView.IDrawingFragmentListener() {


            @Override
            public void onSizeChange(int pWidth, int pHeight) {
                // TODO Auto-generated method stub
                if (pWidth > 0 && pHeight > 0) {
                    viewWidth = pWidth;
                    viewHeight = pHeight;
                }
            }

            @Override
            public void onTouchEvent(short EventId, float pSX, float pSY) {
                if (drawingCb.isChecked()) {
                    if (mSocketThread != null && mSocketThread.isConnected()) {
                        Packet packet = new Packet();
                        packet.mPacketID = EventId;
                        packet.setColor(mTouchView.getPenColor());
                        packet.setPenThickness((int) mTouchView.getThickness());
                        packet.setPenType(0);
                        packet.setXY(pSX, pSY);
                        mSocketThread.sendDrawingEvent(packet);
                    }
                }
            }

            @Override
            public void onChangeModeTitle() {
                // TODO Auto-generated method stub
                if (mTouchView.isEraseMode()) {
                    setTitle("Erase Mode");
                } else {
                    if (drawingCb.isChecked()) {
                        setTitle("Drawing Mode");
                    } else {
                        if (browserCb.isChecked()) {
                            setTitle("Browser Mode");
                        }
                    }
                }
            }
        });

        mControl = new MouseControl(this, mTouchView);

        mSocketThread = new SyncSocket(this, "175.198.74.199", 2315, mHandler);
    }

    private final void initActionbar() {
        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();

        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomActionBar = mInflater.inflate(R.layout.actionbar_custom, null);
        mActionBarTitleTv = (TextView) mCustomActionBar.findViewById(R.id.title_text);
        mActionBarTitleTv.setText("Title");

        browserCb = (CheckBox) mCustomActionBar.findViewById(R.id.cbBrowser);
        drawingCb = (CheckBox) mCustomActionBar.findViewById(R.id.cbDrawing);

        connectBtn = (Button) mCustomActionBar.findViewById(R.id.btConnect);
        disconnectBtn = (Button) mCustomActionBar.findViewById(R.id.btDisconnect);
        disconnectBtn.setEnabled(false);
        connectBtn.setOnClickListener(mActionBarClickListener);
        disconnectBtn.setOnClickListener(mActionBarClickListener);

        browserCb.setOnCheckedChangeListener(mActionBarCheckListener);
        drawingCb.setOnCheckedChangeListener(mActionBarCheckListener);

        mActionBar.setCustomView(mCustomActionBar);
        mActionBar.setDisplayShowCustomEnabled(true);

    }

    private void setTitle(String pTitle) {

        if (pTitle == null) {
            mActionBarTitleTv.setText("Title");
        } else {
            mActionBarTitleTv.setText(pTitle);
        }
    }

    private final OnClickListener mActionBarClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btConnect:
                    onConnect();
                    break;
                case R.id.btDisconnect:
                    onSocketDisconnect();
                    break;
                default:
                    break;
            }

        }
    };

    public void onSocketDisconnect() {
        if (mSocketThread != null) {
            mSocketThread.disconnect();
            disconnectBtn.setEnabled(false);
            connectBtn.setEnabled(true);
            mSocketThread = null;
            Toast.makeText(this, "Disconnected", Toast.LENGTH_LONG).show();
        }
    }

    public void onConnect() {
        if (mSocketThread != null) {
            if (!mSocketThread.isConnected()) {
                if (mSocketThread.getState() == Thread.State.NEW) {
                    mSocketThread.setDaemon(true);
                    mSocketThread.start();
                    connectBtn.setEnabled(false);
                    disconnectBtn.setEnabled(true);
                }
            }
        } else {
            mSocketThread = new SyncSocket(this, "175.198.74.199", 2315, mHandler);
        }
    }

    private final OnCheckedChangeListener mActionBarCheckListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // TODO Auto-generated method stub
            switch (buttonView.getId()) {
                case R.id.cbDrawing:
                    if (isChecked) {
                        setTitle("Drawing Mode");
                        mTouchView.setVisibility(View.VISIBLE);
                    } else {
                        if (browserCb.isChecked()) {
                            setTitle("Browser Mode");
                        } else {
                            setTitle(null);
                        }
                        mTouchView.setVisibility(View.GONE);
                    }
                    break;
                case R.id.cbBrowser:
                    setTitle("Browser Mode");
                    break;
                default:
                    break;
            }
        }
    };

    private void showUrlDialog() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.add_url_dialog, null);

        final EditText mAddUrl = (EditText) dialogView.findViewById(R.id.etAddUrl);

        String title = "Add URL";

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title).setView(dialogView).setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mAddUrl != null) {
                    String addingUrl = mAddUrl.getText().toString();
                    if (addingUrl != null && addingUrl.length() > 0) {
                        mFragment.setUrl(addingUrl, "");
                    }
                }
            }

        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });

        dialog.show();

    }

    private void showColorPickerDialog(int pType) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.color_picker_dialog, null);
        final ColorPickerView mColorPicker = (ColorPickerView) dialogView.findViewById(R.id.colorPicker);
        mColorPicker.setTag(pType);
        final ColorPickerPanelView mColorPannel = (ColorPickerPanelView) dialogView.findViewById(R.id.colorPannel);
        mColorPicker.setOnColorChangedListener(new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int pAlpha, float pHue, float pSat, float pVal) {
                final int color = Color.HSVToColor(pAlpha, new float[]{pHue, pSat, pVal});
                mColorPannel.setColor(color);
            }
        });

        String title = "";
        if (pType == 0) {
            title = "Select Pen Color";
        } else {
            title = "Select Background Color";
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title).setView(dialogView).setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                int color = mColorPannel.getColor();
                int pType = (Integer) mColorPicker.getTag();

                if (pType == 0) {
                    mTouchView.setPenColor(color);
                } else {
                    if (mTouchView != null) {
                        mTouchView.setDefaultCanvas(color);

                        if (mSocketThread != null && mSocketThread.isConnected()) {
                            Packet packet = new Packet();
                            packet.setColor(mTouchView.getBackgroundColor());
                            packet.mPacketID = PacketID.BC_DRAW_CLEAR;
                            mSocketThread.sendClearEvent(packet);
                        }
                    }
                }
            }

        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });

        dialog.show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionbar();
        setContentView(R.layout.activity_main);

        initView();
        initSubView();
        getWindowSize();

        mFragment = new MainFragment(new MainFragment.IMainFragmentListener() {

            @Override
            public void onSendUrl(String pString, String pUSerAgent, short pEventId) {
                // TODO Auto-generated method stub
                Log.e("test", "sendUrl : " + pString);
                if (browserCb.isChecked()) {
                    if (mSocketThread != null && mSocketThread.isConnected()) {
                        mSocketThread.sendNavigationEvent(pString, pUSerAgent);
                    }
                }
            }

        });

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, mFragment).commit();
        }
        mBroadcast = new ConnectionBroadCast();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (mFragment != null && mFragment.isAdded()) {
                if (mFragment.onBackkeyPress()) {
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            showUrlDialog();
            return true;
        } else if (id == R.id.action_filter1) {
            mTouchView.setPenFilterType(0);
            return true;
        }else if (id == R.id.action_filter2) {
            mTouchView.setPenFilterType(1);
            return true;
        }else if (id == R.id.action_filter3) {
            mTouchView.setPenFilterType(2);
            return true;
        }else if (id == R.id.action_filter4) {
            mTouchView.setPenFilterType(3);
            return true;
        }else if (id == R.id.action_filter5) {
            mTouchView.setPenFilterType(4);
            return true;
        }else if (id == R.id.action_filter6) {
            mTouchView.setPenFilterType(5);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy() {
        if (mSocketThread != null) {
            mSocketThread.disconnect();
            mSocketThread = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("syncdrawing.example.com.connectfail");
        registerReceiver(mBroadcast, intentFilter);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        if (mBroadcast != null) {
            unregisterReceiver(mBroadcast);
        }
    }

    public void setTouchEvent(short packId, long eventTime, PointF pXY, int pColor, int pPenThickness) {
        mControl.setTouchAction(packId, eventTime, pXY, pColor, pPenThickness);
    }

    public void setClearView(int color) {
        if (mTouchView != null) {
            mTouchView.setDefaultCanvas(color);
        }
    }

    final Handler mHandler = new Handler(Looper.getMainLooper()) {
        float sx = 0;
        float sy = 0;

        @Override
        public void handleMessage(Message inputMessage) {

            switch (inputMessage.what) {

                case SyncSocket.MessageTypeClass.SIMSOCK_DATA:
                    Packet msg = (Packet) inputMessage.obj;


                    switch (msg.mPacketID) {
                        case PacketID.BC_BROWSER_NAVIGATE:
                            if (browserCb.isChecked()) {
                                // mTouchView.setVisibility(View.GONE);
                                mFragment.setUrl(msg.getUrl(), msg.getUserAgent());
                            }
                            break;
                        case PacketID.BC_DRAW_GESTURE_DOWN:
                            Log.e("DrawingView", "BC_DRAW_GESTURE_DOWN");
                            if (drawingCb.isChecked()) {
                                sx = viewWidth * msg.getX();
                                sy = viewHeight * msg.getY();
                                setTouchEvent(msg.mPacketID, msg.eventTime, new PointF(sx, sy), msg.getPenColor(), msg.getPenThickness());
                            }
                            break;
                        case PacketID.BC_DRAW_GESTURE_MOVE:
                            Log.e("DrawingView", "BC_DRAW_GESTURE_MOVE");
                            if (drawingCb.isChecked()) {
                                sx = viewWidth * msg.getX();
                                sy = viewHeight * msg.getY();
                                setTouchEvent(msg.mPacketID, msg.eventTime, new PointF(sx, sy), msg.getPenColor(), msg.getPenThickness());
                            }
                            break;
                        case PacketID.BC_DRAW_GESTURE_UP:
                            Log.e("DrawingView", "BC_DRAW_GESTURE_UP");
                            if (drawingCb.isChecked()) {
                                sx = viewWidth * msg.getX();
                                sy = viewHeight * msg.getY();
                                setTouchEvent(msg.mPacketID, msg.eventTime, new PointF(sx, sy), msg.getPenColor(), msg.getPenThickness());
                            }
                            break;
                        case PacketID.BC_DRAW_CLEAR:
                            if (drawingCb.isChecked()) {
                                setClearView(msg.colorBackgroudnARGB);
                            }
                            break;
                        default:
                            break;
                    }

                    // do something with UI
                    break;

                case SyncSocket.MessageTypeClass.SIMSOCK_CONNECTED:
                    // do something with UI

                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG).show();
                    break;

                case SyncSocket.MessageTypeClass.SIMSOCK_DISCONNECTED:
                    Toast.makeText(MainActivity.this, "Connect Fail", Toast.LENGTH_LONG).show();
                    break;
                case SyncSocket.MessageTypeClass.SIMSOCK_DISCONNECTED_2:
                    Toast.makeText(MainActivity.this, "Connect Fail2", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    public class ConnectionBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();

            if (action.equals("syncdrawing.example.com.connectfail")) {
                onSocketDisconnect();
            }
        }
    }

}
