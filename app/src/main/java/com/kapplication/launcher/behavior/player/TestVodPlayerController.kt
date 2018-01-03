package com.kapplication.launcher.behavior.player

import android.os.Bundle
import com.kapplication.launcher.CommonMessage
import com.kapplication.launcher.player.XulMediaPlayer
import com.starcor.xul.Prop.XulPropNameCache
import com.starcor.xul.Wrapper.XulMassiveAreaWrapper
import com.starcor.xul.XulArea
import com.starcor.xul.XulDataNode
import com.starcor.xul.XulView
import com.starcor.xulapp.message.XulSubscriber
import com.starcor.xulapp.model.XulDataCallback
import com.starcor.xulapp.model.XulDataService
import com.starcor.xulapp.utils.XulLog
import java.util.*

/**
 * Created by hy on 2015/11/4.
 */
class TestVodPlayerController(mp: Player) : TestBasePlayerController(mp) {
    internal var _mediaInfo: XulDataNode? = null
    internal var _mediaId: String? = null
    internal var _playlist = HashMap<String, VideoItem>()

    override val uiComponents: Array<PlayerController.UiComponentInfo>
        get() = defaultUiComponents

    protected override val currentMediaId: String
        get() {
            val mediaInfo = _mediaInfo
            if (mediaInfo == null) {
                val mediaId = DataModelUtils.parseMediaId(_mediaId)
                return DataModelUtils.buildMediaId(mediaId.videoId, mediaId.videoType)
            }
            return mediaInfo.getChildNodeValue("id")
        }

    override fun init(behaviorParameters: Bundle) {
        val xulDataService = dataService
        val mediaId = behaviorParameters.getString("mediaId")
        doPlay(mediaId)

        xulDataService.query(TestProvider.DP_MEDIAINFO)
                .where(TestProvider.DK_MEDIA_ID).`is`(mediaId)
                .pull(object : XulDataCallback() {
                    override fun onResult(clause: XulDataService.Clause, code: Int, data: XulDataNode) {
                        _mediaInfo = data

                        xulDataService.insert(TestProvider.DP_USER_PRIVATE_DATA)
                                .where(TestProvider.DK_TYPE).`is`(TestProvider.DKV_TYPE_HISTORY)
                                .where("id").`is`(currentMediaId)
                                .value(_mediaInfo)
                                .exec(null)

                        syncPlayList(mediaId)
                        syncMediaTitle()
                        syncMediaSubTitle(mediaId)
                        super.onResult(clause, code, data)
                    }
                })
    }

    override fun doAction(view: XulView, action: String, type: String, command: String, userdata: Any): Boolean {
        if ("click" == action && "PlayVideo" == command) {
            val xulDataNode = view.bindingData!![0]
            val mediaId = xulDataNode.getChildNodeValue("id")
            doPlay(mediaId)
            syncMediaSubTitle(mediaId)
            return true
        }
        return super.doAction(view, action, type, command, userdata)
    }

    override fun doPlay(mediaId: String?) {
        val mediaPlayer = mediaPlayer
        mediaPlayer!!.stop()
        _mediaId = mediaId
        _mp.isMediaRunning = false

        fireEventPrePlay(mediaId, "vod")
        val xulDataService = dataService
        xulDataService.query(TestProvider.DP_PURCHASE)
                .where(TestProvider.DK_ACTION).`is`(TestProvider.DKV_ACT_AUTH_PLAY)
                .where(TestProvider.DK_MEDIA_ID).`is`(mediaId)
                .exec(object : XulDataCallback() {
                    override fun onResult(clause: XulDataService.Clause, code: Int, data: XulDataNode) {
                        if (_mediaId != mediaId) {
                            // cancelled result
                            return
                        }
                        val errCode = data.getChildNodeValue("code")

                        if ("0" != errCode) {
                            _mp.showErrorDialog("error", data.getChildNodeValue("info") + " - " + errCode)
                            return
                        }

                        val playUrl = data.getChildNodeValue("url")

                        _mp.setProgress(0f)
                        mediaPlayer.open(playUrl)
                        mediaPlayer.play()
                        fireEventPostPlay(playUrl)
                    }

                    override fun onError(clause: XulDataService.Clause, code: Int) {
                        if (_mediaId != mediaId) {
                            // cancelled result
                            return
                        }
                        _mp.showErrorDialog("error", clause.message + " - " + code)
                    }
                })
    }

