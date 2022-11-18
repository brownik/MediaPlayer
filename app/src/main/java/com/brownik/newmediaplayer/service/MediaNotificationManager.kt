package com.brownik.newmediaplayer.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.brownik.newmediaplayer.R
import com.brownik.newmediaplayer.userinterface.MediaPlayerActivity

class MediaNotificationManager(service: MediaPlayerService) {

    val mediaNotificationManager: NotificationManager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val _service = service
    // 미디어 버튼 action
    private val mPlayAction: NotificationCompat.Action =
        NotificationCompat.Action(
            R.drawable.btn_play,
            service.getString(R.string.label_play),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                service,
                PlaybackStateCompat.ACTION_PLAY
            )
        )
    private val mPauseAction: NotificationCompat.Action =
        NotificationCompat.Action(
            R.drawable.btn_pause,
            service.getString(R.string.label_pause),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                service,
                PlaybackStateCompat.ACTION_PAUSE
            )
        )
    private val mNextAction: NotificationCompat.Action =
        NotificationCompat.Action(
            R.drawable.btn_next,
            service.getString(R.string.label_next),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                service,
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            )
        )
    private val mPrevAction: NotificationCompat.Action =
        NotificationCompat.Action(
            R.drawable.btn_previous,
            service.getString(R.string.label_previous),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                service,
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
        )
    private val mStopAction: NotificationCompat.Action =
        NotificationCompat.Action(
            R.drawable.btn_close,
            service.getString(R.string.label_stop),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                service,
                PlaybackStateCompat.ACTION_STOP
            )
        )

    fun getNotificationManager(): NotificationManager {
        return mediaNotificationManager
    }

    fun getNotification(
        metadata: MediaMetadataCompat?,
        state: PlaybackStateCompat,
        token: MediaSessionCompat.Token
    ) : Notification {
        val isPlaying = state.state == PlaybackStateCompat.STATE_PLAYING
        val builder = getMediaNotificationBuilder(metadata!!.description, token)
        if ((state.actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0L) builder.addAction(mPrevAction)
        builder.addAction(if (isPlaying) mPauseAction else mPlayAction)
        if ((state.actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0L) builder.addAction(mNextAction)
        if ((state.actions and PlaybackStateCompat.ACTION_STOP) != 0L)  builder.addAction(mStopAction)

        return builder.build()
    }

    private fun getMediaNotificationBuilder(description: MediaDescriptionCompat, token: MediaSessionCompat.Token): NotificationCompat.Builder =
        NotificationCompat.Builder(_service, "123")
            .setSmallIcon(R.drawable.basic_img)
            .setColor(ContextCompat.getColor(_service, R.color.white))
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(token).setShowActionsInCompactView(0, 1, 2, 3))
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
            501,
            openUI,
            PendingIntent.FLAG_CANCEL_CURRENT)
    }
}