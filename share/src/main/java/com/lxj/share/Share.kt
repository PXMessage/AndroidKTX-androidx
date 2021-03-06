package com.lxj.share

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.umeng.commonsdk.UMConfigure
import com.umeng.socialize.*
import com.umeng.socialize.bean.SHARE_MEDIA
import com.umeng.socialize.media.UMImage
import com.umeng.socialize.media.UMMin
import com.umeng.socialize.media.UMWeb


/**
 * Description:
 * Create by dance, at 2019/4/1
 */
object Share {
    fun init(context: Context, isDebug: Boolean, umengAppKey: String, umengMessageSecret: String? = null,
             wxAppId: String, wxAppKey: String,
             qqAppId: String = "", qqAppKey: String = "",
             weiboAppId: String = "", weiboAppKey: String = "", weiboCallbackUrl: String = ""
    ) {
        UMConfigure.setLogEnabled(isDebug)
        UMConfigure.init(context, umengAppKey, "Umeng", UMConfigure.DEVICE_TYPE_PHONE, umengMessageSecret)
        PlatformConfig.setWeixin(wxAppId, wxAppKey)
        PlatformConfig.setWXFileProvider("${context.packageName}.fileprovider")
        if (qqAppId.isNotEmpty()) {
            PlatformConfig.setQQZone(qqAppId, qqAppKey)
            PlatformConfig.setQQFileProvider("${context.packageName}.fileprovider")
        }
        if (weiboAppId.isNotEmpty()) PlatformConfig.setSinaWeibo(weiboAppId, weiboAppKey, weiboCallbackUrl)
    }

    fun wxLogin(activity: Activity, alwaysNeedConfirm: Boolean = false ,callback: ShareCallback? = null) {
        innerLogin(activity, SharePlatform.WxFriend, alwaysNeedConfirm = alwaysNeedConfirm, callback = callback)
    }

    fun qqLogin(activity: Activity, alwaysNeedConfirm: Boolean = false, callback: ShareCallback? = null) {
        innerLogin(activity, SharePlatform.QQ, alwaysNeedConfirm = alwaysNeedConfirm, callback = callback)
    }

    fun sinaLogin(activity: Activity, alwaysNeedConfirm: Boolean = false, callback: ShareCallback? = null) {
        innerLogin(activity, SharePlatform.Sina, alwaysNeedConfirm = alwaysNeedConfirm, callback = callback)
    }

    private fun innerLogin(activity: Activity, platform: SharePlatform, alwaysNeedConfirm: Boolean = false, callback: ShareCallback? = null){
        val innerPlatform = convertPlatform(platform)
        if(!checkInstall(activity, innerPlatform)) return
        if(alwaysNeedConfirm){
            val config = UMShareConfig()
            config.isNeedAuthOnGetUserInfo(true)
            UMShareAPI.get(activity).setShareConfig(config)
        }
        UMShareAPI.get(activity).getPlatformInfo(activity, innerPlatform, OauthCallback(callback))
    }

    private fun checkInstall(activity: Activity, platform: SHARE_MEDIA): Boolean{
        if(!UMShareAPI.get(activity).isInstall(activity, platform)){
            ToastUtils.showShort("?????????${getAppNameByPlatform(platform)}")
            return false
        }
        return true
    }

    /**
     * ????????????
     */
    fun shareWeb(activity: Activity, platform: SharePlatform, url: String, title: String,
                 thumbUrl: String? = null, thumbRes: Int? = null,
                 desc: String? = "", cb: ShareCallback? = null){
        val innerPlatform = convertPlatform(platform)
        if(!checkInstall(activity, innerPlatform)) return
        val web = UMWeb(url)
        web.title = title
        if(thumbUrl!=null) web.setThumb(UMImage(activity, thumbUrl))
        if(thumbRes!=null) web.setThumb(UMImage(activity, thumbRes))
        web.description = desc
        ShareAction(activity)
                .setPlatform(innerPlatform)
                .withMedia(web)
                .setCallback(InnerShareCallback(cb = cb))
                .share()
    }