    override fun onComplete(xmp: XulMediaPlayer): Boolean {
        if (_mp.isMediaRunning) {
            super.onComplete(xmp)
            _mp.buildMessage()
                    .setTag(CommonMessage.EVENT_PLAYER_NEXT_MEDIA)
                    .post()
        } else {
            super.onComplete(xmp)
            XulLog.e(TAG, "invalid onComplete event!!!")
            _mp.showErrorDialog("error", "player error: invalid onComplete event")
        }
        return true
    }

    private fun syncMediaSubTitle(mediaId: String?) {
        val titleFrame = _mp.getUiComponent(Player.UI_TITLE_FRAME) as XulArea
        val mediaSubTitle = titleFrame.ownerPage.findItemById(titleFrame, "media-sub-title")

        val videoItem = _playlist[mediaId] ?: return

        val videoNode = videoItem.info
        val subName = videoNode.getChildNodeValue("name")
        mediaSubTitle.setAttr(XulPropNameCache.TagId.TEXT, subName)
        mediaSubTitle.resetRender()
    }

    private fun syncMediaTitle() {
        val name = _mediaInfo!!.getChildNodeValue("name")
        val titleFrame = _mp.getUiComponent(Player.UI_TITLE_FRAME) as XulArea
        val mediaTitle = titleFrame.ownerPage.findItemById(titleFrame, "media-title")
        mediaTitle.setAttr(XulPropNameCache.TagId.TEXT, name)
        mediaTitle.resetRender()
    }

    private fun syncPlayList(mediaId: String?) {
        val playListFrame = _mp.getUiComponent(Player.UI_PLAY_LIST) as XulArea
        val playListView = playListFrame.ownerPage.findItemById(playListFrame, "playlist")
        val playerListMassive = XulMassiveAreaWrapper.fromXulView(playListView)

        var playlist = _mediaInfo!!.getChildNode("playlist")
        while (playlist != null && "main" != playlist.getAttributeValue("type")) {
            playlist = playlist.getNext("playlist")
        }
        if (playlist != null) {
            var videoNode: XulDataNode? = playlist.firstChild
            var idx = 0
            while (videoNode != null) {
                val id = videoNode.getChildNodeValue("id")
                _playlist.put(id, VideoItem(idx, videoNode))
                playerListMassive.addItem(videoNode)
                videoNode = videoNode.next
                ++idx
            }
            playerListMassive.syncContentView()
        }
        syncPlayListFocus(mediaId)
    }

    private fun syncPlayListFocus(mediaId: String?) {
        val playListFrame = _mp.getUiComponent(Player.UI_PLAY_LIST) as XulArea
        val playListView = playListFrame.ownerPage.findItemById(playListFrame, "playlist")
        val playerListMassive = XulMassiveAreaWrapper.fromXulView(playListView)

        val videoItem = _playlist[mediaId] ?: return

        val idx = videoItem.idx
        val slider = playListView.findParentByType("slider")
        playerListMassive.makeChildVisible(slider, idx, false) {
            val itemView = playerListMassive.getItemView(idx)
            itemView?.rootLayout?.requestFocus(itemView)
        }
    }

    @XulSubscriber(tag = CommonMessage.EVENT_PLAYER_NEXT_MEDIA)
    fun playNextMedia(obj: Any) {
        val videoItem = _playlist[_mediaId]
        if (videoItem != null) {
            var videoNode: XulDataNode? = videoItem.info
            videoNode = videoNode!!.next
            if (videoNode != null) {
                val mediaId = videoNode.getChildNodeValue("id")
                doPlay(mediaId)
                syncMediaSubTitle(mediaId)
                syncPlayListFocus(mediaId)
                return
            }
        }
        // no next media
        _mp.buildMessage()
                .setTag(CommonMessage.EVENT_PLAYER_PLAYLIST_FINISHED)
                .post()
    }

    class VideoItem(val idx: Int, val info: XulDataNode)

    companion object {
        private val TAG = TestVodPlayerController::class.java.simpleName
        private val defaultUiComponents = arrayOf(PlayerController.UiComponentInfo(Player.UI_TITLE_FRAME, "media-player-title-frame", "media_player_default_ui.xml"), PlayerController.UiComponentInfo(Player.UI_CONTROL_BAR, "media-player-control-bar", "media_player_default_ui.xml"), PlayerController.UiComponentInfo(Player.UI_MENU, "media-player-menu", "media_player_default_ui.xml"), PlayerController.UiComponentInfo(Player.UI_PLAY_LIST, "media-player-playlist", "media_player_default_ui.xml"))
    }
}
