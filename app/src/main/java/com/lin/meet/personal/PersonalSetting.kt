package com.lin.meet.personal

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import cn.bmob.v3.BmobUser
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.lin.meet.R
import com.lin.meet.bean.User
import com.lin.meet.main.MainActivity
import com.lin.meet.my_util.MyUtil
import com.lljjcoder.Interface.OnCityItemClickListener
import com.lljjcoder.bean.CityBean
import com.lljjcoder.bean.DistrictBean
import com.lljjcoder.bean.ProvinceBean
import com.lljjcoder.citywheel.CityConfig
import com.lljjcoder.style.citypickerview.CityPickerView
import kotlinx.android.synthetic.main.activity_persetting.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class PersonalSetting : AppCompatActivity(), View.OnClickListener,PerStContract.View {
    override fun setUID(str: String) {
        perStUIDText.text = str
    }

    override fun getUID(): String {
        return perStUIDText.text.toString()
    }

    override fun updateData(str: String, id: Int) {
        when(id){
            1->{
                setName(str)
            }
            2->{
                setSex(str)
            }
            3->{
                setBirth(str)
            }
            4->{
                setWork(str)
            }
            5->{
                setEmail(str)
            }
            6->{
                setFrom(str)
            }
            0->{
                setUID(str)
            }
        }
    }

    override fun updateImageView(result: Int,path:String) {
        Glide.get(this).clearMemory()
        when(result){
            1->{
                setHeader(path)
            }
            2->{
                setBackground(path)
            }
        }
    }

    override fun closeProgressDialog() {
        if(progressDialog!=null)
            progressDialog!!.dismiss()
    }


    override fun createProgressDialog() {
        progressView = layoutInflater.inflate(R.layout.progress_view,null)
        uploadText = progressView!!.findViewById(R.id.cancel_upload)
        uploadText!!.setOnClickListener(this)
        var build:AlertDialog.Builder = AlertDialog.Builder(this)
        build.setCancelable(false)
        build.setView(progressView)
        progressDialog = build.create()
        progressDialog!!.show()
    }

    override fun toast(str:String) {
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show()
    }

    private var checkSave:Int = -1
    private var constellationList:ArrayList<String> = ArrayList()
    private var picker:CityPickerView = CityPickerView()
    private var isChange:Boolean = false
    private var option:RequestOptions ?= null
    private var progressView:View? = null
    private var progressDialog:AlertDialog? = null
    private val SETTING_BACKGROUND:Int = 10000
    private val SETTING_HEADER:Int = 10001
    private var checkEdit:Int = 0 //ck 1-6
    private var saveEdit = false
    private var inEdit = false
    private var isShortEdit = false
    private var persenter:PerStContract.Presenter? = null
    private var uploadText:TextView? =null
    override fun setHeader(url: String) {
        Glide.with(this).asDrawable().load(url).apply(option!!).into(perStHead)
    }

    override fun setBackground(url: String) {
        Glide.with(this).asDrawable().load(url).apply(option!!).into(perStBackground)
    }

    override fun setName(str: String) {
        perStNameText.text = str
    }

    override fun getName(): String {
        return perStNameText.text.toString()
    }

    override fun setSex(str: String) {
        perStSexText.text = str
    }

    override fun getSex(): String {
        return perStSexText.text.toString()
    }

    override fun getBirth():String {
        return perStBirthText.text.toString()
    }

    override fun setBirth(str: String) {
        var birth = str.split("/")
        if(birth.size>=3){
            var str_2 = birth.get(0)+"年"+birth.get(1)+"月"+birth.get(2)+"日"
            Log.d("错误 ",":"+str_2)
            perStBirthText.text = str_2
        }
    }

    override fun setWork(str: String) {
        perStWorkText.text = str
    }

    override fun getWork(): String {
        return perStWorkText.text.toString()
    }

    override fun setEmail(str: String) {
        perStEmailText.text = str
    }

    override fun getEmail(): String {
        return perStEmailText.text.toString()
    }


    override fun setFrom(str: String) {
        perStFromText.text = str
    }

    override fun getFrom(): String {
        return perStFromText.text.toString()
    }

    override fun showLongEdit(msg: String, id: Int) {
        isShortEdit = false
        inEdit = true
        checkSave = id
        stNormalLayout.visibility = View.GONE
        stLongEdit.setText("")
        stLongEdit.visibility = View.VISIBLE
        stLongEdit.hint = msg
        perStSave.visibility = View.VISIBLE
    }

    override fun showShortEdit(msg: String, id: Int) {
        isShortEdit = true
        inEdit = true
        checkSave = id
        stNormalLayout.visibility = View.GONE
        stShortEdit.visibility = View.VISIBLE
        stShortEdit.setText("")
        stShortEdit.hint = msg
        perStSave.visibility = View.VISIBLE
    }

    override fun onClick(v: View?) {
        if (!isChange) isChange = true
        when(v?.id){
            R.id.perStHeader->{
                openPhoto(SETTING_HEADER)
            }
            R.id.perStBg->{
                openPhoto(SETTING_BACKGROUND)
            }
            R.id.perStName->{
                showShortEdit("请输入您的昵称",1)
            }
            R.id.perStSex->{
                createSexDialog()
            }
            R.id.perStBoth->{
                createDatePickDialog()
            }
            R.id.perStWork->{
                showShortEdit("请输入您的职业",2)
            }
            R.id.perStEmail->{
                showShortEdit("请输入您的邮箱",3)
            }
            R.id.perStFrom->{
                picker.showCityPicker()
            }
            R.id.perStSignature->{
                showLongEdit("请输入您的个性签名",4)
            }
            R.id.perStIntroduce->{
                showLongEdit("请输入您的自我介绍",5)
            }
            R.id.perStSave->{
                saveEdit = true
                saveText()
            }
            R.id.cancel_upload->{
                closeProgressDialog()
                persenter!!.canelUpload()
            }
            R.id.perStUID->{
                if(perStUIDText.text.toString().equals("")){
                    showShortEdit("UID被设置后便无法再更改",0)
                }else
                    toast("无法再修改")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor=resources.getColor(R.color.camera_setting_color)
        setContentView(R.layout.activity_persetting)
        initView()
    }

    private fun initView(){
        setSupportActionBar(perStToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.mipmap.back_x)
        persenter = PerStPresenter(this)
        perStBg.setOnClickListener(this)
        perStEmail.setOnClickListener(this)
        perStFrom.setOnClickListener(this)
        perStHeader.setOnClickListener(this)
        perStIntroduce.setOnClickListener(this)
        perStName.setOnClickListener(this)
        perStSignature.setOnClickListener(this)
        perStSex.setOnClickListener(this)
        perStWork.setOnClickListener(this)
        perStSave.setOnClickListener(this)
        perStBoth.setOnClickListener(this)
        perStUID.setOnClickListener(this)

        initAreaPickDialog()
        option = RequestOptions()
        option!!.diskCacheStrategy(DiskCacheStrategy.NONE)
        option!!.skipMemoryCache(true)
        initLoadCache()
    }

    private fun initLoadCache(){
        var user:User? = null
        if(User.isLogin()){
            user = BmobUser.getCurrentUser(User::class.java)
        }
        else{
            toast("用户未登录")
            return
        }

        var cachePre:SharedPreferences = MyUtil.getShardPreferences(this,"Cache")

        var fileName:String = cachePre.getString("background","[null]");
        var cache:File = File(MainActivity.savePath+fileName)
        if(cache.exists())
            setBackground(MainActivity.savePath+fileName)
        else if ("[null]" != fileName)
            persenter!!.downloadToCache(user.backgroundUri,fileName,2)

        fileName = cachePre.getString("header","[null]");
        cache = File(MainActivity.savePath+fileName)
        if(cache.exists())
            setHeader(MainActivity.savePath+fileName)
        else if ("[null]" != fileName)
            persenter!!.downloadToCache(user.headerUri,fileName,1)

        setName(user.nickName)
        setSex(user.sex)
        setBirth(user.brith)
        setWork(user.work)
        setEmail(user.e_mail)
        setFrom(user.area)
        setUID(user.uid)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId==android.R.id.home) {
            if(inEdit){
                saveEditFinish()
            }else
                finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if(inEdit){
            saveEditFinish()
        }else
            super.onBackPressed()
    }

    private fun saveEditFinish(){
        if((stShortEdit.text.toString().length==0&&isShortEdit)
                ||(stLongEdit.text.toString().length==0&&!isShortEdit)
                ||saveEdit){
            finishSave()
        }else{
            AlertDialog.Builder(this)
                    .setMessage("确定保存更改？")
                    .setTitle("未保存")
                    .setPositiveButton("确定",object : DialogInterface.OnClickListener{
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            runOnUiThread(Runnable {
                                saveText()
                            })
                        }
                    })
                    .setNeutralButton("取消",object :DialogInterface.OnClickListener{
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            runOnUiThread(Runnable {
                                finishSave()
                            })
                        }
                    })
                    .create().show()
        }
    }

    private fun finishSave(){
        inEdit = false
        saveEdit = false
        stNormalLayout.visibility = View.VISIBLE
        perStSave.visibility = View.GONE
        stLongEdit.visibility = View.GONE
        stShortEdit.visibility = View.GONE
    }

    private fun openPhoto(requestCode:Int){
        var intent:Intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == SETTING_BACKGROUND&&data!=null){
            val uri: Uri = data.data as Uri
            var path:String = MyUtil.get_path_from_url(this,uri)
            var preferences:SharedPreferences = MyUtil.getShardPreferences(this,"LoginToken")
            persenter!!.updateBackground(preferences.getString("username",""),preferences.getString("token",""),path)
        }
        if(requestCode == SETTING_HEADER&&data!=null){
            val uri: Uri = data.data as Uri
            var path:String = MyUtil.get_path_from_url(this,uri)
            var preferences:SharedPreferences = MyUtil.getShardPreferences(this,"LoginToken")
            persenter!!.updateHeader(preferences.getString("username","[null]"),preferences.getString("token",""),path)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun finish() {
        if(isChange){
            setResult(1)
        }else
            setResult(0)
        super.finish()
    }

    private fun createDatePickDialog(){
        var calendar:Calendar = Calendar.getInstance()
        DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    var birth:String = year.toString() + "/" + month + "/" +dayOfMonth;
                    persenter!!.onSettingBirth(birth)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
    }

    private fun initAreaPickDialog(){
        picker.init(this)
        val cityConfig:CityConfig = CityConfig.Builder().build()
        picker.setConfig(cityConfig)
        picker.setOnCityItemClickListener(object :OnCityItemClickListener(){
            override fun onSelected(province: ProvinceBean?, city: CityBean?, district: DistrictBean?) {
                super.onSelected(province, city, district)
                val area:String = province.toString()+","+city.toString()+","+district.toString()
                persenter!!.onSettingArea(area)
            }

            override fun onCancel() {
                super.onCancel()
            }
        })
    }

    private fun initConstellationPicker(){
       constellationList.add("")
    }

    private fun createSexDialog(){
        var sexBuilder:AlertDialog.Builder = AlertDialog.Builder(this)
        sexBuilder.setSingleChoiceItems(arrayOf("秘密","男","女"),0,object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                when(which){
                    0->{
                        persenter!!.onSettingSex("秘密")
                    }
                    1->{
                        persenter!!.onSettingSex("男")
                    }
                    2->{
                        persenter!!.onSettingSex("女")
                    }
                }
                dialog!!.dismiss()
            }
        })
        sexBuilder.show()
    }

    private fun saveText(){
        when(checkSave){
            1->{//nickName
                persenter!!.onSettingNickName(stShortEdit.text.toString())
            }
            2->{//work
                persenter!!.onSettingWork(stShortEdit.text.toString())
            }
            3->{//Email
                persenter!!.onSettingEmail(stShortEdit.text.toString())
            }
            4->{//signature
                persenter!!.onSettingSignature(stLongEdit.text.toString())
            }
            5->{//introduce
                persenter!!.onSettingIntroduce(stLongEdit.text.toString())
            }
            0->{
                persenter!!.onSettingUID(stShortEdit.text.toString())
            }
        }
        finishSave()
    }


}