    /**
     * ????????????
     *  imgUrl???bitmap???imgRes???????????????
     */
    fun shareImage(activity: Activity, platform: SharePlatform, imgUrl: String? = null,
                   bitmap: Bitmap? = null, imgRes: Int? = null, cb: ShareCallback? = null){
        val innerPlatform = convertPlatform(platform)
        if(!checkInstall(activity, innerPlatform)) return
        var image:UMImage? = null
        if(imgUrl!=null)image = UMImage(activity, imgUrl)
        if(bitmap!=null)image = UMImage(activity, bitmap)
        if(imgRes!=null)image = UMImage(activity, imgRes)
        if(image==null)return
        ShareAction(activity)
                .setPlatform(innerPlatform)
                .withMedia(image)
                .setCallback(InnerShareCallback(cb = cb))
                .share()
    }
    /**
     * ????????????
     */
    fun shareText(activity: Activity, platform: SharePlatform, text: String, cb: ShareCallback? = null){
        val innerPlatform = convertPlatform(platform)
        if(!checkInstall(activity, innerPlatform)) return
        ShareAction(activity)
                .setPlatform(innerPlatform)
                .withText(text)
                .setCallback(InnerShareCallback(cb = cb))
                .share()
    }


    /**
     * ?????????????????????
     * @param miniAppId ??????????????????id????????????????????????id???????????????????????????
     */
    fun shareMiniProgram(activity: Activity, url: String, bitmap: Bitmap? = null, imgRes: Int? = null, title: String, desc: String, path: String,
                         miniAppId: String, forTestVersion: Boolean = false,
                         forPreviewVersion: Boolean = false, cb: ShareCallback? = null) {
        if(!UMShareAPI.get(activity).isInstall(activity, SHARE_MEDIA.WEIXIN)){
            ToastUtils.showShort("???????????????")
            return
        }
        if (forTestVersion) {
            Config.setMiniTest()
        }
        if (forPreviewVersion) {
            Config.setMiniPreView()
        }
        val umMin = UMMin(url) //??????????????????????????????
        val img = if (bitmap == null) UMImage(activity, imgRes!!) else UMImage(activity, bitmap)
        umMin.setThumb(img) // ???????????????????????????
        umMin.title = title // ???????????????title
        umMin.description = desc // ?????????????????????
        umMin.path = path //?????????????????????
        umMin.userName = miniAppId // ???????????????id,?????????????????????
        ShareAction(activity)
                .withMedia(umMin)
                .setPlatform(SHARE_MEDIA.WEIXIN)
                .setCallback(InnerShareCallback(cb = cb))
                .share()
    }


    /**
     * ?????????????????????
     * @param miniAppId ??????????????????id????????????????????????id???????????????????????????
     */
    fun openMiniProgram(activity: Activity, appId: String, miniAppId: String,
                        path: String, forTestVersion: Boolean = false,
                        forPreviewVersion: Boolean = false) {
        if(!UMShareAPI.get(activity).isInstall(activity, SHARE_MEDIA.WEIXIN)){
            ToastUtils.showShort("???????????????")
            return
        }
        val api = WXAPIFactory.createWXAPI(activity, appId)
        val req: WXLaunchMiniProgram.Req = WXLaunchMiniProgram.Req()
        req.userName = miniAppId // ??????????????????id
        req.path = path ////??????????????????????????????????????????????????????????????????????????????????????????????????????????????? query ????????????????????????????????????????????? "?foo=bar"???
        if (forTestVersion) {
            req.miniprogramType = WXLaunchMiniProgram.Req.MINIPROGRAM_TYPE_TEST // ???????????? ?????????????????????????????????
        } else if (forPreviewVersion) {
            req.miniprogramType = WXLaunchMiniProgram.Req.MINIPROGRAM_TYPE_PREVIEW // ???????????? ?????????????????????????????????
        } else {
            req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE // ???????????? ?????????????????????????????????
        }
        api.sendReq(req)
    }

