package pl.polidea.webimageview;

import java.io.File;
import java.net.URL;

import pl.polidea.imagecache.ImageCache;
import pl.polidea.imagecache.OnCacheResultListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 
 * @author Marek Multarzynski
 * 
 */
public class WebImageView extends ImageView {

    private static ImageCache imageCache;
    private static WebClient webClient;
    private BitmapProcessor bitmapProcessor;
    private String path;
    AttributeSet attrs;
    String layout_height;
    String layout_width;

    public WebImageView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        this.attrs = attrs;
        init(context, attrs);
    }

    public WebImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.attrs = attrs;
        init(context, attrs);
    }

    public WebImageView(final Context context) {
        super(context);
        init(context, null);
    }

    private synchronized void init(final Context context, final AttributeSet attrsSet) {

        imageCache = getCache(context);
        webClient = getWebClient(context);
        bitmapProcessor = new DefaultBitmapProcessor(this);
        this.attrs = attrsSet;
        if (attrsSet != null) {
            layout_height = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "layout_height");
            layout_width = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "layout_width");
        }
    }

    public static ImageCache getCache(final Context context) {
        return imageCache == null ? new ImageCache(context) : imageCache;
    }

    public WebClient getWebClient(final Context context) {
        return webClient == null ? new WebClient(context) : webClient;
    }

    /**
     * Sets the content of this WebImageView to the specified path.
     * 
     * @param path
     *            The path of an image
     */
    public void setImageURL(final String path) {
        this.path = path;
        if (path == null) {
            return;
        }
        imageCache.get(path, new OnCacheResultListener() {

            @Override
            public void onCacheMiss(final String key) {

                webClient.requestForImage(path, new WebCallback() {

                    @Override
                    public void onWebMiss(final String path) {
                        // N/A
                    }

                    @Override
                    public void onWebHit(final String path, final File file) {
                        final Bitmap bmp = bitmapProcessor.process(file);
                        imageCache.put(path, bmp);
                        setBitmap(path, bmp);
                    }
                });
            }

            @Override
            public void onCacheHit(final String key, final Bitmap bitmap) {
                setBitmap(key, bitmap);
            }
        });
    }

    private void setBitmap(final String key, final Bitmap bitmap) {
        if (key.equals(path)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    setImageBitmap(bitmap);
                }
            });
        }
    }

    /**
     * Sets the content of this WebImageView to the specified URL.
     * 
     * @param url
     *            The Uri of an image
     */
    public void setImageURL(final URL url) {
        if (url == null) {
            return;
        }
        setImageURL(url.getPath());
    }

    public BitmapProcessor getBitmapProcessor() {
        return bitmapProcessor;
    }

    public void setBitmapProcessor(final BitmapProcessor bitmapProcessor) {
        this.bitmapProcessor = bitmapProcessor;
    }

    public static interface BitmapProcessor {
        Bitmap process(File pathToBitmap);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
    }

    public void disableBitmapProcessor() {
        bitmapProcessor = new BitmapProcessor() {

            @Override
            public Bitmap process(final File pathToBitmap) {
                Bitmap bmp = null;

                try {
                    bmp = BitmapFactory.decodeFile(pathToBitmap.getPath());
                } catch (final OutOfMemoryError e) {

                }
                return bmp;
            }
        };
    }

    public void enableDefaultBitmapProcessor() {
        bitmapProcessor = new DefaultBitmapProcessor(this);
    }

}
