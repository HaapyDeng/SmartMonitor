package com.nodemedia.demo;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.nodemedia.NodePlayer;
import cn.nodemedia.NodePlayerDelegate;
import cn.nodemedia.NodePlayerView;
import cn.nodemedia.demo.R;
import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity implements NodePlayerDelegate {

    NodePlayer np;

    private static final int REQUEST_PLAY = 1000;
    private static final String TAG = "TAG";
    private RecyclerView mRecyclerView;
    private int mPos;
    private Cursor mCursor;
    TextureView mSurfaceView, mSurfaceView2;
    ResultReceiver mResultReceiver, mResultReceiver2;


    public String wsUrl = "ws://101.201.28.83:86";
    private LinearLayout customBarChart1, customBarChart2;
    int[] data1 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] data2 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    TextView time_hour, time_year, week;

    private PopupWindowHelper popupWindowHelper, popupWindowHelper2, popupWindowHelper3;
    private View popView, popView2, popView3;
    View view;
    ImageView img_1, img_2, img_3;
    ImageView img1, img2, img3, img4, img5, img6;
    TextView name1, name2, name3, grade1, grade2, grade3;

    LinearLayout text;
    ImageView rl;

    String noticeContent = "", noticeId = "", studentId = "", studentId2 = "", studentId3 = "", studentId4 = "", studentId5 = "";
    String imageUrl, weather;
    Bitmap bitmapWeather;
    WebView webView, webView2;
    int tag = 0;
    String userName = "";
    String userGrade = "";
    String userClass = "";
    int flag = 0;
    int dataflag1 = 0, dataflag2 = 0;

    //    String video1 = "rtsp://admin:Abcd1234@192.168.1.2:554";
//    String video2 = "rtsp://admin:Abcd1234@192.168.1.3:554";
//
//    String video1 = "rtsp://admin:admin@192.168.1.6:554";
//    String video2 = "rtsp://admin:Abcd1234@192.168.1.4:554";
    String video1 = "";
    String video2 = "";
    String faceIp = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File file = new File(Environment.getExternalStorageDirectory(), "/ipconfig/ipconfig.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String readline = "";
            StringBuffer sb = new StringBuffer();
            while ((readline = br.readLine()) != null) {
                System.out.println("readline:" + readline);
                sb.append(readline);
            }
            br.close();
            System.out.println("读取成功：" + sb.toString());
            JSONObject object = new JSONObject(sb.toString());
            video1 = object.getString("video1");
//            video2 = object.getString("video2");
            faceIp = object.getString("ip");
//            System.out.println("video:" + video1 + ":" + video2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Android 6.0及以上并且targetSdkVersion 23及以上,摄像头和麦克风权限需要用代码请求
        //这里使用第三方的权限管理库来请求
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this,
                new PermissionsResultAction() {
                    @Override
                    public void onGranted() {

                    }

                    @Override
                    public void onDenied(String permission) {

                    }
                });

        //创建NodePlayer实例
        np = new NodePlayer(this);
        //查询播放视图
        NodePlayerView npv = (NodePlayerView) findViewById(R.id.nodeplayerview1);
        //设置播放视图的渲染器模式,可以使用SurfaceView或TextureView. 默认SurfaceView
        npv.setRenderType(NodePlayerView.RenderType.SURFACEVIEW);
        //设置视图的内容缩放模式
        npv.setUIViewContentMode(NodePlayerView.UIViewContentMode.ScaleAspectFit);

        //将播放视图绑定到播放器
        np.setPlayerView(npv);
        //设置事件回调代理
        np.setNodePlayerDelegate(this);
        //开启硬件解码,支持4.3以上系统,初始化失败自动切为软件解码,默认开启.
        np.setHWEnable(true);

        /**
         * 设置启动缓冲区时长,单位毫秒.此参数关系视频流连接成功开始获取数据后缓冲区存在多少毫秒后开始播放
         */
        np.setBufferTime(0);
        /**
         * 设置最大缓冲区时长,单位毫秒.此参数关系视频最大缓冲时长.
         * RTMP基于TCP协议不丢包,网络抖动且缓冲区播完,之后仍然会接受到抖动期的过期数据包.
         * 设置改参数,sdk内部会自动清理超出部分的数据包以保证不会存在累计延迟,始终与直播时间线保持最大maxBufferTime的延迟
         */
        np.setMaxBufferTime(1000);

        /**
         * 设置连接超时时长,单位毫秒.默认为0 一直等待.
         * 连接部分RTMP服务器,握手并连接成功后,当播放一个不存在的流地址时,会一直等待下去.
         * 如需超时,设置该值.超时后返回1006状态码.
         */
//        np.setConnectWaitTimeout(10*1000);

        /**
         * @brief rtmpdump 风格的connect参数
         * Append arbitrary AMF data to the Connect message. The type must be B for Boolean, N for number, S for string, O for object, or Z for null.
         * For Booleans the data must be either 0 or 1 for FALSE or TRUE, respectively. Likewise for Objects the data must be 0 or 1 to end or begin an object, respectively.
         * Data items in subobjects may be named, by prefixing the type with 'N' and specifying the name before the value, e.g. NB:myFlag:1.
         * This option may be used multiple times to construct arbitrary AMF sequences. E.g.
         */
//        np.setConnArgs("S:info O:1 NS:uid:10012 NB:vip:1 NN:num:209.12 O:0");


        /**
         * 设置RTSP使用TCP传输模式
         * 支持的模式有:
         * NodePlayer.RTSP_TRANSPORT_UDP
         * NodePlayer.RTSP_TRANSPORT_TCP
         * NodePlayer.RTSP_TRANSPORT_UDP_MULTICAST
         * NodePlayer.RTSP_TRANSPORT_HTTP
         */
