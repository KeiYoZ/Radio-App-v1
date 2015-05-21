package ph.com.cherrymobile.radio;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.util.Log;

import java.util.HashMap;

public class MetaData {
    public String metaTitle;
    public String metaArtist;
    public String metaAlbum;
    public String metaGenre;
    public Bitmap metaAlbumImage;

    public MetaData(String metaURL) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        if (Build.VERSION.SDK_INT >= 14)
            retriever.setDataSource(metaURL, new HashMap < String, String > ());
        else
            retriever.setDataSource(metaURL);

        if (retriever != null) {

            metaTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            metaArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            metaAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            metaGenre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);

            byte[] art = retriever.getEmbeddedPicture();

            if (art != null)
                metaAlbumImage = BitmapFactory.decodeByteArray(art, 0, art.length);
        }
        retriever.release();
    }
}