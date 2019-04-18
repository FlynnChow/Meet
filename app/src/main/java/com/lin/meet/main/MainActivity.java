package com.lin.meet.main;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.lin.meet.R;
import com.lin.meet.bean.User;
import com.lin.meet.camera_demo.CameraActivity;
import com.lin.meet.login.StartActivity;
import com.lin.meet.main.fragment.Book.Book;
import com.lin.meet.main.fragment.Find.Find;
import com.lin.meet.main.fragment.Home.Home;
import com.lin.meet.main.fragment.Know.Know;
import com.lin.meet.my_util.MyUtil;
import com.lin.meet.personal.PersonalActivity;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,MainConstract.View {
    public static String savePath = Environment.getExternalStorageDirectory().getAbsoluteFile()+ File.separator+"Mybitmap"+File.separator+"Cache"+File.separator;
    private boolean isLogin = false;//是否处于登录状态
    private DataBase dataBase;
    private ImageView imageView;
    private BottomNavigationView bv;
    private NavigationView nv;
    private Fragment fragments[];
    private FloatingActionButton faButton;
    private FrameLayout animator_layout;
    private Handler handler;
    private int lastShow;
    private DrawerLayout drawer;
    private CircleImageView header;
    private TextView name;
    private FloatingActionButton actionButton;
    private User user;
    private RequestOptions options;
    private MainConstract.Presenter presenter;
    private RelativeLayout headLayout;
    private ImageView headBackground;
    private Book book = new Book();
    private Find find = new Find();
    private Home home = new Home();
    private Know know = new Know();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initLoadUserView();
        startActivity(new Intent(this,HelloTest.class));
    }

    private void initView(){
        presenter = new MainPresenter(this);
        request_permissions();
        handler = new Handler();
        faButton = (FloatingActionButton)findViewById(R.id.open_camera_activity);
        animator_layout = (FrameLayout)findViewById(R.id.animator_layout);
        actionButton = (FloatingActionButton)findViewById(R.id.open_camera_activity);
        actionButton.setOnClickListener(this);
        options = new RequestOptions();
        options.skipMemoryCache(true);
        options.diskCacheStrategy(DiskCacheStrategy.NONE);
        options.error(R.color.bank_FF6C6C6C);

        initFragment();
        dataBase = new DataBaseModel();
        bv = (BottomNavigationView)findViewById(R.id.main_bnv);
        bv.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        bv.setItemIconTintList(null);
        bv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                startFabAnimation();
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                switch (menuItem.getItemId()){
                    case R.id.item_home:
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        if(lastShow==0)
                            return true;
                        switchFragment(0);
                        break;
                    case R.id.item_book:
                        if(lastShow==1)
                            return true;
                        switchFragment(1);
                        break;
                    case R.id.item_know:
                        if(lastShow==2)
                            return true;
                        switchFragment(2);
                        break;
                    case R.id.item_find:
                        if(lastShow==3)
                            return true;
                        switchFragment(3);
                        break;
                }
                return true;
            }
        });
        drawer = (DrawerLayout) findViewById(R.id.main_drawer);
        nv = (NavigationView) findViewById(R.id.main_nv);
        headLayout = (RelativeLayout) nv.getHeaderView(0);
        headBackground = (ImageView) headLayout.findViewById(R.id.user_background);
        header = (CircleImageView)headLayout.findViewById(R.id.user_header);
        name = (TextView)headLayout.findViewById(R.id.user_name);
        headLayout.setOnClickListener(this);
        checkCacheFile();
    }

    private void initLoginData(){
        SharedPreferences preferences = MyUtil.getShardPreferences(this,"LoginToken");

    }

    private static final String TAG = "MainActivity";

    private void initFragment(){
        book = new Book();
        find = new Find();
        home = new Home();
        know = new Know();
        fragments = new Fragment[]{home,book,know,find};
        lastShow = 0;
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_content,home)
                .show(home)
                .commit();
    }

    private void switchFragment(int index){
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.hide(fragments[lastShow]);
        if(!fragments[index].isAdded())
            transaction.add(R.id.main_content,fragments[index]);
        transaction.show(fragments[index]).commitAllowingStateLoss();
        lastShow = index;
    }

    private void request_permissions() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {

                        int grantResult = grantResults[i];
                        if (grantResult == PackageManager.PERMISSION_DENIED) {
                            String s = permissions[i];
                            Toast.makeText(this, s + " permission was denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==1){
            initLoadUserView();
        }
    }



    private void startCamera(){
        int rx = (faButton.getLeft()+faButton.getRight())/2;
        int ry = (faButton.getTop()+faButton.getBottom())/4;
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(faButton,"translationY",0,-ry);
        animator1.setDuration(125);
        Animator animator2 = ViewAnimationUtils.createCircularReveal(animator_layout,rx,ry,0,faButton.getBottom());
        animator2.setDuration(500);
        AnimatorSet set = new AnimatorSet();
        set.play(animator1).before(animator2);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                CameraActivity.startCameraActivity(MainActivity.this);
                overridePendingTransition(R.anim.anim_in,R.anim.anim_out);
                faButton.setTranslationY(0);
                hideStateBar();
                super.onAnimationEnd(animation);
            }
        });
        animator2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                animator_layout.getBackground().setAlpha(255);
                faButton.hide();
                super.onAnimationStart(animation);
            }
        });
        set.start();
    }

    @Override
    protected void onResume() {
        animator_layout.getBackground().setAlpha(0);
        faButton.show();
        showStateBar();
        super.onResume();
    }

    private void startFabAnimation(){
        ObjectAnimator animator = ObjectAnimator.ofFloat(faButton,"rotation",0,-20,20,0);
        animator.setDuration(200);
        animator.start();
    }

    private void hideStateBar(){
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    private void showStateBar(){
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    public void openDrawer(){
        drawer.openDrawer(GravityCompat.START);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mainHeadLayout:
                if(BmobUser.isLogin()){
                    Intent intent = new Intent(this, PersonalActivity.class);
                    startActivityForResult(intent,1);
                }else {
                    startActivity(new Intent(this, StartActivity.class));
                }
                break;
            case R.id.open_camera_activity:
                startCamera();
                break;
        }
    }

    @Override
    public void setHeader(@NotNull String str) {
        Glide.with(this).asDrawable().load(str).apply(options).into(header);
    }

    @Override
    public void setName(@NotNull String str) {
        name.setText(str);
    }

    @NotNull
    @Override
    public String getName(@NotNull String str) {
        return null;
    }

    private void checkCacheFile(){
        savePath = Environment.getExternalStorageDirectory().getAbsoluteFile()+ File.separator+"Mybitmap"+File.separator+"Cache"+File.separator;
        File file = new File(savePath);
        if(file.exists())
            file.mkdirs();
    }

    private void initLoadUserView(){
        if(BmobUser.isLogin()){
            user = BmobUser.getCurrentUser(User.class);
        }else{
            return;
        }

        SharedPreferences cachePre = MyUtil.getShardPreferences(this,"Cache");
        String fileName = cachePre.getString("header","[null]");
        File file = new File(savePath+fileName);
        if(file.exists()){
            setHeader(savePath+fileName);
        }else if(!"[null]".equals(fileName)){
            presenter.downloadToCache(user.getHeaderUri(),fileName,1);
        }

        fileName = cachePre.getString("background","");
        file = new File(savePath+fileName);
        if(file.exists()){
            setHeaderBackground(savePath+fileName);
        }else if(!"[null]".equals(fileName)){
            presenter.downloadToCache(user.getBackgroundUri(),fileName,2);
        }

        setName(user.getNickName());
    }

    @Override
    public void updateImageView(int id, @NotNull String path) {
        switch (id){
            case 1://侧边头像
                setHeader(path);
                break;
            case 2:
                setHeaderBackground(path);
        }
    }

    @Override
    public void setHeaderBackground(@NotNull String str) {
        Glide.with(this).asDrawable().apply(options).load(str).into(headBackground);
    }


}
