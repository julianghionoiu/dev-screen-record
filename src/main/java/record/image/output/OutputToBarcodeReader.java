package record.image.output;

import record.time.TimeSource;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class OutputToBarcodeReader implements ImageOutput {
    private TimeSource timeSource;
    private final List<TimestampPair> decodedBarcodes;

    public OutputToBarcodeReader(TimeSource timeSource) {
        this.timeSource = timeSource;
        decodedBarcodes = new ArrayList<>();
    }

    @Override
    public void open() {
    }

    @Override
    public BufferedImage getSuggestedOutputSample(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    }

    @Override
    public void writeImage(BufferedImage image) {
        long systemTimeNano = timeSource.currentTimeNano();
        long barcodeTimeNano = 0; //TODO read barcode
        decodedBarcodes.add(new TimestampPair(systemTimeNano, barcodeTimeNano));
    }

    @Override
    public void close() {
        //Nothing to close
    }

    public List<TimestampPair> getDecodedBarcodes() {
        return decodedBarcodes;
    }

    static class TimestampPair {
        Long systemTimestamp;
        Long barcodeTimestamp;

        TimestampPair(Long systemTimestamp, Long barcodeTimestamp) {
            this.systemTimestamp = systemTimestamp;
            this.barcodeTimestamp = barcodeTimestamp;
        }
    }
}
