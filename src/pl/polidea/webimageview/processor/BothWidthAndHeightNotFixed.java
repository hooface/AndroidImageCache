package pl.polidea.webimageview.processor;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.util.AttributeSet;
import pl.polidea.utils.DimensionCalculator;

/**
 * @author Mateusz Grzechociński <mateusz.grzechocinski@polidea.pl>
 */
class BothWidthAndHeightNotFixed extends AbstractBitmapProcessorCreationChain {

    private static final String ANDROID_SCHEMA = "http://schemas.android.com/apk/res/android";

    private static final String PARAM_LAYOUT_HEIGHT = "layout_height";

    private static final String PARAM_LAYOUT_WIDTH = "layout_width";

    private final Context context;

    private final AttributeSet attributeSet;

    private int height;

    private int width;

    public BothWidthAndHeightNotFixed(Context context, AttributeSet attributeSet) {
        this.context = context;
        this.attributeSet = attributeSet;
    }

    @Override
    public AbstractBitmapProcessorCreationChain next() {
        return new BothWidthAndHeightFixed(height, width);

    }

    @Override
    protected Processor create() {
        String layoutHeight = attributeSet.getAttributeValue(ANDROID_SCHEMA, PARAM_LAYOUT_HEIGHT);
        String layoutWidth = attributeSet.getAttributeValue(ANDROID_SCHEMA, PARAM_LAYOUT_WIDTH);

        height = guessValue(layoutHeight);
        width = guessValue(layoutWidth);

        if (height + width < 0) {
            return new Processor(Processor.ProcessorType.ORIGNAL);
        }
        return NOT_CREATED_PROCESSOR;
    }

    private int guessValue(final String value) {
        try {
            if ("match_parent".equals(value) || "fill_parent".equals(value)) {
                return MATCH_PARENT;
            } else if ("wrap_content".equals(value)) {
                return WRAP_CONTENT;
            } else {
                return DimensionCalculator.toRoundedPX(context, value);
            }
        } catch (final NumberFormatException e) {
            return MATCH_PARENT;
        }
    }
}
