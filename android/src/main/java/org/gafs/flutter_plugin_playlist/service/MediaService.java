package org.gafs.flutter_plugin_playlist.service;

import com.devbrackets.android.playlistcore.api.MediaPlayerApi;
import com.devbrackets.android.playlistcore.components.playlisthandler.PlaylistHandler;
import com.devbrackets.android.playlistcore.service.BasePlaylistService;

import org.gafs.flutter_plugin_playlist.data.AudioTrack;
import org.gafs.flutter_plugin_playlist.manager.PlaylistManager;
import org.gafs.flutter_plugin_playlist.playlist.AudioApi;
import org.gafs.flutter_plugin_playlist.playlist.AudioPlaylistHandler;

/**
 * A simple service that extends {@link BasePlaylistService} in order to provide
 * the application specific information required.
 */
public class MediaService extends BasePlaylistService<AudioTrack, PlaylistManager> {

    @Override
    public void onCreate() {
        super.onCreate();

        // Adds the audio player implementation, otherwise there's nothing to play media with
        AudioApi newAudio = new AudioApi(getApplicationContext());
        newAudio.addErrorListener(getPlaylistManager());

        if(getPlaylistManager() != null) {
            getPlaylistManager().getMediaPlayers().add(newAudio);
            getPlaylistManager().onMediaServiceInit(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(getPlaylistManager() != null) {
            // Releases and clears all the MediaPlayersMediaImageProvider
            for (MediaPlayerApi<AudioTrack> player : getPlaylistManager().getMediaPlayers()) {
                player.release();
            }

            getPlaylistManager().getMediaPlayers().clear();
        }
    }

    @Override
    protected PlaylistManager getPlaylistManager() {
        return PlaylistManager.getInstance();
    }

    @Override
    public PlaylistHandler<AudioTrack> newPlaylistHandler() {
        MediaImageProvider imageProvider = new MediaImageProvider(getApplicationContext(), new MediaImageProvider.OnImageUpdatedListener() {
            @Override
            public void onImageUpdated() {
                getPlaylistHandler().updateMediaControls();
            }
        });

        AudioPlaylistHandler.Listener<AudioTrack> listener = new AudioPlaylistHandler.Listener<AudioTrack>() {
            @Override
            public void onMediaPlayerChanged(MediaPlayerApi<AudioTrack> oldPlayer, MediaPlayerApi<AudioTrack> newPlayer) {
                getPlaylistManager().onMediaPlayerChanged(newPlayer);
            }

            @Override
            public void onItemSkipped(AudioTrack item) {
                // We don't need to do anything with this right now
                // The PluginManager receives notifications of the current item changes.
            }
        };

        return new AudioPlaylistHandler.Builder<>(
                getApplicationContext(),
                getClass(),
                getPlaylistManager(),
                imageProvider,
                listener
        ).build();
    }
}