    fun deleteOauth(activity: Activity, platform: SharePlatform, callback: ShareCallback? = null) {
        UMShareAPI.get(activity).deleteOauth(activity, convertPlatform(platform), OauthCallback(callback))
    }

//    fun shareWithUI(
//            activity: Activity, platform: SharePlatform, bitmap: Bitmap? = null, text: String = "", url: String = "",
//            title: String = "", callback: ShareCallback? = null
//    ) {
//        checkPermission(activity) {
//            XPopup.Builder(activity).asCustom(SharePopup(activity)).show()
////            doShare(activity, platform, bitmap, text, url, title, callback)
//        }
//    }

    private fun getAppNameByPlatform(platform: SHARE_MEDIA) = when(platform){
        SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE -> "??????"
        SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE -> "QQ"
        SHARE_MEDIA.SINA -> "????????????"
        SHARE_MEDIA.ALIPAY -> "?????????"
        SHARE_MEDIA.DINGTALK -> "??????"
        SHARE_MEDIA.DOUBAN -> "??????"
        SHARE_MEDIA.DROPBOX -> "DROPBOX"
        SHARE_MEDIA.FACEBOOK, SHARE_MEDIA.FACEBOOK_MESSAGER -> "Facebook"
        SHARE_MEDIA.TWITTER -> "Twitter"
        SHARE_MEDIA.WHATSAPP -> "WhatsApp"
        SHARE_MEDIA.GOOGLEPLUS -> "GooglePlus"
        SHARE_MEDIA.EVERNOTE -> "Evernote"
        SHARE_MEDIA.INSTAGRAM -> "Instagram"
        SHARE_MEDIA.LINE -> "Line"
        SHARE_MEDIA.LINKEDIN -> "LinkedIn"
        else -> "??????App"
    }

    private fun convertPlatform(platform: SharePlatform) = when (platform) {
        SharePlatform.WxFriend -> SHARE_MEDIA.WEIXIN
        SharePlatform.WxCircle -> SHARE_MEDIA.WEIXIN_CIRCLE
        SharePlatform.QQ -> SHARE_MEDIA.QQ
        SharePlatform.QZone -> SHARE_MEDIA.QZONE
        SharePlatform.Sina -> SHARE_MEDIA.SINA
    }
    class OauthCallback(var cb: ShareCallback?) : UMAuthListener {
        override fun onComplete(p: SHARE_MEDIA, p1: Int, map: MutableMap<String, String>?) {
            LogUtils.d("UMAuthListener->onComplete???$p $map")
            cb?.onComplete(if (map == null) null else LoginData.fromMap(map))
        }
        override fun onError(p: SHARE_MEDIA, p1: Int, t: Throwable) {
            LogUtils.e("UMAuthListener->onError???$p ${t.message}")
            cb?.onError(t)
        }
        override fun onStart(p: SHARE_MEDIA) {
            LogUtils.d("UMAuthListener->onStart???$p")
            cb?.onStart()
        }
        override fun onCancel(p: SHARE_MEDIA, p1: Int) {
            LogUtils.d("UMAuthListener->onCancel???$p")
            cb?.onCancel()
        }
    }

    class InnerShareCallback(var cb: ShareCallback?) : UMShareListener{
        override fun onResult(p: SHARE_MEDIA) {
            LogUtils.d("share->onResult $p")
            cb?.onComplete()
        }
        override fun onCancel(p: SHARE_MEDIA) {
            LogUtils.d("share->onCancel $p")
            cb?.onCancel()
        }
        override fun onError(p: SHARE_MEDIA, t: Throwable) {
            LogUtils.e("share->onError $p  ${t.message}")
            cb?.onError(t)
        }
        override fun onStart(p: SHARE_MEDIA) {
            LogUtils.d("share->onStart $p")
            cb?.onStart()
        }
    }

    interface ShareCallback {
        fun onCancel() {}
        fun onStart() {}
        fun onError(t: Throwable) {}
        fun onComplete(loginData: LoginData? = null) {}
    }

    /**
     * ?????????????????????????????????
     */
    fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?){
        UMShareAPI.get(activity).onActivityResult(requestCode, resultCode, data)
    }
}