//        np.setRtspTransport(NodePlayer.RTSP_TRANSPORT_TCP);

        /**
         * 设置播放直播视频url
         */
        np.setInputUrl(video1);


        /**
         * 在本地开起一个RTMP服务,并进行监听播放,局域网内其他手机或串流器能推流到手机上直接进行播放,无需中心服务器支持
         * 播放的ip可以是本机IP,也可以是0.0.0.0,但不能用127.0.0.1
         * app/stream 可加可不加,只要双方匹配就行
         */
//        np.setLocalRTMP(true);


        /**
         * 开始播放直播视频
         */
        np.start();


        //初始化保存noticeId
        SharedPreferences notice = getSharedPreferences("noticeId", MODE_PRIVATE);
        SharedPreferences.Editor edit = notice.edit(); //编辑文件
        edit.putString("content", "暂时没有通知");
        edit.putString("id", "0");
        edit.commit();  //保存数据信息


        img1 = (ImageView) findViewById(R.id.img1);
        name1 = (TextView) findViewById(R.id.name1);
        grade1 = (TextView) findViewById(R.id.grade1);

        img2 = (ImageView) findViewById(R.id.img2);
        name2 = (TextView) findViewById(R.id.name2);
        grade2 = (TextView) findViewById(R.id.grade2);

        //studentId
        SharedPreferences sId = getSharedPreferences("studentId", MODE_PRIVATE);
        SharedPreferences.Editor edit1 = sId.edit(); //编辑文件
        edit1.putString("id", "0");
        edit1.commit();

        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl("file:///android_asset/html/index.html");
        // 获取到UserAgentString
        String userAgent = webView.getSettings().getUserAgentString();
        // 打印结果
        Log.i("TAG", "User Agent:" + userAgent);

        view = View.inflate(this, R.layout.activity_main, null);
        customBarChart1 = (LinearLayout) findViewById(R.id.customBarChart1);
        customBarChart2 = (LinearLayout) findViewById(R.id.customBarChart2);
        initData();

        initId();
        //时间显示
        time_hour = (TextView) findViewById(R.id.time_hour);
        time_year = (TextView) findViewById(R.id.time_year);
        week = (TextView) findViewById(R.id.week);
        new TimeThread().start();

    }


    private void initId() {
//        String url = "http://192.168.1.122:8080/";
//        String url = "http://172.18.31.222:8080/";
//        String url = "http://192.168.1.222:8080/";
        com.loopj.android.http.AsyncHttpClient client = new com.loopj.android.http.AsyncHttpClient();
        client.get(faceIp, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    JSONArray id = response.getJSONArray("id");
                    Log.d("id", id.toString());
                    if (id.length() == 0) {
//                        new InitIdTimeThread().start();
                        mHandler.sendEmptyMessageDelayed(12, 3000);
                    } else {
                        SharedPreferences getSId = getSharedPreferences("studentId", 0);
                        String sid = getSId.getString("id", "0");
                        Log.d("sid", "......" + sid);
                        switch (1) {
                            case 1:
                                studentId = id.get(0).toString().replace("\"", "");
                                //保存数据信息
                                if (sid.equals(studentId)) {
//                                    new InitIdTimeThread().start();
                                    mHandler.sendEmptyMessageDelayed(12, 3000);
                                } else {
                                    tag = 1;
                                    SharedPreferences sId2 = getSharedPreferences("studentId", MODE_PRIVATE);
                                    SharedPreferences.Editor edit2 = sId2.edit(); //编辑文件
                                    edit2.putString("id", studentId);
                                    edit2.commit();
//                                    Toast.makeText(PlaylistActivity.this, studentId, Toast.LENGTH_LONG).show();
                                    Log.d("studentId", studentId);
                                    popView = LayoutInflater.from(MainActivity.this).inflate(R.layout.popupview, null);
                                    Message msg = new Message();
                                    msg.what = 7;
                                    msg.obj = "1";
                                    mHandler.sendMessage(msg);

                                }
                                break;
                            default:
                                break;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });

    }


    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {

        @SuppressLint("ResourceAsColor")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case 1:
                    img_1 = (ImageView) popView.findViewById(R.id.img_1);
                    img_1.setVisibility(View.VISIBLE);
                    mHandler.sendEmptyMessageDelayed(2, 1500);
                    break;
                case 2:
                    img_1 = (ImageView) popView.findViewById(R.id.img_1);
                    img_1.setVisibility(View.VISIBLE);
                    img_1.setImageDrawable(getResources().getDrawable(R.drawable.img_moshengrenshibei));
                    mHandler.sendEmptyMessageDelayed(3, 2000);
                    break;
                case 3:
                    rl = (ImageView) popView.findViewById(R.id.rl);
                    rl.setVisibility(View.VISIBLE);
                    img_1 = (ImageView) popView.findViewById(R.id.img_1);
                    img_1.setVisibility(View.VISIBLE);
                    if (studentId.length() == 6) {
                        img_1.setImageDrawable(getResources().getDrawable(R.drawable.img_moshengrenbiaoji));
                        text = (LinearLayout) popView.findViewById(R.id.ll_text);
                        text.setVisibility(View.VISIBLE);
                        TextView nameText2 = (TextView) popView.findViewById(R.id.name);
                        TextView gradeText2 = (TextView) popView.findViewById(R.id.grade);
                        userName = "校外人员";
                        nameText2.setText(userName);
                        nameText2.setTextColor(Color.RED);
                        gradeText2.setText("请勿擅自进入");
                        gradeText2.setTextColor(Color.RED);
                        mHandler.sendEmptyMessageDelayed(4, 3000);

                    } else {
                        String sdDir = Environment.getExternalStorageDirectory().getPath();
                        img_1.setImageURI(Uri.fromFile(new File(sdDir + "/school/" + studentId + ".jpg")));
                        String dataa = "{\"1001\":[\"十班\",\"高一\",\"尹佳怡\"],\"1002\":[\"十班\",\"高一\",\"李诺\"],\"1003\":[\"十班\",\"高一\",\"闫璟\"],\"1004\":[\"十班\",\"高一\",\"范朝悦\"],\"1005\":[\"十班\",\"高一\",\"王瀚茹\"],\"1006\":[\"十班\",\"高一\",\"程明轩\"],\"1007\":[\"十班\",\"高一\",\"周欣语\"],\"1008\":[\"十班\",\"高一\",\"陈桦\"],\"1009\":[\"十班\",\"高一\",\"杨灿\"],\"1010\":[\"十班\",\"高一\",\"古思融\"],\"1011\":[\"十班\",\"高一\",\"张颖慧\"],\"1012\":[\"十班\",\"高一\",\"杨忻缘\"],\"1013\":[\"十班\",\"高一\",\"赵嘉宝\"],\"1101\":[\"十一班\",\"高一\",\"高煜\"],\"1102\":[\"十一班\",\"高一\",\"于思萌\"],\"1103\":[\"十一班\",\"高一\",\"邵璟琦\"],\"1104\":[\"十一班\",\"高一\",\"苑佳慧\"],\"1105\":[\"十一班\",\"高一\",\"唐昕\"],\"1106\":[\"十一班\",\"高一\",\"李雅睿\"],\"1107\":[\"十一班\",\"高一\",\"张瑛琦\"],\"1108\":[\"十一班\",\"高一\",\"陈曦\"],\"1109\":[\"十一班\",\"高一\",\"兰钰洁\"],\"1110\":[\"十一班\",\"高一\",\"张百盈\"],\"1111\":[\"十一班\",\"高一\",\"王欣怡\"],\"1112\":[\"十一班\",\"高一\",\"白晟源\"],\"1113\":[\"十一班\",\"高一\",\"董莹莹\"],\"1114\":[\"十一班\",\"高一\",\"褚宇轩\"],\"1115\":[\"十一班\",\"高一\",\"南士圆\"],\"1116\":[\"十一班\",\"高一\",\"宋佳妍\"],\"1117\":[\"十一班\",\"高一\",\"孙淑雨\"],\"1201\":[\"十二班\",\"高一\",\"张以宁\"],\"1202\":[\"十二班\",\"高一\",\"白梦凡\"],\"1203\":[\"十二班\",\"高一\",\"陈薇\"],\"1204\":[\"十二班\",\"高一\",\"陈安琪\"],\"1205\":[\"十二班\",\"高一\",\"张洋\"],\"1206\":[\"十二班\",\"高一\",\"刘丹\"],\"1207\":[\"十二班\",\"高一\",\"马珊珊\"],\"1208\":[\"十二班\",\"高一\",\"白琳萱\"],\"1209\":[\"十二班\",\"高一\",\"赵伟宏\"],\"1210\":[\"十二班\",\"高一\",\"潘诗蕊\"],\"1211\":[\"十二班\",\"高一\",\"白倩\"],\"1212\":[\"十二班\",\"高一\",\"尹正聪\"],\"1213\":[\"十二班\",\"高一\",\"张欣怡\"],\"1214\":[\"十二班\",\"高一\",\"王宇杰\"],\"1215\":[\"十二班\",\"高一\",\"田琪\"],\"1301\":[\"十三班\",\"高一\",\"吴曼\"],\"1302\":[\"十三班\",\"高一\",\"李金红\"],\"1303\":[\"十三班\",\"高一\",\"赵紫逸\"],\"1304\":[\"十三班\",\"高一\",\"张卜凡\"],\"1305\":[\"十三班\",\"高一\",\"杨皓玉\"],\"1306\":[\"十三班\",\"高一\",\"李文骏\"],\"1307\":[\"十三班\",\"高一\",\"郭慧琪\"],\"1308\":[\"十三班\",\"高一\",\"王海悦\"],\"1309\":[\"十三班\",\"高一\",\"周晗\"],\"1310\":[\"十三班\",\"高一\",\"王艳桐\"],\"1311\":[\"十三班\",\"高一\",\"李文璐\"],\"1312\":[\"十三班\",\"高一\",\"殷梦楠\"],\"1313\":[\"十三班\",\"高一\",\"王玉\"],\"1314\":[\"十三班\",\"高一\",\"李晗\"],\"1315\":[\"十三班\",\"高一\",\"张乐孜\"],\"1401\":[\"十四班\",\"高一\",\"李曼宁\"],\"1402\":[\"十四班\",\"高一\",\"陈梓祎\"],\"1403\":[\"十四班\",\"高一\",\"张琪\"],\"1404\":[\"十四班\",\"高一\",\"张金璐\"],\"1405\":[\"十四班\",\"高一\",\"宋姝娴\"],\"1406\":[\"十四班\",\"高一\",\"张月娇\"],\"1407\":[\"十四班\",\"高一\",\"陈星竹\"],\"1408\":[\"十四班\",\"高一\",\"周美诗\"],\"1409\":[\"十四班\",\"高一\",\"邹蕾\"],\"1410\":[\"十四班\",\"高一\",\"周彤\"],\"1411\":[\"十四班\",\"高一\",\"潘旌阁\"],\"1412\":[\"十四班\",\"高一\",\"刘广茁\"],\"1413\":[\"十四班\",\"高一\",\"卢灿\"],\"1414\":[\"十四班\",\"高一\",\"王甜甜\"],\"1415\":[\"十四班\",\"高一\",\"翟鸿颖\"],\"1501\":[\"十五班\",\"高一\",\"刘文萱\"],\"1502\":[\"十五班\",\"高一\",\"王子一\"],\"1503\":[\"十五班\",\"高一\",\"冯爽\"],\"1504\":[\"十五班\",\"高一\",\"王美琪\"],\"1505\":[\"十五班\",\"高一\",\"杨文宣\"],\"1506\":[\"十五班\",\"高一\",\"王佳佳\"],\"1507\":[\"十五班\",\"高一\",\"韩雅\"],\"1508\":[\"十五班\",\"高一\",\"张秋\"],\"1601\":[\"十六班\",\"高一\",\"何静雅\"],\"1602\":[\"十六班\",\"高一\",\"窦佳怡\"],\"1603\":[\"十六班\",\"高一\",\"李金津\"],\"1604\":[\"十六班\",\"高一\",\"张雪晴\"],\"1605\":[\"十六班\",\"高一\",\"赵欣雨\"],\"1606\":[\"十六班\",\"高一\",\"商诗玉\"],\"1607\":[\"十六班\",\"高一\",\"吕姝怡\"],\"1608\":[\"十六班\",\"高一\",\"何斯诺\"],\"1609\":[\"十六班\",\"高一\",\"房冰杰\"],\"1610\":[\"十六班\",\"高一\",\"崔玉鑫\"],\"1611\":[\"十六班\",\"高一\",\"张艺萱\"],\"1612\":[\"十六班\",\"高一\",\"赵馨一\"],\"1613\":[\"十六班\",\"高一\",\"李洪运\"],\"1614\":[\"十六班\",\"高一\",\"冯冉\"],\"1615\":[\"十六班\",\"高一\",\"焦俊杨\"],\"1701\":[\"十七班\",\"高一\",\"蒙建文\"],\"1702\":[\"十七班\",\"高一\",\"张秀怡\"],\"1703\":[\"十七班\",\"高一\",\"方新爽\"],\"1704\":[\"十七班\",\"高一\",\"高蕊\"],\"1705\":[\"十七班\",\"高一\",\"邓锦佳\"],\"1706\":[\"十七班\",\"高一\",\"马帅\"],\"1707\":[\"十七班\",\"高一\",\"马然\"],\"1708\":[\"十七班\",\"高一\",\"尹欣璐\"],\"1709\":[\"十七班\",\"高一\",\"李畅\"],\"1710\":[\"十七班\",\"高一\",\"唐玉琪\"],\"1711\":[\"十七班\",\"高一\",\"郑思琪\"],\"1801\":[\"十八班\",\"高一\",\"石瑞晓\"],\"1802\":[\"十八班\",\"高一\",\"庞子倩\"],\"1803\":[\"十八班\",\"高一\",\"杨楠\"],\"1804\":[\"十八班\",\"高一\",\"牛建晴\"],\"1805\":[\"十八班\",\"高一\",\"闫宇彤\"],\"1806\":[\"十八班\",\"高一\",\"孙蕊\"],\"1807\":[\"十八班\",\"高一\",\"张欢\"],\"1808\":[\"十八班\",\"高一\",\"闫鹔\"],\"1809\":[\"十八班\",\"高一\",\"张紫璇\"],\"1810\":[\"十八班\",\"高一\",\"席靖\"],\"1811\":[\"十八班\",\"高一\",\"周晶\"],\"0101\":[\"一班\",\"高一\",\"张心怡\"],\"0102\":[\"一班\",\"高一\",\"张桂媛\"],\"0103\":[\"一班\",\"高一\",\"林璐\"],\"0104\":[\"一班\",\"高一\",\"岳博傲\"],\"0105\":[\"一班\",\"高一\",\"王伶怡\"],\"0106\":[\"一班\",\"高一\",\"程菲\"],\"0107\":[\"一班\",\"高一\",\"陈思\"],\"0108\":[\"一班\",\"高一\",\"杨荣慧\"],\"0201\":[\"二班\",\"高一\",\"曹英琦\"],\"0202\":[\"二班\",\"高一\",\"林欣悦\"],\"0203\":[\"二班\",\"高一\",\"马静怡\"],\"0204\":[\"二班\",\"高一\",\"曹鑫愿\"],\"0205\":[\"二班\",\"高一\",\"王晨\"],\"0206\":[\"二班\",\"高一\",\"孙晓雨\"],\"0207\":[\"二班\",\"高一\",\"于瀛\"],\"0208\":[\"二班\",\"高一\",\"王琳\"],\"0209\":[\"二班\",\"高一\",\"关彤\"],\"0210\":[\"二班\",\"高一\",\"刘欣雨\"],\"0301\":[\"三班\",\"高一\",\"王昕蕊\"],\"0302\":[\"三班\",\"高一\",\"李雅茜\"],\"0303\":[\"三班\",\"高一\",\"段蕊\"],\"0304\":[\"三班\",\"高一\",\"焦雅萱\"],\"0305\":[\"三班\",\"高一\",\"杨硕\"],\"0306\":[\"三班\",\"高一\",\"王天琪\"],\"0307\":[\"三班\",\"高一\",\"杜晨\"],\"0308\":[\"三班\",\"高一\",\"李梦晨\"],\"0309\":[\"三班\",\"高一\",\"赵铭希\"],\"0310\":[\"三班\",\"高一\",\"郭骏\"],\"0311\":[\"三班\",\"高一\",\"马双双\"],\"0312\":[\"三班\",\"高一\",\"孙浩然\"],\"0313\":[\"三班\",\"高一\",\"陈佳蔚\"],\"0314\":[\"三班\",\"高一\",\"赵雅洁\"],\"0315\":[\"三班\",\"高一\",\"孙雨轩\"],\"0401\":[\"四班\",\"高一\",\"李畅\"],\"0402\":[\"四班\",\"高一\",\"王靖瑶\"],\"0403\":[\"四班\",\"高一\",\"白馨怡\"],\"0404\":[\"四班\",\"高一\",\"王君妍\"],\"0405\":[\"四班\",\"高一\",\"倪彬钰\"],\"0406\":[\"四班\",\"高一\",\"卜玉柱\"],\"0407\":[\"四班\",\"高一\",\"董乐瑶\"],\"0408\":[\"四班\",\"高一\",\"王皓月\"],\"0409\":[\"四班\",\"高一\",\"单子湘\"],\"0410\":[\"四班\",\"高一\",\"杜鑫宇\"],\"0411\":[\"四班\",\"高一\",\"李欣芸\"],\"0412\":[\"四班\",\"高一\",\"崔玉涛\"],\"0413\":[\"四班\",\"高一\",\"马蓉蓉\"],\"0414\":[\"四班\",\"高一\",\"张海柔\"],\"0415\":[\"四班\",\"高一\",\"李鹤\"],\"0501\":[\"五班\",\"高一\",\"王丽颖\"],\"0502\":[\"五班\",\"高一\",\"孙胜男\"],\"0503\":[\"五班\",\"高一\",\"赵淑雅\"],\"0504\":[\"五班\",\"高一\",\"白玉鸿\"],\"0505\":[\"五班\",\"高一\",\"薛研\"],\"0506\":[\"五班\",\"高一\",\"杨允\"],\"0507\":[\"五班\",\"高一\",\"刘雨轩\"],\"0508\":[\"五班\",\"高一\",\"芮瑞\"],\"0509\":[\"五班\",\"高一\",\"刘卓琦\"],\"0510\":[\"五班\",\"高一\",\"张延静\"],\"0511\":[\"五班\",\"高一\",\"李鑫伟\"],\"0512\":[\"五班\",\"高一\",\"尹丽雪\"],\"0513\":[\"五班\",\"高一\",\"刘畅\"],\"0514\":[\"五班\",\"高一\",\"张相文\"],\"0515\":[\"五班\",\"高一\",\"王健怡\"],\"0516\":[\"五班\",\"高一\",\"王帆\"],\"0517\":[\"五班\",\"高一\",\"赵盼\"],\"0518\":[\"五班\",\"高一\",\"周一凡\"],\"0601\":[\"六班\",\"高一\",\"马英\"],\"0602\":[\"六班\",\"高一\",\"王峥\"],\"0603\":[\"六班\",\"高一\",\"房佳懿\"],\"0604\":[\"六班\",\"高一\",\"苏怡\"],\"0605\":[\"六班\",\"高一\",\"孙璐彤\"],\"0606\":[\"六班\",\"高一\",\"吴然\"],\"0607\":[\"六班\",\"高一\",\"袁紫君\"],\"0608\":[\"六班\",\"高一\",\"闫雪菲\"],\"0609\":[\"六班\",\"高一\",\"王婧瑀\"],\"0610\":[\"六班\",\"高一\",\"何蕊\"],\"0611\":[\"六班\",\"高一\",\"王月颖\"],\"0701\":[\"七班\",\"高一\",\"吴会芮\"],\"0702\":[\"七班\",\"高一\",\"戴梦媛\"],\"0703\":[\"七班\",\"高一\",\"徐煜萍\"],\"0704\":[\"七班\",\"高一\",\"吕宏骏\"],\"0705\":[\"七班\",\"高一\",\"梅雨欣\"],\"0706\":[\"七班\",\"高一\",\"周雨轩\"],\"0707\":[\"七班\",\"高一\",\"孙瑶\"],\"0708\":[\"七班\",\"高一\",\"王艺杰\"],\"0709\":[\"七班\",\"高一\",\"王晓磊\"],\"0710\":[\"七班\",\"高一\",\"张婧妍\"],\"0711\":[\"七班\",\"高一\",\"杨蔓\"],\"0712\":[\"七班\",\"高一\",\"杜亚晴\"],\"0713\":[\"七班\",\"高一\",\"王茜\"],\"0714\":[\"七班\",\"高一\",\"李德宇\"],\"0715\":[\"七班\",\"高一\",\"蒋薇\"],\"0716\":[\"七班\",\"高一\",\"郭雯晴\"],\"0717\":[\"七班\",\"高一\",\"王志彤\"],\"0718\":[\"七班\",\"高一\",\"杨梦迪\"],\"0801\":[\"八班\",\"高一\",\"孙萍\"],\"0802\":[\"八班\",\"高一\",\"薛天琪\"],\"0803\":[\"八班\",\"高一\",\"张雨琪\"],\"0804\":[\"八班\",\"高一\",\"郝楚曦\"],\"0805\":[\"八班\",\"高一\",\"杨雨晨\"],\"0806\":[\"八班\",\"高一\",\"张习\"],\"0807\":[\"八班\",\"高一\",\"刘秀晴\"],\"0808\":[\"八班\",\"高一\",\"杨文茹\"],\"0809\":[\"八班\",\"高一\",\"刘文玉\"],\"0810\":[\"八班\",\"高一\",\"刘佳怡\"],\"0811\":[\"八班\",\"高一\",\"王雨潇\"],\"0901\":[\"九班\",\"高一\",\"王茗萱\"],\"0902\":[\"九班\",\"高一\",\"武彦颍\"],\"0903\":[\"九班\",\"高一\",\"齐月\"],\"0904\":[\"九班\",\"高一\",\"芮爽\"],\"0905\":[\"九班\",\"高一\",\"赵彤\"],\"0906\":[\"九班\",\"高一\",\"马艺杭\"],\"0907\":[\"九班\",\"高一\",\"李想\"],\"0908\":[\"九班\",\"高一\",\"王璐瑶\"],\"0909\":[\"九班\",\"高一\",\"孙杨\"],\"0910\":[\"九班\",\"高一\",\"任冠熹\"],\"0911\":[\"九班\",\"高一\",\"许阳\"],\"0912\":[\"九班\",\"高一\",\"李天晴\"],\"0913\":[\"九班\",\"高一\",\"齐伟然\"],\"0914\":[\"九班\",\"高一\",\"张楠\"]}";
                        try {
                            JSONObject info = new JSONObject(dataa);
                            Log.d("infoa", info.toString());
                            JSONArray array = info.getJSONArray(studentId);
                            Log.d("array", array.toString());
                            userName = array.get(2).toString();
                            userGrade = array.get(1).toString();
                            userClass = array.get(0).toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        text = (LinearLayout) popView.findViewById(R.id.ll_text);
                        text.setVisibility(View.VISIBLE);
                        TextView nameText = (TextView) popView.findViewById(R.id.name);
                        TextView gradeText = (TextView) popView.findViewById(R.id.grade);
                        nameText.setText(userName);
                        gradeText.setText(userGrade + " · " + userClass);
                        mHandler.sendEmptyMessageDelayed(4, 3000);
                    }

                    break;
                case 4:
//
                    popupWindowHelper.dismiss();
                    flag = flag + 1;
                    switch (flag % 2) {
                        case 1:
                            LinearLayout ll_11 = (LinearLayout) findViewById(R.id.ll__11);
                            ll_11.setVisibility(View.VISIBLE);
                            if (userName.equals("校外人员")) {
                                img1.setImageDrawable(getResources().getDrawable(R.drawable.img_moshengrenbiaoji));
                                name1.setText(userName);
                                grade1.setText("请勿擅自进入");
                                name1.setTextColor(Color.RED);
                                grade1.setTextColor(Color.RED);
                            } else {
                                String sdDir2 = Environment.getExternalStorageDirectory().getPath();
                                img1.setImageURI(Uri.fromFile(new File(sdDir2 + "/school/" + studentId + ".jpg")));
                                name1.setText(userName);
                                name1.setTextColor(Color.WHITE);
                                grade1.setText(userGrade + " · " + userClass);
                                grade1.setTextColor(Color.WHITE);

                            }
                            break;
                        case 0:
                            LinearLayout ll_12 = (LinearLayout) findViewById(R.id.ll__12);
                            ll_12.setVisibility(View.VISIBLE);
                            if (userName.equals("校外人员")) {
                                img2.setImageDrawable(getResources().getDrawable(R.drawable.img_moshengrenbiaoji));
                                name2.setText(userName);
                                grade2.setText("请勿擅自进入");
                                name2.setTextColor(Color.RED);
                                grade2.setTextColor(Color.RED);
                            } else {
                                String sdDir2 = Environment.getExternalStorageDirectory().getPath();
                                img2.setImageURI(Uri.fromFile(new File(sdDir2 + "/school/" + studentId + ".jpg")));
                                name2.setText(userName);
                                name2.setTextColor(Color.WHITE);
                                grade2.setText(userGrade + " · " + userClass);
                                grade2.setTextColor(Color.WHITE);

                            }
                            break;
                    }
//
                    if (tag == 1) {
                        mHandler.sendEmptyMessageDelayed(12, 3000);
                    }
                    break;
                case 5:
                    popupWindowHelper.dismiss();
                    if (tag == 1) {
                        mHandler.sendEmptyMessageDelayed(12, 3000);
                    }
                    break;
                case 6:
                    long time = System.currentTimeMillis();
                    Date date = new Date(time);
                    SimpleDateFormat format1 = new SimpleDateFormat("HH:mm");
                    SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
                    final Calendar c = Calendar.getInstance();
                    String mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
                    if ("1".equals(mWay)) {
                        mWay = "周日";
                    } else if ("2".equals(mWay)) {
                        mWay = "周一";
                    } else if ("3".equals(mWay)) {
                        mWay = "周二";
                    } else if ("4".equals(mWay)) {
                        mWay = "周三";
                    } else if ("5".equals(mWay)) {
                        mWay = "周四";
                    } else if ("6".equals(mWay)) {
                        mWay = "周五";
                    } else if ("7".equals(mWay)) {
                        mWay = "周六";
                    }
                    time_hour.setText(format1.format(date));
                    time_year.setText(format2.format(date));
                    week.setText(mWay);
                    break;
                case 7:
                    int y = 0;
                    popView.setPadding(50, 0, 0, 0);
                    if (msg.obj.equals("1")) {
                        y = 120;
                        popupWindowHelper = new PopupWindowHelper(popView);
                        popupWindowHelper.showFromTopLeft(view, y);
                        mHandler.sendEmptyMessageDelayed(1, 300);
                    } else if (msg.obj.equals("2")) {
                        popView2.setPadding(50, 0, 0, 0);
                        y = 200;
                        popupWindowHelper2 = new PopupWindowHelper(popView2);
                        popupWindowHelper2.showFromTopLeft(view, y);
                        mHandler.sendEmptyMessageDelayed(11, 300);
                    } else if (msg.obj.equals("3")) {
                        popView3.setPadding(50, 0, 0, 0);
                        y = 280;
                        popupWindowHelper3 = new PopupWindowHelper(popView3);
                        popupWindowHelper3.showFromTopLeft(view, y);
                        mHandler.sendEmptyMessageDelayed(111, 300);
                    }

                    break;
                case 8:
                    Log.d("noticeContent", noticeContent + "dfdfdfd");
                    MarqureeTextView noticeText = (MarqureeTextView) findViewById(R.id.notice);
                    noticeText.setText(noticeContent);
                    break;
                case 9:
                    ImageView tianqi_img = (ImageView) findViewById(R.id.tianqi_img);
                    TextView tianqi_text = (TextView) findViewById(R.id.tianqi_text);

                    tianqi_img.setImageBitmap(bitmapWeather);
                    tianqi_text.setText(weather);
                    break;
                case 10:
//                    new InitIdTimeThread().start();
                    mHandler.sendEmptyMessageDelayed(12, 3000);
                    break;
                case 11:
                    Log.d("data1:", data1.toString());
                    Log.d("data2:", data2.toString());
                    initBarChart1();
                    initBarChart2();
                    break;
                case 12:
                    initId();
                    break;
                default:
                    break;
            }
        }
    };

    public class TimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = 6;
                    mHandler.sendMessage(msg);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }


    public void initData() {
        AsyncHttpClient.getDefaultInstance().websocket(wsUrl, "my-protocol", new AsyncHttpClient.WebSocketConnectCallback() {

            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }
                Log.d("mac==>>", jsonMacData());
                webSocket.send(jsonMacData());
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override
                    public void onStringAvailable(String s) {
                        System.out.println("I got a string: " + s);
                        try {
                            JSONObject object = new JSONObject(s);
                            if (object.has("data")) {
                                JSONObject dataObject = object.getJSONObject("data");
                                //获取天气情况
                                JSONObject weatherinfoObject = dataObject.getJSONObject("weatherinfo");
                                if (!weatherinfoObject.has("status")) {
                                    imageUrl = weatherinfoObject.getString("img2");
                                    weather = weatherinfoObject.getString("weather");
                                    Log.d("weather", weather);
                                    if (weather.equals("晴")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.qing);
                                    } else if (weather.equals("多云")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.duoyun);
                                    } else if (weather.equals("阴")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.yin);
                                    } else if (weather.equals("阵雨")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.zhenyu);
                                    } else if (weather.equals("小雨")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.xiaoyu);
                                    } else if (weather.equals("中雨")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.zhongyu);
                                    } else if (weather.equals("大雨")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.dayu);
                                    } else if (weather.equals("雷阵雨")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.leizhenyu);
                                    } else if (weather.equals("暴雨")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.baoyu);
                                    } else if (weather.equals("大暴雨")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.dabaoyu);
                                    } else if (weather.equals("特大暴雨")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.tedabaoyu);
                                    } else if (weather.equals("雨夹雪")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.yujiaxue);
                                    } else if (weather.equals("阵雪")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.zhenxue);
                                    } else if (weather.equals("小雪")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.xiaoxue);
                                    } else if (weather.equals("中雪")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.zhongxue);
                                    } else if (weather.equals("大雪")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.daxue);
                                    } else if (weather.equals("暴雪")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.baoxue);
                                    } else if (weather.equals("雾")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.wu);
                                    } else if (weather.equals("雾霾")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.wumai);
                                    } else if (weather.equals("冰雹和冻雪")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.bingbaohedongyu);
                                    } else if (weather.equals("沙尘暴")) {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.shaochengbao);
                                    } else {
                                        bitmapWeather = BitmapFactory.decodeResource(getResources(), R.drawable.duoyun);
                                    }
                                    Message msg = new Message();
                                    msg.what = 9;
                                    mHandler.sendMessage(msg);
                                }
                                //获取0-23小时进入和离开的人数
                                final JSONArray jsonArray = dataObject.getJSONArray("hoursStatistics");
