package com.brownik.newmediaplayer.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.brownik.newmediaplayer.R
import com.brownik.newmediaplayer.userinterface.MediaPlayerActivity
import com.brownik.newmediaplayer.userinterface.MyObject
import androidx.media.app.NotificationCompat.MediaStyle

class MediaNotificationManager(service: MediaPlayerService) {

    private val mediaNotificationManager: NotificationManager =
        service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationId = 512
    private val channelId = "${R.string.app_name}"
    private val _service = service

    // 미디어 버튼 action
    private val playAction: NotificationCompat.Action =
        NotificationCompat.Action(
            R.drawable.ic_play,
            service.getString(R.string.label_play),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                _service,
                PlaybackStateCompat.ACTION_PLAY
            )
        )
    private val pauseAction: NotificationCompat.Action =
        NotificationCompat.Action(
            R.drawable.ic_pause,
            service.getString(R.string.label_pause),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                _service,
                PlaybackStateCompat.ACTION_PAUSE
            )
        )
    private val nextAction: NotificationCompat.Action =
        NotificationCompat.Action(
            R.drawable.ic_next,
            service.getString(R.string.label_next),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                _service,
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            )
        )
    private val prevAction: NotificationCompat.Action =
        NotificationCompat.Action(
            R.drawable.ic_previous,
            service.getString(R.string.label_previous),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                _service,
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
        )
    private val stopAction: NotificationCompat.Action =
        NotificationCompat.Action(
            R.drawable.btn_close,
            service.getString(R.string.label_stop),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                _service,
                PlaybackStateCompat.ACTION_STOP
            )
        )

    init {
        mediaNotificationManager.cancel(notificationId)
    }

    fun getNotificationId() = notificationId

    fun getNotification(
        metadata: MediaMetadataCompat?,
        state: PlaybackStateCompat,
        token: MediaSessionCompat.Token,
    ): NotificationCompat.Builder {
        val isPlaying = state.state == PlaybackStateCompat.STATE_PLAYING
        val builder = getNotificationBuilder(metadata!!.description, token)
        builder.addAction(prevAction)
        builder.addAction(if (isPlaying) pauseAction else playAction)
        builder.addAction(nextAction)

        return builder
    }

    private fun getNotificationBuilder(
        description: MediaDescriptionCompat,
        token: MediaSessionCompat.Token,
    ): NotificationCompat.Builder =
        NotificationCompat.Builder(_service, channelId)
            .setSmallIcon(R.drawable.basic_img)
            .setColor(ContextCompat.getColor(_service, R.color.black))
            .setStyle(
                MediaStyle()
                    .setMediaSession(token)
                    .setShowActionsInCompactView(0, 1, 2)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            _service,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )
            .setContentIntent(createPendingIntent())
            .setContentTitle(description.title)
            .setContentText(description.subtitle)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(0)
            .setAutoCancel(false)

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun createPendingIntent(): PendingIntent? {
        val openUI = Intent(_service, MediaPlayerActivity::class.java)
        openUI.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(
            _service,
            0,
            openUI,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    // NotificationChannel 생성
    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel() {
        if (mediaNotificationManager.getNotificationChannel(channelId) == null) {
            MyObject.makeLog("MediaNotificationManager.createChannel.NewChannel")
            val name: CharSequence = "MediaSession"
            val description = "MediaPlayer"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            channel.enableLights(true)
            mediaNotificationManager.createNotificationChannel(channel)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        } else {
            MyObject.makeLog("MediaNotificationManager.createChannel.ReuseChannel")
        }
    }
}