//                                new drawThread().start();
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject jsondata = null;
                                            try {
                                                jsondata = jsonArray.getJSONObject(i);
                                                data1[i] = jsondata.getInt("in") * 20;
                                                data2[i] = jsondata.getInt("out") * 20;
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        Message msg = new Message();
                                        msg.what = 11;
                                        mHandler.sendMessage(msg);

                                    }
                                });

                                thread.start();

                                //获取通知
                                SharedPreferences getId = getSharedPreferences("noticeId", 0);
                                String id = getId.getString("id", "0");
                                JSONObject noticeObject = dataObject.getJSONObject("notice");
                                if (noticeObject.getInt("status") == 1) {
                                    JSONObject list = noticeObject.getJSONObject("list");
                                    if (list.getString("id").equals("" + id)) {
//                                        Log.d("noticeObject", "");
//                                        noticeContent = getId.getString("content", "0");
//                                        Message msg = new Message();
//                                        msg.what = 8;
//                                        mHandler.sendEmptyMessageDelayed(8,2000);
                                    } else {
                                        noticeId = list.getString("id");
                                        noticeContent = list.getString("content");
                                        SharedPreferences notice = getSharedPreferences("noticeId", MODE_PRIVATE);
                                        SharedPreferences.Editor edit = notice.edit(); //编辑文件
                                        edit.putString("id", noticeId);
                                        edit.putString("content", noticeContent);
                                        edit.commit();


                                        Log.d("noticeContent", noticeContent + ":" + noticeId + ":" + id);

                                        if (!noticeId.equals(id)) {
                                            Message msg = new Message();
                                            msg.what = 8;
                                            mHandler.sendEmptyMessageDelayed(8, 2000);
                                        }
                                    }
                                } else {
                                    noticeContent = getId.getString("content", "0");
//                                    Log.d("noticeContent", noticeContent + ":" + noticeId + ":" + id);
//                                    Message msg = new Message();
//                                    msg.what = 8;
//                                    mHandler.sendEmptyMessageDelayed(8,2000);
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }


    /**
     * 初始化柱状图1数据
     */
    private void initBarChart1() {

        if (dataflag1 == 1) {
            Log.d("view1","remove!!!");
            customBarChart1.removeView(customBarChart1);
        }

        String[] xLabel = {"0", "00:00", "", "", "", "", "", "07:00", "", "", "", "", "12:00", "",
                "", "", "", "", "18:00", "", "", "", "22:00", "", ""};
        String[] yLabel = {"0", "0", "0", "0", "0", "0", "0", "0"};

        List<int[]> data = new ArrayList<>();
        data.add(data1);
        List<Integer> color = new ArrayList<>();
        color.add(R.color.color12);
        color.add(R.color.color16);
        color.add(R.color.color16);
        customBarChart1.addView(new CustomBarChart(this, xLabel, yLabel, data, color));
        dataflag1 = 1;
    }

    /**
     * 初始化柱状图2数据
     */
    private void initBarChart2() {
        if (dataflag2 == 1) {
            Log.d("view2","remove!!!");
            customBarChart2.removeView(customBarChart2);
        }

        String[] xLabel = {"0", "00:00", "", "", "", "", "", "07:00", "", "", "", "", "12:00", "",
                "", "", "", "", "18:00", "", "", "", "22:00", "", ""};
        String[] yLabel = {"0", "0", "0", "0", "0", "0", "0", "0"};
//        int[] data2 = {3, 5, 5, 5, 3, 7, 8, 7, 5, 6, 4, 3, 4, 6, 5,
//                7, 3, 5, 5, 5, 3, 7, 8, 7, 8};
        List<int[]> data = new ArrayList<>();
        data.add(data2);
        List<Integer> color = new ArrayList<>();
        color.add(R.color.color12);
        color.add(R.color.color16);
        color.add(R.color.color16);
        customBarChart2.addView(new CustomBarChart(this, xLabel, yLabel, data, color));
        dataflag2 = 1;
    }

    public String jsonMacData() {
        String jsonresult = "";//定义返回字符串
//        JSONObject object = new JSONObject();//创建一个总的对象，这个对象对整个json串
        try {
            JSONArray jsonarray = new JSONArray();//json数组，里面包含的内容为pet的所有对象
            JSONObject jsonObj = new JSONObject();//pet对象，json形式
            jsonObj.put("sushe", "1_1");//向pet对象里面添加值
            // 把每个数据当作一对象添加到数组里
//            jsonarray.put(jsonObj);//向json数组里面添加pet对象
//            object.put("data", jsonarray);//向总对象里面添加包含pet的数组
            jsonresult = jsonObj.toString();//生成返回字符串
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("生成的json串为:", jsonresult);
        return jsonresult;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**
         * 停止播放
         */
        np.stop();

        /**
         * 释放资源
         */
        np.release();
    }


    @Override
    public void onEventCallback(NodePlayer nodePlayer, int event, String msg) {
        Log.i("NodeMedia.NodePlayer", "onEventCallback:" + event + " msg:" + msg);

        switch (event) {
            case 1000:
                // 正在连接视频
                break;
            case 1001:
                // 视频连接成功
                break;
            case 1002:
                // 视频连接失败 流地址不存在，或者本地网络无法和服务端通信，回调这里。5秒后重连， 可停止
//                nodePlayer.stopPlay();
                break;
            case 1003:
                // 视频开始重连,自动重连总开关
//                nodePlayer.stopPlay();
                break;
            case 1004:
                // 视频播放结束
                break;
            case 1005:
                // 网络异常,播放中断,播放中途网络异常，回调这里。1秒后重连，如不需要，可停止
//                nodePlayer.stopPlay();
                break;
            case 1006:
                //RTMP连接播放超时
                break;
            case 1100:
                // 播放缓冲区为空
//				System.out.println("NetStream.Buffer.Empty");
                break;
            case 1101:
                // 播放缓冲区正在缓冲数据,但还没达到设定的bufferTime时长
//				System.out.println("NetStream.Buffer.Buffering");
                break;
            case 1102:
                // 播放缓冲区达到bufferTime时长,开始播放.
                // 如果视频关键帧间隔比bufferTime长,并且服务端没有在缓冲区间内返回视频关键帧,会先开始播放音频.直到视频关键帧到来开始显示画面.
//				System.out.println("NetStream.Buffer.Full");
                break;
            case 1103:
//				System.out.println("Stream EOF");
                // 客户端明确收到服务端发送来的 StreamEOF 和 NetStream.Play.UnpublishNotify时回调这里
                // 注意:不是所有云cdn会发送该指令,使用前请先测试
                // 收到本事件，说明：此流的发布者明确停止了发布，或者因其网络异常，被服务端明确关闭了流
                // 本sdk仍然会继续在1秒后重连，如不需要，可停止
//                nodePlayer.stopPlay();
                break;
            case 1104:
                //解码后得到的视频高宽值 格式为:{width}x{height}
                break;
            default:
                break;
        }
    }
}
